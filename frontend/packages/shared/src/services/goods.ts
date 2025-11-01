/**
 * 商品 API 服务
 * @author BaSui 😎
 * @description 商品发布、查询、更新、删除、收藏等接口（对接 OpenAPI 生成的类型）
 */

import { DefaultApi } from '../api';
import { createApiConfig, axiosInstance } from '../utils/http';
import type {
  GoodsResponse,
  CreateGoodsRequest,
  PageGoodsResponse,
  ApiResponsePageGoodsResponse,
  ApiResponseGoodsDetailResponse,
  ApiResponseListGoodsResponse,
  ApiResponseListCategoryNodeResponse,
  ApiResponseListTagResponse,
  ApiResponseVoid,
  GoodsDetailResponse,
} from '../api/models';

/**
 * 商品列表查询参数
 */
export interface GoodsListParams {
  keyword?: string;        // 搜索关键词
  categoryId?: number;     // 分类 ID
  minPrice?: number;       // 最低价格
  maxPrice?: number;       // 最高价格
  page?: number;           // 页码（从 0 开始）
  size?: number;           // 每页大小
  sortBy?: string;         // 排序字段
  sortDirection?: string;  // 排序方向（asc/desc）
  tags?: number[];         // 标签 ID 列表
}

/**
 * 商品 API 服务类
 */
class GoodsService {
  private api: DefaultApi;

  constructor() {
    // 使用生成的 OpenAPI 客户端
    this.api = new DefaultApi(createApiConfig(), undefined, axiosInstance);
  }

  // ==================== 商品相关接口 ====================

  /**
   * 获取商品列表（分页）
   * @param params 查询参数
   * @returns 商品列表（分页）
   */
  async listGoods(params?: GoodsListParams): Promise<PageGoodsResponse> {
    const response = await this.api.listGoods(
      params?.keyword,
      params?.categoryId,
      params?.minPrice,
      params?.maxPrice,
      params?.page,
      params?.size,
      params?.sortBy,
      params?.sortDirection,
      params?.tags
    );
    return response.data.data as PageGoodsResponse;
  }

  /**
   * 获取商品详情
   * @param id 商品 ID
   * @returns 商品详情
   */
  async getGoodsDetail(id: number): Promise<GoodsDetailResponse> {
    const response = await this.api.getGoodsDetail(id);
    return response.data.data as GoodsDetailResponse;
  }

  /**
   * 获取推荐商品（热门榜单）
   * @param limit 数量限制
   * @returns 推荐商品列表
   */
  async getRecommendGoods(limit?: number): Promise<GoodsResponse[]> {
    // 使用 hot 接口获取热门商品（无需校区ID,后端会自动推断）
    const response = await this.api.hot(undefined, limit);
    return response.data.data as GoodsResponse[];
  }

  /**
   * 发布商品
   * @param data 商品信息
   * @returns 创建的商品 ID
   */
  async createGoods(data: CreateGoodsRequest): Promise<number> {
    const response = await this.api.createGoods(data);
    return response.data.data as number;
  }

  /**
   * 获取我发布的商品
   * @param params 查询参数
   * @returns 我的商品列表（分页）
   */
  async getMyGoods(params?: {
    status?: string;
    page?: number;
    size?: number;
  }): Promise<PageGoodsResponse> {
    const response = await this.api.getMyGoods(
      params?.status,
      params?.page,
      params?.size
    );
    return response.data.data as PageGoodsResponse;
  }

  /**
   * 搜索商品
   * @param keyword 搜索关键词
   * @param page 页码
   * @param size 每页大小
   * @returns 搜索结果（分页）
   */
  async searchGoods(keyword: string, page?: number, size?: number): Promise<PageGoodsResponse> {
    return this.listGoods({ keyword, page, size });
  }

  // ==================== 收藏相关接口 ====================

  /**
   * 收藏商品
   * @param goodsId 商品 ID
   * @returns 操作结果
   */
  async favoriteGoods(goodsId: number): Promise<void> {
    await this.api.favoriteGoods(goodsId);
  }

  /**
   * 取消收藏商品
   * @param goodsId 商品 ID
   * @returns 操作结果
   */
  async unfavoriteGoods(goodsId: number): Promise<void> {
    await this.api.unfavoriteGoods(goodsId);
  }

  /**
   * 检查是否已收藏
   * @param goodsId 商品 ID
   * @returns 是否已收藏
   */
  async isFavorited(goodsId: number): Promise<boolean> {
    const response = await this.api.isFavorited(goodsId);
    return response.data.data as boolean;
  }

  /**
   * 获取我的收藏列表
   * @param page 页码
   * @param size 每页大小
   * @returns 收藏列表（分页）
   */
  async getMyFavorites(page?: number, size?: number): Promise<PageGoodsResponse> {
    const response = await this.api.getMyFavorites(page, size);
    return response.data.data as PageGoodsResponse;
  }

  // ==================== 分类相关接口 ====================

  /**
   * 获取分类树
   * @returns 分类树列表
   */
  async getCategoryTree(): Promise<any[]> {
    const response = await this.api.getCategoryTree();
    return response.data.data as any[];
  }

  // ==================== 标签相关接口 ====================

  /**
   * 获取热门标签
   * @param limit 数量限制
   * @returns 标签列表
   */
  async getHotTags(limit?: number): Promise<any[]> {
    const response = await this.api.getPopularTags(limit);
    return response.data.data as any[];
  }
}

// 导出单例
export const goodsService = new GoodsService();
export default goodsService;
