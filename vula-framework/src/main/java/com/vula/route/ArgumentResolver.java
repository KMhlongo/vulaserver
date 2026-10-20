package com.vula.route;

import java.lang.Class;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.vula.model.HttpRequestMessage;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import com.vula.annotations.params.*;
import com.vula.exception.BadRequestException;
import com.vula.exception.RouteConfigurationException;

public class ArgumentResolver {

    private final ObjectMapper mapper = new ObjectMapper();

    public Object[] resolve(Route route, HttpRequestMessage requestMessage, String path) throws BadRequestException {
        Parameter[] parameters = route.getMethod().getParameters();
        Object[] params = new Object[parameters.length];
        Map<String, Object> variableParams = new HashMap();
        String definedPathTokens[] = route.getPath().split("/");
        String requestPathTokens[] = path.substring(1).split("/");

        if (definedPathTokens.length > 0) {
            for (int i = 0; i < definedPathTokens.length; i++) {
                if (definedPathTokens[i].startsWith("{") && definedPathTokens[i].endsWith("}")) {
                    variableParams.put(definedPathTokens[i].substring(1, definedPathTokens[i].length()-1), requestPathTokens[i]);
                }
            }
        }

        Parameter parameter;

        for (int i = 0; i < parameters.length; i++) {
            parameter = parameters[i];

            PathVariable pathVarAnno = parameter.getDeclaredAnnotation(PathVariable.class);
            if (null != pathVarAnno) {                    
                params[i] = variableParams.get(pathVarAnno.name());
            }

            RequestBody reqBodyAnno = parameter.getDeclaredAnnotation(RequestBody.class);
            if (null != reqBodyAnno) {
                params[i] = readBody(parameter, requestMessage.getBody());
            }
        }
 
        return params;
    }

    private Object readBody(Parameter parameter, byte[] body) throws BadRequestException {
        Class<?> rawType = parameter.getType();

        if (rawType == byte[].class) return body;
        if (body == null || body.length == 0) {
            throw new BadRequestException("Empty body for request");
        }

        if (rawType == String.class) {
            return new String(body, StandardCharsets.UTF_8);
        }

        try {
            return mapper.readValue(body, rawType);
        } catch (JacksonException e) {
            throw new BadRequestException("Request body is not valid JSON for " + rawType, e);
        }

    }

}
