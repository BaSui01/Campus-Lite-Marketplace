/**
 * 软删除治理服务
 */

import { http } from '../utils/http';
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
