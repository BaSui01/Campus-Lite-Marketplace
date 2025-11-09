/**
 * 标签管理 API 服务
 * @author BaSui 😎
 * @description 标签列表、添加、编辑、删除、合并、热门标签等接口
 * @updated 2025-11-08 - 重构为使用 OpenAPI 生成的 DefaultApi ✅
 */

import { getApi } from '../utils/apiClient';
import type { TagResponse, CreateTagRequest, UpdateTagRequest, MergeTagRequest, TagStatisticsResponse } from '../api/models';

// ==================== 类型重导出（使用 OpenAPI 生成的类型）====================
export type { TagResponse as Tag, CreateTagRequest, UpdateTagRequest, MergeTagRequest, TagStatisticsResponse } from '../api/models';

export type TagRequest = CreateTagRequest;

// ==================== 标签类型枚举（保持兼容）====================
export enum TagType {
  GOODS = 'GOODS',
  POST = 'POST',
  COMMON = 'COMMON'
}

export enum TagStatus {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED'
}

// ==================== 热门标签类型 ====================
export interface HotTag {
  id: number;
  name: string;
  usageCount: number;
}

// ==================== 标签列表查询参数 ====================
export interface TagListParams {
  keyword?: string;
  type?: TagType;
  status?: TagStatus;
  page?: number;
  size?: number;
}

/**
 * 标签 API 服务类
 * ✅ 完全基于 OpenAPI 生成的 DefaultApi
 */
export class TagService {
  /**
   * 获取标签列表（分页）
   * @param params 查询参数
   * @returns 标签分页数据
   */
  async list(params?: TagListParams): Promise<TagResponse[]> {
    const api = getApi();
    const response = await api.listTags(
      params?.keyword,
      params?.status === TagStatus.ENABLED ? true : params?.status === TagStatus.DISABLED ? false : undefined,
      params?.page,
      params?.size
    );

    // 如果返回的是分页数据，提取 content
    const data = response.data.data as any;
    return (data?.content || data) as TagResponse[];
  }

  /**
   * 获取热门标签
   * @param limit 返回数量
   * @returns 热门标签列表
   */
  async getHotTags(limit: number = 20): Promise<HotTag[]> {
    const api = getApi();
    const response = await api.getHotTags({ limit });
    const hotTags = response.data.data as any[];

    return hotTags.map(tag => ({
      id: tag.tagId,
      name: tag.tagName,
      usageCount: tag.goodsCount || 0
    }));
  }

  /**
   * 获取标签详情
   * @param id 标签ID
   * @returns 标签详情
   */
  async getDetail(id: number): Promise<TagResponse> {
    const api = getApi();
    const response = await api.getTagById({ id });
    return response.data.data as TagResponse;
  }

  /**
   * 创建标签
   * @param data 标签信息
   * @returns 创建的标签ID
   */
  async create(data: CreateTagRequest): Promise<number> {
    const api = getApi();
    const response = await api.createTag({ createTagRequest: data });
    return response.data.data as number;
  }

  /**
   * 更新标签
   * @param id 标签ID
   * @param data 标签信息
   */
  async update(id: number, data: UpdateTagRequest): Promise<void> {
    const api = getApi();
    await api.updateTag({ id, updateTagRequest: data });
  }

  /**
   * 删除标签
   * @param id 标签ID
   */
  async delete(id: number): Promise<void> {
    const api = getApi();
    await api.deleteTag({ id });
  }

  /**
   * 合并标签
   * @param request 合并请求
   */
  async merge(request: MergeTagRequest): Promise<void> {
    const api = getApi();
    await api.mergeTags({ mergeTagRequest: request });
  }

  /**
   * 批量删除标签
   * @param ids 标签ID列表
   */
  async batchDelete(ids: number[]): Promise<number> {
    const api = getApi();
    const response = await api.batchDeleteTags({ requestBody: ids });
    return response.data.data as number;
  }

  /**
   * 获取标签使用统计
   * @param id 标签ID
   * @returns 使用统计数据
   */
  async getStatistics(id: number): Promise<TagStatisticsResponse> {
    const api = getApi();
    const response = await api.getTagStatistics({ id });
    return response.data.data as TagStatisticsResponse;
  }

  /**
   * 更新标签状态
   * @param id 标签ID
   * @param status 标签状态
   */
  async updateStatus(id: number, status: TagStatus): Promise<void> {
    const api = getApi();
    await api.toggleEnabled({ id });
  }
}

/**
 * 标签服务实例
 */
export const tagService = new TagService();
export default tagService;
