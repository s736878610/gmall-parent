package com.atguigu.gmall.gateway.filter;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.result.ResultCodeEnum;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 网关鉴权过滤器
 */
@Component
public class AuthFilter implements GlobalFilter {

    @Autowired
    UserFeignClient userFeignClient;

    @Value("${authUrls.url}")
    private String authUrls;

    AntPathMatcher antPathMatcher = new AntPathMatcher();


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String uri = request.getURI().toString();

        // 请求鉴权，sso系统

        // 可以直接放行的资源
        if (uri.contains("passport") || uri.contains("jpg") || uri.contains("ico") || uri.contains("js") || uri.contains("png") || uri.contains("css")) {
            return chain.filter(exchange);
        }

        // 黑名单
        if (antPathMatcher.match("**/inner/**", uri)) {
            return out(response, ResultCodeEnum.PERMISSION);// 拒绝访问，并返回209状态码(没有权限)
        }

        // 获取请求中的token
        String userId = null;
        String token = getCookiesValue(request, "token");
        if (StringUtils.isNotEmpty(token)) {
            Map<String, Object> verifyMap = userFeignClient.verify(token);
            if (verifyMap != null && verifyMap.size() > 0) {
                userId = (String) verifyMap.get("userId");
            }
        }

        if (antPathMatcher.match("**/auth/**", uri)) {
            if (StringUtils.isEmpty(userId)){
                return out(response, ResultCodeEnum.PERMISSION);// 拒绝访问，并返回209状态码(没有权限)
            }
        }


        // 白名单
        String[] authUrlsArray = authUrls.split(",");// 将字符串以","分割  封装成白名单数组
        if (authUrlsArray != null && authUrlsArray.length > 0) {
            for (String authUrl : authUrlsArray) {
                if (uri.contains(authUrl)) {
                    // 如果请求中包含白名单字段，则需要携带认证Token才能访问，否则重定向到登录页面
                    if (StringUtils.isEmpty(userId)) {
                        response.setStatusCode(HttpStatus.SEE_OTHER);// 303 重定向
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://passport.gmall.com/login.html?originUrl=" + uri);// set(请求头,url路径)  网关将原始地址带过去
                        Mono<Void> voidMono = response.setComplete();// 完毕，执行
                        return voidMono;
                    }
                }
            }
        }

        // 将userId传递到后台
        if (StringUtils.isNotEmpty(userId)) {
            request.mutate().header("userId", userId);
            exchange.mutate().request(request);
        }

        // 将userTempId传递到后台
        String userTempId = getCookiesValue(request, "userTempId");
        if (StringUtils.isNotEmpty(userTempId)) {
            request.mutate().header("userTempId", userTempId);
            exchange.mutate().request(request);
        }


        System.out.println(uri);
        return chain.filter(exchange);
    }

    /**
     * 获取请求中的Token
     *
     * @param request
     * @return
     */
    private String getCookiesValue(ServerHttpRequest request, String cookiesName) {
        String result = "";
        // 如果是同步请求  可以从cookie获取
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies != null && cookies.size() > 0) {
            List<HttpCookie> cookieList = cookies.get(cookiesName);
            if (cookieList != null && cookieList.size() > 0) {
                for (HttpCookie cookie : cookieList) {
                    String name = cookie.getName();
                    if (name.equals(cookiesName)) {
                        result = cookie.getValue();
                    }
                }
            }
        }

        // 如果是购物车的异步请求(无cookies)  可以从headers获取
        if (StringUtils.isEmpty(result)) {
            List<String> stringList = request.getHeaders().get(cookiesName);
            if (stringList != null && stringList.size() > 0) {
                result = stringList.get(0);
            }
        }

        return result;
    }

    /**
     * 接口鉴权失败返回数据
     *
     * @param response
     * @param resultCodeEnum
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        // 返回用户没有权限登录
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        Mono<DataBuffer> just = Mono.just(wrap);
        Mono<Void> voidMono = response.writeWith(just);
        // 输入到页面
        return voidMono;
    }

}



