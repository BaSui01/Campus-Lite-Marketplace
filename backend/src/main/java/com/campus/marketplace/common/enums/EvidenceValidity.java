package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 证据有效性枚举
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@RequiredArgsConstructor
public enum EvidenceValidity {

    VALID("有效"),
    INVALID("无效"),
    DOUBTFUL("存疑");

    private final String description;
}
