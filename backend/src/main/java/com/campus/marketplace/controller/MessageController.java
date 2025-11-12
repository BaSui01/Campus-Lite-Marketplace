package com.campus.marketplace.controller;

import com.campus.marketplace.common.annotation.RateLimit;
import com.campus.marketplace.common.dto.MessageSearchHistory;
import com.campus.marketplace.common.dto.MessageSearchSuggestion;
import com.campus.marketplace.common.dto.MessageSearchStatistics;
import com.campus.marketplace.common.dto.request.MessageSearchRequest;
import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.ConversationResponse;
import com.campus.marketplace.common.dto.response.MessageResponse;
import com.campus.marketplace.common.dto.response.MessageSearchResponse;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 *
 * 提供私信发送、会话查询、聊天记录查询等功能
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "私信发送、查询、未读消息数等接口")
public class MessageController {

    private final MessageService messageService;

        @Operation(summary = "发送消息", description = "发送私信给指定用户，支持文本/图片/商品卡片")
    @PostMapping("/send")
    @PreAuthorize("hasRole('USER')")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SendMessageRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"receiverId\": 10086,
                                      \"messageType\": \"TEXT\",
                                      \"content\": \"你好，请问还在吗？\"
                                    }
                                    """
                    )
            )
    )
    @RateLimit(key = "message:send", maxRequests = 20, timeWindow = 60)
    public ApiResponse<Long> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        log.info("用户发送消息：username={}, receiverId={}, type={}",
                SecurityUtil.getCurrentUsername(), request.receiverId(), request.messageType());

        Long messageId = messageService.sendMessage(request);

        log.info("消息发送成功：messageId={}", messageId);
        return ApiResponse.success(messageId);
    }

        @Operation(summary = "获取未读消息数", description = "获取当前用户的未读消息总数")
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Integer> getUnreadCount() {
        log.debug("查询未读消息数：username={}", SecurityUtil.getCurrentUsername());

        // MessageService内部会通过getCurrentUsername()查询userId
        int unreadCount = messageService.getUnreadCount();

        return ApiResponse.success(unreadCount);
    }

        @Operation(summary = "查询会话列表", description = "获取当前用户的所有私信会话，按最后消息时间倒序")
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Page<ConversationResponse>> listConversations(
            @Parameter(description = "页码（从0开始）", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size) {

        log.info("查询会话列表：username={}, page={}, size={}",
                SecurityUtil.getCurrentUsername(), page, size);

        Page<ConversationResponse> conversations = messageService.listConversations(page, size);

        log.info("会话列表查询成功：total={}", conversations.getTotalElements());
        return ApiResponse.success(conversations);
    }

        @Operation(summary = "查询聊天记录", description = "获取指定会话的消息历史，按时间倒序")
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Page<MessageResponse>> listMessages(
            @Parameter(description = "会话ID", example = "20001") @PathVariable Long conversationId,
            @Parameter(description = "页码（从0开始）", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "50") @RequestParam(defaultValue = "50") int size) {

        log.info("查询聊天记录：username={}, conversationId={}, page={}, size={}",
                SecurityUtil.getCurrentUsername(), conversationId, page, size);

        Page<MessageResponse> messages = messageService.listMessages(conversationId, page, size);

        log.info("聊天记录查询成功：conversationId={}, total={}",
                conversationId, messages.getTotalElements());
        return ApiResponse.success(messages);
    }

        @Operation(summary = "标记会话为已读", description = "批量标记指定会话的所有未读消息为已读")
    @PostMapping("/conversations/{conversationId}/mark-read")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Integer> markConversationAsRead(
            @Parameter(description = "会话ID", example = "20001") @PathVariable Long conversationId) {

        log.info("标记会话为已读：username={}, conversationId={}",
                SecurityUtil.getCurrentUsername(), conversationId);

        int count = messageService.markConversationAsRead(conversationId);

        log.info("会话已标记为已读：conversationId={}, count={}", conversationId, count);
        return ApiResponse.success(count);
    }

        @Operation(summary = "撤回消息", description = "撤回自己发送的消息（1分钟内有效）")
    @PostMapping("/messages/{messageId}/recall")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> recallMessage(
            @Parameter(description = "消息ID", example = "30001") @PathVariable Long messageId) {

        log.info("撤回消息：username={}, messageId={}",
                SecurityUtil.getCurrentUsername(), messageId);

        messageService.recallMessage(messageId);

        log.info("消息已撤回：messageId={}", messageId);
        return ApiResponse.success(null);
    }

    // ==================== 消息搜索相关接口 ====================

    @Operation(summary = "搜索协商消息", description = "在纠纷协商中搜索聊天消息，支持多种筛选条件")
    @PostMapping("/search")
    @PreAuthorize("hasRole('USER')")
    @RateLimit(key = "message:search", maxRequests = 30, timeWindow = 60)
    public ApiResponse<Page<MessageSearchResponse>> searchMessages(@Valid @RequestBody MessageSearchRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        log.info("搜索消息：userId={}, disputeId={}, keyword={}",
                currentUserId, request.getDisputeId(), request.getKeyword());

        Page<MessageSearchResponse> result = messageService.searchMessages(request, currentUserId);

        log.info("消息搜索完成：userId={}, total={}", currentUserId, result.getTotalElements());
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取消息搜索建议", description = "根据输入提供智能搜索建议")
    @GetMapping("/search/suggestions")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<MessageSearchSuggestion>> getSearchSuggestions(
            @Parameter(description = "纠纷ID", required = true) @RequestParam Long disputeId,
            @Parameter(description = "关键词前缀") @RequestParam(required = false) String keyword,
            @Parameter(description = "建议类型 (keyword/user/date)") @RequestParam(required = false) String suggestionType,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") int limit) {

        Long currentUserId = SecurityUtil.getCurrentUserId();

        log.debug("获取搜索建议：userId={}, disputeId={}, keyword={}, type={}",
                currentUserId, disputeId, keyword, suggestionType);

        List<MessageSearchSuggestion> suggestions = messageService.getSearchSuggestions(
                disputeId, keyword, suggestionType, limit, currentUserId);

        return ApiResponse.success(suggestions);
    }

    @Operation(summary = "获取消息搜索历史", description = "获取用户的消息搜索历史记录")
    @GetMapping("/search/history")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<List<MessageSearchHistory>> getSearchHistory(
            @Parameter(description = "纠纷ID", required = true) @RequestParam Long disputeId,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "20") int limit) {

        Long currentUserId = SecurityUtil.getCurrentUserId();

        log.debug("获取搜索历史：userId={}, disputeId={}, limit={}",
                currentUserId, disputeId, limit);

        List<MessageSearchHistory> history = messageService.getSearchHistory(disputeId, limit, currentUserId);

        return ApiResponse.success(history);
    }

    @Operation(summary = "清空消息搜索历史", description = "清空用户在指定纠纷的消息搜索历史记录")
    @DeleteMapping("/search/history")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> clearSearchHistory(
            @Parameter(description = "纠纷ID", required = true) @RequestParam Long disputeId) {

        Long currentUserId = SecurityUtil.getCurrentUserId();

        log.info("清空搜索历史：userId={}, disputeId={}", currentUserId, disputeId);

        messageService.clearSearchHistory(disputeId, currentUserId);

        log.info("搜索历史已清空：userId={}, disputeId={}", currentUserId, disputeId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取消息搜索统计", description = "获取搜索相关的统计信息")
    @GetMapping("/search/statistics")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<MessageSearchStatistics> getSearchStatistics(
            @Parameter(description = "纠纷ID", required = true) @RequestParam Long disputeId) {

        Long currentUserId = SecurityUtil.getCurrentUserId();

        log.debug("获取搜索统计：userId={}, disputeId={}", currentUserId, disputeId);

        MessageSearchStatistics statistics = messageService.getSearchStatistics(disputeId, currentUserId);

        return ApiResponse.success(statistics);
    }
}
