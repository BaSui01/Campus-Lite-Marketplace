package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.config.WechatPayV2Config;
import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.wechat.pay.java.core.notification.RequestParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 微信支付V2服务实现类（沙箱环境） 💰
 *
 * 仅在配置 wechat.pay.version=v2 时启用
 * 使用WxJava SDK实现V2版本的支付功能（支持沙箱环境）
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "wechat.pay.version", havingValue = "v2")
public class WechatPaymentServiceV2 {

    private final WxPayService wxPayService;
    private final WechatPayV2Config wechatPayV2Config;

    /**
     * 支付超时时间（分钟）
     */
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

    /**
     * 创建微信支付订单（V2沙箱） 🚀
     *
     * @param order 订单信息
     * @return 支付响应（包含支付二维码URL）
     */
    public PaymentResponse createPayment(Order order) {
        log.info("🎯 创建微信支付V2订单（沙箱）: orderNo={}, amount={}",
                order.getOrderNo(), order.getActualAmount());

        try {
            // 1. 构建统一下单请求
            WxPayUnifiedOrderRequest request = WxPayUnifiedOrderRequest.newBuilder()
                    .outTradeNo(order.getOrderNo()) // 商户订单号
                    .body("校园轻享集市 - 订单支付") // 商品描述
                    .totalFee(convertToFen(order.getActualAmount())) // 订单金额（分）
                    .tradeType(WxPayConstants.TradeType.NATIVE) // Native扫码支付
                    .notifyUrl(wechatPayV2Config.getNotifyUrl()) // 异步通知地址
                    .timeExpire(calculateExpireTime()) // 支付超时时间
                    .build();

            // 2. 调用统一下单API
            WxPayUnifiedOrderResult result = wxPayService.unifiedOrder(request);

            log.info("✅ 微信支付V2订单创建成功（沙箱）: orderNo={}, prepayId={}",
                    order.getOrderNo(), result.getPrepayId());

            // 3. 返回支付二维码URL
            return PaymentResponse.builder()
                    .orderNo(order.getOrderNo())
                    .paymentUrl(result.getCodeURL()) // 二维码URL
                    .qrCode(result.getCodeURL()) // 二维码内容
                    .expireSeconds(PAYMENT_TIMEOUT_MINUTES * 60)
                    .build();

        } catch (WxPayException e) {
            log.error("💥 微信支付V2 API调用异常: orderNo={}, errCode={}, errCodeDes={}",
                    order.getOrderNo(), e.getErrCode(), e.getErrCodeDes(), e);

            throw new BusinessException(ErrorCode.PAYMENT_CREATE_FAILED,
                    "微信支付创建失败：" + e.getErrCodeDes());
        }
    }

    /**
     * 处理支付成功回调（V2） 🎉
     *
     * @param xmlData 微信支付回调的XML数据
     * @return 包含订单号和交易流水号的数组 [orderNo, transactionId]
     */
    public String[] handleNotify(String xmlData) {
        try {
            // 1. 解析并验证回调数据（SDK会自动验签）
            WxPayOrderNotifyResult result = wxPayService.parseOrderNotifyResult(xmlData);

            // 2. 获取订单信息
            String outTradeNo = result.getOutTradeNo(); // 商户订单号
            String transactionId = result.getTransactionId(); // 微信支付订单号
            String resultCode = result.getResultCode(); // 业务结果
            Integer totalFee = result.getTotalFee(); // 订单金额（分）

            log.info("📥 微信支付V2异步通知（沙箱）: orderNo={}, transactionId={}, resultCode={}, totalFee={}",
                    outTradeNo, transactionId, resultCode, totalFee);

            // 3. 判断支付结果
            if (WxPayConstants.ResultCode.SUCCESS.equals(resultCode)) {
                log.info("✅ 微信支付V2成功（沙箱）: orderNo={}, transactionId={}", outTradeNo, transactionId);
                return new String[]{outTradeNo, transactionId};
            } else {
                log.warn("⚠️ 微信支付V2失败（沙箱）: orderNo={}, resultCode={}", outTradeNo, resultCode);
                return null;
            }

        } catch (WxPayException e) {
            log.error("💥 微信支付V2回调处理异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 构建V2回调请求参数（用于统一接口）
     * V2使用XML，这里做适配
     */
    public String[] handleNotify(RequestParam requestParam) {
        // V2不支持V3的RequestParam，抛出提示
        log.warn("⚠️ V2沙箱不支持V3的RequestParam，请使用XML格式");
        return null;
    }

    /**
     * 查询订单状态（V2暂不实现，沙箱用）
     */
    public String queryOrderStatus(String orderNo) {
        log.info("🔍 V2沙箱暂不支持订单查询，返回UNKNOWN");
        return "UNKNOWN";
    }

    /**
     * 生成微信支付成功响应（V2） ✅
     */
    public String buildSuccessResponse() {
        return WxPayNotifyResponse.success("OK");
    }

    /**
     * 生成微信支付失败响应（V2） ❌
     */
    public String buildFailResponse(String errorMsg) {
        return WxPayNotifyResponse.fail(errorMsg);
    }

    /**
     * 将元转换为分 💴
     */
    private Integer convertToFen(BigDecimal amount) {
        return amount.multiply(new BigDecimal("100")).intValue();
    }

    /**
     * 计算支付超时时间 ⏰
     * V2格式：yyyyMMddHHmmss
     */
    private String calculateExpireTime() {
        java.time.LocalDateTime expireTime = java.time.LocalDateTime.now()
                .plusMinutes(PAYMENT_TIMEOUT_MINUTES);

        return expireTime.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        );
    }
}
