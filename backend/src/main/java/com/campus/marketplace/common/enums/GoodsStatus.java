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
     * 已锁定（待支付占用）
     *
     * 下单后在支付完成前的过渡状态；
     * - 列表不可见；
     * - 详情仅买家/卖家可见；
     * - 超时或取消回退到 APPROVED；
     * - 支付成功流转到 SOLD。
     */
    LOCKED("已锁定"),
    
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
