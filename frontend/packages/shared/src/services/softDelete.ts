/**
 * ✅ 软删除治理服务 - 已重构为 OpenAPI
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 *
 * 功能：
 * - 查询支持软删除的实体列表
 * - 恢复已软删除的记录
 * - 彻底删除记录（绕过软删除）
 *
 * ⚠️ 注意：所有接口需要管理员权限（ADMIN角色）
 * 📋 API 路径：/api/admin/soft-delete/*
 */

import { getApi } from '../utils/apiClient';

/**
 * 软删除治理服务类
 */
export class SoftDeleteService {
  /**
   * 查询支持软删除的实体列表
   * GET /api/admin/soft-delete/targets
   * @returns 实体标识列表（如：["post", "goods", "user"]）
   */
  async listTargets(): Promise<string[]> {
    const api = getApi();
    const response = await api.listSoftDeleteTargets();
    return response.data.data as string[];
  }

  /**
   * 恢复已软删除的记录
   * POST /api/admin/soft-delete/{entity}/{id}/restore
   * @param entity 实体标识（如："post", "goods", "user"）
   * @param id 记录ID
   */
  async restore(entity: string, id: number): Promise<void> {
    const api = getApi();
    await api.restore({ entity, id });
  }

  /**
   * 彻底删除记录（绕过软删除）
   * DELETE /api/admin/soft-delete/{entity}/{id}/purge
   * @param entity 实体标识（如："post", "goods", "user"）
   * @param id 记录ID
   */
  async purge(entity: string, id: number): Promise<void> {
    const api = getApi();
    await api.purge({ entity, id });
  }
}

export const softDeleteService = new SoftDeleteService();
export default softDeleteService;
