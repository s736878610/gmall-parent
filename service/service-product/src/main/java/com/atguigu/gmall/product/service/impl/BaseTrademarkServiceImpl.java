package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseTrademarkServiceImpl implements BaseTrademarkService {

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * 获取品牌分页列表
     * @param page 当前页数
     * @param limit 每页数据条数
     * @return
     */
    @Override
    public IPage<BaseTrademark> getBaseTrademarkPage(Long page, Long limit) {
        Page<BaseTrademark> iPage = new Page<>();
        iPage.setSize(limit);
        iPage.setCurrent(page);
        IPage<BaseTrademark> BaseTrademarkPage = baseTrademarkMapper.selectPage(iPage, null);
        return BaseTrademarkPage;
    }

    /**
     * 获取所有品牌属性
     * @return
     */
    @Override
    public List<BaseTrademark> getTrademarkList() {
        return baseTrademarkMapper.selectList(null);
    }

    /**
     * 根据id获取品牌信息
     * @param tmId
     * @return
     */
    @Override
    public BaseTrademark getBaseTrademarkById(Long tmId) {
        BaseTrademark baseTrademark = baseTrademarkMapper.selectById(tmId);
        return baseTrademark;
    }
}
