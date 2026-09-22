package com.vula.constants;

public enum StatusCode {
    OK(200),
    CREATED(201),
    BAD_REQUEST(400),
    UNATHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    INTERNAL_SERVER_ERROR(500);

    private int code;

    private StatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
