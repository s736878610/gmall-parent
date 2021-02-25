package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 结算页面跳转控制器
 */
@Controller
public class TradeController {

    @Autowired
    CartFeignClient cartFeignClient;
    @Autowired
    OrderFeignClient orderFeignClient;
    @Autowired
    UserFeignClient userFeignClient;
    @Autowired
    ProductFeignClient productFeignClient;

    /**
     * 跳转到结算页面 (核对订单信息)
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("trade.html")
    public String trade(HttpServletRequest request, Model model) {
        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理的结果
        if (StringUtils.isEmpty(userId)) {
            userId = request.getHeader("userTempId");
        }

        // 调用结算服务(本项目业务不需要在这里结算)


        // 订单详情数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        List<CartInfo> cartInfoList = cartFeignClient.cartListInner();
        BigDecimal totalAmount = new BigDecimal("0");
        if (cartInfoList != null && cartInfoList.size() > 0) {
            for (CartInfo cartInfo : cartInfoList) {
                OrderDetail orderDetail = new OrderDetail();
                if (cartInfo.getIsChecked() == 1) {// 只要用户选中的购物车数据
                    orderDetail.setSkuId(cartInfo.getSkuId());
                    orderDetail.setSkuName(cartInfo.getSkuName());
                    orderDetail.setSkuNum(cartInfo.getSkuNum());
                    orderDetail.setImgUrl(cartInfo.getImgUrl());
                    SkuInfo skuInfo = productFeignClient.getSkuInfoById(cartInfo.getSkuId());
                    orderDetail.setOrderPrice(skuInfo.getPrice());
                    totalAmount = totalAmount.add(cartInfo.getCartPrice());// 订单总价
                }
                orderDetailList.add(orderDetail);
            }
        }

        // 收货信息数据
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);

        // 生成一个订单码，页面放一个，redis放一个
        String tradeNo = orderFeignClient.genTradeNo();

        // 封装数据
        model.addAttribute("detailArrayList",orderDetailList);
        model.addAttribute("userAddressList",userAddressList);
        model.addAttribute("totalAmount",totalAmount);
        model.addAttribute("tradeNo",tradeNo);
        return "order/trade";
    }

}
