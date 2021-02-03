package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 *  首页展示控制器
 */
@Controller
public class ListController {

    @Autowired
    ListFeignClient listFeignClient;

    /**
     * 首页分类列表查询
     * @param model
     * @return
     */
    @RequestMapping("index")
    public String index(Model model){
        // 远程调用
        List<JSONObject> jsonObjects = listFeignClient.getBaseCategoryList();
        model.addAttribute("list",jsonObjects);
        return "index/index";
    }

    /**
     * 搜索结果列表
     * @param model
     * @return
     */
    @RequestMapping("list.html")
    public String list(Model model, SearchParam searchParam){
        SearchResponseVo searchResponseVo = listFeignClient.list(searchParam);
        model.addAttribute("goodsList",searchResponseVo.getGoodsList());
        return "list/index";
    }

}
