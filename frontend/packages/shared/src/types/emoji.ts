/**
 * 表情包类型定义文件
 *
 * @author BaSui 😎
 * @description 表情包相关的TypeScript类型定义
 * @date 2025-11-07
 */

/**
 * 表情包类型枚举
 */
export enum EmojiPackType {
  SYSTEM = 'SYSTEM',       // 系统内置表情包
  CUSTOM = 'CUSTOM',       // 用户自定义表情包
  PREMIUM = 'PREMIUM',     // 付费高级表情包
}

/**
 * 表情包状态枚举
 */
export enum EmojiPackStatus {
  ACTIVE = 'ACTIVE',       // 启用状态
  DISABLED = 'DISABLED',   // 禁用状态
  PENDING = 'PENDING',     // 待审核
}

/**
 * 表情分类枚举
 */
export enum EmojiCategory {
  SMILEYS = 'SMILEYS',     // 笑脸
  GESTURES = 'GESTURES',   // 手势
  ANIMALS = 'ANIMALS',     // 动物
  FOOD = 'FOOD',           // 食物
  ACTIVITIES = 'ACTIVITIES', // 活动
  OBJECTS = 'OBJECTS',     // 物品
  SYMBOLS = 'SYMBOLS',     // 符号
  FLAGS = 'FLAGS',         // 旗帜
  CUSTOM = 'CUSTOM',       // 自定义
}

/**
 * 单个表情数据结构
 */
export interface EmojiItem {
  /** 表情ID */
  id: string;
  /** 表情名称 */
  name: string;
  /** 表情符号或图片URL */
  content: string;
  /** 表情类型：text (符号) 或 image (图片) */
  contentType: 'text' | 'image';
  /** 表情分类 */
  category: EmojiCategory;
  /** 所属表情包ID */
  packId: string;
  /** 排序权重 */
  sortOrder: number;
  /** 是否为收藏表情 */
  isFavorite: boolean;
  /** 使用次数 */
  useCount: number;
  /** 创建时间 */
  createdAt: string;
  /** 更新时间 */
  updatedAt: string;
}

/**
 * 表情包数据结构
 */
export interface EmojiPack {
  /** 表情包ID */
  id: string;
  /** 表情包名称 */
  name: string;
  /** 表情包描述 */
  description?: string;
  /** 表情包类型 */
  type: EmojiPackType;
  /** 表情包状态 */
  status: EmojiPackStatus;
  /** 表情包封面图片 */
  coverImage?: string;
  /** 表情包作者 */
  author?: string;
  /** 包含的表情列表 */
  emojis: EmojiItem[];
  /** 是否为内置表情包 */
  isBuiltIn: boolean;
  /** 下载次数 */
  downloadCount: number;
  /** 收藏次数 */
  favoriteCount: number;
  /** 排序权重 */
  sortOrder: number;
  /** 创建时间 */
  createdAt: string;
  /** 更新时间 */
  updatedAt: string;
}

/**
 * 表情使用记录
 */
export interface EmojiUsage {
  /** 记录ID */
  id: string;
  /** 表情ID */
  emojiId: string;
  /** 用户ID */
  userId: number;
  /** 使用场景：chat/dispute等 */
  context: string;
  /** 使用时间 */
  usedAt: string;
}

/**
 * 表情包查询参数
 */
export interface EmojiPackQuery {
  /** 表情包类型筛选 */
  type?: EmojiPackType;
  /** 表情包状态筛选 */
  status?: EmojiPackStatus;
  /** 关键词搜索 */
  keyword?: string;
  /** 分类筛选 */
  category?: EmojiCategory;
  /** 是否只显示收藏的 */
  favoriteOnly?: boolean;
  /** 排序方式 */
  sortBy?: 'name' | 'createdAt' | 'downloadCount' | 'useCount';
  /** 排序方向 */
  sortOrder?: 'asc' | 'desc';
  /** 页码 */
  page?: number;
  /** 每页大小 */
  size?: number;
}

/**
 * 表情包API响应类型
 */
export interface EmojiPackListResponse {
  /** 表情包列表 */
  packs: EmojiPack[];
  /** 总数量 */
  total: number;
  /** 当前页码 */
  page: number;
  /** 每页大小 */
  size: number;
  /** 总页数 */
  totalPages: number;
}

/**
 * 表情包创建请求
 */
export interface CreateEmojiPackRequest {
  /** 表情包名称 */
  name: string;
  /** 表情包描述 */
  description?: string;
  /** 表情包类型 */
  type: EmojiPackType;
  /** 表情包封面图片 */
  coverImage?: string;
  /** 表情分类 */
  category?: EmojiCategory;
}

/**
 * 表情包更新请求
 */
export interface UpdateEmojiPackRequest {
  /** 表情包名称 */
  name?: string;
  /** 表情包描述 */
  description?: string;
  /** 表情包状态 */
  status?: EmojiPackStatus;
  /** 表情包封面图片 */
  coverImage?: string;
}

/**
 * 表情添加请求
 */
export interface AddEmojiRequest {
  /** 表情名称 */
  name: string;
  /** 表情内容 */
  content: string;
  /** 表情类型 */
  contentType: 'text' | 'image';
  /** 表情分类 */
  category: EmojiCategory;
  /** 排序权重 */
  sortOrder?: number;
}

/**
 * 表情选择器配置
 */
export interface EmojiPickerConfig {
  /** 是否显示收藏标签 */
  showFavoriteTab?: boolean;
  /** 是否显示搜索框 */
  showSearch?: boolean;
  /** 每行显示的表情数量 */
  emojisPerRow?: number;
  /** 最大显示行数 */
  maxRows?: number;
  /** 是否支持自定义表情上传 */
  allowCustomUpload?: boolean;
  /** 默认选中的分类 */
  defaultCategory?: EmojiCategory;
  /** 主题色彩 */
  theme?: 'light' | 'dark';
}

/**
 * 表情消息数据结构
 */
export interface EmojiMessage {
  /** 消息类型标识 */
  type: 'emoji';
  /** 表情包ID */
  packId: string;
  /** 表情ID */
  emojiId: string;
  /** 表情内容（符号或图片URL） */
  content: string;
  /** 表情名称 */
  emojiName: string;
  /** 表情类型 */
  contentType: 'text' | 'image';
  /** 表情包名称 */
  packName?: string;
}

/**
 * 表情使用统计
 */
export interface EmojiStatistics {
  /** 总使用次数 */
  totalUsage: number;
  /** 最常使用的表情 */
  mostUsedEmojis: EmojiItem[];
  /** 最近使用的表情 */
  recentlyUsedEmojis: EmojiItem[];
  /** 收藏的表情列表 */
  favoriteEmojis: EmojiItem[];
  /** 按分类统计使用次数 */
  usageByCategory: {
    category: EmojiCategory;
    count: number;
  }[];
}