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

/**
 * 标签 API 服务类
 * ✅ 完全基于 OpenAPI 生成的 DefaultApi
 */
export class TagService {
  /**
   * 获取标签列表
   * @returns 标签列表
   */
  async list(): Promise<TagResponse[]> {
    const api = getApi();
    const response = await api.listTags();
    return response.data.data as TagResponse[];
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
}

/**
 * 标签服务实例
 */
export const tagService = new TagService();
export default tagService;
