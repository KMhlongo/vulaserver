package com.vula.parser;

import static com.vula.constants.HttpHeader.CONTENT_LENGTH;
import static com.vula.constants.HttpVersion.getVersion;
import static com.vula.constants.HttpMethod.getHttpMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vula.constants.HttpHeader;
import com.vula.constants.HttpMethod;
import com.vula.constants.HttpVersion;
import com.vula.model.Headers;
import com.vula.model.HttpRequestMessage;
import com.vula.model.RequestLine;
import com.vula.exception.MalformedRequestException;

import static com.vula.constants.StatusCode.NOT_IMPLEMENTED;
import static com.vula.constants.StatusCode.HTTP_VERSION_NOT_SUPPORTED;

/**
 * Used to parse an HTTP Request into a Request object
 * @see HttpRequestMessage
 */
public class MessageParser {
    private final static int MAX_HEADER_LINE_SIZE = 8192;
    private final static int MAX_HEADERS_SIZE = 8192;
    private int size = 8192;
    private byte[] buf = new byte[size];
    private int pos = 0;
    private int limit = 0;

    private static final Logger logger = Logger.getLogger(MessageParser.class.getName());

    /**
     * Returns a line from the InputStream in. Assumes each line is seperated by a carriage return (\r\n). 
     * 
     * @param in
     * @return byte array representing a line from the input stream.
     * @throws IOException
     */
    public byte[] readLine(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean lastByteCR = false;
        // check to see if we need to read another line
        // either because this is the beginning of the message or there was a CLRF at exactly the end of the previous buffer
        if (pos>=limit) {
            limit = in.read(buf);
            pos = 0;
        }        
        
        int crlf;
        do {
            crlf = pos;
            while (crlf < limit) {
                if (out.size() >= MAX_HEADER_LINE_SIZE) {
                    throw new IOException("Header line is too long");
                }
                
                // if we have found a carriage return, write it out to the input stream and return
                if (buf[crlf] == '\n' && crlf > 0 ? buf[crlf-1] == '\r' : lastByteCR) {
                    out.write(buf, pos, crlf-pos);
                    pos = crlf+1;
                    return out.toByteArray();
                }
                crlf++;
            }
            out.write(buf, pos, crlf-pos);
            pos = 0;
            lastByteCR = buf[limit-1] == '\r';
        } while ((limit = in.read(buf)) != -1);

        throw new IOException();
        
    }

    /**
     * Returns a specified number of bytes from the input stream. This is meant to be used to read the body at the 
     * end of a request. 
     * 
     * @param in
     * @param size the number of bytes required to read
     * @return
     * @throws IOException
     */
    public byte[] read(InputStream in, int size) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        if (limit-pos >= size) {
            out.write(buf, pos, size);
            pos += size;
            return out.toByteArray();
        }

        do {
            int n = Math.min(limit-pos, size);
            out.write(buf, pos, n);
            pos += n;
            size -= n;
            if (size == 0) return out.toByteArray();
            pos = 0;
        } while ((limit = in.read(buf)) != -1);
        
        throw new IOException();
    }
    
    /**
     * Returns the request made by parsing the data from InputStream according to the http format.
     * 
     * @param in InputStream
     * @return Request object consisting of the request line, headers and the body of the request if applicable.
     * @throws IOException
     */
    public HttpRequestMessage parseMessage(InputStream in) throws IOException, MalformedRequestException {

        HttpRequestMessage request = new HttpRequestMessage();
        
        // Parse the request line first, obtaining the method, target and the version
        byte[] byteRequestLine = readLine(in);
        String stringRequestLine = new String(byteRequestLine, 0, byteRequestLine.length-1, StandardCharsets.US_ASCII);
        String[] parts = stringRequestLine.split(" ");

        if (parts.length != 3) {
            throw new MalformedRequestException("Invalid Request Line: " + stringRequestLine);
        }

        HttpMethod method = getHttpMethod(parts[0]);
        if (null == method) {
            throw new MalformedRequestException(NOT_IMPLEMENTED, "Unrecognized request method: " + parts[0]);
        }

        HttpVersion version = getVersion(parts[2]);
        if (null == version) {
            throw new MalformedRequestException(HTTP_VERSION_NOT_SUPPORTED, "HTTP Version Not Supported: " + parts[2]);
        }

        RequestLine requestLine = new RequestLine(HttpMethod.getHttpMethod(parts[0]), parts[1], getVersion(parts[2]));
        request.setRequestLine(requestLine);

        // Parse headers until the empty line signaling the end of the headers
        byte[] header = new byte[0];
        Headers headers = new Headers();
        int headersSize = 0;
        int colonIdx = 0;

        // the size of the header needs to be at least 2. It could have a single byte value 13 for carriage return.
        while ((header = readLine(in)).length > 1) {

            // There is a max headers size of 8192
            if ((headersSize += header.length) > MAX_HEADERS_SIZE) {
                throw new MalformedRequestException("Headers exceeds max size of " + MAX_HEADERS_SIZE + " btyes");
            }
            
            String stringHeader = new String(header, 0, header.length-1, StandardCharsets.US_ASCII);
            colonIdx = stringHeader.indexOf(":");
            if (colonIdx < 1) {
                throw new MalformedRequestException("Invalid Header Line: " + stringHeader);
            }
            String key = stringHeader.substring(0, stringHeader.indexOf(":"));
            String[] headerValues = stringHeader.substring((stringHeader.indexOf(":") + 1)).trim().split(",");
            for (String part : headerValues) {
                headers.addHeader(key, part.trim());
            }
        }
        request.setHeaders(headers);

        if (logger.isLoggable(Level.FINEST)) {
            for (String key : headers.getKeySet()) {
                logger.finest(key + " : " + headers.getHeader(key));
            }
        }

        if (headers.containsHeader(HttpHeader.CONTENT_LENGTH.getName())) {
            try {
                int contentLength = Integer.parseInt(headers.getHeader(HttpHeader.CONTENT_LENGTH.getName()).get(0));
                if (contentLength < 0) {
                    throw new MalformedRequestException("Content length must be a non negative number");
                }
                byte[] body = read(in, contentLength);
                if (logger.isLoggable(Level.FINEST)) {
                    String strBody = new String(body, StandardCharsets.US_ASCII);
                    logger.finest("Request Body: " + strBody);
                    logger.finest("Request Body length: " + strBody.length());
                }
                request.setBody(body);
            } catch (NumberFormatException e) {
                throw new MalformedRequestException(
                    CONTENT_LENGTH.name() + " is not a number: " + headers.getHeader(HttpHeader.CONTENT_LENGTH.getName()).get(0)
                );
            }
        }
        
        return request;

    }
}
