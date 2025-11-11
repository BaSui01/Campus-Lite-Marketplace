/**
 * 支付页面 💳
 * @author BaSui 😎
 * @description 支付方式选择、二维码展示、支付宝跳转、支付状态监听
 */

import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { orderService } from '@campus/shared/services';
import { PayOrderRequestPaymentMethodEnum } from '@campus/shared/api/models';
import { PaymentMethodSelector } from './components/PaymentMethodSelector';
import { WechatPayQRCode } from './components/WechatPayQRCode';
import { AlipayRedirect } from './components/AlipayRedirect';
import { PaymentStatusPoller } from './components/PaymentStatusPoller';
import './Payment.css';

type PaymentMethod = 'WECHAT' | 'ALIPAY' | 'BANK_CARD';

interface PaymentResponse {
  orderNo: string;
  paymentUrl: string;
  qrCode?: string;
  expireSeconds: number;
}

export const Payment: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const orderNo = searchParams.get('orderNo');

  const [selectedMethod, setSelectedMethod] = useState<PaymentMethod | null>(null);
  const [paymentData, setPaymentData] = useState<PaymentResponse | null>(null);
  const [isPaymentStarted, setIsPaymentStarted] = useState(false);

  // 参数校验
  useEffect(() => {
    if (!orderNo) {
      toast.error('缺少订单号参数');
      navigate('/orders');
    }
  }, [orderNo, navigate]);

  // 查询订单详情
  const {
    data: orderDetail,
    isLoading: orderLoading,
    error: orderError,
  } = useQuery({
    queryKey: ['order-detail', orderNo],
    queryFn: () => orderService.getOrderDetail(orderNo!),
    enabled: !!orderNo,
  });

  // 调用支付接口
  const payMutation = useMutation({
    mutationFn: async (method: PaymentMethod) => {
      if (!orderNo) throw new Error('订单号不存在');
      
      // 银行卡支付提示开发中
      if (method === 'BANK_CARD') {
        toast.info('银行卡支付功能开发中，敬请期待！');
        throw new Error('银行卡支付暂未开放');
      }

      // 🎯 使用新的支付接口：POST /api/orders/{orderNo}/pay
      const response = await orderService.payOrder(orderNo, {
        orderNo,
        paymentMethod: method as PayOrderRequestPaymentMethodEnum,
      });

      return {
        orderNo: response.orderNo,
        paymentUrl: response.paymentUrl,
        qrCode: response.qrCode || (method === 'WECHAT' ? response.paymentUrl : undefined),
        expireSeconds: response.expireSeconds || 1800, // 默认 30 分钟
      };
    },
    onSuccess: (data) => {
      setPaymentData(data);
      setIsPaymentStarted(true);
      toast.success('支付订单创建成功！');
    },
    onError: (error: any) => {
      toast.error(error?.message || '支付创建失败，请重试');
    },
  });

  // 处理支付方式选择
  const handlePaymentMethodSelect = (method: PaymentMethod) => {
    setSelectedMethod(method);
  };

  // 处理立即支付
  const handlePay = () => {
    if (!selectedMethod) {
      toast.warning('请选择支付方式');
      return;
    }

    payMutation.mutate(selectedMethod);
  };

  // 处理支付成功
  const handlePaymentSuccess = () => {
    toast.success('支付成功！正在跳转...');
    setTimeout(() => {
      navigate(`/order/${orderNo}`);
    }, 1500);
  };

  // 处理支付失败
  const handlePaymentFailed = () => {
    toast.error('支付失败或已取消');
    setIsPaymentStarted(false);
    setPaymentData(null);
    setSelectedMethod(null);
  };

  // 错误处理
  useEffect(() => {
    if (orderError) {
      toast.error('订单加载失败');
      navigate('/orders');
    }
  }, [orderError, navigate]);

  if (orderLoading) {
    return (
      <div className="payment-page">
        <div className="payment-loading">
          <div className="spinner"></div>
          <p>正在加载订单信息...</p>
        </div>
      </div>
    );
  }

  if (!orderDetail) {
    return (
      <div className="payment-page">
        <div className="payment-error">
          <h2>订单不存在</h2>
          <button onClick={() => navigate('/orders')}>返回订单列表</button>
        </div>
      </div>
    );
  }

  return (
    <div className="payment-page">
      <div className="payment-container">
        <h1 className="payment-title">订单支付</h1>

        {/* 订单信息摘要 */}
        <div className="order-summary">
          <div className="summary-item">
            <span className="label">订单号：</span>
            <span className="value">{orderDetail.orderNo}</span>
          </div>
          <div className="summary-item">
            <span className="label">订单金额：</span>
            <span className="value price">¥{orderDetail.amount?.toFixed(2)}</span>
          </div>
        </div>

        {/* 支付流程区域 */}
        {!isPaymentStarted ? (
          <>
            {/* 支付方式选择 */}
            <PaymentMethodSelector
              selectedMethod={selectedMethod}
              onSelect={handlePaymentMethodSelect}
            />

            {/* 立即支付按钮 */}
            <div className="payment-actions">
              <button
                className="btn-cancel"
                onClick={() => navigate(`/order/${orderNo}`)}
              >
                取消支付
              </button>
              <button
                className="btn-pay"
                onClick={handlePay}
                disabled={!selectedMethod || payMutation.isPending}
              >
                {payMutation.isPending ? '创建中...' : '立即支付'}
              </button>
            </div>
          </>
        ) : (
          <>
            {/* 微信支付二维码 */}
            {selectedMethod === 'WECHAT' && paymentData?.qrCode && (
              <WechatPayQRCode
                qrCodeUrl={paymentData.qrCode}
                expireSeconds={paymentData.expireSeconds}
                onCancel={() => {
                  setIsPaymentStarted(false);
                  setPaymentData(null);
                }}
              />
            )}

            {/* 支付宝跳转 */}
            {selectedMethod === 'ALIPAY' && paymentData?.paymentUrl && (
              <AlipayRedirect
                paymentHtml={paymentData.paymentUrl}
                onCancel={() => {
                  setIsPaymentStarted(false);
                  setPaymentData(null);
                }}
              />
            )}

            {/* 支付状态轮询 */}
            {orderNo && (
              <PaymentStatusPoller
                orderNo={orderNo}
                onSuccess={handlePaymentSuccess}
                onFailed={handlePaymentFailed}
              />
            )}
          </>
        )}

        {/* 温馨提示 */}
        <div className="payment-tips">
          <h3>温馨提示</h3>
          <ul>
            <li>支付成功后会自动跳转到订单详情页</li>
            <li>请在 30 分钟内完成支付，超时订单将自动取消</li>
            <li>如遇问题请联系客服：400-123-4567</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Payment;
