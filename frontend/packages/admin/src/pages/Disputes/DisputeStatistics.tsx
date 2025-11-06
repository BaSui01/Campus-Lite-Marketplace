/**
 * 纠纷统计分析页面
 * 
 * 功能：
 * - 纠纷概览统计
 * - 趋势图表
 * - 分类分布饼图
 * - 仲裁员排行榜
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */

import { useState } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Select,
  Space,
  Button,
  Spin,
} from 'antd';
import {
  SafetyCertificateOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { disputeStatisticsService } from '@campus/shared/services/disputeStatistics';
import type { DisputeStatistics } from '@campus/shared/services/disputeStatistics';
import ReactECharts from 'echarts-for-react';
import type { EChartsOption } from 'echarts';

const { Option } = Select;

export const DisputeStatistics: React.FC = () => {
  // 时间范围选择（暂未使用，预留扩展）
  const [timeRange, setTimeRange] = useState<number>(30);

  // 查询统计数据
  const { data: stats, isLoading, refetch } = useQuery({
    queryKey: ['disputes', 'statistics'],
    queryFn: () => disputeStatisticsService.getStatistics(),
    refetchInterval: 60000, // 60秒自动刷新
  });

  // ==================== 图表配置 ====================

  /**
   * 纠纷趋势图
   */
  const trendOption: EChartsOption = {
    title: {
      text: '纠纷数量趋势',
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
      },
    },
    legend: {
      data: ['新增纠纷', '已解决', '已关闭'],
      top: 'bottom',
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: stats?.trendData?.map((item) => item.date) || [],
    },
    yAxis: {
      type: 'value',
      name: '数量',
    },
    series: [
      {
        name: '新增纠纷',
        type: 'line',
        smooth: true,
        data: stats?.trendData?.map((item) => item.newDisputes) || [],
        areaStyle: {
          opacity: 0.3,
        },
        lineStyle: {
          color: '#faad14',
        },
      },
      {
        name: '已解决',
        type: 'line',
        smooth: true,
        data: stats?.trendData?.map((item) => item.resolvedDisputes) || [],
        lineStyle: {
          color: '#52c41a',
        },
      },
      {
        name: '已关闭',
        type: 'line',
        smooth: true,
        data: stats?.trendData?.map((item) => item.closedDisputes) || [],
        lineStyle: {
          color: '#8c8c8c',
        },
      },
    ],
  };

  /**
   * 分类分布饼图
   */
  const categoryOption: EChartsOption = {
    title: {
      text: '纠纷类型分布',
      left: 'center',
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
    },
    series: [
      {
        name: '纠纷类型',
        type: 'pie',
        radius: '60%',
        data: stats?.categoryDistribution?.map((item) => ({
          value: item.count,
          name: item.category,
        })) || [],
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

  // ==================== 表格列定义 ====================

  /**
   * 仲裁员排行榜列
   */
  const arbitratorColumns = [
    {
      title: '排名',
      key: 'rank',
      width: 80,
      render: (_: unknown, __: unknown, index: number) => index + 1,
    },
    {
      title: '仲裁员',
      dataIndex: 'arbitratorName',
      key: 'arbitratorName',
      width: 150,
      render: (name: string) => (
        <span>
          <UserOutlined style={{ marginRight: 4 }} />
          {name}
        </span>
      ),
    },
    {
      title: '处理数量',
      dataIndex: 'handledCount',
      key: 'handledCount',
      width: 120,
      sorter: (a: any, b: any) => a.handledCount - b.handledCount,
    },
    {
      title: '成功解决',
      dataIndex: 'resolvedCount',
      key: 'resolvedCount',
      width: 120,
      sorter: (a: any, b: any) => a.resolvedCount - b.resolvedCount,
    },
    {
      title: '解决率',
      dataIndex: 'resolutionRate',
      key: 'resolutionRate',
      width: 120,
      sorter: (a: any, b: any) => a.resolutionRate - b.resolutionRate,
      render: (rate: number) => `${rate.toFixed(2)}%`,
    },
    {
      title: '平均处理时长',
      dataIndex: 'avgProcessingTimeHours',
      key: 'avgProcessingTimeHours',
      width: 150,
      sorter: (a: any, b: any) => a.avgProcessingTimeHours - b.avgProcessingTimeHours,
      render: (hours: number) => `${hours.toFixed(1)} 小时`,
    },
  ];

  // ==================== 渲染 ====================

  if (isLoading) {
    return (
      <div style={{ padding: 24, textAlign: 'center' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div style={{ padding: 24 }}>
      {/* 页面头部 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2>
          <SafetyCertificateOutlined style={{ marginRight: 8 }} />
          纠纷统计分析
        </h2>
        <Space>
          <Select
            value={timeRange}
            onChange={setTimeRange}
            style={{ width: 150 }}
          >
            <Option value={7}>最近7天</Option>
            <Option value={30}>最近30天</Option>
            <Option value={90}>最近90天</Option>
          </Select>
          <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
            刷新
          </Button>
        </Space>
      </div>

      {/* 统计概览卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="纠纷总数"
              value={stats?.totalDisputes || 0}
              prefix={<SafetyCertificateOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="处理中"
              value={stats?.processingDisputes || 0}
              valueStyle={{ color: '#faad14' }}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已解决"
              value={stats?.resolvedDisputes || 0}
              valueStyle={{ color: '#52c41a' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已关闭"
              value={stats?.closedDisputes || 0}
              valueStyle={{ color: '#8c8c8c' }}
              prefix={<CloseCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 解决率和平均处理时长 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card>
            <Statistic
              title="解决率"
              value={stats?.resolutionRate || 0}
              precision={2}
              suffix="%"
              valueStyle={{ 
                color: (stats?.resolutionRate || 0) >= 80 ? '#52c41a' : '#faad14' 
              }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={12}>
          <Card>
            <Statistic
              title="平均处理时长"
              value={stats?.avgProcessingTimeHours || 0}
              precision={1}
              suffix="小时"
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 图表区域 */}
      {stats?.trendData && stats.trendData.length > 0 && (
        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col span={14}>
            <Card>
              <ReactECharts option={trendOption} style={{ height: 400 }} />
            </Card>
          </Col>
          <Col span={10}>
            <Card>
              {stats?.categoryDistribution && stats.categoryDistribution.length > 0 ? (
                <ReactECharts option={categoryOption} style={{ height: 400 }} />
              ) : (
                <div style={{ textAlign: 'center', padding: 100, color: '#8c8c8c' }}>
                  暂无分类数据
                </div>
              )}
            </Card>
          </Col>
        </Row>
      )}

      {/* 仲裁员排行榜 */}
      {stats?.arbitratorStats && stats.arbitratorStats.length > 0 && (
        <Card title="🏆 仲裁员排行榜">
          <Table
            dataSource={stats.arbitratorStats}
            columns={arbitratorColumns}
            rowKey="arbitratorId"
            pagination={{
              pageSize: 10,
              showTotal: (total) => `共 ${total} 位仲裁员`,
            }}
          />
        </Card>
      )}
    </div>
  );
};
