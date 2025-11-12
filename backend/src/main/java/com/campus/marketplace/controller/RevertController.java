package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateRevertRequestDto;
import com.campus.marketplace.common.dto.response.RevertStatistics;
import com.campus.marketplace.common.entity.RevertRequest;
import com.campus.marketplace.common.enums.RevertRequestStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import com.campus.marketplace.service.RevertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 撤销操作控制器
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Tag(name = "操作撤销")
@RestController
@RequestMapping("/revert")
@RequiredArgsConstructor
public class RevertController {

    private final RevertService revertService;

        @Operation(summary = "申请撤销操作")

    @PostMapping("/request")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RevertExecutionResult> requestRevert(
            @RequestParam Long auditLogId,
            @Valid @RequestBody CreateRevertRequestDto request) {
        
        Long userId = SecurityUtil.getCurrentUserId();
        RevertExecutionResult result = revertService.requestRevert(auditLogId, request, userId);
        
        return ApiResponse.success(result);
    }

        @Operation(summary = "查询用户的撤销请求历史")
    @GetMapping("/requests")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<?>> getUserRevertRequests(Pageable pageable) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<?> requests = revertService.getUserRevertRequests(userId, pageable);
        return ApiResponse.success(requests);
    }

        @Operation(summary = "执行撤销操作")

    @PostMapping("/execute/{revertRequestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RevertExecutionResult> executeRevert(@PathVariable Long revertRequestId) {
        Long adminId = SecurityUtil.getCurrentUserId();
        RevertExecutionResult result = revertService.executeRevert(revertRequestId, adminId);
        return ApiResponse.success(result);
    }

    // ==================== 管理员接口 ====================

    @Operation(summary = "查询所有撤销请求（管理员）")
    @GetMapping("/admin/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<RevertRequest>> listRevertRequests(
            @RequestParam(required = false) RevertRequestStatus status,
            Pageable pageable) {
        Page<RevertRequest> requests = revertService.listRevertRequests(status, pageable);
        return ApiResponse.success(requests);
    }

    @Operation(summary = "获取撤销统计数据（管理员）")
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RevertStatistics> getRevertStatistics() {
        RevertStatistics statistics = revertService.getRevertStatistics();
        return ApiResponse.success(statistics);
    }

    @Operation(summary = "批准撤销请求（管理员）")
    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> approveRevert(
            @PathVariable Long id,
            @RequestParam(required = false) String comment) {
        Long approverId = SecurityUtil.getCurrentUserId();
        revertService.approveRevert(id, comment, approverId);
        return ApiResponse.success();
    }

    @Operation(summary = "拒绝撤销请求（管理员）")
    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> rejectRevert(
            @PathVariable Long id,
            @RequestParam(required = true) String reason) {
        Long approverId = SecurityUtil.getCurrentUserId();
        revertService.rejectRevert(id, reason, approverId);
        return ApiResponse.success();
    }
}
