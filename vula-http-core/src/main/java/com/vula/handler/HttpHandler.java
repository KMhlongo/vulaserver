package com.vula.handler;
import com.vula.model.HttpRequestMessage;
import com.vula.model.HttpResponseMessage;

public interface HttpHandler {
    
    void handleRoute(HttpRequestMessage requestMessage, HttpResponseMessage responseMessage) throws Exception;

}
