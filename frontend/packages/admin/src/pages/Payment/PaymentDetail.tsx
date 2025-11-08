/**
 * 支付详情页
 * 
 * 功能：
 * - 查看支付详情
 * - 查看交易流水
 * - 查看订单关联信息
 * - 查看买家信息
 * - 查看商品信息
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Descriptions,
  Button,
  Space,
  Tag,
  Spin,
  Alert,
  Timeline,
  Divider,
} from 'antd';
import {
  ArrowLeftOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  DollarOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { paymentService, orderService } from '@campus/shared';
import dayjs from 'dayjs';

/**
 * 支付状态映射
 */
const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING_PAYMENT: { text: '待支付', color: 'orange' },
  PAID: { text: '已支付', color: 'blue' },
  SHIPPED: { text: '已发货', color: 'cyan' },
  COMPLETED: { text: '已完成', color: 'green' },
  CANCELLED: { text: '已取消', color: 'default' },
  REFUNDED: { text: '已退款', color: 'red' },
};

/**
 * 支付方式映射
 */
const PAYMENT_METHOD_MAP: Record<string, { text: string; color: string }> = {
  WECHAT_PAY: { text: '微信支付', color: 'green' },
  ALIPAY: { text: '支付宝', color: 'blue' },
  BALANCE: { text: '余额支付', color: 'orange' },
};

