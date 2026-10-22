package com.vula.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.MalformedInputException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vula.exception.MalformedRequestException;
import com.vula.model.HttpRequestMessage;
import com.vula.model.HttpResponseMessage;
import com.vula.parser.MessageParser;
import com.vula.writer.MessageWriter;

public class RequestHandler implements Runnable {

    private Socket socket;
    private HttpHandler httpHandler;
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket socket, HttpHandler httpHandler) {
        this.socket = socket;
        this.httpHandler = httpHandler;
    }

    public void run() {
        MessageParser parser = new MessageParser();
        MessageWriter writer = new MessageWriter();
        HttpResponseMessage responseMessage = new HttpResponseMessage();
        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream());
        ) {
            
            HttpRequestMessage requestMessage = parser.parseMessage(socket.getInputStream());
            logger.finest("requestMessage is " + requestMessage);
            httpHandler.handleRoute(requestMessage, responseMessage);
            writer.writeMessage(responseMessage, out);
            out.flush();
            logger.finest("Closing socket");


        } catch (MalformedRequestException e) {
            logger.log(Level.WARNING, "Invalid Request received", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occurred while processing request", e);
        }
    }
    
}