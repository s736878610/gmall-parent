package com.atguigu.gmall.list.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.util.List;

public interface ListApiService {
    List<JSONObject> getBaseCategoryList();

    void hotScore(Long skuId);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    SearchResponseVo list(SearchParam searchParam);
}
