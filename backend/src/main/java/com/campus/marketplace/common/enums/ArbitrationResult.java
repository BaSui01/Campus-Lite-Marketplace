package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 仲裁结果枚举
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
public enum ArbitrationResult {

    FULL_REFUND("全额退款"),
    PARTIAL_REFUND("部分退款"),
    REJECT("驳回申请"),
    NEED_MORE_EVIDENCE("需补充证据");

    private final String description;

    ArbitrationResult(String description) {
        this.description = description;
    }
}
