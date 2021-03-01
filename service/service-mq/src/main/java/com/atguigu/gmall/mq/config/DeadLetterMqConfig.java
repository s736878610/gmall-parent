package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列配置类
 */
@Configuration
public class DeadLetterMqConfig  {

    public static final String exchange_dead= "exchange.dead";
    public static final String routing_dead_1 = "routing.dead.1";
    public static final String routing_dead_2 = "routing.dead.2";
    public static final String queue_dead_1 = "queue.dead.1";
    public static final String queue_dead_2 = "queue.dead.2";

    /**
     * 其他队列可以在RabbitListener上面做绑定
     * @return
     */

    @Bean
    public DirectExchange exchange(){
        // 声明交换机
        return new DirectExchange(exchange_dead, true, false, null);
    }

    @Bean
    public Queue queue1(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", exchange_dead);// 设置死信交换机
        arguments.put("x-dead-letter-routing-key", routing_dead_2);// 设置死信key
        return new Queue(queue_dead_1, true, false, false, arguments);
    }

    @Bean
    public Binding binding(){
        // 队列绑定交换机并设置路由key
        return BindingBuilder.bind(queue1()).to(exchange()).with(routing_dead_1);
    }

    @Bean
    public Queue queue2(){
        // 声明存放死信消息的死信队列
        return new Queue(queue_dead_2, true, false, false, null);
    }

    @Bean
    public Binding deadBinding(){
        // 队列绑定交换机并设置路由key
        return BindingBuilder.bind(queue2()).to(exchange()).with(routing_dead_2);
    }


}
