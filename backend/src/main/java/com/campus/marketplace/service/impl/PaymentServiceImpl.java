package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.PaymentMethod;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付服务实现类（门面模式） 💳
 *
 * 根据支付方式路由到对应的支付服务
 * - 支付宝：AlipayPaymentService
 * - 微信支付：WechatPaymentService
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final AlipayPaymentService alipayPaymentService;
    private final org.springframework.beans.factory.ObjectProvider<WechatPaymentService> wechatPaymentServiceProvider;

    /**
     * 创建支付订单 🚀
     *
     * 根据支付方式调用对应的支付服务
     *
     * @param order 订单信息
     * @param paymentMethod 支付方式（ALIPAY/WECHAT）
     * @return 支付响应
     */
    @Override
    public PaymentResponse createPayment(Order order, PaymentMethod paymentMethod) {
        log.info("🎯 创建支付订单: orderNo={}, paymentMethod={}, amount={}",
                order.getOrderNo(), paymentMethod, order.getActualAmount());

        return switch (paymentMethod) {
            case ALIPAY -> {
                log.info("💳 使用支付宝支付");
                yield alipayPaymentService.createPayment(order);
            }
            case WECHAT -> {
                var wechat = wechatPaymentServiceProvider.getIfAvailable();
                if (wechat == null) {
                    log.warn("微信支付未启用，拒绝创建订单: orderNo={}", order.getOrderNo());
                    throw new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED, "微信支付未启用");
                }
                log.info("💰 使用微信支付");
                yield wechat.createPayment(order);
            }
            default -> {
                log.error("❌ 不支持的支付方式: {}", paymentMethod);
                throw new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED, "不支持的支付方式");
            }
        };
    }

    /**
     * 验证支付回调签名 🔐
     *
     * ✅ 说明：签名验证由具体的支付服务（WechatPaymentService/AlipayPaymentService）完成
     * 此方法仅用于兼容旧代码，实际回调处理中应直接调用具体服务的验签方法
     *
     * @deprecated 请使用具体支付服务的验签方法
     */
    @Override
    @Deprecated
    public boolean verifySignature(String orderNo, String transactionId, String signature) {
        log.warn("⚠️ verifySignature方法已废弃，请使用具体支付服务的验签方法");
        // 返回 true 仅为兼容旧代码，新代码应使用具体服务的验签方法
        return true;
    }

    /**
     * 发起退款 💰
     *
     * ✅ 真实实现：根据支付方式路由到对应的退款服务
     * - 支付宝：调用 AlipayPaymentService.refund()
     * - 微信支付：调用 WechatPaymentService.refund()
     */
    @Override
    public boolean refund(com.campus.marketplace.common.entity.Order order,
                          java.math.BigDecimal amount,
                          PaymentMethod paymentMethod) {
        log.info("🚀 发起退款: orderNo={}, method={}, amount={}", order.getOrderNo(), paymentMethod, amount);

        return switch (paymentMethod) {
            case ALIPAY -> {
                log.info("💳 使用支付宝退款");
                yield alipayPaymentService.refund(order, amount);
            }
            case WECHAT -> {
                var wechat = wechatPaymentServiceProvider.getIfAvailable();
                if (wechat == null) {
                    log.error("❌ 微信支付未启用，拒绝退款: orderNo={}", order.getOrderNo());
                    yield false;
                }
                log.info("💰 使用微信支付退款");
                yield wechat.refund(order, amount);
            }
            default -> {
                log.error("❌ 不支持的支付方式: {}", paymentMethod);
                yield false;
            }
        };
    }
}
