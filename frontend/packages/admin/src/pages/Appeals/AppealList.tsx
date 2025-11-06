/**
 * 申诉列表页
 * 
 * 功能：
 * - 分页查询申诉列表
 * - 状态筛选、类型筛选
 * - 用户搜索
 * - 查看申诉详情
 * - 统计卡片
 * 
 * @author BaSui 😎
 * @date 2025-11-05
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Button, Input, Select, Space, Tag, Card, Row, Col, Statistic } from 'antd';
import { SearchOutlined, EyeOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { appealService } from '@campus/shared/services/appeal';

const { Option } = Select;

const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待审核', color: 'orange' },
  REVIEWING: { text: '审核中', color: 'blue' },
  APPROVED: { text: '已批准', color: 'green' },
  REJECTED: { text: '已拒绝', color: 'red' },
  EXPIRED: { text: '已过期', color: 'default' },
};

const TYPE_MAP: Record<string, string> = {
  ACCOUNT_BAN: '封禁申诉',
  GOODS_REJECTION: '商品申诉',
  ORDER_DISPUTE: '订单申诉',
  OTHER: '其他申诉',
};

export const AppealList: React.FC = () => {
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState<string>('');
  const [type, setType] = useState<string | undefined>();
  const [status, setStatus] = useState<string | undefined>();
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['appeals', 'list', { keyword, type, status, page, size }],
    queryFn: async () => {
      const response = await appealService.listAppeals({ keyword, type, status, page, size });
      return response.data;
    },
    staleTime: 2 * 60 * 1000,
  });

  const { data: stats } = useQuery({
    queryKey: ['appeals', 'statistics'],
    queryFn: async () => {
      const response = await appealService.getAppealStatistics();
      return response.data;
    },
    refetchInterval: 30000,
  });

  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  const handleReset = () => {
    setKeyword('');
    setType(undefined);
    setStatus(undefined);
    setPage(0);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '标题', dataIndex: 'title', key: 'title', width: 200, ellipsis: true },
    { title: '申诉人', dataIndex: 'userName', key: 'userName', width: 120 },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type: string) => TYPE_MAP[type] || type,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const info = STATUS_MAP[status];
        return <Tag color={info.color}>{info.text}</Tag>;
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
      render: (_: any, record: any) => (
        <Button
          type="link"
          size="small"
          icon={<EyeOutlined />}
          onClick={() => navigate(`/admin/appeals/${record.id}`)}
        >
          查看详情
        </Button>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card><Statistic title="总申诉数" value={stats?.total || 0} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="待审核" value={stats?.pending || 0} valueStyle={{ color: '#fa8c16' }} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="已批准" value={stats?.approved || 0} valueStyle={{ color: '#52c41a' }} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="已拒绝" value={stats?.rejected || 0} valueStyle={{ color: '#f5222d' }} /></Card>
        </Col>
      </Row>

      <Card style={{ marginBottom: 16 }}>
        <Space>
          <Input
            placeholder="搜索申诉ID/用户名"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={handleSearch}
            style={{ width: 200 }}
            prefix={<SearchOutlined />}
          />
          <Select placeholder="申诉类型" value={type} onChange={setType} allowClear style={{ width: 130 }}>
            {Object.entries(TYPE_MAP).map(([k, v]) => (
              <Option key={k} value={k}>{v}</Option>
            ))}
          </Select>
          <Select placeholder="状态" value={status} onChange={setStatus} allowClear style={{ width: 120 }}>
            {Object.entries(STATUS_MAP).map(([k, v]) => (
              <Option key={k} value={k}>{v.text}</Option>
            ))}
          </Select>
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>搜索</Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
      </Card>

      <Table
        rowKey="id"
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
          onChange: (p, s) => { setPage(p - 1); setSize(s); },
        }}
        scroll={{ x: 1200 }}
      />
    </div>
  );
};

export default AppealList;
