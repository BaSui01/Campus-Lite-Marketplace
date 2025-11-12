/**
 * ✅ 已重构：使用 OpenAPI 生成的 DefaultApi（管理端统计服务）
 * 📋 API 路径：/api/admin/statistics/*
 * 🔗 对应 Controller：backend/.../controller/admin/AdminStatisticsController.java
 *
 * ⚠️ 注意：所有接口需要管理员权限（ADMIN角色）
 */

import { getApi } from '../utils/apiClient';

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
 * 📊 趋势统计数据
 */
export interface TrendStatistics {
  userTrend: TrendPoint[];   // 用户趋势
  goodsTrend: TrendPoint[];  // 商品趋势
  orderTrend: TrendPoint[];  // 订单趋势
}

/**
 * 🏆 排行榜项
 */
export interface RankingItem {
  id: number | string;
  name: string;
  value: number;
  avatar?: string;
  category?: string;
  count?: number;
}

/**
 * 📂 分类统计
 */
export interface CategoryStat {
  categoryId: number;
  categoryName: string;
  count: number;
}

/**
 * 📅 今日统计
 */
export interface TodayStatistics {
  newUsers: number;
  newGoods: number;
  newOrders: number;
  revenue: number;
}

/**
 * 📦 订单统计数据
 */
export interface OrderStatistics {
  totalOrders: number;
  pendingPaymentOrders: number;
  paidOrders: number;
  completedOrders: number;
  cancelledOrders: number;
  refundingOrders: number;
  refundedOrders: number;
  totalAmount: number;
  completedAmount: number;
  refundedAmount: number;
  averageAmount: number;
  completionRate: number;
  cancellationRate: number;
  refundRate: number;
  ordersByStatus: Record<string, number>;
  amountByPaymentMethod: Record<string, number>;
  countByPaymentMethod: Record<string, number>;
  todayNewOrders: number;
  todayAmount: number;
  todayCompletedOrders: number;
}

/**
 * 💰 退款统计数据
 */
export interface RefundStatistics {
  totalRefunds: number;
  appliedRefunds: number;
  approvedRefunds: number;
  rejectedRefunds: number;
  processingRefunds: number;
  completedRefunds: number;
  failedRefunds: number;
  totalAmount: number;
  completedAmount: number;
  processingAmount: number;
  averageAmount: number;
  approvalRate: number;
  successRate: number;
  failureRate: number;
  refundsByStatus: Record<string, number>;
  amountByChannel: Record<string, number>;
  countByChannel: Record<string, number>;
  todayNewRefunds: number;
  todayAmount: number;
  todayCompletedRefunds: number;
  avgReviewTime: number;
  avgCompletionTime: number;
}

/**
 * 管理端统计服务（基于真实后端 API）
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
      const response = await api.getTrendData({ days });

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
      const response = await api.getRevenueByMonth({ months });

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

      const data = (response.data.data || []) as Record<string, any>[];

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

      const data = (response.data.data || []) as Record<string, any>[];

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
   * 📦 获取订单统计
   * GET /api/admin/statistics/orders
   */
  async getOrderStatistics(startDate?: string, endDate?: string): Promise<OrderStatistics> {
    try {
      const api = getApi();
      const response = await api.getOrderStatistics({ startDate, endDate });

      const data = response.data.data as any;

      return {
        totalOrders: data.totalOrders || 0,
        pendingPaymentOrders: data.pendingPaymentOrders || 0,
        paidOrders: data.paidOrders || 0,
        completedOrders: data.completedOrders || 0,
        cancelledOrders: data.cancelledOrders || 0,
        refundingOrders: data.refundingOrders || 0,
        refundedOrders: data.refundedOrders || 0,
        totalAmount: data.totalAmount || 0,
        completedAmount: data.completedAmount || 0,
        refundedAmount: data.refundedAmount || 0,
        averageAmount: data.averageAmount || 0,
        completionRate: data.completionRate || 0,
        cancellationRate: data.cancellationRate || 0,
        refundRate: data.refundRate || 0,
        ordersByStatus: data.ordersByStatus || {},
        amountByPaymentMethod: data.amountByPaymentMethod || {},
        countByPaymentMethod: data.countByPaymentMethod || {},
        todayNewOrders: data.todayNewOrders || 0,
        todayAmount: data.todayAmount || 0,
        todayCompletedOrders: data.todayCompletedOrders || 0,
      };
    } catch (error: any) {
      console.error('❌ 获取订单统计失败:', error.response?.data?.message || error.message);
      throw error;
    }
  }

  /**
   * 💰 获取退款统计
   * GET /api/admin/statistics/refunds
   */
  async getRefundStatistics(startDate?: string, endDate?: string): Promise<RefundStatistics> {
    try {
      const api = getApi();
      const response = await api.getRefundStatistics({ startDate, endDate });

      const data = response.data.data as any;

      return {
        totalRefunds: data.totalRefunds || 0,
        appliedRefunds: data.appliedRefunds || 0,
        approvedRefunds: data.approvedRefunds || 0,
        rejectedRefunds: data.rejectedRefunds || 0,
        processingRefunds: data.processingRefunds || 0,
        completedRefunds: data.completedRefunds || 0,
        failedRefunds: data.failedRefunds || 0,
        totalAmount: data.totalAmount || 0,
        completedAmount: data.completedAmount || 0,
        processingAmount: data.processingAmount || 0,
        averageAmount: data.averageAmount || 0,
        approvalRate: data.approvalRate || 0,
        successRate: data.successRate || 0,
        failureRate: data.failureRate || 0,
        refundsByStatus: data.refundsByStatus || {},
        amountByChannel: data.amountByChannel || {},
        countByChannel: data.countByChannel || {},
        todayNewRefunds: data.todayNewRefunds || 0,
        todayAmount: data.todayAmount || 0,
        todayCompletedRefunds: data.todayCompletedRefunds || 0,
        avgReviewTime: data.avgReviewTime || 0,
        avgCompletionTime: data.avgCompletionTime || 0,
      };
    } catch (error: any) {
      console.error('❌ 获取退款统计失败:', error.response?.data?.message || error.message);
      throw error;
    }
  }

  /**
   * 🛠️ 工具方法：转换趋势数据格式
   * @private
   */
  private convertTrendData(dates: string[], counts: number[]): TrendPoint[] {
    if (!dates || !counts) return [];
    return dates.map((date, index) => ({
      date,
      value: counts[index] || 0,
    }));
  }
}

// 导出单例实例（方便使用）
export const statisticsService = new StatisticsService();
export default statisticsService;
