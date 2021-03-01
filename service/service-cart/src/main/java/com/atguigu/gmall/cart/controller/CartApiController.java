package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.result.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired
    CartInfoService cartInfoService;

    /**
     * 从请求头中获取userId
     * @param request
     * @return
     */
    private String getUserIdByHeader(HttpServletRequest request) {
        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理的结果
        if (StringUtils.isEmpty(userId)) {
            userId = request.getHeader("userTempId");
        }
        return userId;
    }
    
    /**
     * 添加/修改购物车
     *
     * @param skuId
     * @param skuNum
     */
    @RequestMapping("addCart/{skuId}/{skuNum}")
    void addCart(HttpServletRequest request, @PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum) {
        String userId = getUserIdByHeader(request);

        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuNum(skuNum);
        cartInfo.setUserId(userId);

        cartInfoService.addCart(cartInfo);

    }

    /**
     * 购物车列表展示
     *
     * @return
     */
    @RequestMapping("cartList")
    public Result cartList(HttpServletRequest request) {
        String userId = getUserIdByHeader(request);
        
        List<CartInfo> CartInfoList = cartInfoService.cartList(userId);
        return Result.ok(CartInfoList);
    }
    
    @RequestMapping("cartListInner")
    public List<CartInfo> cartListInner(HttpServletRequest request) {
        String userId = getUserIdByHeader(request);
        return cartInfoService.cartList(userId);
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
        String userId = getUserIdByHeader(request);

        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        cartInfo.setIsChecked(isChecked);
        cartInfoService.checkCart(cartInfo);

        return Result.ok();
    }

    /**
     * 提交订单后  删除购物车选中商品
     * @param userId
     */
    @RequestMapping("api/cart/delCart/{userId}")
    void delCart(@PathVariable("userId") String userId){
        // TODO
    }

}
