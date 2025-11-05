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
    @Index(name = "idx_health_checked_at", columnList = "checked_at DESC"),
    @Index(name = "idx_health_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckRecord extends BaseEntity {

    /**
     * 服务名称
     */
    @Column(name = "service_name", nullable = false, length = 50)
    private String serviceName;

    /**
     * 检查类型
     */
    @Column(name = "check_type", nullable = false, length = 30)
    private String checkType;

    /**
     * 健康状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HealthStatus status;

    /**
     * 响应时间（毫秒）
     */
    @Column(name = "response_time", nullable = false)
    private Long responseTime;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 详情（JSONB格式）
     */
    @Type(JsonBinaryType.class)
    @Column(name = "details", columnDefinition = "JSONB")
    private Map<String, Object> details;

    /**
     * 检查时间
     */
    @Column(name = "checked_at", nullable = false)
    @Builder.Default
    private LocalDateTime checkedAt = LocalDateTime.now();
}
