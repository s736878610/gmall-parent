package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

public interface CartInfoService {
    void addCart(CartInfo cartInfo);

    List<CartInfo> cartList(String userId);

    void checkCart(CartInfo cartInfo);
}
