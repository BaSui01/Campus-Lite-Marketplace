package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.ReviewReplyDTO;
import com.campus.marketplace.common.dto.request.CreateReviewReplyRequest;
import com.campus.marketplace.common.entity.ReviewReply;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.ReviewReplyService;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.common.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评价回复管理Controller
 *
 * Spec #7：回复功能API（卖家回复、管理员回复）
 *
 * @author BaSui 😎 - 卖家回复暖人心，管理员回复定纷争！
 * @since 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "评价回复管理", description = "卖家回复、管理员回复、已读标记接口")
public class ReviewReplyController {

    private final ReviewReplyService reviewReplyService;
    private final UserRepository userRepository;

    @PostMapping("/{reviewId}/replies")
    @Operation(summary = "创建评价回复", description = "卖家或管理员对评价进行回复")
    public ApiResponse<ReviewReplyDTO> createReply(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId,

            @Valid @RequestBody CreateReviewReplyRequest request
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        log.info("创建评价回复：reviewId={}, replierId={}, replyType={}", 
                reviewId, currentUserId, request.getReplyType());

        ReviewReply reply = reviewReplyService.createReply(
                reviewId, 
                currentUserId, 
                request.getReplyType(), 
                request.getContent(), 
                request.getTargetUserId()
        );

        ReviewReplyDTO dto = convertToDTO(reply);
        return ApiResponse.success(dto);
    }

    @GetMapping("/{reviewId}/replies")
    @Operation(summary = "获取评价的所有回复", description = "返回卖家回复和管理员回复列表，按创建时间升序")
    public ApiResponse<List<ReviewReplyDTO>> getReviewReplies(
            @Parameter(description = "评价ID", required = true)
            @PathVariable Long reviewId
    ) {
        List<ReviewReply> replyList = reviewReplyService.getReviewReplies(reviewId);
        List<ReviewReplyDTO> dtoList = replyList.stream()
                .map(this::convertToDTO)
                .toList();

        return ApiResponse.success(dtoList);
    }

    @GetMapping("/replies/unread")
    @Operation(summary = "获取当前用户的未读回复", description = "返回所有未读的回复列表")
    public ApiResponse<List<ReviewReplyDTO>> getUnreadReplies() {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        List<ReviewReply> replyList = reviewReplyService.getUnreadReplies(currentUserId);
        List<ReviewReplyDTO> dtoList = replyList.stream()
                .map(this::convertToDTO)
                .toList();

        return ApiResponse.success(dtoList);
    }

    @GetMapping("/replies/unread/count")
    @Operation(summary = "统计当前用户的未读回复数量", description = "返回未读回复数量")
    public ApiResponse<Long> countUnreadReplies() {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        long count = reviewReplyService.countUnreadReplies(currentUserId);
        return ApiResponse.success(count);
    }

    @PutMapping("/replies/{replyId}/read")
    @Operation(summary = "标记回复为已读", description = "将指定的回复标记为已读状态")
    public ApiResponse<Void> markReplyAsRead(
            @Parameter(description = "回复ID", required = true)
            @PathVariable Long replyId
    ) {
        log.info("标记回复为已读：replyId={}", replyId);
        reviewReplyService.markReplyAsRead(replyId);
        return ApiResponse.success();
    }

    @PutMapping("/replies/read/all")
    @Operation(summary = "标记所有回复为已读", description = "将当前用户的所有未读回复标记为已读")
    public ApiResponse<Void> markAllRepliesAsRead() {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        log.info("批量标记用户{}的所有回复为已读", currentUserId);
        reviewReplyService.markAllRepliesAsRead(currentUserId);
        return ApiResponse.success();
    }

    @DeleteMapping("/replies/{replyId}")
    @Operation(summary = "删除评价回复", description = "删除指定的回复（需管理员权限）")
    public ApiResponse<Void> deleteReply(
            @Parameter(description = "回复ID", required = true)
            @PathVariable Long replyId
    ) {
        log.info("删除评价回复：replyId={}", replyId);
        reviewReplyService.deleteReply(replyId);
        return ApiResponse.success();
    }

    /**
     * 实体转DTO（含用户信息查询）
     *
     * 从User表查询回复者的用户名和头像
     * 如果用户不存在，使用fallback值
     */
    private ReviewReplyDTO convertToDTO(ReviewReply reply) {
        // 查询用户信息
        User user = userRepository.findById(reply.getReplierId()).orElse(null);
        
        String username = (user != null) ? user.getUsername() : "用户" + reply.getReplierId();
        String avatar = (user != null) ? user.getAvatar() : null;
        
        return ReviewReplyDTO.builder()
                .id(reply.getId())
                .reviewId(reply.getReviewId())
                .replierId(reply.getReplierId())
                .replierUsername(username)
                .replierAvatar(avatar)
                .replyType(reply.getReplyType())
                .content(reply.getContent())
                .isRead(reply.getIsRead())
                .targetUserId(reply.getTargetUserId())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}
