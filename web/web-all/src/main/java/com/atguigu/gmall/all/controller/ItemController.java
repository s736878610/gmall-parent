package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.item.client.ItemFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    /**
     * 根据skuId查询商品详情
     * @param skuId
     * @param model
     * @param request
     * @param response
     * @return
     *
     */
    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable("skuId") Long skuId, Model model, HttpServletRequest request, HttpServletResponse response) {
        // 调用后端服务，查询sku数据
        // sku基本信息，图片信息，销售属性列表，分类信息，价格信息
        Map<String, Object> map = itemFeignClient.getItem(skuId);
        model.addAllAttributes(map);

        // 将数据静态化成js文本
//        File file = new File("I:\\category1List.js");
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//
//            //fos.write(JSON.toJSONString(category1List).getBytes());// 效果同上
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return "item/index.html";
    }



    @RequestMapping("test")
    public String test(Model model, HttpServletRequest request, HttpServletResponse response){
        model.addAttribute("key","hello thymeleaf");

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("元素"+i);
        }
        model.addAttribute("list",list);
        return "test.html";
    }

}
