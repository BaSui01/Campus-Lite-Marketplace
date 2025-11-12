/**
 * ✅ 已重构：使用 OpenAPI 生成的 DefaultApi
 *
 * 管理员分类服务
 * @author BaSui 😎
 * @description 分类的创建、更新、删除、排序等管理员专属功能
 * @updated 2025-11-08 - 重构为使用 OpenAPI 生成代码 ✅
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  Category,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  CategoryBatchSortRequest,
} from '@campus/shared/api/models';

/**
 * 管理员分类服务类
 */
export class AdminCategoryService {
  /**
   * 添加分类（管理员）
   * @param data 分类信息
   * @returns 创建的分类ID
   */
  async create(data: CreateCategoryRequest): Promise<number> {
    const api = getApi();
    const response = await api.createCategory({ createCategoryRequest: data });
    return response.data.data as number;
  }

  /**
   * 更新分类信息（管理员）
   * @param id 分类ID
   * @param data 分类信息
   */
  async update(id: number, data: UpdateCategoryRequest): Promise<void> {
    const api = getApi();
    await api.updateCategory({ id, updateCategoryRequest: data });
  }

  /**
   * 删除分类（管理员）
   * @param id 分类ID
   */
  async delete(id: number): Promise<void> {
    const api = getApi();
    await api.deleteCategory({ id });
  }

  /**
   * 批量排序（管理员）
   * @param request 排序请求（包含items数组）
   */
  async batchSort(request: CategoryBatchSortRequest): Promise<void> {
    const api = getApi();
    await api.batchUpdateSort({ categoryBatchSortRequest: request });
  }

  /**
   * 移动分类（修改父分类，管理员）
   * @param id 分类ID
   * @param newParentId 新父分类ID
   */
  async move(id: number, newParentId: number | null): Promise<void> {
    const api = getApi();
    const data: UpdateCategoryRequest = {
      name: '', // 需要从当前分类获取
      parentId: newParentId ?? undefined,
    };
    await api.updateCategory({ id, updateCategoryRequest: data });
  }
}

// 导出单例
export const adminCategoryService = new AdminCategoryService();
export default adminCategoryService;
