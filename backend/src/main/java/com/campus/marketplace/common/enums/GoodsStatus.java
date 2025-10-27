package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 物品状态枚举
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Getter
public enum GoodsStatus {
    
    /**
     * 待审核
     */
    PENDING("待审核"),
    
    /**
     * 审核通过
     */
    APPROVED("审核通过"),
    
    /**
     * 审核拒绝
     */
    REJECTED("审核拒绝"),
    
    /**
     * 已售出
     */
    SOLD("已售出"),
    
    /**
     * 已下架
     */
    OFFLINE("已下架");

    private final String description;

    GoodsStatus(String description) {
        this.description = description;
    }
}
