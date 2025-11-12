/**
 * 纠纷协商通信Hook - 聊天连接专家！🔌
 *
 * @author BaSui 😎
 * @description 纠纷协商实时通信的封装Hook
 * @date 2025-11-07
 */

import { useState, useEffect, useCallback, useRef } from 'react';
import { useWebSocket, WebSocketReadyState } from '@campus/shared/hooks';
import { disputeService } from '../../services';

/**
 * 纠纷消息类型
 */
export interface DisputeMessage {
  id: string;
  disputeId: number;
  senderId: number;
  senderName: string;
  senderRole: 'buyer' | 'seller' | 'arbitrator';
  content: string;
  messageType: 'text' | 'image' | 'file';
  fileUrl?: string;
  fileName?: string;
  fileSize?: number;
  timestamp: string;
  isRead: boolean;
  isOwn: boolean;
}

/**
 * 聊天状态
 */
export interface ChatState {
  isConnected: boolean;
  isConnecting: boolean;
  reconnectCount: number;
  unreadCount: number;
  otherUserTyping: boolean;
  error: string | null;
}

/**
 * 聊天配置选项
 */
export interface UseDisputeChatOptions {
  /** 纠纷ID */
  disputeId: number;
  /** 当前用户ID */
  currentUserId: number;
  /** 消息接收回调 */
  onMessage?: (message: DisputeMessage) => void;
  /** 连接状态变化回调 */
  onConnectionChange?: (isConnected: boolean) => void;
  /** 错误处理回调 */
  onError?: (error: string) => void;
  /** 自动重连 */
  autoReconnect?: boolean;
}

/**
 * 发送消息请求
 */
export interface SendMessageRequest {
  content: string;
  messageType: 'text' | 'image' | 'file' | 'emoji';
  fileUrl?: string;
  fileName?: string;
  fileSize?: number;
  emojiId?: string;
  emojiName?: string;
  emojiPackId?: string;
}

/**
 * 纠纷协商通信Hook
 */
