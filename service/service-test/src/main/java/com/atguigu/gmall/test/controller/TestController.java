package com.atguigu.gmall.test.controller;


import com.atguigu.gmall.result.Result;
import org.aspectj.lang.annotation.Around;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;

//    @RequestMapping("getStock")
//    public Result getStock() {
//        Integer stock = 0;
//
//        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock",1);
//
//        if(lock){
//            stock = (Integer)redisTemplate.opsForValue().get("stock");
//
//            stock -- ;
//
//            System.out.println("剩余库存数量："+stock);
//
//            redisTemplate.opsForValue().set("stock",stock);
//
//            redisTemplate.delete("lock");
//        }
//
//        return Result.ok(stock);
//    }


    @RequestMapping("getStock")
    public Result getStock() {
        Integer stock = 0;

        RLock lock = redissonClient.getLock("lock");// 只要锁的名称相同就是同一把锁
        lock.lock();// 加锁

        stock = (Integer) redisTemplate.opsForValue().get("stock");
        stock--;
        System.out.println("剩余库存数量：" + stock);
        redisTemplate.opsForValue().set("stock", stock);

        lock.unlock(); // 解锁

        return Result.ok(stock);
    }


}
