package com.vula.parser;

import static com.vula.constants.HttpVersion.getVersion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.vula.constants.HttpHeader;
import com.vula.constants.HttpMethod;
import com.vula.model.Headers;
import com.vula.model.HttpRequestMessage;
import com.vula.model.RequestLine;

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
                    throw new IOException("Header line is tooo big. What are we doing");
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
            out.write(buf, pos, limit-pos);
            return out.toByteArray();
        }

        do {
            out.write(buf, pos, limit-pos);
            size -= limit-pos;
            if (size <= 0) return out.toByteArray();
        } while ((limit = in.read(buf)) != -1);
        
        throw new IOException();
    }
    
    /**
     * Returns the request made by parsing the data from InputStream according to the http format.
     * 
     * @param socket
     * @return Request object consisting of the request line, headers and the body of the request if applicable.
     * @throws IOException
     */
    public HttpRequestMessage parseMessage(Socket socket) throws IOException {

        HttpRequestMessage request = new HttpRequestMessage();
        
        try {
            InputStream in = socket.getInputStream();
            // Parse the request line first, obtaining the method, target and the version
            byte[] byteRequestLine = readLine(in);
            String stringRequestLine = new String(byteRequestLine, 0, byteRequestLine.length-1, StandardCharsets.US_ASCII);
            String[] parts = stringRequestLine.split(" ");
            RequestLine requestLine = new RequestLine(HttpMethod.getHttpMethod(parts[0]), parts[1], getVersion(parts[2]));
            request.setRequestLine(requestLine);

            // Parse headers until the empty line signaling the end of the headers
            byte[] header = new byte[0];
            Headers headers = new Headers();
            int headersSize = 0;

            // the size of the header needs to be at least 2. It could have a single byte value 13 for carriage return.
            while ((header = readLine(in)).length > 1) {

                // There is a max headers size of 8192
                if ((headersSize += header.length) > MAX_HEADERS_SIZE) {
                    throw new IOException("Headers size is tooo big! What's going on");
                }
                
                String stringHeader = new String(header, 0, header.length-1, StandardCharsets.US_ASCII);
                String key = stringHeader.substring(0, stringHeader.indexOf(":"));
                String[] headerValues = stringHeader.substring((stringHeader.indexOf(":") + 1)).trim().split(",");
                for (String part : headerValues) {
                    headers.addHeader(key, part.trim());
                }
            }
            request.setHeaders(headers);

            for (String key : headers.getKeySet()) {
                System.out.println(key + " : " + headers.getHeader(key));
            }

            // okay now we need to parse the body
            // we will only parse the body of the request if the following headers are present:
            // transfer-encoding
            // content-length

            if (headers.containsHeader(HttpHeader.CONTENT_LENGTH.getName())) {
                System.out.println("Content-Length header found. Reading body");
                int contentLength = Integer.parseInt(headers.getHeader(HttpHeader.CONTENT_LENGTH.getName()).get(0));
                byte[] body = read(in, contentLength);
                String strBody = new String(body, StandardCharsets.US_ASCII);
                System.out.println("body: " + strBody);
                System.out.println("body length: " + strBody.length());
                request.setBody(body);
            }
            
            return request;

        } catch (IOException e) {
            System.out.println("failed to parse message");
            e.printStackTrace();
        } 
        
        return request;
    }
}
