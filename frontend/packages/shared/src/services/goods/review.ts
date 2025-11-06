/**
 * Review Service - 评价服务
 * @author BaSui 😎
 * @description 评价系统服务层：发布评价、查询评价、点赞、回复、媒体上传
 */

import { http } from '../../utils/http';
import type { AxiosResponse } from 'axios';
import type {
  Review,
  ReviewReplyDTO,
  ReviewMediaDTO,
} from '../../api/models';
import type {
  CreateReviewRequest,
  ReviewListQuery,
  ReviewListResponse,
  ReviewDetail,
  ReviewStatistics,
} from './types';

// 导出类型供外部使用
export type {
  CreateReviewRequest,
  ReviewListQuery,
  ReviewListResponse,
  ReviewDetail,
  ReviewStatistics,
} from './types';

/**
 * 评价服务接口
 */
export interface ReviewService {
  // ==================== 核心评价接口 ====================
  
  /**
   * 发布评价
   * @param request 评价请求
   * @returns 评价ID
   */
  createReview(request: CreateReviewRequest): Promise<number>;

  /**
   * 查询商品评价列表
   * @param goodsId 商品ID
   * @param params 查询参数
   * @returns 评价列表（分页）
   */
  listReviews(goodsId: number, params: ReviewListQuery): Promise<ReviewListResponse>;

  /**
   * 获取我的评价列表
   * @param params 查询参数
   * @returns 评价列表（分页）
   */
  getMyReviews(params: { page: number; size: number }): Promise<ReviewListResponse>;

  /**
   * 删除评价
   * @param reviewId 评价ID
   */
  deleteReview(reviewId: number): Promise<void>;

  // ==================== 点赞接口 ====================

  /**
   * 点赞评价
   * @param reviewId 评价ID
   */
  likeReview(reviewId: number): Promise<void>;

  /**
   * 取消点赞评价
   * @param reviewId 评价ID
   */
  unlikeReview(reviewId: number): Promise<void>;

  /**
   * 切换点赞状态
   * @param reviewId 评价ID
   */
  toggleLike(reviewId: number): Promise<void>;

  /**
   * 查询点赞状态
   * @param reviewId 评价ID
   * @returns 是否已点赞
   */
  getLikeStatus(reviewId: number): Promise<boolean>;

  /**
   * 获取点赞数
   * @param reviewId 评价ID
   * @returns 点赞数
   */
  getLikeCount(reviewId: number): Promise<number>;

  // ==================== 回复接口 ====================

  /**
   * 回复评价（卖家/管理员）
   * @param reviewId 评价ID
   * @param content 回复内容
   * @returns 回复详情
   */
  replyReview(reviewId: number, content: string): Promise<ReviewReplyDTO>;

  /**
   * 获取评价回复列表
   * @param reviewId 评价ID
   * @returns 回复列表
   */
  getReviewReplies(reviewId: number): Promise<ReviewReplyDTO[]>;

  /**
   * 获取未读回复数
   * @returns 未读回复数
   */
  getUnreadReplyCount(): Promise<number>;

  /**
   * 标记回复已读
   * @param replyId 回复ID
   */
  markReplyAsRead(replyId: number): Promise<void>;

  /**
   * 全部标记已读
   */
  markAllRepliesAsRead(): Promise<void>;

  /**
   * 删除回复
   * @param replyId 回复ID
   */
  deleteReply(replyId: number): Promise<void>;

  // ==================== 媒体接口 ====================

  /**
   * 上传评价媒体（图片/视频）
   * @param reviewId 评价ID
   * @param files 文件列表
   * @returns 媒体URL列表
   */
  uploadReviewMedia(reviewId: number, files: File[]): Promise<string[]>;

  /**
   * 批量上传评价媒体
   * @param reviewId 评价ID
   * @param files 文件列表
   * @returns 媒体详情列表
   */
  batchUploadReviewMedia(reviewId: number, files: File[]): Promise<ReviewMediaDTO[]>;

  /**
   * 获取评价媒体列表
   * @param reviewId 评价ID
   * @returns 媒体列表
   */
  getReviewMedia(reviewId: number): Promise<ReviewMediaDTO[]>;

  /**
   * 按类型获取评价媒体
   * @param reviewId 评价ID
   * @param mediaType 媒体类型（IMAGE/VIDEO）
   * @returns 媒体列表
   */
  getReviewMediaByType(
    reviewId: number,
    mediaType: 'IMAGE' | 'VIDEO'
  ): Promise<ReviewMediaDTO[]>;

  /**
   * 删除媒体
   * @param mediaId 媒体ID
   */
  deleteReviewMedia(mediaId: number): Promise<void>;

