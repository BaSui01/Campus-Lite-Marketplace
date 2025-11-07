/**
 * 表情展示组件 - 聊天表情渲染专家！😎
 *
 * @author BaSui 😎
 * @description 在聊天消息中正确显示表情，支持点击预览等功能
 * @date 2025-11-07
 */

import React, { useState } from 'react';
import type { EmojiMessage, EmojiItem } from '@campus/shared/types/emoji';

/**
 * 表情展示属性
 */
export interface EmojiDisplayProps {
  /** 表情消息数据 */
  emoji: EmojiMessage;
  /** 显示大小 */
  size?: 'small' | 'medium' | 'large';
  /** 是否可点击 */
  clickable?: boolean;
  /** 点击回调 */
  onClick?: (emoji: EmojiMessage) => void;
  /** 是否显示工具提示 */
  showTooltip?: boolean;
  /** 自定义样式类名 */
  className?: string;
  /** 最大宽度（用于响应式） */
  maxWidth?: number;
}

/**
 * 表情预览模态框属性
 */
interface EmojiPreviewModalProps {
  /** 表情数据 */
  emoji: EmojiMessage | null;
  /** 关闭回调 */
  onClose: () => void;
  /** 主题 */
  theme?: 'light' | 'dark';
}

/**
 * 表情预览模态框组件
 */
const EmojiPreviewModal: React.FC<EmojiPreviewModalProps> = ({
  emoji,
  onClose,
  theme = 'light',
}) => {
  if (!emoji) return null;

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 9999,
      }}
      onClick={onClose}
    >
      <div
        style={{
          backgroundColor: theme === 'dark' ? '#1f1f1f' : '#ffffff',
          borderRadius: '12px',
          padding: '24px',
          maxWidth: '90%',
          maxHeight: '90%',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* 表情展示 */}
        <div style={{
          marginBottom: '16px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          minWidth: '120px',
          minHeight: '120px',
        }}>
          {emoji.contentType === 'text' ? (
            <span style={{ fontSize: '80px' }}>{emoji.content}</span>
          ) : (
            <img
              src={emoji.content}
              alt={emoji.emojiName}
              style={{
                maxWidth: '200px',
                maxHeight: '200px',
                width: 'auto',
                height: 'auto',
                objectFit: 'contain',
              }}
              onError={(e) => {
                e.currentTarget.style.display = 'none';
                e.currentTarget.parentElement!.innerHTML = '<span style="font-size: 40px;">🚫</span>';
              }}
            />
          )}
        </div>

        {/* 表情信息 */}
        <div style={{ textAlign: 'center' }}>
          <h3 style={{
            margin: '0 0 8px 0',
            color: theme === 'dark' ? '#fff' : '#333',
            fontSize: '18px',
            fontWeight: '600',
          }}>
            {emoji.emojiName}
          </h3>
          {emoji.packName && (
            <p style={{
              margin: '0 0 16px 0',
              color: theme === 'dark' ? '#999' : '#666',
              fontSize: '14px',
            }}>
              来自：{emoji.packName}
            </p>
          )}
        </div>

        {/* 关闭按钮 */}
        <button
          onClick={onClose}
          style={{
            padding: '8px 24px',
            backgroundColor: theme === 'dark' ? '#333' : '#f0f0f0',
            border: 'none',
            borderRadius: '20px',
            color: theme === 'dark' ? '#fff' : '#333',
            cursor: 'pointer',
            fontSize: '14px',
            transition: 'background-color 0.2s',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.backgroundColor = theme === 'dark' ? '#444' : '#e0e0e0';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.backgroundColor = theme === 'dark' ? '#333' : '#f0f0f0';
          }}
        >
          关闭
        </button>
      </div>
    </div>
  );
};

/**
 * 表情展示组件
 */
