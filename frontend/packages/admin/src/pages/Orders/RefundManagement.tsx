/**
 * 退款管理页
 * 
 * 功能：
 * - 展示退款列表
 * - 支持单个退款审核（批准/拒绝）
 * - 支持批量审核
 * - 查看退款详情
 * - 统计卡片
 * 
 * @author BaSui 😎
 * @date 2025-11-05
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Radio,
  Card,
  Row,
  Col,
  Statistic,
  Image,
  Descriptions,
  App,
  Modal,
} from 'antd';
import {
  CheckOutlined,
  CloseOutlined,
  EyeOutlined,
  DollarOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { refundService } from '@campus/shared/services/refund';
import type { Refund } from '@campus/shared/services/refund';
import { FilterPanel } from '@campus/shared/components';
import type { FilterConfig, FilterValues } from '@campus/shared/types/filter';
import { REFUND_STATUS_OPTIONS } from '@campus/shared/constants';
import dayjs from 'dayjs';

const { TextArea } = Form.Item;

/**
 * 退款状态映射
 */
const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待审核', color: 'orange' },
  APPROVED: { text: '已批准', color: 'green' },
  REJECTED: { text: '已拒绝', color: 'red' },
  PROCESSING: { text: '处理中', color: 'blue' },
  COMPLETED: { text: '已完成', color: 'green' },
  FAILED: { text: '失败', color: 'red' },
};

// 退款筛选配置
const refundFilters: FilterConfig[] = [
  {
    type: 'input',
    field: 'keyword',
    label: '关键词',
    placeholder: '搜索退款单号/订单号/商品名/买家',
    width: 280,
  },
  {
    type: 'select',
    field: 'status',
    label: '退款状态',
    placeholder: '选择状态',
    options: REFUND_STATUS_OPTIONS,
    width: 130,
  },
  {
    type: 'dateRange',
    field: 'dateRange',
    label: '申请时间',
    format: 'YYYY-MM-DD',
  },
];

