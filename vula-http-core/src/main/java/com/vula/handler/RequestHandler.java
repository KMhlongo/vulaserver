package com.vula.handler;

import java.io.PrintWriter;
import java.net.Socket;

import com.vula.model.HttpRequestMessage;
import com.vula.model.HttpResponseMessage;
import com.vula.parser.MessageParser;
import com.vula.writer.MessageWriter;

public class RequestHandler implements Runnable {

    private Socket socket;
    private HttpHandler httpHandler;

    public RequestHandler(Socket socket, HttpHandler httpHandler) {
        this.socket = socket;
        this.httpHandler = httpHandler;
    }

    public void run() {

        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream());
        ) {
            
            MessageParser parser = new MessageParser();
            HttpRequestMessage requestMessage = parser.parseMessage(socket.getInputStream());
            System.out.println("requestMessage is " + requestMessage);
                                
            HttpResponseMessage responseMessage = new HttpResponseMessage();
            httpHandler.handleRoute(requestMessage, responseMessage);
                    
            MessageWriter writer = new MessageWriter();
            writer.writeMessage(responseMessage, out);

            out.flush();
            System.out.println("Closing socket --------------");


        } catch (Exception e) {
            System.out.println("Exception occurred");
            e.printStackTrace();
        }

    }
    
}