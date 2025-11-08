/**
 * ✅ 管理端统计服务 - 完全重构版
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 *
 * 功能：
 * - 系统概览统计
 * - 趋势数据分析
 * - 收入趋势
 * - 热门商品排行
 * - 活跃用户排行
 * - 分类统计
 * - 今日统计
 *
 * ⚠️ 注意：所有接口需要管理员权限（ADMIN角色）
 * 📋 API 路径：/api/admin/statistics/*
 */

import { getApi } from '@campus/shared/utils/apiClient';

// ==================== 类型定义 ====================

/**
 * 🎯 系统概览统计数据
 */
export interface SystemOverview {
  totalUsers: number;        // 总用户数
  totalGoods: number;        // 总商品数
  totalOrders: number;       // 总订单数
  totalRevenue: number;      // 总收入
  todayNewUsers: number;     // 今日新增用户
  todayNewGoods: number;     // 今日新增商品
  todayNewOrders: number;    // 今日新增订单
  activeUsers: number;       // 活跃用户数
  pendingGoods: number;      // 待审核商品数
}

/**
 * 📈 趋势数据点
 */
export interface TrendPoint {
  date: string;   // 日期（格式：YYYY-MM-DD）
  value: number;  // 数值
}

/**
 * 📈 趋势统计数据
 */
export interface TrendStatistics {
  userTrend: TrendPoint[];   // 用户趋势
  goodsTrend: TrendPoint[];  // 商品趋势
  orderTrend: TrendPoint[];  // 订单趋势
}

/**
 * 🏆 排行榜项目
 */
export interface RankingItem {
  id: number | string;  // ID
  name: string;         // 名称
  value: number;        // 数值
  avatar?: string;      // 头像（可选）
  category?: string;    // 分类（可选）
  count?: number;       // 数量（可选）
}

/**
 * 📂 分类统计
 */
export interface CategoryStat {
  categoryId: number;     // 分类ID
  categoryName: string;   // 分类名称
  count: number;          // 商品数量
}

/**
 * 📅 今日统计
 */
export interface TodayStatistics {
  newUsers: number;   // 今日新增用户
  newGoods: number;   // 今日新增商品
  newOrders: number;  // 今日新增订单
  revenue: number;    // 今日收入
}

// ==================== Service 类 ====================

/**
 * 管理端统计服务类
 */
export class StatisticsService {
  /**
   * 📊 获取系统概览统计
   * GET /api/admin/statistics/overview
   */
  async getSystemOverview(): Promise<SystemOverview> {
    try {
      const api = getApi();
      const response = await api.getSystemOverview();

      const data = response.data.data as Record<string, any>;

      // 转换后端返回的 Map<String, Object> 为前端需要的类型
      return {
        totalUsers: Number(data.totalUsers) || 0,
        totalGoods: Number(data.totalGoods) || 0,
        totalOrders: Number(data.totalOrders) || 0,
        totalRevenue: Number(data.totalRevenue) || 0,
        todayNewUsers: Number(data.todayNewUsers) || 0,
        todayNewGoods: Number(data.todayNewGoods) || 0,
        todayNewOrders: Number(data.todayNewOrders) || 0,
        activeUsers: Number(data.activeUsers) || 0,
        pendingGoods: Number(data.pendingGoods) || 0,
      };
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
      const api = getApi();
      const response = await api.getTrend({ days });

      const data = response.data.data as Record<string, any>;

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
      const api = getApi();
      const response = await api.getRevenueTrend({ months });

      const data = response.data.data as Record<string, any>;

      // 转换格式：{ months: [...], revenues: [...] } => [{ name, value }]
      if (data.months && data.revenues) {
        const monthsList = data.months as string[];
        const revenuesList = data.revenues as number[];

        return monthsList.map((month: string, index: number) => ({
          name: month,
          value: Number(revenuesList[index]) || 0,
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
      const api = getApi();
      const response = await api.getTopGoods({ limit });

      const data = (response.data.data || []) as Array<Record<string, any>>;

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
      const api = getApi();
      const response = await api.getTopUsers({ limit });

      const data = (response.data.data || []) as Array<Record<string, any>>;

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
   * 📂 获取分类统计
   * GET /api/admin/statistics/categories
   */
  async getCategoryStatistics(): Promise<CategoryStat[]> {
    try {
      const api = getApi();
      const response = await api.getCategoryStatistics();

      const data = (response.data.data || {}) as Record<string, number>;

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
      const api = getApi();
      const response = await api.getTodayStatistics();

      const data = response.data.data as Record<string, any>;

      return {
        newUsers: Number(data.newUsers) || 0,
        newGoods: Number(data.newGoods) || 0,
        newOrders: Number(data.newOrders) || 0,
        revenue: Number(data.revenue) || 0,
      };
    } catch (error: any) {
      console.error('❌ 获取今日统计失败:', error.response?.data?.message || error.message);
      return { newUsers: 0, newGoods: 0, newOrders: 0, revenue: 0 };
    }
  }

  /**
   * 🔧 工具方法：转换趋势数据格式
   */
  private convertTrendData(dates: string[], counts: number[]): TrendPoint[] {
    if (!dates || !counts) return [];
    return dates.map((date, index) => ({
      date,
      value: counts[index] || 0,
    }));
  }
}

// ==================== 导出单例 ====================

export const statisticsService = new StatisticsService();
export default statisticsService;
