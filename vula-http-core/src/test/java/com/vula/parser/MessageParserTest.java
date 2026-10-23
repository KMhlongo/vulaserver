package com.vula.parser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.vula.exception.MalformedRequestException;

public class MessageParserTest {

    private MessageParser messageParser = new MessageParser();

    @Test
    public void readsSmallBodyThatArrivedWithTheHeaders() throws Exception {
        String body = "{\"name\":\"vula\"}";
        InputStream in = chunks("Content-Length: 15\r\n" + body);

        messageParser.readLine(in);

        assertArrayEquals(bytes(body), messageParser.read(in, 15));
    }

    /**
     * A body larger than the buffer needs several reads, and the last read also holds bytes
     * that are not part of the body. The body must be complete and stop at the content length.
     */
    @Test
    public void readsBodySpanningSeveralReadsAndStopsAtContentLength() throws Exception {
        InputStream in = chunks("Content-Length: 10000\r\n", "x".repeat(8192), "x".repeat(1808) + "EXTRA");

        messageParser.readLine(in);

        assertArrayEquals(bytes("x".repeat(10000)), messageParser.read(in, 10000));
    }

    /**
     * A line ending can be split by the network, with the carriage return at the end of one
     * read and the newline at the start of the next. Both lines must still be read correctly.
     * Lines are returned with their trailing carriage return, which callers strip.
     */
    @Test
    public void readsLinesWhenLineEndingIsSplitAcrossReads() throws Exception {
        InputStream in = chunks("Host: localhost\r", "\nAccept: */*\r\n");

        assertEquals("Host: localhost\r", string(messageParser.readLine(in)));
        assertEquals("Accept: */*\r", string(messageParser.readLine(in)));
    }

    /** A client that connects and closes without sending anything is not an error. */
    @Test
    public void emptyConnectionParsesToNull() throws Exception {
        assertNull(messageParser.parseMessage(chunks()));
    }

    /** A request cut off partway through the headers is incomplete, so it is rejected. */
    @Test
    public void connectionClosedBeforeEndOfHeadersIsMalformed() {
        InputStream in = chunks("GET /hello HTTP/1.1\r\n", "Host: localhost\r\n");

        assertThrows(MalformedRequestException.class, () -> messageParser.parseMessage(in));
    }

    /** Returns each part from its own read() call, like separate TCP packets. */
    private static InputStream chunks(String... parts) {
        return new InputStream() {
            private int part = 0;
            private int offset = 0;

            @Override
            public int read() {
                throw new UnsupportedOperationException("The parser should read into a buffer");
            }

            @Override
            public int read(byte[] b, int off, int len) {
                if (part >= parts.length) return -1;
                byte[] current = bytes(parts[part]);
                int n = Math.min(len, current.length - offset);
                System.arraycopy(current, offset, b, off, n);
                offset += n;
                if (offset == current.length) {
                    part++;
                    offset = 0;
                }
                return n;
            }
        };
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static String string(byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
    }
}
