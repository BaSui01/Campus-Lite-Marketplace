/**
 * ✅ 消息 API 服务 - 已重构为 OpenAPI
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 *
 * 功能：
 * - 会话管理（查询、创建、删除）
 * - 消息发送与查询
 * - 消息已读标记
 * - 消息撤回
 * - 未读消息数查询
 *
 * 📋 API 路径：/api/messages/*
 */

import { getApi } from '../utils/apiClient';
import type {
  SendMessageRequest,
  ConversationResponse,
  MessageResponse,
} from '../api/models';

/**
 * 会话列表查询参数
 */
export interface ConversationListParams {
  page?: number;
  size?: number;
}

/**
 * 消息列表查询参数
 */
export interface MessageListParams {
  conversationId: number;
  page?: number;
  size?: number;
}

/**
 * 消息 API 服务类
 */
export class MessageService {
  // ==================== 会话相关接口 ====================

  /**
   * 获取会话列表
   * GET /api/messages/conversations
   * @param params 查询参数
   * @returns 会话列表（分页）
   */
  async getConversations(params?: ConversationListParams) {
    const api = getApi();
    const response = await api.listConversations({
      page: params?.page,
      size: params?.size,
    });
    return response.data.data;
  }

  /**
   * 获取会话详情
   * GET /api/messages/conversations/{conversationId}
   * @param conversationId 会话ID
   * @returns 会话详情
   */
  async getConversationById(conversationId: number): Promise<ConversationResponse> {
    const api = getApi();
    const response = await api.getConversation({ conversationId });
    return response.data.data as ConversationResponse;
  }

  /**
   * 创建或获取会话
   * POST /api/messages/conversations
   * @param userId 对方用户ID
   * @returns 会话信息
   */
  async getOrCreateConversation(userId: number): Promise<ConversationResponse> {
    const api = getApi();
    const response = await api.createConversation({ createConversationRequest: { userId } });
    return response.data.data as ConversationResponse;
  }

  /**
   * 删除会话
   * DELETE /api/messages/conversations/{conversationId}
   * @param conversationId 会话ID
   */
  async deleteConversation(conversationId: number): Promise<void> {
    const api = getApi();
    await api.deleteConversation({ conversationId });
  }

  // ==================== 消息相关接口 ====================

  /**
   * 发送消息
   * POST /api/messages/send
   * @param data 发送消息请求参数
   * @returns 消息ID
   */
  async sendMessage(data: SendMessageRequest): Promise<number> {
    const api = getApi();
    const response = await api.sendMessage({ sendMessageRequest: data });
    return response.data.data as number;
  }

  /**
   * 获取消息列表
   * GET /api/messages/conversations/{conversationId}/messages
   * @param params 查询参数
   * @returns 消息列表（分页）
   */
  async getMessages(params: MessageListParams) {
    const api = getApi();
    const response = await api.listMessages({
      conversationId: params.conversationId,
      page: params.page,
      size: params.size,
    });
    return response.data.data;
  }

  /**
   * 标记会话所有消息已读
   * POST /api/messages/conversations/{conversationId}/mark-read
   * @param conversationId 会话ID
   * @returns 标记的消息数量
   */
  async markConversationAsRead(conversationId: number): Promise<number> {
    const api = getApi();
    const response = await api.markConversationAsRead({ conversationId });
    return response.data.data as number;
  }

  /**
   * 撤回消息
   * POST /api/messages/{messageId}/recall
   * @param messageId 消息ID
   */
  async recallMessage(messageId: number): Promise<void> {
    const api = getApi();
    await api.recallMessage({ messageId });
  }

  /**
   * 获取未读消息数
   * GET /api/messages/unread-count
   * @returns 未读消息数
   */
  async getUnreadCount(): Promise<number> {
    const api = getApi();
    const response = await api.getUnreadCount();
    return response.data.data as number;
  }
}

// 导出单例
export const messageService = new MessageService();
export default messageService;
