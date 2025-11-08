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
 * 软删除治理服务
 */

import { getApi } from '../utils/apiClient';
import type { ApiResponse } from '../types';

export class SoftDeleteService {
  async listTargets(): Promise<string[]> {
    const res = await http.get<ApiResponse<string[]>>('/api/admin/soft-delete/targets');
    return res.data;
  }

  async restore(entity: string, id: number): Promise<void> {
    await http.post<ApiResponse<void>>(`/api/admin/soft-delete/${entity}/${id}/restore`);
  }

  async purge(entity: string, id: number): Promise<void> {
    await http.delete<ApiResponse<void>>(`/api/admin/soft-delete/${entity}/${id}/purge`);
  }
}

export const softDeleteService = new SoftDeleteService();
export default softDeleteService;
