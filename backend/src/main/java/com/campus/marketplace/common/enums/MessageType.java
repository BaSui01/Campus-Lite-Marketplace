package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Getter
public enum MessageType {

    /**
     * 文本消息
     */
    TEXT("文本"),

    /**
     * 图片消息
     */
    IMAGE("图片"),

    /**
     * 商品卡片
     */
    GOODS_CARD("商品卡片"),

    /**
     * 系统消息
     */
    SYSTEM("系统消息");

    /**
     * 类型描述
     */
    private final String description;

    MessageType(String description) {
        this.description = description;
    }
}
