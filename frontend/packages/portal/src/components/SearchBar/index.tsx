/**
 * 搜索栏组件 - 聊天搜索大师！🔍
 *
 * @author BaSui 😎
 * @description 聊天消息搜索功能，支持关键词搜索、筛选、快捷搜索等
 * @date 2025-11-07
 */

import React, { useState, useEffect, useRef, useCallback } from 'react';
import type {
  SearchFilters,
  SearchHistory,
  SearchSuggestion,
  QuickSearch,
  SearchState,
  MessageType,
  PRESET_QUICK_SEARCHES,
} from '@campus/shared/types/search';

/**
 * 搜索栏属性
 */
export interface SearchBarProps {
  /** 搜索状态 */
  searchState: SearchState;
  /** 搜索回调 */
  onSearch: (keyword: string, filters: SearchFilters) => void;
  /** 清除搜索回调 */
  onClear: () => void;
  /** 快捷搜索回调 */
  onQuickSearch: (quickSearch: QuickSearch) => void;
  /** 搜索历史 */
  searchHistory: SearchHistory[];
  /** 搜索建议 */
  suggestions: SearchSuggestion[];
  /** 是否显示快捷搜索 */
  showQuickSearch?: boolean;
  /** 自定义样式类名 */
  className?: string;
  /** 占位符文本 */
  placeholder?: string;
  /** 是否禁用 */
  disabled?: boolean;
}

/**
 * 搜索栏组件
 */
