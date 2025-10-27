package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.PaymentMethod;

/**
 * 支付服务接口
 * 
 * 提供统一的支付接口，支持多种支付方式
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public interface PaymentService {

    /**
     * 创建支付订单
     * 
     * @param order 订单信息
     * @param paymentMethod 支付方式
     * @return 支付响应
     */
    PaymentResponse createPayment(Order order, PaymentMethod paymentMethod);

    /**
     * 验证支付回调签名
     * 
     * @param orderNo 订单号
     * @param transactionId 支付流水号
     * @param signature 签名
     * @return 是否验证通过
     */
    boolean verifySignature(String orderNo, String transactionId, String signature);
}
