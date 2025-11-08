/**
 * ✅ 管理员商品服务
 * @author BaSui 😎
 * @description 商品审核、状态管理、批量操作等管理员专属功能（基于 OpenAPI）
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { PageGoodsResponse } from '@campus/shared/api/models';

/**
 * 管理员商品服务类
 */
export class AdminGoodsService {
  /**
   * 获取待审核商品列表（管理员）
   * @param params 查询参数
   * @returns 待审核商品列表
   */
  async listPendingGoods(params?: {
    page?: number;
    size?: number;
  }): Promise<PageGoodsResponse> {
    const api = getApi();
    const response = await api.getPendingGoods(params?.page, params?.size);
    return response.data.data as PageGoodsResponse;
  }

  /**
   * 审核商品（管理员）
   * @param id 商品 ID
   * @param request 审核请求（approved: boolean, reason?: string）
   * @returns 操作结果
   */
  async approveGoods(id: number, request: { approved: boolean; reason?: string }): Promise<void> {
    const api = getApi();
    await api.approveGoods(id, request);
  }

  /**
   * 更新商品状态（管理员）
   * @param id 商品 ID
   * @param status 目标状态（APPROVED/REJECTED/DELETED）
   * @returns 操作结果
   */
  async updateGoodsStatus(id: number, status: string): Promise<void> {
    const api = getApi();
    await api.updateGoodsStatus(id, { status });
  }

  /**
   * 删除商品（管理员）
   * @param id 商品 ID
   * @returns 操作结果
   */
  async deleteGoods(id: number): Promise<void> {
    const api = getApi();
    await api.deleteGoods1(id);
  }

  /**
   * 批量更新商品（管理员）
   * @param request 批量操作请求
   * @returns 操作结果
   */
  async batchUpdateGoods(request: {
    goodsIds: number[];
    targetStatus: string;
  }): Promise<void> {
    const api = getApi();
    await api.batchUpdateGoodsStatus({
      goodsIds: request.goodsIds,
      targetStatus: request.targetStatus,
    });
  }
}

// 导出单例
export const adminGoodsService = new AdminGoodsService();
export default adminGoodsService;
