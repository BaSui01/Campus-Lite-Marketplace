/**
 * 纠纷消息列表组件 - 消息展示专家！📋
 *
 * @author BaSui 😎
 * @description 纠纷协商消息的展示和管理，支持多种消息类型
 * @date 2025-11-07
 */

import React, { useRef, useEffect, useState } from 'react';
import { EmojiDisplay } from '../../../components/EmojiDisplay';
import RecallConfirmDialog from '../../../components/RecallConfirmDialog';
import { SearchHighlight } from '../../../components/SearchHighlight';
import { messageService } from '@campus/shared/services/message';
import type { DisputeMessage } from './ChatInterface';

/**
 * 消息列表组件属性
 */
export interface MessageListProps {
  /** 消息列表 */
  messages: DisputeMessage[];
  /** 当前用户ID */
  currentUserId: number;
  /** 是否显示时间分隔符 */
  showDateSeparators?: boolean;
  /** 自定义样式类名 */
  className?: string;
  /** 消息点击回调 */
  onMessageClick?: (message: DisputeMessage) => void;
  /** 消息撤回回调 */
  onMessageRecall?: (messageId: string) => void;
  /** 搜索关键词 */
  searchKeyword?: string;
  /** 高亮消息ID列表 */
  highlightedMessageIds?: string[];
  /** 自动滚动到指定消息ID */
  scrollToMessageId?: string;
}

/**
 * 格式化日期
 */
const formatDate = (timestamp: string): string => {
  const date = new Date(timestamp);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
};

/**
 * 格式化时间
 */
const formatTime = (timestamp: string): string => {
  const date = new Date(timestamp);
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  });
};

/**
 * 判断是否是今天
 */
const isToday = (timestamp: string): boolean => {
  const date = new Date(timestamp);
  const today = new Date();
  return date.toDateString() === today.toDateString();
};

/**
 * 获取相对时间
 */
const getRelativeTime = (timestamp: string): string => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);

  if (diffMins < 1) return '刚刚';
  if (diffMins < 60) return `${diffMins}分钟前`;
  if (diffHours < 24) return `${diffHours}小时前`;

  return formatDate(timestamp);
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
 * 消息项组件
 */
