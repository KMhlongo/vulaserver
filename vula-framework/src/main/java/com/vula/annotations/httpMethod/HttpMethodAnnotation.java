package com.vula.annotations.httpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vula.constants.HttpMethod;

/**
 * Marker annotation for every http method annotation
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpMethodAnnotation {
    HttpMethod value();
}
