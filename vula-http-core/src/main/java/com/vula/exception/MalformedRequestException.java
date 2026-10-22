package com.vula.exception;

import com.vula.constants.StatusCode;

public class MalformedRequestException extends Exception {

    private final StatusCode statusCode;
    
    public MalformedRequestException(StatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public MalformedRequestException(String message) {
        this(StatusCode.BAD_REQUEST, message);

    }

    public MalformedRequestException(StatusCode statusCode, String message, Throwable throwable) {
        super(message, throwable);
        this.statusCode = statusCode;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

}
