package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.HealthStatus;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查记录实体
 * 
 * 用于存储系统健康检查的历史记录
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_health_check_record", indexes = {
    @Index(name = "idx_health_check_time", columnList = "check_time DESC"),
    @Index(name = "idx_health_status", columnList = "overall_status")
})
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckRecord extends BaseEntity {

    /**
     * 检查时间
     */
    @Column(name = "check_time", nullable = false)
    private LocalDateTime checkTime;

    /**
     * 整体健康状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", nullable = false, length = 20)
    private HealthStatus overallStatus;

    /**
     * 各组件状态详情（JSONB格式）
     */
    @Type(JsonBinaryType.class)
    @Column(name = "component_details", columnDefinition = "JSONB")
    private Map<String, ComponentStatus> componentDetails;

    /**
     * 响应时间（毫秒）
     */
    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    /**
     * 错误信息
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * 组件状态（内部类）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentStatus {
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
