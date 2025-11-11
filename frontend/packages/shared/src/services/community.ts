/**
 * 社区广场服务
 * @author BaSui 😎
 * @description 社区广场话题标签、动态流、互动功能（基于 OpenAPI 生成代码）
 */

import { getApi, apiClient, getApiBaseUrl } from '../utils/apiClient';
// 引入 OpenAPI 生成的类型，避免与本文件中的视图模型命名冲突
import type { UserFeed as ApiUserFeed, UserFeedFeedTypeEnum } from '../api/models/user-feed';
// 兼容层：在未生成 OpenAPI DTO 之前，使用本地定义；生成后可切换为 '../api/models/user-feed-dto'
import type { UserFeedDTO } from '../api/compat/user-feed-dto';

// ==================== 类型定义 ====================

/**
 * 用户动态（视图模型）
 * 说明：由 OpenAPI 的 UserFeed 模型映射而来，
 *      补充了头像/昵称与兜底逻辑，前端直接用于展示。
 */
export interface UserFeedView {
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
  getUserFeed(): Promise<UserFeedView[]>;

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

  async getUserFeed(): Promise<UserFeedView[]> {
    // 优先调用 v2（DTO 精简版），失败则回退旧接口，保证兼容
    try {
      const base = getApiBaseUrl();
      const { data } = await apiClient.get(`${base}/community/feed/v2`);
      const list = (data?.data as UserFeedDTO[]) ?? [];
      return list.map((f) => ({
        id: Number(f.id ?? 0),
        userId: Number(f.actorId ?? 0),
        userName: f.displayName ?? '',
        userAvatar: f.avatarUrl || undefined,
        actionType: mapActionType(f.feedType as any),
        targetType: f.targetType === 'GOODS' ? 'GOODS' : 'POST',
        targetId: Number(f.targetId ?? 0),
        content: undefined,
        createdAt: f.createdAt || ''
      }));
    } catch (e) {
      // 回退至旧接口（实体返回）
      const api = getApi();
      const response = await api.getUserFeed();
      const list = (response.data.data as ApiUserFeed[]) ?? [];
      return list.map((f) => ({
        id: Number(f.id || 0),
        // 展示以发起人（actor）为准
        userId: Number(f.actorId || 0),
        userName: (f.actor?.nickname?.trim()?.length ? f.actor!.nickname! : (f.actor?.username || '')) || '',
        userAvatar: f.actor?.avatar || undefined,
        actionType: mapActionType(f.feedType),
        // 旧接口暂无目标类型，默认 POST
        targetType: 'POST',
        targetId: Number(f.targetId || 0),
        content: undefined,
        createdAt: f.createdAt || ''
      }));
    }
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

// ==================== 私有工具方法 ====================

/**
 * 将后端的 FeedType 映射为前端展示枚举
 * REVIEW → COMMENT（语义一致）
 */
function mapActionType(t?: ApiUserFeed['feedType']): 'POST' | 'LIKE' | 'COLLECT' | 'COMMENT' {
  switch (t) {
    case UserFeedFeedTypeEnum.Post:
      return 'POST';
    case UserFeedFeedTypeEnum.Like:
      return 'LIKE';
    case UserFeedFeedTypeEnum.Collect:
      return 'COLLECT';
    case UserFeedFeedTypeEnum.Review:
      return 'COMMENT';
    default:
      return 'POST';
  }
}
