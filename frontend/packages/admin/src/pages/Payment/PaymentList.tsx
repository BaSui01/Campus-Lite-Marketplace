/**
 * 支付记录列表页
 * 
 * 功能：
 * - 分页查询支付记录（管理员视角）
 * - 搜索支付记录（订单号、商品名、买家）
 * - 多状态筛选（待支付、已支付、支付失败、已退款）
 * - 支付方式筛选（微信、支付宝、余额）
 * - 时间范围筛选
 * - 支付统计卡片
 * - 查看支付详情
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
} from 'antd';
import {
  EyeOutlined,
  DollarOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { paymentService } from '@campus/shared';
import type { OrderResponse } from '@campus/shared/api';
import { FilterPanel } from '@campus/shared/components';
import type { FilterConfig, FilterValues } from '@campus/shared/types/filter';
import { PAYMENT_STATUS_OPTIONS, PAYMENT_METHOD_OPTIONS } from '@campus/shared/constants';
import dayjs from 'dayjs';

/**
 * 支付状态映射
 */
const STATUS_MAP: Record<string, { text: string; color: string; icon: React.ReactNode }> = {
  PENDING_PAYMENT: { 
    text: '待支付', 
    color: 'orange',
    icon: <SyncOutlined spin />
  },
  PAID: { 
    text: '已支付', 
    color: 'blue',
    icon: <CheckCircleOutlined />
  },
  SHIPPED: { 
    text: '已发货', 
    color: 'cyan',
    icon: <CheckCircleOutlined />
  },
  COMPLETED: { 
    text: '已完成', 
    color: 'green',
    icon: <CheckCircleOutlined />
  },
  CANCELLED: { 
    text: '已取消', 
    color: 'default',
    icon: <CloseCircleOutlined />
  },
  REFUNDED: { 
    text: '已退款', 
    color: 'red',
    icon: <CloseCircleOutlined />
  },
};

/**
 * 支付方式映射
 */
const PAYMENT_METHOD_MAP: Record<string, { text: string; color: string }> = {
  WECHAT_PAY: { text: '微信支付', color: 'green' },
  ALIPAY: { text: '支付宝', color: 'blue' },
  BALANCE: { text: '余额支付', color: 'orange' },
};

// 支付筛选配置
const paymentFilters: FilterConfig[] = [
  {
    type: 'input',
    field: 'keyword',
    label: '关键词',
    placeholder: '搜索订单号/商品名/买家',
    width: 220,
  },
  {
    type: 'select',
    field: 'status',
    label: '支付状态',
    placeholder: '选择状态',
    options: PAYMENT_STATUS_OPTIONS,
    width: 130,
  },
  {
    type: 'select',
    field: 'paymentMethod',
    label: '支付方式',
    placeholder: '选择支付方式',
    options: PAYMENT_METHOD_OPTIONS,
    width: 150,
  },
  {
    type: 'dateRange',
    field: 'dateRange',
    label: '时间范围',
    format: 'YYYY-MM-DD',
  },
];

export const PaymentList: React.FC = () => {
  const navigate = useNavigate();

  // 筛选参数（使用 FilterPanel 统一管理）
  const [filterValues, setFilterValues] = useState<FilterValues>({});
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  // 查询支付记录列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['payments', 'admin', 'list', filterValues, page, size],
    queryFn: () => paymentService.listPayments({
      keyword: filterValues.keyword,
      status: filterValues.status,
      paymentMethod: filterValues.paymentMethod,
      startDate: filterValues.dateRange?.[0],
      endDate: filterValues.dateRange?.[1],
      page,
      size,
    }),
    staleTime: 5 * 60 * 1000, // 缓存5分钟
  });

  // 查询支付统计
  const { data: statistics } = useQuery({
    queryKey: ['payments', 'statistics', filterValues.dateRange],
    queryFn: () => paymentService.getPaymentStatistics(
      filterValues.dateRange?.[0],
      filterValues.dateRange?.[1]
    ),
    staleTime: 5 * 60 * 1000,
  });

  // 搜索处理
  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  // 查看详情
  const handleView = (orderNo: string) => {
    navigate(`/admin/payments/${orderNo}`);
  };

  // 表格列定义
  const columns = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      key: 'orderNo',
      width: 180,
      render: (orderNo: string) => (
        <Button type="link" onClick={() => handleView(orderNo)}>
          {orderNo}
        </Button>
      ),
    },
    {
      title: '商品名称',
      dataIndex: 'goodsTitle',
      key: 'goodsTitle',
      width: 200,
      ellipsis: true,
    },
    {
      title: '买家',
      dataIndex: 'buyerUsername',
      key: 'buyerUsername',
      width: 120,
    },
    {
      title: '支付方式',
      dataIndex: 'paymentMethod',
      key: 'paymentMethod',
      width: 120,
      render: (method: string) => {
        const config = PAYMENT_METHOD_MAP[method] || { text: method, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '支付金额',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      width: 120,
      render: (amount: number) => (
        <span style={{ color: '#f5222d', fontWeight: 'bold' }}>
          ¥{amount?.toFixed(2)}
        </span>
      ),
    },
    {
      title: '支付状态',
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
      title: '支付时间',
      dataIndex: 'paidAt',
      key: 'paidAt',
      width: 180,
      render: (paidAt: string) => paidAt ? dayjs(paidAt).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (createdAt: string) => dayjs(createdAt).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right' as const,
      width: 100,
      render: (_: unknown, record: OrderResponse) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleView(record.orderNo!)}
          >
            查看
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
              title="总交易金额"
              value={statistics?.totalAmount || 0}
              precision={2}
              prefix={<DollarOutlined />}
              suffix="元"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="总交易笔数"
              value={statistics?.totalCount || 0}
              prefix={<DollarOutlined />}
              suffix="笔"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="成功交易金额"
              value={statistics?.successAmount || 0}
              precision={2}
              prefix={<CheckCircleOutlined />}
              suffix="元"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="退款金额"
              value={statistics?.refundAmount || 0}
              precision={2}
              prefix={<CloseCircleOutlined />}
              suffix="元"
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 筛选面板 */}
      <FilterPanel
        config={{ filters: paymentFilters }}
        values={filterValues}
        onChange={setFilterValues}
        onSearch={handleSearch}
        onReset={() => {
          setFilterValues({});
          setPage(0);
        }}
      />

      {/* 数据表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={data?.content || []}
          loading={isLoading}
          rowKey="id"
          scroll={{ x: 1500 }}
          pagination={{
            current: page + 1,
            pageSize: size,
            total: data?.totalElements || 0,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            onChange: (newPage, newSize) => {
              setPage(newPage - 1);
              setSize(newSize!);
            },
          }}
        />
      </Card>
    </div>
  );
};

export default PaymentList;
