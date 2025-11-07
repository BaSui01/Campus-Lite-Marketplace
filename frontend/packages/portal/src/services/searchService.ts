/**
 * 搜索服务 - 聊天消息搜索专家！🔍
 *
 * @author BaSui 😎
 * @description 处理聊天消息搜索功能，包括关键词搜索、筛选、高亮显示等
 * @date 2025-11-07
 */

import { messageService } from './message';
import type {
  SearchRequest,
  SearchResponse,
  SearchResult,
  SearchFilters,
  SearchHistory,
  SearchSuggestion,
  SearchStatistics,
  SearchOptions,
  MessageType,
  SEARCH_CONFIG,
} from '@campus/shared/types/search';

/**
 * 搜索服务类
 */
class SearchService {
  // 搜索历史缓存
  private searchHistory: SearchHistory[] = [];

  // 搜索建议缓存
  private suggestionCache = new Map<string, SearchSuggestion[]>();

  // 统计信息缓存
  private statisticsCache: SearchStatistics | null = null;

  constructor() {
    this.loadSearchHistory();
  }

  /**
   * 执行搜索
   */
  async searchMessages(request: SearchRequest): Promise<SearchResponse> {
    try {
      // 模拟搜索延迟
      await this.simulateDelay(200 + Math.random() * 300);

      // 获取消息数据
      const messages = await this.getMessagesForSearch(request.filters);

      // 执行搜索匹配
      const results = this.performSearch(messages, request);

      // 排序结果
      const sortedResults = this.sortResults(results, request.options);

      // 分页处理
      const paginatedResults = this.paginateResults(
        sortedResults,
        request.pagination.page,
        request.pagination.pageSize
      );

      // 生成匹配关键词
      const matchedKeywords = this.extractMatchedKeywords(request.keyword, results);

      // 记录搜索历史
      this.saveSearchHistory({
        id: this.generateId(),
        keyword: request.keyword,
        searchedAt: new Date().toISOString(),
        resultCount: results.length,
        filters: request.filters,
      });

      // 更新统计信息
      this.updateStatistics(request.keyword, results.length > 0);

      return {
        results: paginatedResults.items,
        pagination: {
          page: request.pagination.page,
          pageSize: request.pagination.pageSize,
          total: paginatedResults.total,
          totalPages: Math.ceil(paginatedResults.total / request.pagination.pageSize),
          hasNext: request.pagination.page + 1 < Math.ceil(paginatedResults.total / request.pagination.pageSize),
          hasPrev: request.pagination.page > 0,
        },
        statistics: {
          totalResults: results.length,
          searchTime: Date.now(), // 模拟搜索耗时
          matchedKeywords,
        },
      };
    } catch (error) {
      console.error('搜索失败:', error);
      throw new Error('搜索服务暂时不可用，请稍后重试');
    }
  }

  /**
   * 获取搜索建议
   */
  async getSearchSuggestions(keyword: string): Promise<SearchSuggestion[]> => {
    if (!keyword.trim()) {
      return [];
    }

    // 检查缓存
    const cacheKey = keyword.toLowerCase();
    if (this.suggestionCache.has(cacheKey)) {
      return this.suggestionCache.get(cacheKey)!;
    }

    try {
      // 模拟API调用
      await this.simulateDelay(100);

      const suggestions: SearchSuggestion[] = [];

      // 关键词建议
      const keywordSuggestions = this.generateKeywordSuggestions(keyword);
      suggestions.push(...keywordSuggestions);

      // 用户名建议
      const userSuggestions = await this.generateUserSuggestions(keyword);
      suggestions.push(...userSuggestions);

      // 日期建议
      const dateSuggestions = this.generateDateSuggestions(keyword);
      suggestions.push(...dateSuggestions);

      // 缓存结果
      this.suggestionCache.set(cacheKey, suggestions.slice(0, SEARCH_CONFIG.MAX_SUGGESTIONS));

      return suggestions.slice(0, SEARCH_CONFIG.MAX_SUGGESTIONS);
    } catch (error) {
      console.error('获取搜索建议失败:', error);
      return [];
    }
  }

