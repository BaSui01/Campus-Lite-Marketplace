/**
 * /ØÓœU:ub
 * @author BaSui =
 * @description /ØŸ1%…öIÓœ„U:ub
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

  // ÂpŒÁ
  React.useEffect(() => {
    if (!orderNo || !validateOrderNo(orderNo)) {
      toast.error('àH„¢U÷');
      navigate('/orders');
      return;
    }

    if (!status || !['SUCCESS', 'FAILED', 'TIMEOUT'].includes(status)) {
      toast.error('àH„/Ø¶');
      navigate('/orders');
      return;
    }
  }, [orderNo, status, navigate]);

  // ·Ö¢UæÅ
  const {
    data: orderInfo,
    isLoading,
    error
  } = useQuery({
    queryKey: ['order-detail', orderNo],
    queryFn: () => orderService.getOrderDetail(orderNo!),
    enabled: !!orderNo && validateOrderNo(orderNo),
  });

  // ;Í\
  const handlePrimaryAction = () => {
    if (status === 'SUCCESS') {
      // å¢UæÅ
      navigate(`/order/${orderNo}`);
    } else if (status === 'FAILED') {
      // Í°/Ø
      navigate(`/order/${orderNo}`);
    } else if (status === 'TIMEOUT') {
      // Í°Uól0FÁæÅu	
      if (orderInfo?.goodsId) {
        navigate(`/goods/${orderInfo.goodsId}`);
      } else {
        navigate('/goods');
      }
    }
  };

  // !Í\
  const handleSecondaryAction = () => {
    if (status === 'SUCCESS') {
      // çí-i
      navigate('/goods');
    } else {
      // Tû¢
      window.open('/customer-service', '_blank');
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">c( }/ØÓœ...</p>
        </div>
      </div>
    );
  }

  if (error || !orderInfo) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">=</div>
          <h2 className="text-xl font-semibold mb-2"> }1%</h2>
          <p className="text-gray-600 mb-4">
            {error?.message || 'àÕ·Ö¢Uáo'}
          </p>
          <button
            onClick={() => navigate('/orders')}
            className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
          >
            ÔŞ¢Uh
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
            /ØÓœ
          </h1>
          <p className="text-gray-600">
            ¢U {orderNo} „/Ø{status === 'SUCCESS' ? 'Ÿ' : status === 'FAILED' ? '1%' : '…ö'}
          </p>
        </div>

        <ResultCard
          status={status || 'FAILED'}
          orderInfo={orderInfo}
          onPrimaryAction={handlePrimaryAction}
          onSecondaryAction={handleSecondaryAction}
        />

        {/* ¨PFÁ:ß - Å/ØŸö>: */}
        {status === 'SUCCESS' && (
          <div className="mt-8">
            <h2 className="text-xl font-semibold mb-4 text-gray-900">:¨¨P</h2>
            <div className="bg-white rounded-lg shadow p-6">
              <div className="text-center text-gray-500">
                <div className="text-4xl mb-4">=Í</div>
                <p>ô¾iFÁI¨Ñ°</p>
                <button
                  onClick={() => navigate('/goods')}
                  className="mt-4 bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700"
                >
                  »
                </button>
              </div>
            </div>
          </div>
        )}

        {/* .©áo */}
        <div className="mt-8 bg-blue-50 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-900 mb-3">
            =¡ )¨Ğ:
          </h3>
          <div className="space-y-2 text-blue-800">
            {status === 'SUCCESS' && (
              <>
                <li>" /ØŸ÷ÃI…F¶Ñ'</li>
                <li>" ¨ïå("„¢U"-åiAáo</li>
                <li>" 60FÁ÷Êön¤6'vÄ÷</li>
              </>
            )}
            {status === 'FAILED' && (
              <>
                <li>" /Ø1%§ûU9(</li>
                <li>" ¨ïåÍ°	é/Ø¹!/Ø</li>
                <li>" ‚!/Ø1%÷Tû¢O©</li>
              </>
            )}
            {status === 'TIMEOUT' && (
              <>
                <li>" /Ø…ö¢Uòê¨Öˆ</li>
                <li>" ¨ïåÍ°U-påFÁ</li>
                <li>" ‚	‘î÷Tû¢</li>
              </>
            )}
            <li>" ‚	ûUî˜"ÎTû(¿¢</li>
          </div>
        </div>

        {/* •èÍ\ */}
        <div className="mt-8 text-center">
          <button
            onClick={() => navigate('/orders')}
            className="text-blue-600 hover:text-blue-800 font-medium"
          >
            å„¢U ’
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentResult;