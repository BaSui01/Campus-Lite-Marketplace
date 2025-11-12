/**
 * 商家数据统计服务 - 数据驱动决策！📊
 * @author BaSui 😎
 * @description 商家销售数据、访客分析、商品排行统计
 */

import { apiClient } from '@campus/shared/utils/apiClient';

// ==================== 类型定义 ====================

/**
 * 今日数据概览
 */
export interface TodayOverview {
  salesAmount: number;        // 今日销售额
  orderCount: number;          // 今日订单数
  visitorCount: number;        // 今日访客数
  newVisitorCount: number;     // 今日新访客数
  pageViewCount: number;       // 今日浏览量
  conversionRate: number;      // 转化率
  
  // 对比昨日
  salesAmountGrowth: number;   // 销售额增长率
  orderCountGrowth: number;    // 订单数增长率
  visitorCountGrowth: number;  // 访客数增长率
}

/**
 * 销售趋势数据点
 */
export interface SalesTrendPoint {
  date: string;                // 日期 (YYYY-MM-DD)
  salesAmount: number;         // 销售额
  orderCount: number;          // 订单数
  visitorCount: number;        // 访客数
  conversionRate: number;      // 转化率
}

/**
 * 销售趋势数据
 */
export interface SalesTrend {
  dates: string[];             // 日期列表
  salesAmounts: number[];      // 销售额列表
  orderCounts: number[];       // 订单数列表
  visitorCounts: number[];     // 访客数列表
  conversionRates: number[];   // 转化率列表
  totalSalesAmount: number;    // 总销售额
  totalOrderCount: number;     // 总订单数
  avgDailySales: number;       // 日均销售额
}

/**
 * 商品排行项
 */
export interface GoodsRankingItem {
  goodsId: number;
  goodsTitle: string;
  goodsCoverImage?: string;
  salesCount: number;          // 销量
  salesAmount: number;         // 销售额
  viewCount: number;           // 浏览量
  favoriteCount: number;       // 收藏数
  conversionRate: number;      // 转化率
}

/**
 * 商品排行榜
 */
export interface GoodsRanking {
  topBySales: GoodsRankingItem[];      // 按销量排行
  topByAmount: GoodsRankingItem[];     // 按销售额排行
  topByViews: GoodsRankingItem[];      // 按浏览量排行
}

/**
 * 访客来源分布
 */
export interface VisitorSource {
  source: string;              // 来源（搜索、推荐、直接访问、分享等）
  count: number;               // 访客数
  percentage: number;          // 占比
}

/**
 * 访客分析
 */
export interface VisitorAnalysis {
  totalVisitors: number;       // 总访客数
  newVisitors: number;         // 新访客数
  returningVisitors: number;   // 回访客数
  avgPageViews: number;        // 平均浏览页数
  avgStayTime: number;         // 平均停留时间（秒）
  sources: VisitorSource[];    // 来源分布
  peakHours: number[];         // 访问高峰时段（0-23小时）
}

/**
 * 数据报表类型
 */
export enum ReportType {
  DAILY = 'DAILY',       // 日报
  WEEKLY = 'WEEKLY',     // 周报
  MONTHLY = 'MONTHLY',   // 月报
}

/**
 * 数据报表
 */
export interface DataReport {
  reportId: string;
  merchantId: number;
  reportType: ReportType;
  startDate: string;
  endDate: string;
  data: {
    overview: TodayOverview;
    salesTrend: SalesTrend;
    goodsRanking: GoodsRanking;
    visitorAnalysis: VisitorAnalysis;
  };
  generatedAt: string;
}

// ==================== Service 类 ====================

/**
 * 商家数据统计服务类
 */
class SellerStatisticsService {
  /**
   * 获取今日数据概览
   */
  async getTodayOverview(merchantId?: number): Promise<TodayOverview> {
    const url = merchantId 
      ? `/merchant/dashboard/${merchantId}/today` 
      : '/merchant/dashboard/today';
    
    const response = await apiClient.get(url);
    return response.data;
  }

