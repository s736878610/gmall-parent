package com.atguigu.gmall.payment.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.mq.constant.MqConst;
import com.atguigu.gmall.mq.service.RabbitService;
import com.atguigu.gmall.payment.service.PaymentService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 定时查询支付交易结果的监听器
 */
@Component
public class PaymentReceiver {

    @Autowired
    PaymentService paymentService;
    @Autowired
    RabbitService rabbitService;

    /**
     * 定时查询支付交易结果的监听器
     *
     * @param channel
     * @param message
     * @param orderInfoJson
     */
    @SneakyThrows
    @RabbitListener(queues = MqConst.QUEUE_PAYMENT_CHECK)
    public void paymentConsumer(Channel channel, Message message, String orderInfoJson) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();// 消息标记
        OrderInfo orderInfo = JSON.parseObject(orderInfoJson, OrderInfo.class);

        // 调用检查支付结果服务
        // TODO

        // 如果查询支付结果是交易未创建，延时重复查询，查询5次
        Long count = orderInfo.getCount();
        if (count < 5) {
            orderInfo.setCount(count + 1);
            // 发送延时消息队列  定时检查是否支付成功
            rabbitService.sendDelayMessage(MqConst.EXCHANGE_PAYMENT_CHECK,
                    MqConst.ROUTING_PAYMENT_CHECK,
                    JSON.toJSONString(orderInfo),
                    5,
                    TimeUnit.SECONDS);
        }

        // 幂等性检查
        String payStatus = paymentService.getPayStatus(orderInfo.getOutTradeNo());
        if (payStatus.equals("UNPAID")) {
            // 修改支付订单操作
            // TODO
        }

        // 手动签收消息
        channel.basicAck(deliveryTag, false);
    }

}
