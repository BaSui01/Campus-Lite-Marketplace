/**
 * 仪表盘页面
 *
 * 功能：
 * - 系统概览统计（4个卡片）
 * - 趋势图表（折线图）
 * - 排行榜（热门商品、活跃用户）
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */

import React from 'react';
import { Row, Col, Typography, Card, List, Avatar, Tag } from 'antd';
import {
  UserOutlined,
  ShoppingOutlined,
  FileTextOutlined,
  DollarOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { 
  StatCard, 
  LineChart, 
  BarChart, 
  statisticsService 
} from '@campus/shared';
import './Dashboard.css';

const { Title, Text } = Typography;

const Dashboard: React.FC = () => {
  // ===== 查询系统概览 =====
  const { data: overview, isLoading: overviewLoading } = useQuery({
    queryKey: ['system-overview'],
    queryFn: () => statisticsService.getSystemOverview(),
  });

  // ===== 查询热门商品排行 =====
  const { data: topGoods, isLoading: goodsLoading } = useQuery({
    queryKey: ['top-goods'],
    queryFn: () => statisticsService.getTopGoods(10),
  });

  // ===== 查询活跃用户排行 =====
  const { data: topUsers, isLoading: usersLoading } = useQuery({
    queryKey: ['top-users'],
    queryFn: () => statisticsService.getTopUsers(10),
  });

  // ===== 查询趋势数据 =====
  const { data: trendData, isLoading: trendLoading } = useQuery({
    queryKey: ['user-trend'],
    queryFn: () => statisticsService.getUserTrend(30),
  });

  // ===== 查询收入趋势 =====
  const { data: revenueData, isLoading: revenueLoading } = useQuery({
    queryKey: ['revenue-trend'],
    queryFn: () => statisticsService.getRevenueTrend(30),
  });

  return (
    <div className="dashboard">
      <Title level={2}>📊 数据看板</Title>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]} className="stat-cards">
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="总用户数"
            value={overview?.totalUsers || 0}
            icon={<UserOutlined />}
            color="#1677ff"
            trend={12}
            trendLabel="较上月"
            loading={overviewLoading}
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="总商品数"
            value={overview?.totalGoods || 0}
            icon={<ShoppingOutlined />}
            color="#52c41a"
            trend={8}
            trendLabel="较上月"
            loading={overviewLoading}
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="总订单数"
            value={overview?.totalOrders || 0}
            icon={<FileTextOutlined />}
            color="#faad14"
            trend={-3}
            trendLabel="较上月"
            loading={overviewLoading}
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="总收入"
            value={overview?.totalRevenue || 0}
            prefix="¥"
            icon={<DollarOutlined />}
            color="#f5222d"
            trend={15}
            trendLabel="较上月"
            loading={overviewLoading}
          />
        </Col>
      </Row>

      {/* 排行榜 */}
      <Row gutter={[16, 16]} className="ranking-section">
        <Col xs={24} lg={12}>
          <Card title="🏆 热门商品 Top 10" loading={goodsLoading}>
            <List
              dataSource={topGoods || []}
              renderItem={(item, index) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={
                      <Avatar
                        style={{
                          backgroundColor: index < 3 ? '#faad14' : '#d9d9d9',
                        }}
                      >
                        {index + 1}
                      </Avatar>
                    }
                    title={item.name}
                    description={`浏览量: ${item.value}`}
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card title="👥 活跃用户 Top 10" loading={usersLoading}>
            <List
              dataSource={topUsers || []}
              renderItem={(item, index) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={
                      <Avatar
                        src={item.avatar}
                        style={{
                          backgroundColor: index < 3 ? '#52c41a' : '#d9d9d9',
                        }}
                      >
                        {!item.avatar && (index + 1)}
                      </Avatar>
                    }
                    title={item.name}
                    description={`积分: ${item.value}`}
                  />
                  {index < 3 && (
                    <Tag color={index === 0 ? 'gold' : index === 1 ? 'silver' : 'orange'}>
                      Top {index + 1}
                    </Tag>
                  )}
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>

      {/* 今日统计 */}
      <Card title="📅 今日数据" className="today-stats">
        <Row gutter={[16, 16]}>
          <Col xs={12} sm={6}>
            <div className="stat-item">
              <Text type="secondary">今日新增用户</Text>
              <div className="stat-value">{overview?.todayNewUsers || 0}</div>
            </div>
          </Col>
          <Col xs={12} sm={6}>
            <div className="stat-item">
              <Text type="secondary">今日新增商品</Text>
              <div className="stat-value">{overview?.todayNewGoods || 0}</div>
            </div>
          </Col>
          <Col xs={12} sm={6}>
            <div className="stat-item">
              <Text type="secondary">今日新增订单</Text>
              <div className="stat-value">{overview?.todayNewOrders || 0}</div>
            </div>
          </Col>
          <Col xs={12} sm={6}>
            <div className="stat-item">
              <Text type="secondary">活跃用户数</Text>
              <div className="stat-value">{overview?.activeUsers || 0}</div>
            </div>
          </Col>
        </Row>
      </Card>

      {/* 图表区域 */}
      <Row gutter={[16, 16]} className="chart-section">
        <Col xs={24} lg={12}>
          <Card title="📈 用户增长趋势" loading={trendLoading}>
            <LineChart 
              data={trendData || []}
              height={300}
              color="#52c41a"
            />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="💰 收入趋势" loading={revenueLoading}>
            <LineChart 
              data={revenueData || []}
              height={300}
              color="#faad14"
            />
          </Card>
        </Col>
      </Row>

      {/* 商品类别分布 */}
      <Row gutter={[16, 16]} className="chart-section">
        <Col xs={24}>
          <Card title="📦 商品类别分布" loading={goodsLoading}>
            <BarChart 
              data={topGoods?.map(item => ({ 
                name: item.category || '其他', 
                value: item.count || item.value || 0 
              })) || []}
              height={350}
              color="#1890ff"
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
