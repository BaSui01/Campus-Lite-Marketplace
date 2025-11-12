/**
 * ✅ 申诉 API 服务 - 完全重构版
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 *
 * 功能：
 * - 申诉列表查询（管理员视角）
 * - 申诉详情查看
 * - 申诉审核（单个/批量）
 * - 申诉统计数据
 * - 申诉材料管理
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  Appeal,
  AppealDetailResponse,
  AppealMaterial,
  PageAppealResponse,
  ReviewAppealRequest,
  BatchReviewRequest,
  BatchReviewResult,
  AppealStatistics,
} from '@campus/shared/api';

/**
 * 申诉列表查询参数
 */
export interface AppealListParams {
  keyword?: string;
  type?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

/**
 * 申诉 API 服务类
 */
export class AppealService {
  /**
   * 获取申诉列表（管理员视角）
   * @param params 查询参数
   * @returns 申诉列表（分页）
   */
  async listAppeals(params?: AppealListParams): Promise<PageAppealResponse> {
    const api = getApi();
    const response = await api.listAppeals({
      keyword: params?.keyword,
      type: params?.type,
      status: params?.status,
      startDate: params?.startDate,
      endDate: params?.endDate,
      page: params?.page,
      size: params?.size,
    });
    return response.data.data as PageAppealResponse;
  }

  /**
   * 获取待审核申诉列表
   * @param params 查询参数
   * @returns 待审核申诉列表（分页）
   */
  async listPendingAppeals(params?: {
    page?: number;
    size?: number;
  }): Promise<PageAppealResponse> {
    const api = getApi();
    const response = await api.listPendingAppeals({
      page: params?.page,
      size: params?.size,
    });
    return response.data.data as PageAppealResponse;
  }

  /**
   * 获取申诉详情
   * @param appealId 申诉ID
   * @returns 申诉详情
   */
  async getAppealDetail(appealId: number): Promise<AppealDetailResponse> {
    const api = getApi();
    const response = await api.getAppealDetail({ appealId });
    return response.data.data as AppealDetailResponse;
  }

  /**
   * 审核申诉（管理员）
   * @param appealId 申诉ID
   * @param approved 是否通过
   * @param reason 审核理由
   * @returns 审核后的申诉信息
   */
  async reviewAppeal(
    appealId: number,
    approved: boolean,
    reason: string
  ): Promise<Appeal> {
    const api = getApi();
    const response = await api.reviewAppeal({
      appealId,
      reviewAppealRequest: { approved, reason },
    });
    return response.data.data as Appeal;
  }

  /**
   * 批量审核申诉（管理员）
   * @param request 批量审核请求
   * @returns 批量审核结果
   */
  async batchReviewAppeals(request: BatchReviewRequest): Promise<BatchReviewResult> {
    const api = getApi();
    const response = await api.batchReviewAppeals({
      batchReviewRequest: request,
    });
    return response.data.data as BatchReviewResult;
  }

  /**
   * 获取申诉统计（管理员）
   * @returns 统计数据
   */
  async getAppealStatistics(): Promise<AppealStatistics> {
    const api = getApi();
    const response = await api.getAppealStatistics();
    return response.data.data as AppealStatistics;
  }

  /**
   * 获取申诉材料
   * @param appealId 申诉ID
   * @returns 申诉材料列表
   */
  async getAppealMaterials(appealId: string): Promise<AppealMaterial[]> {
    const api = getApi();
    const response = await api.getAppealMaterials({ appealId });
    return response.data.data as AppealMaterial[];
  }
}

// 导出单例
export const appealService = new AppealService();
export default appealService;
