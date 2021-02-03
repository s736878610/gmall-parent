package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.result.Result;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("list/test")
@RestController
public class ListTestController {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @RequestMapping("search")
    public Result test1() throws IOException {

        // 请求
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("goods");
        searchRequest.types("info");


        // DSL语句封装
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("tmId", 1);// 过滤
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title", "黑色");// 匹配
        boolQueryBuilder.filter(termQueryBuilder);
        boolQueryBuilder.must(matchQueryBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);// 第0页
        searchSourceBuilder.size(60);// 每页60条数据
        searchRequest.source(searchSourceBuilder);
        String dsl = searchSourceBuilder.toString();
        System.out.println(dsl);


        // 返回语句
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        long totalHits = searchResponse.getHits().getTotalHits();
        System.out.println("总条数" + totalHits);

        return Result.ok();
    }
}
