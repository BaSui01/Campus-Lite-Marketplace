package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * API性能日志实体
 *
 * 用于记录API接口调用的性能数据
 *
 * @author BaSui
 * @date 2025-11-03
 */
@Entity
@Table(name = "t_api_performance_log")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiPerformanceLog extends BaseEntity {

    /**
     * API路径
     */
    @Column(name = "api_path", nullable = false, length = 200)
    private String apiPath;

    /**
     * HTTP方法（GET/POST/PUT/DELETE）
     */
    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    /**
     * 响应时间（毫秒）
     */
    @Column(name = "response_time", nullable = false)
    private Integer responseTime;

    /**
     * HTTP状态码
     */
    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 客户端IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
}
