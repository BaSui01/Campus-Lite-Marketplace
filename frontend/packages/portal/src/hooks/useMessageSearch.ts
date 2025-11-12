/**
 * 消息搜索Hook - 搜索状态管理大师！🔍
 *
 * @author BaSui 😎
 * @description 管理聊天消息搜索的状态和逻辑
 * @date 2025-11-07
 */

import { useState, useCallback, useEffect, useMemo } from 'react';
import { searchService } from '@/services/search';
import type {
  SearchFilters,
  SearchState,
  SearchRequest,
  SearchResponse,
  SearchSuggestion,
  SearchHistory,
  SearchOptions,
  MessageType,
  SEARCH_CONFIG,
} from '@campus/shared/types/search';

/**
 * 使用消息搜索的Hook
 */
export const useMessageSearch = (currentUserId: number, disputeId: number) => {
  // 搜索状态
  const [searchState, setSearchState] = useState<SearchState>({
    searching: false,
    currentKeyword: '',
    currentFilters: {
      keyword: '',
      messageTypes: [],
      senders: [],
      dateRange: null,
      ownMessagesOnly: false,
      includeRecalled: false,
    },
    results: [],
    error: null,
    suggestions: [],
    showAdvancedFilters: false,
  });

  // 搜索结果
  const [searchResponse, setSearchResponse] = useState<SearchResponse | null>(null);

  // 搜索历史
  const [searchHistory, setSearchHistory] = useState<SearchHistory[]>([]);

  // 搜索选项
  const [searchOptions, setSearchOptions] = useState<SearchOptions>({
    pageSize: SEARCH_CONFIG.DEFAULT_PAGE_SIZE,
    maxResults: SEARCH_CONFIG.MAX_RESULTS,
    fuzzySearch: true,
    pinyinSearch: true,
    sortBy: 'relevance',
    sortOrder: 'desc',
  });

  // 加载搜索历史
  useEffect(() => {
    const history = searchService.getSearchHistory();
    setSearchHistory(history);
  }, []);

  // 执行搜索
  const performSearch = useCallback(async (keyword: string, filters: SearchFilters) => {
    if (!keyword.trim() && !filters.messageTypes.length && !filters.senders.length && !filters.dateRange) {
      setSearchResponse(null);
      setSearchState(prev => ({
        ...prev,
        searching: false,
        currentKeyword: '',
        results: [],
        error: null,
      }));
      return;
    }

    setSearchState(prev => ({
      ...prev,
      searching: true,
      currentKeyword: keyword,
      currentFilters: filters,
      error: null,
    }));

    try {
      const request: SearchRequest = {
        keyword,
        filters: {
          ...filters,
          keyword,
        },
        options: searchOptions,
        pagination: {
          page: 0,
          pageSize: searchOptions.pageSize,
        },
      };

      const response = await searchService.searchMessages(request);
      setSearchResponse(response);
      setSearchState(prev => ({
        ...prev,
        searching: false,
        results: response.results,
        error: null,
      }));

      // 更新搜索历史
      const updatedHistory = searchService.getSearchHistory();
      setSearchHistory(updatedHistory);
    } catch (error) {
      console.error('搜索失败:', error);
      setSearchState(prev => ({
        ...prev,
        searching: false,
        error: error instanceof Error ? error.message : '搜索失败',
        results: [],
      }));
    }
  }, [searchOptions]);

  // 搜索建议
  const getSuggestions = useCallback(async (keyword: string) => {
    if (!keyword.trim()) {
      setSearchState(prev => ({ ...prev, suggestions: [] }));
      return;
    }

    try {
      const suggestions = await searchService.getSearchSuggestions(keyword);
      setSearchState(prev => ({ ...prev, suggestions }));
    } catch (error) {
      console.error('获取搜索建议失败:', error);
      setSearchState(prev => ({ ...prev, suggestions: [] }));
    }
  }, []);

  // 清除搜索
  const clearSearch = useCallback(() => {
    setSearchResponse(null);
    setSearchState(prev => ({
      ...prev,
      currentKeyword: '',
      results: [],
      error: null,
      suggestions: [],
    }));
  }, []);

  // 加载更多结果
  const loadMoreResults = useCallback(async () => {
    if (!searchResponse || !searchResponse.pagination.hasNext || searchState.searching) {
      return;
    }

    setSearchState(prev => ({ ...prev, searching: true }));

    try {
      const nextPage = searchResponse.pagination.page + 1;
      const request: SearchRequest = {
        keyword: searchState.currentKeyword,
        filters: searchState.currentFilters,
        options: searchOptions,
        pagination: {
          page: nextPage,
          pageSize: searchOptions.pageSize,
        },
      };

      const response = await searchService.searchMessages(request);

      // 合并结果
      const mergedResponse: SearchResponse = {
        ...response,
        results: [...searchResponse.results, ...response.results],
      };

      setSearchResponse(mergedResponse);
      setSearchState(prev => ({
        ...prev,
        searching: false,
        results: mergedResponse.results,
      }));
    } catch (error) {
      console.error('加载更多结果失败:', error);
      setSearchState(prev => ({
        ...prev,
        searching: false,
        error: error instanceof Error ? error.message : '加载失败',
      }));
    }
  }, [searchResponse, searchState.searching, searchState.currentKeyword, searchState.currentFilters, searchOptions]);

  // 跳转到消息
  const jumpToMessage = useCallback((messageId: string, timestamp: string) => {
    // 这里可以实现跳转到指定消息的逻辑
    // 比如滚动到指定消息位置，高亮显示等
    console.log('跳转到消息:', messageId, timestamp);

    // 通过事件通知父组件或其他组件
    const event = new CustomEvent('jumpToMessage', {
      detail: { messageId, timestamp }
    });
    document.dispatchEvent(event);

    // 通过另一个事件通知ChatInterface组件
    const chatEvent = new CustomEvent('highlightMessage', {
      detail: { messageId, keyword: searchState.currentKeyword }
    });
    document.dispatchEvent(chatEvent);
  }, [searchState.currentKeyword]);

  // 切换高级筛选
  const toggleAdvancedFilters = useCallback(() => {
    setSearchState(prev => ({
      ...prev,
      showAdvancedFilters: !prev.showAdvancedFilters,
    }));
  }, []);

  // 更新搜索选项
  const updateSearchOptions = useCallback((options: Partial<SearchOptions>) => {
    setSearchOptions(prev => ({ ...prev, ...options }));
  }, []);

  // 添加到搜索历史
  const addToHistory = useCallback((keyword: string, filters: Partial<SearchFilters>) => {
    // 这个逻辑已经在searchService中处理了
    // 这里只是为了提供外部调用接口
    const history = searchService.getSearchHistory();
    setSearchHistory(history);
  }, []);

  // 清除搜索历史
  const clearHistory = useCallback(() => {
    searchService.clearSearchHistory();
    setSearchHistory([]);
  }, []);

  // 搜索统计信息
  const searchStatistics = useMemo(() => {
    return searchService.getSearchStatistics();
  }, [searchHistory]);

  // 计算值：是否有搜索结果
  const hasResults = useMemo(() => {
    return searchResponse && searchResponse.results.length > 0;
  }, [searchResponse]);

  // 计算值：是否正在加载
  const isLoading = useMemo(() => {
    return searchState.searching;
  }, [searchState.searching]);

  // 计算值：搜索错误
  const searchError = useMemo(() => {
    return searchState.error;
  }, [searchState.error]);

  // 计算值：当前搜索关键词
  const currentKeyword = useMemo(() => {
    return searchState.currentKeyword;
  }, [searchState.currentKeyword]);

  return {
    // 状态
    searchState,
    searchResponse,
    searchHistory,
    searchOptions,

    // 计算值
    hasResults,
    isLoading,
    searchError,
    currentKeyword,
    searchStatistics,

    // 方法
    performSearch,
    getSuggestions,
    clearSearch,
    loadMoreResults,
    jumpToMessage,
    toggleAdvancedFilters,
    updateSearchOptions,
    addToHistory,
    clearHistory,
  };
};

export default useMessageSearch;