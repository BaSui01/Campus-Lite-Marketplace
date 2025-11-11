/**
 * 系统监控页面
 * 
 * 功能：
 * - 系统概览卡片（CPU、内存、JVM、磁盘）
 * - API监控图表（QPS趋势、慢接口TOP10）
 * - 错误日志列表
 * - 实时更新（30秒轮询）
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */

import { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Tag,
  Space,
  Button,
  Select,
  Alert,
  Spin,
  Badge,
  Tabs,
  App,
} from 'antd';
import {
  DashboardOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  WarningOutlined,
  ThunderboltOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { monitorService, ErrorSeverity } from '../../services/monitor';
import type { 
  SystemMetrics, 
  ApiPerformanceLog, 
  EndpointStats, 
  ErrorLog,
  QpsData,
} from '../../services/monitor';
import ReactECharts from 'echarts-for-react';
import type { EChartsOption } from 'echarts';

const { Option } = Select;
const { TabPane } = Tabs;

/**
 * 健康状态颜色映射
 */
const HEALTH_STATUS_MAP = {
  UP: { text: '正常', color: 'success', icon: <CheckCircleOutlined /> },
  DOWN: { text: '异常', color: 'error', icon: <CloseCircleOutlined /> },
  DEGRADED: { text: '降级', color: 'warning', icon: <WarningOutlined /> },
};

/**
 * 错误严重程度颜色映射
 */
const ERROR_SEVERITY_MAP = {
  LOW: { text: '低', color: 'default' },
  MEDIUM: { text: '中', color: 'orange' },
  HIGH: { text: '高', color: 'red' },
  CRITICAL: { text: '严重', color: 'purple' },
};

export const SystemMonitor: React.FC = () => {
  const { message, modal } = App.useApp();
  // 时间范围选择
  const [timeRange, setTimeRange] = useState<number>(24);

  // 查询系统健康检查
  const { data: healthCheck, isLoading: healthLoading, refetch: refetchHealth } = useQuery({
    queryKey: ['monitor', 'health'],
    queryFn: () => monitorService.healthCheck(),
    refetchInterval: 30000, // 30秒自动刷新
  });

  // 查询系统指标
  const { data: metrics, isLoading: metricsLoading, refetch: refetchMetrics } = useQuery({
    queryKey: ['monitor', 'metrics'],
    queryFn: () => monitorService.getMetrics(),
    refetchInterval: 30000,
  });

  // 查询QPS统计
  const { data: qpsData, isLoading: qpsLoading } = useQuery({
    queryKey: ['monitor', 'qps', timeRange],
    queryFn: () => monitorService.getQpsStatistics(1),
    refetchInterval: 30000,
  });

  // 查询慢查询日志
  const { data: slowQueries, isLoading: slowQueriesLoading } = useQuery({
    queryKey: ['monitor', 'slow-queries', timeRange],
    queryFn: () => monitorService.getSlowQueries(timeRange),
  });

  // 查询端点统计
  const { data: endpointStats, isLoading: endpointStatsLoading } = useQuery({
    queryKey: ['monitor', 'endpoint-stats', timeRange],
    queryFn: () => monitorService.getEndpointStatistics(timeRange),
  });

  // 查询错误日志
  const { data: errors, isLoading: errorsLoading } = useQuery({
    queryKey: ['monitor', 'errors', timeRange],
    queryFn: () => monitorService.getUnresolvedErrors(),
  });

  // 查询性能报表
  const { data: performanceReport } = useQuery({
    queryKey: ['monitor', 'report', timeRange],
    queryFn: () => monitorService.generatePerformanceReport(timeRange),
  });

  // 手动刷新所有数据
  const handleRefreshAll = () => {
    message.loading('正在刷新数据...', 0.5);
    refetchHealth();
    refetchMetrics();
  };

  // ==================== 图表配置 ====================

  /**
   * QPS趋势图配置
   */
  const qpsTrendOption: EChartsOption = {
    title: {
      text: 'QPS 趋势',
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985',
        },
      },
    },
    legend: {
      data: ['QPS', '平均响应时间'],
      top: 'bottom',
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: qpsData?.map((item) => new Date(item.timestamp).toLocaleTimeString()) || [],
    },
    yAxis: [
      {
        type: 'value',
        name: 'QPS',
        position: 'left',
      },
      {
        type: 'value',
        name: '响应时间 (ms)',
        position: 'right',
      },
    ],
    series: [
      {
        name: 'QPS',
        type: 'line',
        smooth: true,
        data: qpsData?.map((item) => item.qps) || [],
        areaStyle: {
          opacity: 0.3,
        },
      },
      {
        name: '平均响应时间',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: qpsData?.map((item) => item.avgResponseTime) || [],
        lineStyle: {
          color: '#faad14',
        },
      },
    ],
  };

  /**
   * 慢接口TOP10柱状图
   */
  const slowQueriesOption: EChartsOption = {
    title: {
      text: '慢接口 TOP 10',
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
    },
    xAxis: {
      type: 'value',
      name: '响应时间 (ms)',
    },
    yAxis: {
      type: 'category',
      data: endpointStats?.slice(0, 10).map((item) => `${item.method} ${item.endpoint}`) || [],
      axisLabel: {
        interval: 0,
        formatter: (value: string) => {
          return value.length > 30 ? value.substring(0, 30) + '...' : value;
        },
      },
    },
    series: [
      {
        name: '平均响应时间',
        type: 'bar',
        data: endpointStats?.slice(0, 10).map((item) => item.avgDuration) || [],
        itemStyle: {
          color: (params) => {
            const value = params.value as number;
            if (value > 1000) return '#f5222d';
            if (value > 500) return '#faad14';
            return '#52c41a';
          },
        },
      },
    ],
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
  };

  // ==================== 表格列定义 ====================

  /**
   * 慢查询日志表格列
   */
  const slowQueriesColumns = [
    {
      title: '端点',
      dataIndex: 'endpoint',
      key: 'endpoint',
      width: 300,
      ellipsis: true,
    },
    {
      title: '方法',
      dataIndex: 'method',
      key: 'method',
      width: 80,
      render: (method: string) => (
        <Tag color={method === 'GET' ? 'blue' : 'orange'}>{method}</Tag>
      ),
    },
    {
      title: '响应时间',
      dataIndex: 'duration',
      key: 'duration',
      width: 120,
      sorter: (a, b) => a.duration - b.duration,
      render: (duration: number) => {
        const color = duration > 1000 ? 'red' : duration > 500 ? 'orange' : 'green';
        return <Tag color={color}>{duration} ms</Tag>;
      },
    },
    {
      title: '状态码',
      dataIndex: 'statusCode',
      key: 'statusCode',
      width: 100,
      render: (statusCode: number) => {
        const color = statusCode >= 500 ? 'red' : statusCode >= 400 ? 'orange' : 'green';
        return <Tag color={color}>{statusCode}</Tag>;
      },
    },
    {
      title: '时间',
      dataIndex: 'timestamp',
      key: 'timestamp',
      width: 180,
      render: (timestamp: string) => new Date(timestamp).toLocaleString(),
    },
  ];

  /**
   * 错误日志表格列
   */
  const errorLogsColumns = [
    {
      title: '严重程度',
      dataIndex: 'severity',
      key: 'severity',
      width: 100,
      render: (severity: ErrorSeverity) => {
        const config = ERROR_SEVERITY_MAP[severity];
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '错误信息',
      dataIndex: 'message',
      key: 'message',
      width: 400,
      ellipsis: true,
    },
    {
      title: '端点',
      dataIndex: 'endpoint',
      key: 'endpoint',
      width: 200,
      ellipsis: true,
    },
    {
      title: '时间',
      dataIndex: 'timestamp',
      key: 'timestamp',
      width: 180,
      render: (timestamp: string) => new Date(timestamp).toLocaleString(),
    },
    {
      title: '状态',
      dataIndex: 'resolved',
      key: 'resolved',
      width: 100,
      render: (resolved: boolean) => (
        <Tag color={resolved ? 'green' : 'red'}>{resolved ? '已解决' : '未解决'}</Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_, record: ErrorLog) => (
        <Space>
          {!record.resolved && (
            <Button 
              size="small" 
              type="link"
              onClick={() => handleResolveError(record.id)}
            >
              标记已解决
            </Button>
          )}
        </Space>
      ),
    },
  ];

  // ==================== 事件处理 ====================

  /**
   * 标记错误为已解决
   */
  const handleResolveError = async (errorId: number) => {
    try {
      await monitorService.markErrorAsResolved(errorId);
      message.success('已标记为已解决');
      // 刷新错误日志
      refetchHealth();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // ==================== 渲染 ====================

  return (
    <div style={{ padding: 24 }}>
      {/* 页面头部 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2>
          <DashboardOutlined style={{ marginRight: 8 }} />
          系统监控
        </h2>
        <Space>
          <Select
            value={timeRange}
            onChange={setTimeRange}
            style={{ width: 150 }}
          >
            <Option value={1}>最近1小时</Option>
            <Option value={6}>最近6小时</Option>
            <Option value={24}>最近24小时</Option>
            <Option value={72}>最近3天</Option>
          </Select>
          <Button icon={<ReloadOutlined />} onClick={handleRefreshAll}>
            刷新
          </Button>
        </Space>
      </div>

      {/* 健康状态告警 */}
      {healthCheck && healthCheck.status !== 'UP' && (
        <Alert
          message="系统健康状态异常"
          description={`当前状态: ${HEALTH_STATUS_MAP[healthCheck.status].text}`}
          type="error"
          showIcon
          closable
          style={{ marginBottom: 16 }}
        />
      )}

      {/* 系统概览卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="CPU 使用率"
              value={metrics?.cpu.usage || 0}
              precision={2}
              suffix="%"
              valueStyle={{ 
                color: (metrics?.cpu.usage || 0) > 80 ? '#cf1322' : '#3f8600' 
              }}
              prefix={<ThunderboltOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="内存使用率"
              value={metrics?.memory.usagePercent || 0}
              precision={2}
              suffix="%"
              valueStyle={{ 
                color: (metrics?.memory.usagePercent || 0) > 80 ? '#cf1322' : '#3f8600' 
              }}
              prefix={<DashboardOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="JVM 堆内存"
              value={metrics?.jvm.heapUsagePercent || 0}
              precision={2}
              suffix="%"
              valueStyle={{ 
                color: (metrics?.jvm.heapUsagePercent || 0) > 80 ? '#cf1322' : '#3f8600' 
              }}
              prefix={<DashboardOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="线程数"
              value={metrics?.jvm.threadCount || 0}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 性能概览卡片 */}
      {performanceReport && (
        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col span={6}>
            <Card>
              <Statistic
                title="总请求数"
                value={performanceReport.summary.totalRequests}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="平均响应时间"
                value={performanceReport.summary.avgResponseTime}
                precision={2}
                suffix="ms"
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="错误率"
                value={performanceReport.summary.errorRate}
                precision={2}
                suffix="%"
                valueStyle={{ 
                  color: performanceReport.summary.errorRate > 5 ? '#cf1322' : '#3f8600' 
                }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="慢查询数"
                value={performanceReport.summary.slowQueryCount}
                valueStyle={{ 
                  color: performanceReport.summary.slowQueryCount > 100 ? '#faad14' : '#3f8600' 
                }}
              />
            </Card>
          </Col>
        </Row>
      )}

      {/* 图表区域 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card>
            {qpsLoading ? (
              <div style={{ textAlign: 'center', padding: 100 }}>
                <Spin />
              </div>
            ) : (
              <ReactECharts option={qpsTrendOption} style={{ height: 400 }} />
            )}
          </Card>
        </Col>
        <Col span={12}>
          <Card>
            {endpointStatsLoading ? (
              <div style={{ textAlign: 'center', padding: 100 }}>
                <Spin />
              </div>
            ) : (
              <ReactECharts option={slowQueriesOption} style={{ height: 400 }} />
            )}
          </Card>
        </Col>
      </Row>

      {/* 详细数据表格 */}
      <Card>
        <Tabs defaultActiveKey="slowQueries">
          <TabPane tab="慢查询日志" key="slowQueries">
            <Table
              dataSource={slowQueries || []}
              columns={slowQueriesColumns}
              rowKey="id"
              loading={slowQueriesLoading}
              pagination={{
                pageSize: 20,
                showTotal: (total) => `共 ${total} 条`,
              }}
            />
          </TabPane>
          
          <TabPane tab="错误日志" key="errors">
            <Table
              dataSource={errors || []}
              columns={errorLogsColumns}
              rowKey="id"
              loading={errorsLoading}
              pagination={{
                pageSize: 20,
                showTotal: (total) => `共 ${total} 条`,
              }}
            />
          </TabPane>
        </Tabs>
      </Card>
    </div>
  );
};
