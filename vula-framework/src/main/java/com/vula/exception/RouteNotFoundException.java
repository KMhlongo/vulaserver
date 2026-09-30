package com.vula.exception;

public class RouteNotFoundException extends Exception{

    public RouteNotFoundException() {
        super();
    }

    public RouteNotFoundException(String message) {
        super(message);
    }

    public RouteNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
}
