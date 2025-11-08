/**
 * 搜索服务测试
 * @author BaSui 😎
 * @description 测试搜索服务的功能
 */

import searchService from './searchService';
import type { SearchRequest, SearchFilters, MessageType } from '@campus/shared/types/search';

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: jest.fn((key: string) => store[key] || null),
    setItem: jest.fn((key: string, value: string) => {
      store[key] = value;
    }),
    removeItem: jest.fn((key: string) => {
      delete store[key];
    }),
    clear: jest.fn(() => {
      store = {};
    }),
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

// Mock messageService
jest.mock('@/services/message', () => ({
  messageService: {
    getDisputeMessages: jest.fn(),
  },
}));

import { messageService } from '@/services/message';

describe('SearchService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorageMock.clear();
    (messageService.getDisputeMessages as jest.Mock).mockResolvedValue([]);
  });

  describe('searchMessages', () => {
    it('should return empty results for empty keyword', async () => {
      const request: SearchRequest = {
        keyword: '',
        filters: {
          keyword: '',
          messageTypes: [],
          senders: [],
          dateRange: null,
          ownMessagesOnly: false,
          includeRecalled: false,
        },
        options: {
          pageSize: 20,
          maxResults: 1000,
          fuzzySearch: true,
          pinyinSearch: true,
          sortBy: 'relevance',
          sortOrder: 'desc',
        },
        pagination: {
          page: 0,
          pageSize: 20,
        },
      };

      const result = await searchService.searchMessages(request);

      expect(result.results).toHaveLength(0);
      expect(result.pagination.total).toBe(0);
    });

    it('should search messages with keyword', async () => {
      const mockMessages = [
        {
          id: '1',
          content: '这是一条测试消息',
          messageType: 'text',
          senderId: 1,
          senderName: '张三',
          senderRole: 'buyer',
          timestamp: new Date().toISOString(),
          isOwn: false,
        },
        {
          id: '2',
          content: '包含测试关键词的另一条消息',
          messageType: 'text',
          senderId: 2,
          senderName: '李四',
          senderRole: 'seller',
          timestamp: new Date().toISOString(),
          isOwn: true,
        },
      ];

      (messageService.getDisputeMessages as jest.Mock).mockResolvedValue(mockMessages);

      const request: SearchRequest = {
        keyword: '测试',
        filters: {
          keyword: '测试',
          messageTypes: [],
          senders: [],
          dateRange: null,
          ownMessagesOnly: false,
          includeRecalled: false,
        },
        options: {
          pageSize: 20,
          maxResults: 1000,
          fuzzySearch: true,
          pinyinSearch: true,
          sortBy: 'relevance',
          sortOrder: 'desc',
        },
        pagination: {
          page: 0,
          pageSize: 20,
        },
      };

      const result = await searchService.searchMessages(request);

      expect(result.results).toHaveLength(2);
      expect(result.pagination.total).toBe(2);
      expect(result.statistics.matchedKeywords).toContain('测试');
    });

    it('should filter by message types', async () => {
      const mockMessages = [
        {
          id: '1',
          content: '文字消息',
          messageType: 'text',
          senderId: 1,
          senderName: '张三',
          senderRole: 'buyer',
          timestamp: new Date().toISOString(),
          isOwn: false,
        },
        {
          id: '2',
          content: '图片消息',
          messageType: 'image',
          senderId: 2,
          senderName: '李四',
          senderRole: 'seller',
          timestamp: new Date().toISOString(),
          isOwn: true,
        },
      ];

      (messageService.getDisputeMessages as jest.Mock).mockResolvedValue(mockMessages);

      const request: SearchRequest = {
        keyword: '消息',
        filters: {
          keyword: '消息',
          messageTypes: [MessageType.TEXT],
          senders: [],
          dateRange: null,
          ownMessagesOnly: false,
          includeRecalled: false,
        },
        options: {
          pageSize: 20,
          maxResults: 1000,
          fuzzySearch: true,
          pinyinSearch: true,
          sortBy: 'relevance',
          sortOrder: 'desc',
        },
        pagination: {
          page: 0,
          pageSize: 20,
        },
      };

      const result = await searchService.searchMessages(request);

      expect(result.results).toHaveLength(1);
      expect(result.results[0].messageType).toBe('text');
    });

    it('should filter by date range', async () => {
      const now = new Date();
      const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);
      const twoDaysAgo = new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000);

      const mockMessages = [
        {
          id: '1',
          content: '昨天的消息',
          messageType: 'text',
          senderId: 1,
          senderName: '张三',
          senderRole: 'buyer',
          timestamp: yesterday.toISOString(),
          isOwn: false,
        },
        {
          id: '2',
          content: '前天的消息',
          messageType: 'text',
          senderId: 2,
          senderName: '李四',
          senderRole: 'seller',
          timestamp: twoDaysAgo.toISOString(),
          isOwn: true,
        },
      ];

      (messageService.getDisputeMessages as jest.Mock).mockResolvedValue(mockMessages);

      const request: SearchRequest = {
        keyword: '消息',
        filters: {
          keyword: '消息',
          messageTypes: [],
          senders: [],
          dateRange: {
            start: yesterday.toISOString(),
            end: now.toISOString(),
          },
          ownMessagesOnly: false,
          includeRecalled: false,
        },
        options: {
          pageSize: 20,
          maxResults: 1000,
          fuzzySearch: true,
          pinyinSearch: true,
          sortBy: 'relevance',
          sortOrder: 'desc',
        },
        pagination: {
          page: 0,
          pageSize: 20,
        },
      };

      const result = await searchService.searchMessages(request);

      expect(result.results).toHaveLength(1);
      expect(result.results[0].content).toBe('昨天的消息');
    });

    it('should filter own messages only', async () => {
      const currentUserId = 2;
      const mockMessages = [
        {
          id: '1',
          content: '别人的消息',
          messageType: 'text',
          senderId: 1,
          senderName: '张三',
          senderRole: 'buyer',
          timestamp: new Date().toISOString(),
          isOwn: false,
        },
        {
          id: '2',
          content: '自己的消息',
          messageType: 'text',
          senderId: currentUserId,
          senderName: '我',
          senderRole: 'seller',
          timestamp: new Date().toISOString(),
          isOwn: true,
        },
      ];

      (messageService.getDisputeMessages as jest.Mock).mockResolvedValue(mockMessages);

      const request: SearchRequest = {
        keyword: '消息',
        filters: {
          keyword: '消息',
          messageTypes: [],
          senders: [],
          dateRange: null,
          ownMessagesOnly: true,
          includeRecalled: false,
        },
        options: {
          pageSize: 20,
          maxResults: 1000,
          fuzzySearch: true,
          pinyinSearch: true,
          sortBy: 'relevance',
          sortOrder: 'desc',
        },
        pagination: {
          page: 0,
          pageSize: 20,
        },
      };

      const result = await searchService.searchMessages(request);

      expect(result.results).toHaveLength(1);
      expect(result.results[0].isOwn).toBe(true);
    });

    it('should sort results by relevance score', async () => {
      const mockMessages = [
        {
          id: '1',
          content: '测试',
          messageType: 'text',
          senderId: 1,
          senderName: '张三',
          senderRole: 'buyer',
          timestamp: new Date().toISOString(),
          isOwn: false,
        },
        {
          id: '2',
          content: '这是一条包含测试关键词的消息',
          messageType: 'text',
          senderId: 2,
          senderName: '李四',
          senderRole: 'seller',
          timestamp: new Date().toISOString(),
          isOwn: true,
        },
      ];

      (messageService.getDisputeMessages as jest.Mock).mockResolvedValue(mockMessages);

      const request: SearchRequest = {
        keyword: '测试',
        filters: {
          keyword: '测试',
          messageTypes: [],
          senders: [],
          dateRange: null,
          ownMessagesOnly: false,
          includeRecalled: false,
        },
        options: {
          pageSize: 20,
          maxResults: 1000,
          fuzzySearch: true,
          pinyinSearch: true,
          sortBy: 'relevance',
          sortOrder: 'desc',
        },
        pagination: {
          page: 0,
          pageSize: 20,
        },
      };

      const result = await searchService.searchMessages(request);

      expect(result.results).toHaveLength(2);
      // 第一个结果应该有更高的相关性得分（完全匹配）
      expect(result.results[0].score).toBeGreaterThan(result.results[1].score);
    });
  });

  describe('getSearchSuggestions', () => {
    it('should return empty suggestions for empty keyword', async () => {
      const suggestions = await searchService.getSearchSuggestions('');
      expect(suggestions).toHaveLength(0);
    });

    it('should return suggestions for keyword', async () => {
      // 添加一些搜索历史
      localStorageMock.setItem('chat_search_history', JSON.stringify([
        {
          id: '1',
          keyword: '测试关键词',
          searchedAt: new Date().toISOString(),
          resultCount: 5,
          filters: {},
        },
      ]));

      const suggestions = await searchService.getSearchSuggestions('测');
      expect(suggestions.length).toBeGreaterThan(0);
    });

    it('should cache suggestions', async () => {
      const suggestions1 = await searchService.getSearchSuggestions('测试');
      const suggestions2 = await searchService.getSearchSuggestions('测试');

      expect(suggestions1).toEqual(suggestions2);
    });
  });

  describe('getSearchHistory', () => {
    it('should return empty history when none exists', () => {
      const history = searchService.getSearchHistory();
      expect(history).toHaveLength(0);
    });

    it('should return search history from localStorage', () => {
      const mockHistory = [
        {
          id: '1',
          keyword: '测试1',
          searchedAt: new Date(Date.now() - 60000).toISOString(),
          resultCount: 5,
          filters: {},
        },
        {
          id: '2',
          keyword: '测试2',
          searchedAt: new Date(Date.now() - 120000).toISOString(),
          resultCount: 3,
          filters: {},
        },
      ];

      localStorageMock.setItem('chat_search_history', JSON.stringify(mockHistory));

      const history = searchService.getSearchHistory();
      expect(history).toHaveLength(2);
      expect(history[0].keyword).toBe('测试1'); // 应该按时间倒序排列
      expect(history[1].keyword).toBe('测试2');
    });
  });

  describe('clearSearchHistory', () => {
    it('should clear search history', () => {
      localStorageMock.setItem('chat_search_history', JSON.stringify([
        {
          id: '1',
          keyword: '测试',
          searchedAt: new Date().toISOString(),
          resultCount: 5,
          filters: {},
        },
      ]));

      searchService.clearSearchHistory();

      expect(localStorageMock.removeItem).toHaveBeenCalledWith('chat_search_history');
      const history = searchService.getSearchHistory();
      expect(history).toHaveLength(0);
    });
  });

  describe('highlightSearchText', () => {
    it('should return original text for empty keyword', () => {
      const result = searchService.highlightSearchText('测试文本', '');
      expect(result).toHaveLength(1);
      expect(result[0].text).toBe('测试文本');
      expect(result[0].isMatch).toBe(false);
    });

    it('should highlight matching text', () => {
      const result = searchService.highlightSearchText('这是测试文本', '测试');
      expect(result).toHaveLength(3);
      expect(result[0]).toEqual({ text: '这是', isMatch: false });
      expect(result[1]).toEqual({ text: '测试', isMatch: true });
      expect(result[2]).toEqual({ text: '文本', isMatch: false });
    });

    it('should handle case insensitive matching', () => {
      const result = searchService.highlightSearchText('这是TEST文本', 'test');
      expect(result).toHaveLength(3);
      expect(result[1]).toEqual({ text: 'TEST', isMatch: true });
    });

    it('should handle multiple matches', () => {
      const result = searchService.highlightSearchText('测试测试', '测试');
      expect(result).toHaveLength(2);
      expect(result[0]).toEqual({ text: '测试', isMatch: true });
      expect(result[1]).toEqual({ text: '测试', isMatch: true });
    });
  });

  describe('getSearchStatistics', () => {
    it('should return default statistics when no history exists', async () => {
      const stats = await searchService.getSearchStatistics();
      expect(stats.totalSearches).toBe(0);
      expect(stats.popularKeywords).toHaveLength(0);
      expect(stats.recentSearches).toHaveLength(0);
      expect(stats.successRate).toBe(0);
    });

    it('should calculate statistics from history', async () => {
      const mockHistory = [
        {
          id: '1',
          keyword: '测试',
          searchedAt: new Date(Date.now() - 60000).toISOString(),
          resultCount: 5,
          filters: {},
        },
        {
          id: '2',
          keyword: '搜索',
          searchedAt: new Date(Date.now() - 120000).toISOString(),
          resultCount: 0,
          filters: {},
        },
        {
          id: '3',
          keyword: '测试',
          searchedAt: new Date(Date.now() - 180000).toISOString(),
          resultCount: 3,
          filters: {},
        },
      ];

      localStorageMock.setItem('chat_search_history', JSON.stringify(mockHistory));

      const stats = await searchService.getSearchStatistics();
      expect(stats.totalSearches).toBe(3);
      expect(stats.successRate).toBe(66.66666666666666); // 2/3 * 100
      expect(stats.popularKeywords[0]).toEqual({ keyword: '测试', count: 2 });
    });
  });
});