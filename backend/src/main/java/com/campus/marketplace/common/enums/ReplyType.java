package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 回复类型枚举
 *
 * 用于评价回复的身份标识
 *
 * @author BaSui 😎 - 卖家回复还是管理员回复？看这里！
 * @since 2025-11-03
 */
@Getter
public enum ReplyType {

    /**
     * 卖家回复
     * 卖家对买家评价的回复
     */
    SELLER_REPLY("卖家回复"),

    /**
     * 管理员回复
     * 管理员对违规评价的处理说明
     */
    ADMIN_REPLY("管理员回复");

    private final String description;

    ReplyType(String description) {
        this.description = description;
    }
}
