/**
 * 商家数据看板页面 - 数据驱动决策！📊
 * @author BaSui 😎
 * @description 销售数据可视化、访客分析、商品排行
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ReactECharts from 'echarts-for-react';
import type { EChartsOption } from 'echarts';
import { Button, Skeleton } from '@campus/shared/components';
import { 
  sellerStatisticsService, 
  ReportType,
  TodayOverview,
  SalesTrend,
  GoodsRanking,
  VisitorAnalysis
} from '../../services';
import { useNotificationStore } from '../../../store';
import './Dashboard.css';

/**
 * 商家Dashboard组件
 */
const SellerDashboard: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [todayData, setTodayData] = useState<TodayOverview | null>(null);
  const [salesTrend, setSalesTrend] = useState<SalesTrend | null>(null);
  const [goodsRanking, setGoodsRanking] = useState<GoodsRanking | null>(null);
  const [visitorAnalysis, setVisitorAnalysis] = useState<VisitorAnalysis | null>(null);
  const [loading, setLoading] = useState(true);
  const [trendDays, setTrendDays] = useState(7);
  const [exporting, setExporting] = useState(false);

  // ==================== 数据加载 ====================

  /**
   * 加载所有数据
   */
  const loadAllData = async () => {
    setLoading(true);

    try {
      // 并行加载所有数据
      const [today, trend, ranking, visitor] = await Promise.all([
        sellerStatisticsService.getTodayOverview(),
        sellerStatisticsService.getSalesTrend(trendDays),
        sellerStatisticsService.getGoodsRanking(10),
        sellerStatisticsService.getVisitorAnalysis(trendDays),
      ]);

      setTodayData(today);
      setSalesTrend(trend);
      setGoodsRanking(ranking);
      setVisitorAnalysis(visitor);
    } catch (err: any) {
      console.error('加载数据失败:', err);
      toast.error(err.response?.data?.message || '加载数据失败!😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllData();
  }, [trendDays]);

  // ==================== 事件处理 ====================

  /**
   * 导出报表
   */
  const handleExportReport = async (reportType: ReportType) => {
    setExporting(true);

    try {
      const blob = await sellerStatisticsService.exportReport(reportType, 'excel');
      const filename = `${sellerStatisticsService.getTimeRangeLabel(
        reportType === ReportType.DAILY ? 1 : reportType === ReportType.WEEKLY ? 7 : 30
      )}_数据报表_${new Date().toLocaleDateString('zh-CN')}.xlsx`;
      
      sellerStatisticsService.downloadReportFile(blob, filename);
      toast.success('报表导出成功！✅');
    } catch (err: any) {
      console.error('导出报表失败:', err);
      toast.error(err.response?.data?.message || '导出报表失败!😭');
    } finally {
      setExporting(false);
    }
  };

  // ==================== ECharts 配置 ====================

  /**
   * 销售趋势图表配置
   */
  const getSalesTrendOption = (): EChartsOption => {
    if (!salesTrend) return {};

    return {
      title: {
        text: `销售趋势（${sellerStatisticsService.getTimeRangeLabel(trendDays)}）`,
        left: 'center',
        textStyle: { fontSize: 16, fontWeight: 'bold' },
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' },
      },
      legend: {
        data: ['销售额', '订单数', '访客数'],
        bottom: 10,
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: salesTrend.dates,
        boundaryGap: false,
      },
      yAxis: [
        {
          type: 'value',
          name: '金额（元）',
          position: 'left',
        },
        {
          type: 'value',
          name: '数量',
          position: 'right',
        },
      ],
      series: [
        {
          name: '销售额',
          type: 'line',
          data: salesTrend.salesAmounts,
          yAxisIndex: 0,
          smooth: true,
          lineStyle: { width: 3 },
          itemStyle: { color: '#1890ff' },
        },
        {
          name: '订单数',
          type: 'bar',
          data: salesTrend.orderCounts,
          yAxisIndex: 1,
          itemStyle: { color: '#52c41a' },
        },
        {
          name: '访客数',
          type: 'line',
          data: salesTrend.visitorCounts,
          yAxisIndex: 1,
          smooth: true,
          lineStyle: { width: 2, type: 'dashed' },
          itemStyle: { color: '#faad14' },
        },
      ],
    };
  };

  /**
   * 访客来源饼图配置
   */
  const getVisitorSourceOption = (): EChartsOption => {
    if (!visitorAnalysis) return {};

    return {
      title: {
        text: '访客来源分布',
        left: 'center',
        textStyle: { fontSize: 16, fontWeight: 'bold' },
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)',
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'middle',
      },
      series: [
        {
          name: '访客来源',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['40%', '50%'],
          data: visitorAnalysis.sources.map(s => ({
            name: s.source,
            value: s.count,
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
          label: {
            formatter: '{b}\n{d}%',
          },
        },
      ],
    };
  };

  /**
   * 商品销量排行柱状图配置
   */
  const getGoodsRankingOption = (): EChartsOption => {
    if (!goodsRanking) return {};

    const topGoods = goodsRanking.topBySales.slice(0, 10);

    return {
      title: {
        text: '商品销量排行 Top 10',
        left: 'center',
        textStyle: { fontSize: 16, fontWeight: 'bold' },
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'value',
      },
      yAxis: {
        type: 'category',
        data: topGoods.map(g => g.goodsTitle.length > 20 ? g.goodsTitle.substring(0, 20) + '...' : g.goodsTitle),
        inverse: true,
      },
      series: [
        {
          name: '销量',
          type: 'bar',
          data: topGoods.map(g => g.salesCount),
          itemStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 1,
              y2: 0,
              colorStops: [
                { offset: 0, color: '#667eea' },
                { offset: 1, color: '#764ba2' },
              ],
            },
          },
          label: {
            show: true,
            position: 'right',
            formatter: '{c} 件',
          },
        },
      ],
    };
  };

  // ==================== 渲染 ====================

  if (loading) {
    return (
      <div className="seller-dashboard-page">
        <div className="seller-dashboard-container">
          <Skeleton type="card" count={4} animation="wave" />
        </div>
      </div>
    );
  }

  if (!todayData || !salesTrend) {
    return (
      <div className="seller-dashboard-page">
        <div className="seller-dashboard-container">
          <div className="dashboard-error">
            <div className="error-icon">⚠️</div>
            <h3 className="error-text">加载失败</h3>
            <Button type="primary" size="large" onClick={loadAllData}>
              重试
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="seller-dashboard-page">
      <div className="seller-dashboard-container">
        {/* ==================== 头部 ==================== */}
        <div className="dashboard-header">
          <div className="header-content">
            <h1 className="dashboard-header__title">📊 数据看板</h1>
            <p className="dashboard-header__subtitle">
              数据驱动决策，助力业务增长
            </p>
          </div>
          <div className="header-actions">
            <Button 
              type="default" 
              size="medium" 
              onClick={() => handleExportReport(ReportType.DAILY)}
              disabled={exporting}
            >
              {exporting ? '导出中...' : '📥 导出日报'}
            </Button>
            <Button 
              type="primary" 
              size="medium" 
              onClick={loadAllData}
            >
              🔄 刷新数据
            </Button>
          </div>
        </div>

        {/* ==================== 今日数据概览 ==================== */}
        <div className="dashboard-overview">
          <h2 className="section-title">今日数据概览</h2>
          <div className="overview-cards">
            <div className="overview-card overview-card--sales">
              <div className="card-icon">💰</div>
              <div className="card-content">
                <div className="card-label">销售额</div>
                <div className="card-value">
                  {sellerStatisticsService.formatAmount(todayData.salesAmount)}
                </div>
                <div className="card-growth" style={{ color: sellerStatisticsService.getGrowthColor(todayData.salesAmountGrowth) }}>
                  {sellerStatisticsService.formatGrowthRate(todayData.salesAmountGrowth)} 较昨日
                </div>
              </div>
            </div>

            <div className="overview-card overview-card--orders">
              <div className="card-icon">📦</div>
              <div className="card-content">
                <div className="card-label">订单数</div>
                <div className="card-value">{todayData.orderCount}</div>
                <div className="card-growth" style={{ color: sellerStatisticsService.getGrowthColor(todayData.orderCountGrowth) }}>
                  {sellerStatisticsService.formatGrowthRate(todayData.orderCountGrowth)} 较昨日
                </div>
              </div>
            </div>

            <div className="overview-card overview-card--visitors">
              <div className="card-icon">👥</div>
              <div className="card-content">
                <div className="card-label">访客数</div>
                <div className="card-value">{todayData.visitorCount}</div>
                <div className="card-growth" style={{ color: sellerStatisticsService.getGrowthColor(todayData.visitorCountGrowth) }}>
                  {sellerStatisticsService.formatGrowthRate(todayData.visitorCountGrowth)} 较昨日
                </div>
              </div>
            </div>

            <div className="overview-card overview-card--conversion">
              <div className="card-icon">📈</div>
              <div className="card-content">
                <div className="card-label">转化率</div>
                <div className="card-value">{(todayData.conversionRate * 100).toFixed(2)}%</div>
                <div className="card-tip">浏览转购买比例</div>
              </div>
            </div>
          </div>
        </div>

        {/* ==================== 销售趋势图表 ==================== */}
        <div className="dashboard-section">
          <div className="section-header">
            <h2 className="section-title">销售趋势分析</h2>
            <div className="section-actions">
              <Button 
                type={trendDays === 7 ? 'primary' : 'default'} 
                size="small" 
                onClick={() => setTrendDays(7)}
              >
                近7天
              </Button>
              <Button 
                type={trendDays === 30 ? 'primary' : 'default'} 
                size="small" 
                onClick={() => setTrendDays(30)}
              >
                近30天
              </Button>
            </div>
          </div>
          <div className="chart-container">
            <ReactECharts option={getSalesTrendOption()} style={{ height: '400px' }} />
          </div>
        </div>

        {/* ==================== 双列布局 ==================== */}
        <div className="dashboard-grid">
          {/* 访客来源分析 */}
          <div className="dashboard-section">
            <h2 className="section-title">访客来源分析</h2>
            <div className="chart-container">
              <ReactECharts option={getVisitorSourceOption()} style={{ height: '350px' }} />
            </div>
          </div>

          {/* 商品排行榜 */}
          <div className="dashboard-section">
            <h2 className="section-title">商品销量排行</h2>
            <div className="chart-container">
              <ReactECharts option={getGoodsRankingOption()} style={{ height: '350px' }} />
            </div>
          </div>
        </div>

        {/* ==================== 访客统计 ==================== */}
        {visitorAnalysis && (
          <div className="dashboard-section">
            <h2 className="section-title">访客统计详情</h2>
            <div className="visitor-stats">
              <div className="stat-item">
                <div className="stat-label">总访客数</div>
                <div className="stat-value">{visitorAnalysis.totalVisitors}</div>
              </div>
              <div className="stat-item">
                <div className="stat-label">新访客</div>
                <div className="stat-value">{visitorAnalysis.newVisitors}</div>
              </div>
              <div className="stat-item">
                <div className="stat-label">回访客</div>
                <div className="stat-value">{visitorAnalysis.returningVisitors}</div>
              </div>
              <div className="stat-item">
                <div className="stat-label">平均浏览页数</div>
                <div className="stat-value">{visitorAnalysis.avgPageViews.toFixed(1)}</div>
              </div>
              <div className="stat-item">
                <div className="stat-label">平均停留时间</div>
                <div className="stat-value">
                  {sellerStatisticsService.formatStayTime(visitorAnalysis.avgStayTime)}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default SellerDashboard;
