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
  Input,
  Select,
  Space,
  Tag,
  message,
  Modal,
  Form,
  Radio,
  Card,
  Row,
  Col,
  Statistic,
  Image,
  Descriptions,
  DatePicker,
} from 'antd';
import {
  SearchOutlined,
  CheckOutlined,
  CloseOutlined,
  EyeOutlined,
  DollarOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { refundService } from '@campus/shared/services/refund';
import type { Refund } from '@campus/shared/services/refund';
import dayjs, { Dayjs } from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;
const { RangePicker } = DatePicker;

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

export const RefundManagement: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();

  // 查询参数
  const [keyword, setKeyword] = useState<string>('');
  const [status, setStatus] = useState<string | undefined>();
  const [dateRange, setDateRange] = useState<[Dayjs, Dayjs] | null>(null);
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

  // 构建查询参数
  const queryParams = {
    keyword,
    status,
    startDate: dateRange?.[0]?.format('YYYY-MM-DD'),
    endDate: dateRange?.[1]?.format('YYYY-MM-DD'),
    page,
    size,
  };

  // 查询退款列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['refunds', 'list', queryParams],
    queryFn: async () => {
      const response = await refundService.listRefunds(queryParams);
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
      setCurrentRefundId(null);
      form.resetFields();
      refetch();
      queryClient.invalidateQueries({ queryKey: ['refunds'] });
    },
    onError: () => {
      message.error('审核失败');
    },
  });

  // 批量审核
  const batchReviewMutation = useMutation({
    mutationFn: (params: { refundIds: number[]; approved: boolean; reason?: string }) =>
      refundService.batchReviewRefunds(params.refundIds, params.approved, params.reason),
    onSuccess: (response) => {
      const { successCount, failureCount } = response.data;
      message.success(`批量审核完成：成功${successCount}个，失败${failureCount}个`);
      setBatchReviewModalVisible(false);
      setSelectedRowKeys([]);
      form.resetFields();
      refetch();
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

  // 重置筛选
  const handleReset = () => {
    setKeyword('');
    setStatus(undefined);
    setDateRange(null);
    setPage(0);
  };

  // 查看详情
  const handleViewDetail = (record: Refund) => {
    setCurrentRefund(record);
    setDetailModalVisible(true);
  };

  // 打开单个审核弹窗
  const handleOpenReviewModal = (refundId: number) => {
    setCurrentRefundId(refundId);
    form.resetFields();
    form.setFieldsValue({ approved: true });
    setReviewModalVisible(true);
  };

  // 提交单个审核
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
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要审核的退款');
      return;
    }
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
      render: (date: string) => new Date(date).toLocaleString('zh-CN'),
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
            查看
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
    <div style={{ padding: 24 }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总退款数"
              value={statistics?.total || 0}
              prefix={<DollarOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="待审核"
              value={statistics?.pending || 0}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已批准"
              value={statistics?.approved || 0}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已拒绝"
              value={statistics?.rejected || 0}
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 搜索和筛选栏 */}
      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <Input
            placeholder="搜索退款单号/订单号/商品名/买家"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={handleSearch}
            style={{ width: 280 }}
            prefix={<SearchOutlined />}
          />
          <Select
            placeholder="选择状态"
            value={status}
            onChange={setStatus}
            allowClear
            style={{ width: 130 }}
          >
            <Option value="PENDING">待审核</Option>
            <Option value="APPROVED">已批准</Option>
            <Option value="REJECTED">已拒绝</Option>
            <Option value="PROCESSING">处理中</Option>
            <Option value="COMPLETED">已完成</Option>
          </Select>
          <RangePicker
            value={dateRange}
            onChange={setDateRange}
            style={{ width: 260 }}
            format="YYYY-MM-DD"
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
      </Card>

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
        scroll={{ x: 1500 }}
      />

      {/* 审核弹窗 */}
      <Modal
        title="审核退款"
        open={reviewModalVisible}
        onOk={handleReviewSubmit}
        onCancel={() => {
          setReviewModalVisible(false);
          setCurrentRefundId(null);
          form.resetFields();
        }}
        confirmLoading={reviewMutation.isPending}
        okText="提交审核"
        cancelText="取消"
      >
        <Form form={form} layout="vertical" initialValues={{ approved: true }}>
          <Form.Item
            name="approved"
            label="审核结果"
            rules={[{ required: true, message: '请选择审核结果' }]}
          >
            <Radio.Group>
              <Radio value={true}>批准退款</Radio>
              <Radio value={false}>拒绝退款</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item
            name="reason"
            label="审核意见"
            rules={[
              { required: false },
              { max: 200, message: '审核意见不能超过200字' },
            ]}
          >
            <TextArea
              rows={4}
              placeholder="请填写审核意见（选填，最多200字）"
              showCount
              maxLength={200}
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 批量审核弹窗 */}
      <Modal
        title={`批量审核 (${selectedRowKeys.length}个退款)`}
        open={batchReviewModalVisible}
        onOk={handleBatchReviewSubmit}
        onCancel={() => {
          setBatchReviewModalVisible(false);
          form.resetFields();
        }}
        confirmLoading={batchReviewMutation.isPending}
        okText="提交审核"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="approved"
            label="审核结果"
            rules={[{ required: true, message: '请选择审核结果' }]}
          >
            <Radio.Group>
              <Radio value={true}>批准退款</Radio>
              <Radio value={false}>拒绝退款</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item
            name="reason"
            label="审核意见"
            rules={[
              { required: false },
              { max: 200, message: '审核意见不能超过200字' },
            ]}
          >
            <TextArea
              rows={4}
              placeholder="请填写统一的审核意见（选填，最多200字）"
              showCount
              maxLength={200}
            />
          </Form.Item>
        </Form>
      </Modal>

      {/* 详情弹窗 */}
      <Modal
        title="退款详情"
        open={detailModalVisible}
        onCancel={() => {
          setDetailModalVisible(false);
          setCurrentRefund(null);
        }}
        footer={[
          <Button key="close" onClick={() => setDetailModalVisible(false)}>
            关闭
          </Button>,
        ]}
        width={800}
      >
        {currentRefund && (
          <div>
            <Descriptions column={2} bordered>
              <Descriptions.Item label="退款单号">{currentRefund.refundNo}</Descriptions.Item>
              <Descriptions.Item label="订单号">
                <Button
                  type="link"
                  size="small"
                  onClick={() => {
                    setDetailModalVisible(false);
                    navigate(`/admin/orders/${currentRefund.orderNo}`);
                  }}
                >
                  {currentRefund.orderNo}
                </Button>
              </Descriptions.Item>
              <Descriptions.Item label="买家">{currentRefund.buyerName}</Descriptions.Item>
              <Descriptions.Item label="卖家">{currentRefund.sellerName}</Descriptions.Item>
              <Descriptions.Item label="退款金额">
                <span style={{ color: '#f5222d', fontWeight: 'bold' }}>
                  ¥{currentRefund.refundAmount?.toFixed(2)}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={STATUS_MAP[currentRefund.status]?.color}>
                  {STATUS_MAP[currentRefund.status]?.text}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="退款原因" span={2}>
                {currentRefund.refundReason}
              </Descriptions.Item>
              {currentRefund.refundProof && currentRefund.refundProof.length > 0 && (
                <Descriptions.Item label="退款凭证" span={2}>
                  <Image.PreviewGroup>
                    <Space>
                      {currentRefund.refundProof.map((url, index) => (
                        <Image
                          key={index}
                          src={url}
                          alt={`凭证${index + 1}`}
                          width={80}
                          height={80}
                          style={{ objectFit: 'cover' }}
                        />
                      ))}
                    </Space>
                  </Image.PreviewGroup>
                </Descriptions.Item>
              )}
              <Descriptions.Item label="申请时间">
                {new Date(currentRefund.createdAt).toLocaleString('zh-CN')}
              </Descriptions.Item>
              <Descriptions.Item label="更新时间">
                {new Date(currentRefund.updatedAt).toLocaleString('zh-CN')}
              </Descriptions.Item>
              {currentRefund.reviewedAt && (
                <Descriptions.Item label="审核时间">
                  {new Date(currentRefund.reviewedAt).toLocaleString('zh-CN')}
                </Descriptions.Item>
              )}
              {currentRefund.reviewerName && (
                <Descriptions.Item label="审核人">
                  {currentRefund.reviewerName}
                </Descriptions.Item>
              )}
              {currentRefund.reviewReason && (
                <Descriptions.Item label="审核意见" span={2}>
                  {currentRefund.reviewReason}
                </Descriptions.Item>
              )}
            </Descriptions>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default RefundManagement;
