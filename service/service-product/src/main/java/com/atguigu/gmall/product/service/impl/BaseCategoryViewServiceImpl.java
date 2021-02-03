package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.BaseCategoryViewService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BaseCategoryViewServiceImpl implements BaseCategoryViewService {

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    /**
     * 根据三级分类id获取分类视图信息
     * @param category3Id
     * @return
     */
    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        QueryWrapper<BaseCategoryView> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id",category3Id);
        BaseCategoryView baseCategoryView = baseCategoryViewMapper.selectOne(queryWrapper);
        return baseCategoryView;
    }

    /**
     * 首页分类列表查询
     * @return
     */
    @Override
    public List<JSONObject> getBaseCategoryList() {
        // 数据库查出来的数据没有结构  需要手动封装
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);

        // Json中应该有三个字段分别是：
        // categoryId
        // categoryName
        // categoryChild

        // 第一层一级分类
        List<JSONObject> category1List = new ArrayList<>();
        Map<Long, List<BaseCategoryView>> c1Map = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        for (Map.Entry<Long, List<BaseCategoryView>> c1Entry : c1Map.entrySet()) {
            JSONObject jsonObjectC1 = new JSONObject();
            Long c1Id = c1Entry.getKey();
            String c1Name = c1Entry.getValue().get(0).getCategory1Name();
            jsonObjectC1.put("categoryId",c1Id);
            jsonObjectC1.put("categoryName",c1Name);

            // 第二层二级分类
            List<JSONObject> category2List = new ArrayList<>();
            Map<Long, List<BaseCategoryView>> c2Map = c1Entry.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> c2Entry : c2Map.entrySet()) {
                JSONObject jsonObjectC2 = new JSONObject();
                Long c2Id = c2Entry.getKey();
                String c2Name = c2Entry.getValue().get(0).getCategory2Name();
                jsonObjectC2.put("categoryId",c2Id);
                jsonObjectC2.put("categoryName",c2Name);

                // 第三层三级分类
                List<JSONObject> category3List = new ArrayList<>();
                Map<Long, List<BaseCategoryView>> c3Map = c2Entry.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                for (Map.Entry<Long, List<BaseCategoryView>> c3Entry : c3Map.entrySet()) {
                    JSONObject jsonObjectC3 = new JSONObject();
                    Long c3Id = c3Entry.getKey();
                    String c3Name = c3Entry.getValue().get(0).getCategory3Name();
                    jsonObjectC3.put("categoryId",c3Id);
                    jsonObjectC3.put("categoryName",c3Name);
                    //jsonObjectC3.put("categoryChild",null);
                    category3List.add(jsonObjectC3);
                }
                jsonObjectC2.put("categoryChild",category3List);
                category2List.add(jsonObjectC2);
            }
            jsonObjectC1.put("categoryChild",category2List);
            category1List.add(jsonObjectC1);
        }

        // 将数据静态化成js文本
        File file = new File("I:\\category1List.js");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(JSONObject.toJSONString(category1List).getBytes());
            //fos.write(JSON.toJSONString(category1List).getBytes());// 效果同上
        } catch (Exception e) {
            e.printStackTrace();
        }

        return category1List;
    }
}
