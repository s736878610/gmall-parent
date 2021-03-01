package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/activity")
public class ActivityApiController {

    @Autowired
    SeckillGoodsService seckillGoodsService;

    /**
     * 秒杀首页数据
     *
     * @return
     */
    @RequestMapping("seckill/getSeckillGoodsList")
    List<SeckillGoods> getSeckillGoodsList() {
        List<SeckillGoods> seckillGoodsList = seckillGoodsService.getSeckillGoodsList();
        return seckillGoodsList;
    }

    /**
     * 商品入库
     *
     * @return
     */
    @RequestMapping("seckill/putSeckillGoods")
    Result putSeckillGoods() {
        seckillGoodsService.putSeckillGoods();
        return Result.ok();
    }

    /**
     * 获取商品状态
     * @param skuId
     * @return
     */
    @RequestMapping("seckill/getSkuStatus/{skuId}")
    Result getSkuStatus(@PathVariable("skuId") String skuId) {
        String skuStatus = (String) CacheHelper.get(skuId);
        return Result.ok(skuId + ":" + skuStatus);
    }

    /**
     * 秒杀商品详情
     * @param skuId
     * @return
     */
    @RequestMapping("seckill/getSeckillGoods/{skuId}")
    SeckillGoods getSeckillGoods(@PathVariable("skuId") Long skuId){
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(skuId);
        return seckillGoods;
    }
}
