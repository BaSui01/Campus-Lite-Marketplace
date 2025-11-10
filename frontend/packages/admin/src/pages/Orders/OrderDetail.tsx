/**
 * 订单详情页
 * 
 * 功能：
 * - 展示订单完整信息（订单信息、商品信息、买卖家信息、支付信息、物流信息）
 * - 订单状态时间线
 * - 支持取消订单（管理员）
 * - 支持强制完成订单（管理员）
 * - 支持查看退款信息
 * 
 * @author BaSui 😎
 * @date 2025-11-05
 */

import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Descriptions,
  Button,
  Space,
  Timeline,
  Modal,
  Form,
  Input,
  message,
  Spin,
  Tag,
  Row,
  Col,
  Avatar,
  Image,
  Divider,
  App,
} from 'antd';
import {
  ArrowLeftOutlined,
  CloseOutlined,
  CheckOutlined,
  UserOutlined,
  ShoppingOutlined,
  DollarOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { orderService } from '@campus/shared/services/order';

const { TextArea } = Input;

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

export const OrderDetail: React.FC = () => {
  const { orderNo } = useParams<{ orderNo: string }>();
  const navigate = useNavigate();
  const { modal } = App.useApp();
  const queryClient = useQueryClient();

  const [cancelModalVisible, setCancelModalVisible] = useState(false);
  const [form] = Form.useForm();

  // 查询订单详情
  const { data: order, isLoading } = useQuery({
    queryKey: ['order', 'detail', orderNo],
    queryFn: async () => {
      const response = await orderService.getOrderByNo(orderNo!);
      return response.data;
    },
    enabled: !!orderNo,
  });

  // 取消订单（管理员）
  const cancelMutation = useMutation({
    mutationFn: (reason: string) => orderService.cancelOrderAdmin(orderNo!, reason),
    onSuccess: () => {
      message.success('订单已取消');
      setCancelModalVisible(false);
      form.resetFields();
      queryClient.invalidateQueries({ queryKey: ['order', 'detail', orderNo] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
    onError: () => {
      message.error('取消订单失败');
    },
  });

  // 强制完成订单（管理员）
  const forceCompleteMutation = useMutation({
    mutationFn: () => orderService.forceCompleteOrder(orderNo!),
    onSuccess: () => {
      message.success('订单已强制完成');
      queryClient.invalidateQueries({ queryKey: ['order', 'detail', orderNo] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
    onError: () => {
      message.error('操作失败');
    },
  });

  // 返回列表
  const handleBack = () => {
    navigate('/admin/orders/list');
  };

  // 打开取消订单弹窗
  const handleOpenCancelModal = () => {
    form.resetFields();
    setCancelModalVisible(true);
  };

  // 提交取消订单
  const handleCancelSubmit = async () => {
    try {
      const values = await form.validateFields();
      cancelMutation.mutate(values.reason);
    } catch (error) {
      console.error('表单校验失败:', error);
    }
  };

  // 强制完成订单
  const handleForceComplete = () => {
    modal.confirm({
      title: '确认强制完成订单',
      content: '确定要强制完成这个订单吗？此操作不可撤销！',
      okType: 'danger',
      onOk: () => {
        forceCompleteMutation.mutate();
      },
    });
  };

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!order) {
    return (
      <Card>
        <div style={{ textAlign: 'center', padding: '50px 0' }}>
          <p>订单不存在或已被删除</p>
          <Button type="primary" onClick={handleBack}>
            返回列表
          </Button>
        </div>
      </Card>
    );
  }

  const statusInfo = STATUS_MAP[order.status] || { text: order.status, color: 'default' };

  return (
    <div style={{ padding: 24 }}>
      {/* 顶部操作栏 */}
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={handleBack}>
          返回列表
        </Button>
        {(order.status === 'PENDING_PAYMENT' || order.status === 'PAID') && (
          <Button danger icon={<CloseOutlined />} onClick={handleOpenCancelModal}>
            取消订单
          </Button>
        )}
        {order.status === 'SHIPPED' && (
          <Button
            type="primary"
            icon={<CheckOutlined />}
            onClick={handleForceComplete}
          >
            强制完成
          </Button>
        )}
      </Space>

      {/* 订单基本信息卡片 */}
      <Card title="订单信息" style={{ marginBottom: 16 }}>
        <Descriptions column={2} bordered>
          <Descriptions.Item label="订单号">{order.orderNo}</Descriptions.Item>
          <Descriptions.Item label="状态">
            <Tag color={statusInfo.color}>{statusInfo.text}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="创建时间">
            {new Date(order.createdAt).toLocaleString('zh-CN')}
          </Descriptions.Item>
          <Descriptions.Item label="更新时间">
            {new Date(order.updatedAt).toLocaleString('zh-CN')}
          </Descriptions.Item>
          {order.paidAt && (
            <Descriptions.Item label="支付时间">
              {new Date(order.paidAt).toLocaleString('zh-CN')}
            </Descriptions.Item>
          )}
          {order.shippedAt && (
            <Descriptions.Item label="发货时间">
              {new Date(order.shippedAt).toLocaleString('zh-CN')}
            </Descriptions.Item>
          )}
          {order.completedAt && (
            <Descriptions.Item label="完成时间">
              {new Date(order.completedAt).toLocaleString('zh-CN')}
            </Descriptions.Item>
          )}
          {order.cancelledAt && (
            <Descriptions.Item label="取消时间">
              {new Date(order.cancelledAt).toLocaleString('zh-CN')}
            </Descriptions.Item>
          )}
        </Descriptions>
      </Card>

      {/* 商品信息卡片 */}
      <Card title="商品信息" style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col span={4}>
            <Image
              src={order.goodsImage || 'https://picsum.photos/150/150?random=5'}
              alt={order.goodsTitle}
              width={150}
              height={150}
              style={{ objectFit: 'cover', borderRadius: 8 }}
            />
          </Col>
          <Col span={20}>
            <Descriptions column={2}>
              <Descriptions.Item label="商品标题" span={2}>
                {order.goodsTitle}
              </Descriptions.Item>
              <Descriptions.Item label="商品ID">{order.goodsId}</Descriptions.Item>
              <Descriptions.Item label="商品价格">
                <span style={{ fontSize: 18, color: '#f5222d', fontWeight: 'bold' }}>
                  ¥{order.goodsPrice?.toFixed(2)}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="购买数量">{order.quantity || 1}</Descriptions.Item>
              <Descriptions.Item label="订单总金额">
                <span style={{ fontSize: 20, color: '#f5222d', fontWeight: 'bold' }}>
                  ¥{order.totalAmount?.toFixed(2)}
                </span>
              </Descriptions.Item>
            </Descriptions>
          </Col>
        </Row>
      </Card>

      <Row gutter={16} style={{ marginBottom: 16 }}>
        {/* 买家信息卡片 */}
        <Col span={12}>
          <Card title="买家信息">
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <Space>
                <Avatar size={64} icon={<UserOutlined />} src={order.buyerAvatar} />
                <div>
                  <h3>{order.buyerName}</h3>
                  <p style={{ color: '#8c8c8c', margin: 0 }}>
                    联系方式：{order.buyerPhone || '未提供'}
                  </p>
                </div>
              </Space>
              {order.shippingAddress && (
                <div>
                  <Divider style={{ margin: '12px 0' }} />
                  <p style={{ margin: 0 }}>
                    <strong>收货地址：</strong>
                    {order.shippingAddress}
                  </p>
                </div>
              )}
            </Space>
          </Card>
        </Col>

        {/* 卖家信息卡片 */}
        <Col span={12}>
          <Card title="卖家信息">
            <Space>
              <Avatar size={64} icon={<UserOutlined />} src={order.sellerAvatar} />
              <div>
                <h3>{order.sellerName}</h3>
                <p style={{ color: '#8c8c8c', margin: 0 }}>
                  联系方式：{order.sellerPhone || '未提供'}
                </p>
              </div>
            </Space>
          </Card>
        </Col>
      </Row>

      {/* 支付信息卡片 */}
      {(order.status !== 'PENDING_PAYMENT' && order.status !== 'CANCELLED') && (
        <Card title="支付信息" style={{ marginBottom: 16 }}>
          <Descriptions column={2} bordered>
            <Descriptions.Item label="支付方式">
              {order.paymentMethod === 'ALIPAY' ? '支付宝' : order.paymentMethod === 'WECHAT' ? '微信支付' : order.paymentMethod || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="支付金额">
              <span style={{ color: '#f5222d', fontWeight: 'bold' }}>
                ¥{order.totalAmount?.toFixed(2)}
              </span>
            </Descriptions.Item>
            <Descriptions.Item label="支付时间">
              {order.paidAt ? new Date(order.paidAt).toLocaleString('zh-CN') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="交易流水号">
              {order.transactionId || '-'}
            </Descriptions.Item>
          </Descriptions>
        </Card>
      )}

      {/* 物流信息卡片 */}
      {(order.status === 'SHIPPED' || order.status === 'COMPLETED') && (
        <Card title="物流信息" style={{ marginBottom: 16 }}>
          <Descriptions column={2} bordered>
            <Descriptions.Item label="物流公司">
              {order.logisticsCompany || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="运单号">
              {order.trackingNumber || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="物流状态" span={2}>
              {order.logisticsStatus || '运输中'}
            </Descriptions.Item>
          </Descriptions>
        </Card>
      )}

      {/* 订单状态时间线 */}
      <Card title="订单状态时间线">
        <Timeline>
          {order.completedAt && (
            <Timeline.Item color="green">
              <p><strong>订单已完成</strong></p>
              <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                {new Date(order.completedAt).toLocaleString('zh-CN')}
              </p>
            </Timeline.Item>
          )}
          {order.shippedAt && (
            <Timeline.Item color="cyan">
              <p><strong>卖家已发货</strong></p>
              <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                {new Date(order.shippedAt).toLocaleString('zh-CN')}
              </p>
            </Timeline.Item>
          )}
          {order.paidAt && (
            <Timeline.Item color="blue">
              <p><strong>买家已支付</strong></p>
              <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                {new Date(order.paidAt).toLocaleString('zh-CN')}
              </p>
            </Timeline.Item>
          )}
          <Timeline.Item color={order.status === 'CANCELLED' ? 'red' : 'gray'}>
            <p><strong>订单已创建</strong></p>
            <p style={{ color: '#8c8c8c', fontSize: 12 }}>
              {new Date(order.createdAt).toLocaleString('zh-CN')}
            </p>
          </Timeline.Item>
          {order.cancelledAt && (
            <Timeline.Item color="red">
              <p><strong>订单已取消</strong></p>
              {order.cancelReason && (
                <p style={{ color: '#8c8c8c' }}>原因：{order.cancelReason}</p>
              )}
              <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                {new Date(order.cancelledAt).toLocaleString('zh-CN')}
              </p>
            </Timeline.Item>
          )}
        </Timeline>
      </Card>

      {/* 取消订单弹窗 */}
      <Modal
        title="取消订单"
        open={cancelModalVisible}
        onOk={handleCancelSubmit}
        onCancel={() => {
          setCancelModalVisible(false);
          form.resetFields();
        }}
        confirmLoading={cancelMutation.isPending}
        okText="确认取消"
        cancelText="返回"
        okType="danger"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="reason"
            label="取消原因"
            rules={[
              { required: true, message: '请填写取消原因' },
              { max: 200, message: '取消原因不能超过200字' },
            ]}
          >
            <TextArea
              rows={4}
              placeholder="请填写取消原因（必填，最多200字）"
              showCount
              maxLength={200}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default OrderDetail;
