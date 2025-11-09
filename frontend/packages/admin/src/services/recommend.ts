/**
 * 推荐配置 API 服务
 * @author BaSui 😎
 * @description 推荐算法配置管理服务（管理端专属）
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { RecommendConfigDTO, RecommendStatisticsDTO } from '@campus/shared/api';

/**
 * 推荐配置 API 服务类
 */
export class RecommendService {
  /**
   * 获取推荐配置
   * @returns 推荐配置
   */
  async getConfig(): Promise<RecommendConfigDTO> {
    const api = getApi();
    const response = await api.getRecommendConfig();
    return response.data.data as RecommendConfigDTO;
  }

  /**
   * 更新推荐配置
   * @param config 推荐配置
   */
  async updateConfig(config: RecommendConfigDTO): Promise<void> {
    const api = getApi();
    await api.updateRecommendConfig({ recommendConfigDTO: config });
  }

  /**
   * 获取推荐统计
   * @returns 推荐统计
   */
  async getStatistics(): Promise<RecommendStatisticsDTO> {
    const api = getApi();
    const response = await api.getRecommendStatistics();
    return response.data.data as RecommendStatisticsDTO;
  }

  /**
   * 刷新热门榜单
   * @param campusId 校区ID（可选）
   */
  async refreshHotRanking(campusId?: number): Promise<void> {
    const api = getApi();
    await api.refreshHotRanking({ campusId });
  }
}

/**
 * 推荐配置服务实例
 */
export const recommendService = new RecommendService();

/**
 * 导出单例
 */
export default recommendService;
