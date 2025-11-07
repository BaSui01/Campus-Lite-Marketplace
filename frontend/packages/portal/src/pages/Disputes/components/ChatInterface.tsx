/**
 * 纠纷协商沟通界面 - 实时聊天大师！💬
 *
 * @author BaSui 😎
 * @description 纠纷双方实时沟通界面，支持文字、图片、文件消息
 * @date 2025-11-07
 */

import React, { useState, useEffect, useRef } from 'react';
import { useWebSocket, WebSocketReadyState } from '@campus/shared/hooks';
import { disputeService } from '../../../services';
import { MessageList } from './MessageList';

/**
 * 消息类型
 */
export interface DisputeMessage {
  id: string;
  disputeId: number;
  senderId: number;
  senderName: string;
  senderRole: 'buyer' | 'seller' | 'arbitrator';
  content: string;
  messageType: 'text' | 'image' | 'file' | 'emoji';
  fileUrl?: string;
  fileName?: string;
  fileSize?: number;
  timestamp: string;
  isRead: boolean;
  isOwn: boolean;
  isRecalled?: boolean; // 是否已撤回
  recalledAt?: string; // 撤回时间
}

/**
 * 聊天界面属性
 */
export interface ChatInterfaceProps {
  /** 纠纷ID */
  disputeId: number;
  /** 当前用户ID */
  currentUserId: number;
  /** 当前用户角色 */
  currentUserRole: 'buyer' | 'seller' | 'arbitrator';
  /** 对方用户信息 */
  otherUser: {
    id: number;
    name: string;
    role: 'buyer' | 'seller' | 'arbitrator';
    avatar?: string;
  };
  /** 纠纷状态 */
  disputeStatus: string;
  /** 消息发送回调 */
  onMessageSent?: (message: DisputeMessage) => void;
  /** 自定义样式类名 */
  className?: string;
}

/**
 * 格式化时间
 */
const formatTime = (timestamp: string): string => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return '刚刚';
  if (diffMins < 60) return `${diffMins}分钟前`;
  if (diffHours < 24) return `${diffHours}小时前`;
  if (diffDays < 7) return `${diffDays}天前`;

  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

/**
 * 获取角色标签样式
 */
