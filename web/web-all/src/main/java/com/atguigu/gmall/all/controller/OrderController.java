package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 订单页面跳转控制器
 */
@Controller
public class OrderController {

    @Autowired
    UserFeignClient userFeignClient;

    @RequestMapping("myOrder.html")
    public String myOrder(HttpServletRequest request) {
        String userId = request.getHeader("userId");
        if (StringUtils.isNotEmpty(userId)) {
            System.out.println("userId：" + userId);
        }

        String userTempId = request.getHeader("userTempId");
        if (StringUtils.isNotEmpty(userTempId)) {
            System.out.println("userTempId：" + userTempId);
        }
        return "order/myOrder";
    }

}