export const PaymentDetail: React.FC = () => {
  const { orderNo } = useParams<{ orderNo: string }>();
  const navigate = useNavigate();

  // 查询支付详情
  const { data: payment, isLoading, error } = useQuery({
    queryKey: ['payment', 'detail', orderNo],
    queryFn: () => paymentService.getPaymentDetail(orderNo!),
    enabled: !!orderNo,
  });

  // 查询订单详情（获取更多信息）
  const { data: order } = useQuery({
    queryKey: ['order', 'detail', orderNo],
    queryFn: () => orderService.getOrderDetail(orderNo!),
    enabled: !!orderNo,
  });

  // 返回列表
  const handleBack = () => {
    navigate('/admin/payments/list');
  };

  if (isLoading) {
    return (
      <div style={{ padding: '24px', textAlign: 'center' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  if (error || !payment) {
    return (
      <div style={{ padding: '24px' }}>
        <Alert
          message="加载失败"
          description="无法加载支付详情，请稍后重试"
          type="error"
          showIcon
        />
        <Button
          type="primary"
          icon={<ArrowLeftOutlined />}
          onClick={handleBack}
          style={{ marginTop: 16 }}
        >
          返回列表
        </Button>
      </div>
    );
  }

  const statusConfig = STATUS_MAP[payment.status] || { text: payment.status, color: 'default' };
  const methodConfig = PAYMENT_METHOD_MAP[order?.paymentMethod as string] || { 
    text: order?.paymentMethod, 
    color: 'default' 
  };

  return (
    <div style={{ padding: '24px' }}>
      {/* 页面头部 */}
      <Space style={{ marginBottom: 24 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={handleBack}>
          返回
        </Button>
      </Space>

      {/* 支付状态卡片 */}
      <Card style={{ marginBottom: 24 }}>
        <div style={{ textAlign: 'center', padding: '24px 0' }}>
          {payment.status === 'PAID' || payment.status === 'SHIPPED' || payment.status === 'COMPLETED' ? (
            <CheckCircleOutlined style={{ fontSize: 64, color: '#52c41a' }} />
          ) : payment.status === 'REFUNDED' ? (
            <CloseCircleOutlined style={{ fontSize: 64, color: '#f5222d' }} />
          ) : (
            <DollarOutlined style={{ fontSize: 64, color: '#faad14' }} />
          )}
          <h2 style={{ marginTop: 16, marginBottom: 8 }}>
            <Tag color={statusConfig.color} style={{ fontSize: 18, padding: '8px 16px' }}>
              {statusConfig.text}
            </Tag>
          </h2>
          <p style={{ fontSize: 32, fontWeight: 'bold', color: '#f5222d', margin: '16px 0' }}>
            ¥{payment.amount / 100}
          </p>
          <p style={{ color: '#8c8c8c' }}>订单号：{payment.orderNo}</p>
        </div>
      </Card>

      {/* 支付信息 */}
      <Card title="支付信息" style={{ marginBottom: 24 }}>
        <Descriptions column={2} bordered>
          <Descriptions.Item label="订单号">{payment.orderNo}</Descriptions.Item>
          <Descriptions.Item label="支付方式">
            <Tag color={methodConfig.color}>{methodConfig.text}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="支付金额">
            <span style={{ color: '#f5222d', fontWeight: 'bold', fontSize: 16 }}>
              ¥{payment.amount / 100}
            </span>
          </Descriptions.Item>
          <Descriptions.Item label="支付状态">
            <Tag color={statusConfig.color}>{statusConfig.text}</Tag>
          </Descriptions.Item>
          {payment.transactionId && (
            <Descriptions.Item label="第三方交易号" span={2}>
              {payment.transactionId}
            </Descriptions.Item>
          )}
          <Descriptions.Item label="创建时间">
            {dayjs(payment.createdAt).format('YYYY-MM-DD HH:mm:ss')}
          </Descriptions.Item>
          {payment.paidAt && (
            <Descriptions.Item label="支付时间">
              {dayjs(payment.paidAt).format('YYYY-MM-DD HH:mm:ss')}
            </Descriptions.Item>
          )}
        </Descriptions>
      </Card>

      {/* 订单信息 */}
      {order && (
        <Card title="订单信息" style={{ marginBottom: 24 }}>
          <Descriptions column={2} bordered>
            <Descriptions.Item label="商品名称" span={2}>
              {order.goodsTitle}
            </Descriptions.Item>
            <Descriptions.Item label="买家">
              {order.buyerUsername}
            </Descriptions.Item>
            <Descriptions.Item label="卖家">
              {order.sellerUsername}
            </Descriptions.Item>
            <Descriptions.Item label="商品价格">
              ¥{order.goodsPrice?.toFixed(2)}
            </Descriptions.Item>
            <Descriptions.Item label="运费">
              ¥{order.shippingFee?.toFixed(2)}
            </Descriptions.Item>
            {order.shippingAddress && (
              <Descriptions.Item label="收货地址" span={2}>
                {order.shippingAddress}
              </Descriptions.Item>
            )}
            {order.buyerNote && (
              <Descriptions.Item label="买家留言" span={2}>
                {order.buyerNote}
              </Descriptions.Item>
            )}
          </Descriptions>
        </Card>
      )}

      {/* 交易时间线 */}
      <Card title="交易流水">
        <Timeline
          items={[
            {
              color: 'green',
              children: (
                <>
                  <p>订单创建</p>
                  <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                    {dayjs(payment.createdAt).format('YYYY-MM-DD HH:mm:ss')}
                  </p>
                </>
              ),
            },
            ...(payment.paidAt
              ? [
                  {
                    color: 'blue',
                    children: (
                      <>
                        <p>支付成功</p>
                        <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                          {dayjs(payment.paidAt).format('YYYY-MM-DD HH:mm:ss')}
                        </p>
                        <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                          支付方式：{methodConfig.text}
                        </p>
                      </>
                    ),
                  },
                ]
              : []),
            ...(order?.shippedAt
              ? [
                  {
                    color: 'cyan',
                    children: (
                      <>
                        <p>商品发货</p>
                        <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                          {dayjs(order.shippedAt).format('YYYY-MM-DD HH:mm:ss')}
                        </p>
                      </>
                    ),
                  },
                ]
              : []),
            ...(order?.completedAt
              ? [
                  {
                    color: 'green',
                    children: (
                      <>
                        <p>交易完成</p>
                        <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                          {dayjs(order.completedAt).format('YYYY-MM-DD HH:mm:ss')}
                        </p>
                      </>
                    ),
                  },
                ]
              : []),
            ...(payment.status === 'REFUNDED'
              ? [
                  {
                    color: 'red',
                    children: (
                      <>
                        <p>已退款</p>
                        <p style={{ color: '#8c8c8c', fontSize: 12 }}>
                          退款金额：¥{payment.amount / 100}
                        </p>
                      </>
                    ),
                  },
                ]
              : []),
            ...(payment.status === 'CANCELLED'
              ? [
                  {
                    color: 'gray',
                    children: (
                      <>
                        <p>订单取消</p>
                      </>
                    ),
                  },
                ]
              : []),
          ]}
        />
      </Card>
    </div>
  );
};

export default PaymentDetail;
