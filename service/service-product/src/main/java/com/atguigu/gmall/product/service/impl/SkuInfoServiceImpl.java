package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    /**
     * Sku分页
     * @param page
     * @param limit
     * @return
     */
    @Override
    public IPage<SkuInfo> getSkuInfoPage(Long page, Long limit) {
        Page<SkuInfo> iPage = new Page<>();
        iPage.setSize(limit);
        iPage.setCurrent(page);
        IPage<SkuInfo> SkuInfoPage = skuInfoMapper.selectPage(iPage, null);
        return SkuInfoPage;
    }
}
