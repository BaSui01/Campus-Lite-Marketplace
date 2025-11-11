package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.EvidenceDTO;
import com.campus.marketplace.common.dto.EvidenceSummaryDTO;
import com.campus.marketplace.common.dto.request.UploadEvidenceRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.enums.EvidenceValidity;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.DisputeEvidenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 纠纷证据控制器
 *
 * 提供证据上传、查询、评估、删除等 REST API
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/disputes/evidence")
@RequiredArgsConstructor
@Tag(name = "纠纷证据", description = "证据上传、查询与评估")
public class DisputeEvidenceController {

    private final DisputeEvidenceService evidenceService;

    /**
     * 上传证据
     *
     * POST /api/disputes/evidence
     *
     * @param request 上传请求
     * @return 证据ID
     */
    @PostMapping
    @Operation(summary = "上传证据", description = "买卖双方上传图片、视频或聊天记录作为证据")
    public ApiResponse<Long> uploadEvidence(@Valid @RequestBody UploadEvidenceRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("上传证据: disputeId={}, userId={}, type={}",
                request.getDisputeId(), userId, request.getEvidenceType());
        Long evidenceId = evidenceService.uploadEvidence(request, userId);
        return ApiResponse.success(evidenceId);
    }

    /**
     * 查询纠纷所有证据
     *
     * GET /api/disputes/{disputeId}/evidence
     *
     * @param disputeId 纠纷ID
     * @return 证据列表
     */
    @GetMapping("/{disputeId}")

    @Operation(summary = "查询纠纷所有证据", description = "查询纠纷的完整证据列表")
    public ApiResponse<List<EvidenceDTO>> getDisputeEvidence(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId
    ) {
        log.info("查询纠纷所有证据: disputeId={}", disputeId);
        List<EvidenceDTO> evidence = evidenceService.getDisputeEvidence(disputeId);
        return ApiResponse.success(evidence);
    }

    /**
     * 查询买家证据
     *
     * GET /api/disputes/{disputeId}/evidence/buyer
     *
     * @param disputeId 纠纷ID
     * @return 买家证据列表
     */
    @GetMapping("/{disputeId}/buyer")

    @Operation(summary = "查询买家证据", description = "查询买家上传的所有证据")
    public ApiResponse<List<EvidenceDTO>> getBuyerEvidence(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId
    ) {
        log.info("查询买家证据: disputeId={}", disputeId);
        List<EvidenceDTO> evidence = evidenceService.getBuyerEvidence(disputeId);
        return ApiResponse.success(evidence);
    }

    /**
     * 查询卖家证据
     *
     * GET /api/disputes/{disputeId}/evidence/seller
     *
     * @param disputeId 纠纷ID
     * @return 卖家证据列表
     */
    @GetMapping("/{disputeId}/seller")

    @Operation(summary = "查询卖家证据", description = "查询卖家上传的所有证据")
    public ApiResponse<List<EvidenceDTO>> getSellerEvidence(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId
    ) {
        log.info("查询卖家证据: disputeId={}", disputeId);
        List<EvidenceDTO> evidence = evidenceService.getSellerEvidence(disputeId);
        return ApiResponse.success(evidence);
    }

    /**
     * 评估证据有效性（仲裁员）
     *
     * POST /api/disputes/evidence/{evidenceId}/evaluate
     *
     * @param evidenceId 证据ID
     * @param validity 有效性
     * @param reason 评估理由
     * @return 是否成功
     */
    @PostMapping("/{evidenceId}/evaluate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "评估证据有效性", description = "仲裁员对证据进行有效性评估")
    public ApiResponse<Boolean> evaluateEvidence(
            @Parameter(description = "证据ID", example = "1")
            @PathVariable Long evidenceId,
            @Parameter(description = "有效性", example = "VALID")
            @RequestParam EvidenceValidity validity,
            @Parameter(description = "评估理由", example = "证据真实有效")
            @RequestParam String reason
    ) {
        Long evaluatorId = SecurityUtil.getCurrentUserId();
        log.info("评估证据有效性: evidenceId={}, validity={}, evaluatorId={}",
                evidenceId, validity, evaluatorId);
        boolean result = evidenceService.evaluateEvidence(evidenceId, validity, reason, evaluatorId);
        return ApiResponse.success(result);
    }

    /**
     * 查询证据统计
     *
     * GET /api/disputes/{disputeId}/evidence/summary
     *
     * @param disputeId 纠纷ID
     * @return 证据统计信息
     */
    @GetMapping("/{disputeId}/summary")

    @Operation(summary = "查询证据统计", description = "查询纠纷的证据统计信息")
    public ApiResponse<EvidenceSummaryDTO> getEvidenceSummary(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId
    ) {
        log.info("查询证据统计: disputeId={}", disputeId);
        EvidenceSummaryDTO summary = evidenceService.getEvidenceSummary(disputeId);
        return ApiResponse.success(summary);
    }

    /**
     * 查询待评估证据（仲裁员）
     *
     * GET /api/disputes/{disputeId}/evidence/unevaluated
     *
     * @param disputeId 纠纷ID
     * @return 待评估证据列表
     */
    @GetMapping("/{disputeId}/unevaluated")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询待评估证据", description = "仲裁员查询待评估的证据列表")
    public ApiResponse<List<EvidenceDTO>> getUnevaluatedEvidence(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId
    ) {
        log.info("查询待评估证据: disputeId={}", disputeId);
        List<EvidenceDTO> evidence = evidenceService.getUnevaluatedEvidence(disputeId);
        return ApiResponse.success(evidence);
    }

    /**
     * 删除证据
     *
     * DELETE /api/disputes/evidence/{evidenceId}
     *
     * @param evidenceId 证据ID
     * @return 是否成功
     */
    @DeleteMapping("/{evidenceId}")
    @Operation(summary = "删除证据", description = "上传者删除自己上传的未评估证据")
    public ApiResponse<Boolean> deleteEvidence(
            @Parameter(description = "证据ID", example = "1")
            @PathVariable Long evidenceId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("删除证据: evidenceId={}, userId={}", evidenceId, userId);
        boolean result = evidenceService.deleteEvidence(evidenceId, userId);
        return ApiResponse.success(result);
    }
}
