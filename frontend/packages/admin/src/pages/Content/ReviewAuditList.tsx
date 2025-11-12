/**
 * 评价审核列表页
 * @author BaSui 😎
 */

import { useState } from 'react';
import { Table, Button, Space, Tag, App, Modal, Form, Radio, Card, Row, Col, Statistic, Rate, Input } from 'antd';
import { CheckOutlined, CloseOutlined, EyeOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { reviewService } from '@campus/shared/services/goods/review';

const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待审核', color: 'orange' },
  APPROVED: { text: '已通过', color: 'green' },
  REJECTED: { text: '已拒绝', color: 'red' },
};

export const ReviewAuditList: React.FC = () => {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);
  const [auditModalVisible, setAuditModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [currentReview, setCurrentReview] = useState<any>(null);

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['reviews', 'pending', { page, size }],
    queryFn: () => reviewService.listPendingReviews({ page, size }),
    staleTime: 2 * 60 * 1000,
  });

  const auditMutation = useMutation({
    mutationFn: ({ reviewId, approved, reason }: { reviewId: number; approved: boolean; reason?: string }) =>
      reviewService.auditReview(reviewId, { approved, reason }),
    onSuccess: () => {
      message.success('审核成功');
      setAuditModalVisible(false);
      setCurrentReview(null);
      form.resetFields();
      refetch();
      queryClient.invalidateQueries({ queryKey: ['reviews'] });
    },
    onError: () => {
      message.error('审核失败');
    },
  });

  const handleOpenAuditModal = (review: any) => {
    setCurrentReview(review);
    form.resetFields();
    form.setFieldsValue({ approved: true });
    setAuditModalVisible(true);
  };

  const handleAuditSubmit = async () => {
    if (!currentReview) return;
    try {
      const values = await form.validateFields();
      auditMutation.mutate({
        reviewId: currentReview.id,
        approved: values.approved,
        reason: values.reason,
      });
    } catch (error) {
      console.error('表单校验失败:', error);
    }
  };

  const handleViewDetail = (review: any) => {
    setCurrentReview(review);
    setDetailModalVisible(true);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '商品', dataIndex: 'goodsTitle', key: 'goodsTitle', width: 200, ellipsis: true },
    { title: '买家', dataIndex: 'buyerName', key: 'buyerName', width: 120 },
    { title: '评分', dataIndex: 'rating', key: 'rating', width: 150, render: (r: number) => <Rate disabled value={r} /> },
    { title: '内容', dataIndex: 'content', key: 'content', ellipsis: true },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100, render: (status: string) => {
      const info = STATUS_MAP[status] || { text: status, color: 'default' };
      return <Tag color={info.color}>{info.text}</Tag>;
    }},
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180, render: (d: string) => new Date(d).toLocaleString('zh-CN') },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right' as const,
      width: 200,
      render: (_: any, record: any) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)}>查看</Button>
          <Button type="link" size="small" icon={<CheckOutlined />} onClick={() => handleOpenAuditModal(record)}>审核</Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={8}><Card><Statistic title="待审核" value={data?.totalElements || 0} valueStyle={{ color: '#faad14' }} /></Card></Col>
        <Col span={8}><Card><Statistic title="已通过" value={0} valueStyle={{ color: '#52c41a' }} /></Card></Col>
        <Col span={8}><Card><Statistic title="已拒绝" value={0} valueStyle={{ color: '#f5222d' }} /></Card></Col>
      </Row>

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

      <Modal
        title="审核评价"
        open={auditModalVisible}
        onOk={handleAuditSubmit}
        onCancel={() => {
          setAuditModalVisible(false);
          setCurrentReview(null);
          form.resetFields();
        }}
        confirmLoading={auditMutation.isPending}
        okText="提交审核"
        cancelText="取消"
      >
        <Form form={form} layout="vertical" initialValues={{ approved: true }}>
          <Form.Item name="approved" label="审核结果" rules={[{ required: true }]}>
            <Radio.Group>
              <Radio value={true}>批准发布</Radio>
              <Radio value={false}>拒绝发布</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item name="reason" label="审核意见">
            <Input.TextArea rows={4} placeholder="选填，最多200字" maxLength={200} showCount />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="评价详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={[<Button key="close" onClick={() => setDetailModalVisible(false)}>关闭</Button>]}
        width={700}
      >
        {currentReview && (
          <div>
            <p><strong>商品：</strong>{currentReview.goodsTitle}</p>
            <p><strong>买家：</strong>{currentReview.buyerName}</p>
            <p><strong>评分：</strong><Rate disabled value={currentReview.rating} /></p>
            <p><strong>内容：</strong>{currentReview.content}</p>
            <p><strong>创建时间：</strong>{new Date(currentReview.createdAt).toLocaleString('zh-CN')}</p>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default ReviewAuditList;
