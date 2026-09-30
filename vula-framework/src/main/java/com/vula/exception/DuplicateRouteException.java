package com.vula.exception;

public class DuplicateRouteException extends Exception {

    public DuplicateRouteException() {
        super();
    }

    public DuplicateRouteException(String message) {
        super(message);
    }

    public DuplicateRouteException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
}
