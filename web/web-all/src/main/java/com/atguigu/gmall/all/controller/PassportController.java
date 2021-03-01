package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 登录页面跳转控制器
 */
@Controller
public class PassportController {

    /**
     *  跳转到登录页面
     * @param originUrl 请求要访问的url(登录成功后网关才放行请求到这个url去)
     * @return
     */
    @RequestMapping("login.html")
    public String login(String originUrl, Model model){
        // 将原始Url地址带到页面
        model.addAttribute("originUrl",originUrl);
        return "login";
    }
}
