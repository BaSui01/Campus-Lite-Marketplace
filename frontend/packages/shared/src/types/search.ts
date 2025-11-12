/**
 * 搜索功能类型定义文件
 *
 * @author BaSui 😎
 * @description 聊天消息搜索相关的TypeScript类型定义
 * @date 2025-11-07
 */

/**
 * 搜索结果项
 */
export interface SearchResult {
  /** 消息ID */
  messageId: string;
  /** 匹配的消息内容 */
  content: string;
  /** 消息类型 */
  messageType: 'text' | 'image' | 'file' | 'emoji';
  /** 发送者信息 */
  sender: {
    id: number;
    name: string;
    role: 'buyer' | 'seller' | 'arbitrator';
  };
  /** 消息时间 */
  timestamp: string;
  /** 纠纷ID */
  disputeId: number;
  /** 匹配的关键词 */
  matchedKeywords: string[];
  /** 匹配高亮信息 */
  highlights: TextHighlight[];
  /** 相关性得分 */
  score: number;
  /** 是否为当前用户的消息 */
  isOwn: boolean;
}

/**
 * 文本高亮信息
 */
export interface TextHighlight {
  /** 高亮文本 */
  text: string;
  /** 是否为匹配文本 */
  isMatch: boolean;
  /** 匹配的关键词 */
  keyword?: string;
}

/**
 * 搜索筛选条件
 */
export interface SearchFilters {
  /** 关键词 */
  keyword: string;
  /** 消息类型筛选 */
  messageTypes: MessageType[];
  /** 发送者筛选 */
  senders: number[];
  /** 时间范围 */
  dateRange: {
    start: string;
    end: string;
  } | null;
  /** 是否只搜索自己的消息 */
  ownMessagesOnly: boolean;
  /** 是否包含已撤回的消息 */
  includeRecalled: boolean;
}

/**
 * 消息类型枚举
 */
export enum MessageType {
  TEXT = 'text',
  IMAGE = 'image',
  FILE = 'file',
  EMOJI = 'emoji',
}

/**
 * 搜索历史记录
 */
export interface SearchHistory {
  /** 搜索ID */
  id: string;
  /** 搜索关键词 */
  keyword: string;
  /** 搜索时间 */
  searchedAt: string;
  /** 搜索结果数量 */
  resultCount: number;
  /** 搜索筛选条件 */
  filters: Partial<SearchFilters>;
}

/**
 * 搜索统计信息
 */
export interface SearchStatistics {
  /** 总搜索次数 */
  totalSearches: number;
  /** 热门搜索词 */
  popularKeywords: Array<{
    keyword: string;
    count: number;
  }>;
  /** 最近搜索 */
  recentSearches: SearchHistory[];
  /** 搜索成功率 */
  successRate: number;
}

/**
 * 搜索选项配置
 */
export interface SearchOptions {
  /** 每页结果数量 */
  pageSize: number;
  /** 最大结果数量 */
  maxResults: number;
  /** 是否启用模糊搜索 */
  fuzzySearch: boolean;
  /** 是否启用拼音搜索 */
  pinyinSearch: boolean;
  /** 搜索结果排序方式 */
  sortBy: 'relevance' | 'time' | 'sender';
  /** 排序方向 */
  sortOrder: 'asc' | 'desc';
}

/**
 * 搜索请求参数
 */
export interface SearchRequest {
  /** 搜索关键词 */
  keyword: string;
  /** 搜索筛选条件 */
  filters: SearchFilters;
  /** 搜索选项 */
  options: SearchOptions;
  /** 分页信息 */
  pagination: {
    page: number;
    pageSize: number;
  };
}

/**
 * 搜索响应结果
 */
export interface SearchResponse {
  /** 搜索结果列表 */
  results: SearchResult[];
  /** 分页信息 */
  pagination: {
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
    hasNext: boolean;
    hasPrev: boolean;
  };
  /** 搜索统计 */
  statistics: {
    totalResults: number;
    searchTime: number;
    matchedKeywords: string[];
  };
}

/**
 * 搜索建议
 */
export interface SearchSuggestion {
  /** 建议文本 */
  text: string;
  /** 建议类型 */
  type: 'keyword' | 'user' | 'date';
  /** 建议描述 */
  description?: string;
  /** 建议图标 */
  icon?: string;
}

/**
 * 快捷搜索配置
 */
export interface QuickSearch {
  /** 搜索名称 */
  name: string;
  /** 搜索图标 */
  icon: string;
  /** 搜索关键词 */
  keyword: string;
  /** 搜索筛选条件 */
  filters: Partial<SearchFilters>;
  /** 是否为预设搜索 */
  isPreset: boolean;
}

/**
 * 搜索状态
 */
export interface SearchState {
  /** 是否正在搜索 */
  searching: boolean;
  /** 当前搜索关键词 */
  currentKeyword: string;
  /** 当前筛选条件 */
  currentFilters: SearchFilters;
  /** 搜索结果 */
  results: SearchResult[];
  /** 错误信息 */
  error: string | null;
  /** 搜索建议 */
  suggestions: SearchSuggestion[];
  /** 是否显示高级筛选 */
  showAdvancedFilters: boolean;
}

/**
 * 搜索配置常量
 */
export const SEARCH_CONFIG = {
  /** 默认每页大小 */
  DEFAULT_PAGE_SIZE: 20,
  /** 最大每页大小 */
  MAX_PAGE_SIZE: 100,
  /** 搜索结果最大数量 */
  MAX_RESULTS: 1000,
  /** 搜索历史最大保存数量 */
  MAX_HISTORY_ITEMS: 50,
  /** 搜索建议最大数量 */
  MAX_SUGGESTIONS: 10,
  /** 搜索防抖延迟（毫秒） */
  SEARCH_DEBOUNCE_DELAY: 300,
  /** 最小搜索关键词长度 */
  MIN_KEYWORD_LENGTH: 1,
  /** 最大搜索关键词长度 */
  MAX_KEYWORD_LENGTH: 100,
} as const;

/**
 * 预设快捷搜索
 */
export const PRESET_QUICK_SEARCHES: QuickSearch[] = [
  {
    name: '我的消息',
    icon: '📤',
    keyword: '',
    filters: {
      ownMessagesOnly: true,
      includeRecalled: false,
    },
    isPreset: true,
  },
  {
    name: '图片消息',
    icon: '🖼️',
    keyword: '',
    filters: {
      messageTypes: [MessageType.IMAGE],
      includeRecalled: false,
    },
    isPreset: true,
  },
  {
    name: '文件消息',
    icon: '📎',
    keyword: '',
    filters: {
      messageTypes: [MessageType.FILE],
      includeRecalled: false,
    },
    isPreset: true,
  },
  {
    name: '表情消息',
    icon: '😊',
    keyword: '',
    filters: {
      messageTypes: [MessageType.EMOJI],
      includeRecalled: false,
    },
    isPreset: true,
  },
  {
    name: '今天',
    icon: '📅',
    keyword: '',
    filters: {
      dateRange: {
        start: new Date(new Date().setHours(0, 0, 0, 0)).toISOString(),
        end: new Date(new Date().setHours(23, 59, 59, 999)).toISOString(),
      },
      includeRecalled: false,
    },
    isPreset: true,
  },
  {
    name: '本周',
    icon: '📆',
    keyword: '',
    filters: {
      dateRange: {
        start: new Date(new Date().setDate(new Date().getDate() - new Date().getDay())).toISOString(),
        end: new Date().toISOString(),
      },
      includeRecalled: false,
    },
    isPreset: true,
  },
];