export const useDisputeChat = (options: UseDisputeChatOptions) => {
  const {
    disputeId,
    currentUserId,
    onMessage,
    onConnectionChange,
    onError,
    autoReconnect = true,
  } = options;

  // 消息列表
  const [messages, setMessages] = useState<DisputeMessage[]>([]);

  // 聊天状态
  const [chatState, setChatState] = useState<ChatState>({
    isConnected: false,
    isConnecting: true,
    reconnectCount: 0,
    unreadCount: 0,
    otherUserTyping: false,
    error: null,
  });

  // 输入状态管理
  const [typingUsers, setTypingUsers] = useState<Set<number>>(new Set());
  const typingTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // WebSocket连接
  // 🔧 BaSui: 修正WebSocket URL - 后端端点是 /ws/dispute（不带disputeId参数）
  const { lastMessage, send, readyState, reconnectCount } = useWebSocket(
    `${process.env.REACT_APP_WS_URL || 'ws://localhost:8200/api'}/ws/dispute`,
    {
      onOpen: () => {
        setChatState(prev => ({
          ...prev,
          isConnected: true,
          isConnecting: false,
          error: null,
        }));
        onConnectionChange?.(true);
        console.log('纠纷聊天室已连接');
      },
      onClose: () => {
        setChatState(prev => ({
          ...prev,
          isConnected: false,
          isConnecting: false,
        }));
        onConnectionChange?.(false);
        console.log('纠纷聊天室已断开');
      },
      onError: () => {
        const errorMessage = '聊天室连接错误';
        setChatState(prev => ({
          ...prev,
          isConnected: false,
          isConnecting: false,
          error: errorMessage,
        }));
        onError?.(errorMessage);
      },
      onMessage: (data) => {
        handleWebSocketMessage(data);
      },
      reconnect: autoReconnect,
      heartbeatInterval: 30000,
      heartbeatMessage: JSON.stringify({ type: 'HEARTBEAT' }), // 🔧 统一使用后端定义的类型常量
    }
  );

  /**
   * 处理WebSocket消息
   */
  const handleWebSocketMessage = useCallback((data: any) => {
    try {
      switch (data.type) {
        case 'message':
          handleNewMessage(data.payload);
          break;
        case 'typing':
          handleTypingEvent(data.payload);
          break;
        case 'read_receipt':
          handleReadReceipt(data.payload);
          break;
        case 'HEARTBEAT': // 🔧 统一使用后端定义的类型常量
          // 心跳消息，不需要处理
          break;
        case 'SYSTEM': // 🔧 处理系统消息
          console.log('系统消息:', data.content);
          break;
        case 'ERROR': // 🔧 统一使用后端定义的错误类型
          handleError(data.payload || data.content);
          break;
        case 'status_update':
          handleStatusUpdate(data.payload);
          break;
        default:
          console.log('未知消息类型:', data.type);
      }
    } catch (error) {
      console.error('处理WebSocket消息失败:', error);
    }
  }, [currentUserId, onMessage]);

  /**
   * 处理新消息
   */
  const handleNewMessage = useCallback((messageData: any) => {
    const message: DisputeMessage = {
      id: messageData.id,
      disputeId: messageData.disputeId,
      senderId: messageData.senderId,
      senderName: messageData.senderName,
      senderRole: messageData.senderRole,
      content: messageData.content,
      messageType: messageData.messageType || 'text',
      fileUrl: messageData.fileUrl,
      fileName: messageData.fileName,
      fileSize: messageData.fileSize,
      timestamp: messageData.timestamp,
      isRead: messageData.isRead,
      isOwn: messageData.senderId === currentUserId,
    };

    setMessages(prev => [...prev, message]);

    // 如果不是自己的消息，增加未读计数
    if (!message.isOwn) {
      setChatState(prev => ({
        ...prev,
        unreadCount: prev.unreadCount + 1,
      }));
    }

    onMessage?.(message);
  }, [currentUserId, onMessage]);

  /**
   * 处理输入状态事件
   */
  const handleTypingEvent = useCallback((payload: any) => {
    const { userId, isTyping } = payload;

    if (userId === currentUserId) return;

    setTypingUsers(prev => {
      const newTypingUsers = new Set(prev);
      if (isTyping) {
        newTypingUsers.add(userId);
      } else {
        newTypingUsers.delete(userId);
      }
      return newTypingUsers;
    });

    // 设置输入状态超时
    if (isTyping) {
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }
      typingTimeoutRef.current = setTimeout(() => {
        setTypingUsers(prev => {
          const newTypingUsers = new Set(prev);
          newTypingUsers.delete(userId);
          return newTypingUsers;
        });
      }, 3000);
    }
  }, [currentUserId]);

  /**
   * 处理已读回执
   */
  const handleReadReceipt = useCallback((payload: any) => {
    const { messageId, userId } = payload;

    setMessages(prev => prev.map(msg =>
      msg.id === messageId ? { ...msg, isRead: true } : msg
    ));
  }, []);

  /**
   * 处理错误
   */
  const handleError = useCallback((payload: any) => {
    const errorMessage = payload.message || '未知错误';
    setChatState(prev => ({
      ...prev,
      error: errorMessage,
    }));
    onError?.(errorMessage);
  }, [onError]);

  /**
   * 处理状态更新
   */
  const handleStatusUpdate = useCallback((payload: any) => {
    // 处理纠纷状态更新
    console.log('纠纷状态更新:', payload);
  }, []);

  /**
   * 发送消息
   */
  const sendMessage = useCallback(async (request: SendMessageRequest): Promise<boolean> => {
    if (readyState !== WebSocketReadyState.OPEN) {
      const error = '连接已断开，无法发送消息';
      onError?.(error);
      return false;
    }

    try {
      const messageData = {
        type: 'message',
        payload: {
          disputeId,
          ...request,
        },
      };

      send(messageData);
      return true;
    } catch (error) {
      console.error('发送消息失败:', error);
      onError?.('发送消息失败');
      return false;
    }
  }, [disputeId, readyState, send, onError]);

  /**
   * 发送输入状态
   */
  const sendTypingStatus = useCallback((isTyping: boolean) => {
    if (readyState !== WebSocketReadyState.OPEN) return;

    const messageData = {
      type: 'typing',
      payload: {
        isTyping,
      },
    };

    send(messageData);
  }, [readyState, send]);

  /**
   * 发送已读回执
   */
  const sendReadReceipt = useCallback((messageId: string) => {
    if (readyState !== WebSocketReadyState.OPEN) return;

    const messageData = {
      type: 'read_receipt',
      payload: {
        messageId,
      },
    };

    send(messageData);
  }, [readyState, send]);

  /**
   * 撤回消息
   */
  const recallMessage = useCallback(async (messageId: string): Promise<boolean> => {
    try {
      // 这里需要调用实际的API撤回消息
      // const success = await disputeService.recallMessage(parseInt(messageId));

      // 模拟API调用
      const success = true;

      if (success) {
        // 发送WebSocket通知撤回消息
        if (readyState === WebSocketReadyState.OPEN) {
          const messageData = {
            type: 'message_recall',
            payload: {
              messageId,
              senderId: currentUserId,
            },
          };
          send(messageData);
        }

        // 更新本地消息状态为已撤回
        setMessages(prev => prev.map(msg =>
          msg.id === messageId
            ? { ...msg, isRecalled: true, content: '[消息已撤回]' }
            : msg
        ));
      }

      return success;
    } catch (error) {
      console.error('撤回消息失败:', error);
      onError?.('撤回消息失败');
      return false;
    }
  }, [readyState, send, currentUserId, onError]);

  /**
   * 加载历史消息
   */
  const loadHistoryMessages = useCallback(async () => {
    try {
      // 这里需要调用实际的API获取历史消息
      // const historyMessages = await disputeService.getDisputeMessages(disputeId);
      // setMessages(historyMessages);

      // 临时模拟数据
      const mockMessages: DisputeMessage[] = [
        {
          id: '1',
          disputeId,
          senderId: 2,
          senderName: '张三',
          senderRole: 'buyer',
          content: '你好，关于这个纠纷我想和你协商一下',
          messageType: 'text',
          timestamp: new Date(Date.now() - 3600000).toISOString(),
          isRead: true,
          isOwn: false,
        },
        {
          id: '2',
          disputeId,
          senderId: currentUserId,
          senderName: '我',
          senderRole: 'seller',
          content: '好的，我也希望尽快解决这个问题',
          messageType: 'text',
          timestamp: new Date(Date.now() - 3000000).toISOString(),
          isRead: true,
          isOwn: true,
        },
      ];
      setMessages(mockMessages);
    } catch (error) {
      console.error('加载历史消息失败:', error);
      onError?.('加载历史消息失败');
    }
  }, [disputeId, currentUserId, onError]);

  /**
   * 清空未读计数
   */
  const clearUnreadCount = useCallback(() => {
    setChatState(prev => ({
      ...prev,
      unreadCount: 0,
    }));
  }, []);

  /**
   * 重连
   */
  const reconnect = useCallback(() => {
    // WebSocket会自动重连，这里只是更新状态
    setChatState(prev => ({
      ...prev,
      isConnecting: true,
    }));
  }, []);

  // 初始化加载历史消息
  useEffect(() => {
    loadHistoryMessages();
  }, [disputeId]);

  // 更新连接状态
  useEffect(() => {
    setChatState(prev => ({
      ...prev,
      isConnected: readyState === WebSocketReadyState.OPEN,
      isConnecting: readyState === WebSocketReadyState.CONNECTING,
      reconnectCount,
    }));
  }, [readyState, reconnectCount]);

  // 清理定时器
  useEffect(() => {
    return () => {
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }
    };
  }, []);

  return {
    // 消息相关
    messages,
    sendMessage,
    sendTypingStatus,
    sendReadReceipt,
    recallMessage,
    loadHistoryMessages,
    clearUnreadCount,

    // 状态相关
    chatState,
    isConnected: chatState.isConnected,
    isConnecting: chatState.isConnecting,
    error: chatState.error,
    unreadCount: chatState.unreadCount,
    otherUserTyping: typingUsers.size > 0,
    typingUsers: Array.from(typingUsers),

    // 连接控制
    reconnect,
  };
};

export default useDisputeChat;