package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    String alipayTradePagePay(Long orderId);

    void updatePayment(PaymentInfo paymentInfo);

    Map<String, Object> alipayQuery(String outTradeNo);
}
