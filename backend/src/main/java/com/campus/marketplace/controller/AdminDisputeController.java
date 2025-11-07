package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.DisputeDTO;
import com.campus.marketplace.common.dto.DisputeDetailDTO;
import com.campus.marketplace.common.dto.request.ArbitrateDisputeRequest;
import com.campus.marketplace.common.dto.request.UploadEvidenceRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.enums.DisputeStatus;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.DisputeArbitrationService;
import com.campus.marketplace.service.DisputeEvidenceService;
import com.campus.marketplace.service.DisputeService;
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
import java.util.List;

/**
 * 管理员纠纷管理控制器
 *
 * 提供管理员视角的纠纷查询、仲裁处理等 REST API
 * 适配前端 /api/admin/disputes 路由
 *
 * @author BaSui 😎
 * @since 2025-11-06
 */
@Slf4j
@RestController
@RequestMapping("/admin/disputes")
@RequiredArgsConstructor
@Tag(name = "管理员纠纷管理", description = "管理员查询和处理纠纷")
public class AdminDisputeController {

    private final DisputeService disputeService;
    private final DisputeArbitrationService arbitrationService;
    private final DisputeEvidenceService evidenceService;

    /**
     * 查询纠纷列表（管理员）
     *
     * GET /api/admin/disputes?keyword=&type=&status=&page=0&size=20
     *
     * @param keyword 搜索关键字（纠纷编号、订单号）
     * @param status 纠纷状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 纠纷列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询纠纷列表", description = "管理员查询所有纠纷（支持筛选）")
    public ApiResponse<Page<DisputeDTO>> listDisputes(
            @Parameter(description = "搜索关键字", example = "DSP-20251106-001")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "纠纷状态", example = "ARBITRATING")
            @RequestParam(required = false) DisputeStatus status,
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("管理员查询纠纷列表: keyword={}, status={}, page={}, size={}",
                keyword, status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<DisputeDTO> disputes = disputeService.getUserDisputes(null, status, pageable);
        return ApiResponse.success(disputes);
    }

    /**
     * 查询待处理纠纷列表
     *
     * GET /api/admin/disputes/pending
     *
     * @param page 页码
     * @param size 每页大小
     * @return 待处理纠纷列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询待处理纠纷", description = "查询待仲裁状态的纠纷列表")
    public ApiResponse<Page<DisputeDTO>> listPendingDisputes(
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("查询待处理纠纷列表: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<DisputeDTO> disputes = disputeService.getUserDisputes(
                null, DisputeStatus.PENDING_ARBITRATION, pageable);
        return ApiResponse.success(disputes);
    }

    /**
     * 查询我的仲裁纠纷列表（当前仲裁员）
     *
     * GET /api/admin/disputes/my?status=ARBITRATING&page=0&size=20
     *
     * @param status 纠纷状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 我的仲裁纠纷列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询我的仲裁纠纷", description = "查询当前仲裁员处理的纠纷列表")
    public ApiResponse<Page<DisputeDTO>> listMyDisputes(
            @Parameter(description = "纠纷状态", example = "ARBITRATING")
            @RequestParam(required = false) DisputeStatus status,
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Long arbitratorId = SecurityUtil.getCurrentUserId();
        log.info("查询仲裁员纠纷列表: arbitratorId={}, status={}, page={}, size={}",
                arbitratorId, status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        // ✅ 调用新增的按仲裁员ID查询方法
        Page<DisputeDTO> disputes = disputeService.getArbitratorDisputes(arbitratorId, status, pageable);
        return ApiResponse.success(disputes);
    }

    /**
     * 查询纠纷详情
     *
     * GET /api/admin/disputes/{id}
     *
     * @param id 纠纷ID
     * @return 纠纷详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询纠纷详情", description = "查询指定纠纷的完整信息（含证据、协商记录、仲裁结果）")
    public ApiResponse<DisputeDetailDTO> getDisputeDetail(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long id
    ) {
        log.info("管理员查询纠纷详情: id={}", id);
        DisputeDetailDTO detail = disputeService.getDisputeDetail(id);
        return ApiResponse.success(detail);
    }

    /**
     * 认领纠纷（仲裁员接受处理）
     *
     * POST /api/admin/disputes/{id}/claim
     *
     * @param id 纠纷ID
     * @return 是否成功
     */
    @PostMapping("/{id}/claim")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "认领纠纷", description = "仲裁员认领待处理的纠纷")
    public ApiResponse<Void> claimDispute(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long id
    ) {
        Long arbitratorId = SecurityUtil.getCurrentUserId();
        log.info("仲裁员认领纠纷: disputeId={}, arbitratorId={}", id, arbitratorId);
        boolean result = arbitrationService.assignArbitrator(id, arbitratorId);
        if (!result) {
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "认领失败");
        }
        return ApiResponse.success(null);
    }

    /**
     * 仲裁纠纷（处理纠纷）
     *
     * POST /api/admin/disputes/{id}/arbitrate
     * Body:
     * {
     *   "action": "RESOLVE",
     *   "decision": "支持买家退款申请，商品存在质量问题",
     *   "compensationAmount": 99.00,
     *   "reason": "经核实，买家提供的证据充分，确认商品存在质量瑕疵"
     * }
     *
     * @param id 纠纷ID
     * @param request 仲裁请求
     * @return 是否成功
     */
    @PostMapping("/{id}/arbitrate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "仲裁纠纷", description = "仲裁员提交仲裁决定")
    public ApiResponse<Void> arbitrateDispute(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ArbitrateDisputeRequest request
    ) {
        Long arbitratorId = SecurityUtil.getCurrentUserId();
        log.info("仲裁纠纷: disputeId={}, arbitratorId={}, result={}",
                id, arbitratorId, request.getResult());

        // 设置纠纷ID
        request.setDisputeId(id);

        // 提交仲裁决定
        Long arbitrationId = arbitrationService.submitArbitration(request, arbitratorId);
        if (arbitrationId == null) {
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "仲裁失败");
        }

        return ApiResponse.success(null);
    }

    /**
     * 提交证据材料
     *
     * POST /api/admin/disputes/{id}/evidence
     * Body:
     * {
     *   "type": "IMAGE",
     *   "url": "https://example.com/evidence.jpg",
     *   "fileName": "证据1.jpg",
     *   "fileSize": 102400,
     *   "description": "商品质量问题照片"
     * }
     *
     * @param id 纠纷ID
     * @param request 证据请求
     * @return 是否成功
     */
    @PostMapping("/{id}/evidence")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "提交证据", description = "上传纠纷相关的证据材料")
    public ApiResponse<Void> submitEvidence(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UploadEvidenceRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("提交证据材料: disputeId={}, userId={}, type={}",
                id, userId, request.getEvidenceType());

        // 设置纠纷ID
        request.setDisputeId(id);

        // 提交证据
        Long evidenceId = evidenceService.uploadEvidence(request, userId);
        if (evidenceId == null) {
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "提交证据失败");
        }

        return ApiResponse.success(null);
    }

    /**
     * 关闭纠纷
     *
     * POST /api/admin/disputes/{id}/close
     * Body: { "reason": "用户撤销申请" }
     *
     * @param id 纠纷ID
     * @param reason 关闭原因
     * @return 是否成功
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "关闭纠纷", description = "管理员手动关闭纠纷")
    public ApiResponse<Void> closeDispute(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "关闭原因", example = "用户撤销申请")
            @RequestParam String reason
    ) {
        log.info("关闭纠纷: disputeId={}, reason={}", id, reason);
        boolean result = disputeService.closeDispute(id, reason);
        if (!result) {
            return ApiResponse.error(ErrorCode.OPERATION_FAILED, "关闭失败");
        }
        return ApiResponse.success(null);
    }

    /**
     * 批量分配仲裁员
     *
     * POST /api/admin/disputes/batch-assign
     * Body:
     * {
     *   "disputeIds": [1, 2, 3],
     *   "arbitratorId": 100
     * }
     *
     * @param disputeIds 纠纷ID列表
     * @param arbitratorId 仲裁员ID
     * @return 是否成功
     */
    @PostMapping("/batch-assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量分配仲裁员", description = "管理员批量分配仲裁员")
    public ApiResponse<Void> batchAssignArbitrator(
            @Parameter(description = "纠纷ID列表", example = "[1, 2, 3]")
            @RequestParam List<Long> disputeIds,
            @Parameter(description = "仲裁员ID", example = "100")
            @RequestParam Long arbitratorId
    ) {
        log.info("批量分配仲裁员: disputeIds={}, arbitratorId={}", disputeIds, arbitratorId);

        for (Long disputeId : disputeIds) {
            arbitrationService.assignArbitrator(disputeId, arbitratorId);
        }

        return ApiResponse.success(null);
    }
}
