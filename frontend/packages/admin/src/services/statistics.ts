/**
 * 管理端统计服务（基于真实后端API）
 *
 * ⚠️ 注意：所有接口需要管理员权限（ADMIN角色）
 * 后端接口路径：/api/admin/statistics/*
 */

import { getApi } from '../utils/apiClient';
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
  // 🔥 修复：改为 getter 方法，延迟初始化（避免 import.meta.env 还没加载完）
  private get api() {
    return getApi();
  }

  /**
   * 📊 获取系统概览统计
   * GET /api/admin/statistics/overview
   */
  async getSystemOverview(): Promise<SystemOverview> {
    try {
      // 使用 axios 直接调用（因为 OpenAPI 可能还没生成这个接口）
      const response = await this.api.axiosInstance.get<ApiResponse<SystemOverview>>(
        '/admin/statistics/overview'
      );
      return response.data.data as SystemOverview;
    } catch (error: any) {
      console.error('❌ 获取系统概览失败:', error.response?.data?.message || error.message);
      throw error;
    }
  }

  /**
   * 📈 获取趋势统计
   * GET /api/admin/statistics/trend?days=30
   */
  async getTrendStatistics(days = 7): Promise<TrendStatistics> {
    try {
      const response = await this.api.axiosInstance.get<ApiResponse<any>>(
        '/admin/statistics/trend',
        { params: { days } }
      );

      const data = response.data.data;

      // 转换后端数据格式为前端需要的格式
      return {
        userTrend: this.convertTrendData(data.dates, data.userCounts),
        goodsTrend: this.convertTrendData(data.dates, data.goodsCounts),
        orderTrend: this.convertTrendData(data.dates, data.orderCounts),
      };
    } catch (error: any) {
      console.error('❌ 获取趋势数据失败:', error.response?.data?.message || error.message);
      return { userTrend: [], goodsTrend: [], orderTrend: [] };
    }
  }

  /**
   * 📈 获取用户趋势（用于图表）
   */
  async getUserTrend(days = 30): Promise<{ name: string; value: number }[]> {
    const trendData = await this.getTrendStatistics(days);
    return trendData.userTrend.map(item => ({
      name: item.date,
      value: item.value,
    }));
  }

  /**
   * 💰 获取收入趋势
   * GET /api/admin/statistics/revenue?months=1
   */
  async getRevenueTrend(months = 1): Promise<{ name: string; value: number }[]> {
    try {
      const response = await this.api.axiosInstance.get<ApiResponse<any>>(
        '/admin/statistics/revenue',
        { params: { months } }
      );

      const data = response.data.data;

      // 转换格式：{ months: [...], revenues: [...] } => [{ name, value }]
      if (data.months && data.revenues) {
        return data.months.map((month: string, index: number) => ({
          name: month,
          value: Number(data.revenues[index]) || 0,
        }));
      }

      return [];
    } catch (error: any) {
      console.error('❌ 获取收入趋势失败:', error.response?.data?.message || error.message);
      return [];
    }
  }

  /**
   * 🏆 获取热门商品排行
   * GET /api/admin/statistics/top-goods?limit=10
   */
  async getTopGoods(limit = 10): Promise<RankingItem[]> {
    try {
      const response = await this.api.axiosInstance.get<ApiResponse<any[]>>(
        '/admin/statistics/top-goods',
        { params: { limit } }
      );

      const data = response.data.data || [];
      return data.map((item: any) => ({
        id: item.id || 0,
        name: item.title || '未知商品',
        value: item.viewCount || 0,
        category: item.category,
        count: item.viewCount || 0,
      }));
    } catch (error: any) {
      console.error('❌ 获取热门商品失败:', error.response?.data?.message || error.message);
      return [];
    }
  }

  /**
   * 👥 获取活跃用户排行
   * GET /api/admin/statistics/top-users?limit=10
   */
  async getTopUsers(limit = 10): Promise<RankingItem[]> {
    try {
      const response = await this.api.axiosInstance.get<ApiResponse<any[]>>(
        '/admin/statistics/top-users',
        { params: { limit } }
      );

      const data = response.data.data || [];
      return data.map((item: any) => ({
        id: item.userId || 0,
        name: item.username || '未知用户',
        value: item.goodsCount || 0,
        avatar: item.avatar,
      }));
    } catch (error: any) {
      console.error('❌ 获取活跃用户失败:', error.response?.data?.message || error.message);
      return [];
    }
  }

  /**
   * 工具方法：转换趋势数据格式
   */
  private convertTrendData(dates: string[], counts: number[]): TrendPoint[] {
    if (!dates || !counts) return [];
    return dates.map((date, index) => ({
      date,
      value: counts[index] || 0,
    }));
  }

  /**
   * 📂 获取分类统计
   * GET /api/admin/statistics/categories
   */
  async getCategoryStatistics(): Promise<CategoryStat[]> {
    try {
      const response = await this.api.axiosInstance.get<ApiResponse<Record<string, number>>>(
        '/admin/statistics/categories'
      );

      const data = response.data.data || {};

      // 转换格式：{ "电子产品": 10 } => [{ categoryName: "电子产品", count: 10 }]
      return Object.entries(data).map(([categoryName, count], index) => ({
        categoryId: index + 1,
        categoryName,
        count: Number(count) || 0,
      }));
    } catch (error: any) {
      console.error('❌ 获取分类统计失败:', error.response?.data?.message || error.message);
      return [];
    }
  }

  /**
   * 📅 获取今日统计
   * GET /api/admin/statistics/today
   */
  async getTodayStatistics(): Promise<TodayStatistics> {
    try {
      const response = await this.api.axiosInstance.get<ApiResponse<TodayStatistics>>(
        '/admin/statistics/today'
      );
      return response.data.data as TodayStatistics;
    } catch (error: any) {
      console.error('❌ 获取今日统计失败:', error.response?.data?.message || error.message);
      return { newUsers: 0, newGoods: 0, newOrders: 0, revenue: 0 };
    }
  }
}

export const statisticsService = new StatisticsService();
export default statisticsService;
