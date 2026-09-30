package com.vula.exception;

public class RouteConfigurationException extends RuntimeException {
    public RouteConfigurationException() {
        super();
    }

    public RouteConfigurationException(String message) {
        super(message);
    }

    public RouteConfigurationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