  /**
   * 删除评价的所有媒体
   * @param reviewId 评价ID
   */
  deleteAllReviewMedia(reviewId: number): Promise<void>;
}

/**
 * 评价服务实现类
 */
class ReviewServiceImpl implements ReviewService {
  // ==================== 核心评价接口实现 ====================

  async createReview(request: CreateReviewRequest): Promise<number> {
    const response = await http.post<{ data: number }>('/api/reviews', request);
    return response.data.data;
  }

  async listReviews(
    goodsId: number,
    params: ReviewListQuery
  ): Promise<ReviewListResponse> {
    const response = await http.get<ReviewListResponse>(
      `/api/goods/${goodsId}/reviews`,
      { params }
    );
    return response.data;
  }

  async getMyReviews(params: {
    page: number;
    size: number;
  }): Promise<ReviewListResponse> {
    const response = await http.get<ReviewListResponse>('/api/reviews/my', {
      params,
    });
    return response.data;
  }

  async deleteReview(reviewId: number): Promise<void> {
    await http.delete(`/api/reviews/${reviewId}`);
  }

  // ==================== 点赞接口实现 ====================

  async likeReview(reviewId: number): Promise<void> {
    await http.post(`/api/reviews/${reviewId}/like`);
  }

  async unlikeReview(reviewId: number): Promise<void> {
    await http.delete(`/api/reviews/${reviewId}/like`);
  }

  async toggleLike(reviewId: number): Promise<void> {
    await http.post(`/api/reviews/${reviewId}/like/toggle`);
  }

  async getLikeStatus(reviewId: number): Promise<boolean> {
    const response = await http.get<{ data: boolean }>(
      `/api/reviews/${reviewId}/like/status`
    );
    return response.data.data;
  }

  async getLikeCount(reviewId: number): Promise<number> {
    const response = await http.get<{ data: number }>(
      `/api/reviews/${reviewId}/likes/count`
    );
    return response.data.data;
  }

  // ==================== 回复接口实现 ====================

  async replyReview(reviewId: number, content: string): Promise<ReviewReplyDTO> {
    const response = await http.post<{ data: ReviewReplyDTO }>(
      `/api/reviews/${reviewId}/replies`,
      { content, replyType: 'SELLER_REPLY' }
    );
    return response.data.data;
  }

  async getReviewReplies(reviewId: number): Promise<ReviewReplyDTO[]> {
    const response = await http.get<{ data: ReviewReplyDTO[] }>(
      `/api/reviews/${reviewId}/replies`
    );
    return response.data.data;
  }

  async getUnreadReplyCount(): Promise<number> {
    const response = await http.get<{ data: number }>(
      '/api/reviews/replies/unread/count'
    );
    return response.data.data;
  }

  async markReplyAsRead(replyId: number): Promise<void> {
    await http.put(`/api/reviews/replies/${replyId}/read`);
  }

  async markAllRepliesAsRead(): Promise<void> {
    await http.put('/api/reviews/replies/read/all');
  }

  async deleteReply(replyId: number): Promise<void> {
    await http.delete(`/api/reviews/replies/${replyId}`);
  }

  // ==================== 媒体接口实现 ====================

  async uploadReviewMedia(reviewId: number, files: File[]): Promise<string[]> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });

    const response = await http.post<{ data: string[] }>(
      `/api/reviews/${reviewId}/media`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.data;
  }

  async batchUploadReviewMedia(
    reviewId: number,
    files: File[]
  ): Promise<ReviewMediaDTO[]> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });

    const response = await http.post<{ data: ReviewMediaDTO[] }>(
      `/api/reviews/${reviewId}/media/batch`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.data;
  }

  async getReviewMedia(reviewId: number): Promise<ReviewMediaDTO[]> {
    const response = await http.get<{ data: ReviewMediaDTO[] }>(
      `/api/reviews/${reviewId}/media`
    );
    return response.data.data;
  }

  async getReviewMediaByType(
    reviewId: number,
    mediaType: 'IMAGE' | 'VIDEO'
  ): Promise<ReviewMediaDTO[]> {
    const response = await http.get<{ data: ReviewMediaDTO[] }>(
      `/api/reviews/${reviewId}/media/${mediaType}`
    );
    return response.data.data;
  }

  async deleteReviewMedia(mediaId: number): Promise<void> {
    await http.delete(`/api/reviews/media/${mediaId}`);
  }

  async deleteAllReviewMedia(reviewId: number): Promise<void> {
    await http.delete(`/api/reviews/${reviewId}/media`);
  }
}

/**
 * 评价服务实例
 */
export const reviewService = new ReviewServiceImpl();
