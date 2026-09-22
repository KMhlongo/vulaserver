package com.vula;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;

import com.vula.annotations.Controller;
import com.vula.annotations.httpMethod.Get;
import com.vula.annotations.httpMethod.HttpMethodAnnotation;
import com.vula.route.Route;
import com.vula.route.RouteRegistry;

public class ClassScanner {

    private RouteRegistry routeRegistry = RouteRegistry.getInstance();
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private String basePackagePath;

    public ClassScanner(String basePackagePath) {
        this.basePackagePath = basePackagePath;
    }
    
    public void scanBasePackage() {
        String packagePath = this.basePackagePath.replace(".", "/");
        classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(packagePath);
        try {
            File file = new File(url.toURI());
            handleDirectory(file);
        } catch (URISyntaxException e) {
            System.out.println("Failed to load file");
            System.out.println(e.getStackTrace());
        }
        
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
        try {
            if (anno != null) {
                Object controllerInstance = clazz.getDeclaredConstructor().newInstance();
                controllerPath = anno.path();
                Method[] methods = clazz.getDeclaredMethods();
                for (Method m : methods) {
                    System.out.println("Checking to see if method has http annotation");
                    for (Annotation a : m.getDeclaredAnnotations()) {
                        if (a.annotationType().isAnnotationPresent(HttpMethodAnnotation.class)) 
                            handleHttpMethod(controllerInstance, controllerPath, m);
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            System.out.println("No Default Constructor found for class: " + clazz.getCanonicalName());
            System.out.println(e.getStackTrace());
        } catch (Exception e) {
            System.out.println("Error instantiaing controllerInstance object from constructor");
            System.out.println(e.getStackTrace());
        }
        
    }

    private void handleHttpMethod(Object controllerInstance, String controllerPath, Method method) {
        System.out.println("Method found: " + method + ", in class: " + controllerInstance.getClass().getName());
        if (method.getDeclaredAnnotation(Get.class) != null) {
            String methodPath = method.getDeclaredAnnotation(Get.class).path();
            String fullPath = controllerPath + "/" + methodPath;
            Route route = new Route(fullPath, controllerInstance, method);
            routeRegistry.register(route.getPath(), route);
        }
    }

}
