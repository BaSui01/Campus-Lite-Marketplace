/**
 * 订单列表页
 * 
 * 功能：
 * - 分页查询订单列表（管理员视角）
 * - 搜索订单（订单号、商品名、买家/卖家）
 * - 多状态筛选（待支付、已支付、已发货、已完成、已取消、退款中、已退款）
 * - 时间范围筛选
 * - 订单统计卡片
 * - 查看订单详情
 * 
 * @author BaSui 😎
 * @date 2025-11-05
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
} from 'antd';
import {
  SearchOutlined,
  EyeOutlined,
  DollarOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { orderService } from '@campus/shared/services/order';
import type { OrderResponse } from '@campus/shared/api';
import dayjs, { Dayjs } from 'dayjs';

const { Option } = Select;
const { RangePicker } = DatePicker;

/**
 * 订单状态映射
 */
const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING_PAYMENT: { text: '待支付', color: 'orange' },
  PAID: { text: '已支付', color: 'blue' },
  SHIPPED: { text: '已发货', color: 'cyan' },
  COMPLETED: { text: '已完成', color: 'green' },
  CANCELLED: { text: '已取消', color: 'default' },
  REFUNDING: { text: '退款中', color: 'purple' },
  REFUNDED: { text: '已退款', color: 'red' },
};

export const OrderList: React.FC = () => {
  const navigate = useNavigate();

  // 查询参数
  const [keyword, setKeyword] = useState<string>('');
  const [status, setStatus] = useState<string | undefined>();
  const [dateRange, setDateRange] = useState<[Dayjs, Dayjs] | null>(null);
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  // 构建查询参数
  const queryParams = {
    keyword,
    status,
    startDate: dateRange?.[0]?.format('YYYY-MM-DD'),
    endDate: dateRange?.[1]?.format('YYYY-MM-DD'),
    page,
    size,
  };

  // 查询订单列表（管理员视角）
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['orders', 'admin', 'list', queryParams],
    queryFn: () => orderService.listOrdersAdmin(queryParams),
    staleTime: 5 * 60 * 1000, // 缓存5分钟
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
    setDateRange(null);
    setPage(0);
  };

  // 查看详情
  const handleView = (orderNo: string) => {
    navigate(`/admin/orders/${orderNo}`);
  };

  // 快速筛选今日订单
  const handleToday = () => {
    const today = dayjs();
    setDateRange([today.startOf('day'), today.endOf('day')]);
    setPage(0);
  };

  // 快速筛选近7天
  const handleLast7Days = () => {
    const today = dayjs();
    setDateRange([today.subtract(6, 'day').startOf('day'), today.endOf('day')]);
    setPage(0);
  };

  // 快速筛选近30天
  const handleLast30Days = () => {
    const today = dayjs();
    setDateRange([today.subtract(29, 'day').startOf('day'), today.endOf('day')]);
    setPage(0);
  };

  // 计算统计数据
  const totalOrders = data?.totalElements || 0;
  const pendingPayment =
    data?.content?.filter((o: OrderResponse) => o.status === 'PENDING_PAYMENT').length || 0;
  const completed =
    data?.content?.filter((o: OrderResponse) => o.status === 'COMPLETED').length || 0;
  const refunding =
    data?.content?.filter((o: OrderResponse) => o.status === 'REFUNDING').length || 0;

  // 表格列定义
  const columns = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      key: 'orderNo',
      width: 180,
      fixed: 'left' as const,
    },
    {
      title: '商品',
      dataIndex: 'goodsTitle',
      key: 'goodsTitle',
      width: 200,
      ellipsis: true,
      render: (_: any, record: OrderResponse) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <img
            src={record.goodsImage || 'https://picsum.photos/40/40?random=3'}
            alt={record.goodsTitle}
            style={{ width: 40, height: 40, objectFit: 'cover', borderRadius: 4 }}
          />
          <span>{record.goodsTitle}</span>
        </div>
      ),
    },
    {
      title: '买家',
      dataIndex: 'buyerName',
      key: 'buyerName',
      width: 120,
    },
    {
      title: '卖家',
      dataIndex: 'sellerName',
      key: 'sellerName',
      width: 120,
    },
    {
      title: '金额',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      width: 120,
      render: (amount: number) => (
        <span style={{ color: '#f5222d', fontWeight: 'bold' }}>¥{amount?.toFixed(2)}</span>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: string) => {
        const statusInfo = STATUS_MAP[status] || { text: status, color: 'default' };
        return <Tag color={statusInfo.color}>{statusInfo.text}</Tag>;
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString('zh-CN'),
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right' as const,
      width: 120,
      render: (_: any, record: OrderResponse) => (
        <Button
          type="link"
          size="small"
          icon={<EyeOutlined />}
          onClick={() => handleView(record.orderNo)}
        >
          查看详情
        </Button>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic title="总订单数" value={totalOrders} prefix={<DollarOutlined />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="待支付"
              value={pendingPayment}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="已完成" value={completed} valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="退款中" value={refunding} valueStyle={{ color: '#722ed1' }} />
          </Card>
        </Col>
      </Row>

      {/* 搜索和筛选栏 */}
      <Card style={{ marginBottom: 16 }}>
        <Space wrap style={{ width: '100%', marginBottom: 16 }}>
          <Input
            placeholder="搜索订单号/商品名/买家/卖家"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={handleSearch}
            style={{ width: 250 }}
            prefix={<SearchOutlined />}
          />
          <Select
            placeholder="选择状态"
            value={status}
            onChange={setStatus}
            allowClear
            style={{ width: 150 }}
          >
            <Option value="PENDING_PAYMENT">待支付</Option>
            <Option value="PAID">已支付</Option>
            <Option value="SHIPPED">已发货</Option>
            <Option value="COMPLETED">已完成</Option>
            <Option value="CANCELLED">已取消</Option>
            <Option value="REFUNDING">退款中</Option>
            <Option value="REFUNDED">已退款</Option>
          </Select>
          <RangePicker
            value={dateRange}
            onChange={setDateRange}
            style={{ width: 280 }}
            format="YYYY-MM-DD"
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
        <Space>
          <Button size="small" onClick={handleToday}>
            今日
          </Button>
          <Button size="small" onClick={handleLast7Days}>
            近7天
          </Button>
          <Button size="small" onClick={handleLast30Days}>
            近30天
          </Button>
        </Space>
      </Card>

      {/* 订单表格 */}
      <Table
        rowKey="orderNo"
        columns={columns}
        dataSource={data?.content || []}
        loading={isLoading}
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
        scroll={{ x: 1400 }}
      />
    </div>
  );
};

export default OrderList;
