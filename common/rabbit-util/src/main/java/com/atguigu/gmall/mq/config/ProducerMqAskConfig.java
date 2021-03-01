package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 测试rabbitmq的确认与回调机制
 */
@Component
public class ProducerMqAskConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    RabbitTemplate rabbitTemplate;

    // @PostConstruct：bean创建时会调用方法执行一次
    @PostConstruct
    void init(){
        rabbitTemplate.setReturnCallback(this);
        rabbitTemplate.setConfirmCallback(this);
    }

    /**
     * 确认模式 (针对交换机)
     *      无论消息发送成功或失败，都会调用该方法
     * @param correlationData 数据
     * @param ack             是否收到消息，如果为true表示收到消息，如果为false表示没收到消息
     * @param cause           如果没收到消息，原因是什么
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        System.out.println("进入confirm方法：");
        if (ack){
            System.out.println("消息发送成功");
        }else {
            System.out.println("消息发送失败，原因是：" + cause);
        }
    }

    /**
     * 回退模式 (针对路由规则)
     *      路由key填写错误、消息发送失败，会调用该方法，消息发送成功不会被调用
     * @param message    消息对象
     * @param replyCode  错误码
     * @param replyText  错误信息(错误码的解释)
     * @param exchange   交换机名字
     * @param routingKey 路由规则
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("进入returnedMessage方法：");
        System.out.println("message：" + message);
        System.out.println("replyCode：" + replyCode);
        System.out.println("replyText：" + replyText);
        System.out.println("exchange：" + exchange);
        System.out.println("routingKey：" + routingKey);
    }

}
