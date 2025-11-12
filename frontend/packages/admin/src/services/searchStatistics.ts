/**
 * 搜索统计服务
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { SearchStatistics, PopularKeyword } from '@campus/shared/api';

/**
 * 搜索统计服务类
 */
export class SearchStatisticsService {
  /**
   * 获取搜索统计数据
   */
  async getStatistics(startDate: string, endDate: string): Promise<SearchStatistics> {
    const api = getApi();
    const response = await api.getMessageSearchStatistics({ startDate, endDate });
    return response.data.data as SearchStatistics;
  }

  /**
   * 获取热门搜索关键词
   */
  async getPopularKeywords(limit: number = 10): Promise<PopularKeyword[]> {
    const api = getApi();
    const response = await api.getPopularKeywords({ limit });
    return response.data.data as PopularKeyword[];
  }
}

export const searchStatisticsService = new SearchStatisticsService();
export default searchStatisticsService;
