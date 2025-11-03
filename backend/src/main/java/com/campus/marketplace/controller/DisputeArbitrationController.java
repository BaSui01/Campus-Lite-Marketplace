package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.ArbitrationDTO;
import com.campus.marketplace.common.dto.request.ArbitrateDisputeRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.DisputeArbitrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 纠纷仲裁控制器
 *
 * 提供仲裁员分配、仲裁决定提交、仲裁查询等 REST API
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/api/disputes/arbitrations")
@RequiredArgsConstructor
@Tag(name = "纠纷仲裁", description = "仲裁员分配、仲裁决定与执行")
public class DisputeArbitrationController {

    private final DisputeArbitrationService arbitrationService;

    /**
     * 分配仲裁员（管理员）
     *
     * POST /api/disputes/{disputeId}/arbitrations/assign
     *
     * @param disputeId 纠纷ID
     * @param arbitratorId 仲裁员ID
     * @return 是否成功
     */
    @PostMapping("/{disputeId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分配仲裁员", description = "管理员为纠纷分配仲裁员")
    public ApiResponse<Boolean> assignArbitrator(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId,
            @Parameter(description = "仲裁员ID", example = "300")
            @RequestParam Long arbitratorId
    ) {
        log.info("分配仲裁员: disputeId={}, arbitratorId={}", disputeId, arbitratorId);
        boolean result = arbitrationService.assignArbitrator(disputeId, arbitratorId);
        return ApiResponse.success(result);
    }

    /**
     * 提交仲裁决定
     *
     * POST /api/disputes/arbitrations
     *
     * @param request 仲裁请求
     * @return 仲裁ID
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "提交仲裁决定", description = "仲裁员提交对纠纷的最终裁决")
    public ApiResponse<Long> submitArbitration(@Valid @RequestBody ArbitrateDisputeRequest request) {
        Long arbitratorId = SecurityUtil.getCurrentUserId();
        log.info("提交仲裁决定: disputeId={}, arbitratorId={}, result={}",
                request.getDisputeId(), arbitratorId, request.getResult());
        Long arbitrationId = arbitrationService.submitArbitration(request, arbitratorId);
        return ApiResponse.success(arbitrationId);
    }

    /**
     * 查询仲裁详情
     *
     * GET /api/disputes/{disputeId}/arbitrations
     *
     * @param disputeId 纠纷ID
     * @return 仲裁详情
     */
    @GetMapping("/{disputeId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    @Operation(summary = "查询仲裁详情", description = "查询纠纷的仲裁决定详情")
    public ApiResponse<Optional<ArbitrationDTO>> getArbitrationDetail(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId
    ) {
        log.info("查询仲裁详情: disputeId={}", disputeId);
        Optional<ArbitrationDTO> arbitration = arbitrationService.getArbitrationDetail(disputeId);
        return ApiResponse.success(arbitration);
    }

    /**
     * 查询仲裁员案件列表
     *
     * GET /api/disputes/arbitrations/my-cases
     *
     * @return 案件列表
     */
    @GetMapping("/my-cases")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询我的仲裁案件", description = "仲裁员查询自己处理的案件列表")
    public ApiResponse<List<ArbitrationDTO>> getArbitratorCases() {
        Long arbitratorId = SecurityUtil.getCurrentUserId();
        log.info("查询仲裁员案件列表: arbitratorId={}", arbitratorId);
        List<ArbitrationDTO> cases = arbitrationService.getArbitratorCases(arbitratorId);
        return ApiResponse.success(cases);
    }

    /**
     * 查询待执行仲裁列表（管理员）
     *
     * GET /api/disputes/arbitrations/pending-execution
     *
     * @return 待执行仲裁列表
     */
    @GetMapping("/pending-execution")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询待执行仲裁", description = "管理员查询需要执行退款的仲裁列表")
    public ApiResponse<List<ArbitrationDTO>> getPendingExecutions() {
        log.info("查询待执行仲裁列表");
        List<ArbitrationDTO> arbitrations = arbitrationService.getPendingExecutions();
        return ApiResponse.success(arbitrations);
    }

    /**
     * 标记仲裁为已执行（管理员）
     *
     * POST /api/disputes/arbitrations/{arbitrationId}/mark-executed
     *
     * @param arbitrationId 仲裁ID
     * @param executionNote 执行说明
     * @return 是否成功
     */
    @PostMapping("/{arbitrationId}/mark-executed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "标记仲裁已执行", description = "管理员标记退款已处理完成")
    public ApiResponse<Boolean> markExecuted(
            @Parameter(description = "仲裁ID", example = "1")
            @PathVariable Long arbitrationId,
            @Parameter(description = "执行说明", example = "退款已成功处理")
            @RequestParam String executionNote
    ) {
        log.info("标记仲裁为已执行: arbitrationId={}", arbitrationId);
        boolean result = arbitrationService.markExecuted(arbitrationId, executionNote);
        return ApiResponse.success(result);
    }
}
