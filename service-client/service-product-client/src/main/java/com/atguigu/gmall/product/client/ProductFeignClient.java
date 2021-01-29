package com.atguigu.gmall.product.client;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(value = "service-product")// 要调用的服务名
public interface ProductFeignClient {

    @RequestMapping("api/product/getSkuInfoById/{skuId}")// 要调用的接口完整路径
    public SkuInfo getSkuInfoById(@PathVariable("skuId") Long skuId);

    @GetMapping("api/product/getSkuPrice/{skuId}")
    BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId);

    @GetMapping("api/product/getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id);

    @GetMapping("api/product/getSpuSaleAttrListCheckBySku/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("spuId")Long spuId, @PathVariable("skuId")Long skuId);

    @GetMapping("api/product/getSaleAttrValuesBySpu/{spuId}")
    List<Map<String,Object>> getSaleAttrValuesBySpu(@PathVariable("spuId")Long spuId);
}
