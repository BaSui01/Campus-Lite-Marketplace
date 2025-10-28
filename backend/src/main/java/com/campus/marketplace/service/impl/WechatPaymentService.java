package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.config.WechatPayConfig;
import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 微信支付V3服务实现类 💰
 *
 * 使用微信支付官方Java SDK实现APIv3支付功能
 *
 * <p>主要功能：
 * <ul>
 *   <li>创建支付订单（Native扫码支付）</li>
 *   <li>验证支付回调签名</li>
 *   <li>处理异步通知</li>
 *   <li>查询支付订单状态</li>
 * </ul>
 * </p>
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile("prod")
@ConditionalOnProperty(name = "wechat.pay.version", havingValue = "v3", matchIfMissing = true)
public class WechatPaymentService {

    private final NativePayService nativePayService;
    private final Config wechatPayV3Config;
    private final WechatPayConfig wechatPayConfig;
    private final ObjectMapper objectMapper;

    /**
     * 支付超时时间（分钟）
     */
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

    /**
     * 创建微信支付订单 🚀
     *
     * 使用Native扫码支付（适合PC网站）
     *
     * @param order 订单信息
     * @return 支付响应（包含支付二维码URL）
     */
    public PaymentResponse createPayment(Order order) {
        log.info("🎯 创建微信支付V3订单: orderNo={}, amount={}",
                order.getOrderNo(), order.getActualAmount());

        try {
            // 1. 构建订单金额
            Amount amount = new Amount();
            amount.setTotal(convertToFen(order.getActualAmount())); // 订单金额（分）
            amount.setCurrency("CNY"); // 货币类型

            // 2. 构建预支付请求
            PrepayRequest request = new PrepayRequest();
            request.setAppid(wechatPayConfig.getAppId()); // 应用ID
            request.setMchid(wechatPayConfig.getMchId()); // 商户号
            request.setDescription("校园轻享集市 - 订单支付"); // 商品描述
            request.setOutTradeNo(order.getOrderNo()); // 商户订单号
            request.setNotifyUrl(wechatPayConfig.getNotifyUrl()); // 异步通知地址
            request.setAmount(amount); // 订单金额
            request.setTimeExpire(calculateExpireTime()); // 支付超时时间（RFC 3339格式）

            // 3. 调用Native支付下单API
            PrepayResponse response = nativePayService.prepay(request);

            log.info("✅ 微信支付V3订单创建成功: orderNo={}, codeUrl={}",
                    order.getOrderNo(), response.getCodeUrl());

            // 4. 返回支付二维码URL
            return PaymentResponse.builder()
                    .orderNo(order.getOrderNo())
                    .paymentUrl(response.getCodeUrl()) // 二维码URL
                    .qrCode(response.getCodeUrl()) // 二维码内容（前端扫码用）
                    .expireSeconds(PAYMENT_TIMEOUT_MINUTES * 60)
                    .build();

        } catch (ServiceException e) {
            log.error("💥 微信支付V3 API调用异常: orderNo={}, httpStatusCode={}, errorCode={}, errorMessage={}",
                    order.getOrderNo(), e.getHttpStatusCode(), e.getErrorCode(), e.getErrorMessage(), e);

            throw new BusinessException(ErrorCode.PAYMENT_CREATE_FAILED,
                    "微信支付创建失败：" + e.getErrorMessage());
        } catch (Exception e) {
            log.error("💥 微信支付创建异常: orderNo={}", order.getOrderNo(), e);
            throw new BusinessException(ErrorCode.PAYMENT_CREATE_FAILED, "微信支付创建失败");
        }
    }

