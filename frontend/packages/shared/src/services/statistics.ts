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
    return res.data || {} as SystemOverview;
  }

  async getTrendStatistics(days = 7): Promise<TrendStatistics> {
    const res = await http.get<ApiResponse<TrendStatistics>>('/api/admin/statistics/trend', {
      params: { days },
    });
    return res.data || { userTrend: [], goodsTrend: [], orderTrend: [] };
  }

  async getUserTrend(days = 30): Promise<{ name: string; value: number }[]> {
    const res = await http.get<ApiResponse<any>>('/api/admin/statistics/trend', {
      params: { days },
    });
    // 后端返回的 trend 数据包含多个维度，这里提取用户趋势
    const trendData = res.data || {};
    const userTrend = trendData.userTrend || [];
    return userTrend.map((item: any) => ({
      name: item.date || item.name || '',
      value: item.value || item.count || 0,
    }));
  }

  async getRevenueTrend(days = 30): Promise<{ name: string; value: number }[]> {
    const res = await http.get<ApiResponse<any>>('/api/admin/statistics/revenue', {
      params: { months: Math.ceil(days / 30) },
    });
    // 将收入数据转换为图表格式
    const revenueData = res.data || {};
    return Object.entries(revenueData).map(([month, value]) => ({
      name: month,
      value: Number(value) || 0,
    }));
  }

  async getTopGoods(limit = 10): Promise<RankingItem[]> {
    const res = await http.get<ApiResponse<any[]>>('/api/admin/statistics/top-goods', {
      params: { limit },
    });
    const data = res.data || [];
    return data.map((item: any) => ({
      id: item.id || item.goodsId || 0,
      name: item.name || item.title || '未知商品',
      value: item.value || item.viewCount || item.count || 0,
      category: item.category || item.categoryName,
      count: item.count || item.value || 0,
    }));
  }

  async getTopUsers(limit = 10): Promise<RankingItem[]> {
    const res = await http.get<ApiResponse<any[]>>('/api/admin/statistics/top-users', {
      params: { limit },
    });
    const data = res.data || [];
    return data.map((item: any) => ({
      id: item.id || item.userId || 0,
      name: item.name || item.username || item.nickname || '未知用户',
      value: item.value || item.points || item.score || 0,
      avatar: item.avatar || item.avatarUrl,
    }));
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
