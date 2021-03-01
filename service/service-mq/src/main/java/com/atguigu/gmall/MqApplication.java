package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)// 取消数据源自动配置
@ComponentScan("com.atguigu")
public class MqApplication {
    public static void main(String[] args) {
        SpringApplication.run(MqApplication.class,args);
    }
}
