package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.PaymentMethod;
import com.campus.marketplace.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 支付服务实现类（沙箱模式）
 * 
 * 模拟支付流程，不调用真实的支付接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    /**
     * 支付超时时间（秒）- 30分钟
     */
    private static final int PAYMENT_EXPIRE_SECONDS = 1800;

    /**
     * 创建支付订单（沙箱模式）
     * 
     * 返回模拟的支付链接，实际项目中应调用真实的支付接口
     */
    @Override
    public PaymentResponse createPayment(Order order, PaymentMethod paymentMethod) {
        log.info("创建支付订单（沙箱模式）: orderNo={}, paymentMethod={}, amount={}",
                order.getOrderNo(), paymentMethod, order.getActualAmount());

        // 生成模拟支付链接
        String paymentUrl = generateMockPaymentUrl(order, paymentMethod);
        String qrCode = generateMockQrCode(order, paymentMethod);

        log.info("支付订单创建成功: orderNo={}, paymentUrl={}", order.getOrderNo(), paymentUrl);

        return PaymentResponse.builder()
                .orderNo(order.getOrderNo())
                .paymentUrl(paymentUrl)
                .qrCode(qrCode)
                .expireSeconds(PAYMENT_EXPIRE_SECONDS)
                .build();
    }

    /**
     * 验证支付回调签名（沙箱模式）
     * 
     * 简化验证逻辑，实际项目中应使用真实的签名算法
     */
    @Override
    public boolean verifySignature(String orderNo, String transactionId, String signature) {
        log.info("验证支付签名（沙箱模式）: orderNo={}, transactionId={}", orderNo, transactionId);

        // 沙箱模式：只要签名不为空就认为验证通过
        boolean isValid = signature != null && !signature.isEmpty();

        log.info("签名验证结果: {}", isValid ? "通过" : "失败");
        return isValid;
    }

    /**
     * 生成模拟支付链接
     */
    private String generateMockPaymentUrl(Order order, PaymentMethod paymentMethod) {
        String baseUrl = switch (paymentMethod) {
            case WECHAT -> "https://sandbox.wechat.com/pay";
            case ALIPAY -> "https://sandbox.alipay.com/pay";
        };

        return String.format("%s?orderNo=%s&amount=%s",
                baseUrl, order.getOrderNo(), order.getActualAmount());
    }

    /**
     * 生成模拟二维码
     */
    private String generateMockQrCode(Order order, PaymentMethod paymentMethod) {
        return String.format("data:image/png;base64,MOCK_QR_CODE_%s_%s",
                paymentMethod.name(), order.getOrderNo());
    }
}
