package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.bouncycastle.math.raw.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 支付页面跳转控制器
 */
@Controller
public class PayController {

    @Autowired
    OrderFeignClient orderFeignClient;

    /**
     * 跳转到选择支付方式页面
     * @return
     */
    @RequestMapping("pay.html")
    public String index(Long orderId, Model model){
        OrderInfo orderInfo = orderFeignClient.getOrderInfoById(orderId);
        model.addAttribute("orderInfo",orderInfo);
        return "payment/pay";
    }

}
