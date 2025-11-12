/**
 * 搜索结果组件 - 搜索结果展示专家！📊
 *
 * @author BaSui 😎
 * @description 聊天消息搜索结果的展示和高亮显示
 * @date 2025-11-07
 */

import React, { useState, useRef, useEffect } from 'react';
import type {
  SearchResult,
  TextHighlight,
  SearchResponse,
  MessageType,
} from '@campus/shared/types/search';

/**
 * 搜索结果组件属性
 */
export interface SearchResultsProps {
  /** 搜索响应结果 */
  searchResponse: SearchResponse | null;
  /** 是否正在加载 */
  loading?: boolean;
  /** 错误信息 */
  error?: string | null;
  /** 搜索关键词 */
  keyword: string;
  /** 当前用户ID */
  currentUserId: number;
  /** 跳转到消息的回调 */
  onJumpToMessage?: (messageId: string, timestamp: string) => void;
  /** 自定义样式类名 */
  className?: string;
  /** 是否显示统计信息 */
  showStatistics?: boolean;
}

/**
 * 文本高亮组件
 */
const TextHighlight: React.FC<{
  text: string;
  highlights: TextHighlight[];
  className?: string;
}> = ({ text, highlights, className = '' }) => {
  if (!highlights || highlights.length === 0) {
    return <span className={className}>{text}</span>;
  }

  return (
    <span className={className}>
      {highlights.map((highlight, index) => (
        <span
          key={index}
          className={
            highlight.isMatch
              ? 'bg-yellow-200 text-yellow-900 font-medium px-1 py-0.5 rounded'
              : ''
          }
        >
          {highlight.text}
        </span>
      ))}
    </span>
  );
};

/**
 * 搜索结果项组件
 */
const SearchResultItem: React.FC<{
  result: SearchResult;
  keyword: string;
  currentUserId: number;
  onJumpToMessage?: (messageId: string, timestamp: string) => void;
}> = ({ result, keyword, currentUserId, onJumpToMessage }) => {
  const isOwn = result.isOwn;
  const messageTime = new Date(result.timestamp).toLocaleString();

  // 获取消息类型图标
  const getMessageIcon = (type: MessageType) => {
    switch (type) {
      case MessageType.TEXT:
        return '💬';
      case MessageType.IMAGE:
        return '🖼️';
      case MessageType.FILE:
        return '📎';
      case MessageType.EMOJI:
        return '😊';
      default:
        return '💬';
    }
  };

  // 获取发送者角色样式
  const getRoleStyle = (role: string) => {
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

  return (
    <div
      className={`search-result-item p-4 border-b border-gray-200 hover:bg-gray-50 cursor-pointer transition-colors ${
        isOwn ? 'bg-blue-50' : 'bg-white'
      }`}
      onClick={() => onJumpToMessage?.(result.messageId, result.timestamp)}
    >
      <div className="flex items-start space-x-3">
        {/* 消息类型图标 */}
        <div className="flex-shrink-0 w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
          <span className="text-lg">{getMessageIcon(result.messageType)}</span>
        </div>

        {/* 消息内容 */}
        <div className="flex-1 min-w-0">
          {/* 发送者信息 */}
          <div className="flex items-center space-x-2 mb-2">
            <span
              className={`inline-flex items-center px-2 py-1 rounded text-xs font-medium ${getRoleStyle(
                result.sender.role
              )}`}
            >
              {result.sender.name}
            </span>
            <span className="text-xs text-gray-500">{messageTime}</span>
            {isOwn && (
              <span className="text-xs text-blue-600 bg-blue-100 px-2 py-1 rounded">我</span>
            )}
          </div>

          {/* 消息内容 */}
          <div className="text-sm text-gray-900">
            <TextHighlight
              text={result.content}
              highlights={result.highlights}
              className="break-words"
            />
          </div>

          {/* 匹配关键词 */}
          {result.matchedKeywords.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-2">
              {result.matchedKeywords.map((keyword, index) => (
                <span
                  key={index}
                  className="inline-flex items-center px-2 py-1 bg-green-100 text-green-800 text-xs rounded-full"
                >
                  {keyword}
                </span>
              ))}
            </div>
          )}
        </div>

        {/* 相关性得分 */}
        <div className="flex-shrink-0 text-xs text-gray-400">
          {Math.round(result.score * 100)}%
        </div>
      </div>
    </div>
  );
};

