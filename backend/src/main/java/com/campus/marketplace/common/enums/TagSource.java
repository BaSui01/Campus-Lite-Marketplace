package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 标签来源枚举
 *
 * 用于区分标签是系统自动提取还是用户手动输入
 *
 * @author BaSui 😎 - AI自动提取还是用户手动打？看来源！
 * @since 2025-11-03
 */
@Getter
public enum TagSource {

    /**
     * 系统自动提取
     * 通过NLP分析评价内容自动生成
     */
    SYSTEM("系统提取"),

    /**
     * 用户手动输入
     * 用户在评价时主动选择或输入
     */
    USER_INPUT("用户输入");

    private final String description;

    TagSource(String description) {
        this.description = description;
    }
}
