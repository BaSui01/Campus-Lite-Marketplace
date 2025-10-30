package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.enums.NotificationStatus;
import com.campus.marketplace.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知响应DTO
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知类型
     */
    private NotificationType type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联对象ID
     */
    private Long relatedId;

    /**
     * 关联对象类型
     */
    private String relatedType;

    /**
     * 跳转链接
     */
    private String link;

    /**
     * 通知状态
     */
    private NotificationStatus status;

    /**
     * 是否已发送邮件
     */
    private Boolean emailSent;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 已读时间
     */
    private LocalDateTime readAt;
}