export const SearchBar: React.FC<SearchBarProps> = ({
  searchState,
  onSearch,
  onClear,
  onQuickSearch,
  searchHistory,
  suggestions,
  showQuickSearch = true,
  className = '',
  placeholder = '搜索聊天记录...',
  disabled = false,
}) => {
  // 状态管理
  const [inputValue, setInputValue] = useState(searchState.currentKeyword);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const [focusedIndex, setFocusedIndex] = useState(-1);
  const [showFilters, setShowFilters] = useState(false);

  // 引用
  const inputRef = useRef<HTMLInputElement>(null);
  const suggestionsRef = useRef<HTMLDivElement>(null);

  // 当前筛选条件
  const [filters, setFilters] = useState<SearchFilters>({
    keyword: '',
    messageTypes: [],
    senders: [],
    dateRange: null,
    ownMessagesOnly: false,
    includeRecalled: false,
  });

  // 同步外部状态
  useEffect(() => {
    setInputValue(searchState.currentKeyword);
  }, [searchState.currentKeyword]);

  // 处理输入变化
  const handleInputChange = useCallback((value: string) => {
    setInputValue(value);
    setFilters(prev => ({ ...prev, keyword: value }));

    if (value.trim()) {
      setShowSuggestions(true);
      setShowHistory(false);
    } else {
      setShowSuggestions(false);
      setShowHistory(true);
    }
    setFocusedIndex(-1);
  }, []);

  // 处理搜索
  const handleSearch = useCallback((keyword: string = inputValue) => {
    if (keyword.trim()) {
      onSearch(keyword.trim(), { ...filters, keyword: keyword.trim() });
      setShowSuggestions(false);
      setShowHistory(false);
      setFocusedIndex(-1);
    }
  }, [inputValue, filters, onSearch]);

  // 处理清除搜索
  const handleClear = useCallback(() => {
    setInputValue('');
    setFilters({
      keyword: '',
      messageTypes: [],
      senders: [],
      dateRange: null,
      ownMessagesOnly: false,
      includeRecalled: false,
    });
    onClear();
    setShowSuggestions(false);
    setShowHistory(false);
    setFocusedIndex(-1);
    inputRef.current?.focus();
  }, [onClear]);

  // 处理键盘事件
  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();

      if (showSuggestions && focusedIndex >= 0) {
        // 选择建议项
        const suggestion = suggestions[focusedIndex];
        if (suggestion) {
          setInputValue(suggestion.text);
          handleSearch(suggestion.text);
        }
      } else {
        handleSearch();
      }
    } else if (e.key === 'Escape') {
      setShowSuggestions(false);
      setShowHistory(false);
      setFocusedIndex(-1);
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      const maxIndex = showSuggestions
        ? suggestions.length - 1
        : searchHistory.length - 1;

      if (maxIndex >= 0) {
        setFocusedIndex(prev => Math.min(prev + 1, maxIndex));
      }
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      if (focusedIndex > 0) {
        setFocusedIndex(prev => prev - 1);
      }
    }
  }, [showSuggestions, showHistory, focusedIndex, suggestions, searchHistory, handleSearch]);

  // 处理快捷搜索
  const handleQuickSearch = useCallback((quickSearch: QuickSearch) => {
    setInputValue(quickSearch.keyword);
    setFilters({ ...filters, ...quickSearch.filters });
    onQuickSearch(quickSearch);
    setShowSuggestions(false);
    setShowHistory(false);
    inputRef.current?.blur();
  }, [filters, onQuickSearch]);

  // 处理历史搜索点击
  const handleHistoryClick = useCallback((history: SearchHistory) => {
    setInputValue(history.keyword);
    setFilters({ ...filters, ...history.filters });
    handleSearch(history.keyword);
    setShowHistory(false);
    setFocusedIndex(-1);
    inputRef.current?.blur();
  }, [filters, handleSearch]);

  // 处理建议点击
  const handleSuggestionClick = useCallback((suggestion: SearchSuggestion) => {
    setInputValue(suggestion.text);
    handleSearch(suggestion.text);
    setShowSuggestions(false);
    setFocusedIndex(-1);
  }, [handleSearch]);

  // 点击外部关闭
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        suggestionsRef.current &&
        !suggestionsRef.current.contains(event.target as Node) &&
        !inputRef.current?.contains(event.target as Node)
      ) {
        setShowSuggestions(false);
        setShowHistory(false);
        setFocusedIndex(-1);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // 渲染搜索建议
  const renderSuggestions = () => {
    if (!showSuggestions || suggestions.length === 0) return null;

    return (
      <div
        ref={suggestionsRef}
        className="absolute top-full left-0 right-0 bg-white border border-gray-200 rounded-lg shadow-lg z-10 mt-1 max-h-64 overflow-y-auto"
      >
        {suggestions.map((suggestion, index) => (
          <div
            key={index}
            className={`px-4 py-3 cursor-pointer hover:bg-gray-50 flex items-center space-x-3 ${
              index === focusedIndex ? 'bg-blue-50' : ''
            }`}
            onClick={() => handleSuggestionClick(suggestion)}
          >
            {suggestion.icon && (
              <span className="text-lg">{suggestion.icon}</span>
            )}
            <div className="flex-1">
              <div className="text-sm font-medium text-gray-900">
                {suggestion.text}
              </div>
              {suggestion.description && (
                <div className="text-xs text-gray-500">
                  {suggestion.description}
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    );
  };

  // 渲染搜索历史
  const renderHistory = () => {
    if (!showHistory || searchHistory.length === 0) return null;

    return (
      <div
        ref={suggestionsRef}
        className="absolute top-full left-0 right-0 bg-white border border-gray-200 rounded-lg shadow-lg z-10 mt-1 max-h-64 overflow-y-auto"
      >
        <div className="px-4 py-2 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-gray-700">搜索历史</span>
            <button
              onClick={() => {
                // 清除历史
                localStorage.removeItem('chat_search_history');
                setShowHistory(false);
              }}
              className="text-xs text-blue-600 hover:text-blue-800"
            >
              清除
            </button>
          </div>
        </div>
        {searchHistory.map((history, index) => (
          <div
            key={history.id}
            className={`px-4 py-3 cursor-pointer hover:bg-gray-50 flex items-center justify-between ${
              index === focusedIndex ? 'bg-blue-50' : ''
            }`}
            onClick={() => handleHistoryClick(history)}
          >
            <div className="flex-1">
              <div className="text-sm text-gray-900">{history.keyword}</div>
              <div className="text-xs text-gray-500">
                {new Date(history.searchedAt).toLocaleString()}
              </div>
            </div>
            <div className="text-xs text-gray-400">
              {history.resultCount} 结果
            </div>
          </div>
        ))}
      </div>
    );
  };

  // 渲染快捷搜索
  const renderQuickSearch = () => {
    if (!showQuickSearch) return null;

    return (
      <div className="flex items-center space-x-2 p-2 border-t border-gray-200">
        <span className="text-xs text-gray-500">快捷搜索:</span>
        {PRESET_QUICK_SEARCHES.map((quickSearch, index) => (
          <button
            key={index}
            onClick={() => handleQuickSearch(quickSearch)}
            className="flex items-center space-x-1 px-2 py-1 text-xs bg-gray-100 hover:bg-gray-200 rounded-full transition-colors"
            title={quickSearch.name}
          >
            <span>{quickSearch.icon}</span>
            <span>{quickSearch.name}</span>
          </button>
        ))}
      </div>
    );
  };

  return (
    <div className={`search-bar relative ${className}`}>
      {/* 搜索输入区域 */}
      <div className="relative">
        {/* 搜索图标 */}
        <div className="absolute left-3 top-1/2 transform -translate-y-1/2">
          <svg
            className={`w-4 h-4 ${
              searchState.searching ? 'animate-spin' : ''
            } ${searchState.error ? 'text-red-500' : 'text-gray-400'}`}
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            {searchState.searching ? (
              <path
                fillRule="evenodd"
                d="M11.49 3.17c-.38-1.56-2.6-1.56-5.08 0a3.5 3.5 0 015.08 5.08 5.08 5.08-1.56 2.6-5.08 2.6a3.5 3.5 0 01-5.08-5.08zm1.41 1.41a1.5 1.5 0 012.12 2.12A5.5 5.5 0 0113.16 4.5a1.5 1.5 0 01-1.42 2.12 5.5 5.5 0 01-8.16 0z"
                clipRule="evenodd"
              />
            ) : searchState.error ? (
              <path
                fillRule="evenodd"
                d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h3a1 1 0 100-2h-3a1 1 0 00-1 1V9a1 1 0 011-1z"
                clipRule="evenodd"
              />
            ) : (
              <path
                fillRule="evenodd"
                d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
                clipRule="evenodd"
              />
            )}
          </svg>
        </div>

        {/* 搜索输入框 */}
        <input
          ref={inputRef}
          type="text"
          value={inputValue}
          onChange={(e) => handleInputChange(e.target.value)}
          onKeyDown={handleKeyDown}
          onFocus={() => {
            if (!inputValue.trim()) {
              setShowHistory(true);
            } else {
              setShowSuggestions(true);
            }
          }}
          placeholder={disabled ? '搜索已禁用' : placeholder}
          disabled={disabled}
          className={`w-full pl-10 pr-10 py-2 border ${
            searchState.error ? 'border-red-300' : 'border-gray-300'
          } rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            disabled ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'
          }`}
        />

        {/* 清除按钮 */}
        {inputValue && !disabled && (
          <button
            onClick={handleClear}
            className="absolute right-3 top-1/2 transform -translate-y-1/2 p-1 text-gray-400 hover:text-gray-600"
            title="清除搜索"
          >
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293 4.293a1 1 0 001.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 01-1.414-1.414L10 8.586l4.293-4.293a1 1 0 011.414 0z"
                clipRule="evenodd"
              />
            </svg>
          </button>
        )}

        {/* 筛选按钮 */}
        <button
          onClick={() => setShowFilters(!showFilters)}
          className={`absolute right-10 top-1/2 transform -translate-y-1/2 p-1 ${
            filters.messageTypes.length > 0 || filters.ownMessagesOnly || filters.dateRange
              ? 'text-blue-600'
              : 'text-gray-400'
          } hover:text-gray-600`}
          title="搜索筛选"
        >
          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2.586a1 1 0 01-.293.707l-2.414 2.414A1 1 0 016.414 2.414l2.414 2.414A1 1 0 0118 8.586V5a1 1 0 00-1-1H4z" />
            <path d="M8 10a2 2 0 12-2v2a2 2 0 01-2 2v-2a2 2 0 012-2v-2a2 2 0 012 0z" />
          </svg>
        </button>
      </div>

      {/* 搜索建议和历史 */}
      {renderSuggestions()}
      {renderHistory()}

      {/* 快捷搜索 */}
      {renderQuickSearch()}

      {/* 错误提示 */}
      {searchState.error && (
        <div className="mt-2 text-sm text-red-600">
          搜索出错：{searchState.error}
        </div>
      )}
    </div>
  );
};

export default SearchBar;