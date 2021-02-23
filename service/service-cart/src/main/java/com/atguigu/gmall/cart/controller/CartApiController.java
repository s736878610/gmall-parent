package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import java.io.LineNumberInputStream;
import java.util.List;

@RestController
@RequestMapping("api/cart")
@CrossOrigin
public class CartApiController {

    @Autowired
    CartInfoService cartApiService;

    /**
     * 添加/修改购物车
     * @param skuId
     * @param skuNum
     */
    @RequestMapping("addCart/{skuId}/{skuNum}")
    void addCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum")Integer skuNum){
        String userId = "1";// 通过sso系统和网关的鉴权拦截器处理的结果

        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuNum(skuNum);
        cartInfo.setUserId(userId);

        cartApiService.addCart(cartInfo);

    }

    /**
     * 购物车列表展示
     * @return
     */
    @RequestMapping("cartList")
    public Result cartList(){
        String userId = "1";// 通过sso系统和网关的鉴权拦截器处理的结果

        List<CartInfo> CartInfoList = cartApiService.cartList(userId);
        return Result.ok(CartInfoList);
    }

    /**
     * 更新购物车选中状态
     * @param skuId
     * @param isChecked
     * @return
     */
    @RequestMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable("skuId") Long skuId,@PathVariable("isChecked")Integer isChecked){
        String userId = "1";// 通过sso系统和网关的鉴权拦截器处理的结果

        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        cartInfo.setIsChecked(isChecked);
        cartApiService.checkCart(cartInfo);

        return Result.ok();
    }


}
