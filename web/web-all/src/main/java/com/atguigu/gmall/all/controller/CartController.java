package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 购物车展示控制器
 */
@Controller
public class CartController {

    @Autowired
    CartFeignClient cartFeignClient;


    /**
     * 添加/修改购物车
     * @param skuId
     * @param skuNum
     * @return
     */
    @RequestMapping("addCart.html")
    public String addCart(Long skuId,Integer skuNum){
        // 调用购物车服务，添加购物车信息
        cartFeignClient.addCart(skuId,skuNum);

        return "redirect:http://cart.gmall.com/cart/addCartSuccess.html?skuId=?&skuNum=?";// 静态传参
    }

    /**
     * 购物车列表展示
     * @return
     */
    @RequestMapping({"cart/cart.html","cart.html"})
    public String cartList(){

        return "cart/index";
    }

}
