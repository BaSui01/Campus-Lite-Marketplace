package com.campus.marketplace.common.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 退款响应DTO（增强版）
 * 
 * 包含关联的商品、买家、卖家信息
 * 
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Builder
public record RefundResponseDTO(
    // 基础退款信息
    Long id,
    String refundNo,
    String orderNo,
    BigDecimal amount,
    String reason,
    Map<String, Object> evidence,
    String status,
    String channel,
    Integer retryCount,
    String lastError,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    
    // 关联商品信息
    Long goodsId,
    String goodsTitle,
    String goodsImage,
    
    // 关联买家信息
    Long buyerId,
    String buyerUsername,
    String buyerAvatar,
    
    // 关联卖家信息
    Long sellerId,
    String sellerUsername,
    String sellerAvatar
) {}
