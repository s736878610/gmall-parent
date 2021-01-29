package com.atguigu.gmall.item.client;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.Map;

/**
 *  item远程调用
 */
@FeignClient(value = "service-item")// 要调用的服务名
public interface ItemFeignClient {

    @GetMapping("api/product/{skuId}")// 要调用的接口路径完整路径
    Map<String, Object> getItem(@PathVariable("skuId") Long skuId);
}
