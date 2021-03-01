package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.mq.constant.MqConst;
import com.atguigu.gmall.mq.service.RabbitService;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.mapper.PaymentMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    AlipayClient alipayClient;
    @Autowired
    OrderFeignClient orderFeignClient;
    @Autowired
    PaymentMapper paymentMapper;
    @Autowired
    RabbitService rabbitService;

    /**
     * 生成阿里支付 支付页面
     *
     * @param orderId
     * @return
     */
    @Override
    public String alipayTradePagePay(Long orderId) {
        OrderInfo orderInfo = orderFeignClient.getOrderInfoById(orderId);
        // 公共参数
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(AlipayConfig.return_payment_url);// 回调页面
        request.setNotifyUrl(AlipayConfig.notify_payment_url);// 异步回调通知接口

        // 请求参数
        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());// 外部订单号
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");// 支付宝的产品名称
        map.put("total_amount", 0.01);// 订单金额
        map.put("subject", orderInfo.getOrderDetailList().get(0).getSkuName());// 订单标题
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradePagePayResponse response = null;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());// 金额
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());// 外部交易码
        paymentInfo.setOrderId(orderId);
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.toString());// 支付状态
        paymentInfo.setPaymentType(PaymentType.ALIPAY.getComment());
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());// 标题
        paymentInfo.setCreateTime(new Date());

        paymentMapper.insert(paymentInfo);

        if (response.isSuccess()) {
            System.out.println("生成支付二维码页面接口：调用成功");
        } else {
            System.out.println("生成支付二维码页面接口：调用失败");
        }

        return response.getBody();
    }

    /**
     * 修改支付信息
     *
     * @param paymentInfo
     */
    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", paymentInfo.getOutTradeNo());
        paymentMapper.update(paymentInfo, queryWrapper);

        // 使用rabbitmq发送订单已支付的消息，order服务消费消息，将订单修改为已支付
        PaymentInfo payment = paymentMapper.selectOne(queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("orderId",payment.getOrderId());
        map.put("outTradeNo",payment.getOutTradeNo());
        map.put("tradeNo",payment.getTradeNo());
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,// 交换机名称
                MqConst.ROUTING_PAYMENT_PAY,// 路由规则
                JSON.toJSONString(map));// 消息内容

    }

    /**
     * 查询交易状态
     *
     * @param outTradeNo
     * @return
     */
    @Override
    public Map<String, Object> alipayQuery(String outTradeNo) {
        Map<String, Object> result = new HashMap<>();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

        // 请求参数
        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", outTradeNo);
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (response.isSuccess()) {
            System.out.println("查询交易状态接口：调用成功");
            result.put("状态：", response.getTradeStatus());
        } else {
            System.out.println("查询交易状态接口：调用失败");
            result.put("状态：", "调用失败");
        }

        return result;
    }
}
