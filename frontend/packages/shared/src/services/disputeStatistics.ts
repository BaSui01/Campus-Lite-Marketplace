/**
 * 纠纷统计服务
 * @author BaSui 😎
 * @description 纠纷数据统计与分析
 */

import { http } from '@campus/shared/utils/http';
import type { BaseResponse } from '@campus/shared/api';

// ==================== 类型定义 ====================

/**
 * 纠纷统计数据
 */
export interface DisputeStatistics {
  // 概览数据
  totalDisputes: number;
  processingDisputes: number;
  resolvedDisputes: number;
  closedDisputes: number;
  
  // 解决率与平均处理时长
  resolutionRate: number;
  avgProcessingTimeHours: number;
  
  // 趋势数据（按日期）
  trendData?: {
    date: string;
    newDisputes: number;
    resolvedDisputes: number;
    closedDisputes: number;
  }[];
  
  // 分类统计
  categoryDistribution?: {
    category: string;
    count: number;
    percentage: number;
  }[];
  
  // 仲裁员统计
  arbitratorStats?: {
    arbitratorId: number;
    arbitratorName: string;
    handledCount: number;
    resolvedCount: number;
    resolutionRate: number;
    avgProcessingTimeHours: number;
  }[];
}

// ==================== 服务接口 ====================

export interface DisputeStatisticsService {
  /** 获取纠纷统计数据 */
  getStatistics(): Promise<DisputeStatistics>;
}

// ==================== 服务实现 ====================

class DisputeStatisticsServiceImpl implements DisputeStatisticsService {
  private readonly BASE_PATH = '/api/disputes/statistics';

  async getStatistics(): Promise<DisputeStatistics> {
    const response = await http.get<BaseResponse<DisputeStatistics>>(`${this.BASE_PATH}`);
    return response.data.data;
  }
}

// ==================== 导出服务实例 ====================

export const disputeStatisticsService = new DisputeStatisticsServiceImpl();
