package com.atguigu.gmall.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 属性值表
 * </p>
 *
 * @author zhongyuan
 * @since 2021-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("base_attr_value")
@ApiModel(value="BaseAttrValue对象", description="属性值表")
public class BaseAttrValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "编号")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "属性值名称")
    private String valueName;

    @ApiModelProperty(value = "属性id")
    private Long attrId;


}
