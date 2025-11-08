/**
 * 支付结果页面
 * @author BaSui 😎
 * @description 显示支付成功/失败/超时结果页面
 */

import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { orderService } from '../../../shared/src/services/order';
import { ResultCard } from './components/ResultCard';
import { validateOrderNo } from './utils/paymentUtils';

const PaymentResult: React.FC = () => {
  const { orderNo, status } = useParams<{
    orderNo: string;
    status: 'SUCCESS' | 'FAILED' | 'TIMEOUT';
  }>();
  const navigate = useNavigate();

  // 参数验证
  React.useEffect(() => {
    if (!orderNo || !validateOrderNo(orderNo)) {
      toast.error('无效的订单号');
      navigate('/orders');
      return;
    }

    if (!status || !['SUCCESS', 'FAILED', 'TIMEOUT'].includes(status)) {
      toast.error('无效的支付状态');
      navigate('/orders');
      return;
    }
  }, [orderNo, status, navigate]);

  // 获取订单详情
  const {
    data: orderInfo,
    isLoading,
    error
  } = useQuery({
    queryKey: ['order-detail', orderNo],
    queryFn: () => orderService.getOrderDetail(orderNo!),
    enabled: !!orderNo && validateOrderNo(orderNo),
  });

  // 处理主操作
  const handlePrimaryAction = () => {
    if (status === 'SUCCESS') {
      // 查看订单详情
      navigate(`/order/${orderNo}`);
    } else if (status === 'FAILED') {
      // 重新支付
      navigate(`/order/${orderNo}`);
    } else if (status === 'TIMEOUT') {
      // 重新下单,返回商品详情页
      if (orderInfo?.goodsId) {
        navigate(`/goods/${orderInfo.goodsId}`);
      } else {
        navigate('/goods');
      }
    }
  };

  // 处理次操作
  const handleSecondaryAction = () => {
    if (status === 'SUCCESS') {
      // 继续购物
      navigate('/goods');
    } else {
      // 联系客服
      window.open('/customer-service', '_blank');
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">正在加载支付结果...</p>
        </div>
      </div>
    );
  }

  if (error || !orderInfo) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">😔</div>
          <h2 className="text-xl font-semibold mb-2">加载失败</h2>
          <p className="text-gray-600 mb-4">
            {error?.message || '无法获取订单信息'}
          </p>
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
      <div className="max-w-2xl mx-auto px-4">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            支付结果
          </h1>
          <p className="text-gray-600">
            订单 {orderNo} 的支付{status === 'SUCCESS' ? '成功' : status === 'FAILED' ? '失败' : '超时'}
          </p>
        </div>

        <ResultCard
          status={status || 'FAILED'}
          orderInfo={orderInfo}
          onPrimaryAction={handlePrimaryAction}
          onSecondaryAction={handleSecondaryAction}
        />

        {/* 推荐商品区域 - 仅支付成功时显示 */}
        {status === 'SUCCESS' && (
          <div className="mt-8">
            <h2 className="text-xl font-semibold mb-4 text-gray-900">为您推荐</h2>
            <div className="bg-white rounded-lg shadow p-6">
              <div className="text-center text-gray-500">
                <div className="text-4xl mb-4">🛍️</div>
                <p>更多优质商品即将推送给您</p>
                <button
                  onClick={() => navigate('/goods')}
                  className="mt-4 bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
                >
                  去逛逛
                </button>
              </div>
            </div>
          </div>
        )}

        {/* 温馨提示 */}
        <div className="mt-8 bg-blue-50 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-900 mb-3">
            😊 温馨提示:
          </h3>
          <div className="space-y-2 text-blue-800">
            {status === 'SUCCESS' && (
              <>
                <li>✓ 支付成功后,请等待商家发货</li>
                <li>✓ 您可以在"我的订单"中查看物流信息</li>
                <li>✓ 收到商品后,请及时确认收货并评价</li>
              </>
            )}
            {status === 'FAILED' && (
              <>
                <li>✓ 支付失败可能是余额不足或网络异常</li>
                <li>✓ 您可以重新选择支付方式进行支付</li>
                <li>✓ 如多次支付失败,请联系客服协助</li>
              </>
            )}
            {status === 'TIMEOUT' && (
              <>
                <li>✓ 支付超时订单已自动取消</li>
                <li>✓ 您可以重新下单购买该商品</li>
                <li>✓ 如有疑问,请联系客服</li>
              </>
            )}
            <li>✓ 如有任何问题,欢迎随时联系客服</li>
          </div>
        </div>

        {/* 返回按钮 */}
        <div className="mt-8 text-center">
          <button
            onClick={() => navigate('/orders')}
            className="text-blue-600 hover:text-blue-800 font-medium"
          >
            查看我的订单 →
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentResult;
