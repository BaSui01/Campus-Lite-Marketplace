package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 性能报表响应DTO
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReportResponse {

    /**
     * 报表时间范围
     */
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /**
     * 系统健康度评分（0-100）
     */
    private Double healthScore;

    /**
     * 系统健康概览
     */
    private HealthOverview healthOverview;

    /**
     * API性能概览
     */
    private ApiPerformanceOverview apiPerformanceOverview;

    /**
     * 错误统计
     */
    private ErrorStatistics errorStatistics;

    /**
     * 优化建议列表
     */
    private List<OptimizationSuggestion> suggestions;

    /**
     * 健康概览
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthOverview {
        /**
         * 健康检查总次数
         */
        private Long totalChecks;
        
        /**
         * 健康次数
         */
        private Long healthyCount;
        
        /**
         * 降级次数
         */
        private Long degradedCount;
        
        /**
         * 不健康次数
         */
        private Long unhealthyCount;
        
        /**
         * 健康率（百分比）
         */
        private Double healthRate;
        
        /**
         * 平均响应时间（毫秒）
         */
        private Double avgResponseTime;
    }

    /**
     * API性能概览
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiPerformanceOverview {
        /**
         * 总请求数
         */
        private Long totalRequests;
        
        /**
         * 成功请求数
         */
        private Long successRequests;
        
        /**
         * 失败请求数
         */
        private Long failedRequests;
        
        /**
         * 成功率（百分比）
         */
        private Double successRate;
        
        /**
         * 慢查询数
         */
        private Long slowQueryCount;
        
        /**
         * 慢查询率（百分比）
         */
        private Double slowQueryRate;
        
        /**
         * 平均响应时间（毫秒）
         */
        private Double avgResponseTime;
        
        /**
         * P95响应时间（毫秒）
         */
        private Double p95ResponseTime;
        
        /**
         * P99响应时间（毫秒）
         */
        private Double p99ResponseTime;
        
        /**
         * Top 10慢接口
         */
        private List<ApiPerformanceStatistics.EndpointStats> top10SlowApis;
    }

    /**
     * 错误统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorStatistics {
        /**
         * 总错误数
         */
        private Long totalErrors;
        
        /**
         * 严重错误数
         */
        private Long criticalErrors;
        
        /**
         * 高级错误数
         */
        private Long highErrors;
        
        /**
         * 中级错误数
         */
        private Long mediumErrors;
        
        /**
         * 低级错误数
         */
        private Long lowErrors;
        
        /**
         * 未解决错误数
         */
        private Long unresolvedErrors;
        
        /**
         * Top 10错误类型
         */
        private Map<String, Long> top10ErrorTypes;
    }

    /**
     * 优化建议
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationSuggestion {
        /**
         * 建议类型
         */
        private SuggestionType type;
        
        /**
         * 优先级（高/中/低）
         */
        private String priority;
        
        /**
         * 建议标题
         */
        private String title;
        
        /**
         * 建议描述
         */
        private String description;
        
        /**
         * 影响范围
         */
        private String impact;
        
        /**
         * 预期收益
         */
        private String expectedBenefit;
    }

    /**
     * 建议类型
     */
    public enum SuggestionType {
        PERFORMANCE,  // 性能优化
        STABILITY,    // 稳定性
        RESOURCE,     // 资源使用
        SECURITY      // 安全性
    }
}
