/**
 * 物流管理列表页
 * 
 * 功能：
 * - 分页查询物流信息
 * - 搜索物流（订单号、快递单号）
 * - 物流状态筛选
 * - 物流统计卡片
 * - 查看物流轨迹
 * - 物流异常标记
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Table,
  Button,
  Space,
  Tag,
  Card,
  Row,
  Col,
  Statistic,
  message,
  Timeline,
  App,
} from 'antd';
import {
  EyeOutlined,
  CarOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  EnvironmentOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { logisticsService } from '@campus/shared';
import type { Logistics, LogisticsTrack } from '@campus/shared';
import { FilterPanel } from '@campus/shared/components';
import type { FilterConfig, FilterValues } from '@campus/shared/types/filter';
import dayjs from 'dayjs';

/**
 * 物流状态映射
 */
const STATUS_MAP: Record<string, { text: string; color: string; icon: React.ReactNode }> = {
  PENDING: { 
    text: '待揽件', 
    color: 'default',
    icon: <SyncOutlined />
  },
  IN_TRANSIT: { 
    text: '运输中', 
    color: 'blue',
    icon: <CarOutlined />
  },
  OUT_FOR_DELIVERY: { 
    text: '派送中', 
    color: 'cyan',
    icon: <EnvironmentOutlined />
  },
  DELIVERED: { 
    text: '已签收', 
    color: 'green',
    icon: <CheckCircleOutlined />
  },
  EXCEPTION: { 
    text: '异常', 
    color: 'red',
    icon: <CloseCircleOutlined />
  },
};

// 物流筛选配置
const logisticsFilters: FilterConfig[] = [
  {
    type: 'input',
    field: 'keyword',
    label: '关键词',
    placeholder: '搜索订单ID/快递单号',
    width: 250,
  },
  {
    type: 'select',
    field: 'status',
    label: '物流状态',
    placeholder: '选择物流状态',
    options: [
      { label: '待揽件', value: 'PENDING' },
      { label: '运输中', value: 'IN_TRANSIT' },
      { label: '派送中', value: 'OUT_FOR_DELIVERY' },
      { label: '已签收', value: 'DELIVERED' },
      { label: '异常', value: 'EXCEPTION' },
    ],
    width: 150,
  },
];

export const LogisticsList: React.FC = () => {
  const navigate = useNavigate();
  const { modal } = App.useApp();

  // 筛选参数（使用 FilterPanel 统一管理）
  const [filterValues, setFilterValues] = useState<FilterValues>({});
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  // 查询物流统计
  const { data: statistics } = useQuery({
    queryKey: ['logistics', 'statistics'],
    queryFn: () => logisticsService.getLogisticsStatistics(),
    staleTime: 5 * 60 * 1000,
  });

  // 查看物流轨迹
  const handleViewTrack = async (orderId: number) => {
    try {
      const logistics = await logisticsService.getOrderLogistics(orderId);
      
      modal.info({
        title: `物流轨迹 - ${logistics.expressName} (${logistics.trackingNumber})`,
        width: 600,
        content: (
          <Timeline
            style={{ marginTop: 16 }}
            items={logistics.tracks.map((track: LogisticsTrack) => ({
              color: track.status === 'DELIVERED' ? 'green' : 'blue',
              children: (
                <>
                  <p style={{ margin: 0 }}>{track.description}</p>
                  {track.location && (
                    <p style={{ margin: 0, color: '#8c8c8c', fontSize: 12 }}>
                      <EnvironmentOutlined /> {track.location}
                    </p>
                  )}
                  <p style={{ margin: 0, color: '#8c8c8c', fontSize: 12 }}>
                    {dayjs(track.time).format('YYYY-MM-DD HH:mm:ss')}
                  </p>
                </>
              ),
            }))}
          />
        ),
      });
    } catch (error) {
      message.error('获取物流轨迹失败');
    }
  };

  // ✅ 后端物流API已完全实现，前端Service已完成集成（2025-11-10）
  // 现有API：
  //   - GET /api/logistics/order/{orderId} - 根据订单ID查询物流
  //   - GET /api/logistics/tracking/{trackingNumber} - 根据快递单号查询物流
  //   - GET /api/logistics/statistics - 物流统计（已使用✅）
  //   - GET /api/admin/logistics - 管理员物流列表（已集成✅）

  // 查询物流列表（调用真实API）✅
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['logistics', 'admin', 'list', filterValues, page, size],
    queryFn: () => logisticsService.listLogistics({
      keyword: filterValues.keyword,
      status: filterValues.status,
      page,
      size,
      sortBy: 'createdAt',
      sortDirection: 'DESC',
    }),
    staleTime: 5 * 60 * 1000, // 缓存5分钟
  });

  // 搜索处理
  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  // 表格列定义
  const columns = [
    {
      title: '订单ID',
      dataIndex: 'orderId',
      key: 'orderId',
      width: 100,
    },
    {
      title: '快递公司',
      dataIndex: 'expressName',
      key: 'expressName',
      width: 120,
    },
    {
      title: '快递单号',
      dataIndex: 'trackingNumber',
      key: 'trackingNumber',
      width: 200,
      render: (trackingNumber: string) => (
        <span style={{ fontFamily: 'monospace' }}>{trackingNumber}</span>
      ),
    },
    {
      title: '物流状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (statusValue: string) => {
        const config = STATUS_MAP[statusValue] || { text: statusValue, color: 'default', icon: null };
        return (
          <Tag color={config.color} icon={config.icon}>
            {config.text}
          </Tag>
        );
      },
    },
    {
      title: '发货时间',
      dataIndex: 'shippedAt',
      key: 'shippedAt',
      width: 180,
      render: (shippedAt: string) => shippedAt ? dayjs(shippedAt).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '签收时间',
      dataIndex: 'deliveredAt',
      key: 'deliveredAt',
      width: 180,
      render: (deliveredAt: string) => deliveredAt ? dayjs(deliveredAt).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 180,
      render: (updatedAt: string) => dayjs(updatedAt).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right' as const,
      width: 150,
      render: (_: unknown, record: Logistics) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewTrack(record.orderId)}
          >
            查看轨迹
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总订单数"
              value={statistics?.totalOrders || 0}
              prefix={<CarOutlined />}
              suffix="单"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="待发货"
              value={statistics?.pendingShipment || 0}
              prefix={<SyncOutlined />}
              suffix="单"
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="运输中"
              value={statistics?.inTransit || 0}
              prefix={<CarOutlined />}
              suffix="单"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已送达"
              value={statistics?.delivered || 0}
              prefix={<CheckCircleOutlined />}
              suffix="单"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="异常订单"
              value={statistics?.exception || 0}
              prefix={<CloseCircleOutlined />}
              suffix="单"
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="平均配送时长"
              value={statistics?.avgDeliveryTime || 0}
              precision={1}
              suffix="小时"
            />
          </Card>
        </Col>
      </Row>

      {/* 筛选面板 */}
      <FilterPanel
        config={{ filters: logisticsFilters }}
        values={filterValues}
        onChange={setFilterValues}
        onSearch={handleSearch}
        onReset={() => {
          setFilterValues({});
          setPage(0);
        }}
        style={{ marginBottom: 24 }}
      />

      {/* 数据表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={data?.content || []}
          loading={isLoading}
          rowKey="orderId"
          scroll={{ x: 1300 }}
          pagination={{
            current: page + 1,
            pageSize: size,
            total: data?.totalElements || 0,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            onChange: (p, s) => {
              setPage(p - 1);
              setSize(s);
            },
          }}
          locale={{
            emptyText: (
              <div style={{ padding: '40px', color: '#8c8c8c' }}>
                <CarOutlined style={{ fontSize: 48, marginBottom: 16 }} />
                <div>暂无物流数据</div>
                <div style={{ fontSize: 12, marginTop: 8 }}>
                  💡 提示：物流信息需要订单发货后才会显示
                </div>
              </div>
            ),
          }}
        />
      </Card>
    </div>
  );
};

export default LogisticsList;
