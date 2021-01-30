package com.atguigu.gmall.item.config;

import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.concurrent.RejectedExecutionHandlers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置类
 */
@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                10,// 核心线程数
                1000,// 最大线程数
                3,// 空闲线程存活时间
                TimeUnit.SECONDS,// 空闲线程存活时间参数的时间单位
                new ArrayBlockingQueue<>(1024),// 阻塞队列
                new DefaultThreadFactory("MyDefaultThreadFactory"),// 线程工厂
                new ThreadPoolExecutor.CallerRunsPolicy());// 拒绝策略
        return threadPoolExecutor;
    }

}
