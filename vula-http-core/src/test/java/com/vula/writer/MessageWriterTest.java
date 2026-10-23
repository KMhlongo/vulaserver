package com.vula.writer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.vula.constants.HttpVersion;
import com.vula.constants.StatusCode;
import com.vula.exception.MalformedRequestException;
import com.vula.model.Headers;
import com.vula.model.HttpResponseMessage;
import com.vula.model.StatusLine;

public class MessageWriterTest {

    private final MessageWriter writer = new MessageWriter();
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    public void writesStatusLineHeadersAndBodyWithCrlf() {
        byte[] body = bytes("\"hello\"");
        Headers headers = new Headers();
        headers.addHeader("Content-Length", String.valueOf(body.length));
        headers.addHeader("Content-Type", "application/json");

        writer.writeMessage(response(StatusCode.OK, headers, body), out);

        Written written = Written.parse(out.toByteArray());
        assertEquals("HTTP/1.1 200", written.statusLine);
        assertEquals(Set.of("content-length: 7", "content-type: application/json"), written.headerLines);
        assertArrayEquals(body, written.body, "body must be exactly the content length, with nothing appended");
    }

    @Test
    public void writesBlankLineEvenWithoutBody() {
        Headers headers = new Headers();
        headers.addHeader("Connection", "close");

        writer.writeMessage(response(StatusCode.NOT_FOUND, headers, null), out);

        assertEquals("HTTP/1.1 404\r\nconnection: close\r\n\r\n", string(out.toByteArray()));
    }

    @Test
    public void errorContentLengthCountsBytesNotCharacters() {
        // 'é' is one character but two bytes in UTF-8
        String message = "Invalid header: café";

        writer.writeError(StatusCode.BAD_REQUEST, new MalformedRequestException(message), out);

        Written written = Written.parse(out.toByteArray());
        byte[] expectedBody = message.getBytes(StandardCharsets.UTF_8);
        assertEquals("HTTP/1.1 400", written.statusLine);
        assertTrue(written.headerLines.contains("content-length: " + expectedBody.length), written.headerLines.toString());
        assertTrue(written.headerLines.contains("connection: close"), written.headerLines.toString());
        assertArrayEquals(expectedBody, written.body);
    }

    @Test
    public void errorWithoutMessageIsStillAValidResponse() {
        writer.writeError(StatusCode.BAD_REQUEST, new RuntimeException(), out);

        Written written = Written.parse(out.toByteArray());
        assertEquals("HTTP/1.1 400", written.statusLine);
        assertTrue(written.headerLines.contains("connection: close"), written.headerLines.toString());
        assertEquals(0, written.body.length);
    }

    private static HttpResponseMessage response(StatusCode status, Headers headers, byte[] body) {
        StatusLine statusLine = new StatusLine();
        statusLine.setVersion(HttpVersion.HTTP_VERSION_1);
        statusLine.setStatusCode(status);
        HttpResponseMessage response = new HttpResponseMessage();
        response.setStatusLine(statusLine);
        response.setHeaders(headers);
        response.setBody(body);
        return response;
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static String string(byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
    }

    /**
     * Splits raw response bytes into status line, header lines and body. Header order is not
     * checked, because headers are stored in a hash map. Fails if any line in the head ends with
     * a bare newline instead of CRLF.
     */
    static final class Written {
        final String statusLine;
        final Set<String> headerLines;
        final byte[] body;

        private Written(String statusLine, Set<String> headerLines, byte[] body) {
            this.statusLine = statusLine;
            this.headerLines = headerLines;
            this.body = body;
        }

        static Written parse(byte[] raw) {
            String all = new String(raw, StandardCharsets.ISO_8859_1);   // one char per byte
            int end = all.indexOf("\r\n\r\n");
            assertTrue(end >= 0, "response has no blank line ending the headers: " + all);

            String head = all.substring(0, end);
            assertFalse(head.replace("\r\n", "").contains("\n"), "a header line ends with a bare newline");
            assertFalse(head.replace("\r\n", "").contains("\r"), "a header line ends with a bare carriage return");

            String[] lines = head.split("\r\n");
            Set<String> headerLines = new HashSet<>(Arrays.asList(lines).subList(1, lines.length));
            byte[] body = Arrays.copyOfRange(raw, end + 4, raw.length);
            return new Written(lines[0], headerLines, body);
        }
    }
}
