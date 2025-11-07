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
 * 话题管理服务
 * @author BaSui 😎
 * @description 话题CRUD、关注、热门推荐
 */

import { getApi } from '../utils/apiClient';
import type { BaseResponse } from '@campus/shared/api';

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
  private readonly BASE_PATH = '/api/topics';

  async create(request: CreateTopicRequest): Promise<number> {
    const response = await http.post<BaseResponse<number>>(`${this.BASE_PATH}`, request);
    return response.data.data;
  }

  async update(topicId: number, request: UpdateTopicRequest): Promise<void> {
    await http.put(`${this.BASE_PATH}/${topicId}`, request);
  }

  async delete(topicId: number): Promise<void> {
    await http.delete(`${this.BASE_PATH}/${topicId}`);
  }

  async getById(topicId: number): Promise<Topic> {
    const response = await http.get<BaseResponse<Topic>>(`${this.BASE_PATH}/${topicId}`);
    return response.data.data;
  }

  async getAll(): Promise<Topic[]> {
    const response = await http.get<BaseResponse<Topic[]>>(`${this.BASE_PATH}`);
    return response.data.data;
  }

  async getHotTopics(): Promise<Topic[]> {
    const response = await http.get<BaseResponse<Topic[]>>(`${this.BASE_PATH}/hot`);
    return response.data.data;
  }

  async follow(topicId: number): Promise<void> {
    await http.post(`${this.BASE_PATH}/${topicId}/follow`);
  }

  async unfollow(topicId: number): Promise<void> {
    await http.delete(`${this.BASE_PATH}/${topicId}/follow`);
  }

  async getMyFollowedTopics(): Promise<Topic[]> {
    const response = await http.get<BaseResponse<Topic[]>>(`${this.BASE_PATH}/followed`);
    return response.data.data;
  }

  async checkFollowed(topicId: number): Promise<boolean> {
    const response = await http.get<BaseResponse<boolean>>(`${this.BASE_PATH}/${topicId}/followed`);
    return response.data.data;
  }

  async getFollowerCount(topicId: number): Promise<number> {
    const response = await http.get<BaseResponse<number>>(`${this.BASE_PATH}/${topicId}/followers/count`);
    return response.data.data;
  }

  async getStatistics(topicId: number): Promise<TopicStatistics> {
    // 扩展接口，假设后端会添加
    // const response = await http.get<BaseResponse<TopicStatistics>>(
    //   `${this.BASE_PATH}/${topicId}/statistics`
    // );
    // return response.data.data;
    
    // 临时实现：从详情接口获取基本统计
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
