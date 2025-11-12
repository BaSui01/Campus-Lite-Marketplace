/**
 * 📊 统计数据 Dashboard - BaSui 搞笑专业版 😎
 *
 * 功能：
 * - 系统概览统计卡片
 * - 趋势折线图（用户、物品、订单）
 * - 收入柱状图
 * - 热门商品排行榜
 * - 活跃用户排行榜
 * - 分类统计饼图
 * - 今日数据快速查看
 *
 * @author BaSui
 * @date 2025-11-07
 */

import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Statistic, Select, Button, Spin, App, DatePicker } from 'antd';
import {
  UserOutlined,
  ShoppingOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  ReloadOutlined,
  RiseOutlined,
  FallOutlined,
} from '@ant-design/icons';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { statisticsService } from '@/services';
import type {
  SystemOverview,
  TrendStatistics,
  RankingItem,
  CategoryStat,
  OrderStatistics,
  RefundStatistics,
} from '../../services/statistics';
import { paymentService, type PaymentStatistics } from '@campus/shared';
import TrendChart from './components/TrendChart';
import RevenueChart from './components/RevenueChart';
import RankingList from './components/RankingList';
import CategoryChart from './components/CategoryChart';

const { RangePicker } = DatePicker;

/**
 * 统计 Dashboard 主页面
 */
