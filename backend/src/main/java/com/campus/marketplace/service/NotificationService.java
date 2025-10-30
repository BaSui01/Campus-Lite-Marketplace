package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.NotificationResponse;
import com.campus.marketplace.common.enums.NotificationStatus;
import com.campus.marketplace.common.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口 - 站内消息和邮件通知
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
public interface NotificationService {

    /**
     * 发送站内通知
     *
     * @param receiverId  接收者ID
     * @param type        通知类型
     * @param title       通知标题
     * @param content     通知内容
     * @param relatedId   关联对象ID
     * @param relatedType 关联对象类型
     * @param link        跳转链接
     */
    void sendNotification(
            Long receiverId,
            NotificationType type,
            String title,
            String content,
            Long relatedId,
            String relatedType,
            String link
    );

    /**
     * 发送邮件通知（异步）
     *
     * @param receiverId 接收者ID
     * @param subject    邮件主题
     * @param text       邮件内容
     */
    void sendEmailNotification(Long receiverId, String subject, String text);

    /**
     * 同时发送站内通知和邮件通知
     *
     * @param receiverId  接收者ID
     * @param type        通知类型
     * @param title       通知标题
     * @param content     通知内容
     * @param relatedId   关联对象ID
     * @param relatedType 关联对象类型
     * @param link        跳转链接
     */
    void sendNotificationWithEmail(
            Long receiverId,
            NotificationType type,
            String title,
            String content,
            Long relatedId,
            String relatedType,
            String link
    );

    /**
     * 查询通知列表
     *
     * @param status   通知状态（可选）
     * @param pageable 分页参数
     * @return 通知列表
     */
    Page<NotificationResponse> listNotifications(NotificationStatus status, Pageable pageable);

    /**
     * 查询未读通知数量
     *
     * @return 未读数量
     */
    long getUnreadCount();

    /**
     * 标记通知为已读
     *
     * @param notificationIds 通知ID列表
     */
    void markAsRead(List<Long> notificationIds);

    /**
     * 全部标记为已读
     */
    void markAllAsRead();

    /**
     * 删除通知
     *
     * @param notificationIds 通知ID列表
     */
    void deleteNotifications(List<Long> notificationIds);

    /**
     * 基于模板渲染并发送通知（尊重偏好/退订）
     */
    void sendTemplateNotification(Long receiverId,
                                  String templateCode,
                                  Map<String, Object> params,
                                  NotificationType type,
                                  Long relatedId,
                                  String relatedType,
                                  String link);
}
