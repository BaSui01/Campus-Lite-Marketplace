package com.campus.marketplace.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 批量操作配置
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "batch")
public class BatchConfiguration {

    private ThreadPool threadPool = new ThreadPool();
    private Shard shard = new Shard();
    private RateLimit rateLimit = new RateLimit();
    private Limits limits = new Limits();
    private Monitor monitor = new Monitor();
    private Task task = new Task();

    @Data
    public static class ThreadPool {
        private int coreSize = 4;
        private int maxSize = 20;
        private int queueCapacity = 1000;
        private String threadNamePrefix = "batch-";
    }

    @Data
    public static class Shard {
        private int smallBatchSize = 100;
        private int mediumBatchSize = 1000;
        private int smallShardSize = 100;
        private int largeShardSize = 500;
    }

    @Data
    public static class RateLimit {
        private double userPermitsPerSecond = 0.1;
        private double vipPermitsPerSecond = 1.0;
        private double adminPermitsPerSecond = 10.0;
    }

    @Data
    public static class Limits {
        private int regularUser = 500;
        private int vipUser = 2000;
        private int admin = 999999;
    }

    @Data
    public static class Monitor {
        private boolean enabled = true;
        private int metricsInterval = 60;
        private double alertThreshold = 0.5;
    }

    @Data
    public static class Task {
        private int maxRetry = 3;
        private int timeoutMultiplier = 2;
        private int cleanupDays = 30;
    }
}
