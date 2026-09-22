package com.vula.model;

import com.vula.constants.HttpVersion;
import com.vula.constants.StatusCode;

public class StatusLine {
    private HttpVersion version;
    private StatusCode statusCode;

    public HttpVersion getVersion() {
        return version;
    }

    public void setVersion(HttpVersion version) {
        this.version = version;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return this.version.getVersion() + " " + this.statusCode.getCode();
    }

}
