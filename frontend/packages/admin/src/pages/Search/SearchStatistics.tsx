/**
 * 搜索统计分析页
 * 
 * 功能：
 * - 搜索统计概览
 * - 热门搜索词排行
 * - 搜索趋势分析
 * - 搜索无结果关键词
 * - 搜索转化率分析
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
  SearchOutlined,
  FireOutlined,
  RiseOutlined,
  FallOutlined,
  BarChartOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { getApi } from '@campus/shared/utils/apiClient';
import ReactECharts from 'echarts-for-react';
import dayjs, { Dayjs } from 'dayjs';

const { RangePicker } = DatePicker;

export const SearchStatistics: React.FC = () => {
  const [dateRange, setDateRange] = useState<[Dayjs, Dayjs]>([
    dayjs().subtract(7, 'days'),
    dayjs(),
  ]);

  // 查询搜索统计
  const { data: statistics, isLoading, refetch } = useQuery({
    queryKey: ['search', 'statistics', dateRange],
    queryFn: async () => {
      const api = getApi();
      const response = await api.getMessageSearchStatistics(
        dateRange[0].format('YYYY-MM-DD'),
        dateRange[1].format('YYYY-MM-DD')
      );
      return response.data.data;
    },
    staleTime: 5 * 60 * 1000,
  });

  // 查询热门关键词
  const { data: popularKeywords } = useQuery({
    queryKey: ['search', 'popular', dateRange],
    queryFn: async () => {
      const api = getApi();
      const response = await api.getPopularKeywords(10);
      return response.data.data || [];
    },
    staleTime: 5 * 60 * 1000,
  });

  // 搜索趋势图表配置
  const searchTrendOption = {
    title: {
      text: '搜索趋势',
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
        name: '搜索次数',
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

  // 热门关键词表格列
  const keywordColumns = [
    {
      title: '排名',
      key: 'rank',
      width: 80,
      render: (_: unknown, __: unknown, index: number) => (
        <Tag color={index < 3 ? 'red' : 'default'}>
          {index + 1}
        </Tag>
      ),
    },
    {
      title: '关键词',
      dataIndex: 'keyword',
      key: 'keyword',
      width: 200,
      render: (keyword: string) => (
        <span style={{ fontWeight: 'bold' }}>{keyword}</span>
      ),
    },
    {
      title: '搜索次数',
      dataIndex: 'count',
      key: 'count',
      width: 120,
      render: (count: number) => (
        <span style={{ color: '#1890ff', fontWeight: 'bold' }}>
          {count?.toLocaleString()}
        </span>
      ),
    },
    {
      title: '结果数',
      dataIndex: 'resultCount',
      key: 'resultCount',
      width: 120,
      render: (count: number) => (
        <Tag color={count > 0 ? 'green' : 'red'}>
          {count?.toLocaleString()}
        </Tag>
      ),
    },
    {
      title: '点击率',
      dataIndex: 'clickRate',
      key: 'clickRate',
      width: 120,
      render: (rate: number) => (
        <span style={{ color: rate > 0.5 ? '#3f8600' : '#faad14' }}>
          {((rate || 0) * 100).toFixed(2)}%
        </span>
      ),
    },
    {
      title: '转化率',
      dataIndex: 'conversionRate',
      key: 'conversionRate',
      width: 120,
      render: (rate: number) => (
        <span style={{ color: rate > 0.1 ? '#3f8600' : '#faad14' }}>
          {((rate || 0) * 100).toFixed(2)}%
        </span>
      ),
    },
    {
      title: '趋势',
      dataIndex: 'trend',
      key: 'trend',
      width: 100,
      render: (trend: number) => {
        if (trend > 0) {
          return <Tag color="red" icon={<RiseOutlined />}>上升</Tag>;
        } else if (trend < 0) {
          return <Tag color="green" icon={<FallOutlined />}>下降</Tag>;
        }
        return <Tag color="default">持平</Tag>;
      },
    },
  ];

  // 模拟热门关键词数据
  const mockKeywords = [
    { keyword: 'iPhone 15', count: 1234, resultCount: 89, clickRate: 0.65, conversionRate: 0.12, trend: 1 },
    { keyword: '笔记本电脑', count: 987, resultCount: 156, clickRate: 0.58, conversionRate: 0.09, trend: 1 },
    { keyword: '运动鞋', count: 856, resultCount: 234, clickRate: 0.52, conversionRate: 0.08, trend: -1 },
    { keyword: '连衣裙', count: 745, resultCount: 178, clickRate: 0.48, conversionRate: 0.07, trend: 0 },
    { keyword: '二手教材', count: 623, resultCount: 98, clickRate: 0.45, conversionRate: 0.15, trend: 1 },
  ];

  return (
    <div style={{ padding: '24px' }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总搜索次数"
              value={statistics?.totalSearches || 0}
              prefix={<SearchOutlined />}
              suffix="次"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="独立搜索用户"
              value={statistics?.uniqueUsers || 0}
              prefix={<SearchOutlined />}
              suffix="人"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="平均结果数"
              value={statistics?.avgResults || 0}
              precision={1}
              prefix={<BarChartOutlined />}
              suffix="个"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="无结果搜索"
              value={statistics?.zeroResultSearches || 0}
              prefix={<SearchOutlined />}
              suffix="次"
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="热门关键词数"
              value={statistics?.popularKeywords || 0}
              prefix={<FireOutlined />}
              suffix="个"
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="平均点击率"
              value={(statistics?.avgClickRate || 0) * 100}
              precision={2}
              suffix="%"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="平均转化率"
              value={(statistics?.avgConversionRate || 0) * 100}
              precision={2}
              suffix="%"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 搜索趋势图 */}
      <Card style={{ marginBottom: 24 }}>
        <ReactECharts option={searchTrendOption} style={{ height: 400 }} />
      </Card>

      {/* 筛选区域 */}
      <Card style={{ marginBottom: 24 }}>
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

      {/* 热门关键词列表 */}
      <Card title={<><FireOutlined /> 热门搜索词</>}>
        <Table
          columns={keywordColumns}
          dataSource={mockKeywords}
          loading={isLoading}
          rowKey="keyword"
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 个关键词`,
          }}
        />
      </Card>
    </div>
  );
};

export default SearchStatistics;
