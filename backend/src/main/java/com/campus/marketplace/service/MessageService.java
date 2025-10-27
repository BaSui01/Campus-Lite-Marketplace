package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.dto.response.ConversationResponse;
import com.campus.marketplace.common.dto.response.MessageResponse;
import org.springframework.data.domain.Page;

/**
 * 消息服务接口
 *
 * 提供私信发送、查询等功能
 *
 * @author BaSui
 * @date 2025-10-27
 */
public interface MessageService {

    /**
     * 发送消息
     *
     * @param request 发送消息请求
     * @return 消息ID
     */
    Long sendMessage(SendMessageRequest request);

    /**
     * 获取当前登录用户的未读消息数
     *
     * 从SecurityContext获取当前用户，无需传参
     *
     * @return 未读消息数
     */
    int getUnreadCount();

    /**
     * 获取指定用户的未读消息数
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    int getUnreadCount(Long userId);

    /**
     * 查询会话列表
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 会话列表
     */
    Page<ConversationResponse> listConversations(int page, int size);

    /**
     * 查询聊天记录
     *
     * @param conversationId 会话ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 消息列表
     */
    Page<MessageResponse> listMessages(Long conversationId, int page, int size);

    /**
     * 标记会话消息为已读
     *
     * 将指定会话中所有未读消息标记为已读
     *
     * @param conversationId 会话ID
     * @return 已读消息数量
     */
    int markConversationAsRead(Long conversationId);

    /**
     * 撤回消息
     *
     * 只能撤回2分钟内的消息，且只能撤回自己发送的消息
     *
     * @param messageId 消息ID
     */
    void recallMessage(Long messageId);
}
