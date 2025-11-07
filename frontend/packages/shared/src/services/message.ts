/**
 * 消息 API 服务
 * @author BaSui 😎
 * @description 即时消息、会话管理等接口
 */

import { http } from '../utils/apiClient';
import type {
  ApiResponse,
  PageInfo,
  Message,
  Conversation,
  SendMessageRequest,
  ConversationListQuery,
  MessageListQuery,
  MarkMessageReadRequest,
} from '../types';

/**
 * 消息 API 服务类
 */
export class MessageService {
  // ==================== 会话相关接口 ====================

  /**
   * 获取会话列表
   * @param params 查询参数
   * @returns 会话列表
   */
  async getConversations(params?: ConversationListQuery): Promise<ApiResponse<PageInfo<Conversation>>> {
    return http.get('/conversations', { params });
  }

  /**
   * 获取会话详情
   * @param conversationId 会话ID
   * @returns 会话详情
   */
  async getConversationById(conversationId: number): Promise<ApiResponse<Conversation>> {
    return http.get(`/conversations/${conversationId}`);
  }

  /**
   * 创建或获取会话
   * @param userId 对方用户ID
   * @returns 会话信息
   */
  async getOrCreateConversation(userId: number): Promise<ApiResponse<Conversation>> {
    return http.post('/conversations', { userId });
  }

  /**
   * 删除会话
   * @param conversationId 会话ID
   * @returns 删除结果
   */
  async deleteConversation(conversationId: number): Promise<ApiResponse<void>> {
    return http.delete(`/conversations/${conversationId}`);
  }

  // ==================== 消息相关接口 ====================

  /**
   * 发送消息
   * @param data 发送消息请求参数
   * @returns 消息信息
   */
  async sendMessage(data: SendMessageRequest): Promise<ApiResponse<Message>> {
    return http.post('/messages', data);
  }

  /**
   * 获取消息列表
   * @param params 查询参数
   * @returns 消息列表
   */
  async getMessages(params: MessageListQuery): Promise<ApiResponse<PageInfo<Message>>> {
    return http.get('/messages', { params });
  }

  /**
   * 标记消息已读
   * @param data 标记消息已读请求参数
   * @returns 标记结果
   */
  async markMessagesAsRead(data: MarkMessageReadRequest): Promise<ApiResponse<void>> {
    return http.post('/messages/mark-read', data);
  }

  /**
   * 标记会话所有消息已读
   * @param conversationId 会话ID
   * @returns 标记结果
   */
  async markConversationAsRead(conversationId: number): Promise<ApiResponse<void>> {
    return http.post(`/messages/mark-read/conversation/${conversationId}`);
  }

  /**
   * 撤回消息
   * @param messageId 消息ID
   * @returns 撤回结果
   */
  async recallMessage(messageId: number): Promise<ApiResponse<void>> {
    return http.post(`/messages/${messageId}/recall`);
  }

  /**
   * 获取未读消息数
   * @returns 未读消息数
   */
  async getUnreadCount(): Promise<ApiResponse<number>> {
    return http.get('/messages/unread-count');
  }
}

// 导出单例
export const messageService = new MessageService();
export default messageService;
