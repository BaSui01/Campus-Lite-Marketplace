package com.campus.marketplace.controller.e2e;

import com.campus.marketplace.common.dto.request.PaymentCallbackRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * E2E 支付回调模拟控制器
 *
 * <p>仅在 {@code e2e} Profile 下启用，用于端到端测试中模拟第三方支付平台的回调。</p>
 *
 * <p>触发流程：
 * <ol>
 *   <li>通过接口提交 {@link PaymentCallbackRequest} 请求</li>
 *   <li>内部直接调用 {@link OrderService#handlePaymentCallback(PaymentCallbackRequest, boolean)} 更新订单状态</li>
 *   <li>返回统一成功响应，供测试脚本断言</li>
 * </ol>
 * </p>
 *
 * @author BaSui
 * @date 2025-10-29
 */
@Slf4j
@RestController
@Profile("e2e")
@RequiredArgsConstructor
@RequestMapping("/api/payment/mock")
@Tag(name = "模拟支付", description = "端到端测试专用的支付回调模拟接口，仅 e2e Profile 可用")
public class PaymentMockController {

    private final OrderService orderService;

    /**
     * 模拟微信支付成功回调。
     *
     * <p>约束：仅超级管理员可调用，避免测试接口被误用。</p>
     *
     * @param request 回调数据
     * @return 操作结果
     */
    @PostMapping("/wechat")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "模拟微信支付回调", description = "直接驱动订单支付成功流程，端到端测试专用")
    public ApiResponse<Void> mockWechatPayment(@Valid @RequestBody PaymentCallbackRequest request) {
        log.info("[E2E] 模拟微信支付回调: orderNo={}, txnId={}, amount={}",
                request.orderNo(), request.transactionId(), request.amount());

        BigDecimal amount = request.amount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "模拟回调金额必须为正数");
        }

        boolean success = orderService.handlePaymentCallback(request, true);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "订单状态不支持模拟支付");
        }

        log.info("[E2E] 模拟支付完成: orderNo={}, txnId={}", request.orderNo(), request.transactionId());
        return ApiResponse.success("模拟支付成功", null);
    }
}