  /**
   * 获取搜索历史
   */
  getSearchHistory(): SearchHistory[] {
    return this.searchHistory
      .sort((a, b) => new Date(b.searchedAt).getTime() - new Date(a.searchedAt).getTime())
      .slice(0, SEARCH_CONFIG.MAX_HISTORY_ITEMS);
  }

  /**
   * 清除搜索历史
   */
  clearSearchHistory(): void {
    this.searchHistory = [];
    localStorage.removeItem('chat_search_history');
  }

  /**
   * 获取搜索统计信息
   */
  async getSearchStatistics(): Promise<SearchStatistics> {
    if (this.statisticsCache) {
      return this.statisticsCache;
    }

    try {
      // 模拟API调用
      await this.simulateDelay(150);

      const history = this.getSearchHistory();
      const keywordCount = new Map<string, number>();

      history.forEach(item => {
        keywordCount.set(item.keyword, (keywordCount.get(item.keyword) || 0) + 1);
      });

      const popularKeywords = Array.from(keywordCount.entries())
        .sort((a, b) => b[1] - a[1])
        .slice(0, 10)
        .map(([keyword, count]) => ({ keyword, count }));

      this.statisticsCache = {
        totalSearches: history.length,
        popularKeywords,
        recentSearches: history.slice(0, 5),
        successRate: history.length > 0 ?
          (history.filter(item => item.resultCount > 0).length / history.length) * 100 : 0,
      };

      return this.statisticsCache;
    } catch (error) {
      console.error('获取搜索统计失败:', error);
      return {
        totalSearches: 0,
        popularKeywords: [],
        recentSearches: [],
        successRate: 0,
      };
    }
  }

  /**
   * 高亮搜索关键词
   */
  highlightSearchText(text: string, keyword: string): Array<{ text: string; isMatch: boolean }> {
    if (!keyword.trim()) {
      return [{ text, isMatch: false }];
    }

    const regex = new RegExp(this.escapeRegExp(keyword), 'gi');
    const matches = Array.from(text.matchAll(regex));

    if (matches.length === 0) {
      return [{ text, isMatch: false }];
    }

    const result: Array<{ text: string; isMatch: boolean }> = [];
    let lastIndex = 0;

    matches.forEach(match => {
      // 添加匹配前的文本
      if (match.index! > lastIndex) {
        result.push({
          text: text.substring(lastIndex, match.index!),
          isMatch: false,
        });
      }

      // 添加匹配的文本
      result.push({
        text: match[0],
        isMatch: true,
      });

      lastIndex = match.index! + match[0].length;
    });

    // 添加剩余文本
    if (lastIndex < text.length) {
      result.push({
        text: text.substring(lastIndex),
        isMatch: false,
      });
    }

    return result;
  }

  /**
   * 私有方法：加载搜索历史
   */
  private loadSearchHistory(): void {
    try {
      const stored = localStorage.getItem('chat_search_history');
      if (stored) {
        this.searchHistory = JSON.parse(stored);
      }
    } catch (error) {
      console.error('加载搜索历史失败:', error);
      this.searchHistory = [];
    }
  }

  /**
   * 私有方法：保存搜索历史
   */
  private saveSearchHistory(history: SearchHistory): void {
    // 避免重复记录
    const existingIndex = this.searchHistory.findIndex(item => item.keyword === history.keyword);
    if (existingIndex >= 0) {
      this.searchHistory.splice(existingIndex, 1);
    }

    this.searchHistory.unshift(history);

    // 限制历史记录数量
    if (this.searchHistory.length > SEARCH_CONFIG.MAX_HISTORY_ITEMS) {
      this.searchHistory = this.searchHistory.slice(0, SEARCH_CONFIG.MAX_HISTORY_ITEMS);
    }

    // 持久化存储
    try {
      localStorage.setItem('chat_search_history', JSON.stringify(this.searchHistory));
    } catch (error) {
      console.error('保存搜索历史失败:', error);
    }
  }

