package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.MessageSearchHistory;
import com.campus.marketplace.common.dto.MessageSearchSuggestion;
import com.campus.marketplace.common.dto.MessageSearchStatistics;
import com.campus.marketplace.common.dto.request.MessageSearchRequest;
import com.campus.marketplace.common.dto.request.SendMessageRequest;
import com.campus.marketplace.common.dto.response.ConversationResponse;
import com.campus.marketplace.common.dto.response.MessageResponse;
import com.campus.marketplace.common.dto.response.MessageSearchResponse;
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

    /**
     * 搜索协商消息
     *
     * @param request 搜索请求
     * @param currentUserId 当前用户ID
     * @return 搜索结果
     */
    Page<MessageSearchResponse> searchMessages(MessageSearchRequest request, Long currentUserId);

    /**
     * 获取搜索建议
     *
     * @param disputeId 纠纷ID
     * @param keyword 关键词
     * @param type 建议类型
     * @param limit 返回数量限制
     * @param currentUserId 当前用户ID
     * @return 搜索建议列表
     */
    java.util.List<MessageSearchSuggestion> getSearchSuggestions(
            Long disputeId, String keyword, String type, int limit, Long currentUserId);

    /**
     * 获取搜索历史
     *
     * @param disputeId 纠纷ID
     * @param limit 返回数量限制
     * @param currentUserId 当前用户ID
     * @return 搜索历史列表
     */
    java.util.List<MessageSearchHistory> getSearchHistory(Long disputeId, int limit, Long currentUserId);

    /**
     * 清空搜索历史
     *
     * @param disputeId 纠纷ID
     * @param currentUserId 当前用户ID
     */
    void clearSearchHistory(Long disputeId, Long currentUserId);

    /**
     * 获取搜索统计
     *
     * @param disputeId 纠纷ID
     * @param currentUserId 当前用户ID
     * @return 搜索统计信息
     */
    MessageSearchStatistics getSearchStatistics(Long disputeId, Long currentUserId);
}
