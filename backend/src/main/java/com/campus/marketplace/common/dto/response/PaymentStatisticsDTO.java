package com.campus.marketplace.common.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付统计DTO
 * 
 * 管理员查看支付统计数据
 * 
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Builder
public record PaymentStatisticsDTO(
    // 总支付金额
    BigDecimal totalAmount,
    // 总支付次数
    Long totalPayments,
    // 成功支付次数
    Long successPayments,
    // 失败支付次数
    Long failedPayments,
    // 已退款次数
    Long refundedPayments,
    // 按支付方式统计（WECHAT/ALIPAY -> 金额）
    Map<String, BigDecimal> amountByMethod,
    // 按支付方式统计（WECHAT/ALIPAY -> 次数）
    Map<String, Long> countByMethod,
    // 平均支付金额
    BigDecimal averageAmount
) {}
