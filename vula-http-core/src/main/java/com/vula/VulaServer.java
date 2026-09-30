package com.vula;

import java.net.ServerSocket;

import com.vula.handler.RequestHandler;
import com.vula.handler.HttpHandler;

public class VulaServer {

    private HttpHandler httpHandler;

    public VulaServer(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    public void start(int port) {
        try (
            ServerSocket serverSocket = new ServerSocket(port);
        ) {
            System.out.println("Inet address: " + serverSocket.getInetAddress().toString());
            System.out.println("Listening on port: " + port);
            while (true) {
                new Thread(new RequestHandler(serverSocket.accept(), httpHandler)).start();
            }
           
        } catch (Exception e) {
            System.out.println("Server error");
            e.printStackTrace();
        }
    }

}
