package com.vula.route;

import java.lang.reflect.Method;

public class Route {
    private String path;
    private Object controllerInstance;
    private Method method;

    public Route(String path, Object controllerInstance, Method method) {
        this.path = path;
        this.controllerInstance = controllerInstance;
        this.method = method;
    }

    public String getPath() {
        return this.path;
    }

    public Object getControllerInstance() {
        return this.controllerInstance;
    }

    public Method getMethod() {
        return this.method;
    }

    @Override
    public String toString() {
        return this.path + " " + 
            this.controllerInstance.getClass().getName() + " " + 
            this.method;
    }
}
