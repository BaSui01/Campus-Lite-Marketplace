package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 情感类型枚举
 *
 * 用于评价的情感分析结果分类
 *
 * @author BaSui 😎 - 情感分析三剑客：积极/中性/消极！
 * @since 2025-11-03
 */
@Getter
public enum SentimentType {

    /**
     * 积极情感（好评）
     * 情感得分 > 0.6
     */
    POSITIVE("积极"),

    /**
     * 中性情感（中评）
     * 情感得分 0.4 ~ 0.6
     */
    NEUTRAL("中性"),

    /**
     * 消极情感（差评）
     * 情感得分 < 0.4
     */
    NEGATIVE("消极");

    private final String description;

    SentimentType(String description) {
        this.description = description;
    }
}
