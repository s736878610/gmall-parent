package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

import java.util.HashMap;
import java.util.Map;

public interface UserService {
    HashMap<String, Object> login(UserInfo userInfo);

    Map<String, Object> verify(String token);
}
