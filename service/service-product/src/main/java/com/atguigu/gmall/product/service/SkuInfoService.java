package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface SkuInfoService {
    IPage<SkuInfo> getSkuInfoPage(Long page, Long limit);
}
