package com.atguigu.gmall.order.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.mq.constant.MqConst;
import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentReceiver {

    @Autowired
    OrderService orderService;

    /**
     * payment同步回调接口监视器
     * @param channel
     * @param message
     * @param messageBody
     */
    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(// 队列绑定交换机并设置路由key
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),// 交换机名称
            value = @Queue(value = MqConst.QUEUE_PAYMENT_PAY),// 队列名称
            key = {MqConst.ROUTING_PAYMENT_PAY}))// 路由key
    public void mqReceiver(Channel channel, Message message, String messageBody) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();// 消息标记
        // 修改订单状态为已支付
        Map map = JSON.parseObject(messageBody, Map.class);
        orderService.update(map);

        // 手动签收消息
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 测试mq
      * @param channel
     * @param message
     * @param messageBody
     */
//    @SneakyThrows
//    @RabbitListener(bindings = @QueueBinding(// 队列绑定交换机并设置路由key
//            exchange = @Exchange(value = "test_exchange1"),// 交换机名称
//            value = @Queue(value = "test_queu1"),// 队列名称
//            key = {"test"}))// 路由key
//    public void test(Channel channel, Message message, String messageBody) {
//        long deliveryTag = message.getMessageProperties().getDeliveryTag();// 消息标记
//
//        // 手动签收消息
//        channel.basicAck(deliveryTag, false);
//    }

}