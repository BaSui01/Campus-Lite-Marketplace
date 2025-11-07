package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 优惠券状态枚举
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Getter
public enum CouponStatus {
    
    AVAILABLE("可用"),
    USED("已使用"),
    EXPIRED("已过期");
    
    private final String description;

    CouponStatus(String description) {
        this.description = description;
    }
}
