package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    ActivityFeignClient activityFeignClient;

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


}
