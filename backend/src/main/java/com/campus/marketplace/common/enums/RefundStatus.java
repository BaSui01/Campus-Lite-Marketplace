package com.campus.marketplace.common.enums;
/**
 * Refund Status
 *
 * @author BaSui
 * @date 2025-10-29
 */


public enum RefundStatus {
    APPLIED,       // 已申请
    APPROVED,      // 已审核通过，待渠道退款
    REJECTED,      // 审核拒绝
    PROCESSING,    // 渠道退款中
    REFUNDED,      // 退款成功
    FAILED         // 退款失败
}