export const RefundManagement: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { message } = App.useApp();
  const [form] = Form.useForm();

  // 筛选参数（使用 FilterPanel 统一管理）
  const [filterValues, setFilterValues] = useState<FilterValues>({});
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  // 审核弹窗
  const [reviewModalVisible, setReviewModalVisible] = useState(false);
  const [currentRefundId, setCurrentRefundId] = useState<number | null>(null);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [currentRefund, setCurrentRefund] = useState<Refund | null>(null);

  // 批量审核
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [batchReviewModalVisible, setBatchReviewModalVisible] = useState(false);

  // 查询退款列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['refunds', 'list', filterValues, page, size],
    queryFn: async () => {
      const response = await refundService.listRefunds({
        keyword: filterValues.keyword,
        status: filterValues.status,
        startDate: filterValues.dateRange?.[0],
        endDate: filterValues.dateRange?.[1],
        page,
        size,
      });
      return response.data;
    },
    staleTime: 5 * 60 * 1000,
  });

  // 查询退款统计
  const { data: statistics } = useQuery({
    queryKey: ['refunds', 'statistics'],
    queryFn: async () => {
      const response = await refundService.getRefundStatistics();
      return response.data;
    },
    refetchInterval: 30000,
  });

  // 单个审核
  const reviewMutation = useMutation({
    mutationFn: (params: { refundId: number; approved: boolean; reason?: string }) =>
      refundService.reviewRefund(params),
    onSuccess: () => {
      message.success('审核成功');
      setReviewModalVisible(false);
      queryClient.invalidateQueries({ queryKey: ['refunds'] });
    },
    onError: () => {
      message.error('审核失败');
    },
  });

  // 批量审核
  const batchReviewMutation = useMutation({
    mutationFn: (params: { refundIds: number[]; approved: boolean; reason?: string }) =>
      refundService.batchReviewRefunds(params),
    onSuccess: () => {
      message.success('批量审核成功');
      setBatchReviewModalVisible(false);
      setSelectedRowKeys([]);
      queryClient.invalidateQueries({ queryKey: ['refunds'] });
    },
    onError: () => {
      message.error('批量审核失败');
    },
  });

  // 搜索处理
  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  // 打开审核弹窗
  const handleOpenReviewModal = (refundId: number) => {
    setCurrentRefundId(refundId);
    setReviewModalVisible(true);
  };

  // 提交审核
  const handleReviewSubmit = async () => {
    if (!currentRefundId) return;
    
    try {
      const values = await form.validateFields();
      reviewMutation.mutate({
        refundId: currentRefundId,
        approved: values.approved,
        reason: values.reason,
      });
    } catch (error) {
      console.error('表单校验失败:', error);
    }
  };

  // 打开批量审核弹窗
  const handleOpenBatchReviewModal = (approved: boolean) => {
    form.resetFields();
    form.setFieldsValue({ approved });
    setBatchReviewModalVisible(true);
  };

  // 提交批量审核
  const handleBatchReviewSubmit = async () => {
    try {
      const values = await form.validateFields();
      batchReviewMutation.mutate({
        refundIds: selectedRowKeys as number[],
        approved: values.approved,
        reason: values.reason,
      });
    } catch (error) {
      console.error('表单校验失败:', error);
    }
  };

  // 查看详情
  const handleViewDetail = async (refund: Refund) => {
    setCurrentRefund(refund);
    setDetailModalVisible(true);
  };

  // 表格列定义
  const columns = [
    {
      title: '退款单号',
      dataIndex: 'refundNo',
      key: 'refundNo',
      width: 180,
      fixed: 'left' as const,
    },
    {
      title: '订单号',
      dataIndex: 'orderNo',
      key: 'orderNo',
      width: 180,
      render: (orderNo: string) => (
        <Button
          type="link"
          size="small"
          onClick={() => navigate(`/admin/orders/${orderNo}`)}
        >
          {orderNo}
        </Button>
      ),
    },
    {
      title: '商品',
      key: 'goods',
      width: 200,
      render: (_: any, record: Refund) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <img
            src={record.goodsImage || 'https://picsum.photos/40/40?random=4'}
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
      title: '退款金额',
      dataIndex: 'refundAmount',
      key: 'refundAmount',
      width: 120,
      render: (amount: number) => (
        <span style={{ color: '#f5222d', fontWeight: 'bold' }}>¥{amount?.toFixed(2)}</span>
      ),
    },
    {
      title: '退款原因',
      dataIndex: 'refundReason',
      key: 'refundReason',
      width: 200,
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const statusInfo = STATUS_MAP[status] || { text: status, color: 'default' };
        return <Tag color={statusInfo.color}>{statusInfo.text}</Tag>;
      },
    },
    {
      title: '申请时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right' as const,
      width: 200,
      render: (_: any, record: Refund) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            详情
          </Button>
          {record.status === 'PENDING' && (
            <Button
              type="link"
              size="small"
              icon={<CheckOutlined />}
              onClick={() => handleOpenReviewModal(record.id)}
            >
              审核
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <h2>💰 退款管理</h2>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总退款单数"
              value={statistics?.totalRefunds || 0}
              prefix={<DollarOutlined />}
              suffix="单"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="待审核"
              value={statistics?.pendingRefunds || 0}
              valueStyle={{ color: '#fa8c16' }}
              suffix="单"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已退款金额"
              value={statistics?.completedRefundAmount || 0}
              precision={2}
              prefix="¥"
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="退款成功率"
              value={statistics?.successRate || 0}
              precision={1}
              suffix="%"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 筛选面板 */}
      <FilterPanel
        config={{ filters: refundFilters }}
        values={filterValues}
        onChange={setFilterValues}
        onSearch={handleSearch}
        onReset={() => {
          setFilterValues({});
          setPage(0);
        }}
        style={{ marginBottom: 16 }}
      />

      {/* 批量操作按钮 */}
      <Space style={{ marginBottom: 16 }}>
        <Button
          type="primary"
          icon={<CheckOutlined />}
          onClick={() => handleOpenBatchReviewModal(true)}
          disabled={selectedRowKeys.length === 0}
        >
          批量批准 ({selectedRowKeys.length})
        </Button>
        <Button
          danger
          icon={<CloseOutlined />}
          onClick={() => handleOpenBatchReviewModal(false)}
          disabled={selectedRowKeys.length === 0}
        >
          批量拒绝 ({selectedRowKeys.length})
        </Button>
      </Space>

      {/* 退款表格 */}
      <Table
        rowKey="id"
        columns={columns}
        dataSource={data?.content || []}
        loading={isLoading}
        rowSelection={{
          selectedRowKeys,
          onChange: setSelectedRowKeys,
          getCheckboxProps: (record: Refund) => ({
            disabled: record.status !== 'PENDING',
          }),
        }}
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
        scroll={{ x: 1600 }}
      />

      {/* 审核弹窗 */}
      <Modal
        title="退款审核"
        open={reviewModalVisible}
        onOk={handleReviewSubmit}
        onCancel={() => setReviewModalVisible(false)}
        okText="提交"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="approved"
            label="审核结果"
            rules={[{ required: true, message: '请选择审核结果' }]}
          >
            <Radio.Group>
              <Radio value={true}>批准</Radio>
              <Radio value={false}>拒绝</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item
            name="reason"
            label="审核说明"
            rules={[{ required: true, message: '请输入审核说明' }]}
          >
            <TextArea rows={4} placeholder="请输入审核说明（批准原因或拒绝理由）" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 批量审核弹窗 */}
      <Modal
        title="批量审核"
        open={batchReviewModalVisible}
        onOk={handleBatchReviewSubmit}
        onCancel={() => setBatchReviewModalVisible(false)}
        okText="提交"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="approved" label="审核结果">
            <Radio.Group>
              <Radio value={true}>批准</Radio>
              <Radio value={false}>拒绝</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item
            name="reason"
            label="审核说明"
            rules={[{ required: true, message: '请输入审核说明' }]}
          >
            <TextArea
              rows={4}
              placeholder={`将对 ${selectedRowKeys.length} 个退款单执行相同操作`}
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 详情弹窗 */}
      <Modal
        title="退款详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={null}
        width={800}
      >
        {currentRefund && (
          <Descriptions column={2} bordered>
            <Descriptions.Item label="退款单号" span={2}>
              {currentRefund.refundNo}
            </Descriptions.Item>
            <Descriptions.Item label="订单号" span={2}>
              {currentRefund.orderNo}
            </Descriptions.Item>
            <Descriptions.Item label="商品">
              {currentRefund.goodsTitle}
            </Descriptions.Item>
            <Descriptions.Item label="商品图片">
              <Image
                src={currentRefund.goodsImage}
                alt="商品"
                style={{ width: 60, height: 60 }}
              />
            </Descriptions.Item>
            <Descriptions.Item label="买家">
              {currentRefund.buyerName}
            </Descriptions.Item>
            <Descriptions.Item label="退款金额">
              <span style={{ color: '#f5222d', fontWeight: 'bold' }}>
                ¥{currentRefund.refundAmount?.toFixed(2)}
              </span>
            </Descriptions.Item>
            <Descriptions.Item label="退款原因" span={2}>
              {currentRefund.refundReason}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={STATUS_MAP[currentRefund.status]?.color}>
                {STATUS_MAP[currentRefund.status]?.text}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="申请时间">
              {currentRefund.createdAt ? dayjs(currentRefund.createdAt).format('YYYY-MM-DD HH:mm:ss') : '-'}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default RefundManagement;
