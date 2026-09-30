package com.vula.route;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;

import com.vula.constants.HttpHeader;
import com.vula.constants.HttpVersion;
import com.vula.constants.StatusCode;
import com.vula.exception.DuplicateRouteException;
import com.vula.exception.RouteMethodNotDefinedException;
import com.vula.exception.RouteNotFoundException;
import com.vula.model.Headers;
import com.vula.model.HttpRequestMessage;
import com.vula.model.HttpResponseMessage;
import com.vula.model.StatusLine;
import com.vula.handler.HttpHandler;
import com.vula.constants.HttpMethod;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class RouteRegistry implements HttpHandler {

    private static final byte[] NO_BODY = new byte[0];
    private final ObjectMapper mapper = new ObjectMapper();

    private Map<String, Map<HttpMethod, Route>> registry = new HashMap<>();

    public void register(HttpMethod method, String path, Route route) throws DuplicateRouteException {
        Map<HttpMethod, Route> byMethod = 
            registry.computeIfAbsent(normalise(path), p -> new EnumMap<>(HttpMethod.class));
        if (byMethod.putIfAbsent(method, route) != null) {
            throw new DuplicateRouteException("Route [" + route.getPath() + "] already exists for method [" + method.name() + "]");
        }
    }

    public Route getRoute(HttpMethod method, String path) throws RouteNotFoundException, RouteMethodNotDefinedException {
        Map<HttpMethod, Route> byMethod = 
            registry.get(normalise(path));
        if (null == byMethod) {
            throw new RouteNotFoundException("Route [" + path + "] not found");
        }
        Route route = byMethod.get(method);
        if (null == route) {
            throw new RouteMethodNotDefinedException("Method [" + method.name() + "] not defined for path [" + path + "]");
        }

        return route;
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
        HttpMethod method = requestMessage.getRequestLine().getHttpMethod();

        Route route;

        try {
            route = getRoute(method, path);
            Object responseObject = route.getMethod().invoke(route.getControllerInstance());
            byte[] body = (null == responseObject) ? NO_BODY : mapper.writeValueAsBytes(responseObject);
            respond(responseMessage, StatusCode.OK, body);
            return;

        } catch (RouteNotFoundException e) {
            e.printStackTrace();
            respond(responseMessage, StatusCode.NOT_FOUND, NO_BODY);
        } catch (RouteMethodNotDefinedException e) {
			e.printStackTrace();
            respond(responseMessage, StatusCode.METHOD_NOT_ALLOWED, NO_BODY);
        } catch (InvocationTargetException e) {
            System.err.println("Handler " + path + " threw an exception");
            e.getCause().printStackTrace();
            respond(responseMessage, StatusCode.INTERNAL_SERVER_ERROR, NO_BODY);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            System.err.println("Could not invoke route method");
            e.printStackTrace();
            respond(responseMessage, StatusCode.INTERNAL_SERVER_ERROR, NO_BODY);
        } catch (JacksonException e) {
            System.err.println("Could not serialise result of route method");
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
