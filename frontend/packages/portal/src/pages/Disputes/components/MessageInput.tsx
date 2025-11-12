/**
 * 纠纷消息输入组件 - 输入大师！⌨️
 *
 * @author BaSui 😎
 * @description 纠纷协商消息输入，支持文字、高级表情、文件上传
 * @date 2025-11-07
 */

import React, { useState, useRef, useEffect } from 'react';
import { EmojiPicker } from '../../../components/EmojiPicker';
import { emojiService } from '../../../services/emoji';
import type { EmojiItem } from '@campus/shared/types/emoji';

/**
 * 输入工具类型
 */
export type InputTool = 'text' | 'emoji' | 'file';

/**
 * 消息输入组件属性
 */
export interface MessageInputProps {
  /** 输入内容 */
  value: string;
  /** 输入变化回调 */
  onChange: (value: string) => void;
  /** 发送消息回调 */
  onSend: (content: string, type: 'text' | 'image' | 'file' | 'emoji', file?: File, emojiData?: EmojiItem) => void;
  /** 是否禁用 */
  disabled?: boolean;
  /** 是否正在上传 */
  uploading?: boolean;
  /** 占位符文本 */
  placeholder?: string;
  /** 最大长度 */
  maxLength?: number;
  /** 支持的文件类型 */
  accept?: string;
  /** 自定义样式类名 */
  className?: string;
  /** 输入状态变化回调 */
  onTypingChange?: (isTyping: boolean) => void;
}

/**
 * 表情符号列表
 */
const EMOJIS = [
  '😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰',
  '😘', '😗', '😙', '😚', '😋', '😛', '😜', '🤪', '😝', '🤗', '🤭', '🤫', '🤔', '🤐',
  '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😔', '😪', '🤤', '😴', '😷',
  '🤒', '🤕', '🤢', '🤮', '🤧', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳', '😎', '🤓',
  '🧐', '😕', '😟', '🙁', '☹️', '😮', '😯', '😲', '😳', '🥺', '😦', '😧', '😨', '😰',
  '😱', '😖', '😣', '😞', '😓', '😩', '😫', '🥱', '😥', '😤', '😡', '😠', '🤬', '😈',
  '👿', '💀', '☠️', '💩', '🤡', '👹', '👺', '👻', '👽', '👾', '🤖', '❤️', '🧡', '💛',
  '💚', '💙', '💜', '🖤', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝',
  '👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙', '👈', '👉', '👆', '👇', '☝️', '✋',
  '🤚', '🖐️', '🖖', '👋', '🤙', '💪', '🙏', '🤝', '✍️', '💅', '🤳', '💃', '🕺',
  '👯', '🧑', '👨', '👩', '👱', '👴', '👶', '👦', '👧', '👨‍🦱', '👩‍🦰', '👱‍🦰', '👨‍🦳', '👩‍🦳',
  '🧓', '👴', '👵', '🙍', '🙎', '🙅', '🙆', '💁', '🙋', '🙇', '🙏', '🤷', '🤸', '💆',
  '💇', '🚶', '🧍', '🧎', '🧏', '🧖', '🧑', '🧒', '🧓', '🧔', '🧕', '👫', '👬', '👭',
];

/**
 * 消息输入组件
 */
