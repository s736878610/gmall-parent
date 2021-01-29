package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.atguigu")
@EnableConfigurationProperties
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class,args);
    }
}
