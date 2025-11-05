package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.PayOrderRequest;
import com.campus.marketplace.common.dto.request.PaymentCallbackRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;

import java.math.BigDecimal;
import com.campus.marketplace.service.OrderService;
import com.campus.marketplace.service.impl.WechatPaymentService;
import com.campus.marketplace.service.impl.WechatPaymentServiceV2;
import com.campus.marketplace.service.impl.AlipayPaymentService;
import com.campus.marketplace.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.wechat.pay.java.core.notification.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.stream.Collectors;

/**
 * 支付控制器 💳
 *
 * 提供支付创建和回调通知处理接口
 * 支持微信支付V2（沙箱）和V3（正式/沙箱）双版本切换
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "支付管理", description = "支付创建、状态查询与第三方回调处理")
public class PaymentController {

    private final OrderService orderService;
    private final RefundService refundService;
    
    /**
     * 微信支付版本配置 🎯
     * v2: 使用V2沙箱环境（推荐开发测试）
     * v3: 使用V3正式/沙箱环境
     */
    @Value("${wechat.pay.version:v3}")
    private String wechatPayVersion;
    
    // V3服务（可选注入）
    @Autowired(required = false)
    private WechatPaymentService wechatPaymentService;
    
    // V2服务（可选注入）
    @Autowired(required = false)
    private WechatPaymentServiceV2 wechatPaymentServiceV2;

    // 支付宝服务（可选注入，用于回调验签）
    @Autowired(required = false)
    private AlipayPaymentService alipayPaymentService;

    /**
     * 创建支付订单 🚀
     *
     * POST /api/payment/create
     *
     * 注：此接口已弃用，请直接使用 POST /api/orders 创建订单并支付
     * OrderService.payOrder() 方法会处理订单创建和支付流程
     *
     * @param request 支付请求
     * @return 提示信息
     */
    @Operation(summary = "创建支付订单")

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    @Deprecated
    public ApiResponse<String> createPayment(@Valid @RequestBody PayOrderRequest request) {
        log.info("📥 收到支付请求（已弃用）: orderNo={}, paymentMethod={}", 
                request.orderNo(), request.paymentMethod());
        
        return ApiResponse.success("请使用 POST /api/orders 创建订单并支付");
    }

    /**
     * 微信支付异步通知回调 🔔
     *
     * POST /api/payment/wechat/notify
     *
     * 微信支付完成后会主动调用此接口
     * 自动根据配置的版本（V2/V3）处理回调
     *
     * @param httpRequest HTTP请求
     * @return 响应给微信服务器
     */
    @Operation(summary = "微信支付回调通知")

    @PostMapping("/wechat/notify")
    public ResponseEntity<String> wechatPayNotify(HttpServletRequest httpRequest) {
        log.info("📥 收到微信支付回调通知，当前版本: {}", wechatPayVersion);

        try {
            // 读取请求体
            String body;
            try (BufferedReader reader = httpRequest.getReader()) {
                body = reader.lines().collect(Collectors.joining());
            }

            String[] result;
            String successResponse;
            String failResponse;

            // 根据配置的版本选择处理方式
            if ("v2".equalsIgnoreCase(wechatPayVersion)) {
                // V2沙箱环境：使用XML格式回调
                log.info("🔍 使用微信支付V2沙箱处理回调");
                
                if (wechatPaymentServiceV2 == null) {
                    log.error("💥 微信支付V2服务未初始化，请检查配置");
                    throw new IllegalStateException("微信支付V2服务未配置");
                }
                
                result = wechatPaymentServiceV2.handleNotify(body);
                successResponse = wechatPaymentServiceV2.buildSuccessResponse();
                failResponse = wechatPaymentServiceV2.buildFailResponse("订单信息为空");
                
            } else {
                // V3正式/沙箱环境：使用JSON格式回调
                log.info("🔍 使用微信支付V3处理回调");
                
                if (wechatPaymentService == null) {
                    log.error("💥 微信支付V3服务未初始化，请检查配置");
                    throw new IllegalStateException("微信支付V3服务未配置");
                }
                
                // 获取V3的HTTP头部信息
                String signature = httpRequest.getHeader("Wechatpay-Signature");
                String serial = httpRequest.getHeader("Wechatpay-Serial");
                String nonce = httpRequest.getHeader("Wechatpay-Nonce");
                String timestamp = httpRequest.getHeader("Wechatpay-Timestamp");
                
                log.info("🔍 微信V3回调头部: serial={}, timestamp={}, nonce={}", serial, timestamp, nonce);
                
                // 构建回调请求参数
                RequestParam requestParam = wechatPaymentService.buildRequestParam(
                        signature, serial, nonce, timestamp, body
                );
                
                result = wechatPaymentService.handleNotify(requestParam);
                successResponse = wechatPaymentService.buildSuccessResponse();
                failResponse = wechatPaymentService.buildFailResponse("订单信息为空");
            }

            // 处理回调结果
            if (result != null && result.length >= 2) {
                String orderNo = result[0];
                String transactionId = result[1];

                log.info("✅ 微信支付{}回调处理成功: orderNo={}, transactionId={}", 
                        wechatPayVersion.toUpperCase(), orderNo, transactionId);

                // 更新订单状态为已支付
                try {
                    // ✅ 安全实践：从数据库查询订单金额，而不是信任回调数据
                    // 原因：防止回调数据被篡改，确保金额以系统记录为准
                    com.campus.marketplace.common.entity.Order order = orderService.getOrderDetail(orderNo);
                    if (order == null) {
                        log.error("💥 订单不存在: orderNo={}", orderNo);
                        throw new IllegalStateException("订单不存在");
                    }

                    BigDecimal amount = order.getAmount();

                    PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest(
                            orderNo,
                            transactionId,
                            amount,
                            "SUCCESS",
                            null // 签名已由微信支付服务验证
                    );
                    boolean updateSuccess = orderService.handlePaymentCallback(callbackRequest, true);

                    if (updateSuccess) {
                        log.info("🎉 订单状态更新成功: orderNo={}", orderNo);
                    } else {
                        log.error("💥 订单状态更新失败: orderNo={}", orderNo);
                    }
                } catch (Exception e) {
                    log.error("💥 订单状态更新异常: orderNo={}", orderNo, e);
                    // 虽然订单更新失败，但仍然返回成功给微信，避免重复回调
                    // 后续可通过定时任务或人工处理
                }

                // 返回成功响应给微信服务器
                return ResponseEntity.ok(successResponse);
            } else {
                log.error("💥 微信支付{}回调处理失败: 订单信息为空", wechatPayVersion.toUpperCase());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failResponse);
            }

        } catch (Exception e) {
            log.error("💥 微信支付{}回调异常", wechatPayVersion.toUpperCase(), e);
            
            String errorResponse = "v2".equalsIgnoreCase(wechatPayVersion) && wechatPaymentServiceV2 != null
                    ? wechatPaymentServiceV2.buildFailResponse("系统异常")
                    : (wechatPaymentService != null 
                        ? wechatPaymentService.buildFailResponse("系统异常") 
                        : "FAIL");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 查询支付状态 🔍
     *
     * GET /api/payment/status/{orderNo}
     *
     * @param orderNo 订单号
     * @return 支付状态
     */
    @Operation(summary = "查询支付状态")

    @GetMapping("/status/{orderNo}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    
    public ApiResponse<String> queryPaymentStatus(@Parameter(description = "订单号", example = "O202510270001") @PathVariable String orderNo) {
        log.info("🔍 查询支付状态: orderNo={}, version={}", orderNo, wechatPayVersion);
        
        String status;
        if ("v2".equalsIgnoreCase(wechatPayVersion) && wechatPaymentServiceV2 != null) {
            status = wechatPaymentServiceV2.queryOrderStatus(orderNo);
        } else if (wechatPaymentService != null) {
            status = wechatPaymentService.queryOrderStatus(orderNo);
        } else {
            log.error("💥 微信支付服务未初始化");
            throw new IllegalStateException("微信支付服务未配置");
        }
        
        return ApiResponse.success(status);
    }

    /**
     * 支付宝退款异步通知回调（简化版）
     *
     * POST /api/payment/alipay/refund/notify
     */
    @Operation(summary = "支付宝退款回调")

    @PostMapping("/alipay/refund/notify")
    public ResponseEntity<String> alipayRefundNotify(HttpServletRequest request) {
        try {
            java.util.Map<String, String[]> paramMap = request.getParameterMap();
            java.util.Map<String, String> params = new java.util.HashMap<>();
            for (var e : paramMap.entrySet()) {
                if (e.getValue() != null && e.getValue().length > 0) {
                    params.put(e.getKey(), e.getValue()[0]);
                }
            }

            boolean verified = alipayPaymentService == null || alipayPaymentService.verifySignature(params);
            if (!verified) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
            }

            String orderNo = params.getOrDefault("out_trade_no", params.getOrDefault("order_no", null));
            String status = params.getOrDefault("refund_status", "SUCCESS");
            boolean success = "SUCCESS".equalsIgnoreCase(status) || "REFUND_SUCCESS".equalsIgnoreCase(status);

            boolean handled = false;
            if (orderNo != null) {
                handled = refundService.handleRefundCallback(orderNo, "ALIPAY", success, params);
            }

            return ResponseEntity.ok(handled ? "success" : "fail");
        } catch (Exception e) {
            log.error("💥 支付宝退款回调异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }
}
