package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 消息状态枚举
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Getter
public enum MessageStatus {
    
    /**
     * 未读
     */
    UNREAD("未读"),
    
    /**
     * 已读
     */
    READ("已读");

    private final String description;

    MessageStatus(String description) {
        this.description = description;
    }
}
