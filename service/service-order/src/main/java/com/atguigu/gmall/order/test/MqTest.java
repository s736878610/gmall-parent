package com.atguigu.gmall.order.test;

import com.atguigu.gmall.mq.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MqTest {

    @Autowired
    RabbitService rabbitService;

    @RequestMapping("testMq")
    public String testMq() {
        // 发送普通消息
        rabbitService.sendMessage("test_exchange1", "test", "Hello");
        return "testMq";
    }

}
