package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemApiService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemApiServiceImpl implements ItemApiService {

    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 根据skuId查询商品详情
     *      sku信息，图片信息，销售属性列表，分类信息，价格信息
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getItem(Long skuId) {
        Map<String, Object> result = new HashMap<>();
        // 获取sku基本信息与图片信息
        SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
        result.put("skuInfo",skuInfo);

        // 获取分类信息
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        result.put("categoryView",baseCategoryView);

        // 获取价格信息
        BigDecimal price = productFeignClient.getSkuPrice(skuId);
        result.put("price",price);

        // 获取spu销售属性，spu销售属性值，对应的sku销售属性
        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getSpuId(),skuId);
        result.put("spuSaleAttrList",spuSaleAttrList);
        // 销售属性切换的hash表
        Map<String,Object> valuesSkuJson = new HashMap<>();
        List<Map<String,Object>> mapList = productFeignClient.getSaleAttrValuesBySpu(skuInfo.getSpuId());
        for (Map<String,Object> map : mapList) {
            valuesSkuJson.put((String) map.get("value_ids"),map.get("sku_id"));
        }
        result.put("valuesSkuJson", JSON.toJSONString(valuesSkuJson));

        return result;
    }

}
