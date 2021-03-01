package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.result.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    OrderService orderService;

    /**
     * 提交订单
     * @param request
     * @param tradeNo
     * @param orderInfo
     * @return
     */
    @RequestMapping("auth/submitOrder")
    public Result submitOrder(HttpServletRequest request,String tradeNo, @RequestBody OrderInfo orderInfo){
        String userId = request.getHeader("userId");
        if (StringUtils.isEmpty(userId)){
            return Result.fail();
        }

        // 检查订单码，确认是否重复提交订单
        boolean b = orderService.checkTradeNo(userId,tradeNo);
        if (b){
            // 保存订单信息，返回订单id
            String orderId = orderService.save(orderInfo,userId);
            if (StringUtils.isEmpty(orderId)){
                return Result.fail();
            }

            return Result.ok(orderId);// 返回200状态码  前端跳转到提交成功页面  用户选择支付方式
        }else {
            // 订单重复提交  跳出
            return Result.fail();
        }

    }

    /**
     * 生成交易码
     * @return
     */
    @RequestMapping("genTradeNo")
    String genTradeNo(HttpServletRequest request){
        String userId = request.getHeader("userId");
        return orderService.genTradeNo(userId);
    }

    /**
     * 根据id获得订单信息
     * @param orderId
     * @return
     */
    @RequestMapping("genTradeNo/{orderId}")
    OrderInfo getOrderInfoById(@PathVariable("orderId") Long orderId){
        return orderService.getOrderInfoById(orderId);
    }

}
