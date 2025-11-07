/**
 * /ÿ€¶ƒˆ
 * @author BaSui =
 * @description >:/ÿ€¶∂å°ˆ
 */

import React from 'react';
import { StatusIcon } from './StatusIcon';
import { CountdownTimer } from './CountdownTimer';
import { getPaymentStatusText, calculatePaymentProgress } from '../utils/paymentUtils';
import { formatCurrency } from '../utils/formatUtils';

interface PaymentProgressProps {
  status: string;
  orderInfo: any;
  className?: string;
}

export const PaymentProgress: React.FC<PaymentProgressProps> = ({
  status,
  orderInfo,
  className = ''
}) => {
  const progressPercentage = calculatePaymentProgress(
    status,
    orderInfo?.createTime,
    orderInfo?.expireTime
  );

  const statusColor = {
    'PENDING': 'bg-blue-500',
    'SUCCESS': 'bg-green-500',
    'FAILED': 'bg-red-500',
    'TIMEOUT': 'bg-gray-500',
    'CANCELLED': 'bg-gray-500',
  }[status] || 'bg-gray-500';

  return (
    <div className={`bg-white rounded-lg shadow-md p-6 ${className}`}>
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-3">
          <StatusIcon status={status} size="large" />
          <div>
            <h3 className="text-lg font-semibold">
              {getPaymentStatusText(status)}
            </h3>
            <p className="text-gray-600 text-sm">
              ¢U˜{orderInfo?.orderNo}
            </p>
          </div>
        </div>

        <div className="text-right">
          <div className="text-2xl font-bold text-blue-600">
            {formatCurrency(orderInfo?.actualAmount || '0.00')}
          </div>
          <div className="text-sm text-gray-500">
            ¢U—ù
          </div>
        </div>
      </div>

      {/* €¶a */}
      <div className="mb-4">
        <div className="flex justify-between text-sm text-gray-600 mb-2">
          <span>/ÿ€¶</span>
          <span>{Math.round(progressPercentage)}%</span>
        </div>
        <div className="w-full bg-gray-200 rounded-full h-3">
          <div
            className={`h-3 rounded-full transition-all duration-300 ${statusColor}`}
            style={{ width: `${progressPercentage}%` }}
          />
        </div>
      </div>

      {/* °ˆ */}
      {orderInfo?.expireTime && (
        <div className="flex justify-center">
          <CountdownTimer
            expireTime={orderInfo.expireTime}
            onExpire={() => {
              // /ÿÖˆ
              console.log('/ÿÖˆ');
            }}
          />
        </div>
      )}
    </div>
  );
};