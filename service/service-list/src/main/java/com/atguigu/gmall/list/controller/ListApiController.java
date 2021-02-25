package com.atguigu.gmall.list.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.ListApiService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 首页功能控制器
 */
@RestController
@RequestMapping("api/list")
@CrossOrigin
public class ListApiController {

    @Autowired
    ListApiService listApiService;
    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    GoodsRepository goodsRepository;


    /**
     * 首页分类列表查询
     * @return
     */
    @GetMapping("getBaseCategoryList")
    List<JSONObject> getBaseCategoryList(){
        List<JSONObject> jsonObjectList = listApiService.getBaseCategoryList();
        return jsonObjectList;
    }

    /**
     * ES创建索引，映射
     * @return
     */
    @RequestMapping("createIndex")
    public Result createIndex(){
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    /**
     * sku上架
     * @param skuId
     */
    @RequestMapping("onSale/{skuId}")
    void onSale(@PathVariable("skuId") Long skuId){
        listApiService.onSale(skuId);
    }

    /**
     * sku下架
     * @param skuId
     */
    @RequestMapping("cancelSale/{skuId}")
    void cancelSale(@PathVariable("skuId") Long skuId){
        listApiService.cancelSale(skuId);
    }

    /**
     * 更新商品热点值
     * @param skuId
     */
    @RequestMapping("hotScore/{skuId}")
    void hotScore(@PathVariable("skuId") Long skuId){
        listApiService.hotScore(skuId);
    }

    /**
     * 搜索功能
     * @param searchParam
     * @return
     */
    @RequestMapping("list")
    SearchResponseVo list(@RequestBody SearchParam searchParam){
        return listApiService.list(searchParam);
    }
}
