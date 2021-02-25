package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

public interface OrderService {
    String save(OrderInfo orderInfo, String userId);

    boolean checkTradeNo(String userId, String tradeNo);

    String genTradeNo(String userId);

    OrderInfo getOrderInfoById(Long orderId);
}
