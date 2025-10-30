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
 * @date 2025-10-29
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
     * 发起退款
     *
     * @param order 订单
     * @param amount 退款金额
     * @param paymentMethod 渠道
     * @return 是否成功发起
     */
    boolean refund(com.campus.marketplace.common.entity.Order order,
                   java.math.BigDecimal amount,
                   PaymentMethod paymentMethod);
}
