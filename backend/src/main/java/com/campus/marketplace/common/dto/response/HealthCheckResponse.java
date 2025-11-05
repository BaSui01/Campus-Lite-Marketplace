package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.enums.HealthStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查响应DTO
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {

    /**
     * 检查时间
     */
    private LocalDateTime checkedAt;

    /**
     * 整体健康状态
     */
    private HealthStatus status;

    /**
     * 各组件状态
     */
    private Map<String, ComponentHealth> components;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 组件健康状态
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentHealth {
        /**
         * 组件名称
         */
        private String name;
        
        /**
         * 健康状态
         */
        private HealthStatus status;
        
        /**
         * 详情信息
         */
        private Map<String, Object> details;
        
        /**
         * 错误信息
         */
        private String error;
    }
}
