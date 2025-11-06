package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.DisputeDTO;
import com.campus.marketplace.common.dto.DisputeDetailDTO;
import com.campus.marketplace.common.dto.request.CreateDisputeRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.enums.DisputeStatus;
import com.campus.marketplace.service.DisputeService;
import com.campus.marketplace.common.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 纠纷管理控制器
 *
 * 提供纠纷提交、查询、升级、关闭等 REST API
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/disputes")
@RequiredArgsConstructor
@Tag(name = "纠纷管理", description = "纠纷提交、查询与处理")
public class DisputeController {

    private final DisputeService disputeService;

    /**
     * 提交纠纷
     *
     * POST /api/disputes
     *
     * @param request 纠纷请求
     * @return 纠纷ID
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    @Operation(summary = "提交纠纷", description = "买家或卖家针对订单提交纠纷申请")
    public ApiResponse<Long> submitDispute(@Valid @RequestBody CreateDisputeRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("收到提交纠纷请求: orderId={}, userId={}", request.getOrderId(), userId);
        Long disputeId = disputeService.submitDispute(request, userId);
        return ApiResponse.success(disputeId);
    }

    /**
     * 查询用户的纠纷列表
     *
     * GET /api/disputes?status=NEGOTIATING&page=0&size=20
     *
     * @param status 纠纷状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 纠纷列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    @Operation(summary = "查询我的纠纷列表", description = "分页查询当前用户参与的纠纷")
    public ApiResponse<Page<DisputeDTO>> getUserDisputes(
            @Parameter(description = "纠纷状态", example = "NEGOTIATING")
            @RequestParam(required = false) DisputeStatus status,
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("查询用户纠纷列表: userId={}, status={}, page={}, size={}",
                userId, status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<DisputeDTO> disputes = disputeService.getUserDisputes(userId, status, pageable);
        return ApiResponse.success(disputes);
    }

    /**
     * 查询纠纷详情
     *
     * GET /api/disputes/{disputeId}
     *
     * @param disputeId 纠纷ID
     * @return 纠纷详情
     */
    @GetMapping("/{disputeId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    @Operation(summary = "查询纠纷详情", description = "查询指定纠纷的完整信息")
    public ApiResponse<DisputeDetailDTO> getDisputeDetail(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId
    ) {
        log.info("查询纠纷详情: disputeId={}", disputeId);
        DisputeDetailDTO detail = disputeService.getDisputeDetail(disputeId);
        return ApiResponse.success(detail);
    }

    /**
     * 升级纠纷为仲裁状态
     *
     * POST /api/disputes/{disputeId}/escalate
     *
     * @param disputeId 纠纷ID
     * @return 是否成功
     */
    @PostMapping("/{disputeId}/escalate")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    @Operation(summary = "升级纠纷", description = "将协商失败的纠纷升级为待仲裁状态")
    public ApiResponse<Boolean> escalateToArbitration(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId
    ) {
        log.info("升级纠纷为仲裁: disputeId={}", disputeId);
        boolean result = disputeService.escalateToArbitration(disputeId);
        return ApiResponse.success(result);
    }

    /**
     * 关闭纠纷
     *
     * POST /api/disputes/{disputeId}/close
     *
     * @param disputeId 纠纷ID
     * @param closeReason 关闭原因
     * @return 是否成功
     */
    @PostMapping("/{disputeId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "关闭纠纷", description = "管理员手动关闭纠纷（需要说明原因）")
    public ApiResponse<Boolean> closeDispute(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId,
            @Parameter(description = "关闭原因", example = "用户撤销")
            @RequestParam String closeReason
    ) {
        log.info("关闭纠纷: disputeId={}, reason={}", disputeId, closeReason);
        boolean result = disputeService.closeDispute(disputeId, closeReason);
        return ApiResponse.success(result);
    }

    /**
     * 查询所有纠纷（管理员）
     *
     * GET /api/disputes/admin/all?status=PENDING_ARBITRATION&page=0&size=20
     *
     * @param status 纠纷状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 纠纷列表
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询所有纠纷", description = "管理员查询系统中的所有纠纷")
    public ApiResponse<Page<DisputeDTO>> getAllDisputes(
            @Parameter(description = "纠纷状态", example = "PENDING_ARBITRATION")
            @RequestParam(required = false) DisputeStatus status,
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("管理员查询所有纠纷: status={}, page={}, size={}", status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        // 使用null作为userId表示查询所有纠纷
        Page<DisputeDTO> disputes = disputeService.getUserDisputes(null, status, pageable);
        return ApiResponse.success(disputes);
    }
}
