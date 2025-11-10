/**
 * 商品详情页
 * 
 * 功能：
 * - 展示商品完整信息（基本信息、图片、卖家信息）
 * - 显示审核记录时间线
 * - 支持审核操作（批准/拒绝）
 * - 支持上下架操作
 * - 支持删除操作
 * 
 * @author BaSui 😎
 * @date 2025-11-05
 */

import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Descriptions,
  Image,
  Tag,
  Button,
  Space,
  Timeline,
  Modal,
  Form,
  Input,
  Radio,
  message,
  Spin,
  Divider,
  Avatar,
  Statistic,
  Row,
  Col,
  Popconfirm,
  App,
} from 'antd';
import {
  ArrowLeftOutlined,
  CheckOutlined,
  CloseOutlined,
  DeleteOutlined,
  EditOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { goodsService } from '@campus/shared/services/goods';

const { TextArea } = Input;

/**
 * 商品状态映射
 */
const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待审核', color: 'orange' },
  APPROVED: { text: '已上架', color: 'green' },
  REJECTED: { text: '已下架', color: 'red' },
  DELETED: { text: '已删除', color: 'gray' },
};

export const GoodsDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [auditModalVisible, setAuditModalVisible] = useState(false);
  const [form] = Form.useForm();

  // 查询商品详情
  const { data: goods, isLoading } = useQuery({
    queryKey: ['goods', 'detail', id],
    queryFn: () => goodsService.getGoodsDetail(Number(id)),
    enabled: !!id,
  });

  // 审核商品
  const auditMutation = useMutation({
    mutationFn: ({ approved, reason }: { approved: boolean; reason?: string }) =>
      goodsService.approveGoods(Number(id), { approved, reason }),
    onSuccess: () => {
      message.success('审核成功');
      setAuditModalVisible(false);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['goods', 'detail', id] });
      queryClient.invalidateQueries({ queryKey: ['goods'] });
    },
    onError: () => {
      message.error('审核失败');
    },
  });

  // 更新状态（上下架）
  const updateStatusMutation = useMutation({
    mutationFn: (status: string) => goodsService.updateGoodsStatus(Number(id), status),
    onSuccess: () => {
      message.success('操作成功');
      queryClient.invalidateQueries({ queryKey: ['goods', 'detail', id] });
      queryClient.invalidateQueries({ queryKey: ['goods'] });
    },
    onError: () => {
      message.error('操作失败');
    },
  });

  // 删除商品
  const deleteMutation = useMutation({
    mutationFn: () => goodsService.deleteGoods(Number(id)),
    onSuccess: () => {
      message.success('删除成功');
      navigate('/admin/goods/list');
    },
    onError: () => {
      message.error('删除失败');
    },
  });

  // 返回列表
  const handleBack = () => {
    navigate('/admin/goods/list');
  };

  // 打开审核弹窗
  const handleOpenAuditModal = () => {
    form.resetFields();
    setAuditModalVisible(true);
  };

  // 提交审核
  const handleAuditSubmit = async () => {
    try {
      const values = await form.validateFields();
      auditMutation.mutate({
        approved: values.approved,
        reason: values.reason,
      });
    } catch (error) {
      console.error('表单校验失败:', error);
    }
  };

  // 上架/下架
  const handleToggleStatus = () => {
    const targetStatus = goods?.status === 'APPROVED' ? 'REJECTED' : 'APPROVED';
    Modal.confirm({
      title: `确认${targetStatus === 'APPROVED' ? '上架' : '下架'}`,
      content: `确定要${targetStatus === 'APPROVED' ? '上架' : '下架'}这个商品吗？`,
      onOk: () => {
        updateStatusMutation.mutate(targetStatus);
      },
    });
  };

  // 删除商品
  const handleDelete = () => {
    deleteMutation.mutate();
  };

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!goods) {
    return (
      <Card>
        <div style={{ textAlign: 'center', padding: '50px 0' }}>
          <p>商品不存在或已被删除</p>
          <Button type="primary" onClick={handleBack}>
            返回列表
          </Button>
        </div>
      </Card>
    );
  }

  const statusInfo = STATUS_MAP[goods.status] || { text: goods.status, color: 'default' };

  return (
    <div style={{ padding: 24 }}>
      {/* 顶部操作栏 */}
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={handleBack}>
          返回列表
        </Button>
        {goods.status === 'PENDING' && (
          <Button type="primary" icon={<CheckOutlined />} onClick={handleOpenAuditModal}>
            审核商品
          </Button>
        )}
        {(goods.status === 'APPROVED' || goods.status === 'REJECTED') && (
          <Button
            icon={goods.status === 'APPROVED' ? <CloseOutlined /> : <CheckOutlined />}
            onClick={handleToggleStatus}
          >
            {goods.status === 'APPROVED' ? '下架' : '上架'}
          </Button>
        )}
        <Popconfirm
          title="确定要删除这个商品吗？"
          description="此操作不可恢复！"
          onConfirm={handleDelete}
          okText="确定"
          cancelText="取消"
        >
          <Button danger icon={<DeleteOutlined />}>
            删除商品
          </Button>
        </Popconfirm>
      </Space>

      {/* 商品基本信息卡片 */}
      <Card title="商品基本信息" style={{ marginBottom: 16 }}>
        <Descriptions column={2} bordered>
          <Descriptions.Item label="商品ID">{goods.id}</Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={statusInfo.color}>{statusInfo.text}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="商品标题" span={2}>
            {goods.title}
          </Descriptions.Item>
          <Descriptions.Item label="商品描述" span={2}>
            {goods.description || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="价格">
            <span style={{ fontSize: 18, color: '#f5222d', fontWeight: 'bold' }}>
              ¥{goods.price?.toFixed(2)}
            </span>
          </Descriptions.Item>
          <Descriptions.Item label="库存">{goods.stock || 1}</Descriptions.Item>
          <Descriptions.Item label="分类">{goods.categoryName || '-'}</Descriptions.Item>
          <Descriptions.Item label="标签">
            {goods.tags?.map((tag: any) => (
              <Tag key={tag.id} color="blue">
                {tag.name}
              </Tag>
            )) || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="浏览量">{goods.viewCount || 0}</Descriptions.Item>
          <Descriptions.Item label="收藏量">{goods.favoriteCount || 0}</Descriptions.Item>
          <Descriptions.Item label="创建时间">
            {new Date(goods.createdAt).toLocaleString('zh-CN')}
          </Descriptions.Item>
          <Descriptions.Item label="更新时间">
            {new Date(goods.updatedAt).toLocaleString('zh-CN')}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* 商品图片 */}
      <Card title="商品图片" style={{ marginBottom: 16 }}>
        <Image.PreviewGroup>
          <Space size={16} wrap>
            {goods.images?.map((img: string, index: number) => (
              <Image
                key={index}
                src={img}
                alt={`商品图片${index + 1}`}
                width={150}
                height={150}
                style={{ objectFit: 'cover', borderRadius: 8 }}
              />
            )) || <p>暂无图片</p>}
          </Space>
        </Image.PreviewGroup>
      </Card>

      {/* 卖家信息卡片 */}
      <Card title="卖家信息" style={{ marginBottom: 16 }}>
        <Row gutter={16}>
          <Col span={12}>
            <Space>
              <Avatar size={64} icon={<UserOutlined />} src={goods.sellerAvatar} />
              <div>
                <h3>{goods.sellerName}</h3>
                <p style={{ color: '#8c8c8c', margin: 0 }}>
                  联系方式：{goods.sellerPhone || '未提供'}
                </p>
              </div>
            </Space>
          </Col>
          <Col span={6}>
            <Statistic title="信用评分" value={goods.sellerCreditScore || 0} suffix="/ 100" />
          </Col>
          <Col span={6}>
            <Statistic title="成交量" value={goods.sellerOrderCount || 0} suffix="笔" />
          </Col>
        </Row>
      </Card>

      {/* 审核记录时间线 */}
      <Card title="审核记录">
        {goods.auditHistory && goods.auditHistory.length > 0 ? (
          <Timeline>
            {goods.auditHistory.map((record: any, index: number) => (
              <Timeline.Item
                key={index}
                color={record.approved ? 'green' : 'red'}
              >
                <p>
                  <strong>{record.reviewerName}</strong>{' '}
                  {record.approved ? '批准' : '拒绝'}了审核
                </p>
                {record.reason && <p style={{ color: '#8c8c8c' }}>原因：{record.reason}</p>}
                <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                  {new Date(record.createdAt).toLocaleString('zh-CN')}
                </p>
              </Timeline.Item>
            ))}
            <Timeline.Item color="blue">
              <p>
                <strong>{goods.sellerName}</strong> 发布了商品
              </p>
              <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                {new Date(goods.createdAt).toLocaleString('zh-CN')}
              </p>
            </Timeline.Item>
          </Timeline>
        ) : (
          <p style={{ color: '#8c8c8c' }}>暂无审核记录</p>
        )}
      </Card>

      {/* 审核弹窗 */}
      <Modal
        title="审核商品"
        open={auditModalVisible}
        onOk={handleAuditSubmit}
        onCancel={() => {
          setAuditModalVisible(false);
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
    </div>
  );
};

export default GoodsDetail;
