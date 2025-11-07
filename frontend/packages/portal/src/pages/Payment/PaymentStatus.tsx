/**
 * /Ø¶åâub
 * @author BaSui =
 * @description žö>:¢U/Ø¶/nâŒWebSocketô°
 */

import React, { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { orderService } from '../../../shared/src/services/order';
import { usePayment } from './hooks/usePayment';
import { PaymentProgress } from './components/PaymentProgress';
import { validateOrderNo } from './utils/paymentUtils';

const PaymentStatus: React.FC = () => {
  const { orderNo } = useParams<{ orderNo: string }>();
  const navigate = useNavigate();

  // ÂpŒÁ
  useEffect(() => {
    if (!orderNo || !validateOrderNo(orderNo)) {
      toast.error('àH„¢U÷');
      navigate('/orders');
      return;
    }
  }, [orderNo, navigate]);

  // ·Ö¢UæÅ
  const {
    data: orderInfo,
    isLoading: orderLoading,
    error: orderError
  } = useQuery({
    queryKey: ['order-detail', orderNo],
    queryFn: () => orderService.getOrderDetail(orderNo!),
    enabled: !!orderNo && validateOrderNo(orderNo),
  });

  // /Ø¶¡
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

  // ï
  useEffect(() => {
    if (orderError || paymentError) {
      toast.error(orderError?.message || paymentError?.message || ' }1%');
    }
  }, [orderError, paymentError]);

  // /ØŸ
  useEffect(() => {
    if (paymentStatus === 'SUCCESS' && orderInfo) {
      toast.success('/ØŸ<‰');
      // ößól©(70Ÿ¶
      setTimeout(() => {
        navigate(`/payment/result?orderNo=${orderNo}&status=SUCCESS`);
      }, 2000);
    }
  }, [paymentStatus, orderInfo, navigate, orderNo]);

  // /Ø1%
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
          <p className="text-gray-600">c(åâ/Ø¶...</p>
        </div>
      </div>
    );
  }

  if (!orderInfo) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">=</div>
          <h2 className="text-xl font-semibold mb-2">¢UX(</h2>
          <p className="text-gray-600 mb-4">÷Àå¢U÷/&cn</p>
          <button
            onClick={() => navigate('/orders')}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
          >
            ÔÞ¢Uh
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
            /Ø¶åâ
          </h1>
          <p className="text-gray-600">
            c(žöÑ§¨„/Ø¶÷...
          </p>
        </div>

        <PaymentProgress
          status={paymentStatus}
          orderInfo={orderInfo}
        />

        {/* Í\	® */}
        <div className="mt-6 flex justify-center space-x-4">
          <button
            onClick={refreshStatus}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
          >
            = 7°¶
          </button>

          <button
            onClick={() => navigate(`/order/${orderNo}`)}
            className="bg-gray-600 text-white px-6 py-2 rounded-lg hover:bg-gray-700 transition-colors"
          >
            =Ë ¢UæÅ
          </button>
        </div>

        {/* .©áo */}
        <div className="mt-8 bg-blue-50 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-900 mb-3">
            =¡ /Øô
          </h3>
          <ul className="space-y-2 text-blue-800">
            <li>" /ØŸê¨ól0Óœub</li>
            <li>" ‚G/Øî˜÷ÕÍ°/Ø</li>
            <li>" /ØŒ÷ÿsíubI…¶ô°</li>
            <li>" ‚	‘î÷Tû¢</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default PaymentStatus;