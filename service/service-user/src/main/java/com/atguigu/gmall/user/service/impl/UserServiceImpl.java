package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserMapper;
import com.atguigu.gmall.user.service.UserService;
import com.atguigu.gmall.util.MD5;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 登录校验(用户点击登录按钮，异步调用)
     *
     * @param userInfo
     * @return
     */
    @Override
    public HashMap<String, Object> login(UserInfo userInfo) {
        HashMap<String, Object> map = new HashMap<>();
        // 查数据  比较有无数据
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name", userInfo.getLoginName());
        queryWrapper.eq("passwd", MD5.encrypt(userInfo.getPasswd()));
        UserInfo info = userMapper.selectOne(queryWrapper);

        if (info == null) {
            // 无数据  直接跳出
            return null;
        }

        map.put("name", info.getName());
        map.put("nickName", info.getNickName());
        String token = UUID.randomUUID().toString();
        map.put("token", token);

        // 放入缓存
        redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token,
                info.getId(),
                RedisConst.USERKEY_TIMEOUT,// 60 * 60 * 24 * 7 (过期时间7天)
                TimeUnit.SECONDS);

        return map;
    }

    /**
     * 从redis取数据 认证登录状态
     *
     * @param token
     * @return
     */
    @Override
    public Map<String, Object> verify(String token) {
        HashMap<String, Object> map = new HashMap<>();
        Object obj = redisTemplate.opsForValue().get(RedisConst.USER_LOGIN_KEY_PREFIX + token);
        if (obj == null) {
            return null;
        }
        String userId = obj.toString();
        map.put("userId", userId);
        return map;
    }
}
