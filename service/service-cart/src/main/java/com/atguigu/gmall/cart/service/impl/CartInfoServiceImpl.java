package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartInfoServiceImpl implements CartInfoService {

    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 添加/修改购物车
     *
     * @param cartInfo
     */
    @Override
    public void addCart(CartInfo cartInfo) {
        SkuInfo skuInfo = productFeignClient.getSkuInfoById(cartInfo.getSkuId());
        String userId = cartInfo.getUserId();

        // 将sku信息封装进购物车
        BigDecimal price = skuInfo.getPrice();// 商品单价
        cartInfo.setCartPrice(price.multiply(new BigDecimal(cartInfo.getSkuNum())));// 单价*数量
        cartInfo.setSkuPrice(price);// 游离态数据，非数据库字段
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setSkuName(skuInfo.getSkuName());

        // 判断是添加还是修改
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("sku_id", cartInfo.getSkuId());
        CartInfo ifCart = cartInfoMapper.selectOne(queryWrapper);
        if (ifCart != null) {
            //修改
            Integer skuNumOld = ifCart.getSkuNum();// 原购物车已有个数
            Integer skuNumNew = cartInfo.getSkuNum();// 本次新增个数
            BigDecimal SkuNum = new BigDecimal("0");// 使用字符串初始化BigDecimal不会出错
            SkuNum = SkuNum.add(new BigDecimal(skuNumOld)).add(new BigDecimal(skuNumNew));

            cartInfo.setSkuNum(SkuNum.intValue());// CartInfo中SkuNum字段是Integer类型
            cartInfo.setCartPrice(price.multiply(SkuNum));// 单价*数量
            cartInfoMapper.update(cartInfo, queryWrapper);
        } else {
            // 添加
            cartInfoMapper.insert(cartInfo);
        }

        // 同步缓存
        QueryWrapper<CartInfo> queryWrapperCache = new QueryWrapper<>();
        queryWrapperCache.eq("user_id", userId);
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(queryWrapperCache);
        if (cartInfoList != null && cartInfoList.size() > 0) {
            // 先删除缓存  再更新缓存
            redisTemplate.delete(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX);

            for (CartInfo info : cartInfoList) {
                // 给购物车每个商品赋上单价
                skuInfo = productFeignClient.getSkuInfoById(info.getSkuId());
                info.setSkuPrice(skuInfo.getPrice());

                // 更新缓存
                // 大key：user:userId:cart , 小key：skuId , 小value：CartInfo
                redisTemplate.opsForHash().put(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX,
                        info.getSkuId() + "",// redis的key只能是String
                        info);
            }
        }

    }

    /**
     * 购物车列表展示
     *
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> cartList(String userId) {
        List<CartInfo> cartInfoList = null;
        // 查询缓存
        cartInfoList = (List<CartInfo>) redisTemplate.opsForHash().values(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX);

        if (cartInfoList != null && cartInfoList.size() > 0) {
            // 缓存中有数据
        } else {
            // 缓存中没数据  查数据库
            QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            cartInfoList = cartInfoMapper.selectList(queryWrapper);
            for (CartInfo cartInfo : cartInfoList) {
                // 给购物车每个商品赋上单价
                Long skuId = cartInfo.getSkuId();
                SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
                cartInfo.setSkuPrice(skuInfo.getPrice());

                // 同步缓存(方法一)
                redisTemplate.opsForHash().put(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX,
                        skuId + "",
                        cartInfo);

                // 同步缓存(方法二)
//                Map<String, Object> map = new HashMap<>();
//                map.put(cartInfo.getSkuId() + "", cartInfo);
//                redisTemplate.boundHashOps(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX).putAll(map);

            }
        }

        return cartInfoList;
    }

    /**
     * 更新购物车选中状态
     *
     * @param cartInfo
     * @return
     */
    @Override
    public void checkCart(CartInfo cartInfo) {
        String userId = cartInfo.getUserId();
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("sku_id", cartInfo.getSkuId());
        cartInfoMapper.update(cartInfo, queryWrapper);

        // 同步缓存   先查缓存，然后修改，再同步
        // get(user:userId:cart,skuId)    get(大key,小key)
        CartInfo cartInfoCache = (CartInfo) redisTemplate.opsForHash().get(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX,
                cartInfo.getSkuId() + "");

        cartInfoCache.setIsChecked(cartInfo.getIsChecked());

        redisTemplate.opsForHash().put(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX,
                cartInfo.getSkuId() + "",
                cartInfoCache);
    }

    /**
     * 合并购物车
     * @param userId
     * @param userTempId
     */
    @Override
    public void mergeCart(String userId, String userTempId) {
        // 将临时用户的购物车userId改成已登录的userId
        QueryWrapper<CartInfo> queryWrapperTemp = new QueryWrapper<>();
        queryWrapperTemp.eq("user_id", userTempId);
        List<CartInfo> tempList = cartInfoMapper.selectList(queryWrapperTemp);
        for (CartInfo tempCartInfo : tempList) {
            tempCartInfo.setUserId(userId);
            cartInfoMapper.update(tempCartInfo,queryWrapperTemp);
        }


        // 同步缓存
        QueryWrapper<CartInfo> queryWrapperCache = new QueryWrapper<>();
        queryWrapperCache.eq("user_id", userId);
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(queryWrapperCache);
        if (cartInfoList != null && cartInfoList.size() > 0) {
            // 先删除缓存  再更新缓存
            redisTemplate.delete(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX);

            for (CartInfo info : cartInfoList) {
                // 给购物车每个商品赋上单价
                SkuInfo skuInfo = productFeignClient.getSkuInfoById(info.getSkuId());
                info.setSkuPrice(skuInfo.getPrice());

                // 更新缓存
                // 大key：user:userId:cart , 小key：skuId , 小value：CartInfo
                redisTemplate.opsForHash().put(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX,
                        info.getSkuId() + "",// redis的key只能是String
                        info);
            }
        }


    }

}
