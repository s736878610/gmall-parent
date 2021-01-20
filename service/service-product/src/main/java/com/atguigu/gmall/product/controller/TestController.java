package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.result.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping("test")
    public Result<String> Test(){
        String test = "1111";
        return Result.ok(test);
    }


}
