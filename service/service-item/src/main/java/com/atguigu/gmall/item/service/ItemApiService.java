package com.atguigu.gmall.item.service;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;

import java.math.BigDecimal;
import java.util.Map;

public interface ItemApiService {
    Map<String, Object> getItem(Long skuId);
}