export const EmojiDisplay: React.FC<EmojiDisplayProps> = ({
  emoji,
  size = 'medium',
  clickable = true,
  onClick,
  showTooltip = true,
  className = '',
  maxWidth = 200,
}) => {
  const [showPreview, setShowPreview] = useState(false);
  const [imageError, setImageError] = useState(false);

  // 尺寸配置
  const sizeConfig = {
    small: { fontSize: 16, imgSize: 16 },
    medium: { fontSize: 24, imgSize: 24 },
    large: { fontSize: 48, imgSize: 48 },
  };

  const { fontSize, imgSize } = sizeConfig[size];

  /**
   * 处理点击事件
   */
  const handleClick = () => {
    if (clickable) {
      if (onClick) {
        onClick(emoji);
      } else {
        // 默认行为：显示预览
        setShowPreview(true);
      }
    }
  };

  /**
   * 处理图片加载错误
   */
  const handleImageError = () => {
    setImageError(true);
  };

  /**
   * 渲染表情内容
   */
  const renderEmojiContent = () => {
    if (emoji.contentType === 'text') {
      return (
        <span
          style={{
            fontSize: `${fontSize}px`,
            lineHeight: 1,
            display: 'inline-block',
          }}
        >
          {emoji.content}
        </span>
      );
    }

    if (imageError) {
      return (
        <span
          style={{
            fontSize: `${fontSize * 0.7}px`,
            color: '#999',
          }}
        >
          🚫
        </span>
      );
    }

    return (
      <img
        src={emoji.content}
        alt={emoji.emojiName}
        style={{
          width: `${imgSize}px`,
          height: `${imgSize}px`,
          objectFit: 'contain',
          maxWidth: '100%',
          maxHeight: '100%',
        }}
        onError={handleImageError}
        loading="lazy"
      />
    );
  };

  return (
    <>
      <span
        className={`emoji-display ${className}`}
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: clickable ? 'pointer' : 'default',
          borderRadius: '4px',
          padding: '2px 4px',
          transition: 'background-color 0.2s',
          verticalAlign: 'middle',
          maxWidth: `${maxWidth}px`,
          overflow: 'hidden',
        }}
        onClick={handleClick}
        title={showTooltip ? emoji.emojiName : undefined}
        onMouseEnter={(e) => {
          if (clickable) {
            e.currentTarget.style.backgroundColor = 'rgba(0, 0, 0, 0.05)';
          }
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.backgroundColor = 'transparent';
        }}
      >
        {renderEmojiContent()}
      </span>

      {/* 预览模态框 */}
      {showPreview && (
        <EmojiPreviewModal
          emoji={emoji}
          onClose={() => setShowPreview(false)}
        />
      )}
    </>
  );
};

/**
 * 文本中的表情渲染器
 * 用于将包含表情符号的文本转换为包含EmojiDisplay组件的JSX
 */
export interface EmojiTextRendererProps {
  /** 包含表情的文本 */
  text: string;
  /** 表情映射表 */
  emojiMap?: Record<string, EmojiMessage>;
  /** 表情大小 */
  emojiSize?: 'small' | 'medium' | 'large';
  /** 是否可点击预览 */
  clickable?: boolean;
  /** 自定义样式类名 */
  className?: string;
}

/**
 * 文本表情渲染器组件
 */
export const EmojiTextRenderer: React.FC<EmojiTextRendererProps> = ({
  text,
  emojiMap = {},
  emojiSize = 'medium',
  clickable = true,
  className = '',
}) => {
  /**
   * 将文本分割为文本和表情的数组
   */
  const parseTextWithEmojis = (): (string | { emoji: EmojiMessage })[] => {
    const parts: (string | { emoji: EmojiMessage })[] = [];
    let currentText = '';
    let currentIndex = 0;

    while (currentIndex < text.length) {
      let foundEmoji = false;

      // 检查当前位置是否有匹配的表情
      for (const [emojiSymbol, emojiData] of Object.entries(emojiMap)) {
        if (text.startsWith(emojiSymbol, currentIndex)) {
          // 如果有累积的文本，先添加到parts中
          if (currentText) {
            parts.push(currentText);
            currentText = '';
          }

          // 添加表情
          parts.push({ emoji: emojiData });
          currentIndex += emojiSymbol.length;
          foundEmoji = true;
          break;
        }
      }

      // 如果没有找到匹配的表情，添加当前字符到累积文本中
      if (!foundEmoji) {
        currentText += text[currentIndex];
        currentIndex++;
      }
    }

    // 添加最后的文本
    if (currentText) {
      parts.push(currentText);
    }

    return parts;
  };

  const parts = parseTextWithEmojis();

  return (
    <span className={`emoji-text-renderer ${className}`}>
      {parts.map((part, index) => {
        if (typeof part === 'string') {
          return <span key={index}>{part}</span>;
        }

        return (
          <EmojiDisplay
            key={`${index}-${part.emoji.emojiId}`}
            emoji={part.emoji}
            size={emojiSize}
            clickable={clickable}
          />
        );
      })}
    </span>
  );
};

export default EmojiDisplay;