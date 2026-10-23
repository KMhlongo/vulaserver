package com.vula.handler;

import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vula.constants.StatusCode;
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
        try (Socket s = socket) {
            handle(s.getInputStream(), s.getOutputStream());
        } catch (IOException e) {
            logger.log(Level.FINE, "Error occurred while processing request", e);
        }
    }

    void handle(InputStream in, OutputStream out) {
        MessageParser parser = new MessageParser();
        MessageWriter writer = new MessageWriter();
        HttpResponseMessage responseMessage = new HttpResponseMessage();
        boolean responded = false;
                
        try {
            HttpRequestMessage requestMessage = parser.parseMessage(in);
            if (null == requestMessage) return;

            logger.finest("RequestMessage is " + requestMessage);
            httpHandler.handleRoute(requestMessage, responseMessage);
            responded = true;
            writer.writeMessage(responseMessage, out);
            out.flush();
            logger.finest("Closing socket");
        } catch (MalformedRequestException e) {
            logger.log(Level.FINE, "Invalid Request received", e);
            writer.writeError(e.getStatusCode(), e, out);
        } catch (IOException e) {
            logger.log(Level.FINE, "Connection failed while processing request", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occurred while processing request", e);
            if (!responded) writer.writeError(StatusCode.INTERNAL_SERVER_ERROR, "Internal Server Error", out);
        }
    }

}