package com.vula;

import org.junit.jupiter.api.Test;

public class ClassScannerTest {
    
    @Test
    public void ScanBasePackageForAnnotations() {
        String basePackageString = VulaApplicationTest.class.getPackageName();
        ClassScanner classScanner = new ClassScanner(basePackageString);
        classScanner.scanBasePackage();

    }

}
