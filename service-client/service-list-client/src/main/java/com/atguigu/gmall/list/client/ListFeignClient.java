package com.atguigu.gmall.list.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "service-list")// 要调用的服务名
public interface ListFeignClient {

    @GetMapping("api/list/getBaseCategoryList")// 要调用的接口路径完整路径
    List<JSONObject> getBaseCategoryList();

    @RequestMapping("api/list/onSale/{skuId}")
    void onSale(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/list/cancelSale/{skuId}")
    void cancelSale(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/list/hotScore/{skuId}")
    void hotScore(@PathVariable("skuId") Long skuId);

    @RequestMapping("api/list/list")
    SearchResponseVo list(@RequestBody SearchParam searchParam);
}
