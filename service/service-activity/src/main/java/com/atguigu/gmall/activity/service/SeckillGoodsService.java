package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserRecode;

import java.util.List;

public interface SeckillGoodsService {

    List<SeckillGoods> getSeckillGoodsList();

    void putSeckillGoods();

    SeckillGoods getSeckillGoods(Long skuId);

    void seckillOrder(Long skuId, String userId);

    boolean userLock(String userId);

    void seckillOrderConsume(UserRecode userRecode);

    String getOrderStr(String userId);

    OrderRecode getOrderRecode(String userId);

    void deleteOrderRecode(String userId);

    void saveSeckillOrderCache(Long orderId, String userId);
}
