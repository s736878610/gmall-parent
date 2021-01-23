package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface SpuInfoService {
    void saveSpuInfo(SpuInfo spuInfo);

    IPage<SpuInfo> getSpuInfoPage(Long page, Long limit, Long category3Id);
}
