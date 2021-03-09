package com.atguigu.gmall.activity.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserRecode;
import com.atguigu.gmall.mq.constant.MqConst;
import com.atguigu.gmall.mq.service.RabbitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RabbitService rabbitService;

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
     * 商品入库(redis发布商品)
     */
    @Override
    public void putSeckillGoods() {
        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(null);
        for (SeckillGoods seckillGoods : seckillGoodsList) {
            if (seckillGoods.getStatus().equals("1")) {// 确保商品处于发布状态
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
    }

    /**
     * 秒杀商品详情页面
     *
     * @param skuId
     * @return
     */
    @Override
    public SeckillGoods getSeckillGoods(Long skuId) {
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId + "");// boundHashOps(key).get(key) 获取hash中单个值
        return seckillGoods;
    }

    /**
     * 发送消息队列，让其他微服务消费，生成订单
     *
     * @param skuId
     * @param userId
     */
    @Override
    public void seckillOrder(Long skuId, String userId) {
        UserRecode userRecode = new UserRecode();
        userRecode.setSkuId(skuId);
        userRecode.setUserId(userId);

        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER,
                MqConst.ROUTING_SECKILL_USER,
                JSON.toJSONString(userRecode));
    }

    /**
     * 限制用户访问频率  redis分布式锁
     *
     * @param userId
     * @return
     */
    @Override
    public boolean userLock(String userId) {
        return redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER + userId + ":lock", "1", 30, TimeUnit.SECONDS);
    }

    /**
     * 尝试抢购，生成订单
     *
     * @param userRecode
     */
    @Override
    public void seckillOrderConsume(UserRecode userRecode) {
        // 抢商品
        String skuId = (String) redisTemplate.opsForList().leftPop(RedisConst.SECKILL_STOCK_PREFIX + userRecode.getSkuId());
        if (StringUtils.isNotEmpty(skuId)) {
            // 有货，生成预订单
            OrderRecode orderRecode = new OrderRecode();
            orderRecode.setNum(1);
            orderRecode.setUserId(userRecode.getUserId());
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId);
            orderRecode.setSeckillGoods(seckillGoods);

            // 预订单放入redis ， 使用hash结构存储
            redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(userRecode.getUserId(), orderRecode);
        } else {
            // 商品售罄，发布redis消息，更改服务器状态(针对某个skuId的状态)
            redisTemplate.convertAndSend("seckillpush", userRecode.getSkuId() + ":" + "0");
        }

    }

    /**
     * 获取成功抢到商品的订单Id
     *
     * @param userId
     * @return
     */
    @Override
    public String getOrderStr(String userId) {
        String orderStr = (String) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).get(userId);
        return orderStr;
    }

    /**
     * 获取预定单
     *
     * @param userId
     * @return
     */
    @Override
    public OrderRecode getOrderRecode(String userId) {
        OrderRecode orderRecode = (OrderRecode) redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);
        return orderRecode;
    }

    /**
     * 删除预订单
     *
     * @param userId
     */
    @Override
    public void deleteOrderRecode(String userId) {
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).delete(userId);
    }

    /**
     * 保存已经下单成功的用户信息
     *
     * @param orderId
     * @param userId
     */
    @Override
    public void saveSeckillOrderCache(Long orderId, String userId) {
        redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).put(userId, orderId + "");
    }


}
