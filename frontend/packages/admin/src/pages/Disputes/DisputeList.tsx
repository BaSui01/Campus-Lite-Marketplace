/**
 * 纠纷仲裁列表页
 * @author BaSui 😎
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Button, Space, Tag, Card, Row, Col, Statistic, Popconfirm, App } from 'antd';
import { EyeOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { disputeService } from '@/services/dispute';
import type { DisputeDTO } from '@campus/shared/api';
import { FilterPanel } from '@campus/shared/components';
import type { FilterConfig, FilterValues } from '@campus/shared/types/filter';
import dayjs from 'dayjs';

// ✅ 纠纷状态映射（对应后端 DisputeStatus 枚举）
const STATUS_MAP: Record<string, { text: string; color: string }> = {
  SUBMITTED: { text: '已提交', color: 'default' },
  NEGOTIATING: { text: '协商中', color: 'blue' },
  PENDING_ARBITRATION: { text: '待仲裁', color: 'orange' },
  ARBITRATING: { text: '仲裁中', color: 'purple' },
  COMPLETED: { text: '已完成', color: 'green' },
  CLOSED: { text: '已关闭', color: 'default' },
};

// ✅ 纠纷类型映射（对应后端 DisputeType 枚举）
const TYPE_MAP: Record<string, string> = {
  GOODS_MISMATCH: '商品不符',
  QUALITY_ISSUE: '质量问题',
  LOGISTICS_DELAY: '物流延误',
  FALSE_ADVERTISING: '虚假宣传',
  OTHER: '其他',
};

// 纠纷类型选项
const DISPUTE_TYPE_OPTIONS = [
  { label: '商品不符', value: 'GOODS_MISMATCH' },
  { label: '质量问题', value: 'QUALITY_ISSUE' },
  { label: '物流延误', value: 'LOGISTICS_DELAY' },
  { label: '虚假宣传', value: 'FALSE_ADVERTISING' },
  { label: '其他', value: 'OTHER' },
];

// 纠纷状态选项
const DISPUTE_STATUS_OPTIONS = Object.entries(STATUS_MAP).map(([value, { text }]) => ({
  label: text,
  value,
}));

// 纠纷筛选配置
const disputeFilters: FilterConfig[] = [
  {
    type: 'input',
    field: 'keyword',
    label: '关键词',
    placeholder: '搜索订单号/纠纷描述',
    width: 200,
  },
  {
    type: 'select',
    field: 'type',
    label: '纠纷类型',
    placeholder: '选择纠纷类型',
    options: DISPUTE_TYPE_OPTIONS,
    width: 150,
  },
  {
    type: 'select',
    field: 'status',
    label: '纠纷状态',
    placeholder: '选择状态',
    options: DISPUTE_STATUS_OPTIONS,
    width: 120,
  },
  {
    type: 'dateRange',
    field: 'dateRange',
    label: '时间范围',
    format: 'YYYY-MM-DD',
  },
  {
    type: 'numberRange',
    field: 'amount',
    label: '涉及金额',
    prefix: '¥',
    min: 0,
  },
];

export const DisputeList: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { message } = App.useApp();

  // 筛选参数（使用 FilterPanel 统一管理）
  const [filterValues, setFilterValues] = useState<FilterValues>({});
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);

  // 查询纠纷列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['disputes', filterValues, page, size],
    queryFn: () => disputeService.listDisputes({
      keyword: filterValues.keyword,
      type: filterValues.type,
      status: filterValues.status,
      startDate: filterValues.dateRange?.[0],
      endDate: filterValues.dateRange?.[1],
      minAmount: filterValues.amount?.min,
      maxAmount: filterValues.amount?.max,
      page,
      size
    }),
  });

  // 查询仲裁员列表
  const { data: arbitrators } = useQuery({
    queryKey: ['arbitrators'],
    queryFn: () => disputeService.listArbitrators(),
  });

  // 删除纠纷
  const deleteMutation = useMutation({
    mutationFn: (id: number) => disputeService.deleteDispute(id),
    onSuccess: () => {
      message.success('删除成功');
      queryClient.invalidateQueries({ queryKey: ['disputes'] });
    },
    onError: () => message.error('删除失败'),
  });

  // 搜索处理
  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  // ✅ 列定义（对应后端 DisputeDTO 字段）
  const columns = [
    {
      title: '纠纷编号',
      dataIndex: 'disputeCode',
      key: 'disputeCode',
      width: 180,
      fixed: 'left' as const
    },
    {
      title: '订单号',
      dataIndex: 'orderNo',
      key: 'orderNo',
      width: 180
    },
    {
      title: '纠纷描述',
      dataIndex: 'descriptionSummary',
      key: 'descriptionSummary',
      width: 200,
      ellipsis: true
    },
    {
      title: '发起人',
      dataIndex: 'initiatorNickname',
      key: 'initiatorNickname',
      width: 120
    },
    {
      title: '被投诉人',
      dataIndex: 'respondentNickname',
      key: 'respondentNickname',
      width: 120
    },
    {
      title: '纠纷类型',
      dataIndex: 'disputeType',
      key: 'disputeType',
      width: 120,
      render: (type: string) => TYPE_MAP[type] || type
    },
    {
      title: '涉及金额',
      dataIndex: 'amount',
      key: 'amount',
      width: 120,
      render: (amount: number) => amount ? `¥${amount.toFixed(2)}` : '-'
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: string) => {
        const config = STATUS_MAP[status];
        return <Tag color={config?.color}>{config?.text || status}</Tag>;
      }
    },
    {
      title: '仲裁员',
      dataIndex: 'arbitratorNickname',
      key: 'arbitratorNickname',
      width: 100,
      render: (nickname: string) => nickname || '-'
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm:ss')
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right' as const,
      width: 150,
      render: (_: any, record: DisputeDTO) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/admin/disputes/${record.id}`)}
          >
            详情
          </Button>
          <Popconfirm
            title="确定删除这条纠纷记录吗？"
            onConfirm={() => deleteMutation.mutate(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <h2>⚖️ 纠纷仲裁管理</h2>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总纠纷数"
              value={data?.totalElements || 0}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="待仲裁"
              value={data?.content?.filter((d) => d.status === 'PENDING_ARBITRATION').length || 0}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="仲裁中"
              value={data?.content?.filter((d) => d.status === 'ARBITRATING').length || 0}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已完成"
              value={data?.content?.filter((d) => d.status === 'COMPLETED').length || 0}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 筛选面板 */}
      <FilterPanel
        config={{ filters: disputeFilters }}
        values={filterValues}
        onChange={setFilterValues}
        onSearch={handleSearch}
        onReset={() => {
          setFilterValues({});
          setPage(0);
        }}
        style={{ marginBottom: 16 }}
      />

      {/* 纠纷表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={data?.content || []}
          loading={isLoading}
          rowKey="id"
          scroll={{ x: 1800 }}
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
        />
      </Card>
    </div>
  );
};

export default DisputeList;
