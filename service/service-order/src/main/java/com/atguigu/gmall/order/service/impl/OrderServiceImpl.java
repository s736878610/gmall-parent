package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    CartFeignClient cartFeignClient;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    ProductFeignClient productFeignClient;

    /**
     * 提交订单  保存订单信息
     *
     * @param orderInfo
     * @param userId
     * @return
     */
    @Transactional
    @Override
    public String save(OrderInfo orderInfo, String userId) {
        // 订单进度：未支付
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.getComment());
        orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());
        // 订单总金额
        orderInfo.sumTotalAmount();// 调用内部方法计算总金额
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();// 订单详情List
        // 外部订单id  (全局唯一，实际是根据公司规定)
        String timeMillis = System.currentTimeMillis() + "";
        String outTradeNo = "atguigu" + timeMillis;
        orderInfo.setOutTradeNo(outTradeNo);
        // 创建时间
        orderInfo.setCreateTime(new Date());
        // 过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);// 当前时间向后推一天
        Date time = calendar.getTime();
        orderInfo.setExpireTime(time);
        // 订单图片
        orderInfo.setImgUrl(orderDetailList.get(0).getImgUrl());
        // 支付方式：在线支付
        orderInfo.setPaymentWay(PaymentWay.ONLINE.getComment());
        // userId
        orderInfo.setUserId(Long.parseLong(userId));


        orderInfoMapper.insert(orderInfo);
        Long orderId = orderInfo.getId();

        // 订单详情
        for (OrderDetail orderDetail : orderDetailList) {
            // 检查库存是否足够

            // 检查价格是否变动
            SkuInfo skuInfo = productFeignClient.getSkuInfoById(orderDetail.getSkuId());
            BigDecimal price = skuInfo.getPrice();
            BigDecimal orderPrice = orderDetail.getOrderPrice();
            int i = price.compareTo(orderPrice);
            if (i != 0){
                return null;
            }

            orderDetail.setOrderId(orderId);
            orderDetailMapper.insert(orderDetail);
        }


        // 删除购物车数据
        // cartFeignClient.delCart();

        return orderId + "";
    }


    /**
     * 检查交易码  是否重复提交订单
     *
     * @param tradeNo
     * @return
     */
    @Override
    public boolean checkTradeNo(String userId, String tradeNo) {
        boolean b = false;
        String tradeNoCache = (String) redisTemplate.opsForValue().get(RedisConst.USER_KEY_PREFIX + userId + ":tradeNo");
        if (StringUtils.isNotEmpty(tradeNoCache) && tradeNoCache.equals(tradeNo)) {
            b = true;
        }
        return b;
    }

    /**
     * 生成交易码
     *
     * @param userId
     * @return
     */
    @Override
    public String genTradeNo(String userId) {
        // 生成交易码
        String tradeNo = UUID.randomUUID().toString();
        // 放入缓存
        redisTemplate.opsForValue().set(RedisConst.USER_KEY_PREFIX + userId + ":tradeNo", tradeNo);
        return tradeNo;
    }

    /**
     * 根据id获得订单信息
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);// 订单信息
        QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(queryWrapper);// 订单详情
        orderInfo.setOrderDetailList(orderDetailList);// 封装订单详情

        return orderInfo;
    }

}
