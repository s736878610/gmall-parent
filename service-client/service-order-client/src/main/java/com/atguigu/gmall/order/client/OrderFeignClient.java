package com.atguigu.gmall.order.client;

import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "service-order")
public interface OrderFeignClient {

    @RequestMapping("api/order/genTradeNo")
    String genTradeNo();

    @RequestMapping("api/order/genTradeNo/{orderId}")
    OrderInfo getOrderInfoById(@PathVariable("orderId") Long orderId);

    @RequestMapping("api/order/saveSeckillOrder/{userId}")
    String saveSeckillOrder(@RequestBody OrderInfo orderInfo, @PathVariable("userId") String userId);
}
