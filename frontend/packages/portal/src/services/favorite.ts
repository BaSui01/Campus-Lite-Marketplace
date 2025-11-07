/**
 * 收藏 API 服务
 * @author BaSui 😎
 * @description 商品收藏、取消收藏、查询收藏列表等接口
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { GoodsResponse, PageGoodsResponse } from '@campus/shared/api/models';

/**
 * 收藏列表查询参数
 */
export interface FavoriteListParams {
  page?: number;           // 页码（从 0 开始）
  size?: number;           // 每页大小
  sortBy?: 'createdAt' | 'price' | 'viewCount';  // 排序字段
  sortDirection?: 'asc' | 'desc';  // 排序方向
  status?: 'ON_SALE' | 'SOLD_OUT' | 'OFF_SHELF';  // 商品状态筛选
}

/**
 * 收藏统计数据
 */
export interface FavoriteStatistics {
  total: number;           // 总收藏数
  onSale: number;          // 在售商品数
  soldOut: number;         // 已售出数
  offShelf: number;        // 已下架数
}

/**
 * 收藏 API 服务类
 */
export class FavoriteService {
  /**
   * 添加收藏
   * @param goodsId 商品 ID
   */
  async addFavorite(goodsId: number): Promise<void> {
    const api = getApi();
    await api.addFavorite(goodsId);
  }

  /**
   * 取消收藏
   * @param goodsId 商品 ID
   */
  async removeFavorite(goodsId: number): Promise<void> {
    const api = getApi();
    await api.removeFavorite(goodsId);
  }

  /**
   * 查询收藏列表
   * @param params 查询参数
   * @returns 收藏的商品列表（分页）
   */
  async listFavorites(params?: FavoriteListParams): Promise<PageGoodsResponse> {
    const api = getApi();
    const response = await api.listFavorites(
      params?.page ?? 0,
      params?.size ?? 20
    );
    
    // 如果需要客户端排序（后端不支持时）
    let content = response.data.data?.content || [];
    
    if (params?.sortBy && params?.sortDirection) {
      content = this.sortFavorites(content, params.sortBy, params.sortDirection);
    }
    
    if (params?.status) {
      content = content.filter((item) => item.status === params.status);
    }
    
    return {
      ...response.data.data,
      content,
    } as PageGoodsResponse;
  }

  /**
   * 检查是否已收藏
   * @param goodsId 商品 ID
   * @returns 是否已收藏
   */
  async isFavorited(goodsId: number): Promise<boolean> {
    const api = getApi();
    const response = await api.isFavorited(goodsId);
    return response.data.data as boolean;
  }

  /**
   * 获取收藏统计（前端计算）
   * @returns 收藏统计数据
   */
  async getFavoriteStatistics(): Promise<FavoriteStatistics> {
    const favorites = await this.listFavorites({ page: 0, size: 1000 });
    const content = favorites.content || [];
    
    return {
      total: content.length,
      onSale: content.filter((item) => item.status === 'ON_SALE').length,
      soldOut: content.filter((item) => item.status === 'SOLD_OUT').length,
      offShelf: content.filter((item) => item.status === 'OFF_SHELF').length,
    };
  }

  /**
   * 客户端排序（工具方法）
   */
  private sortFavorites(
    items: GoodsResponse[],
    sortBy: string,
    sortDirection: string
  ): GoodsResponse[] {
    const sorted = [...items].sort((a, b) => {
      let aValue: any;
      let bValue: any;

      switch (sortBy) {
        case 'createdAt':
          aValue = new Date(a.createdAt || 0).getTime();
          bValue = new Date(b.createdAt || 0).getTime();
          break;
        case 'price':
          aValue = a.price || 0;
          bValue = b.price || 0;
          break;
        case 'viewCount':
          aValue = a.viewCount || 0;
          bValue = b.viewCount || 0;
          break;
        default:
          return 0;
      }

      if (sortDirection === 'asc') {
        return aValue - bValue;
      } else {
        return bValue - aValue;
      }
    });

    return sorted;
  }
}

/**
 * 导出单例实例
 */
export const favoriteService = new FavoriteService();
