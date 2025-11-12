/**
 * 校园活动 API 服务
 * @author BaSui 😎
 * @date 2025-11-11
 * @description 活动相关接口（基于 OpenAPI 生成代码）
 */

import { apiClient } from '../utils/apiClient';

// ==================== 类型定义 ====================

/**
 * 活动信息
 */
export interface Event {
  id: number;
  title: string;
  description: string;
  coverImage?: string;
  startTime: string;
  endTime: string;
  location?: string;
  organizerId: number;
  campusId?: number;
  maxParticipants: number;
  currentParticipants: number;
  status: 'UPCOMING' | 'ONGOING' | 'ENDED' | 'CANCELLED';
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * 活动列表查询参数
 */
export interface EventListParams {
  page?: number;
  size?: number;
  status?: string;
  campusId?: number;
}

/**
 * 活动列表响应
 */
export interface EventListResponse {
  content: Event[];
  totalElements: number;
  totalPages: number;
}

// ==================== 服务实现 ====================

/**
 * 活动 API 服务类
 * ⚠️ 注意：使用 apiClient（axios 实例）而不是 getApi()（DefaultApi 实例）
 */
export class EventService {
  /**
   * 获取活动列表（分页）
   * @param params 查询参数
   * @returns 活动列表（分页）
   */
  async list(params?: EventListParams): Promise<EventListResponse> {
    const response = await apiClient.get('/events', { params });
    return response.data.data as EventListResponse;
  }

  /**
   * 获取活动详情
   * @param id 活动 ID
   * @returns 活动详情
   */
  async getDetail(id: number): Promise<Event> {
    const response = await apiClient.get(`/events/${id}`);
    return response.data.data as Event;
  }

  /**
   * 报名活动
   * @param id 活动 ID
   */
  async register(id: number): Promise<void> {
    await apiClient.post(`/events/${id}/register`);
  }

  /**
   * 取消报名
   * @param id 活动 ID
   */
  async cancelRegistration(id: number): Promise<void> {
    await apiClient.delete(`/events/${id}/register`);
  }

  /**
   * 检查是否已报名
   * @param id 活动 ID
   * @returns 是否已报名
   */
  async isRegistered(id: number): Promise<boolean> {
    const response = await apiClient.get(`/events/${id}/is-registered`);
    return response.data.data as boolean;
  }

  /**
   * 获取我的报名活动
   * @returns 我的报名活动列表
   */
  async getMyRegistrations(): Promise<Event[]> {
    const response = await apiClient.get('/events/my-registrations');
    return response.data.data as Event[];
  }
}

/**
 * 活动服务实例
 */
export const eventService = new EventService();
