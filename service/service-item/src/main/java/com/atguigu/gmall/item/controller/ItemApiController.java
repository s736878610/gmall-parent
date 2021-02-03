package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemApiService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("api/product/")
public class ItemApiController {

    @Autowired
    private ItemApiService itemApiService;

    /**
     * 获取sku基本信息与图片信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("{skuId}")
    public Map<String, Object> getItem(@PathVariable("skuId") Long skuId) {
        return itemApiService.getItem(skuId);
    }

}
