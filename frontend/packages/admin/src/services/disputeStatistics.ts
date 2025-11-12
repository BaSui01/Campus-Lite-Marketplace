/**
 * ✅ 纠纷统计 API 服务 - 完全重构版
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 *
 * 功能：
 * - 纠纷数据统计
 * - 纠纷趋势分析
 * - 仲裁员绩效统计
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { DisputeStatisticsDTO } from '@campus/shared/api';

/**
 * 纠纷统计 API 服务类
 */
export class DisputeStatisticsService {
  /**
   * 获取纠纷统计数据
   * @returns 纠纷统计信息
   */
  async getStatistics(): Promise<DisputeStatisticsDTO> {
    const api = getApi();
    const response = await api.getStatistics1();
    return response.data.data as DisputeStatisticsDTO;
  }
}

/**
 * 纠纷统计服务实例
 */
export const disputeStatisticsService = new DisputeStatisticsService();

/**
 * 导出单例
 */
export default disputeStatisticsService;
