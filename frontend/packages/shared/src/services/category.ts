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
  CategoryStatisticsResponse,
} from '../api/models';

// ==================== 类型重导出 ====================

export type {
  Category,
  CategoryNodeResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  CategoryBatchSortRequest,
  CategoryStatisticsResponse,
} from '../api/models';

export type CategoryRequest = CreateCategoryRequest;
export type CategorySortRequest = CategoryBatchSortRequest;
export type CategoryStatistics = CategoryStatisticsResponse;

export interface CategoryListParams {
  keyword?: string;
  status?: CategoryStatus;
  parentId?: number | null;
  includeDisabled?: boolean;
}
/**
 * 分类树节点（附带层级与路径信息）
 */
export interface CategoryTreeNode extends CategoryNodeResponse {
  /**
   * 当前节点所在层级（根节点为 0）
   */
  level: number;
  /**
   * 从根节点到当前节点的 ID 路径
   */
  path: number[];
  /**
   * 父节点 ID 列表（不包含当前节点）
   */
  parentChain: number[];
  /**
   * 是否叶子节点
   */
  isLeaf: boolean;
  /**
   * 子节点
   */
  children?: CategoryTreeNode[];
}

/**
 * 扁平化配置
 */
export interface CategoryFlattenOptions {
  /**
   * 限制输出的最大层级（默认不限）
   */
  maxDepth?: number;
  /**
   * 需要排除的分类 ID 列表
   */
  excludeIds?: number[];
}

type CategoryTreeInput = CategoryNodeResponse & {
  children?: CategoryTreeInput[];
  [key: string]: any;
};

export enum CategoryStatus {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED'
}

// ==================== 服务类 ====================

export class CategoryService {
  /**
   * 获取分类树（树形结构）
   */
  async tree(): Promise<CategoryTreeNode[]> {
    const api = getApi();
    const response = await api.getCategoryTree();
    const rawTree = (response.data.data as CategoryTreeInput[]) ?? [];
    return this.normalizeTree(rawTree);
  }

  /**
   * 扁平化分类树，方便下拉选择等场景使用
   */
  flatten(
    tree: CategoryNodeResponse[] | CategoryTreeNode[] = [],
    options: CategoryFlattenOptions = {},
  ): CategoryTreeNode[] {
    const normalized = this.ensureTreeNodes(tree);
    if (normalized.length === 0) {
      return [];
    }

    const flat = this.flattenTree(normalized);
    const { maxDepth, excludeIds } = options;
    const excludeSet = excludeIds && excludeIds.length > 0 ? new Set(excludeIds) : null;

    return flat.filter(node => {
      const withinDepth = typeof maxDepth === 'number' ? node.level <= maxDepth : true;
      const notExcluded = excludeSet ? !excludeSet.has(node.id ?? -1) : true;
      return withinDepth && notExcluded;
    });
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

  /**
   * 更新分类状态
   */
  async updateStatus(id: number, status: CategoryStatus): Promise<void> {
    return this.update(id, { status } as UpdateCategoryRequest);
  }

  /**
   * 标准化树节点，补充层级/路径等元数据
   */
  private normalizeTree(
    nodes: CategoryTreeInput[] = [],
    level = 0,
    parentChain: number[] = [],
  ): CategoryTreeNode[] {
    return nodes.map((node) => {
      const nextParentChain =
        node.id !== undefined && node.id !== null
          ? [...parentChain, node.id]
          : [...parentChain];
      const normalizedChildren =
        node.children && node.children.length > 0
          ? this.normalizeTree(node.children as CategoryTreeInput[], level + 1, nextParentChain)
          : [];

      return {
        ...node,
        level,
        parentChain: [...parentChain],
        path: nextParentChain,
        isLeaf: normalizedChildren.length === 0,
        children: normalizedChildren,
      };
    });
  }

  /**
   * 将树结构转换为扁平数组
   */
  private flattenTree(nodes: CategoryTreeNode[] = [], acc: CategoryTreeNode[] = []): CategoryTreeNode[] {
    nodes.forEach((node) => {
      acc.push(node);
      if (node.children && node.children.length > 0) {
        this.flattenTree(node.children, acc);
      }
    });
    return acc;
  }

  /**
   * 确保节点已经包含层级元数据
   */
  private ensureTreeNodes(
    tree: CategoryNodeResponse[] | CategoryTreeNode[],
  ): CategoryTreeNode[] {
    if (!Array.isArray(tree) || tree.length === 0) {
      return [];
    }

    if (this.hasTreeMeta(tree[0])) {
      return tree as CategoryTreeNode[];
    }

    return this.normalizeTree(tree as CategoryTreeInput[]);
  }

  private hasTreeMeta(
    node?: CategoryNodeResponse | CategoryTreeNode,
  ): node is CategoryTreeNode {
    return !!node && typeof (node as CategoryTreeNode).level === 'number';
  }
}

export const categoryService = new CategoryService();
export default categoryService;
