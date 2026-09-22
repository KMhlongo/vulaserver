package com.vula.route;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.vula.model.HttpRequestMessage;
import com.vula.model.HttpResponseMessage;
import com.vula.constants.HttpHeader;
import com.vula.constants.HttpVersion;
import com.vula.constants.StatusCode;
import com.vula.model.StatusLine;
import com.vula.model.Headers;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class RouteRegistry {

    private static final RouteRegistry instance = new RouteRegistry();

    private static final byte[] NO_BODY = new byte[0];
    private final ObjectMapper mapper = new ObjectMapper();

    public static RouteRegistry getInstance() {
        return instance;
    }

    private RouteRegistry() {
    };

    private HashMap<String, Route> registry = new HashMap<>();

    public void register(String path, Route route) {
        registry.put(normalise(path), route);
    }

    public Route getRoute(String path) {
        return registry.get(normalise(path));
    }    

    String normalise(String path) {
        if (null == path || path.isEmpty()) return "/";
        String p = path.replaceAll("/+", "/");
        if (!p.startsWith("/")) p = "/" + p;
        if (p.length()>1 && p.endsWith("/")) p = p.substring(0, p.length()-1);
        return p;
    }

    public void handleRoute(HttpRequestMessage requestMessage, HttpResponseMessage responseMessage) {
        
        // get the target excluding anything after a '?'
        String target = requestMessage.getRequestLine().getTarget();
        int idx = target.indexOf('?');
        String path = idx==-1 ? target : target.substring(0, idx) ;

        Route route = getRoute(path);

        if (null == route) {
            System.out.println("Route not found");
            respond(responseMessage, StatusCode.NOT_FOUND, NO_BODY);
            return;
        }

        try {
            Object responseObject = route.getMethod().invoke(route.getControllerInstance());
            byte[] body = (null == responseObject) ? NO_BODY : mapper.writeValueAsBytes(responseObject);
            respond(responseMessage, StatusCode.OK, body);
            return;

        } catch (InvocationTargetException e) {
            System.err.println("Handler " + route.getPath() + " threw an exception");
            e.getCause().printStackTrace();
            respond(responseMessage, StatusCode.INTERNAL_SERVER_ERROR, NO_BODY);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            System.err.println("Could not invoke " + route.getMethod());
            e.printStackTrace();
            respond(responseMessage, StatusCode.INTERNAL_SERVER_ERROR, NO_BODY);
        } catch (JacksonException e) {
            System.err.println("Could not serialise result of " + route.getMethod());
            e.printStackTrace();
            respond(responseMessage, StatusCode.INTERNAL_SERVER_ERROR, NO_BODY);
        } 
        
        
    }

    private void respond(HttpResponseMessage response, StatusCode statuscode, byte[] body) {

        StatusLine statusLine = new StatusLine();
        statusLine.setStatusCode(statuscode);
        statusLine.setVersion(HttpVersion.HTTP_VERSION_1);
        response.setStatusLine(statusLine);

        Headers headers = new Headers();
        headers.addHeader(HttpHeader.CONNECTION.getName(), "close");
        headers.addHeader(HttpHeader.CONTENT_LENGTH.getName(), String.valueOf(body.length));
        if (body.length > 0) {
            headers.addHeader(HttpHeader.CONTENT_TYPE.getName(), "application/json");
        }
        response.setHeaders(headers);
        
        response.setBody(body);

    }

    

}
