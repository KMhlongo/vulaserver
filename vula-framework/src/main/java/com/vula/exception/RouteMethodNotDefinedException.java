package com.vula.exception;

public class RouteMethodNotDefinedException extends Exception {
    public RouteMethodNotDefinedException() {
        super();
    }

    public RouteMethodNotDefinedException(String message) {
        super(message);
    }

    public RouteMethodNotDefinedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
