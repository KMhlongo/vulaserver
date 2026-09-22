package com.vula;

public class VulaApplication {
    public static void run(Class<?> mainClass, String[] args) {
        new ClassScanner(mainClass.getPackageName()).scanBasePackage();
        new VulaServer().start(8080);
    }
}
