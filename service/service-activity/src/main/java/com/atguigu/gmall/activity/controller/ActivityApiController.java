package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.result.ResultCodeEnum;
import com.atguigu.gmall.util.MD5;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/activity/seckill")
public class ActivityApiController {

    @Autowired
    SeckillGoodsService seckillGoodsService;
    @Autowired
    OrderFeignClient orderFeignClient;

    /**
     * 秒杀首页数据
     *
     * @return
     */
    @RequestMapping("getSeckillGoodsList")
    List<SeckillGoods> getSeckillGoodsList() {
        List<SeckillGoods> seckillGoodsList = seckillGoodsService.getSeckillGoodsList();
        return seckillGoodsList;
    }

    /**
     * 商品入库(发布商品)
     *
     * @return
     */
    @RequestMapping("putSeckillGoods")
    Result putSeckillGoods() {
        seckillGoodsService.putSeckillGoods();
        return Result.ok();
    }

    /**
     * 获取商品状态
     *
     * @param skuId
     * @return
     */
    @RequestMapping("getSkuStatus/{skuId}")
    Result getSkuStatus(@PathVariable("skuId") String skuId) {
        String skuStatus = (String) CacheHelper.get(skuId);
        return Result.ok(skuId + ":" + skuStatus);
    }

    /**
     * 秒杀商品详情
     *
     * @param skuId
     * @return
     */
    @RequestMapping("getSeckillGoods/{skuId}")
    SeckillGoods getSeckillGoods(@PathVariable("skuId") Long skuId) {
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(skuId);
        return seckillGoods;
    }

    /**
     * 生成抢购码
     */
    @RequestMapping("auth/getSeckillSkuIdStr/{skuId}")
    Result getSeckillSkuIdStr(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        // 判断服务器状态(针对某个skuId)
        if (checkStatus(skuId)) return Result.fail();

        String userId = request.getHeader("userId");
        String seckillCode = MD5.encrypt(skuId + userId);
        return Result.ok(seckillCode);
    }

    /**
     * 发送消息队列，让其他微服务消费，生成订单
     *
     * @param skuId
     * @param skuIdStr 抢购码
     * @param request
     * @return
     */
    @RequestMapping("auth/seckillOrder/{skuId}")
    Result seckillOrder(@PathVariable("skuId") Long skuId, String skuIdStr, HttpServletRequest request) {
        // 判断服务器状态(针对某个skuId)
        if (checkStatus(skuId)) return Result.fail();

        String userId = request.getHeader("userId");
        // 判断抢购码(这里没实现)
        String seckillCode = MD5.encrypt(skuId + userId);

        // 限制用户访问频率
        boolean b = seckillGoodsService.userLock(userId);
        if (b) {
            // 发送消息队列
            seckillGoodsService.seckillOrder(skuId, userId);
        }
        return Result.ok();
    }

    /**
     * 检查抢购结果
     *
     * @param skuId
     * @param request
     * @return
     */
    @RequestMapping("auth/checkOrder/{skuId}")
    Result checkOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
        String userId = request.getHeader("userId");

        // 下单成功
        String orderStr = seckillGoodsService.getOrderStr(userId);
        if (StringUtils.isNotEmpty(orderStr)) {
            return Result.build(orderStr, ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }

        // 下单中
        OrderRecode orderRecode = seckillGoodsService.getOrderRecode(userId);
        if (orderRecode != null) {
            return Result.build(null, ResultCodeEnum.SECKILL_SUCCESS);
        }

        // 无库存
        if (checkStatus(skuId)) {
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);
        }

        // 排队中
        return Result.build(null, ResultCodeEnum.SECKILL_RUN);
    }

    /**
     * 获取预订单
     *
     * @param userId
     * @return
     */
    @RequestMapping("getOrderRecode/{userId}")
    OrderRecode getOrderRecode(@PathVariable("userId") String userId) {
        OrderRecode orderRecode = seckillGoodsService.getOrderRecode(userId);
        return orderRecode;
    }

    /**
     * 提交订单
     *
     * @return
     */
    @RequestMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        String userId = request.getHeader("userId");

        // 检查是否有预订单，确认是否重复提交订单
        OrderRecode orderRecode = seckillGoodsService.getOrderRecode(userId);
        if (orderRecode != null) {
            // 删除redis中的预订单
            seckillGoodsService.deleteOrderRecode(userId);

            // 数据库存一份
            String orderId = orderFeignClient.saveSeckillOrder(orderInfo, userId);
            if (StringUtils.isEmpty(orderId)) {
                return Result.fail();
            }

            // redis存一份orderId，给排队页面提供查询参数
            seckillGoodsService.saveSeckillOrderCache(Long.parseLong(orderId), userId);

            // 返回200状态码  前端跳转到提交成功页面  用户选择支付方式
            return Result.ok(orderId);

        } else {
            // 订单重复提交  跳出
            return Result.fail();
        }

    }


    /**
     * 查商品状态
     *
     * @param skuId
     * @return
     */
    private boolean checkStatus(@PathVariable("skuId") Long skuId) {
        String seckillStatus = (String) CacheHelper.get(skuId + "");
        if (seckillStatus.equals("0")) {
            return true;
        }
        return false;
    }

}
