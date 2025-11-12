package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 纠纷类型枚举
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
public enum DisputeType {

    GOODS_MISMATCH("商品不符"),
    QUALITY_ISSUE("质量问题"),
    LOGISTICS_DELAY("物流延误"),
    FALSE_ADVERTISING("虚假宣传"),
    OTHER("其他");

    private final String description;

    DisputeType(String description) {
        this.description = description;
    }
}
