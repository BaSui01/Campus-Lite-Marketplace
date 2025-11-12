/**
 * 纠纷服务 - 用户端
 * ✅ 已重构：使用 OpenAPI 生成的 DefaultApi
 *
 * @author BaSui 😎
 * @description 用户端纠纷提交、查询、升级、协商等功能
 * @date 2025-11-07
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  ApiResponseLong,
  ApiResponsePageDisputeDTO,
  ApiResponseDisputeDetailDTO,
  ApiResponseBoolean,
  CreateDisputeRequest,
  GetUserDisputesStatusEnum,
} from '@campus/shared/api';

/**
 * 纠纷状态枚举 (对应后端 DisputeStatus)
 */
export enum DisputeStatus {
  NEGOTIATING = 'NEGOTIATING', // 协商中
  PENDING_ARBITRATION = 'PENDING_ARBITRATION', // 待仲裁
  ARBITRATING = 'ARBITRATING', // 仲裁中
  RESOLVED = 'RESOLVED', // 已解决
  CLOSED = 'CLOSED', // 已关闭
}

/**
 * 纠纷服务类
 */
export class DisputeService {
  private api = getApi();

  /**
   * 提交纠纷 🚀
   *
   * POST /api/disputes
   *
   * @param request 纠纷请求
   * @returns 纠纷ID
   */
  async submitDispute(request: CreateDisputeRequest): Promise<number> {
    const response = await this.api.submitDispute(request);
    const result = response.data as ApiResponseLong;

    if (result.code === 200 && result.data !== undefined) {
      return result.data;
    }

    throw new Error(result.message || '提交纠纷失败');
  }

  /**
   * 查询我的纠纷列表 📋
   *
   * GET /api/disputes?status=NEGOTIATING&page=0&size=20
   *
   * @param params 查询参数
   * @returns 纠纷列表（分页）
   */
  async getUserDisputes(params?: {
    status?: GetUserDisputesStatusEnum;
    page?: number;
    size?: number;
  }): Promise<ApiResponsePageDisputeDTO> {
    const response = await this.api.getUserDisputes(
      params?.status,
      params?.page || 0,
      params?.size || 20
    );
    return response.data as ApiResponsePageDisputeDTO;
  }

  /**
   * 查询纠纷详情 🔍
   *
   * GET /api/disputes/{disputeId}
   *
   * @param disputeId 纠纷ID
   * @returns 纠纷详情（包含订单信息、证据、协商记录等）
   */
  async getDisputeDetail(disputeId: number): Promise<ApiResponseDisputeDetailDTO> {
    const response = await this.api.getDisputeDetail(disputeId);
    return response.data as ApiResponseDisputeDetailDTO;
  }

  /**
   * 升级纠纷为仲裁 ⚡
   *
   * POST /api/disputes/{disputeId}/escalate
   *
   * @param disputeId 纠纷ID
   * @returns 是否成功
   */
  async escalateToArbitration(disputeId: number): Promise<boolean> {
    const response = await this.api.escalateToArbitration(disputeId);
    const result = response.data as ApiResponseBoolean;

    if (result.code === 200 && result.data !== undefined) {
      return result.data;
    }

    throw new Error(result.message || '升级纠纷失败');
  }

  /**
   * 关闭纠纷 ❌
   *
   * POST /api/disputes/{disputeId}/close
   *
   * 注意：用户端一般不直接调用此接口，由管理员关闭纠纷
   *
   * @param disputeId 纠纷ID
   * @param closeReason 关闭原因
   * @returns 是否成功
   */
  async closeDispute(disputeId: number, closeReason: string): Promise<boolean> {
    const response = await this.api.closeDispute(disputeId, closeReason);
    const result = response.data as ApiResponseBoolean;

    if (result.code === 200 && result.data !== undefined) {
      return result.data;
    }

    throw new Error(result.message || '关闭纠纷失败');
  }
}

/**
 * 导出纠纷服务单例
 */
export const disputeService = new DisputeService();
export default disputeService;
