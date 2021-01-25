package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
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

    /**
     * 根据spuId获取spu销售属性
     * @param spuId
     * @return
     */
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrList(@PathVariable("spuId")Long spuId){
        List<SpuSaleAttr> spuSaleAttrList = spuInfoService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }

    /**
     * 根据spuId获取spu图片列表
     * @param spuId
     * @return
     */
    @GetMapping("spuImageList/{spuId}")
    public Result<List<SpuImage>> getSpuImageList(@PathVariable("spuId")Long spuId){
        List<SpuImage> spuImageList = spuInfoService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

}
