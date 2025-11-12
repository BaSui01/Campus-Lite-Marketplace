package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 纠纷角色枚举
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
public enum DisputeRole {

    BUYER("买家"),
    SELLER("卖家");

    private final String description;

    DisputeRole(String description) {
        this.description = description;
    }
}
