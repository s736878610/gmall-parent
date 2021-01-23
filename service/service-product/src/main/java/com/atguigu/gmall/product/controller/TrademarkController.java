package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.atguigu.gmall.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("admin/product")
public class TrademarkController {

    @Autowired
    private BaseTrademarkService baseTrademarkService;

    /**
     * 获取品牌分页列表
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("baseTrademark/{page}/{limit}")
    public Result<IPage<BaseTrademark>> getBaseTrademarkPage(@PathVariable("page")Long page,
                                                            @PathVariable("limit")Long limit){
        IPage<BaseTrademark> iPage= baseTrademarkService.getBaseTrademarkPage(page,limit);
        return Result.ok(iPage);
    }

    /**
     * 获取所有品牌属性
     * @return
     */
    @GetMapping("baseTrademark/getTrademarkList")
    public Result<List<BaseTrademark>> getTrademarkList(){
        List<BaseTrademark> baseTrademarkList = baseTrademarkService.getTrademarkList();
        return Result.ok(baseTrademarkList);
    }
}
