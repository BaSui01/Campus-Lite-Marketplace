package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.ErrorSeverity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 错误日志实体
 * 
 * 用于记录系统运行过程中的错误信息
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_error_log", indexes = {
    @Index(name = "idx_error_severity", columnList = "severity"),
    @Index(name = "idx_error_type", columnList = "error_type"),
    @Index(name = "idx_error_resolved", columnList = "resolved")
})
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLog extends BaseEntity {

    /**
     * 错误类型（异常类名）
     */
    @Column(name = "error_type", nullable = false, length = 50)
    private String errorType;

    /**
     * 错误消息
     */
    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 堆栈跟踪
     */
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    /**
     * 请求URL
     */
    @Column(name = "request_url", length = 500)
    private String requestUrl;

    /**
     * 请求方法（GET/POST等）
     */
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 严重程度
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    @Builder.Default
    private ErrorSeverity severity = ErrorSeverity.MEDIUM;

    /**
     * 是否已解决
     */
    @Column(name = "resolved")
    @Builder.Default
    private Boolean resolved = false;

    /**
     * 解决时间
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * 解决人ID
     */
    @Column(name = "resolved_by")
    private Long resolvedBy;
}
