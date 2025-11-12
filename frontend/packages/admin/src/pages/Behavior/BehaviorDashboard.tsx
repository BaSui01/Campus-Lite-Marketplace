/**
 * 用户行为分析仪表盘
 * 
 * 功能：
 * - 用户行为统计概览
 * - 行为类型分布图表
 * - 活跃用户趋势
 * - 行为日志列表
 * - 用户画像分析
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useState } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Tag,
  DatePicker,
  Space,
  Button,
} from 'antd';
import {
  EyeOutlined,
  UserOutlined,
  ShoppingOutlined,
  HeartOutlined,
  MessageOutlined,
  BarChartOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { behaviorService } from '@/services';
import type { UserBehaviorLogDTO } from '@campus/shared/api';
import ReactECharts from 'echarts-for-react';
import dayjs, { Dayjs } from 'dayjs';

const { RangePicker } = DatePicker;

/**
 * 行为类型映射
 */
const BEHAVIOR_TYPE_MAP: Record<string, { text: string; color: string; icon: React.ReactNode }> = {
  VIEW_GOODS: { text: '浏览商品', color: 'blue', icon: <EyeOutlined /> },
  SEARCH: { text: '搜索', color: 'cyan', icon: <BarChartOutlined /> },
  ADD_TO_CART: { text: '加入购物车', color: 'orange', icon: <ShoppingOutlined /> },
  FAVORITE: { text: '收藏', color: 'red', icon: <HeartOutlined /> },
  PURCHASE: { text: '购买', color: 'green', icon: <ShoppingOutlined /> },
  VIEW_POST: { text: '浏览帖子', color: 'purple', icon: <EyeOutlined /> },
  COMMENT: { text: '评论', color: 'magenta', icon: <MessageOutlined /> },
  SHARE: { text: '分享', color: 'lime', icon: <UserOutlined /> },
};

export const BehaviorDashboard: React.FC = () => {
  const [dateRange, setDateRange] = useState<[Dayjs, Dayjs]>([
    dayjs().subtract(7, 'days'),
    dayjs(),
  ]);

  // 查询行为日志列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['behavior', 'logs', dateRange],
    queryFn: () => behaviorService.getUserBehaviors(
      undefined,
      undefined,
      dateRange[0].format('YYYY-MM-DD'),
      dateRange[1].format('YYYY-MM-DD'),
      0,
      20
    ),
    staleTime: 5 * 60 * 1000,
  });

  // 查询统计数据
  const { data: statistics } = useQuery({
    queryKey: ['behavior', 'statistics', dateRange],
    queryFn: () => behaviorService.getStatistics(
      dateRange[0].format('YYYY-MM-DD'),
      dateRange[1].format('YYYY-MM-DD')
    ),
    staleTime: 5 * 60 * 1000,
  });

  // 行为类型分布图表配置
  const behaviorTypeChartOption = {
    title: {
      text: '行为类型分布',
      left: 'center',
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
    },
    series: [
      {
        name: '行为类型',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 2345, name: '浏览商品' },
          { value: 1234, name: '搜索' },
          { value: 890, name: '收藏' },
          { value: 567, name: '加入购物车' },
          { value: 345, name: '购买' },
          { value: 234, name: '评论' },
          { value: 123, name: '分享' },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  };

  // 活跃用户趋势图表配置
  const activeUserTrendOption = {
    title: {
      text: '活跃用户趋势',
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    },
    yAxis: {
      type: 'value',
    },
    series: [
      {
        name: '活跃用户数',
        data: [320, 332, 301, 334, 390, 330, 320],
        type: 'line',
        smooth: true,
        areaStyle: {
          color: 'rgba(24, 144, 255, 0.2)',
        },
        itemStyle: {
          color: '#1890ff',
        },
      },
    ],
  };

  // 表格列定义
  const columns = [
    {
      title: '用户ID',
      dataIndex: 'userId',
      key: 'userId',
      width: 100,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: 120,
    },
    {
      title: '行为类型',
      dataIndex: 'behaviorType',
      key: 'behaviorType',
      width: 150,
      render: (type: string) => {
        const config = BEHAVIOR_TYPE_MAP[type] || { text: type, color: 'default', icon: null };
        return (
          <Tag color={config.color} icon={config.icon}>
            {config.text}
          </Tag>
        );
      },
    },
    {
      title: '目标类型',
      dataIndex: 'targetType',
      key: 'targetType',
      width: 120,
    },
    {
      title: '目标ID',
      dataIndex: 'targetId',
      key: 'targetId',
      width: 100,
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 150,
      render: (ip: string) => <span style={{ fontFamily: 'monospace' }}>{ip}</span>,
    },
    {
      title: '设备类型',
      dataIndex: 'deviceType',
      key: 'deviceType',
      width: 100,
    },
    {
      title: '时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm:ss'),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card variant="outlined">
            <Statistic
              title="总用户数"
              value={statistics?.totalUsers ?? 0}
              prefix={<UserOutlined />}
              suffix="人"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card variant="outlined">
            <Statistic
              title="活跃用户"
              value={statistics?.activeUsers ?? 0}
              prefix={<UserOutlined />}
              suffix="人"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card variant="outlined">
            <Statistic
              title="总行为数"
              value={statistics?.totalBehaviors ?? 0}
              prefix={<BarChartOutlined />}
              suffix="次"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card variant="outlined">
            <Statistic
              title="人均行为数"
              value={statistics?.avgBehaviorsPerUser ?? 0}
              precision={1}
              prefix={<BarChartOutlined />}
              suffix="次"
            />
          </Card>
        </Col>
      </Row>

      {/* 图表区域 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card variant="outlined">
            <ReactECharts option={behaviorTypeChartOption} style={{ height: 400 }} />
          </Card>
        </Col>
        <Col span={12}>
          <Card variant="outlined">
            <ReactECharts option={activeUserTrendOption} style={{ height: 400 }} />
          </Card>
        </Col>
      </Row>

      {/* 筛选区域 */}
      <Card variant="outlined" style={{ marginBottom: 24 }}>
        <Space>
          <RangePicker
            value={dateRange}
            onChange={(dates) => dates && setDateRange(dates as [Dayjs, Dayjs])}
            format="YYYY-MM-DD"
            placeholder={['开始日期', '结束日期']}
          />
          <Button type="primary" icon={<ReloadOutlined />} onClick={() => refetch()}>
            刷新
          </Button>
        </Space>
      </Card>

      {/* 行为日志列表 */}
      <Card variant="outlined" title="行为日志">
        <Table
          columns={columns}
          dataSource={data || []}
          loading={isLoading}
          rowKey="id"
          scroll={{ x: 1200 }}
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
          locale={{
            emptyText: (
              <div style={{ padding: '40px', color: '#8c8c8c' }}>
                <BarChartOutlined style={{ fontSize: 48, marginBottom: 16 }} />
                <div>暂无行为数据</div>
              </div>
            ),
          }}
        />
      </Card>
    </div>
  );
};

export default BehaviorDashboard;
