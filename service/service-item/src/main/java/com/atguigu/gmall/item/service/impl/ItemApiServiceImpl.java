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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
public class ItemApiServiceImpl implements ItemApiService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 根据skuId查询商品详情
     * sku信息，图片信息，销售属性列表，分类信息，价格信息
     *
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getItem(Long skuId) {
        long currentTimeMillisStart = System.currentTimeMillis();
        Map<String, Object> map = getItemSingle(skuId);
        long currentTimeMillisEnd = System.currentTimeMillis();
        System.out.println("执行时间：" + (currentTimeMillisEnd - currentTimeMillisStart));

        return map;
    }

    /**
     * 多线程执行
     *
     * @param skuId
     * @return
     */
    private Map<String, Object> getItemThread(Long skuId) {
        Map<String, Object> result = new HashMap<>();
        // 获取sku基本信息与图片信息
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
                result.put("skuInfo", skuInfo);// 封装参数
                return skuInfo;
            }
        }, threadPoolExecutor);


        // 获取分类信息
        CompletableFuture categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
                result.put("categoryView", baseCategoryView);// 封装参数
            }
        }, threadPoolExecutor);


        // 获取价格信息
        CompletableFuture priceCompletableFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                BigDecimal price = productFeignClient.getSkuPrice(skuId);
                result.put("price", price);// 封装参数
            }
        }, threadPoolExecutor);


        // 获取spu销售属性，spu销售属性值，对应的sku销售属性
        CompletableFuture spuSaleAttrListCheckCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getSpuId(), skuId);
                result.put("spuSaleAttrList", spuSaleAttrList);// 封装参数
            }
        }, threadPoolExecutor);
        // 销售属性切换的hash表
        CompletableFuture saleAttrValuesCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                Map<String, Object> valuesSkuJson = new HashMap<>();
                List<Map<String, Object>> mapList = productFeignClient.getSaleAttrValuesBySpu(skuInfo.getSpuId());
                for (Map<String, Object> map : mapList) {
                    valuesSkuJson.put((String) map.get("value_ids"), map.get("sku_id"));
                }
                result.put("valuesSkuJson", JSON.toJSONString(valuesSkuJson));
            }
        }, threadPoolExecutor);

        // 阻塞main线程  编排线程执行顺序
        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                categoryViewCompletableFuture,
                saleAttrValuesCompletableFuture,
                priceCompletableFuture,
                spuSaleAttrListCheckCompletableFuture).join();

        return result;
    }

    /**
     * 单线程执行
     *
     * @param skuId
     * @return
     */
    private Map<String, Object> getItemSingle(Long skuId) {
        Map<String, Object> result = new HashMap<>();
        // 获取sku基本信息与图片信息
        SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
        result.put("skuInfo", skuInfo);


        // 获取分类信息
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        result.put("categoryView", baseCategoryView);


        // 获取价格信息
        BigDecimal price = productFeignClient.getSkuPrice(skuId);
        result.put("price", price);


        // 获取spu销售属性，spu销售属性值，对应的sku销售属性
        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getSpuId(), skuId);
        result.put("spuSaleAttrList", spuSaleAttrList);
        // 销售属性切换的hash表
        Map<String, Object> valuesSkuJson = new HashMap<>();
        List<Map<String, Object>> mapList = productFeignClient.getSaleAttrValuesBySpu(skuInfo.getSpuId());
        for (Map<String, Object> map : mapList) {
            valuesSkuJson.put((String) map.get("value_ids"), map.get("sku_id"));
        }
        result.put("valuesSkuJson", JSON.toJSONString(valuesSkuJson));

        return result;
    }

}