/**
 * 搜索结果组件
 */
export const SearchResults: React.FC<SearchResultsProps> = ({
  searchResponse,
  loading = false,
  error = null,
  keyword,
  currentUserId,
  onJumpToMessage,
  className = '',
  showStatistics = true,
}) => {
  // 结果列表引用
  const resultsListRef = useRef<HTMLDivElement>(null);

  // 处理滚动
  const handleScroll = () => {
    const { scrollTop, scrollHeight, clientHeight } = resultsListRef.current || {};
    const isNearBottom = scrollHeight - scrollTop - clientHeight < 100;

    // 这里可以实现加载更多结果的逻辑
    if (isNearBottom && searchResponse?.pagination.hasNext) {
      // onLoadMore();
    }
  };

  // 添加滚动监听
  useEffect(() => {
    const listElement = resultsListRef.current;
    if (listElement) {
      listElement.addEventListener('scroll', handleScroll);
      return () => listElement.removeEventListener('scroll', handleScroll);
    }
  }, []);

  // 加载状态
  if (loading) {
    return (
      <div className={`search-results-loading flex flex-col items-center justify-center py-8 ${className}`}>
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mb-4"></div>
        <p className="text-gray-500">搜索中...</p>
      </div>
    );
  }

  // 错误状态
  if (error) {
    return (
      <div className={`search-results-error flex flex-col items-center justify-center py-8 ${className}`}>
        <svg className="w-12 h-12 text-red-400 mb-4" fill="currentColor" viewBox="0 0 20 20">
          <path
            fillRule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 1 1 0 00-2zm0 2a1 1 0 10-2 1 1 0 00-2z"
            clipRule="evenodd"
          />
        </svg>
        <p className="text-red-600 mb-2">搜索出错</p>
        <p className="text-sm text-gray-500">{error}</p>
      </div>
    );
  }

  // 无结果状态
  if (!searchResponse || searchResponse.results.length === 0) {
    return (
      <div className={`search-results-empty flex flex-col items-center justify-center py-8 ${className}`}>
        <svg className="w-12 h-12 text-gray-300 mb-4" fill="currentColor" viewBox="0 0 20 20">
          <path
            fillRule="evenodd"
            d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
            clipRule="evenodd"
          />
        </svg>
        <p className="text-gray-500 mb-2">未找到相关消息</p>
        <p className="text-sm text-gray-400">
          尝试使用其他关键词或调整筛选条件
        </p>
      </div>
    );
  }

  return (
    <div className={`search-results ${className}`}>
      {/* 统计信息 */}
      {showStatistics && (
        <div className="search-statistics p-4 bg-gray-50 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <div className="text-sm text-gray-600">
              找到 <span className="font-medium text-gray-900">{searchResponse.pagination.total}</span> 条相关消息
            </div>
            <div className="text-xs text-gray-500">
              耗时 {searchResponse.statistics.searchTime}ms
            </div>
          </div>
          {searchResponse.statistics.matchedKeywords.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-2">
              <span className="text-xs text-gray-500">匹配关键词:</span>
              {searchResponse.statistics.matchedKeywords.map((kw, index) => (
                <span
                  key={index}
                  className="inline-flex items-center px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full"
                >
                  {kw}
                </span>
              ))}
            </div>
          )}
        </div>
      )}

      {/* 搜索结果列表 */}
      <div
        ref={resultsListRef}
        className="search-results-list max-h-96 overflow-y-auto"
      >
        {searchResponse.results.map((result) => (
          <SearchResultItem
            key={result.messageId}
            result={result}
            keyword={keyword}
            currentUserId={currentUserId}
            onJumpToMessage={onJumpToMessage}
          />
        ))}
      </div>

      {/* 分页信息 */}
      {searchResponse.pagination.totalPages > 1 && (
        <div className="search-pagination p-4 border-t border-gray-200">
          <div className="flex items-center justify-between">
            <div className="text-sm text-gray-600">
              第 {searchResponse.pagination.page + 1} 页，共{' '}
              {searchResponse.pagination.totalPages} 页
            </div>
            <div className="text-sm text-gray-600">
              共 {searchResponse.pagination.total} 条结果
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SearchResults;