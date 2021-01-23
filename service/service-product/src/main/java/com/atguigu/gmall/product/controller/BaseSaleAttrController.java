package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseSaleAttrService;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.atguigu.gmall.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class BaseSaleAttrController {

    @Autowired
    private BaseSaleAttrService baseSaleAttrService;

    /**
     * 获取平台销售属性
     * @return
     */
    @GetMapping("baseSaleAttrList")
    public Result<List<BaseSaleAttr>> getBaseSaleAttrList(){
        List<BaseSaleAttr> BaseSaleAttrList = baseSaleAttrService.getBaseSaleAttrList();
        return Result.ok(BaseSaleAttrList);
    }

}
