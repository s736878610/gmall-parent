package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.xml.crypto.KeySelector;

@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class SkuInfoController {

    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * Sku分页
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("list/{page}/{limit}")
    public Result<IPage<SkuInfo>> getSkuInfoPage(@PathVariable("page")Long page,
                                                 @PathVariable("limit")Long limit){
        IPage<SkuInfo> iPage= skuInfoService.getSkuInfoPage(page,limit);
        return Result.ok(iPage);
    }

    /**
     * 添加sku
     * @param skuInfo
     * @return
     */
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        skuInfoService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    /**
     * sku上架
     * @param skuId
     * @return
     */
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId")Long skuId){
        skuInfoService.onSale(skuId);
        return Result.ok();
    }

    /**
     * sku下架
     * @param skuId
     * @return
     */
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId")Long skuId){
        skuInfoService.cancelSale(skuId);
        return Result.ok();
    }

}
