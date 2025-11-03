package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 纠纷角色枚举
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@RequiredArgsConstructor
public enum DisputeRole {

    BUYER("买家"),
    SELLER("卖家");

    private final String description;
}
