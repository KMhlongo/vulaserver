package com.vula;

import com.vula.route.RouteRegistry;

public class VulaApplication {
    public static void run(Class<?> mainClass, String[] args) {
        RouteRegistry registry = new ClassScanner(mainClass.getPackageName()).scanBasePackage();
        new VulaServer(registry).start(8080);
    }
}
