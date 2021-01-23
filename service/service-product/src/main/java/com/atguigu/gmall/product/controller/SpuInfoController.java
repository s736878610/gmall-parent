package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.BaseSaleAttrService;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.atguigu.gmall.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class SpuInfoController {

    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * 添加SpuInfo(SpuImage、SpuPoster、SpuSaleAttr、SpuSaleAttrValue)
     * @param spuInfo
     * @return
     */
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        spuInfoService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    /**
     * Spu 分页查询
     * @param page
     * @param limit
     * @param category3Id
     * @return
     */
    @GetMapping("{page}/{limit}")
    public Result<IPage<SpuInfo>> getSpuInfoPage(@PathVariable("page")Long page,
                                 @PathVariable("limit")Long limit,
                                 Long category3Id){
        IPage<SpuInfo> iPage= spuInfoService.getSpuInfoPage(page,limit,category3Id);
        return Result.ok(iPage);
    }

}
