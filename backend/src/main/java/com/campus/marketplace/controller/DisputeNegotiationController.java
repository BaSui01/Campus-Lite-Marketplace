package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.NegotiationMessageDTO;
import com.campus.marketplace.common.dto.request.ProposeDisputeRequest;
import com.campus.marketplace.common.dto.request.RespondProposalRequest;
import com.campus.marketplace.common.dto.request.SendNegotiationRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.DisputeNegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 纠纷协商控制器
 *
 * 提供协商消息发送、解决方案提议与响应等 REST API
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/disputes/negotiations")
@RequiredArgsConstructor
@Tag(name = "纠纷协商", description = "协商消息、解决方案提议与响应")
public class DisputeNegotiationController {

    private final DisputeNegotiationService negotiationService;

    /**
     * 发送协商消息
     *
     * POST /api/disputes/negotiations/messages
     *
     * @param request 消息请求
     * @return 消息ID
     */
    @PostMapping("/messages")
    @Operation(summary = "发送协商消息", description = "买卖双方发送文字消息进行沟通")
    public ApiResponse<Long> sendMessage(@Valid @RequestBody SendNegotiationRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("发送协商消息: disputeId={}, userId={}", request.disputeId(), userId);
        Long messageId = negotiationService.sendTextMessage(request, userId);
        return ApiResponse.success(messageId);
    }

    /**
     * 提出解决方案
     *
     * POST /api/disputes/negotiations/proposals
     *
     * @param request 方案请求
     * @return 方案ID
     */
    @PostMapping("/proposals")
    @Operation(summary = "提出解决方案", description = "买卖双方提出纠纷解决方案（包含退款金额）")
    public ApiResponse<Long> proposeResolution(@Valid @RequestBody ProposeDisputeRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("提出解决方案: disputeId={}, userId={}, refund={}",
                request.getDisputeId(), userId, request.getProposedRefundAmount());
        Long proposalId = negotiationService.proposeResolution(request, userId);
        return ApiResponse.success(proposalId);
    }

    /**
     * 响应解决方案
     *
     * POST /api/disputes/negotiations/proposals/{proposalId}/respond
     *
     * @param proposalId 方案ID
     * @param request 响应请求
     * @return 是否成功
     */
    @PostMapping("/proposals/{proposalId}/respond")
    @Operation(summary = "响应解决方案", description = "接受或拒绝对方提出的解决方案")
    public ApiResponse<Boolean> respondToProposal(
            @Parameter(description = "方案ID", example = "1")
            @PathVariable Long proposalId,
            @Valid @RequestBody RespondProposalRequest request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 设置proposalId到request中
        request.setProposalId(proposalId);
        log.info("响应解决方案: proposalId={}, userId={}, accepted={}",
                proposalId, userId, request.getAccepted());
        boolean result = negotiationService.respondToProposal(request, userId);
        return ApiResponse.success(result);
    }

    /**
     * 查询协商历史
     *
     * GET /api/disputes/{disputeId}/negotiations/history
     *
     * @param disputeId 纠纷ID
     * @return 协商历史列表
     */
    @GetMapping("/{disputeId}/history")
    @Operation(summary = "查询协商历史", description = "查询纠纷的所有协商消息和方案")
    public ApiResponse<List<NegotiationMessageDTO>> getNegotiationHistory(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId
    ) {
        log.info("查询协商历史: disputeId={}", disputeId);
        List<NegotiationMessageDTO> history = negotiationService.getNegotiationHistory(disputeId);
        return ApiResponse.success(history);
    }

    /**
     * 查询待响应方案
     *
     * GET /api/disputes/{disputeId}/negotiations/pending-proposal
     *
     * @param disputeId 纠纷ID
     * @return 待响应方案
     */
    @GetMapping("/{disputeId}/pending-proposal")
    @Operation(summary = "查询待响应方案", description = "查询纠纷当前待响应的解决方案")
    public ApiResponse<Optional<NegotiationMessageDTO>> getPendingProposal(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId
    ) {
        log.info("查询待响应方案: disputeId={}", disputeId);
        Optional<NegotiationMessageDTO> proposal = negotiationService.getPendingProposal(disputeId);
        return ApiResponse.success(proposal);
    }

    /**
     * 查询已接受方案
     *
     * GET /api/disputes/{disputeId}/negotiations/accepted-proposal
     *
     * @param disputeId 纠纷ID
     * @return 已接受方案
     */
    @GetMapping("/{disputeId}/accepted-proposal")
    @Operation(summary = "查询已接受方案", description = "查询纠纷已接受的解决方案")
    public ApiResponse<Optional<NegotiationMessageDTO>> getAcceptedProposal(
            @Parameter(description = "纠纷ID", example = "1")
            @PathVariable Long disputeId
    ) {
        log.info("查询已接受方案: disputeId={}", disputeId);
        Optional<NegotiationMessageDTO> proposal = negotiationService.getAcceptedProposal(disputeId);
        return ApiResponse.success(proposal);
    }
}
