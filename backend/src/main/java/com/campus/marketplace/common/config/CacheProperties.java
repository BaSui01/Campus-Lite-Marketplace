package com.campus.marketplace.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置类
 *
 * 支持不同缓存空间的过期时间配置
 *
 * @author BaSui
 * @date 2025-11-04
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    /**
     * 默认过期时间
     */
    private Duration defaultTtl = Duration.ofMinutes(30);

    /**
     * 各缓存空间的过期时间配置
     */
    private Map<String, Duration> ttl = new HashMap<>();

    /**
     * 缓存预热配置
     */
    private Warmup warmup = new Warmup();

    /**
     * 缓存监控配置
     */
    private Monitor monitor = new Monitor();

    @Data
    public static class Warmup {
        /**
         * 是否启用缓存预热
         */
        private boolean enabled = false;

        /**
         * 预热延迟时间（应用启动后）
         */
        private Duration delay = Duration.ofSeconds(30);

        /**
         * 需要预热的缓存空间
         */
        private String[] caches = {"user", "goods:detail", "category"};
    }

    @Data
    public static class Monitor {
        /**
         * 是否启用缓存监控
         */
        private boolean enabled = true;

        /**
         * 统计数据保留时间
         */
        private Duration retention = Duration.ofHours(24);

        /**
         * 缓存命中率阈值告警
         */
        private double hitRateThreshold = 0.8;
    }

    /**
     * 获取指定缓存空间的过期时间
     *
     * @param cacheName 缓存空间名称
     * @return 过期时间
     */
    public Duration getTtlForCache(String cacheName) {
        return ttl.getOrDefault(cacheName, defaultTtl);
    }
}