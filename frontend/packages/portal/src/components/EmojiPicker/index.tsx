/**
 * 表情包选择器组件 - 聊天表情选择专家！😎
 *
 * @author BaSui 😎
 * @description 表情选择器，支持分类、搜索、收藏等功能
 * @date 2025-11-07
 */

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { emojiService } from '../../services/emoji';
import type {
  EmojiItem,
  EmojiPack,
  EmojiCategory,
  EmojiPickerConfig,
} from '@campus/shared/types/emoji';

/**
 * 表情选择器属性
 */
export interface EmojiPickerProps {
  /** 选择表情的回调函数 */
  onEmojiSelect: (emoji: EmojiItem) => void;
  /** 关闭选择器的回调函数 */
  onClose: () => void;
  /** 组件配置 */
  config?: Partial<EmojiPickerConfig>;
  /** 自定义样式类名 */
  className?: string;
  /** 是否可见 */
  visible?: boolean;
}

/**
 * 默认配置
 */
const defaultConfig: EmojiPickerConfig = {
  showFavoriteTab: true,
  showSearch: true,
  emojisPerRow: 8,
  maxRows: 6,
  allowCustomUpload: false,
  defaultCategory: 'SMILEYS',
  theme: 'light',
};

/**
 * 表情分类映射
 */
const categoryMap = {
  SMILEYS: { name: '笑脸', icon: '😊' },
  GESTURES: { name: '手势', icon: '👍' },
  ANIMALS: { name: '动物', icon: '🐱' },
  FOOD: { name: '食物', icon: '🍔' },
  ACTIVITIES: { name: '活动', icon: '🎉' },
  OBJECTS: { name: '物品', icon: '💡' },
  SYMBOLS: { name: '符号', icon: '❤️' },
  FLAGS: { name: '旗帜', icon: '🏳️' },
  CUSTOM: { name: '自定义', icon: '📁' },
};

/**
 * 表情选择器组件
 */
