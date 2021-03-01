package com.atguigu.gmall.cart.client;


import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@FeignClient(value = "service-cart")// 要调用的服务名
public interface CartFeignClient {

    @RequestMapping("api/cart/addCart/{skuId}/{skuNum}")// 要调用的接口路径完整路径
    void addCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum")Integer skuNum);

    @RequestMapping("api/cart/cartListInner")
    List<CartInfo> cartListInner();

    @RequestMapping("api/cart/delCart/{userId}")
    void delCart(@PathVariable("userId") String userId);
}
