/**
 * 搜索面板组件 - 聊天搜索界面大师！🔍
 *
 * @author BaSui 😎
 * @description 集成搜索功能的聊天界面面板
 * @date 2025-11-07
 */

import React, { useState, useEffect } from 'react';
import SearchBar from '@/components/SearchBar';
import { SearchResults } from '@/components/SearchResults';
import { useMessageSearch } from '@/hooks/useMessageSearch';
import type { SearchFilters, QuickSearch } from '@campus/shared/types/search';

/**
 * 搜索面板属性
 */
export interface SearchPanelProps {
  /** 当前用户ID */
  currentUserId: number;
  /** 纠纷ID */
  disputeId: number;
  /** 是否显示搜索面板 */
  visible: boolean;
  /** 关闭搜索面板的回调 */
  onClose: () => void;
  /** 自定义样式类名 */
  className?: string;
  /** 是否显示高级筛选 */
  showAdvancedFilters?: boolean;
  /** 占位符文本 */
  placeholder?: string;
}

/**
 * 搜索面板组件
 */
export const SearchPanel: React.FC<SearchPanelProps> = ({
  currentUserId,
  disputeId,
  visible,
  onClose,
  className = '',
  showAdvancedFilters = true,
  placeholder = '搜索聊天记录...',
}) => {
  // 本地状态
  const [isMinimized, setIsMinimized] = useState(false);

  // 使用搜索Hook
  const {
    searchState,
    searchResponse,
    searchHistory,
    hasResults,
    isLoading,
    searchError,
    performSearch,
    getSuggestions,
    clearSearch,
    loadMoreResults,
    jumpToMessage,
    clearHistory,
  } = useMessageSearch(currentUserId, disputeId);

  // 处理搜索
  const handleSearch = (keyword: string, filters: SearchFilters) => {
    performSearch(keyword, filters);
  };

  // 处理清除搜索
  const handleClear = () => {
    clearSearch();
  };

  // 处理快捷搜索
  const handleQuickSearch = (quickSearch: QuickSearch) => {
    performSearch(quickSearch.keyword, {
      ...quickSearch.filters,
      keyword: quickSearch.keyword,
    });
  };

  // 处理跳转到消息
  const handleJumpToMessage = (messageId: string, timestamp: string) => {
    jumpToMessage(messageId, timestamp);
    // 可以选择关闭搜索面板或最小化
    setIsMinimized(true);
  };

  // 处理键盘事件
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && visible) {
        if (isMinimized) {
          setIsMinimized(false);
        } else {
          onClose();
        }
      }

      // Ctrl+F 或 Cmd+F 快速打开搜索
      if ((event.ctrlKey || event.metaKey) && event.key === 'f') {
        event.preventDefault();
        if (!visible) {
          // 这里可以通过props通知父组件显示搜索面板
          console.log('请求显示搜索面板');
        }
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [visible, isMinimized, onClose]);

  // 监听跳转到消息事件
  useEffect(() => {
    const handleJumpEvent = (event: CustomEvent) => {
      const { messageId, timestamp } = event.detail;
      // 这里可以添加额外处理逻辑
      console.log('搜索面板接收到跳转消息事件:', messageId, timestamp);
    };

    document.addEventListener('jumpToMessage', handleJumpEvent as EventListener);
    return () => document.removeEventListener('jumpToMessage', handleJumpEvent as EventListener);
  }, []);

  // 如果面板不可见，不渲染
  if (!visible) {
    return null;
  }

  return (
    <div className={`search-panel fixed inset-y-0 right-0 w-96 bg-white shadow-2xl z-50 flex flex-col ${className}`}>
      {/* 头部 */}
      <div className="search-panel-header flex items-center justify-between p-4 border-b border-gray-200">
        <h3 className="text-lg font-semibold text-gray-900">搜索聊天记录</h3>
        <div className="flex items-center space-x-2">
          {/* 最小化按钮 */}
          <button
            onClick={() => setIsMinimized(!isMinimized)}
            className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition-colors"
            title={isMinimized ? "展开" : "最小化"}
          >
            <svg
              className="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              {isMinimized ? (
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 8V4m0 0h4M4 4l5 5m11-5h-4m4 0v4m0-4l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5h-4m4 0v-4m0 4l-5-5"
                />
              ) : (
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M20 12H4"
                />
              )}
            </svg>
          </button>

          {/* 关闭按钮 */}
          <button
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-red-600 rounded-lg hover:bg-red-50 transition-colors"
            title="关闭"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
      </div>

      {/* 搜索栏 */}
      <div className="search-panel-bar p-4 border-b border-gray-200">
        <SearchBar
          searchState={searchState}
          onSearch={handleSearch}
          onClear={handleClear}
          onQuickSearch={handleQuickSearch}
          searchHistory={searchHistory}
          suggestions={searchState.suggestions}
          showQuickSearch={true}
          placeholder={placeholder}
        />
      </div>

      {/* 搜索结果区域 */}
      <div className={`search-panel-content flex-1 overflow-hidden ${isMinimized ? 'hidden' : 'block'}`}>
        {hasResults && (
          <SearchResults
            searchResponse={searchResponse!}
            loading={isLoading}
            error={searchError}
            keyword={searchState.currentKeyword}
            currentUserId={currentUserId}
            onJumpToMessage={handleJumpToMessage}
            showStatistics={true}
            className="h-full"
          />
        )}

        {/* 空状态 */}
        {!hasResults && !isLoading && !searchError && searchState.currentKeyword && (
          <div className="flex flex-col items-center justify-center h-full text-gray-500">
            <svg className="w-16 h-16 mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M12 12h.01M12 12h-.01M12 12h.01M12 12h-.01M12 12h.01M12 12h-.01M12 12h.01M12 12h-.01M12 12h.01M12 12h-.01"
              />
            </svg>
            <p className="text-lg font-medium mb-2">未找到相关消息</p>
            <p className="text-sm text-gray-400 text-center max-w-xs">
              尝试使用不同的关键词或调整筛选条件
            </p>
          </div>
        )}

        {/* 欢迎状态 */}
        {!hasResults && !isLoading && !searchError && !searchState.currentKeyword && (
          <div className="flex flex-col items-center justify-center h-full text-gray-500">
            <svg className="w-16 h-16 mb-4 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
                clipRule="evenodd"
              />
            </svg>
            <p className="text-lg font-medium mb-2">搜索聊天记录</p>
            <p className="text-sm text-gray-400 text-center max-w-xs mb-4">
              输入关键词开始搜索，支持筛选消息类型、发送者和时间范围
            </p>
            <div className="text-xs text-gray-400 space-y-1">
              <p>💡 使用快捷键 <kbd className="px-2 py-1 bg-gray-100 rounded">Ctrl+F</kbd> 快速打开搜索</p>
              <p>💡 按 <kbd className="px-2 py-1 bg-gray-100 rounded">Esc</kbd> 关闭搜索面板</p>
            </div>
          </div>
        )}
      </div>

      {/* 加载更多按钮 */}
      {hasResults && searchResponse?.pagination.hasNext && (
        <div className="search-panel-footer p-4 border-t border-gray-200">
          <button
            onClick={loadMoreResults}
            disabled={isLoading}
            className="w-full py-2 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
          >
            {isLoading ? (
              <div className="flex items-center justify-center">
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                加载中...
              </div>
            ) : (
              '加载更多结果'
            )}
          </button>
        </div>
      )}
    </div>
  );
};

export default SearchPanel;