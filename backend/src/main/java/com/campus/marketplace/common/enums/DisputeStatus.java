package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 纠纷状态枚举
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@RequiredArgsConstructor
public enum DisputeStatus {

    SUBMITTED("已提交"),
    NEGOTIATING("协商中"),
    PENDING_ARBITRATION("待仲裁"),
    ARBITRATING("仲裁中"),
    COMPLETED("已完成"),
    CLOSED("已关闭");

    private final String description;
}
