package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 优惠券类型枚举
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Getter
public enum CouponType {
    
    FIXED("满减券"),      // 满100减10
    PERCENT("折扣券"),    // 9折
    FREE_SHIPPING("包邮券");
    
    private final String description;

    CouponType(String description) {
        this.description = description;
    }
}
