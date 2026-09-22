package com.vula.writer;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import com.vula.model.HttpResponseMessage;

import static com.vula.constants.HttpHeader.CONTENT_LENGTH;

public class MessageWriter {
 
    public void writeMessage(HttpResponseMessage responseMessage, PrintWriter out) {
        // print status line out
        out.println(responseMessage.getStatusLine());

        // print each header
        boolean responseBody = false;
        for (String key : responseMessage.getHeaders().getKeySet()) {
            System.out.println("is contentLength : " + key.equalsIgnoreCase(CONTENT_LENGTH.getName()));
            if (!responseBody) responseBody = key.equalsIgnoreCase(CONTENT_LENGTH.getName());
            System.out.println("key is " + key + " and responseBody is : " + responseBody);
            out.println(key + ": " + responseMessage.getHeaders().getHeader(key).get(0));
        }

        if (responseBody) {
            out.println("");
            out.println(new String(responseMessage.getBody(), StandardCharsets.US_ASCII));
        }
        
    }

}
