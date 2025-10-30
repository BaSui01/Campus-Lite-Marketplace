package com.campus.marketplace.common.entity;

import com.campus.marketplace.common.enums.NotificationStatus;
import com.campus.marketplace.common.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知实体类 - 存储站内消息和邮件通知
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
@Entity
@Table(name = "t_notification", indexes = {
        @Index(name = "idx_receiver_status", columnList = "receiver_id,status"),
        @Index(name = "idx_receiver_type", columnList = "receiver_id,type"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 接收者ID
     */
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    /**
     * 通知类型 (订单状态变更、帖子回复、@提及等)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    /**
     * 通知标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 通知内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 关联对象ID (订单ID、帖子ID、商品ID等)
     */
    @Column(name = "related_id")
    private Long relatedId;

    /**
     * 关联对象类型 (order、post、goods等)
     */
    @Column(name = "related_type", length = 50)
    private String relatedType;

    /**
     * 跳转链接
     */
    @Column(length = 500)
    private String link;

    /**
     * 通知状态 (未读、已读、已删除)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.UNREAD;

    /**
     * 是否已发送邮件
     */
    @Column(name = "email_sent", nullable = false)
    @Builder.Default
    private Boolean emailSent = false;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 已读时间
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;
}
