package com.campus.marketplace.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

/**
 * Notification Template - 通知模板配置
 * 
 * 管理系统通知的模板（如：订单通知、评价提醒、系统公告等）
 * 支持多渠道（应用内、邮件、推送）
 * 支持软删除，便于模板版本管理和回滚
 *
 * @author BaSui
 * @date 2025-10-29
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_notification_template", indexes = {
        @Index(name = "uk_tpl_code", columnList = "code", unique = true)
})
@SQLRestriction("deleted = false")
public class NotificationTemplate extends BaseEntity {

    /**
     * 模板编码（唯一标识）
     * 例如：ORDER_PAID, REVIEW_RECEIVED
     */
    @Column(name = "code", nullable = false, length = 100)
    private String code;

    /**
     * 标题模板Key（支持国际化）
     */
    @Column(name = "title_key", nullable = false, length = 200)
    private String titleKey;

    /**
     * 内容模板Key（支持国际化）
     */
    @Column(name = "content_key", nullable = false, length = 200)
    private String contentKey;

    /**
     * 可用渠道（逗号分隔）
     * 例如：IN_APP,EMAIL,WEB_PUSH
     */
    @Column(name = "channels", nullable = false, length = 100)
    private String channels;
}
