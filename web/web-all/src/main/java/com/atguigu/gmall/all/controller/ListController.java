package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseAttrVo;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页展示控制器
 */
@Controller
public class ListController {

    @Autowired
    ListFeignClient listFeignClient;

    /**
     * 首页分类列表查询
     *
     * @param model
     * @return
     */
    @RequestMapping("index")
    public String index(Model model) {
        // 远程调用
        List<JSONObject> jsonObjects = listFeignClient.getBaseCategoryList();
        model.addAttribute("list", jsonObjects);
        return "index/index";
    }

    /**
     * 搜索结果列表
     *
     * @param model
     * @return
     */
    @RequestMapping({"list.html", "search.html"})
    public String list(Model model, SearchParam searchParam) {
        SearchResponseVo searchResponseVo = listFeignClient.list(searchParam);
        model.addAttribute("goodsList", searchResponseVo.getGoodsList());
        model.addAttribute("trademarkList", searchResponseVo.getTrademarkList());
        model.addAttribute("attrsList", searchResponseVo.getAttrsList());
        model.addAttribute("urlParam", getUrlParam(searchParam));

        if (StringUtils.isNotEmpty(searchParam.getTrademark())) {
            // 品牌面包屑
            String[] split = searchParam.getTrademark().split(":");
            model.addAttribute("trademarkParam", split[1]);
        }

        if (searchParam.getProps() != null && searchParam.getProps().length > 0) {
            // 属性面包屑
            List<SearchAttr> searchAttrList = new ArrayList<>();
            for (String prop : searchParam.getProps()) {
                String[] split = prop.split(":");
                Long attrId = Long.parseLong(split[0]);
                String attrValue = split[1];
                String attrName = split[2];
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(attrId);
                searchAttr.setAttrValue(attrValue);
                searchAttr.setAttrName(attrName);
                searchAttrList.add(searchAttr);
            }
            model.addAttribute("propsParamList", searchAttrList);
        }
        return "list/index";
    }


    private String getUrlParam(SearchParam searchParam) {
        // http://list.gmall.com:8300/list.html?category3Id=61&trademark=2:华为&order=
        String urlParam = "list.html?";
        // 解析数据
        Long category3Id = searchParam.getCategory3Id();
        String keyword = searchParam.getKeyword();
        String[] props = searchParam.getProps();
        String trademark = searchParam.getTrademark();
        String order = searchParam.getOrder();

        // 三级分类
        if (category3Id != null) {
            urlParam = urlParam + "category3Id=" + category3Id;
        }

        // 关键字
        if (StringUtils.isNotEmpty(keyword)) {
            urlParam = urlParam + "keyword=" + keyword;
        }

        // 属性值  属性值格式 attrId:attrValue:attrName
        if (props != null && props.length > 0) {
            for (String prop : props) {
                // &props=3:8GB:运行内存&props=4:128GB:机身存储
                urlParam = urlParam + "&props=" + prop;
            }
        }

        // 商标  商标格式 tmId:tmName
        if (StringUtils.isNotEmpty(trademark)) {
            String[] split = trademark.split(":");
            String tmId = split[0];
            urlParam = urlParam + "&trademark=" + trademark;
        }

        return urlParam;
    }

}
