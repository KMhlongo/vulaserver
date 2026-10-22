package com.vula;

import java.net.ServerSocket;
import java.util.logging.Logger;


import com.vula.handler.RequestHandler;
import com.vula.handler.HttpHandler;

public class VulaServer {

    private HttpHandler httpHandler;
    private static final Logger logger = Logger.getLogger(VulaServer.class.getName());

    public VulaServer(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    public void start(int port) {
        try (
            ServerSocket serverSocket = new ServerSocket(port);
        ) {
            logger.info("Inet address: " + serverSocket.getInetAddress().toString());
            logger.info("Listening on port: " + port);
            while (true) {
                new Thread(new RequestHandler(serverSocket.accept(), httpHandler)).start();
            }
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