  /**
   * 私有方法：获取用于搜索的消息
   */
  private async getMessagesForSearch(filters: SearchFilters): Promise<any[]> {
    try {
      // 从消息服务获取数据
      const messages = await messageService.getDisputeMessages(1); // 简化处理，实际应该根据纠纷ID

      return messages.filter(message => {
        // 消息类型筛选
        if (filters.messageTypes.length > 0 && !filters.messageTypes.includes(message.messageType as MessageType)) {
          return false;
        }

        // 发送者筛选
        if (filters.senders.length > 0 && !filters.senders.includes(message.senderId)) {
          return false;
        }

        // 时间范围筛选
        if (filters.dateRange) {
          const messageTime = new Date(message.timestamp).getTime();
          const startTime = new Date(filters.dateRange.start).getTime();
          const endTime = new Date(filters.dateRange.end).getTime();

          if (messageTime < startTime || messageTime > endTime) {
            return false;
          }
        }

        // 只搜索自己的消息
        if (filters.ownMessagesOnly && !message.isOwn) {
          return false;
        }

        // 包含已撤回消息
        if (!filters.includeRecalled && message.isRecalled) {
          return false;
        }

        return true;
      });
    } catch (error) {
      console.error('获取搜索消息失败:', error);
      return [];
    }
  }

  /**
   * 私有方法：执行搜索匹配
   */
  private performSearch(messages: any[], request: SearchRequest): SearchResult[] {
    const keyword = request.keyword.toLowerCase();

    if (!keyword.trim()) {
      return [];
    }

    const results: SearchResult[] = [];

    messages.forEach(message => {
      const content = message.content.toLowerCase();

      // 简单的关键词匹配
      if (content.includes(keyword)) {
        // 生成高亮信息
        const highlights = this.highlightSearchText(message.content, request.keyword);

        // 提取匹配的关键词
        const matchedKeywords = this.extractMatchedKeywords(request.keyword, [message]);

        results.push({
          messageId: message.id,
          content: message.content,
          messageType: message.messageType,
          sender: {
            id: message.senderId,
            name: message.senderName,
            role: message.senderRole,
          },
          timestamp: message.timestamp,
          disputeId: message.disputeId || 1,
          matchedKeywords: matchedKeywords,
          highlights: highlights.map(h => ({
            text: h.text,
            isMatch: h.isMatch,
            keyword: h.isMatch ? request.keyword : undefined,
          })),
          score: this.calculateRelevanceScore(message, request.keyword),
          isOwn: message.isOwn || false,
        });
      }
    });

    return results;
  }

  /**
   * 私有方法：计算相关性得分
   */
  private calculateRelevanceScore(message: any, keyword: string): number {
    let score = 0;
    const content = message.content.toLowerCase();
    const keywordLower = keyword.toLowerCase();

    // 完全匹配得分更高
    if (content === keywordLower) {
      score += 1.0;
    }

    // 开头匹配得分较高
    if (content.startsWith(keywordLower)) {
      score += 0.8;
    }

    // 包含匹配得分
    if (content.includes(keywordLower)) {
      score += 0.6;
    }

    // 长度越短得分越高（更精确）
    const lengthScore = Math.max(0, 1 - (content.length - keywordLower.length) / 100);
    score += lengthScore * 0.4;

    return Math.min(score, 1.0);
  }

  /**
   * 私有方法：排序搜索结果
   */
  private sortResults(results: SearchResult[], options: SearchOptions): SearchResult[] {
    return results.sort((a, b) => {
      let comparison = 0;

      switch (options.sortBy) {
        case 'relevance':
          comparison = b.score - a.score;
          break;
        case 'time':
          const timeA = new Date(a.timestamp).getTime();
          const timeB = new Date(b.timestamp).getTime();
          comparison = options.sortOrder === 'desc' ? timeB - timeA : timeA - timeB;
          break;
        case 'sender':
          comparison = a.sender.name.localeCompare(b.sender.name);
          break;
        default:
          comparison = b.score - a.score;
      }

      return options.sortOrder === 'desc' ? -comparison : comparison;
    });
  }

