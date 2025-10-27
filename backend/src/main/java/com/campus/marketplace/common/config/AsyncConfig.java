package com.campus.marketplace.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 异步任务配置
 * 
 * 配置 @Async 注解使用虚拟线程执行器
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 配置异步任务执行器（使用虚拟线程）
     */
    @Override
    public Executor getAsyncExecutor() {
        log.info("配置异步任务使用虚拟线程执行器");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 配置异步任务异常处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("异步任务执行异常 - 方法: {}, 参数: {}", 
                    method.getName(), params, throwable);
        };
    }
}
