package com.atguigu.gmall.aspect;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Around("@annotation(com.atguigu.gmall.aspect.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) {
        Object proceed = null;
        // 执行前
        Object[] args = point.getArgs();// 方法参数
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();// 目标方法对象
        ///String methodName = method.getName();// 方法名
        //Class<?> returnType = method.getReturnType();// 返回值类型
        GmallCache annotation = method.getAnnotation(GmallCache.class);// 方法上的@GmallCache 注解信息

        // 目标执行
        // 准备一个公共key
        String agrKey = "";
        for (Object arg : args) {
            agrKey = agrKey + ":" + arg;
        }
        @NotNull String prefix = annotation.prefix();
        String commonKey = "GmallCache:" + prefix + agrKey;

        // 查缓存
        proceed = getCache(methodSignature, commonKey);
        if (proceed == null) {
            try {
                // 防止缓存穿透、缓存击穿
                String lockTag = UUID.randomUUID().toString();
                Boolean OK = redisTemplate.opsForValue().setIfAbsent(commonKey, lockTag, 3, TimeUnit.MINUTES);

                if (OK) {
                    // 查数据库
                    proceed = point.proceed();
                    if (proceed != null){
                        // 同步缓存  Json字符串格式
                        redisTemplate.opsForValue().set(commonKey, JSONObject.toJSONString(proceed));
                    }else {
                        // 数据库中没有该数据  设置空值与过期时间  防止缓存穿透
                        redisTemplate.opsForValue().set(commonKey, null, 1, TimeUnit.MINUTES);
                    }

                    // LUA脚本执行释放锁步骤 更安全
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";// 脚本查询是否存在 存在则删除 否则返回0
                    // 设置lua脚本返回的数据类型
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    // 设置lua脚本返回类型为Long
                    redisScript.setResultType(Long.class);
                    redisScript.setScriptText(script);
                    Long execute = (Long) redisTemplate.execute(redisScript, Arrays.asList(commonKey), lockTag);// 执行脚本

                } else {
                    // 缓存中没有数据 也没拿到锁
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 自旋(必须加return，不然会有新线程去调用getCache(methodSignature, commonKey)，main线程继续向下执行)
                    return getCache(methodSignature, commonKey);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }finally {

            }
        }

        // 执行后

        return proceed;
    }

    /**
     * 查询缓存
     *
     * @param methodSignature
     * @param commonKey
     * @return
     */
    private Object getCache(MethodSignature methodSignature, String commonKey) {
        // 查缓存
        String cache = (String) redisTemplate.opsForValue().get(commonKey);
        if (!StringUtils.isEmpty(cache)) {
            // Json字符串转Java对象
            return JSONObject.parseObject(cache,methodSignature.getReturnType());
        }

        return null;
    }

}
