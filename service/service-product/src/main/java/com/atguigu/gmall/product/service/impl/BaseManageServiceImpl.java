package com.atguigu.gmall.product.service.impl;


import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.BaseManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseManageServiceImpl implements BaseManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    /**
     * 查询所有的一级分类信息
     *
     * @return
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        List<BaseCategory1> baseCategory1List = baseCategory1Mapper.selectList(null);
        return baseCategory1List;
    }

    /**
     * 根据一级分类Id 查询二级分类数据
     *
     * @param category1Id
     * @return
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper<BaseCategory2> wrapper = new QueryWrapper<>();
        wrapper.eq("category1_id", category1Id);
        List<BaseCategory2> baseCategory2List = baseCategory2Mapper.selectList(wrapper);
        return baseCategory2List;
    }

    /**
     * 根据二级分类Id 查询三级分类数据
     *
     * @param category2Id
     * @return
     */
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> wrapper = new QueryWrapper<>();
        wrapper.eq("category2_id", category2Id);
        List<BaseCategory3> baseCategory3List = baseCategory3Mapper.selectList(wrapper);
        return baseCategory3List;
    }

    /**
     * 根据分类Id 获取平台属性数据
     *
     * @param category3Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category3Id) {
        QueryWrapper<BaseAttrInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category_level", 3);
        wrapper.eq("category_id", category3Id);
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectList(wrapper);
        return baseAttrInfoList;
    }

    /**
     * 添加或修改平台属性和属性值
     *
     * @param baseAttrInfo
     */
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //平台属性
        if (baseAttrInfo.getId() != null) {
            //修改
            baseAttrInfoMapper.updateById(baseAttrInfo);
            //修改平台属性值   先删除 再新增
            QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
            //base_attr_value表的attr_id字段=base_attr_info表的id
            wrapper.eq("attr_id", baseAttrInfo.getId());
            baseAttrValueMapper.delete(wrapper);
        } else {
            //添加
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        //新增平台属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (attrValueList != null && attrValueList.size() > 0) {
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }

    }

    /**
     * 根据平台属性ID获取平台属性
     *
     * @param attrId
     * @return
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id", attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(wrapper);
        return baseAttrValueList;
    }


}
