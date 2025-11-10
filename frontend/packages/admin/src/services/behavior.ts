/**
 * 用户行为分析服务
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { PageUserBehaviorLogDTO, UserBehaviorStatistics } from '@campus/shared/api';

/**
 * 用户行为分析服务类
 */
export class BehaviorService {
  /**
   * 获取用户行为日志列表
   */
  async getUserBehaviors(
    userId?: number,
    behaviorType?: string,
    startDate?: string,
    endDate?: string,
    page: number = 0,
    size: number = 20
  ): Promise<PageUserBehaviorLogDTO> {
    const api = getApi();
    const response = await api.getUserBehaviors({
      userId,
      behaviorType,
      startDate,
      endDate,
      page,
      size,
    });
    return response.data.data as PageUserBehaviorLogDTO;
  }

  /**
   * 获取行为统计数据
   */
  async getStatistics(startDate: string, endDate: string): Promise<UserBehaviorStatistics> {
    const api = getApi();
    const response = await api.getBehaviorStatistics({ startDate, endDate });
    return response.data.data as UserBehaviorStatistics;
  }
}

export const behaviorService = new BehaviorService();
export default behaviorService;
