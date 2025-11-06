/**
 * 标签管理 API 服务
 * @author BaSui 😎
 * @description 标签列表、添加、编辑、删除、合并、热门标签等接口
 */

import { http } from '../utils/http';
import type { BaseResponse } from '@campus/shared/api';

/**
 * 标签类型枚举
 */
export enum TagType {
  GOODS = 'GOODS',      // 商品标签
  POST = 'POST',        // 帖子标签
  COMMON = 'COMMON'     // 通用标签
}

/**
 * 标签状态枚举
 */
export enum TagStatus {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED'
}

/**
 * 标签信息
 */
export interface Tag {
  id: number;
  name: string;
  type: TagType;
  color?: string;
  description?: string;
  hotCount: number;      // 热度（使用次数）
  status: TagStatus;
  createdAt: string;
  updatedAt?: string;
}

/**
 * 标签列表查询参数
 */
export interface TagListParams {
  keyword?: string;
  type?: TagType;
  status?: TagStatus;
  page?: number;
  size?: number;
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * 添加/编辑标签请求
 */
export interface TagRequest {
  name: string;
  type: TagType;
  color?: string;
  description?: string;
  status: TagStatus;
}

/**
 * 标签合并请求
 */
export interface TagMergeRequest {
  sourceIds: number[];   // 源标签ID列表
  targetId: number;      // 目标标签ID
}

/**
 * 热门标签
 */
export interface HotTag {
  id: number;
  name: string;
  type: TagType;
  hotCount: number;
  rank: number;  // 排名
}

/**
 * 标签 API 服务类
 */
export class TagService {
  /**
   * 获取标签列表（分页）
   * @param params 查询参数
   * @returns 标签列表（分页）
   */
  async list(params?: TagListParams): Promise<PageResponse<Tag>> {
    const response = await http.get<PageResponse<Tag>>('/api/tags', {
      params: {
        keyword: params?.keyword,
        type: params?.type,
        status: params?.status,
        page: params?.page ?? 0,
        size: params?.size ?? 20
      }
    });
    return response.data;
  }

  /**
   * 获取标签详情
   * @param id 标签ID
   * @returns 标签详情
   */
  async getDetail(id: number): Promise<Tag> {
    const response = await http.get<Tag>(`/api/tags/${id}`);
    return response.data;
  }

  /**
   * 添加标签
   * @param data 标签信息
   * @returns 创建的标签ID
   */
  async create(data: TagRequest): Promise<number> {
    const response = await http.post<number>('/api/tags', data);
    return response.data;
  }

  /**
   * 更新标签信息
   * @param id 标签ID
   * @param data 标签信息
   * @returns 更新后的标签信息
   */
  async update(id: number, data: Partial<TagRequest>): Promise<Tag> {
    const response = await http.put<Tag>(`/api/tags/${id}`, data);
    return response.data;
  }

  /**
   * 删除标签
   * @param id 标签ID
   */
  async delete(id: number): Promise<void> {
    await http.delete(`/api/tags/${id}`);
  }

  /**
   * 启用/禁用标签
   * @param id 标签ID
   * @param status 状态
   * @returns 更新后的标签信息
   */
  async updateStatus(id: number, status: TagStatus): Promise<Tag> {
    return this.update(id, { status });
  }

  /**
   * 合并标签
   * @param request 合并请求
   */
  async merge(request: TagMergeRequest): Promise<void> {
    await http.post('/api/tags/merge', request);
  }

  /**
   * 获取热门标签（TOP N）
   * @param limit 数量限制，默认20
   * @param type 标签类型筛选
   * @returns 热门标签列表
   */
  async getHotTags(limit: number = 20, type?: TagType): Promise<HotTag[]> {
    const response = await http.get<HotTag[]>('/api/tags/hot', {
      params: {
        limit,
        type
      }
    });
    return response.data;
  }

  /**
   * 批量删除标签
   * @param ids 标签ID列表
   */
  async batchDelete(ids: number[]): Promise<void> {
    await http.post('/api/tags/batch/delete', { ids });
  }

  /**
   * 获取标签使用统计
   * @param id 标签ID
   * @returns 使用统计数据
   */
  async getUsageStatistics(id: number): Promise<{
    tagId: number;
    tagName: string;
    usageCount: number;
    relatedGoodsCount: number;
    relatedPostsCount: number;
  }> {
    const response = await http.get(`/api/tags/${id}/statistics`);
    return response.data;
  }
}

/**
 * 标签服务实例
 */
export const tagService = new TagService();

/**
 * 导出类型
 */
export type {
  Tag as TagType,
  TagListParams as TagListParamsType,
  TagRequest as TagRequestType,
  TagMergeRequest as TagMergeRequestType,
  HotTag as HotTagType
};
