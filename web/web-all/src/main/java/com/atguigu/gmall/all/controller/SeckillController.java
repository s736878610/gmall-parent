package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.atguigu.gmall.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    ActivityFeignClient activityFeignClient;
    @Autowired
    UserFeignClient userFeignClient;
    @Autowired
    ProductFeignClient productFeignClient;

    /**
     * 秒杀首页
     *
     * @param model
     * @return
     */
    @GetMapping("seckill.html")
    public String index(Model model) {
        List<SeckillGoods> seckillGoodsList = activityFeignClient.getSeckillGoodsList();
        model.addAttribute("list", seckillGoodsList);
        return "seckill/index";
    }

    /**
     * 秒杀商品详情
     *
     * @param skuId
     * @param model
     * @return
     */
    @RequestMapping("seckill/{skuId}.html")
    public String getSeckillGoods(@PathVariable("skuId") Long skuId, Model model) {
        SeckillGoods seckillGoods = activityFeignClient.getSeckillGoods(skuId);
        model.addAttribute("item", seckillGoods);
        return "seckill/item";
    }

    /**
     * 跳转排队页面
     *
     * @param skuId
     * @param skuIdStr 页面携带的抢购码
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("seckill/queue.html")
    public String queue(Long skuId, String skuIdStr, HttpServletRequest request, Model model) {
        String userId = request.getHeader("userId");
        String seckillCode = MD5.encrypt(skuId + userId);

        // 确保用户是通过"立即抢购"按钮跳转过来的
        if (seckillCode.equals(skuIdStr)) {
            model.addAttribute("skuId", skuId);
            model.addAttribute("skuIdStr", skuIdStr);
            return "seckill/queue";
        } else {
            model.addAttribute("message", "请求不合法");
            return "seckill/fail";
        }
    }

    /**
     * 用户抢单成功，点击"去下单"按钮，跳转页面
     *
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("seckill/trade.html")
    public String trade(HttpServletRequest request, Model model) {
        String userId = request.getHeader("userId");

        // 收货地址
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);

        // 订单详情
        OrderRecode orderRecode = activityFeignClient.getOrderRecode(userId);
        if (orderRecode != null) {
            SeckillGoods seckillGoods = activityFeignClient.getSeckillGoods(orderRecode.getSeckillGoods().getSkuId());
            SkuInfo skuInfo = productFeignClient.getSkuInfoById(orderRecode.getSeckillGoods().getSkuId());
            // 封装数据
            List<OrderDetail> orderDetailList = new ArrayList<>();
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(skuInfo.getId());
            orderDetail.setSkuName(skuInfo.getSkuName());
            orderDetail.setSkuNum(1);
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());
            orderDetail.setOrderPrice(seckillGoods.getCostPrice());
            orderDetailList.add(orderDetail);

            BigDecimal totalAmount = new BigDecimal("0");
            totalAmount = totalAmount.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("userAddress", userAddressList);
            model.addAttribute("detailArrayList", orderDetailList);
        }else {
            model.addAttribute("totalAmount", new BigDecimal("0"));
        }

        return "seckill/trade";
    }

}
