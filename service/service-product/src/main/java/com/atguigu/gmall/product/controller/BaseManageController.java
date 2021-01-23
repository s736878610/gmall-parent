package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.BaseManageService;
import com.atguigu.gmall.result.Result;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品基础属性接口")
@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class BaseManageController {

    @Autowired
    private BaseManageService baseManageService;

    /**
     * 查询所有的一级分类信息
     * @return
     */
    @GetMapping("getCategory1")
    public Result<List<BaseCategory1>> getCategory1() {
        List<BaseCategory1> baseCategory1List = baseManageService.getCategory1();
        return Result.ok(baseCategory1List);
    }

    /**
     * 根据一级分类Id 查询二级分类数据
     * @param category1Id
     * @return
     */
    @GetMapping("getCategory2/{category1Id}")
    public Result<List<BaseCategory2>> getCategory2(@PathVariable("category1Id") Long category1Id){
        List<BaseCategory2> baseCategory2List = baseManageService.getCategory2(category1Id);
        return Result.ok(baseCategory2List);
    }

    /**
     * 根据二级分类Id 查询三级分类数据
     * @param category2Id
     * @return
     */
    @GetMapping("getCategory3/{category2Id}")
    public Result<List<BaseCategory3>> getCategory3(@PathVariable("category2Id")Long category2Id){
        List<BaseCategory3> baseCategory3List = baseManageService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }

    /**
     * 根据分类Id 获取平台属性数据
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result<List<BaseAttrInfo>> attrInfoList(@PathVariable("category1Id")Long category1Id,
                                                   @PathVariable("category2Id")Long category2Id,
                                                   @PathVariable("category3Id")Long category3Id){
        List<BaseAttrInfo> baseAttrInfoList = baseManageService.getAttrInfoList(category3Id);
        return Result.ok(baseAttrInfoList);
    }

    /**
     * 添加或修改平台属性和属性值
     * @param baseAttrInfo
     * @return
     */
    @RequestMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        baseManageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 根据平台属性ID获取平台属性
     * @param attrId
     * @return
     */
    @GetMapping("getAttrValueList/{attrId}")
    public Result<List<BaseAttrValue>> getAttrValueList(@PathVariable("attrId")Long attrId){
        List<BaseAttrValue> baseAttrValueList = baseManageService.getAttrValueList(attrId);
        return Result.ok(baseAttrValueList);
    }

}
