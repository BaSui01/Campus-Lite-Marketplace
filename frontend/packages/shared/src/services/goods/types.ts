/**
 * Goods Services 类型定义
 * @author BaSui 😎
 * @description 商品相关服务的TypeScript类型定义
 */

import type { Review, ReviewReplyDTO, ReviewMediaDTO } from '../../api/models';

// ==================== 评价相关类型 ====================

/**
 * 创建评价请求
 */
export interface CreateReviewRequest {
  /**
   * 订单ID
   */
  orderId: number;

  /**
   * 评分（1-5星）
   */
  rating: number;

  /**
   * 评价内容（10-500字）
   */
  content?: string;

  /**
   * 评价图片URL列表（最多9张）
   */
  images?: string[];

  /**
   * 是否匿名
   * @default false
   */
  isAnonymous?: boolean;

  /**
   * 商品质量评分（1-5星）
   */
  qualityScore?: number;

  /**
   * 服务评分（1-5星）
   */
  serviceScore?: number;

  /**
   * 物流评分（1-5星）
   */
  deliveryScore?: number;
}

/**
 * 评价列表查询参数
 */
export interface ReviewListQuery {
  /**
   * 页码（从0开始）
   * @default 0
   */
  page?: number;

  /**
   * 每页条数
   * @default 10
   */
  size?: number;

  /**
   * 星级筛选（1-5）
   */
  rating?: number;

  /**
   * 排序方式
   * - 'time': 按时间（默认，最新评论置顶）
   * - 'like': 按点赞数
   * - 'image_first': 有图优先（页内重排）
   * @default 'time'
   */
  sortBy?: 'time' | 'like' | 'image_first';

  /**
   * 是否只看有图评价
   * @default false
   */
  hasImages?: boolean;

  /**
   * 评分分组
   * - 'positive'：好评（4-5星）
   * - 'neutral'：中评（3星）
   * - 'negative'：差评（1-2星）
   */
  group?: 'positive' | 'neutral' | 'negative';
}

/**
 * 评价详情（扩展）
 */
export interface ReviewDetail extends Review {
  /**
   * 买家信息
   */
  buyer?: {
    id: number;
    nickname: string;
    avatar: string;
  };

  /**
   * 卖家信息
   */
  seller?: {
    id: number;
    nickname: string;
    avatar: string;
  };

  /**
   * 评价图片/视频
   */
  media?: ReviewMediaDTO[];

  /**
   * 卖家回复
   */
  reply?: ReviewReplyDTO;

  /**
   * 当前用户是否已点赞
   */
  isLiked?: boolean;
}

/**
 * 评价列表响应
 */
export interface ReviewListResponse {
  /**
   * 评价列表
   */
  content: ReviewDetail[];

  /**
   * 总条数
   */
  totalElements: number;

  /**
   * 总页数
   */
  totalPages: number;

  /**
   * 当前页码
   */
  number: number;

  /**
   * 每页条数
   */
  size: number;

  /**
   * 是否是第一页
   */
  first: boolean;

  /**
   * 是否是最后一页
   */
  last: boolean;

  /**
   * 是否为空
   */
  empty: boolean;
}

/**
 * 评价统计
 */
export interface ReviewStatistics {
  /**
   * 总评价数
   */
  totalCount: number;

  /**
   * 平均评分
   */
  averageRating: number;

  /**
   * 各星级评价数
   */
  ratingDistribution: {
    5: number;
    4: number;
    3: number;
    2: number;
    1: number;
  };

  /**
   * 有图评价数
   */
  imageReviewCount: number;

  /**
   * 好评率（4-5星）
   */
  positiveRate: number;
}

// ==================== 商品相关类型（可能已存在，确认后删除）====================

/**
 * 商品列表查询参数
 */
export interface GoodsListQuery {
  /**
   * 搜索关键词
   */
  keyword?: string;

  /**
   * 分类ID
   */
  categoryId?: number;

  /**
   * 校区ID
   */
  campusId?: number;

  /**
   * 最低价格
   */
  minPrice?: number;

  /**
   * 最高价格
   */
  maxPrice?: number;

  /**
   * 排序方式
   */
  sortBy?: 'newest' | 'price_asc' | 'price_desc' | 'views';

  /**
   * 页码
   */
  page?: number;

  /**
   * 每页条数
   */
  size?: number;
}

/**
 * 商品详情（扩展）
 */
export interface GoodsDetailExtended {
  /**
   * 商品ID
   */
  id: number;

  /**
   * 商品标题
   */
  title: string;

  /**
   * 商品描述
   */
  description?: string;

  /**
   * 商品价格
   */
  price: number;

  /**
   * 卖家信息
   */
  seller: {
    id: number;
    nickname: string;
    avatar: string;
    rating?: number;
  };

  /**
   * 商品图片列表
   */
  images: string[];

  /**
   * 分类信息
   */
  category: {
    id: number;
    name: string;
  };

  /**
   * 商品状态
   */
  status: string;

  /**
   * 浏览次数
   */
  viewCount?: number;

  /**
   * 收藏次数
   */
  favoriteCount?: number;

  /**
   * 评价统计
   */
  reviewStatistics?: ReviewStatistics;

  /**
   * 创建时间
   */
  createdAt: string;

  /**
   * 更新时间
   */
  updatedAt?: string;
}
