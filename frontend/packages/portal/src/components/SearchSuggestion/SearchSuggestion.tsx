/**
 * 搜索建议组件 🔍
 * @author BaSui 😎
 * @description 提供搜索历史、热门推荐、智能建议功能
 * @date 2025-11-08
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './SearchSuggestion.css';

/**
 * 搜索建议项类型
 */
export interface SearchSuggestionItem {
  id: string;
  text: string;
  type: 'history' | 'hot' | 'suggestion';
  icon?: string;
  count?: number; // 搜索次数（热门搜索用）
}

/**
 * 搜索建议组件Props
 */
export interface SearchSuggestionProps {
  visible: boolean;
  keyword: string;
  onSelect: (keyword: string) => void;
  onClose: () => void;
}

/**
 * 热门搜索数据（模拟，后期可从后端获取）
 */
const HOT_SEARCHES: SearchSuggestionItem[] = [
  { id: 'hot-1', text: '二手教材', type: 'hot', icon: '📚', count: 1234 },
  { id: 'hot-2', text: 'iPhone 13', type: 'hot', icon: '📱', count: 890 },
  { id: 'hot-3', text: '自行车', type: 'hot', icon: '🚲', count: 765 },
  { id: 'hot-4', text: '小台灯', type: 'hot', icon: '💡', count: 543 },
  { id: 'hot-5', text: '考研资料', type: 'hot', icon: '📖', count: 432 },
];

/**
 * 本地存储Key
 */
const STORAGE_KEY = 'campus_search_history';

/**
 * 搜索建议组件
 */
export const SearchSuggestion: React.FC<SearchSuggestionProps> = ({
  visible,
  keyword,
  onSelect,
  onClose,
}) => {
  const navigate = useNavigate();
  const [searchHistory, setSearchHistory] = useState<SearchSuggestionItem[]>([]);
  const [suggestions, setSuggestions] = useState<SearchSuggestionItem[]>([]);

  /**
   * 加载搜索历史
   */
  useEffect(() => {
    const historyStr = localStorage.getItem(STORAGE_KEY);
    if (historyStr) {
      try {
        const history = JSON.parse(historyStr);
        setSearchHistory(history.slice(0, 5)); // 最多显示5条历史
      } catch (err) {
        console.error('解析搜索历史失败:', err);
      }
    }
  }, []);

  /**
   * 根据关键词生成搜索建议
   */
  useEffect(() => {
    if (!keyword.trim()) {
      setSuggestions([]);
      return;
    }

    // 简单的建议逻辑（后期可调用后端API）
    const keywordLower = keyword.toLowerCase();
    const matchedHot = HOT_SEARCHES.filter((item) =>
      item.text.toLowerCase().includes(keywordLower)
    ).map((item) => ({
      ...item,
      id: `suggestion-${item.id}`,
      type: 'suggestion' as const,
    }));

    setSuggestions(matchedHot.slice(0, 5));
  }, [keyword]);

  /**
   * 保存搜索历史
   */
  const saveSearchHistory = (text: string) => {
    if (!text.trim()) return;

    const newItem: SearchSuggestionItem = {
      id: `history-${Date.now()}`,
      text,
      type: 'history',
      icon: '🕐',
    };

    // 去重并添加到历史
    const updatedHistory = [
      newItem,
      ...searchHistory.filter((item) => item.text !== text),
    ].slice(0, 10); // 最多保存10条

    setSearchHistory(updatedHistory);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updatedHistory));
  };

  /**
   * 清空搜索历史
   */
  const clearHistory = () => {
    setSearchHistory([]);
    localStorage.removeItem(STORAGE_KEY);
  };

  /**
   * 处理选择搜索建议
   */
  const handleSelect = (item: SearchSuggestionItem) => {
    saveSearchHistory(item.text);
    onSelect(item.text);
  };

  /**
   * 删除单条历史记录
   */
  const handleDeleteHistory = (e: React.MouseEvent, itemId: string) => {
    e.stopPropagation();
    const updatedHistory = searchHistory.filter((item) => item.id !== itemId);
    setSearchHistory(updatedHistory);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updatedHistory));
  };

  if (!visible) return null;

  return (
    <>
      {/* 遮罩层 */}
      <div className="search-suggestion-overlay" onClick={onClose} />

      {/* 建议面板 */}
      <div className="search-suggestion-panel">
        {/* ==================== 智能建议（有关键词时显示）==================== */}
        {keyword.trim() && suggestions.length > 0 && (
          <div className="search-suggestion-section">
            <div className="search-suggestion-header">
              <span className="search-suggestion-title">💡 智能建议</span>
            </div>
            <div className="search-suggestion-list">
              {suggestions.map((item) => (
                <div
                  key={item.id}
                  className="search-suggestion-item"
                  onClick={() => handleSelect(item)}
                >
                  <span className="search-suggestion-icon">{item.icon}</span>
                  <span className="search-suggestion-text">{item.text}</span>
                  {item.count && (
                    <span className="search-suggestion-count">
                      {item.count > 999 ? '999+' : item.count}次
                    </span>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ==================== 搜索历史（无关键词时显示）==================== */}
        {!keyword.trim() && searchHistory.length > 0 && (
          <div className="search-suggestion-section">
            <div className="search-suggestion-header">
              <span className="search-suggestion-title">🕐 搜索历史</span>
              <button
                className="search-suggestion-clear"
                onClick={clearHistory}
              >
                清空
              </button>
            </div>
            <div className="search-suggestion-list">
              {searchHistory.map((item) => (
                <div
                  key={item.id}
                  className="search-suggestion-item"
                  onClick={() => handleSelect(item)}
                >
                  <span className="search-suggestion-icon">{item.icon}</span>
                  <span className="search-suggestion-text">{item.text}</span>
                  <button
                    className="search-suggestion-delete"
                    onClick={(e) => handleDeleteHistory(e, item.id)}
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ==================== 热门搜索（无关键词时显示）==================== */}
        {!keyword.trim() && (
          <div className="search-suggestion-section">
            <div className="search-suggestion-header">
              <span className="search-suggestion-title">🔥 热门搜索</span>
            </div>
            <div className="search-suggestion-list">
              {HOT_SEARCHES.map((item, index) => (
                <div
                  key={item.id}
                  className="search-suggestion-item search-suggestion-item--hot"
                  onClick={() => handleSelect(item)}
                >
                  <span
                    className={`search-suggestion-rank search-suggestion-rank--${
                      index < 3 ? 'top' : 'normal'
                    }`}
                  >
                    {index + 1}
                  </span>
                  <span className="search-suggestion-icon">{item.icon}</span>
                  <span className="search-suggestion-text">{item.text}</span>
                  <span className="search-suggestion-count">
                    {item.count && item.count > 999 ? '999+' : item.count}次
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ==================== 空状态 ==================== */}
        {!keyword.trim() && searchHistory.length === 0 && (
          <div className="search-suggestion-empty">
            <div className="search-suggestion-empty-icon">🔍</div>
            <div className="search-suggestion-empty-text">
              暂无搜索历史，试试热门搜索吧！
            </div>
          </div>
        )}
      </div>
    </>
  );
};

export default SearchSuggestion;
