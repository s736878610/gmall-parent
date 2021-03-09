package com.atguigu.gmall.payment.config;

import com.atguigu.gmall.mq.constant.MqConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 定时检查支付结果的延时队列  配置类
 */
@Configuration
public class DelayedMqConfig {

    public static final String exchange_delay = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY;
    public static final String routing_delay = MqConst.ROUTING_PAYMENT_PAY;
    public static final String queue_delay_1 = MqConst.QUEUE_PAYMENT_CHECK;

    /**
     * 队列不要在RabbitListener上面做绑定，否则不会成功，如队列2，必须在此绑定
     *
     * @return
     */
    @Bean
    public Queue delayQueue1() {
        // 第一个参数是创建的queue的名字，第二个参数是是否支持持久化
        return new Queue(queue_delay_1, true);
    }

    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(exchange_delay, "x-delayed-message", true, false, args);
    }

    @Bean
    public Binding delayBinding1() {
        return BindingBuilder.bind(delayQueue1()).to(delayExchange()).with(routing_delay).noargs();
    }
}
