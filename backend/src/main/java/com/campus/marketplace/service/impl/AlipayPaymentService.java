package com.campus.marketplace.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.campus.marketplace.common.config.AlipayConfig;
import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * 支付宝支付服务实现类 💳
 *
 * 使用支付宝SDK实现支付功能（沙箱环境）
 *
 * <p>主要功能：
 * <ul>
 *   <li>创建支付订单（电脑网站支付）</li>
 *   <li>验证支付回调签名</li>
 *   <li>处理异步通知</li>
 * </ul>
 * </p>
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayPaymentService {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;

    /**
     * 支付超时时间（分钟）
     */
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

    /**
     * 创建支付宝支付订单 🚀
     *
     * 使用电脑网站支付（alipay.trade.page.pay）
     *
     * @param order 订单信息
     * @return 支付响应（包含支付表单HTML）
     */
    public PaymentResponse createPayment(Order order) {
        log.info("🎯 创建支付宝支付订单: orderNo={}, amount={}",
                order.getOrderNo(), order.getActualAmount());

        try {
            // 1. 创建支付请求对象
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

            // 2. 设置异步通知地址
            request.setNotifyUrl(alipayConfig.getNotifyUrl());

            // 3. 设置同步跳转地址
            request.setReturnUrl(alipayConfig.getReturnUrl());

            // 4. 设置业务参数
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(order.getOrderNo()); // 商户订单号
            model.setTotalAmount(order.getActualAmount().toString()); // 订单金额（元）
            model.setSubject("校园轻享集市 - 订单支付"); // 订单标题
            model.setBody("订单号：" + order.getOrderNo()); // 订单描述
            model.setProductCode("FAST_INSTANT_TRADE_PAY"); // 产品码（电脑网站支付固定值）
            model.setTimeoutExpress(PAYMENT_TIMEOUT_MINUTES + "m"); // 支付超时时间

            request.setBizModel(model);

            // 5. 调用SDK生成支付表单
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);

            if (response.isSuccess()) {
                log.info("✅ 支付宝支付订单创建成功: orderNo={}", order.getOrderNo());

                // 返回支付表单HTML（前端直接渲染即可跳转到支付宝）
                return PaymentResponse.builder()
                        .orderNo(order.getOrderNo())
                        .paymentUrl(response.getBody()) // 支付表单HTML
                        .qrCode(null) // 电脑网站支付不需要二维码
                        .expireSeconds(PAYMENT_TIMEOUT_MINUTES * 60)
                        .build();
            } else {
                log.error("❌ 支付宝支付订单创建失败: code={}, msg={}, subCode={}, subMsg={}",
                        response.getCode(), response.getMsg(),
                        response.getSubCode(), response.getSubMsg());

                throw new BusinessException(ErrorCode.PAYMENT_CREATE_FAILED,
                        "支付宝支付创建失败：" + response.getSubMsg());
            }

        } catch (AlipayApiException e) {
            log.error("💥 调用支付宝API异常: orderNo={}", order.getOrderNo(), e);
            throw new BusinessException(ErrorCode.PAYMENT_CREATE_FAILED,
                    "支付宝API调用异常：" + e.getMessage());
        }
    }

    /**
     * 验证支付回调签名 🔐
     *
     * 使用支付宝公钥验证回调数据的签名，确保数据未被篡改
     *
     * @param params 支付宝回调参数（Map格式）
     * @return 验证是否通过
     */
    public boolean verifySignature(java.util.Map<String, String> params) {
        try {
            // 使用支付宝SDK验证签名
            boolean isValid = com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            log.info("🔍 支付宝签名验证结果: {}", isValid ? "✅ 通过" : "❌ 失败");
            return isValid;

        } catch (AlipayApiException e) {
            log.error("💥 支付宝签名验证异常", e);
            return false;
        }
    }

    /**
     * 处理支付成功回调 🎉
     *
     * 解析支付宝异步通知参数，返回订单号和交易流水号
     *
     * @param params 支付宝回调参数
     * @return 包含订单号和交易流水号的数组 [orderNo, transactionId]
     */
    public String[] handleNotify(java.util.Map<String, String> params) {
        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no"); // 商户订单号
        String tradeNo = params.get("trade_no"); // 支付宝交易号
        String totalAmount = params.get("total_amount"); // 交易金额

        log.info("📥 支付宝异步通知: orderNo={}, tradeNo={}, status={}, amount={}",
                outTradeNo, tradeNo, tradeStatus, totalAmount);

        // 判断支付状态
        // TRADE_SUCCESS: 交易支付成功
        // TRADE_FINISHED: 交易结束，不可退款
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            log.info("✅ 支付宝支付成功: orderNo={}, tradeNo={}", outTradeNo, tradeNo);
            return new String[]{outTradeNo, tradeNo};
        } else {
            log.warn("⚠️ 支付宝支付状态异常: orderNo={}, status={}", outTradeNo, tradeStatus);
            return null;
        }
    }

    /**
     * 发起支付宝退款
     */
    public boolean refund(Order order, java.math.BigDecimal amount) {
        try {
            com.alipay.api.request.AlipayTradeRefundRequest request = new com.alipay.api.request.AlipayTradeRefundRequest();
            com.alipay.api.domain.AlipayTradeRefundModel model = new com.alipay.api.domain.AlipayTradeRefundModel();
            model.setOutTradeNo(order.getOrderNo());
            model.setRefundAmount(amount.toPlainString());
            model.setRefundReason("Campus Marketplace Refund");
            request.setBizModel(model);

            com.alipay.api.response.AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                log.info("✅ 支付宝退款成功: orderNo={}, amount={}", order.getOrderNo(), amount);
                return true;
            } else {
                log.error("❌ 支付宝退款失败: code={}, subMsg={}", response.getCode(), response.getSubMsg());
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("💥 支付宝退款异常: orderNo={}", order.getOrderNo(), e);
            return false;
        }
    }
}
