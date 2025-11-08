/**
 * 纠纷仲裁列表页
 * @author BaSui 😎
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Button, Input, Select, Space, Tag, Card, Row, Col, Statistic } from 'antd';
import { SearchOutlined, EyeOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { getApi } from '@campus/shared/utils/apiClient';

const { Option } = Select;

const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待处理', color: 'orange' },
  INVESTIGATING: { text: '调查中', color: 'blue' },
  RESOLVED: { text: '已解决', color: 'green' },
  REJECTED: { text: '已驳回', color: 'red' },
};

export const DisputeList: React.FC = () => {
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<string | undefined>();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);

  const { data, isLoading } = useQuery({
    queryKey: ['disputes', { keyword, status, page, size }],
    queryFn: async () => {
      const api = getApi();
      const response = await api.listAllDisputes(
        keyword || undefined,
        status as any,
        page,
        size
      );
      return response.data.data;
    },
  });

  const columns = [
    { title: '纠纷编号', dataIndex: 'disputeNo', key: 'disputeNo', width: 180, fixed: 'left' as const },
    { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
    { title: '标题', dataIndex: 'title', key: 'title', width: 200, ellipsis: true },
    { title: '申诉方', dataIndex: 'plaintiffName', key: 'plaintiffName', width: 120 },
    { title: '被诉方', dataIndex: 'defendantName', key: 'defendantName', width: 120 },
    { title: '涉及金额', dataIndex: 'amount', key: 'amount', width: 120, render: (a: string) => `¥${a}` },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100, render: (s: string) => <Tag color={STATUS_MAP[s].color}>{STATUS_MAP[s].text}</Tag> },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180, render: (d: string) => new Date(d).toLocaleString('zh-CN') },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right' as const,
      width: 120,
      render: (_: any, record: any) => (
        <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/admin/disputes/${record.id}`)}>查看详情</Button>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}><Card><Statistic title="总纠纷数" value={data?.totalElements || 0} /></Card></Col>
        <Col span={6}><Card><Statistic title="待处理" value={10} valueStyle={{ color: '#fa8c16' }} /></Card></Col>
        <Col span={6}><Card><Statistic title="调查中" value={8} valueStyle={{ color: '#1890ff' }} /></Card></Col>
        <Col span={6}><Card><Statistic title="已解决" value={22} valueStyle={{ color: '#52c41a' }} /></Card></Col>
      </Row>

      <Card style={{ marginBottom: 16 }}>
        <Space>
          <Input placeholder="搜索纠纷编号/订单号" value={keyword} onChange={(e) => setKeyword(e.target.value)} style={{ width: 220 }} prefix={<SearchOutlined />} />
          <Select placeholder="状态" value={status} onChange={setStatus} allowClear style={{ width: 120 }}>
            {Object.entries(STATUS_MAP).map(([k, v]) => <Option key={k} value={k}>{v.text}</Option>)}
          </Select>
          <Button type="primary" icon={<SearchOutlined />}>搜索</Button>
          <Button onClick={() => { setKeyword(''); setStatus(undefined); setPage(0); }}>重置</Button>
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
          onChange: (p, s) => { setPage(p - 1); setSize(s); },
        }}
        scroll={{ x: 1400 }}
      />
    </div>
  );
};

export default DisputeList;
