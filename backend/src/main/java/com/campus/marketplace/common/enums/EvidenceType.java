package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 证据类型枚举
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@RequiredArgsConstructor
public enum EvidenceType {

    IMAGE("图片"),
    VIDEO("视频"),
    CHAT_RECORD("聊天记录截图");

    private final String description;
}
