package com.vula;

import org.junit.jupiter.api.Test;

import com.vula.route.RouteRegistry;

public class ClassScannerTest {
    
    @Test
    public void ScanBasePackageForAnnotations() {
        String basePackageString = VulaApplicationTest.class.getPackageName();
        ClassScanner classScanner = new ClassScanner(basePackageString);
        classScanner.scanBasePackage();

    }

}
