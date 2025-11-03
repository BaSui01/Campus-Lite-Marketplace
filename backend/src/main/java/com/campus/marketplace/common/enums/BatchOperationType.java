package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 批量操作类型枚举
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
public enum BatchOperationType {
    
    /**
     * 批量上架
     */
    BATCH_ONLINE("批量上架"),
    
    /**
     * 批量下架
     */
    BATCH_OFFLINE("批量下架"),
    
    /**
     * 批量删除
     */
    BATCH_DELETE("批量删除"),
    
    /**
     * 批量调价
     */
    BATCH_PRICE_UPDATE("批量调价"),
    
    /**
     * 批量更新库存
     */
    BATCH_INVENTORY_UPDATE("批量更新库存");

    private final String description;

    BatchOperationType(String description) {
        this.description = description;
    }
}
