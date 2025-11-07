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
 * 纠纷统计服务
 * @author BaSui 😎
 * @description 纠纷数据统计与分析
 */

import { apiClient } from '@campus/shared/utils/apiClient';
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
