package com.atguigu.gmall.mq.service.impl;

import com.atguigu.gmall.mq.service.RabbitService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 封装rabbitMq通用类
 */
@Service
public class RabbitServiceImpl implements RabbitService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     *
     * @param exchange   交换机名字
     * @param routingKey 路由规则
     * @param Message    消息体
     */
    @Override
    public void sendMessage(String exchange, String routingKey, String Message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, Message);
    }

    /**
     * 发送死信消息 + 过期时间
     *
     * @param exchange
     * @param routingKey
     * @param Message
     * @param time       过期时间值
     * @param timeType   过期时间单位
     */
    @Override
    public void sendDeadLetterMessage(String exchange, String routingKey, String Message, long time, TimeUnit timeType) {
        rabbitTemplate.convertAndSend(exchange, routingKey, Message, processor -> {
            // 设置消息存活时间
            processor.getMessageProperties().setExpiration(time * 1000 + "");// TTL时间，默认单位毫秒
            return processor;
        });
    }

    /**
     * 插件实现延时队列
     *
     * @param exchange
     * @param routingKey
     * @param Message
     * @param time
     * @param timeType
     */
    @Override
    public void sendDelayMessage(String exchange, String routingKey, String Message, int time, TimeUnit timeType) {
        rabbitTemplate.convertAndSend(exchange, routingKey, Message, processor -> {
            // 设置延时发送时间
            processor.getMessageProperties().setDelay(time * 1000);// TTL时间，默认单位毫秒
            return processor;
        });
    }


}
