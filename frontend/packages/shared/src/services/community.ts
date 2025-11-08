/**
 * ⚠️ 警告：此文件仍使用手写 API 路径（http.get/post/put/delete）
 * 🔧 需要重构：将所有 http. 调用替换为 getApi() + DefaultApi 方法
 * 📋 参考：frontend/packages/shared/src/services/order.ts（已完成重构）
 * 👉 重构步骤：
 *    1. 找到对应的 OpenAPI 生成的方法名（在 api/api/default-api.ts）
 *    2. 替换为：const api = getApi(); api.methodName(...)
 *    3. 更新返回值类型
 */
/**
 * 社区广场服务
 * @author BaSui 😎
 * @description 社区广场话题标签、动态流、互动功能
 */

import { getApi } from '../utils/apiClient';
import type { BaseResponse } from '@campus/shared/api';

// ==================== 类型定义 ====================

/**
 * 用户动态
 */
export interface UserFeed {
  id: number;
  userId: number;
  userName: string;
  userAvatar?: string;
  actionType: 'POST' | 'LIKE' | 'COLLECT' | 'COMMENT';
  targetType: 'POST' | 'GOODS';
  targetId: number;
  content?: string;
  createdAt: string;
}

/**
 * 帖子话题标签
 */
export interface PostTopicTag {
  postId: number;
  topicId: number;
  topicName: string;
}

/**
 * 帖子互动统计
 */
export interface PostInteractionStats {
  postId: number;
  likeCount: number;
  collectCount: number;
  commentCount: number;
  viewCount: number;
  isLiked: boolean;
  isCollected: boolean;
}

// ==================== 服务接口 ====================

export interface CommunityService {
  /** 获取热门话题 */
  getHotTopics(): Promise<any[]>;

  /** 为帖子添加话题标签 */
  addTopicTagsToPost(postId: number, topicIds: number[]): Promise<void>;

  /** 移除帖子的话题标签 */
  removeTopicTagsFromPost(postId: number): Promise<void>;

  /** 点赞帖子 */
  likePost(postId: number): Promise<void>;

  /** 取消点赞 */
  unlikePost(postId: number): Promise<void>;

  /** 收藏帖子 */
  collectPost(postId: number): Promise<void>;

  /** 取消收藏 */
  uncollectPost(postId: number): Promise<void>;

  /** 获取用户动态流 */
  getUserFeed(): Promise<UserFeed[]>;

  /** 获取话题下的帖子 */
  getPostsByTopic(topicId: number): Promise<number[]>;

  /** 检查是否已点赞 */
  checkPostLiked(postId: number): Promise<boolean>;

  /** 检查是否已收藏 */
  checkPostCollected(postId: number): Promise<boolean>;

  /** 获取帖子点赞数 */
  getPostLikeCount(postId: number): Promise<number>;

  /** 获取帖子收藏数 */
  getPostCollectCount(postId: number): Promise<number>;
}

// ==================== 服务实现 ====================

class CommunityServiceImpl implements CommunityService {
  private readonly BASE_PATH = '/api/community';

  async getHotTopics(): Promise<any[]> {
    const response = await http.get<BaseResponse<any[]>>(`${this.BASE_PATH}/topics/hot`);
    return response.data.data;
  }

  async addTopicTagsToPost(postId: number, topicIds: number[]): Promise<void> {
    await http.post(`${this.BASE_PATH}/posts/${postId}/topics`, { topicIds });
  }

  async removeTopicTagsFromPost(postId: number): Promise<void> {
    await http.delete(`${this.BASE_PATH}/posts/${postId}/topics`);
  }

  async likePost(postId: number): Promise<void> {
    await http.post(`${this.BASE_PATH}/posts/${postId}/like`);
  }

  async unlikePost(postId: number): Promise<void> {
    await http.delete(`${this.BASE_PATH}/posts/${postId}/like`);
  }

  async collectPost(postId: number): Promise<void> {
    await http.post(`${this.BASE_PATH}/posts/${postId}/collect`);
  }

  async uncollectPost(postId: number): Promise<void> {
    await http.delete(`${this.BASE_PATH}/posts/${postId}/collect`);
  }

  async getUserFeed(): Promise<UserFeed[]> {
    const response = await http.get<BaseResponse<UserFeed[]>>(`${this.BASE_PATH}/feed`);
    return response.data.data;
  }

  async getPostsByTopic(topicId: number): Promise<number[]> {
    const response = await http.get<BaseResponse<number[]>>(`${this.BASE_PATH}/topics/${topicId}/posts`);
    return response.data.data;
  }

  async checkPostLiked(postId: number): Promise<boolean> {
    const response = await http.get<BaseResponse<boolean>>(`${this.BASE_PATH}/posts/${postId}/liked`);
    return response.data.data;
  }

  async checkPostCollected(postId: number): Promise<boolean> {
    const response = await http.get<BaseResponse<boolean>>(`${this.BASE_PATH}/posts/${postId}/collected`);
    return response.data.data;
  }

  async getPostLikeCount(postId: number): Promise<number> {
    const response = await http.get<BaseResponse<number>>(`${this.BASE_PATH}/posts/${postId}/likes/count`);
    return response.data.data;
  }

  async getPostCollectCount(postId: number): Promise<number> {
    const response = await http.get<BaseResponse<number>>(`${this.BASE_PATH}/posts/${postId}/collects/count`);
    return response.data.data;
  }
}

// ==================== 导出服务实例 ====================

export const communityService = new CommunityServiceImpl();