  /**
   * 获取销售趋势（近N天）
   * @param days 天数（默认7天）
   * @param merchantId 商家ID（可选，默认当前登录商家）
   */
  async getSalesTrend(days: number = 7, merchantId?: number): Promise<SalesTrend> {
    const url = merchantId
      ? `/merchant/dashboard/${merchantId}/sales-trend`
      : '/merchant/dashboard/sales-trend';
    
    const response = await apiClient.get(url, {
      params: { days },
    });
    return response.data;
  }

  /**
   * 获取商品排行榜
   * @param limit 每个排行榜返回的商品数量（默认10）
   * @param merchantId 商家ID（可选）
   */
  async getGoodsRanking(limit: number = 10, merchantId?: number): Promise<GoodsRanking> {
    const url = merchantId
      ? `/merchant/dashboard/${merchantId}/goods-ranking`
      : '/merchant/dashboard/goods-ranking';
    
    const response = await apiClient.get(url, {
      params: { limit },
    });
    return response.data;
  }

  /**
   * 获取访客分析
   * @param days 分析天数（默认7天）
   * @param merchantId 商家ID（可选）
   */
  async getVisitorAnalysis(days: number = 7, merchantId?: number): Promise<VisitorAnalysis> {
    const url = merchantId
      ? `/merchant/dashboard/${merchantId}/visitor-analysis`
      : '/merchant/dashboard/visitor-analysis';
    
    const response = await apiClient.get(url, {
      params: { days },
    });
    return response.data;
  }

  /**
   * 生成数据报表
   * @param reportType 报表类型
   * @param startDate 开始日期（可选，默认根据报表类型自动计算）
   * @param endDate 结束日期（可选，默认今天）
   */
  async generateReport(
    reportType: ReportType,
    startDate?: string,
    endDate?: string
  ): Promise<DataReport> {
    const response = await apiClient.post('/merchant/dashboard/report', {
      reportType,
      startDate,
      endDate,
    });
    return response.data;
  }

  /**
   * 导出数据报表
   * @param reportType 报表类型
   * @param format 导出格式（pdf/excel）
   */
  async exportReport(reportType: ReportType, format: 'pdf' | 'excel' = 'excel'): Promise<Blob> {
    const response = await apiClient.get('/merchant/dashboard/export', {
      params: { reportType, format },
      responseType: 'blob',
    });
    return response.data;
  }

  /**
   * 下载报表文件
   * @param blob 文件Blob
   * @param filename 文件名
   */
  downloadReportFile(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  /**
   * 格式化金额（保留2位小数，添加千分位）
   */
  formatAmount(amount: number | undefined): string {
    if (amount === undefined || amount === null) {
      return '¥0.00';
    }
    return `¥${amount.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;
  }

  /**
   * 格式化增长率
   */
  formatGrowthRate(rate: number | undefined): string {
    if (rate === undefined || rate === null) {
      return '0.0%';
    }
    const sign = rate >= 0 ? '+' : '';
    return `${sign}${(rate * 100).toFixed(1)}%`;
  }

  /**
   * 获取增长率颜色
   */
  getGrowthColor(rate: number): string {
    if (rate > 0) return '#52c41a'; // 绿色
    if (rate < 0) return '#f5222d'; // 红色
    return '#8c8c8c';                // 灰色
  }

  /**
   * 格式化停留时间
   */
  formatStayTime(seconds: number): string {
    if (seconds < 60) {
      return `${seconds}秒`;
    }
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return remainingSeconds > 0 
      ? `${minutes}分${remainingSeconds}秒` 
      : `${minutes}分钟`;
  }

  /**
   * 计算时间段标签
   */
  getTimeRangeLabel(days: number): string {
    if (days === 1) return '今日';
    if (days === 7) return '近7天';
    if (days === 30) return '近30天';
    return `近${days}天`;
  }
}

// ==================== 导出单例 ====================

export const sellerStatisticsService = new SellerStatisticsService();
