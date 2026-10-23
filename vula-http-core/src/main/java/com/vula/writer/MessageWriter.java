package com.vula.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vula.model.Headers;
import com.vula.model.HttpResponseMessage;
import com.vula.model.StatusLine;
import com.vula.constants.StatusCode;

import static com.vula.constants.HttpHeader.CONNECTION;
import static com.vula.constants.HttpHeader.CONTENT_LENGTH;
import static com.vula.constants.HttpHeader.CONTENT_TYPE;
import static com.vula.constants.HttpVersion.HTTP_VERSION_1;

public class MessageWriter {

    private static final Logger logger = Logger.getLogger(MessageWriter.class.getName());
    private static final byte[] CRLF = {'\r', '\n'};
 
    public void writeMessage(HttpResponseMessage responseMessage, OutputStream out) {
        try {
            out.write(responseMessage.getStatusLine().toString().getBytes(StandardCharsets.US_ASCII));
            out.write(CRLF);

            boolean responseBody = false;
            String headerLine;
            for (String key : responseMessage.getHeaders().getKeySet()) {
                if (!responseBody) responseBody = key.equalsIgnoreCase(CONTENT_LENGTH.getName());
                logger.finest("key is " + key + " and responseBody is : " + responseBody);
                headerLine = key + ": " + responseMessage.getHeaders().getHeader(key).get(0);
                out.write(headerLine.getBytes(StandardCharsets.US_ASCII));
                out.write(CRLF);
            }

            out.write(CRLF);

            if (responseBody) {
                out.write(responseMessage.getBody());
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to write response message", e);
        }
        
    }

    public void writeError(StatusCode statusCode, Throwable e, OutputStream out) {
        writeError(statusCode, e.getMessage(), out);
    }

    /**
     * Writes an error response whose body is exactly {@code message}. Use this when the
     * exception's own message must not reach the client, such as for a 500.
     */
    public void writeError(StatusCode statusCode, String message, OutputStream out) {
        StatusLine errorStatusLine = new StatusLine();
        errorStatusLine.setStatusCode(statusCode);
        errorStatusLine.setVersion(HTTP_VERSION_1);

        HttpResponseMessage response = new HttpResponseMessage();
        response.setStatusLine(errorStatusLine);

        Headers headers = new Headers();
        headers.addHeader(CONNECTION.getName(), "close");

        if (null != message && message.length() > 0) {
            byte[] body = message.getBytes(StandardCharsets.UTF_8);

            
            headers.addHeader(CONTENT_TYPE.getName(), "text/plain");
            headers.addHeader(CONTENT_LENGTH.getName(), String.valueOf(body.length));

            response.setBody(body);
        }

        response.setHeaders(headers);
        
        writeMessage(response, out);
    }

}
