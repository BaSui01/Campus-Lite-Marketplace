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
    private final WechatPaymentService wechatPaymentService;

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
                log.info("💰 使用微信支付");
                yield wechatPaymentService.createPayment(order);
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
     * 简化实现：由具体的支付回调Controller调用对应服务的验签方法
     * 这里暂时返回true（实际使用时应该抛出异常，提示使用具体服务）
     */
    @Override
    public boolean verifySignature(String orderNo, String transactionId, String signature) {
        log.warn("⚠️ verifySignature方法已废弃，请使用具体支付服务的验签方法");
        return true;
    }

    @Override
    public boolean refund(com.campus.marketplace.common.entity.Order order,
                          java.math.BigDecimal amount,
                          PaymentMethod paymentMethod) {
        log.info("发起退款: orderNo={}, method={}, amount={}", order.getOrderNo(), paymentMethod, amount);
        // 简化：暂未接入具体SDK退款，此处仅进行路由与占位
        return switch (paymentMethod) {
            case ALIPAY -> {
                boolean ok = alipayPaymentService.refund(order, amount);
                yield ok;
            }
            case WECHAT -> {
                // TODO: 集成 微信支付V3退款接口
                log.warn("微信退款SDK未集成，返回失败占位");
                yield false;
            }
            default -> false;
        };
    }
}