  /**
   * 私有方法：分页处理
   */
  private paginateResults(results: SearchResult[], page: number, pageSize: number) {
    const start = page * pageSize;
    const end = start + pageSize;

    return {
      items: results.slice(start, end),
      total: results.length,
    };
  }

  /**
   * 私有方法：提取匹配关键词
   */
  private extractMatchedKeywords(keyword: string, results: SearchResult[]): string[] {
    const keywords = new Set<string>();

    if (keyword.trim()) {
      keywords.add(keyword.trim());
    }

    // 从结果中提取其他相关关键词
    results.forEach(result => {
      result.matchedKeywords.forEach(kw => keywords.add(kw));
    });

    return Array.from(keywords);
  }

  /**
   * 私有方法：生成关键词建议
   */
  private generateKeywordSuggestions(keyword: string): SearchSuggestion[] {
    const suggestions: SearchSuggestion[] = [];
    const history = this.getSearchHistory();

    // 从历史记录中查找相似的关键词
    history
      .filter(item => item.keyword.toLowerCase().includes(keyword.toLowerCase()))
      .slice(0, 3)
      .forEach(item => {
        suggestions.push({
          text: item.keyword,
          type: 'keyword',
          description: `${item.resultCount} 个结果`,
          icon: '🔍',
        });
      });

    return suggestions;
  }

  /**
   * 私有方法：生成用户建议
   */
  private async generateUserSuggestions(keyword: string): Promise<SearchSuggestion[]> {
    // 模拟用户搜索建议
    const mockUsers = [
      { id: 1, name: '张三', role: 'buyer' },
      { id: 2, name: '李四', role: 'seller' },
      { id: 3, name: '王五', role: 'arbitrator' },
    ];

    return mockUsers
      .filter(user => user.name.toLowerCase().includes(keyword.toLowerCase()))
      .slice(0, 2)
      .map(user => ({
        text: user.name,
        type: 'user' as const,
        description: `${user.role === 'buyer' ? '买家' : user.role === 'seller' ? '卖家' : '仲裁员'}`,
        icon: user.role === 'buyer' ? '👤' : user.role === 'seller' ? '🏪' : '⚖️',
      }));
  }

  /**
   * 私有方法：生成日期建议
   */
  private generateDateSuggestions(keyword: string): SearchSuggestion[] {
    const suggestions: SearchSuggestion[] = [];
    const now = new Date();

    if (keyword.includes('今天') || keyword.includes('今日')) {
      suggestions.push({
        text: '今天',
        type: 'date',
        description: '今天的消息',
        icon: '📅',
      });
    }

    if (keyword.includes('昨天')) {
      suggestions.push({
        text: '昨天',
        type: 'date',
        description: '昨天的消息',
        icon: '📅',
      });
    }

    if (keyword.includes('本周') || keyword.includes('这周')) {
      suggestions.push({
        text: '本周',
        type: 'date',
        description: '本周的消息',
        icon: '📆',
      });
    }

    return suggestions;
  }

  /**
   * 私有方法：更新统计信息
   */
  private updateStatistics(keyword: string, hasResults: boolean): void {
    // 清除缓存，下次访问时重新计算
    this.statisticsCache = null;
  }

  /**
   * 私有方法：生成唯一ID
   */
  private generateId(): string {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }

  /**
   * 私有方法：转义正则表达式特殊字符
   */
  private escapeRegExp(string: string): string {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  /**
   * 私有方法：模拟延迟
   */
  private simulateDelay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}

// 创建搜索服务实例
export const searchService = new SearchService();

// 导出类型和服务
export default searchService;
export type { SearchService };