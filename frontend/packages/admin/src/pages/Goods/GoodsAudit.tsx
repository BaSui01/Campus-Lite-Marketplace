/**
 * 商品审核页
 * 
 * 功能：
 * - 展示待审核商品列表
 * - 支持单个商品审核（批准/拒绝）
 * - 支持批量审核
 * - 支持查看商品详情
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
  Space,
  Tag,
  message,
  Modal,
  Form,
  Radio,
  Card,
  Statistic,
  Row,
  Col,
} from 'antd';
import {
  SearchOutlined,
  CheckOutlined,
  CloseOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { goodsService } from '@campus/shared/services/goods';
import type { GoodsResponse } from '@campus/shared/api';

const { TextArea } = Input;

export const GoodsAudit: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();

  // 查询参数
  const [keyword, setKeyword] = useState<string>('');
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(20);

  // 审核弹窗
  const [auditModalVisible, setAuditModalVisible] = useState(false);
  const [currentGoodsId, setCurrentGoodsId] = useState<number | null>(null);

  // 批量审核
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [batchAuditModalVisible, setBatchAuditModalVisible] = useState(false);

  // 查询待审核商品列表（支持后端关键词搜索）
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['goods', 'pending', { keyword, page, size }],
    queryFn: () =>
      goodsService.listPendingGoods({
        keyword,  // ✅ 传递 keyword 给后端
        page,
        size,
      }),
    staleTime: 30000, // 缓存30秒
    refetchInterval: 30000, // 每30秒刷新
  });

  // 单个审核
  const auditMutation = useMutation({
    mutationFn: ({ id, approved, reason }: { id: number; approved: boolean; reason?: string }) =>
      goodsService.approveGoods(id, { approved, reason }),
    onSuccess: () => {
      message.success('审核成功');
      setAuditModalVisible(false);
      setCurrentGoodsId(null);
      form.resetFields();
      refetch();
      queryClient.invalidateQueries({ queryKey: ['goods'] });
    },
    onError: () => {
      message.error('审核失败');
    },
  });

  // 批量审核
  const batchAuditMutation = useMutation({
    mutationFn: async ({ approved, reason }: { approved: boolean; reason?: string }) => {
      const promises = selectedRowKeys.map((id) =>
        goodsService.approveGoods(id as number, { approved, reason })
      );
      return Promise.all(promises);
    },
    onSuccess: () => {
      message.success('批量审核成功');
      setBatchAuditModalVisible(false);
      setSelectedRowKeys([]);
      form.resetFields();
      refetch();
      queryClient.invalidateQueries({ queryKey: ['goods'] });
    },
    onError: () => {
      message.error('批量审核失败');
    },
  });

  // 搜索处理
  const handleSearch = () => {
    setPage(0); // 重置到第一页
    // React Query 会因为 queryKey 变化自动重新请求
  };

  // 重置搜索
  const handleReset = () => {
    setKeyword('');
    setPage(0);
  };

  // 查看详情
  const handleView = (id: number) => {
    navigate(`/admin/goods/${id}`);
  };

  // 打开单个审核弹窗
  const handleOpenAuditModal = (id: number) => {
    setCurrentGoodsId(id);
    form.resetFields();
    form.setFieldsValue({ approved: true });
    setAuditModalVisible(true);
  };

  // 提交单个审核
  const handleAuditSubmit = async () => {
    if (!currentGoodsId) return;

    try {
      const values = await form.validateFields();
      auditMutation.mutate({
        id: currentGoodsId,
        approved: values.approved,
        reason: values.reason,
      });
    } catch (error) {
      console.error('表单校验失败:', error);
    }
  };

  // 打开批量审核弹窗
  const handleOpenBatchAuditModal = (approved: boolean) => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要审核的商品');
      return;
    }
    form.resetFields();
    form.setFieldsValue({ approved });
    setBatchAuditModalVisible(true);
  };

  // 提交批量审核
  const handleBatchAuditSubmit = async () => {
    try {
      const values = await form.validateFields();
      batchAuditMutation.mutate({
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
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '商品图片',
      dataIndex: 'images',
      key: 'images',
      width: 100,
      render: (images: string[]) => (
        <img
          src={images?.[0] || 'https://picsum.photos/60/60?random=2'}
          alt="商品"
          style={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 4 }}
        />
      ),
    },
    {
      title: '商品标题',
      dataIndex: 'title',
      key: 'title',
      width: 200,
      ellipsis: true,
    },
    {
      title: '价格',
      dataIndex: 'price',
      key: 'price',
      width: 100,
      render: (price: number) => `¥${price?.toFixed(2)}`,
    },
    {
      title: '分类',
      dataIndex: 'categoryName',
      key: 'categoryName',
      width: 100,
    },
    {
      title: '卖家',
      dataIndex: 'sellerName',
      key: 'sellerName',
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: () => <Tag color="orange">待审核</Tag>,
    },
    {
      title: '提交时间',
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
      render: (_: any, record: GoodsResponse) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleView(record.id)}
          >
            查看
          </Button>
          <Button
            type="link"
            size="small"
            icon={<CheckOutlined />}
            onClick={() => handleOpenAuditModal(record.id)}
          >
            审核
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="待审核商品数"
              value={data?.totalElements || 0}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="今日提交数"
              value={
                data?.content?.filter((g: GoodsResponse) => {
                  const today = new Date();
                  const createdDate = new Date(g.createdAt);
                  return (
                    createdDate.getFullYear() === today.getFullYear() &&
                    createdDate.getMonth() === today.getMonth() &&
                    createdDate.getDate() === today.getDate()
                  );
                }).length || 0
              }
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="已选择"
              value={selectedRowKeys.length}
              suffix={`/ ${data?.totalElements || 0}`}
            />
          </Card>
        </Col>
      </Row>

      {/* 搜索栏 */}
      <Card style={{ marginBottom: 16 }}>
        <Space>
          <Input
            placeholder="搜索商品标题/描述"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={handleSearch}
            style={{ width: 250 }}
            prefix={<SearchOutlined />}
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
          onClick={() => handleOpenBatchAuditModal(true)}
          disabled={selectedRowKeys.length === 0}
        >
          批量批准 ({selectedRowKeys.length})
        </Button>
        <Button
          danger
          icon={<CloseOutlined />}
          onClick={() => handleOpenBatchAuditModal(false)}
          disabled={selectedRowKeys.length === 0}
        >
          批量拒绝 ({selectedRowKeys.length})
        </Button>
      </Space>

      {/* 商品表格 */}
      <Table
        rowKey="id"
        columns={columns}
        dataSource={data?.content || []}
        loading={isLoading}
        rowSelection={{
          selectedRowKeys,
          onChange: setSelectedRowKeys,
        }}
        pagination={{
          current: page + 1,
          pageSize: size,
          total: data?.totalElements || 0,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条待审核记录`,
          onChange: (p, s) => {
            setPage(p - 1);
            setSize(s);
          },
        }}
        scroll={{ x: 1300 }}
      />

      {/* 单个审核弹窗 */}
      <Modal
        title="审核商品"
        open={auditModalVisible}
        onOk={handleAuditSubmit}
        onCancel={() => {
          setAuditModalVisible(false);
          setCurrentGoodsId(null);
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
            rules={[{ required: true, message: '请选择审核结果' }]}
          >
            <Radio.Group>
              <Radio value={true}>批准（上架）</Radio>
              <Radio value={false}>拒绝（下架）</Radio>
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
        title={`批量审核 (${selectedRowKeys.length}个商品)`}
        open={batchAuditModalVisible}
        onOk={handleBatchAuditSubmit}
        onCancel={() => {
          setBatchAuditModalVisible(false);
          form.resetFields();
        }}
        confirmLoading={batchAuditMutation.isPending}
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
              <Radio value={true}>批准（上架）</Radio>
              <Radio value={false}>拒绝（下架）</Radio>
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
    </div>
  );
};

export default GoodsAudit;
