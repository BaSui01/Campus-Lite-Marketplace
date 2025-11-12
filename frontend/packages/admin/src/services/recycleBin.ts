/**
 * 回收站服务
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { PageSoftDeleteRecord } from '@campus/shared/api';

/**
 * 回收站服务类
 */
export class RecycleBinService {
  /**
   * 获取软删除记录列表
   */
  async getRecords(
    entity: string,
    page: number = 0,
    size: number = 20
  ): Promise<PageSoftDeleteRecord> {
    const api = getApi();
    const response = await api.listSoftDeleteRecords({ entity, page, size });
    return response.data.data as PageSoftDeleteRecord;
  }
}

export const recycleBinService = new RecycleBinService();
export default recycleBinService;
