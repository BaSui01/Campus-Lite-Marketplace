package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 批量操作类型枚举
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
public enum BatchType {
    
    /**
     * 商品批量操作
     */
    GOODS_BATCH("商品批量操作"),
    
    /**
     * 价格批量调整
     */
    PRICE_BATCH("价格批量调整"),
    
    /**
     * 库存批量更新
     */
    INVENTORY_BATCH("库存批量更新"),
    
    /**
     * 用户通知批量发送
     */
    NOTIFICATION_BATCH("用户通知批量发送");

    private final String description;

    BatchType(String description) {
        this.description = description;
    }
}
