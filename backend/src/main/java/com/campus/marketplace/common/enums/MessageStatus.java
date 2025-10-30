package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 消息状态枚举
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Getter
@RequiredArgsConstructor
public enum MessageStatus {

    /**
     * 未读
     */
    UNREAD("未读"),

    /**
     * 已读
     */
    READ("已读");

    /**
     * 状态描述
     */
    private final String description;
}
