package com.example.app;

import com.vula.annotations.Controller;
import com.vula.annotations.httpMethod.Get;
import com.vula.annotations.PathVariable;

@Controller(path = "hello")
public class HelloController {

    @Get(path = "")
    public String hello() {
        return "hello";
    }

    @Get(path = "world") 
    public String helloworld() {
        return "hello world";
    }

    @Get(path = "var/{id}")
    public String var() {
        return "path variable id ";
    }

}
