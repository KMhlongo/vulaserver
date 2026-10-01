package com.vula.route;

import com.vula.constants.HttpMethod;
import com.vula.exception.DuplicateRouteException;
import com.vula.exception.RouteNotFoundException;
import com.vula.exception.RouteMethodNotDefinedException;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;

public class RouteNode {

    private Map<String, RouteNode> nodes = new HashMap<>();
    private String segment;
    private RouteNode pathVariableNode;
    private Map<HttpMethod, Route> routes = new EnumMap<>(HttpMethod.class);

    public void register(String[] segments, int index, HttpMethod method, Route route) throws DuplicateRouteException {
        if (segments.length == index) {
            if (routes.putIfAbsent(method, route) != null) {
                throw new DuplicateRouteException("Route [" + route.getPath() + "] already exists for method [" + method.name() + "]");
            }
            return;
        }

        String segment = segments[index];
        RouteNode node;

        if (segment.startsWith("{") && segment.endsWith("}")) {
            String name = segment.substring(1, segment.length() - 1);
            if (pathVariableNode == null) {
                pathVariableNode = new RouteNode();
                pathVariableNode.segment = name;
            } else if (!pathVariableNode.segment.equals(name)) {
                throw new DuplicateRouteException("{" + name + "} conflicts with {" + pathVariableNode.segment + "}");
            }
            node = pathVariableNode;
        } else {
            node = nodes.computeIfAbsent(segment, s -> new RouteNode());
            node.segment = segment;
        }

        node.register(segments, index + 1, method, route);
    }

    public Route getRoute(String[] segments, int index, HttpMethod method) throws RouteMethodNotDefinedException, RouteNotFoundException {
        if (segments.length == index) {
            if (routes.isEmpty()) {
                throw new RouteNotFoundException();
            }
            Route route = routes.get(method);
            if (null == route) {
                throw new RouteMethodNotDefinedException("Method [" + method.name() + "] not defined for route");
            }
            return route;
        }

        String segment = segments[index];

        if (nodes.containsKey(segment)) {
            try {
                return nodes.get(segment).getRoute(segments, index + 1, method);
            } catch (RouteNotFoundException e) {
                if (pathVariableNode == null) {
                    throw e;
                }
            }
        } 
        
        if (pathVariableNode != null) {
            return pathVariableNode.getRoute(segments, index + 1, method);
        } 

        throw new RouteNotFoundException();
    }
    
}
