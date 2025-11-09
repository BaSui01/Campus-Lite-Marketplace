/**
 * 话题管理服务
 * @author BaSui 😎
 * @description 话题CRUD、关注、热门推荐（基于 OpenAPI 生成代码）
 */

import { getApi } from '../utils/apiClient';

// ==================== 类型定义 ====================

/**
 * 话题
 */
export interface Topic {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt?: string;
  postCount?: number;
  followerCount?: number;
  viewCount?: number;
  isHot?: boolean;
}

/**
 * 话题统计
 */
export interface TopicStatistics {
  topicId: number;
  topicName: string;
  postCount: number;
  followerCount: number;
  viewCount: number;
  participantCount: number;
  hotScore: number;
}

/**
 * 创建话题请求
 */
export interface CreateTopicRequest {
  name: string;
  description?: string;
}

/**
 * 更新话题请求
 */
export interface UpdateTopicRequest {
  name?: string;
  description?: string;
}

// ==================== 服务接口 ====================

export interface TopicService {
  /** 创建话题 */
  create(request: CreateTopicRequest): Promise<number>;

  /** 更新话题 */
  update(topicId: number, request: UpdateTopicRequest): Promise<void>;

  /** 删除话题 */
  delete(topicId: number): Promise<void>;

  /** 查询话题详情 */
  getById(topicId: number): Promise<Topic>;

  /** 获取所有话题 */
  getAll(): Promise<Topic[]>;

  /** 获取热门话题 */
  getHotTopics(): Promise<Topic[]>;

  /** 关注话题 */
  follow(topicId: number): Promise<void>;

  /** 取消关注话题 */
  unfollow(topicId: number): Promise<void>;

  /** 获取我关注的话题 */
  getMyFollowedTopics(): Promise<Topic[]>;

  /** 检查是否已关注 */
  checkFollowed(topicId: number): Promise<boolean>;

  /** 获取话题关注人数 */
  getFollowerCount(topicId: number): Promise<number>;

  /** 获取话题统计（扩展接口） */
  getStatistics(topicId: number): Promise<TopicStatistics>;
}

// ==================== 服务实现 ====================

class TopicServiceImpl implements TopicService {
  async create(request: CreateTopicRequest): Promise<number> {
    const api = getApi();
    const response = await api.createTopic({ requestBody: request });
    return response.data.data as number;
  }

  async update(topicId: number, request: UpdateTopicRequest): Promise<void> {
    const api = getApi();
    await api.updateTopic({ topicId, requestBody: request });
  }

  async delete(topicId: number): Promise<void> {
    const api = getApi();
    await api.deleteTopic({ topicId });
  }

  async getById(topicId: number): Promise<Topic> {
    const api = getApi();
    const response = await api.getTopicById({ topicId });
    return response.data.data as Topic;
  }

  async getAll(): Promise<Topic[]> {
    const api = getApi();
    const response = await api.getAllTopics();
    return response.data.data as Topic[];
  }

  async getHotTopics(): Promise<Topic[]> {
    const api = getApi();
    const response = await api.getHotTopics1();
    return response.data.data as Topic[];
  }

  async follow(topicId: number): Promise<void> {
    const api = getApi();
    await api.followTopic({ topicId });
  }

  async unfollow(topicId: number): Promise<void> {
    const api = getApi();
    await api.unfollowTopic({ topicId });
  }

  async getMyFollowedTopics(): Promise<Topic[]> {
    const api = getApi();
    const response = await api.getMyFollowedTopics();
    return response.data.data as Topic[];
  }

  async checkFollowed(topicId: number): Promise<boolean> {
    const api = getApi();
    const response = await api.isTopicFollowed({ topicId });
    return response.data.data as boolean;
  }

  async getFollowerCount(topicId: number): Promise<number> {
    const api = getApi();
    const response = await api.getTopicFollowerCount({ topicId });
    return response.data.data as number;
  }

  async getStatistics(topicId: number): Promise<TopicStatistics> {
    const topic = await this.getById(topicId);
    return {
      topicId: topic.id,
      topicName: topic.name,
      postCount: topic.postCount || 0,
      followerCount: topic.followerCount || 0,
      viewCount: topic.viewCount || 0,
      participantCount: 0,
      hotScore: 0,
    };
  }
}

// ==================== 导出服务实例 ====================

export const topicService = new TopicServiceImpl();
