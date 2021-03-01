package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 秒杀首页数据
     *
     * @return
     */
    @Override
    public List<SeckillGoods> getSeckillGoodsList() {
        List<SeckillGoods> seckillGoodsList = (List<SeckillGoods>) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();// boundHashOps(key).values() 获取hash中所有值
        return seckillGoodsList;
    }

    /**
     * 商品入库(redis)
     */
    @Override
    public void putSeckillGoods() {
        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(null);
        for (SeckillGoods seckillGoods : seckillGoodsList) {
            Integer stockCount = seckillGoods.getStockCount();// 秒杀商品的库存数量
            for (int i = 0; i < stockCount; i++) {
                redisTemplate.opsForList().leftPush(RedisConst.SECKILL_STOCK_PREFIX + seckillGoods.getSkuId(), seckillGoods.getSkuId() + "");
            }
            // put(seckill:goods , skuId , 商品)
            redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS, seckillGoods.getSkuId() + "", seckillGoods);

            // 发布商品状态
            redisTemplate.convertAndSend("seckillpush", seckillGoods.getSkuId() + ":" + "1");
        }
    }

    /**
     * 秒杀商品详情
     *
     * @param skuId
     * @return
     */
    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {
        SeckillGoods seckillGoods = (SeckillGoods)redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId + "");// boundHashOps(key).get(key) 获取hash中单个值
        return seckillGoods;
    }
}
