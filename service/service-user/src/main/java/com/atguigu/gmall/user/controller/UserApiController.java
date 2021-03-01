package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * user 认证中心控制器
 */
@RestController
@RequestMapping("api/user/passport")
public class UserApiController {

    @Autowired
    UserService userService;

    @RequestMapping("inner/ping")
    public String ping() {
        return "pong";
    }

    /**
     * 登录校验，生成Token(用户点击登录按钮，异步调用)
     *
     * @param userInfo
     * @return
     */
    @RequestMapping("login")
    public Result login(@RequestBody UserInfo userInfo,HttpServletRequest request) {
        String userTempId = request.getHeader("userTempId");// 给合并购物车使用
        HashMap<String, Object> map = userService.login(userInfo,userTempId);
        if (map == null && map.size() < 0) {
            return Result.fail();
        }
        return Result.ok(map);
    }

    /**
     * 校验登录状态
     * @param token
     * @return
     */
    @RequestMapping("verify/{token}")
    Map<String, Object> verify(@PathVariable("token") String token){
        Map<String, Object> map = userService.verify(token);
        return map;
    }

    /**
     * 获取用户地址
     * @param userId
     * @return
     */
    @RequestMapping("findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable("userId") String userId){
        return userService.findUserAddressListByUserId(userId);
    }

}
