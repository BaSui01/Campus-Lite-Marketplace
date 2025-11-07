/**
 * ⚠️ 警告：此文件仍使用手写 API 路径（http.get/post/put/delete）
 * 🔧 需要重构：将所有 http. 调用替换为 getApi() + DefaultApi 方法
 * 📋 参考：frontend/packages/shared/src/services/order.ts（已完成重构）
 * 👉 重构步骤：
 *    1. 找到对应的 OpenAPI 生成的方法名（在 api/api/default-api.ts）
 *    2. 替换为：const api = getApi(); api.methodName(...)
 *    3. 更新返回值类型
 */
/**
 * Recommend Service - 推荐服务
 * @author BaSui 😎
 * @description 商品推荐算法服务（基于协同过滤、热度排序）
 */

import { getApi } from '../utils/apiClient';
import type { GoodsResponse } from '../api/models';

// ==================== 类型定义 ====================

/**
 * 推荐请求参数
 */
export interface RecommendParams {
  /**
   * 用户ID（可选）
   */
  userId?: number;

  /**
   * 商品ID（基于商品推荐相似商品）
   */
  goodsId?: number;

  /**
   * 分类ID（基于分类推荐）
   */
  categoryId?: number;

  /**
   * 推荐数量
   * @default 10
   */
  size?: number;

  /**
   * 推荐算法
   * - CF: 协同过滤（Collaborative Filtering）
   * - HOT: 热度排序
   * - SIMILAR: 相似商品
   * @default 'CF'
   */
  algorithm?: 'CF' | 'HOT' | 'SIMILAR';
}

/**
 * 推荐结果
 */
export interface RecommendResult {
  /**
   * 推荐商品列表
   */
  goods: GoodsResponse[];

  /**
   * 推荐理由
   */
  reason?: string;

  /**
   * 推荐分数
   */
  score?: number;
}

// ==================== 服务接口 ====================

/**
 * 推荐服务接口
 */
export interface RecommendService {
  /**
   * 获取个性化推荐
   * @param params 推荐参数
   * @returns 推荐商品列表
   */
  getPersonalizedRecommend(params: RecommendParams): Promise<GoodsResponse[]>;

  /**
   * 获取相似商品推荐
   * @param goodsId 商品ID
   * @param size 推荐数量
   * @returns 相似商品列表
   */
  getSimilarGoods(goodsId: number, size?: number): Promise<GoodsResponse[]>;

  /**
   * 获取热门商品推荐
   * @param categoryId 分类ID（可选）
   * @param size 推荐数量
   * @returns 热门商品列表
   */
  getHotGoods(categoryId?: number, size?: number): Promise<GoodsResponse[]>;

  /**
   * 获取猜你喜欢
   * @param userId 用户ID
   * @param size 推荐数量
   * @returns 推荐商品列表
   */
  getGuessYouLike(userId: number, size?: number): Promise<GoodsResponse[]>;
}

// ==================== 服务实现 ====================

/**
 * 推荐服务实现类
 */
class RecommendServiceImpl implements RecommendService {
  /**
   * 获取个性化推荐
   */
  async getPersonalizedRecommend(params: RecommendParams): Promise<GoodsResponse[]> {
    const response = await http.get<{ data: GoodsResponse[] }>('/api/recommend/personalized', {
      params,
    });
    return response.data.data;
  }

  /**
   * 获取相似商品推荐
   */
  async getSimilarGoods(goodsId: number, size: number = 10): Promise<GoodsResponse[]> {
    const response = await http.get<{ data: GoodsResponse[] }>(`/api/recommend/similar/${goodsId}`, {
      params: { size },
    });
    return response.data.data;
  }

  /**
   * 获取热门商品推荐
   */
  async getHotGoods(categoryId?: number, size: number = 10): Promise<GoodsResponse[]> {
    const response = await http.get<{ data: GoodsResponse[] }>('/api/recommend/hot', {
      params: {
        categoryId,
        size,
      },
    });
    return response.data.data;
  }

  /**
   * 获取猜你喜欢
   */
  async getGuessYouLike(userId: number, size: number = 10): Promise<GoodsResponse[]> {
    const response = await http.get<{ data: GoodsResponse[] }>(`/api/recommend/guess/${userId}`, {
      params: { size },
    });
    return response.data.data;
  }
}

/**
 * 推荐服务实例
 */
export const recommendService = new RecommendServiceImpl();
