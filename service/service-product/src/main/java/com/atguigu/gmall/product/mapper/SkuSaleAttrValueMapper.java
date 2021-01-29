package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("spuId")Long spuId, @Param("skuId")Long skuId);

    List<Map<String,Object>> selectSaleAttrValuesBySpu(Long spuId);
}
