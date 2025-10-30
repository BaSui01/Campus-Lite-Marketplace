package com.campus.marketplace.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

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
public class AsyncConfig implements AsyncConfigurer {

    private final Executor virtualThreadExecutor;

    public AsyncConfig(@Qualifier("virtualThreadExecutor") Executor virtualThreadExecutor) {
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    /**
     * 配置异步任务执行器（使用虚拟线程）
     */
    @Override
    public Executor getAsyncExecutor() {
        log.info("配置异步任务复用虚拟线程执行器");
        return virtualThreadExecutor;
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
