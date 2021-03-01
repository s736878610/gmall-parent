package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.atguigu.gmall.mq.config.DelayedMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消费者
 */
@Component
public class MqTestReceiver {

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(// 队列绑定交换机并设置路由key
            exchange = @Exchange(value = "test_exchange"),// 交换机名称
            value = @Queue(value = "test_queue"),// 队列名称
            key = {"test"}))// 路由key
    public void mqReceiver(Channel channel, Message message, String messageBody) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();// 消息标记
        System.out.println("mqReceiver接收到的消息是：" + messageBody);
        try {
            //int i = 1/0;
            /**
             * channel.basicAck()：手动签收消息
             *      参数一：消息标记
             *      参数二：是否批量接收消息
             */
            channel.basicAck(deliveryTag, false);

            //channel.basicNack(deliveryTag,false,true);
        } catch (Exception e) {
            e.printStackTrace();
            /**
             * channel.basicNack()：回滚消息
             *      参数一：消息标记
             *      参数二：是否批量接收消息
             *      参数三：是否回滚(true 回到队列)
             */
            channel.basicNack(deliveryTag, false, true);
        }
    }

    /**
     * 死信队列监听器
     * @param channel
     * @param message
     * @param messageBody
     */
    @SneakyThrows
    @RabbitListener(queues = DeadLetterMqConfig.queue_dead_2)
    public void mqDeadLetterReceiver(Channel channel, Message message, String messageBody){
        long deliveryTag = message.getMessageProperties().getDeliveryTag();// 消息标记
        System.out.println("mqDeadLetterReceiver接收到的消息是：" + messageBody);
        channel.basicAck(deliveryTag, false);
    }

    /**
     * 插件实现的延时队列监听器
     * @param channel
     * @param message
     * @param messageBody
     */
    @SneakyThrows
    @RabbitListener(queues = DelayedMqConfig.queue_delay_1)
    public void mqDelayReceiver(Channel channel, Message message, String messageBody){
        long deliveryTag = message.getMessageProperties().getDeliveryTag();// 消息标记
        System.out.println("mqDeadLetterReceiver接收到的消息是：" + messageBody);
        channel.basicAck(deliveryTag, false);
    }


}
