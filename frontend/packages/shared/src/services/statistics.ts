/**
 * 管理端统计服务
 */

import { http } from '../utils/http';
import type { ApiResponse } from '../types';

export interface SystemOverview {
  totalUsers: number;
  totalGoods: number;
  totalOrders: number;
  totalRevenue: number;
  todayNewUsers: number;
  todayNewGoods: number;
  todayNewOrders: number;
  activeUsers: number;
  pendingGoods: number;
}

export interface TrendPoint {
  date: string;
  value: number;
}

export interface TrendStatistics {
  userTrend: TrendPoint[];
  goodsTrend: TrendPoint[];
  orderTrend: TrendPoint[];
}

export interface RankingItem {
  id: number | string;
  name: string;
  value: number;
  avatar?: string;
}

export interface CategoryStat {
  categoryId: number;
  categoryName: string;
  count: number;
}

export interface TodayStatistics {
  newUsers: number;
  newGoods: number;
  newOrders: number;
  revenue: number;
}

export class StatisticsService {
  async getSystemOverview(): Promise<SystemOverview> {
    const res = await http.get<ApiResponse<SystemOverview>>('/api/admin/statistics/overview');
    return res.data;
  }

  async getTrendStatistics(days = 7): Promise<TrendStatistics> {
    const res = await http.get<ApiResponse<TrendStatistics>>('/api/admin/statistics/trend', {
      params: { days },
    });
    return res.data;
  }

  async getTopGoods(limit = 10): Promise<RankingItem[]> {
    const res = await http.get<ApiResponse<RankingItem[]>>('/api/admin/statistics/top-goods', {
      params: { limit },
    });
    return res.data;
  }

  async getTopUsers(limit = 10): Promise<RankingItem[]> {
    const res = await http.get<ApiResponse<RankingItem[]>>('/api/admin/statistics/top-users', {
      params: { limit },
    });
    return res.data;
  }

  async getCategoryStatistics(): Promise<CategoryStat[]> {
    const res = await http.get<ApiResponse<CategoryStat[]>>('/api/admin/statistics/categories');
    return res.data;
  }

  async getRevenueByMonth(months = 12): Promise<Record<string, number>> {
    const res = await http.get<ApiResponse<Record<string, number>>>('/api/admin/statistics/revenue', {
      params: { months },
    });
    return res.data;
  }

  async getTodayStatistics(): Promise<TodayStatistics> {
    const res = await http.get<ApiResponse<TodayStatistics>>('/api/admin/statistics/today');
    return res.data;
  }
}

export const statisticsService = new StatisticsService();
export default statisticsService;
