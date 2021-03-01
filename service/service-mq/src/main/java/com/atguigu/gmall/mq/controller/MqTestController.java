package com.atguigu.gmall.mq.controller;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.atguigu.gmall.mq.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 生产者
 */
@RestController
public class MqTestController {

    @Autowired
    RabbitService rabbitService;

    @RequestMapping("testMq")
    public String testMq() {
        // 发送普通消息
        rabbitService.sendMessage("test_exchange", "test", "Hello");
        return "testMq";
    }

    @RequestMapping("testDeadLetter")
    public String testDeadLetter() {
        // 测试死信队列
        rabbitService.sendDeadLetterMessage(DeadLetterMqConfig.exchange_dead
                , DeadLetterMqConfig.routing_dead_1,
                "Hello DeadLetter",
                5,
                TimeUnit.SECONDS);
        return "testDeadLetter";
    }

    @RequestMapping("testDelay")
    public String testDelay() {
        // 测试插件实现延时队列
        rabbitService.sendDelayMessage(DelayedMqConfig.exchange_delay,
                DelayedMqConfig.routing_delay,
                "Hello Delay",
                10,
                TimeUnit.SECONDS);
        return "testDelay";
    }

}
