package com.example.app;

import com.vula.annotations.Controller;
import com.vula.annotations.httpMethod.Get;

@Controller(path = "hello")
public class HelloController {

    @Get(path = "")
    public String hello() {
        return "hello";
    }

    @Get(path = "world") 
    public String helloyou() {
        return "hello world";
    }

}
