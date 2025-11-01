/**
 * 聊天页面 - 实时聊天，沟通无障碍！💬
 * @author BaSui 😎
 * @description 支持私聊、消息记录、表情发送
 */

import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Input, Button, Skeleton } from '@campus/shared/components';
import { useWebSocketService } from '@campus/shared/hooks';
import { messageService } from '@campus/shared/services';
import { useAuthStore, useNotificationStore } from '../../store';
import './Chat.css';

// ==================== 类型定义 ====================

interface Conversation {
  conversationId: string;
  userId: string;
  username: string;
  avatar?: string;
  lastMessage?: string;
  lastMessageTime?: string;
  unreadCount: number;
}

interface Message {
  messageId: string;
  conversationId: string;
  senderId: string;
  senderName: string;
  senderAvatar?: string;
  content: string;
  timestamp: string;
  isSent: boolean; // 是否是当前用户发送的
}

/**
 * 聊天页面组件
 */
const Chat: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const toast = useNotificationStore();
  const currentUser = useAuthStore((state) => state.user);
  const { isConnected, sendMessage: wsSendMessage } = useWebSocketService();

  // ==================== 状态管理 ====================

  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [currentConversation, setCurrentConversation] = useState<Conversation | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [messageInput, setMessageInput] = useState('');
  const [loadingConversations, setLoadingConversations] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sendingMessage, setSendingMessage] = useState(false);

  // 滚动到底部
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // ==================== 数据加载 ====================

  /**
   * 加载对话列表
   */
  const loadConversations = async () => {
    setLoadingConversations(true);

    try {
      // 🚀 调用真实后端 API 获取对话列表
      const response = await messageService.getConversations({ page: 0, size: 50 });

      if (response.success && response.data) {
        const apiConversations: Conversation[] = response.data.content.map((conv: any) => ({
          conversationId: String(conv.id),
          userId: String(conv.targetUserId),
          username: conv.targetUserName || '未知用户',
          avatar: conv.targetUserAvatar,
          lastMessage: conv.lastMessage,
          lastMessageTime: conv.lastMessageTime,
          unreadCount: conv.unreadCount || 0,
        }));

        setConversations(apiConversations);

        // 如果 URL 中有 conversationId，自动打开该对话
        const conversationId = searchParams.get('id');
        if (conversationId) {
          const conversation = apiConversations.find((c) => c.conversationId === conversationId);
          if (conversation) {
            handleSelectConversation(conversation);
          }
        }
      }
    } catch (err: any) {
      console.error('加载对话列表失败：', err);
      toast.error(err.response?.data?.message || '加载对话列表失败！😭');
    } finally {
      setLoadingConversations(false);
    }
  };

  /**
   * 加载聊天记录
   */
  const loadMessages = async (conversationId: string) => {
    setLoadingMessages(true);

    try {
      // 🚀 调用真实后端 API 获取聊天记录
      const response = await messageService.getMessages({
        conversationId: Number(conversationId),
        page: 0,
        size: 100,
      });

      if (response.success && response.data) {
        const apiMessages: Message[] = response.data.content.map((msg: any) => ({
          messageId: String(msg.id),
          conversationId,
          senderId: String(msg.senderId),
          senderName: msg.senderName || '未知',
          senderAvatar: msg.senderAvatar,
          content: msg.content,
          timestamp: msg.createTime,
          isSent: String(msg.senderId) === String(currentUser?.id),
        }));

        setMessages(apiMessages);
        scrollToBottom();
      }
    } catch (err: any) {
      console.error('加载聊天记录失败：', err);
      toast.error(err.response?.data?.message || '加载聊天记录失败！😭');
    } finally {
      setLoadingMessages(false);
    }
  };

  useEffect(() => {
    loadConversations();
  }, []);

  // ==================== 事件处理 ====================

  /**
   * 选择对话
   */
  const handleSelectConversation = (conversation: Conversation) => {
    setCurrentConversation(conversation);
    loadMessages(conversation.conversationId);

    // 标记已读
    setConversations((prev) =>
      prev.map((c) =>
        c.conversationId === conversation.conversationId ? { ...c, unreadCount: 0 } : c
      )
    );
  };

  /**
   * 发送消息
   */
  const handleSendMessage = async () => {
    if (!messageInput.trim()) {
      toast.warning('消息不能为空！😰');
      return;
    }

    if (!currentConversation) {
      toast.warning('请选择一个对话！😰');
      return;
    }

    const newMessage: Message = {
      messageId: `msg-${Date.now()}`,
      conversationId: currentConversation.conversationId,
      senderId: currentUser?.id || '',
      senderName: currentUser?.username || '我',
      content: messageInput,
      timestamp: new Date().toISOString(),
      isSent: true,
    };

    // 乐观更新 UI
    setMessages((prev) => [...prev, newMessage]);
    setMessageInput('');
    scrollToBottom();

    setSendingMessage(true);

    try {
      // 🚀 通过 WebSocket 发送消息
      if (isConnected) {
        wsSendMessage({
          type: 'CHAT_MESSAGE',
          conversationId: currentConversation.conversationId,
          content: messageInput,
        });
      } else {
        // 🚀 备用：通过 HTTP API 发送消息
        await messageService.sendMessage({
          receiverId: Number(currentConversation.userId),
          content: messageInput,
        });
        console.log('[Chat] ✅ 消息已发送（HTTP）');
      }

      // 更新对话列表
      setConversations((prev) =>
        prev.map((c) =>
          c.conversationId === currentConversation.conversationId
            ? {
                ...c,
                lastMessage: messageInput,
                lastMessageTime: new Date().toISOString(),
              }
            : c
        )
      );
    } catch (err: any) {
      console.error('发送消息失败：', err);
      toast.error(err.response?.data?.message || '发送消息失败！😭');

      // 移除失败的消息
      setMessages((prev) => prev.filter((m) => m.messageId !== newMessage.messageId));
    } finally {
      setSendingMessage(false);
    }
  };

  /**
   * 按下回车发送消息
   */
  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  /**
   * 滚动到底部
   */
  const scrollToBottom = () => {
    setTimeout(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  /**
   * 格式化时间
   */
  const formatTime = (time?: string) => {
    if (!time) return '';
    const date = new Date(time);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    // 1分钟内
    if (diff < 60 * 1000) {
      return '刚刚';
    }

    // 1小时内
    if (diff < 60 * 60 * 1000) {
      const minutes = Math.floor(diff / (60 * 1000));
      return `${minutes}分钟前`;
    }

    // 今天
    if (date.toDateString() === now.toDateString()) {
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    }

    // 昨天
    const yesterday = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    if (date.toDateString() === yesterday.toDateString()) {
      return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
    }

    // 其他
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' });
  };

  /**
   * 格式化详细时间
   */
  const formatDetailTime = (time?: string) => {
    if (!time) return '';
    const date = new Date(time);
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // ==================== 渲染 ====================

  return (
    <div className="chat-page">
      <div className="chat-container">
        {/* ==================== 左侧对话列表 ==================== */}
        <div className="chat-sidebar">
          <div className="chat-sidebar__header">
            <h2 className="chat-sidebar__title">💬 消息</h2>
          </div>

          <div className="chat-sidebar__list">
            {loadingConversations ? (
              <Skeleton type="list" count={5} animation="wave" />
            ) : conversations.length === 0 ? (
              <div className="chat-sidebar__empty">
                <div className="empty-icon">📭</div>
                <p className="empty-text">还没有对话</p>
                <p className="empty-tip">快去和卖家聊聊吧！</p>
              </div>
            ) : (
              conversations.map((conversation) => (
                <div
                  key={conversation.conversationId}
                  className={`conversation-item ${
                    currentConversation?.conversationId === conversation.conversationId
                      ? 'active'
                      : ''
                  }`}
                  onClick={() => handleSelectConversation(conversation)}
                >
                  <div className="conversation-item__avatar">
                    {conversation.avatar ? (
                      <img src={conversation.avatar} alt={conversation.username} />
                    ) : (
                      <span>👤</span>
                    )}
                  </div>
                  <div className="conversation-item__info">
                    <div className="conversation-item__header">
                      <span className="conversation-item__name">{conversation.username}</span>
                      <span className="conversation-item__time">
                        {formatTime(conversation.lastMessageTime)}
                      </span>
                    </div>
                    <div className="conversation-item__message">
                      <span>{conversation.lastMessage || '暂无消息'}</span>
                      {conversation.unreadCount > 0 && (
                        <span className="conversation-item__badge">{conversation.unreadCount}</span>
                      )}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* ==================== 右侧聊天窗口 ==================== */}
        <div className="chat-main">
          {!currentConversation ? (
            <div className="chat-empty">
              <div className="chat-empty__icon">💬</div>
              <h3 className="chat-empty__text">选择一个对话开始聊天</h3>
              <p className="chat-empty__tip">在左侧选择对话或发起新的聊天</p>
            </div>
          ) : (
            <>
              {/* 聊天标题 */}
              <div className="chat-header">
                <div className="chat-header__user">
                  <div className="chat-header__avatar">
                    {currentConversation.avatar ? (
                      <img src={currentConversation.avatar} alt={currentConversation.username} />
                    ) : (
                      <span>👤</span>
                    )}
                  </div>
                  <div className="chat-header__info">
                    <div className="chat-header__name">{currentConversation.username}</div>
                    <div className="chat-header__status">
                      {isConnected ? '🟢 在线' : '⚪ 离线'}
                    </div>
                  </div>
                </div>
              </div>

              {/* 消息列表 */}
              <div className="chat-messages">
                {loadingMessages ? (
                  <div className="chat-messages__loading">
                    <Skeleton type="list" count={3} animation="wave" />
                  </div>
                ) : messages.length === 0 ? (
                  <div className="chat-messages__empty">
                    <p>还没有消息，说点什么吧！😊</p>
                  </div>
                ) : (
                  <>
                    {messages.map((message) => (
                      <div
                        key={message.messageId}
                        className={`message ${message.isSent ? 'message--sent' : 'message--received'}`}
                      >
                        {!message.isSent && (
                          <div className="message__avatar">
                            {message.senderAvatar ? (
                              <img src={message.senderAvatar} alt={message.senderName} />
                            ) : (
                              <span>👤</span>
                            )}
                          </div>
                        )}
                        <div className="message__content">
                          <div className="message__bubble">{message.content}</div>
                          <div className="message__time">{formatDetailTime(message.timestamp)}</div>
                        </div>
                      </div>
                    ))}
                    <div ref={messagesEndRef} />
                  </>
                )}
              </div>

              {/* 消息输入框 */}
              <div className="chat-input">
                <div className="chat-input__wrapper">
                  <Input
                    type="text"
                    size="large"
                    placeholder="输入消息...（按 Enter 发送）"
                    value={messageInput}
                    onChange={(e) => setMessageInput(e.target.value)}
                    onKeyPress={handleKeyPress}
                    disabled={sendingMessage}
                    maxLength={500}
                  />
                  <Button
                    type="primary"
                    size="large"
                    onClick={handleSendMessage}
                    loading={sendingMessage}
                    disabled={!messageInput.trim()}
                  >
                    📤 发送
                  </Button>
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Chat;
