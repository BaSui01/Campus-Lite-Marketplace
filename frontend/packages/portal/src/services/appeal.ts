/**
 * 申诉服务 - 用户端
 * ✅ 已重构：使用 OpenAPI 生成的 DefaultApi
 *
 * @author BaSui 😎
 * @description 用户端申诉提交、查询、取消等功能
 * @date 2025-11-07
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  ApiResponseLong,
  ApiResponsePageAppeal,
  ApiResponseAppealDetailResponse,
  ApiResponseVoid,
  ApiResponseBoolean,
  CreateAppealRequest,
} from '@campus/shared/api';

/**
 * 申诉服务类
 */
export class AppealService {
  private api = getApi();

  /**
   * 提交申诉 🚀
   *
   * POST /api/appeals
   *
   * @param request 申诉请求
   * @returns 申诉ID
   */
  async submitAppeal(request: CreateAppealRequest): Promise<number> {
    const response = await this.api.submitAppeal(request);
    const result = response.data as ApiResponseLong;

    if (result.code === 200 && result.data !== undefined) {
      return result.data;
    }

    throw new Error(result.message || '提交申诉失败');
  }

  /**
   * 查询我的申诉列表 📋
   *
   * GET /api/appeals/my?page=0&size=10
   *
   * @param params 查询参数
   * @returns 申诉列表（分页）
   */
  async getMyAppeals(params?: {
    page?: number;
    size?: number;
  }): Promise<ApiResponsePageAppeal> {
    const response = await this.api.getMyAppeals(
      params?.page || 0,
      params?.size || 10
    );
    return response.data as ApiResponsePageAppeal;
  }

  /**
   * 查询申诉详情 🔍
   *
   * GET /api/appeals/{appealId}
   *
   * @param appealId 申诉ID
   * @returns 申诉详情（包含材料、历史记录等）
   */
  async getAppealDetail(appealId: number): Promise<ApiResponseAppealDetailResponse> {
    const response = await this.api.getAppealDetail(appealId);
    return response.data as ApiResponseAppealDetailResponse;
  }

  /**
   * 取消申诉 ❌
   *
   * POST /api/appeals/{appealId}/cancel
   *
   * @param appealId 申诉ID
   * @returns 是否成功
   */
  async cancelAppeal(appealId: number): Promise<void> {
    const response = await this.api.cancelAppeal(appealId);
    const result = response.data as ApiResponseVoid;

    if (result.code !== 200) {
      throw new Error(result.message || '取消申诉失败');
    }
  }

  /**
   * 验证申诉资格 ✅
   *
   * POST /api/appeals/validate
   *
   * @param request 申诉请求（用于验证）
   * @returns 是否有资格提交申诉
   */
  async validateAppealEligibility(request: CreateAppealRequest): Promise<boolean> {
    const response = await this.api.validateAppealEligibility(request);
    const result = response.data as ApiResponseBoolean;

    if (result.code === 200 && result.data !== undefined) {
      return result.data;
    }

    return false;
  }
}

/**
 * 导出申诉服务单例
 */
export const appealService = new AppealService();
export default appealService;
