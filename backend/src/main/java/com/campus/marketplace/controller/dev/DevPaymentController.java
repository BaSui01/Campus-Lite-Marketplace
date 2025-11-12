package com.campus.marketplace.controller.dev;

import com.campus.marketplace.common.dto.request.PaymentCallbackRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 开发环境支付辅助控制器（Dev 专用）
 *
 * 仅在 dev Profile 启用，用于在无法暴露公网地址时，本地手动标记订单为已支付，
 * 内部复用正式的回调处理流程，保证与真实回调一致的校验与副作用。
 *
 * 安全约束：
 * - 仅 dev 环境启用（@Profile("dev")）
 * - 仅认证用户可调用
 * - 仅买家本人或管理员可执行
 *
 * 使用方式：
 * POST /api/admin/dev/orders/{orderNo}/mark-paid
 */
@Slf4j
@RestController
@Profile("dev")
@RequiredArgsConstructor
@RequestMapping("/admin/dev")
@Tag(name = "开发辅助-支付", description = "仅 dev 环境启用，用于本地模拟第三方回调（不依赖公网）")
public class DevPaymentController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @PostMapping("/orders/{orderNo}/mark-paid")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "标记订单为已支付（dev）", description = "仅限买家本人或管理员调用；内部走正式回调逻辑，触发通知与商品售出状态。")
    public ApiResponse<Void> markPaid(
            @Parameter(description = "订单号", example = "ORD20251112041820411")
            @PathVariable String orderNo
    ) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 权限校验：买家本人或管理员
        String username = SecurityUtil.getCurrentUsername();
        Long currentUserId = userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        boolean isOwner = currentUserId.equals(order.getBuyerId());
        boolean isAdmin = SecurityUtil.hasRole("ADMIN") || SecurityUtil.hasRole("SUPER_ADMIN");
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "仅买家本人或管理员可执行本操作");
        }

        // 仅允许从待支付流转
        if (!order.isPendingPayment()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "当前状态不支持标记已支付");
        }

        BigDecimal amount = order.getActualAmount() != null ? order.getActualAmount() : order.getAmount();
        PaymentCallbackRequest req = new PaymentCallbackRequest(
                order.getOrderNo(),
                "DEV-" + LocalDateTime.now(),
                amount,
                "SUCCESS",
                null
        );

        boolean ok = orderService.handlePaymentCallback(req, true);
        if (!ok) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "订单状态未更新，请检查日志");
        }
        log.info("[DEV] 手动标记订单已支付成功: orderNo={}", orderNo);
        return ApiResponse.success(null);
    }
}

