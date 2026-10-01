package com.vula;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.net.URL;

import com.vula.annotations.Controller;
import com.vula.annotations.httpMethod.Get;
import com.vula.annotations.httpMethod.HttpMethodAnnotation;
import com.vula.route.Route;
import com.vula.route.RouteRegistry;
import com.vula.constants.HttpMethod;
import com.vula.exception.DuplicateRouteException;
import com.vula.exception.RouteConfigurationException;

public class ClassScanner {

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private final String basePackagePath;
    private final RouteRegistry routeRegistry = new RouteRegistry();

    public ClassScanner(String basePackagePath) {
        this.basePackagePath = basePackagePath;
    }
    
    public RouteRegistry scanBasePackage() {
        String packagePath = this.basePackagePath.replace(".", "/");
        classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(packagePath);
        try {
            File file = new File(url.toURI());
            handleDirectory(file);
        } catch (RouteConfigurationException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RouteConfigurationException(e.getMessage());
        } 
        return routeRegistry;
    }

    private void handleDirectory(File file) {
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                handleDirectory(f);
            } else if (f.getName().endsWith(".class")) {
                handleClassFile(f);
            }
        }
    }

    private void handleClassFile(File file) {
        String absolutePath = file.getAbsolutePath();
        String packagePath = basePackagePath.replace(".", File.separator);
        int packageIndex = absolutePath.indexOf(packagePath);
        if (packageIndex == -1) return;

        String className = absolutePath
                .substring(packageIndex)
                .replace(File.separator, ".")
                .replace(".class", "");

        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load class: " + className);
            return;
        }

        Controller anno = clazz.getDeclaredAnnotation(Controller.class);
        String controllerPath;
        if (anno == null) return;
        try {
            Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
            controllerPath = anno.path();
            for (Method m : clazz.getDeclaredMethods()) {
                for (Annotation a : m.getDeclaredAnnotations()) {
                    if (a.annotationType().isAnnotationPresent(HttpMethodAnnotation.class)) 
                        handleHttpMethod(controllerInstance, controllerPath, m);
                }
            }
        } catch (RouteConfigurationException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RouteConfigurationException(e.getMessage());
        }
        
    }

    private void handleHttpMethod(Object controllerInstance, String controllerPath, Method method) {
        System.out.println("Method found: " + method + ", in class: " + controllerInstance.getClass().getName());
        if (method.getDeclaredAnnotation(Get.class) != null) {
            String methodPath = method.getDeclaredAnnotation(Get.class).path();
            // TODO : implement path variables
            Parameter[] paramters = method.getParameters();
            String fullPath = controllerPath + "/" + methodPath;
            Route route = new Route(fullPath, controllerInstance, method);
            try {
				routeRegistry.register(HttpMethod.GET ,route.getPath(), route);
			} catch (DuplicateRouteException e) {
				e.printStackTrace();
                throw new RouteConfigurationException(e.getMessage());
			}
        }
    }

}
