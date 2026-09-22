package com.vula;

import java.net.ServerSocket;

import com.vula.handler.RequestHandler;

public class VulaServer {

    public void start(int port) {
        try (
            ServerSocket serverSocket = new ServerSocket(port);
        ) {
            System.out.println("Inet address: " + serverSocket.getInetAddress().toString());
            System.out.println("Listening on port: " + port);
            while (true) {
                new Thread(new RequestHandler(serverSocket.accept())).start();
            }
           
        } catch (Exception e) {
            System.out.println("Server error");
            e.printStackTrace();
        }
    }

}
