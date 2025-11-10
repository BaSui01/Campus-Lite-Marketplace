package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.enums.RefundStatus;
import com.campus.marketplace.service.RefundService;
import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Refund Controller
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "退款管理", description = "退款申请、审批与详情查询")
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/orders/{orderNo}/refunds")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    @Operation(summary = "申请退款", description = "买家提交退款申请，支持附带凭证信息")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "退款凭证（可选）",
            required = false,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = java.util.Map.class),
                    examples = @ExampleObject(
                            name = "请求体示例",
                            value = """
                                    {
                                      \"images\": [\"https://cdn.campus.com/refund/e1.png\"],
                                      \"note\": \"到手屏幕破裂\"
                                    }
                                    """
                    )
            )
    )
    public ApiResponse<String> apply(@Parameter(description = "订单号", example = "O202510270001") @PathVariable String orderNo,
                                     @Parameter(description = "退款原因", example = "与描述不符") @RequestParam @NotBlank String reason,
                                     @RequestBody(required = false) Map<String, Object> evidence) {
        String refundNo = refundService.applyRefund(orderNo, reason, evidence);
        return ApiResponse.success(refundNo);
    }

    @PutMapping("/admin/refunds/{refundNo}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "审批通过并退款", description = "管理员审批通过后触发退款")
    public ApiResponse<Void> approve(@Parameter(description = "退款单号", example = "R202510270001") @PathVariable String refundNo) {
        refundService.approveAndRefund(refundNo);
        return ApiResponse.success(null);
    }

    @PutMapping("/admin/refunds/{refundNo}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "驳回退款申请", description = "管理员驳回申请并填写原因")
    public ApiResponse<Void> reject(@Parameter(description = "退款单号", example = "R202510270001") @PathVariable String refundNo,
                                    @Parameter(description = "驳回原因", example = "不满足平台退款规则") @RequestParam @NotBlank String reason) {
        refundService.reject(refundNo, reason);
        return ApiResponse.success(null);
    }

    @GetMapping("/admin/refunds/{refundNo}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "退款详情", description = "管理员查看退款申请详情（包含关联商品、用户信息）")
    public ApiResponse<RefundRequest> detail(@Parameter(description = "退款单号", example = "R202510270001") @PathVariable String refundNo) {
        // TODO: 后续优化为返回 RefundResponseDTO（包含关联的商品、买家、卖家信息）
        return ApiResponse.success(refundService.getByRefundNo(refundNo));
    }

    @GetMapping("/refunds")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    @Operation(summary = "查询我的退款列表", description = "用户查询自己的退款列表，支持分页和状态筛选")
    public ApiResponse<Page<RefundRequest>> listMyRefunds(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "退款状态（可选）", example = "REFUNDED") @RequestParam(required = false) RefundStatus status) {
        Page<RefundRequest> result = refundService.listMyRefunds(page, size, status);
        return ApiResponse.success(result);
    }

    @GetMapping("/refunds/{refundNo}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    @Operation(summary = "查询我的退款详情", description = "用户查询自己的退款详情（仅限本人）")
    public ApiResponse<RefundRequest> getMyRefund(@Parameter(description = "退款单号", example = "RFD202510270001") @PathVariable String refundNo) {
        return ApiResponse.success(refundService.getMyRefund(refundNo));
    }

    @GetMapping("/admin/refunds")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "管理员查询所有退款列表", description = "管理员查询所有退款，支持分页、状态筛选和关键词搜索")
    public ApiResponse<Page<RefundRequest>> listAllRefunds(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "退款状态（可选）", example = "APPLIED") @RequestParam(required = false) RefundStatus status,
            @Parameter(description = "搜索关键词（可选，匹配退款单号或订单号）", example = "RFD") @RequestParam(required = false) String keyword) {
        Page<RefundRequest> result = refundService.listAllRefunds(page, size, status, keyword);
        return ApiResponse.success(result);
    }
}
