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
  Input,
  Select,
  Space,
  Tag,
  Card,
  Row,
  Col,
  Statistic,
  DatePicker,
  message,
} from 'antd';
import {
  SearchOutlined,
  EyeOutlined,
  DollarOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { paymentService } from '@campus/shared';
import type { OrderResponse } from '@campus/shared/api';
import dayjs, { Dayjs } from 'dayjs';

const { Option } = Select;
const { RangePicker } = DatePicker;

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

export const PaymentList: React.FC = () => {
  const navigate = useNavigate();

  // 查询参数
  const [keyword, setKeyword] = useState<string>('');
  const [status, setStatus] = useState<string | undefined>();
  const [paymentMethod, setPaymentMethod] = useState<string | undefined>();
  const [dateRange, setDateRange] = useState<[Dayjs, Dayjs] | null>(null);
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  // 构建查询参数
  const queryParams = {
    keyword,
    status: status === 'PAID_ALL' ? 'PAID,SHIPPED,COMPLETED' : status, // 已支付包含多种状态
    paymentMethod,
    startDate: dateRange?.[0]?.format('YYYY-MM-DD'),
    endDate: dateRange?.[1]?.format('YYYY-MM-DD'),
    page,
    size,
  };

  // 查询支付记录列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['payments', 'admin', 'list', queryParams],
    queryFn: () => paymentService.listPayments(queryParams),
    staleTime: 5 * 60 * 1000, // 缓存5分钟
  });

  // 查询支付统计
  const { data: statistics } = useQuery({
    queryKey: ['payments', 'statistics', dateRange],
    queryFn: () => paymentService.getPaymentStatistics(
      dateRange?.[0]?.format('YYYY-MM-DD'),
      dateRange?.[1]?.format('YYYY-MM-DD')
    ),
    staleTime: 5 * 60 * 1000,
  });

  // 搜索处理
  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  // 重置筛选
  const handleReset = () => {
    setKeyword('');
    setStatus(undefined);
    setPaymentMethod(undefined);
    setDateRange(null);
    setPage(0);
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

      {/* 搜索筛选区域 */}
      <Card style={{ marginBottom: 24 }}>
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Space wrap>
            <Input
              placeholder="搜索订单号/商品名/买家"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onPressEnter={handleSearch}
              style={{ width: 250 }}
              prefix={<SearchOutlined />}
            />

            <Select
              placeholder="支付状态"
              value={status}
              onChange={setStatus}
              allowClear
              style={{ width: 150 }}
            >
              <Option value="PAID_ALL">全部已支付</Option>
              <Option value="PENDING_PAYMENT">待支付</Option>
              <Option value="PAID">已支付</Option>
              <Option value="SHIPPED">已发货</Option>
              <Option value="COMPLETED">已完成</Option>
              <Option value="REFUNDED">已退款</Option>
              <Option value="CANCELLED">已取消</Option>
            </Select>

            <Select
              placeholder="支付方式"
              value={paymentMethod}
              onChange={setPaymentMethod}
              allowClear
              style={{ width: 150 }}
            >
              <Option value="WECHAT_PAY">微信支付</Option>
              <Option value="ALIPAY">支付宝</Option>
              <Option value="BALANCE">余额支付</Option>
            </Select>

            <RangePicker
              value={dateRange}
              onChange={setDateRange}
              format="YYYY-MM-DD"
              placeholder={['开始日期', '结束日期']}
            />

            <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
              搜索
            </Button>

            <Button onClick={handleReset}>重置</Button>
          </Space>
        </Space>
      </Card>

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