const getRoleBadgeStyle = (role: string): string => {
  switch (role) {
    case 'buyer':
      return 'bg-blue-100 text-blue-800';
    case 'seller':
      return 'bg-green-100 text-green-800';
    case 'arbitrator':
      return 'bg-purple-100 text-purple-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

/**
 * 获取角色标签文本
 */
const getRoleLabelText = (role: string): string => {
  switch (role) {
    case 'buyer':
      return '买家';
    case 'seller':
      return '卖家';
    case 'arbitrator':
      return '仲裁员';
    default:
      return '用户';
  }
};

/**
 * 纠纷协商沟通界面组件
 */
export const ChatInterface: React.FC<ChatInterfaceProps> = ({
  disputeId,
  currentUserId,
  currentUserRole,
  otherUser,
  disputeStatus,
  onMessageSent,
  className = '',
}) => {
  // 消息列表
  const [messages, setMessages] = useState<DisputeMessage[]>([]);

  // 输入框内容
  const [inputValue, setInputValue] = useState('');

  // 输入状态
  const [isTyping, setIsTyping] = useState(false);

  // 对方输入状态
  const [otherUserTyping, setOtherUserTyping] = useState(false);

  // 连接状态
  const [connectionStatus, setConnectionStatus] = useState<'connecting' | 'connected' | 'disconnected'>('connecting');

  // 文件上传状态
  const [uploadingFile, setUploadingFile] = useState(false);

  // 消息列表引用
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  // 搜索相关状态
  const [searchKeyword, setSearchKeyword] = useState<string>('');
  const [highlightedMessageId, setHighlightedMessageId] = useState<string>('');
  const [highlightedMessageIds, setHighlightedMessageIds] = useState<string[]>([]);

  // WebSocket 连接
  const { lastMessage, send, readyState, reconnectCount } = useWebSocket(
    `${process.env.REACT_APP_WS_URL || 'ws://localhost:8080'}/ws/disputes/${disputeId}/chat`,
    {
      onOpen: () => {
        setConnectionStatus('connected');
        console.log('纠纷聊天室已连接');
      },
      onClose: () => {
        setConnectionStatus('disconnected');
        console.log('纠纷聊天室已断开');
      },
      onError: () => {
        setConnectionStatus('disconnected');
        console.error('纠纷聊天室连接错误');
      },
      onMessage: (data) => {
        handleWebSocketMessage(data);
      },
      heartbeatInterval: 30000,
      heartbeatMessage: JSON.stringify({ type: 'ping' }),
    }
  );

  /**
   * 处理WebSocket消息
   */
  const handleWebSocketMessage = (data: any) => {
    try {
      switch (data.type) {
        case 'message':
          handleNewMessage(data.payload);
          break;
        case 'typing':
          if (data.userId !== currentUserId) {
            setOtherUserTyping(data.isTyping);
          }
          break;
        case 'ping':
          // 心跳消息，不需要处理
          break;
        case 'error':
          console.error('聊天室错误:', data.message);
          break;
        default:
          console.log('未知消息类型:', data.type);
      }
    } catch (error) {
      console.error('处理WebSocket消息失败:', error);
    }
  };

  /**
   * 处理新消息
   */
  const handleNewMessage = (messageData: any) => {
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
    onMessageSent?.(message);
  };

  /**
   * 加载历史消息
   */
  const loadHistoryMessages = async () => {
    try {
      // 这里需要调用实际的API获取历史消息
      // const historyMessages = await disputeService.getDisputeMessages(disputeId);
      // setMessages(historyMessages);

      // 临时模拟数据
      const mockMessages: DisputeMessage[] = [
        {
          id: '1',
          disputeId,
          senderId: otherUser.id,
          senderName: otherUser.name,
          senderRole: otherUser.role,
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
          senderRole: currentUserRole,
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
    }
  };

  /**
   * 发送消息
   */
  const sendMessage = () => {
    if (!inputValue.trim() || readyState !== WebSocketReadyState.OPEN) {
      return;
    }

    const messageData = {
      type: 'message',
      payload: {
        disputeId,
        content: inputValue.trim(),
        messageType: 'text',
      },
    };

    send(messageData);
    setInputValue('');
    setIsTyping(false);
  };

  /**
   * 处理键盘事件
   */
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  /**
   * 处理输入变化
   */
  const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setInputValue(e.target.value);

    // 发送输入状态
    if (!isTyping && e.target.value.trim()) {
      setIsTyping(true);
      send({
        type: 'typing',
        payload: { isTyping: true },
      });
    } else if (isTyping && !e.target.value.trim()) {
      setIsTyping(false);
      send({
        type: 'typing',
        payload: { isTyping: false },
      });
    }
  };

  /**
   * 处理文件上传
   */
  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    setUploadingFile(true);
    try {
      for (const file of files) {
        // 这里需要实现文件上传逻辑
        // const uploadedFile = await disputeService.uploadDisputeMessageFile(disputeId, file);

        const messageData = {
          type: 'message',
          payload: {
            disputeId,
            content: file.name,
            messageType: file.type.startsWith('image/') ? 'image' : 'file',
            fileUrl: 'temp_url', // 实际应该是上传后的URL
            fileName: file.name,
            fileSize: file.size,
          },
        };

        send(messageData);
      }
    } catch (error) {
      console.error('文件上传失败:', error);
    } finally {
      setUploadingFile(false);
    }
  };

  /**
   * 滚动到底部
   */
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // 初始化加载历史消息
  useEffect(() => {
    loadHistoryMessages();
  }, [disputeId]);

  // 自动滚动到底部
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // 监听搜索高亮事件
  useEffect(() => {
    const handleHighlightMessage = (event: CustomEvent) => {
      const { messageId, keyword } = event.detail;
      setHighlightedMessageId(messageId);
      setSearchKeyword(keyword);
      setHighlightedMessageIds([messageId]);

      // 5秒后清除高亮
      setTimeout(() => {
        setHighlightedMessageId('');
        setSearchKeyword('');
        setHighlightedMessageIds([]);
      }, 5000);
    };

    document.addEventListener('highlightMessage', handleHighlightMessage as EventListener);
    return () => {
      document.removeEventListener('highlightMessage', handleHighlightMessage as EventListener);
    };
  }, []);

  // 检查是否可以发送消息
  const canSendMessage = disputeStatus === 'NEGOTIATING' &&
                       (currentUserRole === 'buyer' || currentUserRole === 'seller');

  return (
    <div className={`chat-interface flex flex-col h-full ${className}`}>
      {/* 聊天头部 */}
      <div className="flex-shrink-0 border-b border-gray-200 p-4 bg-white">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            {/* 对方头像 */}
            <div className="w-10 h-10 rounded-full bg-gray-300 flex items-center justify-center">
              {otherUser.avatar ? (
                <img src={otherUser.avatar} alt={otherUser.name} className="w-10 h-10 rounded-full" />
              ) : (
                <span className="text-gray-600 font-medium">
                  {otherUser.name.charAt(0).toUpperCase()}
                </span>
              )}
            </div>

            {/* 对方信息 */}
            <div>
              <h3 className="font-medium text-gray-900">{otherUser.name}</h3>
              <div className="flex items-center space-x-2">
                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getRoleBadgeStyle(otherUser.role)}`}>
                  {getRoleLabelText(otherUser.role)}
                </span>
                <span className="text-xs text-gray-500">
                  {connectionStatus === 'connected' ? '在线' : '离线'}
                </span>
                {otherUserTyping && (
                  <span className="text-xs text-blue-600">正在输入...</span>
                )}
              </div>
            </div>
          </div>

          {/* 连接状态 */}
          <div className="flex items-center space-x-2">
            {connectionStatus === 'connecting' && (
              <div className="flex items-center text-yellow-600">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-yellow-600 mr-2"></div>
                连接中...
              </div>
            )}
            {connectionStatus === 'connected' && (
              <div className="flex items-center text-green-600">
                <div className="w-2 h-2 bg-green-600 rounded-full mr-2"></div>
                已连接
              </div>
            )}
            {connectionStatus === 'disconnected' && reconnectCount > 0 && (
              <div className="flex items-center text-red-600">
                <div className="w-2 h-2 bg-red-600 rounded-full mr-2"></div>
                重连中...({reconnectCount})
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 消息列表 */}
      <div className="flex-1 bg-gray-50">
        <MessageList
          messages={messages}
          currentUserId={currentUserId}
          searchKeyword={searchKeyword}
          highlightedMessageIds={highlightedMessageIds}
          scrollToMessageId={highlightedMessageId}
          onMessageRecall={(messageId) => {
            // 处理消息撤回
            setMessages(prev => prev.map(msg =>
              msg.id === messageId
                ? { ...msg, isRecalled: true, content: '[消息已撤回]', recalledAt: new Date().toISOString() }
                : msg
            ));
          }}
        />
      </div>

      {/* 输入区域 */}
      {canSendMessage ? (
        <div className="flex-shrink-0 border-t border-gray-200 p-4 bg-white">
          <div className="flex items-end space-x-2">
            {/* 文件上传按钮 */}
            <label className="flex-shrink-0">
              <input
                type="file"
                multiple
                accept="image/*,.pdf,.doc,.docx"
                onChange={handleFileUpload}
                className="hidden"
                disabled={uploadingFile}
              />
              <button
                type="button"
                className={`p-2 rounded-lg border border-gray-300 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed ${
                  uploadingFile ? 'animate-pulse' : ''
                }`}
                disabled={uploadingFile || readyState !== WebSocketReadyState.OPEN}
              >
                {uploadingFile ? (
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"></div>
                ) : (
                  <svg className="w-5 h-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                  </svg>
                )}
              </button>
            </label>

            {/* 输入框 */}
            <div className="flex-1">
              <textarea
                ref={inputRef}
                value={inputValue}
                onChange={handleInputChange}
                onKeyDown={handleKeyDown}
                placeholder={
                  readyState === WebSocketReadyState.OPEN
                    ? '输入消息...'
                    : '连接中，请稍候...'
                }
                disabled={readyState !== WebSocketReadyState.OPEN}
                rows={1}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                style={{
                  minHeight: '40px',
                  maxHeight: '120px',
                  resize: 'none',
                }}
              />
            </div>

            {/* 发送按钮 */}
            <button
              type="button"
              onClick={sendMessage}
              disabled={!inputValue.trim() || readyState !== WebSocketReadyState.OPEN}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              发送
            </button>
          </div>

          {/* 输入提示 */}
          <div className="mt-2 text-xs text-gray-500">
            按 Enter 发送，Shift + Enter 换行
          </div>
        </div>
      ) : (
        <div className="flex-shrink-0 border-t border-gray-200 p-4 bg-gray-50">
          <div className="text-center text-gray-500">
            {disputeStatus === 'PENDING_ARBITRATION' && '纠纷已升级为仲裁，沟通功能已暂停'}
            {disputeStatus === 'ARBITRATING' && '仲裁进行中，请等待仲裁结果'}
            {disputeStatus === 'RESOLVED' && '纠纷已解决'}
            {disputeStatus === 'CLOSED' && '纠纷已关闭'}
            {currentUserRole === 'arbitrator' && '仲裁员无法参与协商沟通'}
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatInterface;