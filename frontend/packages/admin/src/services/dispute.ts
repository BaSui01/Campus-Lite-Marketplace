/**
 * ✅ 纠纷仲裁 API 服务 - 完全重构版
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  DisputeDTO,
  DisputeDetailDTO,
  PageDisputeDTO,
  ArbitrateDisputeRequest,
  BatchAssignRequest,
} from '@campus/shared/api';

export interface DisputeListParams {
  keyword?: string;
  type?: string;
  status?: string;
  arbitratorId?: number;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export class DisputeService {
  async listDisputes(params?: DisputeListParams): Promise<PageDisputeDTO> {
    const api = getApi();
    const response = await api.getAllDisputes({
      status: params?.status as any,
      page: params?.page,
      size: params?.size,
    });
    return response.data.data as PageDisputeDTO;
  }

  async getDisputeDetail(id: number): Promise<DisputeDetailDTO> {
    const api = getApi();
    const response = await api.getDisputeDetail({ disputeId: id });
    return response.data.data as DisputeDetailDTO;
  }

  async claimDispute(id: number): Promise<void> {
    const api = getApi();
    await api.claimDispute({ id });
  }

  async arbitrateDispute(id: number, request: ArbitrateDisputeRequest): Promise<void> {
    const api = getApi();
    await api.arbitrateDispute({ id, arbitrateDisputeRequest: request });
  }

  async closeDispute(id: number, reason: string): Promise<void> {
    const api = getApi();
    await api.closeDispute1({ id, reason });
  }

  async batchAssignArbitrator(disputeIds: number[], arbitratorId: number): Promise<void> {
    const api = getApi();
    await api.batchAssignArbitrator({ disputeIds, arbitratorId });
  }
}

export const disputeService = new DisputeService();
export default disputeService;
