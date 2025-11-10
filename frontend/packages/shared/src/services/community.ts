/**
 * 社区广场服务
 * @author BaSui 😎
 * @description 社区广场话题标签、动态流、互动功能（基于 OpenAPI 生成代码）
 */

import { getApi } from '../utils/apiClient';

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
  async getHotTopics(): Promise<any[]> {
    const api = getApi();
    const response = await api.getHotTopics();
    return response.data.data as any[];
  }

  async addTopicTagsToPost(postId: number, topicIds: number[]): Promise<void> {
    const api = getApi();
    await api.addTopicsToPost({ postId, requestBody: { topicIds } });
  }

  async removeTopicTagsFromPost(postId: number): Promise<void> {
    const api = getApi();
    await api.removeTopicsFromPost({ postId });
  }

  async likePost(postId: number): Promise<void> {
    const api = getApi();
    await api.likePost({ postId });
  }

  async unlikePost(postId: number): Promise<void> {
    const api = getApi();
    await api.unlikePost({ postId });
  }

  async collectPost(postId: number): Promise<void> {
    const api = getApi();
    await api.collectPost({ postId });
  }

  async uncollectPost(postId: number): Promise<void> {
    const api = getApi();
    await api.uncollectPost({ postId });
  }

  async getUserFeed(): Promise<UserFeed[]> {
    const api = getApi();
    const response = await api.getUserFeed();
    return response.data.data as UserFeed[];
  }

  async getPostsByTopic(topicId: number): Promise<number[]> {
    const api = getApi();
    const response = await api.getPostsByTopic({ topicId });
    return response.data.data as number[];
  }

  /**
   * 检查帖子是否已点赞
   * TODO: 等待后端实现 isPostLiked API
   */
  async checkPostLiked(_postId: number): Promise<boolean> {
    // const api = getApi();
    // const response = await api.isPostLiked({ postId });
    // return response.data.data as boolean;
    throw new Error('检查帖子点赞状态功能暂未实现');
  }

  /**
   * 检查帖子是否已收藏
   * TODO: 等待后端实现 isPostCollected API
   */
  async checkPostCollected(_postId: number): Promise<boolean> {
    // const api = getApi();
    // const response = await api.isPostCollected({ postId });
    // return response.data.data as boolean;
    throw new Error('检查帖子收藏状态功能暂未实现');
  }

  async getPostLikeCount(postId: number): Promise<number> {
    const api = getApi();
    const response = await api.getPostLikeCount({ postId });
    return response.data.data as number;
  }

  async getPostCollectCount(postId: number): Promise<number> {
    const api = getApi();
    const response = await api.getPostCollectCount({ postId });
    return response.data.data as number;
  }
}

// ==================== 导出服务实例 ====================

export const communityService = new CommunityServiceImpl();
