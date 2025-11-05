package com.campus.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警消息DTO
 *
 * @author BaSui
 * @date 2025-11-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage {

    /**
     * 告警ID
     */
    private String id;

    /**
     * 告警标题
     */
    private String title;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 严重级别：critical, warning, info
     */
    private String severity;

    /**
     * 告警类型
     */
    private String type;

    /**
     * 告警来源
     */
    private String source;

    /**
     * 告警时间
     */
    private LocalDateTime timestamp;

    /**
     * 标签
     */
    private String labels;

    /**
     * 注释
     */
    private String annotations;

    /**
     * 是否需要确认
     */
    @Builder.Default
    private Boolean needsAck = false;

    /**
     * 确认状态
     */
    @Builder.Default
    private Boolean acknowledged = false;

    /**
     * 确认人
     */
    private String acknowledgedBy;

    /**
     * 确认时间
     */
    private LocalDateTime acknowledgedAt;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 联系方式
     */
    private String contactMethod;
}