package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface SpuInfoService {
    void saveSpuInfo(SpuInfo spuInfo);

    IPage<SpuInfo> getSpuInfoPage(Long page, Long limit, Long category3Id);

    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    List<SpuImage> getSpuImageList(Long spuId);
}
