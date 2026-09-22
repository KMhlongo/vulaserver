package com.vula;

import com.vula.annotations.Controller;
import com.vula.annotations.httpMethod.Get;

@Controller(path = "")
public class TestController {

    @Get(path = "random")
    public double returnRandomNumber() {
        return Math.random();
    }
    
}
