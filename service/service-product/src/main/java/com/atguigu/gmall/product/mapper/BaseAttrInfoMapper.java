package com.atguigu.gmall.product.mapper;


import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 属性表 Mapper 接口
 * </p>
 *
 * @author zhongyuan
 * @since 2021-01-21
 */
@Mapper
@Repository
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {

}
