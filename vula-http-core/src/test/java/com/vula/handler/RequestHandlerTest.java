package com.vula.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.vula.constants.HttpVersion;
import com.vula.constants.StatusCode;
import com.vula.model.Headers;
import com.vula.model.StatusLine;

/**
 * Calls RequestHandler.handle with an in-memory request and checks the raw bytes written back.
 * The HttpHandler is a lambda, so these tests cover only the core module.
 */
public class RequestHandlerTest {

    /** Replies 200 with a fixed JSON body, whatever the request. */
    private static final HttpHandler HELLO = (request, response) -> {
        byte[] body = "\"hello\"".getBytes(StandardCharsets.UTF_8);
        Headers headers = new Headers();
        headers.addHeader("Content-Length", String.valueOf(body.length));
        headers.addHeader("Content-Type", "application/json");
        response.setStatusLine(statusLine(StatusCode.OK));
        response.setHeaders(headers);
        response.setBody(body);
    };

    @Test
    public void validRequestGetsTheHandlersResponse() {
        String response = handle("GET /hello HTTP/1.1\r\nHost: localhost\r\n\r\n", HELLO);

        assertTrue(response.startsWith("HTTP/1.1 200\r\n"), response);
        assertTrue(response.contains("\r\ncontent-length: 7\r\n"), response);
        assertTrue(response.endsWith("\r\n\r\n\"hello\""), "body must follow the blank line with nothing after it: " + response);
    }

    @Test
    public void malformedRequestLineGets400AndSkipsTheHandler() {
        AtomicBoolean handlerCalled = new AtomicBoolean(false);

        String response = handle("GARBAGE\r\n\r\n", (request, resp) -> handlerCalled.set(true));

        assertTrue(response.startsWith("HTTP/1.1 400\r\n"), response);
        assertFalse(handlerCalled.get(), "the handler must not see a request that failed to parse");
    }

    @Test
    public void headerWithoutColonGets400() {
        String response = handle("GET /hello HTTP/1.1\r\nNoColonHere\r\n\r\n", HELLO);

        assertTrue(response.startsWith("HTTP/1.1 400\r\n"), response);
    }

    @Test
    public void unknownMethodGets501() {
        String response = handle("FOO /hello HTTP/1.1\r\n\r\n", HELLO);

        assertTrue(response.startsWith("HTTP/1.1 501\r\n"), response);
    }

    @Test
    public void connectionClosedBeforeEndOfHeadersGets400() {
        String response = handle("GET /hello HTTP/1.1\r\nHost: localhost\r\n", HELLO);

        assertTrue(response.startsWith("HTTP/1.1 400\r\n"), response);
    }

    /** Browsers open spare connections and close them without sending anything. */
    @Test
    public void emptyConnectionGetsNoResponse() {
        assertEquals("", handle("", HELLO));
    }

    /** A request cut off partway through a line means the client is gone, so nothing is sent. */
    @Test
    public void connectionClosedMidLineGetsNoResponse() {
        assertEquals("", handle("GET /hel", HELLO));
    }

    @Test
    public void handlerExceptionGets500WithoutLeakingTheMessage() {
        String response = handle("GET /hello HTTP/1.1\r\n\r\n", (request, resp) -> {
            throw new IllegalStateException("secret internal detail");
        });

        assertTrue(response.startsWith("HTTP/1.1 500\r\n"), response);
        assertTrue(response.endsWith("\r\n\r\nInternal Server Error"), response);
        assertFalse(response.contains("secret internal detail"), "the exception message must not reach the client");
    }

    /**
     * A response with no headers makes the writer fail after the status line is already sent.
     * A 500 must not be appended to the half-written response.
     */
    @Test
    public void failureWhileWritingDoesNotAppendA500() {
        String response = handle("GET /hello HTTP/1.1\r\n\r\n",
                (request, resp) -> resp.setStatusLine(statusLine(StatusCode.OK)));

        assertFalse(response.contains("500"), "a 500 was appended to a half-written response: " + response);
    }

    private static String handle(String rawRequest, HttpHandler httpHandler) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // handle() works only on the streams, so no socket is needed
        new RequestHandler(null, httpHandler).handle(
                new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.ISO_8859_1)), out);
        return out.toString(StandardCharsets.ISO_8859_1);
    }

    private static StatusLine statusLine(StatusCode code) {
        StatusLine statusLine = new StatusLine();
        statusLine.setVersion(HttpVersion.HTTP_VERSION_1);
        statusLine.setStatusCode(code);
        return statusLine;
    }
}
