/**
 * 帖子审核列表页
 * 
 * 功能：
 * - 分页查询待审核帖子
 * - 状态筛选
 * - 帖子详情查看
 * - 单个审核（批准/拒绝）
 * - 统计卡片
 * 
 * @author BaSui 😎
 * @date 2025-11-05
 */

import { useState } from 'react';
import {
  Table,
  Button,
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
} from 'antd';
import {
  CheckOutlined,
  CloseOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getApi } from '@campus/shared/utils/apiClient';

const { Option } = Select;
const { TextArea } = Form.Item;

const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待审核', color: 'orange' },
  PUBLISHED: { text: '已发布', color: 'green' },
  REJECTED: { text: '已拒绝', color: 'red' },
};

export const PostAuditList: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();

  const [status, setStatus] = useState<string | undefined>('PENDING');
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  const [auditModalVisible, setAuditModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [currentPost, setCurrentPost] = useState<any>(null);

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['posts', 'audit', { status, page, size }],
    queryFn: async () => {
      const api = getApi();
      const response = await api.listPosts(
        undefined, // keyword
        status as any,
        page,
        size
      );
      return response.data.data;
    },
    staleTime: 2 * 60 * 1000,
  });

  const auditMutation = useMutation({
    mutationFn: async ({ postId, approved, reason }: { postId: number; approved: boolean; reason?: string }) => {
      const api = getApi();
      await api.auditPost(postId, { approved, reason });
    },
    onSuccess: () => {
      message.success('审核成功');
      setAuditModalVisible(false);
      setCurrentPost(null);
      form.resetFields();
      refetch();
      queryClient.invalidateQueries({ queryKey: ['posts'] });
    },
    onError: () => {
      message.error('审核失败');
    },
  });

  const handleOpenAuditModal = (post: any) => {
    setCurrentPost(post);
    form.resetFields();
    form.setFieldsValue({ approved: true });
    setAuditModalVisible(true);
  };

  const handleAuditSubmit = async () => {
    if (!currentPost) return;
    try {
      const values = await form.validateFields();
      auditMutation.mutate({
        postId: currentPost.id,
        approved: values.approved,
        reason: values.reason,
      });
    } catch (error) {
      console.error('表单校验失败:', error);
    }
  };

  const handleViewDetail = (post: any) => {
    setCurrentPost(post);
    setDetailModalVisible(true);
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      width: 200,
      ellipsis: true,
    },
    {
      title: '内容',
      dataIndex: 'content',
      key: 'content',
      ellipsis: true,
    },
    {
      title: '作者',
      dataIndex: 'authorName',
      key: 'authorName',
      width: 120,
    },
    {
      title: '图片',
      dataIndex: 'images',
      key: 'images',
      width: 80,
      render: (images: string[]) => images.length > 0 ? `${images.length}张` : '-',
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
      title: '发布时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString('zh-CN'),
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right' as const,
      width: 180,
      render: (_: any, record: any) => (
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
              onClick={() => handleOpenAuditModal(record)}
            >
              审核
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const pendingCount = data?.content?.filter((p: any) => p.status === 'PENDING').length || 0;
  const publishedCount = data?.content?.filter((p: any) => p.status === 'PUBLISHED').length || 0;
  const rejectedCount = data?.content?.filter((p: any) => p.status === 'REJECTED').length || 0;

  return (
    <div style={{ padding: 24 }}>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic title="总帖子数" value={data?.totalElements || 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="待审核" value={pendingCount} valueStyle={{ color: '#fa8c16' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="已发布" value={publishedCount} valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="已拒绝" value={rejectedCount} valueStyle={{ color: '#f5222d' }} />
          </Card>
        </Col>
      </Row>

      <Card style={{ marginBottom: 16 }}>
        <Select
          placeholder="选择状态"
          value={status}
          onChange={setStatus}
          style={{ width: 150 }}
        >
          <Option value={undefined}>全部</Option>
          <Option value="PENDING">待审核</Option>
          <Option value="PUBLISHED">已发布</Option>
          <Option value="REJECTED">已拒绝</Option>
        </Select>
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
          onChange: (p, s) => {
            setPage(p - 1);
            setSize(s);
          },
        }}
      />

      <Modal
        title="审核帖子"
        open={auditModalVisible}
        onOk={handleAuditSubmit}
        onCancel={() => {
          setAuditModalVisible(false);
          setCurrentPost(null);
          form.resetFields();
        }}
        confirmLoading={auditMutation.isPending}
        okText="提交审核"
        cancelText="取消"
      >
        <Form form={form} layout="vertical" initialValues={{ approved: true }}>
          <Form.Item
            name="approved"
            label="审核结果"
            rules={[{ required: true }]}
          >
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
        title="帖子详情"
        open={detailModalVisible}
        onCancel={() => {
          setDetailModalVisible(false);
          setCurrentPost(null);
        }}
        footer={[
          <Button key="close" onClick={() => setDetailModalVisible(false)}>
            关闭
          </Button>,
        ]}
        width={700}
      >
        {currentPost && (
          <div>
            <h3>{currentPost.title}</h3>
            <p style={{ color: '#8c8c8c' }}>作者：{currentPost.authorName} | 发布时间：{new Date(currentPost.createdAt).toLocaleString('zh-CN')}</p>
            <p>{currentPost.content}</p>
            {currentPost.images && currentPost.images.length > 0 && (
              <div style={{ marginTop: 16 }}>
                <Image.PreviewGroup>
                  <Space>
                    {currentPost.images.map((url: string, index: number) => (
                      <Image key={index} src={url} width={100} height={100} style={{ objectFit: 'cover' }} />
                    ))}
                  </Space>
                </Image.PreviewGroup>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default PostAuditList;
