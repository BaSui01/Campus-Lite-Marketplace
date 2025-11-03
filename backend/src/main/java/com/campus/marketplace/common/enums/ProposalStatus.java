package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 方案状态枚举
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@RequiredArgsConstructor
public enum ProposalStatus {

    PENDING("待响应"),
    ACCEPTED("已接受"),
    REJECTED("已拒绝");

    private final String description;
}