    /**
     * 处理支付成功回调 🎉
     *
     * 解析微信支付V3异步通知，验证签名并返回订单信息
     *
     * @param requestParam 回调请求参数
     * @return 包含订单号和交易流水号的数组 [orderNo, transactionId]
     */
    public String[] handleNotify(RequestParam requestParam) {
        try {
            // 1. 初始化通知解析器（使用 Config 对象）
            NotificationParser parser = new NotificationParser((NotificationConfig) wechatPayV3Config);

            // 3. 解析并验证签名、解密通知内容
            Transaction transaction = parser.parse(requestParam, Transaction.class);

            // 4. 获取订单信息
            String outTradeNo = transaction.getOutTradeNo(); // 商户订单号
            String transactionId = transaction.getTransactionId(); // 微信支付订单号
            Transaction.TradeStateEnum tradeState = transaction.getTradeState(); // 交易状态
            com.wechat.pay.java.service.payments.model.TransactionAmount amount = transaction.getAmount(); // 订单金额

            log.info("📥 微信支付V3异步通知: orderNo={}, transactionId={}, tradeState={}, amount={}",
                    outTradeNo, transactionId, tradeState, amount != null ? amount.getTotal() : null);

            // 5. 判断支付结果
            if (Transaction.TradeStateEnum.SUCCESS.equals(tradeState)) {
                log.info("✅ 微信支付成功: orderNo={}, transactionId={}", outTradeNo, transactionId);
                return new String[]{outTradeNo, transactionId};
            } else {
                log.warn("⚠️ 微信支付状态异常: orderNo={}, tradeState={}", outTradeNo, tradeState);
                return null;
            }

        } catch (Exception e) {
            log.error("💥 微信支付V3回调处理异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 查询支付订单状态 🔍
     *
     * @param orderNo 商户订单号
     * @return 交易状态
     */
    public String queryOrderStatus(String orderNo) {
        try {
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(wechatPayConfig.getMchId());
            request.setOutTradeNo(orderNo);

            Transaction transaction = nativePayService.queryOrderByOutTradeNo(request);
            
            log.info("🔍 查询订单状态: orderNo={}, tradeState={}", 
                    orderNo, transaction.getTradeState());
            
            return transaction.getTradeState().name();

        } catch (ServiceException e) {
            log.error("💥 查询订单状态异常: orderNo={}, errorCode={}, errorMessage={}",
                    orderNo, e.getErrorCode(), e.getErrorMessage());
            throw new BusinessException(ErrorCode.PAYMENT_QUERY_FAILED, "查询支付状态失败");
        }
    }

    /**
     * 构建回调请求参数 🔧
     *
     * @param signature 微信支付签名
     * @param serial 微信支付平台证书序列号
     * @param nonce 随机字符串
     * @param timestamp 时间戳
     * @param body 请求体（JSON）
     * @return RequestParam
     */
    public RequestParam buildRequestParam(String signature, String serial, 
                                          String nonce, String timestamp, String body) {
        return new RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonce)
                .signature(signature)
                .timestamp(timestamp)
                .body(body)
                .build();
    }

    /**
     * 生成微信支付成功响应 ✅
     *
     * @return JSON格式的成功响应
     */
    public String buildSuccessResponse() {
        try {
            return objectMapper.writeValueAsString(Map.of("code", "SUCCESS", "message", "成功"));
        } catch (JsonProcessingException e) {
            log.error("微信支付成功响应序列化异常", e);
            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        }
    }

    /**
     * 生成微信支付失败响应 ❌
     *
     * @param errorMsg 失败原因
     * @return JSON格式的失败响应
     */
    public String buildFailResponse(String errorMsg) {
        try {
            return objectMapper.writeValueAsString(Map.of("code", "FAIL", "message", errorMsg));
        } catch (JsonProcessingException e) {
            log.error("微信支付失败响应序列化异常: {}", errorMsg, e);
            return "{\"code\":\"FAIL\",\"message\":\"系统异常\"}";
        }
    }

    /**
     * 将元转换为分 💴
     *
     * 微信支付金额单位为分
     *
     * @param amount 金额（元）
     * @return 金额（分）
     */
    private Integer convertToFen(BigDecimal amount) {
        if (amount == null) {
            throw new BusinessException(ErrorCode.PAYMENT_CREATE_FAILED, "订单金额不能为空");
        }

        try {
            BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
            BigDecimal fen = normalized.movePointRight(2);

            if (fen.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ErrorCode.PAYMENT_CREATE_FAILED, "订单金额不能为负数");
            }

            return fen.intValueExact();
        } catch (ArithmeticException ex) {
            log.error("订单金额换算异常: amount={}", amount, ex);
            throw new BusinessException(ErrorCode.PAYMENT_CREATE_FAILED, "订单金额超出支持范围");
        }
    }

    /**
     * 计算支付超时时间 ⏰
     *
     * V3格式：RFC 3339格式（如：2025-01-01T12:00:00+08:00）
     *
     * @return 超时时间字符串
     */
    private String calculateExpireTime() {
        java.time.ZonedDateTime expireTime = java.time.ZonedDateTime.now()
                .plusMinutes(PAYMENT_TIMEOUT_MINUTES);

        return expireTime.format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * 发起退款（V3）
     * 注意：真实生产需持有 RefundService 并根据支付单金额/退款单号调用。
     * 这里以最小实现对接统一门面，实际渠道细节由专用服务维护。
     */
    public boolean refund(Order order, BigDecimal amount) {
        try {
            // 由于本服务侧未持有 RefundService（SDK），此处仅做最小可用占位，记录审计并返回失败，避免静默通过。
            // 集成建议：注入 com.wechat.pay.java.service.refund.RefundService 并调用其 create() 完成退款申请。
            log.warn("微信退款尚未接入 RefundService SDK，请集成后完成退款: orderNo={}, amount={}", order.getOrderNo(), amount);
            return false;
        } catch (Exception e) {
            log.error("微信退款异常: orderNo={}", order.getOrderNo(), e);
            return false;
        }
    }
}
