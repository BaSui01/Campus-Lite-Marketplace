/**
 * 管理员分类服务
 * @author BaSui 😎
 * @description 分类的创建、更新、删除、排序等管理员专属功能
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  Category,
  CategoryRequest,
  CategorySortRequest,
  CategoryStatus,
} from '@campus/shared/services/category';

/**
 * 管理员分类服务类
 */
export class AdminCategoryService {
  /**
   * 添加分类（管理员）
   * @param data 分类信息
   * @returns 创建的分类ID
   */
  async create(data: CategoryRequest): Promise<number> {
    const api = getApi();
    const response = await api.axiosInstance.post<number>('/api/categories', data);
    return response.data;
  }

  /**
   * 更新分类信息（管理员）
   * @param id 分类ID
   * @param data 分类信息
   * @returns 更新后的分类信息
   */
  async update(id: number, data: Partial<CategoryRequest>): Promise<Category> {
    const api = getApi();
    const response = await api.axiosInstance.put<Category>(`/api/categories/${id}`, data);
    return response.data;
  }

  /**
   * 删除分类（管理员）
   * @param id 分类ID
   */
  async delete(id: number): Promise<void> {
    const api = getApi();
    await api.axiosInstance.delete(`/api/categories/${id}`);
  }

  /**
   * 批量排序（管理员）
   * @param items 排序列表
   */
  async batchSort(items: CategorySortRequest[]): Promise<void> {
    const api = getApi();
    await api.axiosInstance.put('/api/categories/sort', items);
  }

  /**
   * 移动分类（修改父分类，管理员）
   * @param id 分类ID
   * @param newParentId 新父分类ID
   * @returns 更新后的分类信息
   */
  async move(id: number, newParentId: number | null): Promise<Category> {
    return this.update(id, { parentId: newParentId });
  }

  /**
   * 启用/禁用分类（管理员）
   * @param id 分类ID
   * @param status 状态
   * @returns 更新后的分类信息
   */
  async updateStatus(id: number, status: CategoryStatus): Promise<Category> {
    return this.update(id, { status });
  }
}

// 导出单例
export const adminCategoryService = new AdminCategoryService();
export default adminCategoryService;
