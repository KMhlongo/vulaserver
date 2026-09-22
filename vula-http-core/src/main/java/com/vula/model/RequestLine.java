package com.vula.model;

import com.vula.constants.HttpMethod;
import com.vula.constants.HttpVersion;

public class RequestLine {
    private HttpMethod httpMethod;
    private String target;
    private HttpVersion version;

    public RequestLine() {}

    public RequestLine(HttpMethod HttpMethod, String target, HttpVersion version) {
        this.httpMethod = HttpMethod;
        this.target = target;
        this.version = version;
    }
        
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = HttpMethod.getHttpMethod(httpMethod);
    }

    public HttpVersion getHttpVersion() {
        return this.version;
    }

    public String getTarget() {
        return this.target;
    }

    public String toString() {
        return this.httpMethod + " " + this.target + " " + this.version;
    }
}
