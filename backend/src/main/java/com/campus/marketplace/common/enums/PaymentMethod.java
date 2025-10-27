package com.campus.marketplace.common.enums;

/**
 * 支付方式枚举
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public enum PaymentMethod {
    
    /**
     * 微信支付
     */
    WECHAT("微信支付"),
    
    /**
     * 支付宝支付
     */
    ALIPAY("支付宝支付");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
