package com.atguigu.gmall.mq.service;

import java.util.concurrent.TimeUnit;

public interface RabbitService {
    void sendMessage(String exchange, String routingKey, String Message);

    void sendDeadLetterMessage(String exchange, String routingKey, String Message, long time, TimeUnit timeType);

    void sendDelayMessage(String exchange, String routingKey, String Message, int time, TimeUnit timeType);
}