const MessageItem: React.FC<{
  message: DisputeMessage;
  currentUserId: number;
  onClick?: (message: DisputeMessage) => void;
  onMessageRecall?: (messageId: string) => void;
  searchKeyword?: string;
  isHighlighted?: boolean;
}> = ({ message, currentUserId, onClick, onMessageRecall, searchKeyword, isHighlighted }) => {
  const isOwn = message.senderId === currentUserId;

  // 撤回相关状态
  const [showRecallDialog, setShowRecallDialog] = useState(false);
  const [recalling, setRecalling] = useState(false);

  // 检查是否可以撤回（5分钟内）
  const canRecall = () => {
    const messageTime = new Date(message.timestamp).getTime();
    const now = Date.now();
    const timeDiff = (now - messageTime) / (1000 * 60); // 分钟
    return isOwn && timeDiff <= 5; // 5分钟内可撤回
  };

  // 获取剩余撤回时间（秒）
  const getRemainingRecallTime = () => {
    const messageTime = new Date(message.timestamp).getTime();
    const now = Date.now();
    const timeDiff = 5 * 60 - (now - messageTime) / 1000; // 5分钟 = 300秒
    return Math.max(0, Math.floor(timeDiff));
  };

  // 处理撤回消息
  const handleRecallMessage = async () => {
    if (!canRecall() || recalling) return;

    setRecalling(true);
    try {
      await messageService.recallMessage(parseInt(message.id));
      setShowRecallDialog(false);
      onMessageRecall?.(message.id);
    } catch (error) {
      console.error('撤回消息失败:', error);
      alert('撤回失败，请重试');
    } finally {
      setRecalling(false);
    }
  };

  return (
    <div
      className={`flex ${isOwn ? 'justify-end' : 'justify-start'} mb-4 group`}
      onClick={() => onClick?.(message)}
    >
      <div className={`max-w-xs lg:max-w-md xl:max-w-lg ${isOwn ? 'order-2' : 'order-1'}`}>
        {/* 非自己消息显示发送者信息 */}
        {!isOwn && (
          <div className="flex items-center space-x-2 mb-1">
            <span className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium ${getRoleBadgeStyle(message.senderRole)}`}>
              {message.senderName}
            </span>
            <span className="text-xs text-gray-500">
              {getRelativeTime(message.timestamp)}
            </span>
          </div>
        )}

        {/* 消息内容 */}
        <div
          className={`rounded-lg px-4 py-2 cursor-pointer transition-colors hover:opacity-90 ${
            isHighlighted
              ? 'ring-2 ring-yellow-400 ring-offset-2'
              : ''
          } ${
            message.isRecalled
              ? 'bg-gray-100 text-gray-500 border border-gray-200'
              : isOwn
                ? 'bg-blue-600 text-white'
                : 'bg-white border border-gray-200 text-gray-900'
          }`}
        >
          {/* 撤回消息的特殊显示 */}
          {message.isRecalled ? (
            <div className="flex items-center space-x-2">
              <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a1 1 0 001 1h12a1 1 0 001-1V6a1 1 0 00-1-1h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
              <span className="text-sm italic">{message.content}</span>
              {message.recalledAt && (
                <span className="text-xs text-gray-400">
                  {formatTime(message.recalledAt)}
                </span>
              )}
            </div>
          ) : (
            <>
              {/* 文本消息 */}
              {message.messageType === 'text' && (
                <p className="text-sm whitespace-pre-wrap break-words">
                  {searchKeyword ? (
                    <SearchHighlight
                      text={message.content}
                      keyword={searchKeyword}
                      highlightClassName={isOwn ? "bg-yellow-300 text-yellow-900 px-1 py-0.5 rounded" : "bg-yellow-200 text-yellow-900 px-1 py-0.5 rounded"}
                    />
                  ) : (
                    message.content
                  )}
                </p>
              )}

          {/* 图片消息 */}
          {message.messageType === 'image' && message.fileUrl && (
            <div className="space-y-2">
              <img
                src={message.fileUrl}
                alt={message.fileName || '图片'}
                className="max-w-full h-auto rounded cursor-pointer hover:opacity-90 transition-opacity"
                onClick={(e) => {
                  e.stopPropagation();
                  window.open(message.fileUrl, '_blank');
                }}
                loading="lazy"
              />
              {message.fileName && (
                <p className={`text-xs ${isOwn ? 'text-blue-100' : 'text-gray-600'}`}>
                  {message.fileName}
                </p>
              )}
            </div>
          )}

          {/* 表情消息 */}
          {message.messageType === 'emoji' && (
            <div className="flex justify-center">
              <EmojiDisplay
                emoji={{
                  type: 'emoji',
                  emojiId: message.id,
                  packId: 'unknown',
                  content: message.content,
                  emojiName: '表情',
                  contentType: 'text',
                  packName: '默认表情包',
                }}
                size="large"
                clickable={true}
              />
            </div>
          )}

          {/* 文件消息 */}
          {message.messageType === 'file' && (
            <div className="flex items-center space-x-3">
              <div className={`p-2 rounded-lg ${isOwn ? 'bg-blue-700' : 'bg-gray-100'}`}>
                <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M8 4a3 3 0 00-3 3v4a5 5 0 0010 0V7a1 1 0 112 0v4a3 3 0 11-6 0V7a5 5 0 0110 0v4a2 2 0 11-4 0V7a1 1 0 012 0v4a1 1 0 11-2 0V7z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="flex-1 min-w-0">
                <p className={`text-sm font-medium truncate ${isOwn ? 'text-white' : 'text-gray-900'}`}>
                  {message.fileName}
                </p>
                {message.fileSize && (
                  <p className={`text-xs ${isOwn ? 'text-blue-100' : 'text-gray-500'}`}>
                    {(message.fileSize / 1024).toFixed(1)} KB
                  </p>
                )}
              </div>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  window.open(message.fileUrl, '_blank');
                }}
                className={`p-1 rounded hover:bg-opacity-10 ${
                  isOwn ? 'hover:bg-white' : 'hover:bg-gray-200'
                }`}
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </button>
            </div>
          )}
            </>
          )}
        </div>

        {/* 自己消息显示状态、时间和撤回按钮 */}
        {isOwn && (
          <div className="flex justify-between items-center mt-1">
            <div className="flex items-center space-x-2">
              <span className={`text-xs ${
                message.isRead ? 'text-blue-600' : 'text-gray-500'
              }`}>
                {formatTime(message.timestamp)}
              </span>
              <span className="text-xs text-gray-500">
                {message.isRead ? '✓✓' : '✓'}
              </span>
            </div>

            {/* 撤回按钮 - 只在5分钟内显示 */}
            {canRecall() && (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowRecallDialog(true);
                }}
                className="opacity-0 group-hover:opacity-100 transition-opacity duration-200 text-xs text-gray-500 hover:text-orange-600 px-2 py-1 rounded hover:bg-orange-50"
                title="撤回消息"
              >
                撤回
              </button>
            )}
          </div>
        )}
      </div>

      {/* 撤回确认对话框 */}
      <RecallConfirmDialog
        visible={showRecallDialog}
        messagePreview={message.content}
        messageTime={formatTime(message.timestamp)}
        onConfirm={handleRecallMessage}
        onCancel={() => setShowRecallDialog(false)}
        loading={recalling}
        remainingTime={getRemainingRecallTime()}
        timeLimit={5}
      />
    </div>
  );
};

/**
 * 日期分隔符组件
 */
const DateSeparator: React.FC<{ date: string }> = ({ date }) => (
  <div className="flex items-center justify-center my-4">
    <div className="px-3 py-1 bg-gray-100 rounded-full">
      <span className="text-xs text-gray-600 font-medium">
        {isToday(date) ? '今天' : date}
      </span>
    </div>
  </div>
);

/**
 * 纠纷消息列表组件
 */
export const MessageList: React.FC<MessageListProps> = ({
  messages,
  currentUserId,
  showDateSeparators = true,
  className = '',
  onMessageClick,
  onMessageRecall,
  searchKeyword,
  highlightedMessageIds = [],
  scrollToMessageId,
}) => {
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messageRefs = useRef<Map<string, HTMLDivElement>>(new Map());

  // 滚动到底部
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // 滚动到指定消息
  const scrollToMessage = (messageId: string) => {
    const messageElement = messageRefs.current.get(messageId);
    if (messageElement) {
      messageElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
      // 添加闪烁效果
      messageElement.classList.add('ring-2', 'ring-yellow-400', 'ring-offset-2');
      setTimeout(() => {
        messageElement.classList.remove('ring-2', 'ring-yellow-400', 'ring-offset-2');
      }, 2000);
    }
  };

  // 自动滚动到底部（当没有特定滚动目标时）
  useEffect(() => {
    if (!scrollToMessageId) {
      scrollToBottom();
    }
  }, [messages, scrollToMessageId]);

  // 滚动到指定消息
  useEffect(() => {
    if (scrollToMessageId) {
      setTimeout(() => {
        scrollToMessage(scrollToMessageId);
      }, 100);
    }
  }, [scrollToMessageId]);

  if (messages.length === 0) {
    return (
      <div className={`flex flex-col items-center justify-center h-full text-gray-500 ${className}`}>
        <svg className="w-12 h-12 text-gray-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
        </svg>
        <p className="text-lg font-medium mb-2">暂无消息</p>
        <p className="text-sm">开始对话吧！</p>
      </div>
    );
  }

  // 按日期分组消息
  const groupedMessages: Array<{ date: string; messages: DisputeMessage[] }> = [];
  let currentDate = '';
  let currentGroup: DisputeMessage[] = [];

  messages.forEach((message) => {
    const messageDate = formatDate(message.timestamp);

    if (messageDate !== currentDate) {
      if (currentGroup.length > 0) {
        groupedMessages.push({ date: currentDate, messages: currentGroup });
      }
      currentDate = messageDate;
      currentGroup = [message];
    } else {
      currentGroup.push(message);
    }
  });

  if (currentGroup.length > 0) {
    groupedMessages.push({ date: currentDate, messages: currentGroup });
  }

  return (
    <div className={`message-list space-y-1 ${className}`}>
      {groupedMessages.map((group, groupIndex) => (
        <div key={group.date}>
          {/* 日期分隔符 */}
          {showDateSeparators && groupIndex > 0 && (
            <DateSeparator date={group.date} />
          )}

          {/* 该日期的消息 */}
          {group.messages.map((message) => (
            <div
              key={message.id}
              ref={(el) => {
                if (el) {
                  messageRefs.current.set(message.id, el);
                }
              }}
            >
              <MessageItem
                message={message}
                currentUserId={currentUserId}
                onClick={onMessageClick}
                onMessageRecall={onMessageRecall}
                searchKeyword={searchKeyword}
                isHighlighted={highlightedMessageIds.includes(message.id)}
              />
            </div>
          ))}
        </div>
      ))}

      {/* 滚动锚点 */}
      <div ref={messagesEndRef} />
    </div>
  );
};

export default MessageList;