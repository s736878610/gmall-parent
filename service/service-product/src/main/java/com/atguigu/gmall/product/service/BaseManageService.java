package com.atguigu.gmall.product.service;


import com.atguigu.gmall.model.product.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BaseManageService {

    List<BaseCategory1> getCategory1();

    List<BaseCategory2> getCategory2(Long category1Id);

    List<BaseCategory3> getCategory3(Long category2Id);

    List<BaseAttrInfo> getAttrInfoList(Long category3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(Long attrId);
}
