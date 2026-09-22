package com.vula.model;

public class HttpRequestMessage {
    private RequestLine requestLine;
    private Headers requestHeaders;
    private byte[] body;

    public void setRequestLine(RequestLine requestLine) {
        this.requestLine = requestLine;
    }

    public RequestLine getRequestLine() {
        return this.requestLine;
    }

    public void setHeaders(Headers requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public Headers getHeaders() {
        return this.requestHeaders;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() { 
        return this.body;
    }

    @Override
    public String toString() {
        return this.requestLine + " \n" +
            this.getHeaders() + "\n" + 
            new String(this.body == null ? "" : new String(this.body)); 
    }
}
