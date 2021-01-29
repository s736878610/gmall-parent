package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseCategoryView;
import org.springframework.stereotype.Service;


public interface BaseCategoryViewService {
    BaseCategoryView getCategoryView(Long category3Id);
}
