package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 通知状态枚举
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
@Getter
public enum NotificationStatus {
    /**
     * 未读
     */
    UNREAD("未读"),

    /**
     * 已读
     */
    READ("已读"),

    /**
     * 已删除
     */
    DELETED("已删除");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }
}
