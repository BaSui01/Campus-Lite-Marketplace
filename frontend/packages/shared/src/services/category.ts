/**
 * 分类管理 API 服务
 * @author BaSui 😎
 * @description 分类树、添加、编辑、删除、排序等接口
 */

import { http } from '../utils/apiClient';
import type { BaseResponse } from '@campus/shared/api';

/**
 * 分类状态枚举
 */
export enum CategoryStatus {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED'
}

/**
 * 分类信息
 */
export interface Category {
  id: number;
  name: string;
  parentId?: number | null;
  level: number;
  icon?: string;
  description?: string;
  sortOrder: number;
  status: CategoryStatus;
  children?: Category[];
  createdAt: string;
  updatedAt?: string;
}

/**
 * 分类树节点（包含子节点）
 */
export interface CategoryTreeNode extends Category {
  children: CategoryTreeNode[];
  key: string;  // 用于树形组件
  title: string;  // 用于树形组件
}

/**
 * 分类列表查询参数
 */
export interface CategoryListParams {
  keyword?: string;
  status?: CategoryStatus;
  parentId?: number | null;  // null表示查询一级分类
}

/**
 * 添加/编辑分类请求
 */
export interface CategoryRequest {
  name: string;
  parentId?: number | null;
  icon?: string;
  description?: string;
  sortOrder?: number;
  status: CategoryStatus;
}

/**
 * 批量排序请求
 */
export interface CategorySortRequest {
  id: number;
  sortOrder: number;
}

/**
 * 分类统计数据
 */
export interface CategoryStatistics {
  categoryId: number;
  categoryName: string;
  goodsCount: number;  // 该分类下的商品数
  childrenCount: number;  // 子分类数
}

/**
 * 分类 API 服务类
 */
export class CategoryService {
  /**
   * 获取分类树（完整层级结构）
   * @returns 分类树
   */
  async tree(): Promise<Category[]> {
    const response = await http.get<Category[]>('/api/categories/tree');
    return response.data;
  }

  /**
   * 获取分类列表（扁平结构）
   * @param params 查询参数
   * @returns 分类列表
   */
  async list(params?: CategoryListParams): Promise<Category[]> {
    const response = await http.get<Category[]>('/api/categories', {
      params: {
        keyword: params?.keyword,
        status: params?.status,
        parentId: params?.parentId
      }
    });
    return response.data;
  }

  /**
   * 获取分类详情
   * @param id 分类ID
   * @returns 分类详情
   */
  async getDetail(id: number): Promise<Category> {
    const response = await http.get<Category>(`/api/categories/${id}`);
    return response.data;
  }

  /**
   * 添加分类
   * @param data 分类信息
   * @returns 创建的分类ID
   */
  async create(data: CategoryRequest): Promise<number> {
    const response = await http.post<number>('/api/categories', data);
    return response.data;
  }

  /**
   * 更新分类信息
   * @param id 分类ID
   * @param data 分类信息
   * @returns 更新后的分类信息
   */
  async update(id: number, data: Partial<CategoryRequest>): Promise<Category> {
    const response = await http.put<Category>(`/api/categories/${id}`, data);
    return response.data;
  }

  /**
   * 删除分类
   * @param id 分类ID
   */
  async delete(id: number): Promise<void> {
    await http.delete(`/api/categories/${id}`);
  }

  /**
   * 批量排序
   * @param items 排序列表
   */
  async batchSort(items: CategorySortRequest[]): Promise<void> {
    await http.put('/api/categories/sort', items);
  }

  /**
   * 移动分类（修改父分类）
   * @param id 分类ID
   * @param newParentId 新父分类ID
   * @returns 更新后的分类信息
   */
  async move(id: number, newParentId: number | null): Promise<Category> {
    return this.update(id, { parentId: newParentId });
  }

  /**
   * 启用/禁用分类
   * @param id 分类ID
   * @param status 状态
   * @returns 更新后的分类信息
   */
  async updateStatus(id: number, status: CategoryStatus): Promise<Category> {
    return this.update(id, { status });
  }

  /**
   * 获取分类统计数据
   * @param id 分类ID
   * @returns 分类统计数据
   */
  async statistics(id: number): Promise<CategoryStatistics> {
    const response = await http.get<CategoryStatistics>(
      `/api/categories/${id}/statistics`
    );
    return response.data;
  }

  /**
   * 获取子分类列表
   * @param parentId 父分类ID
   * @returns 子分类列表
   */
  async getChildren(parentId: number): Promise<Category[]> {
    return this.list({ parentId });
  }

  /**
   * 转换为树形结构（前端辅助方法）
   * @param categories 扁平分类列表
   * @returns 树形结构
   */
  toTree(categories: Category[]): CategoryTreeNode[] {
    const map = new Map<number, CategoryTreeNode>();
    const roots: CategoryTreeNode[] = [];

    // 转换为TreeNode并建立映射
    categories.forEach(category => {
      const node: CategoryTreeNode = {
        ...category,
        children: [],
        key: category.id.toString(),
        title: category.name
      };
      map.set(category.id, node);
    });

    // 构建树形结构
    categories.forEach(category => {
      const node = map.get(category.id)!;
      if (category.parentId && map.has(category.parentId)) {
        const parent = map.get(category.parentId)!;
        parent.children.push(node);
      } else {
        roots.push(node);
      }
    });

    return roots;
  }

  /**
   * 扁平化树形结构（前端辅助方法）
   * @param tree 树形结构
   * @returns 扁平列表
   */
  flatten(tree: Category[]): Category[] {
    const result: Category[] = [];
    
    const traverse = (nodes: Category[]) => {
      nodes.forEach(node => {
        result.push(node);
        if (node.children && node.children.length > 0) {
          traverse(node.children);
        }
      });
    };

    traverse(tree);
    return result;
  }
}

/**
 * 分类服务实例
 */
export const categoryService = new CategoryService();

/**
 * 导出类型
 */
export type {
  Category as CategoryType,
  CategoryTreeNode as CategoryTreeNodeType,
  CategoryListParams as CategoryListParamsType,
  CategoryRequest as CategoryRequestType,
  CategorySortRequest as CategorySortRequestType,
  CategoryStatistics as CategoryStatisticsType
};
