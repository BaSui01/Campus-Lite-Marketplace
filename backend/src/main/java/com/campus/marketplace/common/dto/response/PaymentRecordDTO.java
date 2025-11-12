package com.campus.marketplace.common.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录DTO
 * 
 * 用于管理员查询支付记录列表
 * 
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Builder
public record PaymentRecordDTO(
    Long id,
    String orderNo,
    String transactionId,
    BigDecimal amount,
    String paymentMethod,
    String status,
    LocalDateTime paidAt,
    LocalDateTime createdAt,
    // 关联订单信息
    String goodsTitle,
    String buyerUsername,
    String sellerUsername
) {}
