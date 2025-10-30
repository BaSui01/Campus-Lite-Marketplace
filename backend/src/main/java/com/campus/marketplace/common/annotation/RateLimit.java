package com.campus.marketplace.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 * 
 * 使用 Redis + 滑动窗口算法实现接口频率限制
 * 
 * 使用示例：
 * <pre>
 * {@code @RateLimit(key = "sendMessage", maxRequests = 10, timeWindow = 60)}
 * public void sendMessage() { ... }
 * </pre>
 * 
 * @author BaSui 😎
 * @date 2025-10-27
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流键（用于区分不同接口）
     * 
     * 默认使用 "类名:方法名"
     */
    String key() default "";

    /**
     * 时间窗口内最大请求次数
     * 
     * 默认: 100 次
     */
    int maxRequests() default 100;

    /**
     * 时间窗口大小（秒）
     * 
     * 默认: 60 秒
     */
    long timeWindow() default 60;

    /**
     * 时间单位
     * 
     * 默认: 秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 限流维度（全局/用户/IP）
     * 
     * 默认: 用户级别
     */
    LimitType limitType() default LimitType.USER;

    /**
     * 限流算法（默认滑动窗口）
     */
    Algorithm algorithm() default Algorithm.SLIDING_WINDOW;

    /**
     * 令牌桶容量（algorithm=TOKEN_BUCKET 生效）。
     * 默认 0 表示使用 maxRequests 值。
     */
    int tokenBucketCapacity() default 0;

    /**
     * 每个补给周期补充的令牌数（algorithm=TOKEN_BUCKET 生效）。
     * 默认 0 表示使用 tokenBucketCapacity 值。
     */
    int refillTokens() default 0;

    /**
     * 令牌补给周期（与 timeUnit 搭配，algorithm=TOKEN_BUCKET 生效）。
     * 默认 0 表示使用 timeWindow 值。
     */
    long refillInterval() default 0;

    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 全局限流（针对所有用户）
         */
        GLOBAL,

        /**
         * 用户级别限流（针对单个用户）
         */
        USER,

        /**
         * IP 级别限流（针对单个 IP）
         */
        IP
    }

    /**
     * 限流算法类型
     */
    enum Algorithm {
        SLIDING_WINDOW,
        TOKEN_BUCKET
    }
}
