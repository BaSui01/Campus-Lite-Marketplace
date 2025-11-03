package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Getter
public enum OrderStatus {
    
    /**
     * 待支付
     */
    PENDING_PAYMENT("待支付"),
    
    /**
     * 已支付
     */
    PAID("已支付"),
    
    /**
     * 已发货
     */
    SHIPPED("已发货"),
    
    /**
     * 已送达（待确认收货）
     */
    DELIVERED("已送达"),
    
    /**
     * 已完成
     */
    COMPLETED("已完成"),
    
    /**
     * 已取消
     */
    CANCELLED("已取消"),
    
    /**
     * 已评价
     */
    REVIEWED("已评价");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
