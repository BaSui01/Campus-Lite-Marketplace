package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.annotation.RateLimit;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.ConversationResponse;
import com.campus.marketplace.common.dto.response.MessageResponse;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "私信发送、查询、未读消息数等接口")
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送消息
     *
     * 🚀 支持文本、图片、商品卡片等消息类型
     * ✅ 自动进行敏感词过滤
     * 💬 WebSocket实时推送给接收者
     *
     * @param request 发送消息请求
     * @return 消息ID
     */
    @Operation(summary = "发送消息", description = "发送私信给指定用户，支持文本/图片/商品卡片")
    @PostMapping("/send")
    @PreAuthorize("hasRole('USER')")
    @RateLimit(key = "message:send", maxRequests = 20, timeWindow = 60)
    public ApiResponse<Long> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        log.info("用户发送消息：username={}, receiverId={}, type={}",
                SecurityUtil.getCurrentUsername(), request.receiverId(), request.messageType());

        Long messageId = messageService.sendMessage(request);

        log.info("消息发送成功：messageId={}", messageId);
        return ApiResponse.success(messageId);
    }

    /**
     * 获取未读消息数
     *
     * 📊 返回当前用户的未读消息总数
     * ⚡ 优先从Redis读取，缓存未命中则查询数据库
     *
     * @return 未读消息数
     */
    @Operation(summary = "获取未读消息数", description = "获取当前用户的未读消息总数")
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Integer> getUnreadCount() {
        log.debug("查询未读消息数：username={}", SecurityUtil.getCurrentUsername());

        // MessageService内部会通过getCurrentUsername()查询userId
        int unreadCount = messageService.getUnreadCount();

        return ApiResponse.success(unreadCount);
    }

    /**
     * 查询会话列表
     *
     * 📋 返回当前用户的所有会话
     * 🔄 按最后消息时间倒序排列
     * 📈 包含每个会话的未读消息数
     *
     * @param page 页码（从0开始）
     * @param size 每页大小（默认20）
     * @return 会话列表（分页）
     */
    @Operation(summary = "查询会话列表", description = "获取当前用户的所有私信会话，按最后消息时间倒序")
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Page<ConversationResponse>> listConversations(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        log.info("查询会话列表：username={}, page={}, size={}",
                SecurityUtil.getCurrentUsername(), page, size);

        Page<ConversationResponse> conversations = messageService.listConversations(page, size);

        log.info("会话列表查询成功：total={}", conversations.getTotalElements());
        return ApiResponse.success(conversations);
    }

    /**
     * 查询聊天记录
     *
     * 💬 返回指定会话的消息历史
     * 🔒 自动验证权限（只能查看自己的会话）
     * ⏰ 按消息时间倒序排列
     *
     * @param conversationId 会话ID
     * @param page 页码（从0开始）
     * @param size 每页大小（默认50）
     * @return 消息列表（分页）
     */
    @Operation(summary = "查询聊天记录", description = "获取指定会话的消息历史，按时间倒序")
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Page<MessageResponse>> listMessages(
            @Parameter(description = "会话ID") @PathVariable Long conversationId,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "50") int size) {

        log.info("查询聊天记录：username={}, conversationId={}, page={}, size={}",
                SecurityUtil.getCurrentUsername(), conversationId, page, size);

        Page<MessageResponse> messages = messageService.listMessages(conversationId, page, size);

        log.info("聊天记录查询成功：conversationId={}, total={}",
                conversationId, messages.getTotalElements());
        return ApiResponse.success(messages);
    }

    /**
     * 标记会话消息为已读
     *
     * 📖 批量标记指定会话的所有未读消息为已读
     * 📉 自动更新未读消息数
     * 🔒 权限验证（只能标记自己的消息）
     *
     * @param conversationId 会话ID
     * @return 已读消息数量
     */
    @Operation(summary = "标记会话为已读", description = "批量标记指定会话的所有未读消息为已读")
    @PostMapping("/conversations/{conversationId}/mark-read")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Integer> markConversationAsRead(
            @Parameter(description = "会话ID") @PathVariable Long conversationId) {

        log.info("标记会话为已读：username={}, conversationId={}",
                SecurityUtil.getCurrentUsername(), conversationId);

        int count = messageService.markConversationAsRead(conversationId);

        log.info("会话已标记为已读：conversationId={}, count={}", conversationId, count);
        return ApiResponse.success(count);
    }

    /**
     * 撤回消息
     *
     * 🔙 撤回自己发送的消息
     * ⏰ 仅限2分钟内的消息
     * 📡 实时通知接收者
     *
     * @param messageId 消息ID
     * @return 成功响应
     */
    @Operation(summary = "撤回消息", description = "撤回自己发送的消息（2分钟内有效）")
    @PostMapping("/messages/{messageId}/recall")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Void> recallMessage(
            @Parameter(description = "消息ID") @PathVariable Long messageId) {

        log.info("撤回消息：username={}, messageId={}",
                SecurityUtil.getCurrentUsername(), messageId);

        messageService.recallMessage(messageId);

        log.info("消息已撤回：messageId={}", messageId);
        return ApiResponse.success(null);
    }
}