export const EmojiPicker: React.FC<EmojiPickerProps> = ({
  onEmojiSelect,
  onClose,
  config = {},
  className = '',
  visible = true,
}) => {
  const mergedConfig = { ...defaultConfig, ...config };
  const {
    showFavoriteTab,
    showSearch,
    emojisPerRow,
    maxRows,
    allowCustomUpload,
    defaultCategory,
    theme,
  } = mergedConfig;

  // 状态管理
  const [activeTab, setActiveTab] = useState<string>(
    showFavoriteTab ? 'recent' : defaultCategory
  );
  const [emojiPacks, setEmojiPacks] = useState<EmojiPack[]>([]);
  const [recentEmojis, setRecentEmojis] = useState<EmojiItem[]>([]);
  const [favoriteEmojis, setFavoriteEmojis] = useState<EmojiItem[]>([]);
  const [searchResults, setSearchResults] = useState<EmojiItem[]>([]);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [loading, setLoading] = useState(false);
  const [packLoading, setPackLoading] = useState(false);

  // 引用
  const pickerRef = useRef<HTMLDivElement>(null);
  const searchInputRef = useRef<HTMLInputElement>(null);

  /**
   * 加载表情包数据
   */
  const loadEmojiData = useCallback(async () => {
    setPackLoading(true);
    try {
      const response = await emojiService.getEmojiPacks();
      setEmojiPacks(response.packs);

      // 并行加载最近使用和收藏的表情
      const [recent, favorite] = await Promise.all([
        emojiService.getRecentlyUsedEmojis(20),
        emojiService.getFavoriteEmojis(50),
      ]);

      setRecentEmojis(recent);
      setFavoriteEmojis(favorite);
    } catch (error) {
      console.error('加载表情数据失败:', error);
    } finally {
      setPackLoading(false);
    }
  }, []);

  /**
   * 搜索表情
   */
  const handleSearch = useCallback(async (keyword: string) => {
    setSearchKeyword(keyword);

    if (!keyword.trim()) {
      setSearchResults([]);
      return;
    }

    setLoading(true);
    try {
      const results = await emojiService.searchEmojis(keyword.trim());
      setSearchResults(results);
    } catch (error) {
      console.error('搜索表情失败:', error);
      setSearchResults([]);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 选择表情
   */
  const handleEmojiSelect = useCallback(async (emoji: EmojiItem) => {
    try {
      // 记录使用
      await emojiService.recordEmojiUsage(emoji.id, 'chat');

      // 更新最近使用列表
      const newRecent = [emoji, ...recentEmojis.filter(e => e.id !== emoji.id)].slice(0, 20);
      setRecentEmojis(newRecent);

      // 调用回调
      onEmojiSelect(emoji);
    } catch (error) {
      console.error('选择表情失败:', error);
      // 即使记录失败也要调用回调
      onEmojiSelect(emoji);
    }
  }, [onEmojiSelect, recentEmojis]);

  /**
   * 切换表情收藏状态
   */
  const toggleFavorite = useCallback(async (emoji: EmojiItem, e: React.MouseEvent) => {
    e.stopPropagation(); // 阻止触发选择表情

    try {
      const newFavoriteStatus = !emoji.isFavorite;
      await emojiService.toggleEmojiFavorite(emoji.id, newFavoriteStatus);

      // 更新本地状态
      setFavoriteEmojis(prev => {
        if (newFavoriteStatus) {
          return [...prev, emoji];
        } else {
          return prev.filter(e => e.id !== emoji.id);
        }
      });

      // 更新表情包中的收藏状态
      setEmojiPacks(prev => prev.map(pack => ({
        ...pack,
        emojis: pack.emojis.map(e =>
          e.id === emoji.id ? { ...e, isFavorite: newFavoriteStatus } : e
        )
      })));

    } catch (error) {
      console.error('切换收藏状态失败:', error);
    }
  }, []);

  /**
   * 获取当前显示的表情列表
   */
  const getCurrentEmojis = useCallback((): EmojiItem[] => {
    if (searchKeyword) {
      return searchResults;
    }

    switch (activeTab) {
      case 'recent':
        return recentEmojis;
      case 'favorite':
        return favoriteEmojis;
      default:
        // 根据分类获取表情
        return emojiPacks.flatMap(pack =>
          pack.emojis.filter(emoji => emoji.category === activeTab)
        );
    }
  }, [searchKeyword, searchResults, activeTab, recentEmojis, favoriteEmojis, emojiPacks]);

  /**
   * 点击外部关闭
   */
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (pickerRef.current && !pickerRef.current.contains(event.target as Node)) {
        onClose();
      }
    };

    if (visible) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [visible, onClose]);

  /**
   * 初始化加载数据
   */
  useEffect(() => {
    if (visible) {
      loadEmojiData();
    }
  }, [visible, loadEmojiData]);

  /**
   * 聚焦搜索框
   */
  useEffect(() => {
    if (visible && showSearch && searchInputRef.current) {
      setTimeout(() => searchInputRef.current?.focus(), 100);
    }
  }, [visible, showSearch]);

  if (!visible) {
    return null;
  }

  const currentEmojis = getCurrentEmojis();

  return (
    <div
      ref={pickerRef}
      className={`emoji-picker ${theme} ${className}`}
      style={{
        position: 'absolute',
        bottom: '100%',
        left: 0,
        zIndex: 1000,
        backgroundColor: theme === 'dark' ? '#1f1f1f' : '#ffffff',
        border: `1px solid ${theme === 'dark' ? '#404040' : '#e0e0e0'}`,
        borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
        width: '350px',
        maxHeight: '400px',
        fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      }}
    >
      {/* 搜索框 */}
      {showSearch && (
        <div style={{ padding: '12px', borderBottom: `1px solid ${theme === 'dark' ? '#404040' : '#e0e0e0'}` }}>
          <input
            ref={searchInputRef}
            type="text"
            placeholder="搜索表情..."
            value={searchKeyword}
            onChange={(e) => handleSearch(e.target.value)}
            style={{
              width: '100%',
              padding: '8px 12px',
              border: `1px solid ${theme === 'dark' ? '#555' : '#ddd'}`,
              borderRadius: '20px',
              outline: 'none',
              fontSize: '14px',
              backgroundColor: theme === 'dark' ? '#2a2a2a' : '#f9f9f9',
              color: theme === 'dark' ? '#fff' : '#333',
            }}
          />
        </div>
      )}

      {/* 标签栏 */}
      <div style={{
        display: 'flex',
        borderBottom: `1px solid ${theme === 'dark' ? '#404040' : '#e0e0e0'}`,
        overflowX: 'auto',
        scrollbarWidth: 'none',
      }}>
        {showFavoriteTab && (
          <button
            onClick={() => setActiveTab('recent')}
            style={{
              flex: '0 0 auto',
              padding: '8px 12px',
              border: 'none',
              background: activeTab === 'recent'
                ? (theme === 'dark' ? '#333' : '#f0f0f0')
                : 'transparent',
              color: theme === 'dark' ? '#fff' : '#666',
              cursor: 'pointer',
              fontSize: '12px',
              minWidth: '60px',
            }}
          >
            ⏰ 最近
          </button>
        )}

        {showFavoriteTab && (
          <button
            onClick={() => setActiveTab('favorite')}
            style={{
              flex: '0 0 auto',
              padding: '8px 12px',
              border: 'none',
              background: activeTab === 'favorite'
                ? (theme === 'dark' ? '#333' : '#f0f0f0')
                : 'transparent',
              color: theme === 'dark' ? '#fff' : '#666',
              cursor: 'pointer',
              fontSize: '12px',
              minWidth: '60px',
            }}
          >
            ⭐ 收藏
          </button>
        )}

        {Object.entries(categoryMap).map(([key, { name, icon }]) => {
          const hasEmojis = emojiPacks.some(pack =>
            pack.emojis.some(emoji => emoji.category === key)
          );

          if (!hasEmojis) return null;

          return (
            <button
              key={key}
              onClick={() => setActiveTab(key)}
              style={{
                flex: '0 0 auto',
                padding: '8px 12px',
                border: 'none',
                background: activeTab === key
                  ? (theme === 'dark' ? '#333' : '#f0f0f0')
                  : 'transparent',
                color: theme === 'dark' ? '#fff' : '#666',
                cursor: 'pointer',
                fontSize: '12px',
                minWidth: '60px',
              }}
            >
              <span style={{ marginRight: '2px' }}>{icon}</span>
              {name}
            </button>
          );
        })}
      </div>

      {/* 表情内容区 */}
      <div style={{
        padding: '12px',
        maxHeight: `${maxRows * 40 + 24}px`,
        overflowY: 'auto',
      }}>
        {packLoading ? (
          <div style={{ textAlign: 'center', padding: '20px', color: theme === 'dark' ? '#999' : '#666' }}>
            <div>加载中...</div>
          </div>
        ) : currentEmojis.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '20px', color: theme === 'dark' ? '#999' : '#666' }}>
            {searchKeyword ? '没有找到相关表情' : '暂无表情'}
          </div>
        ) : (
          <div style={{
            display: 'grid',
            gridTemplateColumns: `repeat(${emojisPerRow}, 1fr)`,
            gap: '4px',
          }}>
            {currentEmojis.map((emoji) => (
              <button
                key={emoji.id}
                onClick={() => handleEmojiSelect(emoji)}
                onContextMenu={(e) => toggleFavorite(emoji, e)}
                style={{
                  width: '100%',
                  aspectRatio: '1',
                  border: 'none',
                  background: 'transparent',
                  cursor: 'pointer',
                  borderRadius: '4px',
                  fontSize: '24px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  position: 'relative',
                  transition: 'background-color 0.2s',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = theme === 'dark' ? '#333' : '#f0f0f0';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = 'transparent';
                }}
                title={emoji.name}
              >
                {emoji.contentType === 'text' ? (
                  <span>{emoji.content}</span>
                ) : (
                  <img
                    src={emoji.content}
                    alt={emoji.name}
                    style={{
                      width: '24px',
                      height: '24px',
                      objectFit: 'contain',
                    }}
                    onError={(e) => {
                      // 图片加载失败时显示占位符
                      e.currentTarget.style.display = 'none';
                      e.currentTarget.parentElement!.innerHTML = '<span style="font-size: 16px;">🚫</span>';
                    }}
                  />
                )}

                {/* 收藏标识 */}
                {emoji.isFavorite && (
                  <span
                    style={{
                      position: 'absolute',
                      top: '2px',
                      right: '2px',
                      fontSize: '10px',
                      color: '#ff4757',
                    }}
                  >
                    ⭐
                  </span>
                )}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* 底部提示 */}
      <div style={{
        padding: '8px 12px',
        borderTop: `1px solid ${theme === 'dark' ? '#404040' : '#e0e0e0'}`,
        fontSize: '12px',
        color: theme === 'dark' ? '#999' : '#666',
        textAlign: 'center',
      }}>
        右键点击表情可添加/取消收藏
      </div>
    </div>
  );
};

export default EmojiPicker;