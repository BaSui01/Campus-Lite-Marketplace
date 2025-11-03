package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 评价状态枚举
 *
 * 用于标识评价的可见性和审核状态
 *
 * @author BaSui 😎 - 正常显示、管理员隐藏、被举报？看状态！
 * @since 2025-11-03
 */
@Getter
public enum ReviewStatus {

    /**
     * 正常状态
     * 评价正常显示，对所有用户可见
     */
    NORMAL("正常"),

    /**
     * 已隐藏
     * 管理员手动隐藏，仅评价者和管理员可见
     */
    HIDDEN("已隐藏"),

    /**
     * 已举报
     * 评价被举报，待审核处理
     */
    REPORTED("已举报");

    private final String description;

    ReviewStatus(String description) {
        this.description = description;
    }
}
