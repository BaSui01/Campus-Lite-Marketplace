/**
 * ✅ 合规管理 API 服务 - 完全重构版
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  ComplianceWhitelist,
  ComplianceAuditLog,
  PageComplianceAuditLog,
} from '@campus/shared/api';

export class ComplianceService {
  async listWhitelist(): Promise<ComplianceWhitelist[]> {
    const api = getApi();
    const response = await api.listWhitelist();
    return response.data.data as ComplianceWhitelist[];
  }

  async addWhitelist(type: string, targetId: number): Promise<ComplianceWhitelist> {
    const api = getApi();
    const response = await api.addWhitelist({ type, targetId });
    return response.data.data as ComplianceWhitelist;
  }

  async removeWhitelist(id: number): Promise<void> {
    const api = getApi();
    await api.removeWhitelist({ id });
  }

  async listAudit(
    targetType: string,
    targetId: number,
    page: number = 0,
    size: number = 20
  ): Promise<PageComplianceAuditLog> {
    const api = getApi();
    const response = await api.listAudit({ targetType, targetId, page, size });
    return response.data.data as PageComplianceAuditLog;
  }
}

export const complianceService = new ComplianceService();
export default complianceService;
