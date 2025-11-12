package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 活动状态枚举
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Getter
public enum EventStatus {
    UPCOMING("即将开始"),
    ONGOING("进行中"),
    ENDED("已结束"),
    CANCELLED("已取消");

    private final String description;

    EventStatus(String description) {
        this.description = description;
    }
}
