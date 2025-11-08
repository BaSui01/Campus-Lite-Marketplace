/**
 * 分类管理 API 服务
 * @author BaSui 😎
 * @description 分类树、添加、编辑、删除、排序等接口
 * @updated 2025-11-08 - 重构为使用 OpenAPI 生成的 DefaultApi ✅
 */

import { getApi } from '../utils/apiClient';
import type {
  Category as ApiCategory,
  CategoryNodeResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  CategoryBatchSortRequest,
  CategoryStatisticsResponse
} from '../api/models';

// ==================== 类型重导出 ====================

export type {
  Category,
  CategoryNodeResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  CategoryBatchSortRequest,
  CategoryStatisticsResponse
} from '../api/models';

export enum CategoryStatus {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED'
}

// ==================== 服务类 ====================

export class CategoryService {
  /**
   * 获取分类树（树形结构）
   */
  async tree(): Promise<CategoryNodeResponse[]> {
    const api = getApi();
    const response = await api.getCategoryTree();
    return response.data.data as CategoryNodeResponse[];
  }

  /**
   * 获取分类列表（扁平结构）
   */
  async list(): Promise<ApiCategory[]> {
    const api = getApi();
    const response = await api.listCategories();
    return response.data.data as ApiCategory[];
  }

  /**
   * 获取分类详情
   */
  async getDetail(id: number): Promise<ApiCategory> {
    const api = getApi();
    const response = await api.getCategoryById({ id });
    return response.data.data as ApiCategory;
  }

  /**
   * 创建分类
   */
  async create(data: CreateCategoryRequest): Promise<number> {
    const api = getApi();
    const response = await api.createCategory({ createCategoryRequest: data });
    return response.data.data as number;
  }

  /**
   * 更新分类
   */
  async update(id: number, data: UpdateCategoryRequest): Promise<void> {
    const api = getApi();
    await api.updateCategory({ id, updateCategoryRequest: data });
  }

  /**
   * 删除分类
   */
  async delete(id: number): Promise<void> {
    const api = getApi();
    await api.deleteCategory({ id });
  }

  /**
   * 批量更新排序
   */
  async batchUpdateSort(data: CategoryBatchSortRequest): Promise<void> {
    const api = getApi();
    await api.batchUpdateSort({ categoryBatchSortRequest: data });
  }

  /**
   * 获取分类统计
   */
  async getStatistics(): Promise<Record<string, number>> {
    const api = getApi();
    const response = await api.getCategoryStatistics();
    return response.data.data as Record<string, number>;
  }
}

export const categoryService = new CategoryService();
export default categoryService;
