package com.campus.marketplace.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 物流状态枚举
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@RequiredArgsConstructor
public enum LogisticsStatus {

    /**
     * 待发货
     */
    PENDING("待发货"),

    /**
     * 已揽件
     */
    PICKED_UP("已揽件"),

    /**
     * 运输中
     */
    IN_TRANSIT("运输中"),

    /**
     * 派送中
     */
    DELIVERING("派送中"),

    /**
     * 已签收
     */
    DELIVERED("已签收"),

    /**
     * 已拒签
     */
    REJECTED("已拒签"),

    /**
     * 疑似丢失
     */
    LOST("疑似丢失");

    private final String description;
}