export const MessageInput: React.FC<MessageInputProps> = ({
  value,
  onChange,
  onSend,
  disabled = false,
  uploading = false,
  placeholder = '输入消息...',
  maxLength = 500,
  accept = 'image/*,.pdf,.doc,.docx',
  className = '',
  onTypingChange,
}) => {
  const [showEmojiPanel, setShowEmojiPanel] = useState(false);
  const [currentTool, setCurrentTool] = useState<InputTool>('text');
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // 自动调整文本框高度
  useEffect(() => {
    const textarea = textareaRef.current;
    if (textarea) {
      textarea.style.height = 'auto';
      textarea.style.height = `${Math.min(textarea.scrollHeight, 120)}px`;
    }
  }, [value]);

  // 处理键盘事件
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  // 处理发送
  const handleSend = () => {
    if (!value.trim() || disabled || uploading) return;

    onSend(value.trim(), 'text');
    onChange('');
    setShowEmojiPanel(false);
    setCurrentTool('text');
  };

  // 处理表情选择
  const handleEmojiSelect = async (emoji: EmojiItem) => {
    try {
      // 记录表情使用
      await emojiService.recordEmojiUsage(emoji.id, 'dispute-chat');

      // 直接发送表情消息
      onSend(emoji.content, 'emoji', undefined, emoji);
    } catch (error) {
      console.error('发送表情失败:', error);
      // 即使记录失败也要发送表情
      onSend(emoji.content, 'emoji', undefined, emoji);
    }
  };

  // 处理文件选择
  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    const file = files[0];
    const fileName = file.name.toLowerCase();

    // 判断文件类型
    if (file.type.startsWith('image/')) {
      onSend(file.name, 'image', file);
    } else {
      onSend(file.name, 'file', file);
    }

    // 重置文件输入
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  // 处理输入变化
  const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const newValue = e.target.value;
    if (newValue.length <= maxLength) {
      onChange(newValue);
      onTypingChange?.(newValue.trim().length > 0);
    }
  };

  // 获取字符数
  const getCharCount = () => {
    return value.length;
  };

  // 切换工具
  const toggleTool = (tool: InputTool) => {
    if (currentTool === tool) {
      setCurrentTool('text');
      if (tool === 'emoji') {
        setShowEmojiPanel(false);
      }
    } else {
      setCurrentTool(tool);
      if (tool === 'emoji') {
        setShowEmojiPanel(true);
      } else if (tool === 'file') {
        fileInputRef.current?.click();
      }
    }
  };

  return (
    <div className={`message-input ${className}`}>
      {/* 高级表情选择器 */}
      <EmojiPicker
        visible={showEmojiPanel}
        onEmojiSelect={handleEmojiSelect}
        onClose={() => {
          setShowEmojiPanel(false);
          setCurrentTool('text');
        }}
        config={{
          showFavoriteTab: true,
          showSearch: true,
          emojisPerRow: 8,
          maxRows: 5,
          defaultCategory: 'SMILEYS',
          theme: 'light',
        }}
      />

      {/* 工具栏 */}
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center space-x-1">
          {/* 表情按钮 */}
          <button
            type="button"
            onClick={() => toggleTool('emoji')}
            className={`p-2 rounded-lg transition-colors ${
              currentTool === 'emoji'
                ? 'bg-blue-100 text-blue-600'
                : 'hover:bg-gray-100 text-gray-600'
            }`}
            title="表情"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </button>

          {/* 文件按钮 */}
          <button
            type="button"
            onClick={() => toggleTool('file')}
            className={`p-2 rounded-lg transition-colors ${
              currentTool === 'file'
                ? 'bg-blue-100 text-blue-600'
                : 'hover:bg-gray-100 text-gray-600'
            }`}
            title="发送文件"
            disabled={uploading}
          >
            {uploading ? (
              <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600"></div>
            ) : (
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
              </svg>
            )}
          </button>

          {/* 隐藏的文件输入 */}
          <input
            ref={fileInputRef}
            type="file"
            accept={accept}
            onChange={handleFileSelect}
            className="hidden"
            disabled={uploading}
          />
        </div>

        {/* 字符计数 */}
        <div className="text-xs text-gray-500">
          {getCharCount()}/{maxLength}
        </div>
      </div>

      {/* 输入区域 */}
      <div className="flex items-end space-x-2">
        {/* 文本输入框 */}
        <div className="flex-1">
          <textarea
            ref={textareaRef}
            value={value}
            onChange={handleChange}
            onKeyDown={handleKeyDown}
            placeholder={disabled ? '连接中...' : placeholder}
            disabled={disabled}
            rows={1}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
            style={{
              minHeight: '40px',
              maxHeight: '120px',
            }}
          />
        </div>

        {/* 发送按钮 */}
        <button
          type="button"
          onClick={handleSend}
          disabled={!value.trim() || disabled || uploading}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center space-x-1"
        >
          {uploading ? (
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
          ) : (
            <>
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
              <span>发送</span>
            </>
          )}
        </button>
      </div>

      {/* 输入提示 */}
      <div className="mt-2 text-xs text-gray-500">
        按 Enter 发送，Shift + Enter 换行 • 支持图片、PDF、Word文档 • 表情支持收藏和搜索
      </div>
    </div>
  );
};

export default MessageInput;