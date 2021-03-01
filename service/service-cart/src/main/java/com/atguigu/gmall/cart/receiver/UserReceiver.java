package com.atguigu.gmall.cart.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.mq.constant.MqConst;
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

/**
 * 用户登录监听器
 */
@Component
public class UserReceiver {

    @Autowired
    CartInfoService cartInfoService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(// 队列绑定交换机并设置路由key
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_USER_LOGIN),// 交换机名称
            value = @Queue(value = MqConst.QUEUE_USER_LOGIN),// 队列名称
            key = {MqConst.ROUTING_USER_LOGIN}))// 路由key
    public void loginReceiver(Channel channel, Message message, String messageBody) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();// 消息标记

        // 合并购物车
        Map map = JSON.parseObject(messageBody, Map.class);
        String userId = (String)map.get("userId");
        String userTempId = (String)map.get("userTempId");
        cartInfoService.mergeCart(userId,userTempId);

        // 手动签收
        channel.basicAck(deliveryTag, false);
    }

}
