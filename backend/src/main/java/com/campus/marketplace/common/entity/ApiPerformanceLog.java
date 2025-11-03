package com.campus.marketplace.common.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * API性能日志实体
 * 
 * 用于记录API接口调用的性能数据
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_api_performance_log", indexes = {
    @Index(name = "idx_api_endpoint", columnList = "endpoint"),
    @Index(name = "idx_api_time", columnList = "request_time DESC"),
    @Index(name = "idx_api_duration", columnList = "duration_ms DESC"),
    @Index(name = "idx_api_status", columnList = "http_status")
})
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiPerformanceLog extends BaseEntity {

    /**
     * HTTP方法（GET/POST/PUT/DELETE）
     */
    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    /**
     * 请求端点
     */
    @Column(name = "endpoint", nullable = false, length = 500)
    private String endpoint;

    /**
     * 请求时间
     */
    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime;

    /**
     * 响应时间（毫秒）
     */
    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    /**
     * HTTP状态码
     */
    @Column(name = "http_status")
    private Integer httpStatus;

    /**
     * 是否成功
     */
    @Column(name = "is_success")
    private Boolean isSuccess;

    /**
     * 是否慢查询（> 1000ms）
     */
    @Column(name = "is_slow")
    private Boolean isSlow;

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
     * 错误信息
     */
    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
}
