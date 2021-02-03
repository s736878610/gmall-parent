package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.aspect.GmallCache;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuImageMapper;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ListFeignClient listFeignClient;


    /**
     * Sku分页
     *
     * @param page  当前页
     * @param limit 每页条数
     * @return
     */
    @Override
    public IPage<SkuInfo> getSkuInfoPage(Long page, Long limit) {
        Page<SkuInfo> iPage = new Page<>();
        iPage.setSize(limit);
        iPage.setCurrent(page);
        IPage<SkuInfo> SkuInfoPage = skuInfoMapper.selectPage(iPage, null);
        return SkuInfoPage;
    }

    /**
     * 添加sku
     *
     * @param skuInfo
     */
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        // 添加sku_info
        skuInfoMapper.insert(skuInfo);
        Long skuId = skuInfo.getId();

        // 添加sku_image
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() > 0) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuId);
                skuImageMapper.insert(skuImage);
            }
        }

        // 添加sku_attr_value
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size() > 0) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuId);
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }

        // 添加sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuId);
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }

    }

    /**
     * 上架
     *
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);

        // 同步搜索引擎
        listFeignClient.onSale(skuId);

    }

    /**
     * 下架
     *
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);

        // 同步搜索引擎
        listFeignClient.cancelSale(skuId);
    }

    /**
     * 根据skuId查询商品信息、图片信息
     * 注解实现分布式锁  redis缓存
     *
     * @param skuId
     * @return
     */
    @Override
    @GmallCache(prefix = "sku")
    public SkuInfo getSkuInfoById(Long skuId) {
        SkuInfo skuInfo = null;
        skuInfo = getSkuInfoFromDb(skuId);
        return skuInfo;
    }

    /**
     * 根据skuId查询商品信息、图片信息
     * 代码实现分布式锁  redis缓存(副本)
     *
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoByIdBak(Long skuId) {
        SkuInfo skuInfo = null;
        // 读取缓存 sku:skuId:info
        skuInfo = (SkuInfo) redisTemplate.opsForValue().get(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX);

        // 缓存无数据读取数据库返回
        if (skuInfo == null) {
            // 如用缓存中没有的key恶意访问数据库 使用redis分布式锁 返回ture才能访问DB 锁的key：sku:skuId:lock
            String lockTag = UUID.randomUUID().toString();
            Boolean OK = redisTemplate.opsForValue().setIfAbsent(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX, lockTag, 3, TimeUnit.SECONDS);

            if (OK) {
                //缓存无数据则访问数据库
                skuInfo = getSkuInfoFromDb(skuId);
                if (skuInfo != null) {
                    //同步redis缓存
                    redisTemplate.opsForValue().set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX, skuInfo);
                } else {
                    redisTemplate.opsForValue().set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX, null, 1, TimeUnit.MINUTES);
                }


                // 释放锁
//                String delTag = (String) redisTemplate.opsForValue().get(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX);
//                if (delTag.equals(lockTag)){//(防止其他线程误删锁 判断锁的值)
//                    redisTemplate.delete(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX);
//                }

                // LUA脚本执行释放锁步骤 更安全
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";// 脚本查询是否存在 存在则删除 否则返回0
                // 设置lua脚本返回的数据类型
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                // 设置lua脚本返回类型为Long
                redisScript.setResultType(Long.class);
                redisScript.setScriptText(script);
                Long execute = (Long) redisTemplate.execute(redisScript, Arrays.asList("sku:" + skuId + ":lock"), lockTag);// 执行脚本

            } else {
                // 缓存中没有数据 也没拿到锁
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 自旋(必须加return，不然会有新线程去调用getSkuInfoById(skuId)，main线程继续向下执行)
                return getSkuInfoById(skuId);
            }
        }

        // 缓存有数据直接返回
        return skuInfo;
    }

    /**
     * 访问数据库 查询商品基本信息和图片
     *
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoFromDb(Long skuId) {
        //商品信息
        SkuInfo skuInfo;
        skuInfo = skuInfoMapper.selectById(skuId);
        //图片信息
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(queryWrapper);
        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }

    /**
     * 根据skuId获取价格信息
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        return skuInfo.getPrice();
    }

    /**
     * 获取spu销售属性，spu销售属性值，对应的sku销售属性
     *
     * @param spuId
     * @param skuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long spuId, Long skuId) {
        return skuSaleAttrValueMapper.selectSpuSaleAttrListCheckBySku(spuId, skuId);
    }

    /**
     * 获取销售属性切换的hash表
     *
     * @param spuId
     * @return
     */
    @Override
    public List<Map<String, Object>> getSaleAttrValuesBySpu(Long spuId) {
        return skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
    }


}
