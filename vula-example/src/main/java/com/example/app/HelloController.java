package com.example.app;

import com.example.app.model.Request;
import com.vula.annotations.Controller;
import com.vula.annotations.httpMethod.Get;
import com.vula.annotations.httpMethod.Post;
import com.vula.annotations.params.PathVariable;
import com.vula.annotations.params.RequestBody;

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
    public String var(@PathVariable(name = "id") String id) {
        return "path variable id is " + id;
    }

    @Get(path = "{var1}/{var2}")
    public String twoVars(@PathVariable(name = "var1") String var1, @PathVariable(name = "var2") String var2) {
        return "var1 is " + var1 + " and var2 is " + var2;
    }

    @Post(path = "body")
    public String useBody(@RequestBody Request req) {
        String response = req.toString();
        return response;
    }

}
