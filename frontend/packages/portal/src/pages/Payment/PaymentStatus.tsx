/**
 * 支付状态查询页面
 * @author BaSui
 * @description 实时查询订单支付状态，支持轮询和 WebSocket 实时更新
 */

import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { orderService } from '@campus/shared/services';
import { usePayment } from './hooks/usePayment';
import { PaymentProgress } from './components/PaymentProgress';
import { validateOrderNo } from './utils/paymentUtils';

const PaymentStatus: React.FC = () => {
  const { orderNo } = useParams<{ orderNo: string }>();
  const navigate = useNavigate();

  // 参数校验
  useEffect(() => {
    if (!orderNo || !validateOrderNo(orderNo)) {
      toast.error('无效的订单号');
      navigate('/orders');
      return;
    }
  }, [orderNo, navigate]);

  // 查询订单详情
  const {
    data: orderInfo,
    isLoading: orderLoading,
    error: orderError
  } = useQuery({
    queryKey: ['order-detail', orderNo],
    queryFn: () => orderService.getOrderDetail(orderNo!),
    enabled: !!orderNo && validateOrderNo(orderNo),
  });

  // 查询支付状态
  const {
    status: paymentStatus,
    isLoading: paymentLoading,
    error: paymentError,
    refreshStatus,
  } = usePayment({
    orderNo: orderNo!,
    autoPoll: true,
    websocketEnabled: true,
  });

  // 错误处理
  useEffect(() => {
    if (orderError || paymentError) {
      toast.error(orderError?.message || paymentError?.message || '加载失败');
    }
  }, [orderError, paymentError]);

  // 支付成功处理
  useEffect(() => {
    if (paymentStatus === 'SUCCESS' && orderInfo) {
      toast.success('支付成功！');
      // 延迟跳转（2秒后）
      setTimeout(() => {
        navigate(`/payment/result?orderNo=${orderNo}&status=SUCCESS`);
      }, 2000);
    }
  }, [paymentStatus, orderInfo, navigate, orderNo]);

  // 支付失败处理
  useEffect(() => {
    if (paymentStatus === 'FAILED' && orderInfo) {
      navigate(`/payment/result?orderNo=${orderNo}&status=FAILED`);
    }
  }, [paymentStatus, orderInfo, navigate, orderNo]);

  if (orderLoading || paymentLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">正在加载支付信息...</p>
        </div>
      </div>
    );
  }

  if (!orderInfo) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">😕</div>
          <h2 className="text-xl font-semibold mb-2">订单不存在</h2>
          <p className="text-gray-600 mb-4">无法找到该订单信息</p>
          <button
            onClick={() => navigate('/orders')}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
          >
            返回订单列表
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            支付状态查询
          </h1>
          <p className="text-gray-600">
            正在实时查询您的支付状态...
          </p>
        </div>

        <PaymentProgress
          status={paymentStatus}
          orderInfo={orderInfo}
        />

        {/* 操作按钮 */}
        <div className="mt-6 flex justify-center space-x-4">
          <button
            onClick={refreshStatus}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
          >
            手动刷新
          </button>

          <button
            onClick={() => navigate(`/orders/${orderNo}`)}
            className="bg-gray-600 text-white px-6 py-2 rounded-lg hover:bg-gray-700 transition-colors"
          >
            查看订单详情
          </button>
        </div>

        {/* 温馨提示 */}
        <div className="mt-8 bg-blue-50 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-900 mb-3">
            温馨提示
          </h3>
          <ul className="space-y-2 text-blue-800">
            <li>• 支付成功后会自动跳转到结果页面</li>
            <li>• 若长时间未更新，请手动刷新状态</li>
            <li>• 支付过程中请勿关闭页面或重复提交</li>
            <li>• 如遇问题请联系客服</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default PaymentStatus;
