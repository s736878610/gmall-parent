package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
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
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ListApiServiceImpl implements ListApiService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 首页分类列表查询(远程调用)
     *
     * @return
     */
    @Override
    public List<JSONObject> getBaseCategoryList() {
        return productFeignClient.getBaseCategoryList();
    }

    /**
     * sku上架
     *
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {
        Goods goods = new Goods();
        // 远程调用  获取封装Goods对象需要的数据

        // skuInfo  skuId=Goods.id  skuName=Goods.title  Goods.defaultImg  Goods.price
        SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);

        // 品牌信息
        BaseTrademark baseTrademark = productFeignClient.getBaseTrademarkById(skuInfo.getTmId());

        // 平台属性集合
        List<SearchAttr> searchAttrList = productFeignClient.getSearchAttrBySkuId(skuId);

        // 一二三级分类
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());

        // 封装Goods对象
        goods.setId(skuInfo.getId());
        goods.setTitle(skuInfo.getSkuName());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setPrice(skuInfo.getPrice().doubleValue());

        goods.setTmId(baseTrademark.getId());
        goods.setTmName(baseTrademark.getTmName());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());

        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory3Name(categoryView.getCategory3Name());

        goods.setCreateTime(new Date());
        goods.setHotScore(0L);
        goods.setAttrs(searchAttrList);

        // ElasticSearch添加Goods数据
        goodsRepository.save(goods);
    }

    /**
     * sku下架
     *
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        // 删除ElasticSearch中的数据
        goodsRepository.deleteById(skuId);

    }

    /**
     * 更新商品热点值
     *
     * @param skuId
     */
    @Override
    public void hotScore(Long skuId) {
        // redis中热点值+1
        Long increment = redisTemplate.opsForValue().increment("sku:" + skuId + ":hotScore", 1);
        if (increment % 10 == 0) {
            // if (increment % 1000 == 0)  该商品被访问了1000次  将热点值同步到ElasticSearch中
            Optional<Goods> optionalGoods = goodsRepository.findById(skuId);
            Goods goods = optionalGoods.get();
            goods.setHotScore(increment);
            goodsRepository.save(goods);
        }
    }

    /**
     * 搜索功能
     *
     * @param searchParam
     * @return
     */
    @Override
    public SearchResponseVo list(SearchParam searchParam) {
        // 封装语句
        SearchSourceBuilder searchSourceBuilder = getSearchQueryDSL(searchParam);

        // 执行查询

        // 返回结果
        SearchRequest searchRequest = new SearchRequest();// 请求
        searchRequest.indices("goods");
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);// DSL语句封装

        SearchResponseVo searchResponseVo = new SearchResponseVo();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 解析结果
            searchResponseVo = parseResponseVO(searchResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return searchResponseVo;
    }

    /**
     * 解析ElasticSearch响应结果  封装SearchResponseVo对象
     *
     * @param searchResponse
     * @return
     */
    private SearchResponseVo parseResponseVO(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        List<Goods> goodsList = new ArrayList<>();

        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitsResponse = hits.getHits();
        if (hitsResponse != null && hitsResponse.length > 0) {
            for (SearchHit documentFields : hitsResponse) {
                String sourceAsString = documentFields.getSourceAsString();
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
                goodsList.add(goods);
            }
        }

        searchResponseVo.setGoodsList(goodsList);
        return searchResponseVo;
    }

    /**
     * 解析前端参数  封装DSL语句
     * @param searchParam
     * @return
     */
    private SearchSourceBuilder getSearchQueryDSL(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 解析数据
        Long category3Id = searchParam.getCategory3Id();
        String keyword = searchParam.getKeyword();
        String order = searchParam.getOrder();
        String trademark = searchParam.getTrademark();
        String[] props = searchParam.getProps();

        if (category3Id != null) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("category3Id", category3Id);
            boolQueryBuilder.filter(termQueryBuilder);
            searchSourceBuilder.query(boolQueryBuilder);
        }

        return searchSourceBuilder;
    }

}
