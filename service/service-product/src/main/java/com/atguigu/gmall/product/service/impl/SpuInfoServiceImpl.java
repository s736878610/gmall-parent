package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.SpuImageMapper;
import com.atguigu.gmall.product.mapper.SpuInfoMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.mapper.SpuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpuInfoServiceImpl implements SpuInfoService {

    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    /**
     * 添加SpuInfo(SpuImage、SpuPoster、SpuSaleAttr、SpuSaleAttrValue)
     *
     * @param spuInfo
     */
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        // Spu主表
        spuInfoMapper.insert(spuInfo);
        Long spuInfoId = spuInfo.getId();

        // Spu图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfoId);
                spuImageMapper.insert(spuImage);
            }
        }

        // Spu销售属性(注意：外键是spu_id + base_sale_attr_id需要自己封装)
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfoId);
                spuSaleAttrMapper.insert(spuSaleAttr);
                // Spu销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfoId);
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }


    }

    /**
     * Spu 分页查询
     *
     * @param page        当前页
     * @param limit       每页条数
     * @param category3Id
     * @return
     */
    @Override
    public IPage<SpuInfo> getSpuInfoPage(Long page, Long limit, Long category3Id) {
        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id", category3Id);
        Page<SpuInfo> iPage = new Page<>();
        iPage.setSize(limit);
        iPage.setCurrent(page);
        IPage<SpuInfo> SpuInfoPage = spuInfoMapper.selectPage(iPage, wrapper);
        return SpuInfoPage;
    }

    /**
     * 根据spuId获取spu销售属性
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        //spu销售属性
        QueryWrapper<SpuSaleAttr> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectList(queryWrapper);

        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                //spu销售属性值
                QueryWrapper<SpuSaleAttrValue> attrValueQueryWrapper = new QueryWrapper<>();
                attrValueQueryWrapper.eq("spu_id", spuId);
                attrValueQueryWrapper.eq("base_sale_attr_id", spuSaleAttr.getBaseSaleAttrId());
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.selectList(attrValueQueryWrapper);
                spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValueList);
            }
        }
        return spuSaleAttrList;
    }

    /**
     * 根据spuId获取spu图片列表
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        List<SpuImage> spuImageList = spuImageMapper.selectList(queryWrapper);
        return spuImageList;
    }

}
