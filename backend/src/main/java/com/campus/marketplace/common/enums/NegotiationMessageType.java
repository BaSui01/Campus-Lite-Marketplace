package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 协商消息类型枚举
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@RequiredArgsConstructor
public enum NegotiationMessageType {

    TEXT("文字消息"),
    PROPOSAL("解决方案");

    private final String description;
}
