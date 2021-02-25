package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.result.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import java.io.LineNumberInputStream;
import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired
    CartInfoService cartApiService;

    /**
     * 添加/修改购物车
     *
     * @param skuId
     * @param skuNum
     */
    @RequestMapping("addCart/{skuId}/{skuNum}")
    void addCart(HttpServletRequest request, @PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum) {
        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理的结果
        if (StringUtils.isEmpty(userId)) {
            userId = request.getHeader("userTempId");
        }

        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuNum(skuNum);
        cartInfo.setUserId(userId);

        cartApiService.addCart(cartInfo);

    }

    /**
     * 购物车列表展示
     *
     * @return
     */
    @RequestMapping("cartList")
    public Result cartList(HttpServletRequest request) {
        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理的结果
        if (StringUtils.isEmpty(userId)) {
            userId = request.getHeader("userTempId");
        }

        List<CartInfo> CartInfoList = cartApiService.cartList(userId);
        return Result.ok(CartInfoList);
    }


    @RequestMapping("cartListInner")
    public List<CartInfo> cartListInner(HttpServletRequest request) {
        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理的结果
        if (StringUtils.isEmpty(userId)) {
            userId = request.getHeader("userTempId");
        }
        return cartApiService.cartList(userId);
    }

    /**
     * 更新购物车选中状态
     *
     * @param skuId
     * @param isChecked
     * @return
     */
    @RequestMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(HttpServletRequest request, @PathVariable("skuId") Long skuId, @PathVariable("isChecked") Integer isChecked) {
        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理的结果
        if (StringUtils.isEmpty(userId)) {
            userId = request.getHeader("userTempId");
        }

        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        cartInfo.setIsChecked(isChecked);
        cartApiService.checkCart(cartInfo);

        return Result.ok();
    }


}
