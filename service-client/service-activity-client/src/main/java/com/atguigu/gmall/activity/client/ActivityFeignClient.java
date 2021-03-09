package com.atguigu.gmall.activity.client;

import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("service-activity")
public interface ActivityFeignClient {

    @RequestMapping("api/activity/seckill/getSeckillGoodsList")
    List<SeckillGoods> getSeckillGoodsList();

    @RequestMapping("api/activity/seckill/getSeckillGoods/{skuId}")
    SeckillGoods getSeckillGoods(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/activity/seckill/getOrderRecode/{userId}")
    OrderRecode getOrderRecode(@PathVariable("userId") String userId);
}
