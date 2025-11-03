package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.ErrorSeverity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

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
    @Index(name = "idx_error_time", columnList = "error_time DESC"),
    @Index(name = "idx_error_severity", columnList = "severity"),
    @Index(name = "idx_error_type", columnList = "error_type"),
    @Index(name = "idx_error_resolved", columnList = "is_resolved")
})
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLog extends BaseEntity {

    /**
     * 错误时间
     */
    @Column(name = "error_time", nullable = false)
    private LocalDateTime errorTime;

    /**
     * 错误类型（异常类名）
     */
    @Column(name = "error_type", nullable = false, length = 500)
    private String errorType;

    /**
     * 错误消息
     */
    @Column(name = "error_message", nullable = false, length = 2000)
    private String errorMessage;

    /**
     * 堆栈跟踪
     */
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    /**
     * 严重程度
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private ErrorSeverity severity;

    /**
     * 请求路径
     */
    @Column(name = "request_path", length = 500)
    private String requestPath;

    /**
     * HTTP方法
     */
    @Column(name = "http_method", length = 10)
    private String httpMethod;

    /**
     * 客户端IP
     */
    @Column(name = "client_ip", length = 50)
    private String clientIp;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 请求参数（JSONB格式）
     */
    @Type(JsonBinaryType.class)
    @Column(name = "request_params", columnDefinition = "JSONB")
    private Map<String, Object> requestParams;

    /**
     * 服务器信息（主机名、IP等）
     */
    @Column(name = "server_info", length = 200)
    private String serverInfo;

    /**
     * 是否已解决
     */
    @Column(name = "is_resolved")
    @Builder.Default
    private Boolean isResolved = false;

    /**
     * 解决时间
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * 解决备注
     */
    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    /**
     * 是否已告警
     */
    @Column(name = "is_alerted")
    @Builder.Default
    private Boolean isAlerted = false;

    /**
     * 告警时间
     */
    @Column(name = "alerted_at")
    private LocalDateTime alertedAt;
}
