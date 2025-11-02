/**
 * useChatMessage Hook - 聊天消息订阅大师！💬
 * @author BaSui 😎
 * @description 订阅 WebSocket 聊天消息，提供消息列表和发送功能
 */

import { useState, useEffect, useCallback } from 'react';
import { websocketService, type ChatMessageData } from '../utils/websocket';
import type { Message } from '../types';

/**
 * useChatMessage 配置选项
 */
export interface UseChatMessageOptions {
  /**
   * 会话ID（可选，用于过滤消息）
   */
  conversationId?: number;

  /**
   * 收到新消息回调
   */
  onNewMessage?: (message: Message) => void;

  /**
   * 是否自动订阅
   * @default true
   */
  autoSubscribe?: boolean;
}

/**
 * useChatMessage 返回值
 */
export interface UseChatMessageResult {
  /**
   * 消息列表
   */
  messages: Message[];

  /**
   * 最新消息
   */
  lastMessage: Message | null;

  /**
   * 发送消息
   */
  sendMessage: (data: ChatMessageData) => void;

  /**
   * 清空消息列表
   */
  clearMessages: () => void;

  /**
   * 手动订阅
   */
  subscribe: () => void;

  /**
   * 手动取消订阅
   */
  unsubscribe: () => void;
}

/**
 * useChatMessage Hook
 *
 * @description
 * 订阅 WebSocket 聊天消息，提供消息列表管理和发送功能。
 * 可以通过 conversationId 过滤特定会话的消息。
 *
 * @param options 配置选项
 * @returns 聊天消息订阅结果
 *
 * @example
 * ```tsx
 * // 基础用法
 * function ChatRoom() {
 *   const { messages, sendMessage } = useChatMessage({
 *     conversationId: 123,
 *     onNewMessage: (message) => {
 *       console.log('收到新消息:', message);
 *     },
 *   });
 *
 *   const handleSend = () => {
 *     sendMessage({
 *       conversationId: 123,
 *       content: 'Hello!',
 *       messageType: 'text',
 *       receiver: 456,
 *     });
 *   };
 *
 *   return (
 *     <div>
 *       {messages.map((msg) => (
 *         <div key={msg.id}>{msg.content}</div>
 *       ))}
 *       <Button onClick={handleSend}>发送</Button>
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 手动订阅/取消订阅
 * function ChatWindow() {
 *   const { messages, subscribe, unsubscribe } = useChatMessage({
 *     autoSubscribe: false,
 *   });
 *
 *   useEffect(() => {
 *     // 进入聊天页面时订阅
 *     subscribe();
 *
 *     // 离开聊天页面时取消订阅
 *     return () => {
 *       unsubscribe();
 *     };
 *   }, [subscribe, unsubscribe]);
 *
 *   return (
 *     <div>
 *       {messages.map((msg) => (
 *         <div key={msg.id}>{msg.content}</div>
 *       ))}
 *     </div>
 *   );
 * }
 * ```
 */
export const useChatMessage = (
  options: UseChatMessageOptions = {}
): UseChatMessageResult => {
  const { conversationId, onNewMessage, autoSubscribe = true } = options;

  // 消息列表
  const [messages, setMessages] = useState<Message[]>([]);

  // 最新消息
  const [lastMessage, setLastMessage] = useState<Message | null>(null);

  /**
   * 处理收到的消息
   */
  const handleMessage = useCallback(
    (message: Message) => {
      // 如果指定了会话ID，只处理该会话的消息
      if (conversationId !== undefined && message.conversationId !== conversationId) {
        return;
      }

      // 更新消息列表
      setMessages((prev) => [...prev, message]);
      setLastMessage(message);

      // 触发回调
      onNewMessage?.(message);
    },
    [conversationId, onNewMessage]
  );

  /**
   * 发送消息
   */
  const sendMessage = useCallback((data: ChatMessageData) => {
    websocketService.sendChatMessage(data);
  }, []);

  /**
   * 清空消息列表
   */
  const clearMessages = useCallback(() => {
    setMessages([]);
    setLastMessage(null);
  }, []);

  /**
   * 订阅聊天消息
   */
  const subscribe = useCallback(() => {
    websocketService.onChatMessage(handleMessage);
  }, [handleMessage]);

  /**
   * 取消订阅聊天消息
   */
  const unsubscribe = useCallback(() => {
    websocketService.offChatMessage(handleMessage);
  }, [handleMessage]);

  /**
   * 自动订阅/取消订阅
   */
  useEffect(() => {
    if (autoSubscribe) {
      subscribe();

      // 清理函数
      return () => {
        unsubscribe();
      };
    }
  }, [autoSubscribe, subscribe, unsubscribe]);

  return {
    messages,
    lastMessage,
    sendMessage,
    clearMessages,
    subscribe,
    unsubscribe,
  };
};

export default useChatMessage;
