package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.service.BaseCategoryViewService;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.atguigu.gmall.product.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("api/product")
public class ProductApiController {

    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private BaseCategoryViewService baseCategoryViewService;
    @Autowired
    private BaseTrademarkService baseTrademarkService;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;


    /**
     * 获取sku基本信息与图片信息
     * @param skuId
     * @return
     */
    @RequestMapping("getSkuInfoById/{skuId}")
    public SkuInfo getSkuInfoById(@PathVariable("skuId") Long skuId){
        SkuInfo skuInfo = skuInfoService.getSkuInfoById(skuId);
        return skuInfo;
    }

    /**
     * 根据skuId获取价格信息
     * @param skuId
     * @return
     */
    @GetMapping("getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId){
        return skuInfoService.getSkuPrice(skuId);
    }

    /**
     * 根据三级分类id获取分类信息
     * @param category3Id
     * @return
     */
    @GetMapping("getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id){
        BaseCategoryView categoryView = baseCategoryViewService.getCategoryView(category3Id);
        return categoryView;
    }

    /**
     * 获取spu销售属性，spu销售属性值，对应的sku销售属性
     * @param spuId
     * @param skuId
     * @return
     */
    @GetMapping("getSpuSaleAttrListCheckBySku/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("spuId")Long spuId, @PathVariable("skuId")Long skuId){
        return skuInfoService.getSpuSaleAttrListCheckBySku(spuId,skuId);
    }

    /**
     * 获取销售属性切换的hash表
     * @param spuId
     * @return
     */
    @GetMapping("getSaleAttrValuesBySpu/{spuId}")
    List<Map<String,Object>> getSaleAttrValuesBySpu(@PathVariable("spuId")Long spuId){
        return skuInfoService.getSaleAttrValuesBySpu(spuId);
    }

    /**
     * 首页分类列表查询
     * @return
     */
    @GetMapping("getBaseCategoryList")
    List<JSONObject> getBaseCategoryList(){
        return baseCategoryViewService.getBaseCategoryList();
    }

    /**
     * 根据id获取品牌信息
     * @param tmId
     * @return
     */
    @GetMapping("getBaseTrademarkById/{tmId}")
    BaseTrademark getBaseTrademarkById(@PathVariable("tmId") Long tmId){
        return baseTrademarkService.getBaseTrademarkById(tmId);
    }

    /**
     * 根据skuId获取平台属性
     * @param skuId
     * @return
     */
    @GetMapping("getSearchAttrBySkuId/{skuId}")
    List<SearchAttr> getSearchAttrBySkuId(@PathVariable("skuId") Long skuId){
        List<SearchAttr> searchAttrList = skuAttrValueMapper.selectSearchAttrBySkuId(skuId);
        return searchAttrList;
    }

}
