package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Getter
public enum UserStatus {
    
    /**
     * 正常状态
     */
    ACTIVE("正常"),
    
    /**
     * 已封禁
     */
    BANNED("已封禁"),
    
    /**
     * 已注销
     */
    DELETED("已注销");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}