const StatisticsDashboard: React.FC = () => {
  // ========== Ant Design 静态方法实例 ==========
  const { message } = App.useApp();

  // ========== 状态管理 ==========
  const [loading, setLoading] = useState(false);
  const [overviewData, setOverviewData] = useState<SystemOverview | null>(null);
  const [trendData, setTrendData] = useState<TrendStatistics | null>(null);
  const [revenueData, setRevenueData] = useState<{ name: string; value: number }[]>([]);
  const [topGoods, setTopGoods] = useState<RankingItem[]>([]);
  const [topUsers, setTopUsers] = useState<RankingItem[]>([]);
  const [categoryStats, setCategoryStats] = useState<CategoryStat[]>([]);
  
  // P2 新增：支付/订单/退款统计
  const [paymentStats, setPaymentStats] = useState<PaymentStatistics | null>(null);
  const [orderStats, setOrderStats] = useState<OrderStatistics | null>(null);
  const [refundStats, setRefundStats] = useState<RefundStatistics | null>(null);

  // 筛选条件
  const [trendDays, setTrendDays] = useState(7); // 趋势天数
  const [revenueMonths, setRevenueMonths] = useState(12); // 收入月数

  // ========== 数据加载 ==========

  /**
   * 加载所有统计数据
   */
  const loadAllStatistics = async () => {
    setLoading(true);
    try {
      // 并行加载所有数据（包括P2新增的统计）
      const [overview, trend, revenue, goods, users, categories, payment] = await Promise.all([
        statisticsService.getSystemOverview(),
        statisticsService.getTrendStatistics(trendDays),
        statisticsService.getRevenueTrend(revenueMonths),
        statisticsService.getTopGoods(10),
        statisticsService.getTopUsers(10),
        statisticsService.getCategoryStatistics(),
        paymentService.getPaymentStatistics(),
      ]);

      setOverviewData(overview);
      setTrendData(trend);
      setRevenueData(revenue);
      setTopGoods(goods);
      setTopUsers(users);
      setCategoryStats(categories);
      setPaymentStats(payment);
      
      // TODO: 订单和退款统计暂时使用模拟数据，等待后端接口
      setOrderStats({
        totalOrders: overview.totalOrders,
        completedOrders: 0,
        completionRate: 0,
        todayNewOrders: overview.todayNewOrders,
      });
      setRefundStats({
        totalRefunds: 0,
        completedRefunds: 0,
        approvalRate: 0,
        averageProcessTime: 0,
      });

      message.success('数据加载成功！');
    } catch (error: any) {
      console.error('❌ 加载统计数据失败:', error);
      message.error(error.message || '加载统计数据失败');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 刷新数据
   */
  const handleRefresh = () => {
    loadAllStatistics();
  };

  /**
   * 趋势天数改变
   */
  const handleTrendDaysChange = (days: number) => {
    setTrendDays(days);
  };

  /**
   * 收入月数改变
   */
  const handleRevenueMonthsChange = (months: number) => {
    setRevenueMonths(months);
  };

  // 初始加载
  useEffect(() => {
    loadAllStatistics();
  }, []);

  // 趋势天数改变时重新加载趋势数据
  useEffect(() => {
    if (trendDays) {
      statisticsService.getTrendStatistics(trendDays).then(setTrendData);
    }
  }, [trendDays]);

  // 收入月数改变时重新加载收入数据
  useEffect(() => {
    if (revenueMonths) {
      statisticsService.getRevenueTrend(revenueMonths).then(setRevenueData);
    }
  }, [revenueMonths]);

  // ========== 渲染 ==========

  return (
    <div style={{ padding: '24px', background: '#f0f2f5', minHeight: '100vh' }}>
      {/* 页面标题和操作栏 */}
      <Row justify="space-between" align="middle" style={{ marginBottom: 24 }}>
        <Col>
          <h1 style={{ margin: 0, fontSize: 24, fontWeight: 600 }}>📊 数据统计中心</h1>
          <p style={{ margin: '8px 0 0', color: '#666' }}>
            实时查看系统运营数据，洞察业务趋势 😎
          </p>
        </Col>
        <Col>
          <Button
            type="primary"
            icon={<ReloadOutlined />}
            onClick={handleRefresh}
            loading={loading}
          >
            刷新数据
          </Button>
        </Col>
      </Row>

      <Spin spinning={loading}>
        {/* 系统概览卡片 */}
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="总用户数"
                value={overviewData?.totalUsers || 0}
                prefix={<UserOutlined />}
                valueStyle={{ color: '#3f8600' }}
                suffix={
                  <span style={{ fontSize: 14, color: '#666' }}>
                    今日 +{overviewData?.todayNewUsers || 0}
                  </span>
                }
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="总物品数"
                value={overviewData?.totalGoods || 0}
                prefix={<ShoppingOutlined />}
                valueStyle={{ color: '#1890ff' }}
                suffix={
                  <span style={{ fontSize: 14, color: '#666' }}>
                    今日 +{overviewData?.todayNewGoods || 0}
                  </span>
                }
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="总订单数"
                value={overviewData?.totalOrders || 0}
                prefix={<ShoppingCartOutlined />}
                valueStyle={{ color: '#cf1322' }}
                suffix={
                  <span style={{ fontSize: 14, color: '#666' }}>
                    今日 +{overviewData?.todayNewOrders || 0}
                  </span>
                }
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card>
              <Statistic
                title="总收入"
                value={overviewData?.totalRevenue || 0}
                prefix={<DollarOutlined />}
                precision={2}
                valueStyle={{ color: '#fa8c16' }}
              />
            </Card>
          </Col>
        </Row>

        {/* P2 新增：支付/订单/退款统计卡片 */}
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} sm={12} lg={8}>
            <Card 
              title="💰 支付统计" 
              bordered={false}
              style={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}
            >
              <Row gutter={16}>
                <Col span={12}>
                  <Statistic
                    title="总金额"
                    value={(paymentStats?.totalAmount || 0) / 100}
                    precision={2}
                    prefix="¥"
                    valueStyle={{ color: '#3f8600', fontSize: 20 }}
                  />
                </Col>
                <Col span={12}>
                  <Statistic
                    title="总笔数"
                    value={paymentStats?.totalCount || 0}
                    valueStyle={{ fontSize: 20 }}
                  />
                </Col>
              </Row>
              <Row gutter={16} style={{ marginTop: 16 }}>
                <Col span={12}>
                  <div style={{ fontSize: 14, color: '#666' }}>成功率</div>
                  <div style={{ fontSize: 20, color: '#1890ff', fontWeight: 600 }}>
                    {paymentStats?.totalCount 
                      ? ((paymentStats.successCount / paymentStats.totalCount) * 100).toFixed(1)
                      : '0.0'
                    }%
                  </div>
                </Col>
                <Col span={12}>
                  <div style={{ fontSize: 14, color: '#666' }}>退款笔数</div>
                  <div style={{ fontSize: 20, color: '#ff4d4f', fontWeight: 600 }}>
                    {paymentStats?.refundCount || 0}
                  </div>
                </Col>
              </Row>
            </Card>
          </Col>

          <Col xs={24} sm={12} lg={8}>
            <Card 
              title="📦 订单统计" 
              bordered={false}
              style={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}
            >
              <Row gutter={16}>
                <Col span={12}>
                  <Statistic
                    title="总订单"
                    value={orderStats?.totalOrders || 0}
                    valueStyle={{ fontSize: 20 }}
                  />
                </Col>
                <Col span={12}>
                  <Statistic
                    title="已完成"
                    value={orderStats?.completedOrders || 0}
                    valueStyle={{ color: '#3f8600', fontSize: 20 }}
                  />
                </Col>
              </Row>
              <Row gutter={16} style={{ marginTop: 16 }}>
                <Col span={12}>
                  <div style={{ fontSize: 14, color: '#666' }}>完成率</div>
                  <div style={{ fontSize: 20, color: '#52c41a', fontWeight: 600 }}>
                    {(orderStats?.completionRate || 0).toFixed(1)}%
                  </div>
                </Col>
                <Col span={12}>
                  <div style={{ fontSize: 14, color: '#666' }}>今日新增</div>
                  <div style={{ fontSize: 20, color: '#1890ff', fontWeight: 600 }}>
                    {orderStats?.todayNewOrders || 0}
                  </div>
                </Col>
              </Row>
            </Card>
          </Col>

          <Col xs={24} sm={12} lg={8}>
            <Card 
              title="💸 退款统计" 
              bordered={false}
              style={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}
            >
              <Row gutter={16}>
                <Col span={12}>
                  <Statistic
                    title="总退款"
                    value={refundStats?.totalRefunds || 0}
                    valueStyle={{ fontSize: 20 }}
                  />
                </Col>
                <Col span={12}>
                  <Statistic
                    title="已完成"
                    value={refundStats?.completedRefunds || 0}
                    valueStyle={{ color: '#3f8600', fontSize: 20 }}
                  />
                </Col>
              </Row>
              <Row gutter={16} style={{ marginTop: 16 }}>
                <Col span={12}>
                  <div style={{ fontSize: 14, color: '#666' }}>通过率</div>
                  <div style={{ fontSize: 20, color: '#52c41a', fontWeight: 600 }}>
                    {(refundStats?.approvalRate || 0).toFixed(1)}%
                  </div>
                </Col>
                <Col span={12}>
                  <div style={{ fontSize: 14, color: '#666' }}>平均时长</div>
                  <div style={{ fontSize: 20, color: '#faad14', fontWeight: 600 }}>
                    {(refundStats?.avgCompletionTime || 0).toFixed(1)}h
                  </div>
                </Col>
              </Row>
            </Card>
          </Col>
        </Row>

        {/* 趋势图表 */}
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} lg={16}>
            <Card
              title="📈 数据趋势"
              extra={
                <Select
                  value={trendDays}
                  onChange={handleTrendDaysChange}
                  style={{ width: 120 }}
                  options={[
                    { label: '最近7天', value: 7 },
                    { label: '最近15天', value: 15 },
                    { label: '最近30天', value: 30 },
                    { label: '最近90天', value: 90 },
                  ]}
                />
              }
            >
              <TrendChart data={trendData} />
            </Card>
          </Col>
          <Col xs={24} lg={8}>
            <Card
              title="💰 收入趋势"
              extra={
                <Select
                  value={revenueMonths}
                  onChange={handleRevenueMonthsChange}
                  style={{ width: 120 }}
                  options={[
                    { label: '最近3个月', value: 3 },
                    { label: '最近6个月', value: 6 },
                    { label: '最近12个月', value: 12 },
                  ]}
                />
              }
            >
              <RevenueChart data={revenueData} />
            </Card>
          </Col>
        </Row>

        {/* 排行榜 */}
        <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
          <Col xs={24} lg={12}>
            <Card title="🏆 热门商品 Top 10">
              <RankingList data={topGoods} type="goods" />
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="👥 活跃用户 Top 10">
              <RankingList data={topUsers} type="users" />
            </Card>
          </Col>
        </Row>

        {/* 分类统计 */}
        <Row gutter={[16, 16]}>
          <Col xs={24} lg={12}>
            <Card title="📂 分类统计">
              <CategoryChart data={categoryStats} />
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="📅 今日数据快报">
              <Row gutter={[16, 16]}>
                <Col span={12}>
                  <Statistic
                    title="新增用户"
                    value={overviewData?.todayNewUsers || 0}
                    prefix={<RiseOutlined />}
                    valueStyle={{ color: '#3f8600' }}
                  />
                </Col>
                <Col span={12}>
                  <Statistic
                    title="新增物品"
                    value={overviewData?.todayNewGoods || 0}
                    prefix={<RiseOutlined />}
                    valueStyle={{ color: '#1890ff' }}
                  />
                </Col>
                <Col span={12}>
                  <Statistic
                    title="新增订单"
                    value={overviewData?.todayNewOrders || 0}
                    prefix={<RiseOutlined />}
                    valueStyle={{ color: '#cf1322' }}
                  />
                </Col>
                <Col span={12}>
                  <Statistic
                    title="活跃用户"
                    value={overviewData?.activeUsers || 0}
                    prefix={<UserOutlined />}
                    valueStyle={{ color: '#722ed1' }}
                  />
                </Col>
                <Col span={12}>
                  <Statistic
                    title="待审核物品"
                    value={overviewData?.pendingGoods || 0}
                    prefix={<ShoppingOutlined />}
                    valueStyle={{ color: '#fa8c16' }}
                  />
                </Col>
              </Row>
            </Card>
          </Col>
        </Row>
      </Spin>
    </div>
  );
};

export default StatisticsDashboard;
