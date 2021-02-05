package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.ListApiService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.InternalOrder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
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
import java.util.stream.Collectors;

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
     * 搜索功能实现
     *
     * @param searchParam
     * @return
     */
    @Override
    public SearchResponseVo list(SearchParam searchParam) {
        // 封装语句
        SearchSourceBuilder searchSourceBuilder = getSearchQueryDSL(searchParam);

        // 执行查询
        SearchRequest searchRequest = new SearchRequest();// 请求
        searchRequest.indices("goods");
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);// DSL语句传入请求

        SearchResponseVo searchResponseVo = new SearchResponseVo();
        try {
            // ElasticSearch返回结果
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
        SearchResponseVo searchResponseVo = new SearchResponseVo();// 返回给前端的视图对象
        List<Goods> goodsList = new ArrayList<>();

        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitsResponse = hits.getHits();
        if (hitsResponse != null && hitsResponse.length > 0) {
            // 商品信息解析
            for (SearchHit documentFields : hitsResponse) {
                String sourceAsString = documentFields.getSourceAsString();
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
                goodsList.add(goods);
            }
        }


        Aggregations aggregations = searchResponse.getAggregations();
        if (aggregations != null) {
            // 品牌聚合信息解析
            ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregations.get("tmIdAgg");// 第一层聚合
            if (tmIdAgg != null) {
                List<? extends Terms.Bucket> tmIdAggBuckets = tmIdAgg.getBuckets();

                // 普通for循环方式解析
//        List<SearchResponseTmVo> searchResponseTmVos = new ArrayList<>();
//        for (Terms.Bucket tmIdAggBucket : tmIdAggBuckets) {
//            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
//            // tmIdAggBucket取元素  封装进searchResponseTmVo ......
//            searchResponseTmVos.add(searchResponseTmVo);
//        }

                // 流式循环方式解析  map(buckets->{将SearchResponseTmVo封装好返回})  返回结果自动封装进List<SearchResponseTmVo> 中
                List<SearchResponseTmVo> searchResponseTmVoList = tmIdAggBuckets.stream().map(buckets -> {
                    SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                    Long keyTmId = (Long) buckets.getKey();// 品牌Id
                    searchResponseTmVo.setTmId(keyTmId);

                    ParsedStringTerms tmNameAgg = (ParsedStringTerms) ((Terms.Bucket) buckets).getAggregations().get("tmNameAgg");// 第二层聚合
                    List<? extends Terms.Bucket> tmNameAggBuckets = tmNameAgg.getBuckets();
                    String keyTmName = (String) tmNameAggBuckets.get(0).getKey();// 品牌名称
                    searchResponseTmVo.setTmName(keyTmName);

                    ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) ((Terms.Bucket) buckets).getAggregations().get("tmLogoUrlAgg");// 第二层聚合
                    String keyTmLogoUrl = (String) tmLogoUrlAgg.getBuckets().get(0).getKey();// 品牌logUrl
                    searchResponseTmVo.setTmLogoUrl(keyTmLogoUrl);

                    return searchResponseTmVo;
                }).collect(Collectors.toList());

                searchResponseVo.setTrademarkList(searchResponseTmVoList);// 解析好的品牌信息List封装进VO中
            }


            // 平台属性聚合解析
            ParsedNested attrsAgg = (ParsedNested) aggregations.get("attrsAgg");// 第一层聚合
            if (attrsAgg != null) {
                ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrsAgg.getAggregations().get("attrIdAgg");// 第二层
                List<SearchResponseAttrVo> searchResponseAttrVoList = attrIdAgg.getBuckets().stream().map(attrIdBuckets -> {
                    SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                    Long keyAttrId = (Long) attrIdBuckets.getKey();// 属性id

                    ParsedStringTerms attrNameAgg = attrIdBuckets.getAggregations().get("attrNameAgg");// 第三层
                    String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();// 属性名

                    ParsedStringTerms attrValueAgg = attrIdBuckets.getAggregations().get("attrValueAgg");// 第三层
                    List<String> attrValueList = attrValueAgg.getBuckets().stream().map(attrValueBuckets -> {// 属性值名称List
                        String attrValue = attrValueBuckets.getKeyAsString();
                        return attrValue;
                    }).collect(Collectors.toList());

                    searchResponseAttrVo.setAttrId(keyAttrId);
                    searchResponseAttrVo.setAttrName(attrName);
                    searchResponseAttrVo.setAttrValueList(attrValueList);

                    return searchResponseAttrVo;
                }).collect(Collectors.toList());

                searchResponseVo.setAttrsList(searchResponseAttrVoList);// 解析好的平台属性信息List封装进VO中
            }
        }

        searchResponseVo.setGoodsList(goodsList);// GoodsList封装进VO

        return searchResponseVo;
    }

    /**
     * 解析前端参数  封装DSL语句
     *
     * @param searchParam
     * @return
     */
    private SearchSourceBuilder getSearchQueryDSL(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 解析数据
        Long category3Id = searchParam.getCategory3Id();
        String keyword = searchParam.getKeyword();
        String[] props = searchParam.getProps();
        String trademark = searchParam.getTrademark();
        String order = searchParam.getOrder();

        // 查询

        // 三级分类
        if (category3Id != null) {
            // 查询条件
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("category3Id", category3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        // 关键字
        if (StringUtils.isNotEmpty(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        // 属性值  属性值格式 attrId:attrValue:attrName
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                Long attrId = Long.parseLong(split[0]);
                String attrValue = split[1];
                String attrName = split[2];
                MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder("attrs.attrId", attrId);
                MatchQueryBuilder matchQueryBuilder2 = new MatchQueryBuilder("attrs.attrValue", attrValue);
                MatchQueryBuilder matchQueryBuilder3 = new MatchQueryBuilder("attrs.attrName", attrName);

                // nested内部bool
                BoolQueryBuilder boolQueryBuilderInner = new BoolQueryBuilder();
                boolQueryBuilderInner.must(matchQueryBuilder1);
                boolQueryBuilderInner.must(matchQueryBuilder2);
                boolQueryBuilderInner.must(matchQueryBuilder3);

                // nested包裹内层bool
                NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrs", boolQueryBuilderInner, ScoreMode.None);

                boolQueryBuilder.must(nestedQueryBuilder);// 外层bool
            }
        }

        // 商标  商标格式 tmId:tmName
        if (StringUtils.isNotEmpty(trademark)) {
            String[] split = trademark.split(":");
            String tmId = split[0];
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("tmId", tmId);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        // 排序
        if (order != null) {

        }

        searchSourceBuilder.query(boolQueryBuilder);// 添加查询语句  外层bool包裹着所有查询语句

        // 品牌(商标)聚合查询
        TermsAggregationBuilder tmAgg = AggregationBuilders.terms("tmIdAgg").field("tmId");
        tmAgg.subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"));// tmNameAgg是tmIdAgg的子聚合
        tmAgg.subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));// tmLogoUrlAgg也是tmIdAgg的子聚合
        searchSourceBuilder.aggregation(tmAgg);// 添加聚合语句

        // 平台属性聚合查询  (比品牌聚合多一层Nested)
        NestedAggregationBuilder attrsAgg = AggregationBuilders.nested("attrsAgg", "attrs");// nested层
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"));
        attrsAgg.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(attrsAgg);// 添加聚合语句


        System.out.println(searchSourceBuilder.toString());// 打印dsl语句
        return searchSourceBuilder;
    }

}
