package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
