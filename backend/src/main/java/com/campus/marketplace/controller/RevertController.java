package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateRevertRequestDto;
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
@RequestMapping("/api/revert")
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
}
