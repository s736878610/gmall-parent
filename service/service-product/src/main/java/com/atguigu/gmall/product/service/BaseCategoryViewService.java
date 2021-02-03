package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategoryView;
import org.springframework.stereotype.Service;

import java.util.List;


public interface BaseCategoryViewService {
    BaseCategoryView getCategoryView(Long category3Id);

    List<JSONObject> getBaseCategoryList();
}
