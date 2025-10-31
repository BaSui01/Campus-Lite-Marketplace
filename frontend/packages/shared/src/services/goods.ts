/**
 * 物品 API 服务
 * @author BaSui 😎
 * @description 物品发布、查询、更新、删除、审核等接口
 */

import { http } from '../utils/http';
import type {
  ApiResponse,
  PageInfo,
  Goods,
  Category,
  Tag,
  Favorite,
  PublishGoodsRequest,
  UpdateGoodsRequest,
  GoodsListQuery,
  AuditGoodsRequest,
  FavoriteListQuery,
} from '../types';

/**
 * 物品 API 服务类
 */
class GoodsService {
  // ==================== 物品相关接口 ====================

  /**
   * 发布物品
   * @param data 发布物品请求参数
   * @returns 发布后的物品信息
   */
  async publishGoods(data: PublishGoodsRequest): Promise<ApiResponse<Goods>> {
    return http.post('/goods', data);
  }

  /**
   * 更新物品
   * @param data 更新物品请求参数
   * @returns 更新后的物品信息
   */
  async updateGoods(data: UpdateGoodsRequest): Promise<ApiResponse<Goods>> {
    return http.put(`/goods/${data.id}`, data);
  }

  /**
   * 删除物品
   * @param goodsId 物品ID
   * @returns 删除结果
   */
  async deleteGoods(goodsId: number): Promise<ApiResponse<void>> {
    return http.delete(`/goods/${goodsId}`);
  }

  /**
   * 获取物品详情
   * @param goodsId 物品ID
   * @returns 物品详情
   */
  async getGoodsById(goodsId: number): Promise<ApiResponse<Goods>> {
    return http.get(`/goods/${goodsId}`);
  }

  /**
   * 获取物品列表
   * @param params 查询参数
   * @returns 物品列表
   */
  async getGoodsList(params: GoodsListQuery): Promise<ApiResponse<PageInfo<Goods>>> {
    return http.get('/goods', { params });
  }

  /**
   * 获取我发布的物品
   * @param params 查询参数
   * @returns 物品列表
   */
  async getMyGoods(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageInfo<Goods>>> {
    return http.get('/goods/my', { params });
  }

  /**
   * 获取待审核物品列表（管理员）
   * @param params 查询参数
   * @returns 待审核物品列表
   */
  async getPendingGoods(params?: { page?: number; pageSize?: number }): Promise<ApiResponse<PageInfo<Goods>>> {
    return http.get('/goods/pending', { params });
  }

  /**
   * 审核物品（管理员）
   * @param data 审核请求参数
   * @returns 审核结果
   */
  async auditGoods(data: AuditGoodsRequest): Promise<ApiResponse<void>> {
    return http.post(`/goods/${data.id}/audit`, {
      approved: data.approved,
      reason: data.reason,
    });
  }

  /**
   * 上架物品
   * @param goodsId 物品ID
   * @returns 上架结果
   */
  async onShelfGoods(goodsId: number): Promise<ApiResponse<void>> {
    return http.post(`/goods/${goodsId}/on-shelf`);
  }

  /**
   * 下架物品
   * @param goodsId 物品ID
   * @returns 下架结果
   */
  async offShelfGoods(goodsId: number): Promise<ApiResponse<void>> {
    return http.post(`/goods/${goodsId}/off-shelf`);
  }

  // ==================== 分类相关接口 ====================

  /**
   * 获取所有分类（树形结构）
   * @returns 分类树
   */
  async getCategoryTree(): Promise<ApiResponse<Category[]>> {
    return http.get('/categories/tree');
  }

  /**
   * 获取分类列表（扁平结构）
   * @returns 分类列表
   */
  async getCategoryList(): Promise<ApiResponse<Category[]>> {
    return http.get('/categories');
  }

  /**
   * 获取分类详情
   * @param categoryId 分类ID
   * @returns 分类详情
   */
  async getCategoryById(categoryId: number): Promise<ApiResponse<Category>> {
    return http.get(`/categories/${categoryId}`);
  }

  // ==================== 标签相关接口 ====================

  /**
   * 获取热门标签
   * @param limit 数量限制
   * @returns 标签列表
   */
  async getHotTags(limit = 20): Promise<ApiResponse<Tag[]>> {
    return http.get('/tags/hot', { params: { limit } });
  }

  /**
   * 搜索标签
   * @param keyword 关键词
   * @returns 标签列表
   */
  async searchTags(keyword: string): Promise<ApiResponse<Tag[]>> {
    return http.get('/tags/search', { params: { keyword } });
  }

  // ==================== 收藏相关接口 ====================

  /**
   * 添加收藏
   * @param goodsId 物品ID
   * @returns 收藏结果
   */
  async addFavorite(goodsId: number): Promise<ApiResponse<Favorite>> {
    return http.post(`/favorites/${goodsId}`);
  }

  /**
   * 取消收藏
   * @param goodsId 物品ID
   * @returns 取消结果
   */
  async removeFavorite(goodsId: number): Promise<ApiResponse<void>> {
    return http.delete(`/favorites/${goodsId}`);
  }

  /**
   * 检查是否已收藏
   * @param goodsId 物品ID
   * @returns 是否已收藏
   */
  async checkFavorite(goodsId: number): Promise<ApiResponse<boolean>> {
    return http.get(`/favorites/${goodsId}/check`);
  }

  /**
   * 获取我的收藏列表
   * @param params 查询参数
   * @returns 收藏列表
   */
  async getMyFavorites(params?: FavoriteListQuery): Promise<ApiResponse<PageInfo<Favorite>>> {
    return http.get('/favorites', { params });
  }
}

// 导出单例
export const goodsService = new GoodsService();
export default goodsService;
