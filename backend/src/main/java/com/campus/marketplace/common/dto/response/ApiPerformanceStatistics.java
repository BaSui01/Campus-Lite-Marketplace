package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API性能统计DTO
 * 
 * 这是一个工具类，只包含静态内部类
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public class ApiPerformanceStatistics {

    // 私有构造函数，防止实例化
    private ApiPerformanceStatistics() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 端点统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointStats {
        /**
         * 端点
         */
        private String endpoint;
        
        /**
         * 调用次数
         */
        private Long requestCount;
        
        /**
         * 平均响应时间（毫秒）
         */
        private Double avgDurationMs;
        
        /**
         * 最大响应时间（毫秒）
         */
        private Long maxDurationMs;
        
        /**
         * 慢查询次数
         */
        private Long slowCount;
        
        /**
         * 错误次数
         */
        private Long errorCount;
    }

    /**
     * QPS数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QpsData {
        /**
         * 时间点（分钟级）
         */
        private LocalDateTime minute;
        
        /**
         * 请求数量
         */
        private Long requestCount;
        
        /**
         * QPS（每秒请求数）
         */
        private Double qps;
    }
}
