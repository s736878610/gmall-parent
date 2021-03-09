package com.atguigu.gmall.activity.reciver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.model.user.UserRecode;
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

/**
 * 秒杀商品微服务  消息接收端
 */
@Component
public class SeckillReceiver {

    @Autowired
    SeckillGoodsService seckillGoodsService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(// 队列绑定交换机并设置路由key
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),// 交换机名称
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER),// 队列名称
            key = {MqConst.ROUTING_SECKILL_USER}))// 路由key
    public void seckillConsumer(Channel channel, Message message, String userRecodeJson) {
        // 尝试抢购，生成订单
        UserRecode userRecode = JSON.parseObject(userRecodeJson, UserRecode.class);
        seckillGoodsService.seckillOrderConsume(userRecode);

        // 签收消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
