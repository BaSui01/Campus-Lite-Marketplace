/**
 * /ÿ”úaGƒˆ
 * @author BaSui =
 * @description >:/ÿ”úÑÊ∆·o
 */

import React from 'react';
import { StatusIcon } from './StatusIcon';
import { getPaymentStatusText } from '../utils/paymentUtils';
import { formatCurrency, formatDateTime } from '../utils/formatUtils';

interface ResultCardProps {
  status: string;
  orderInfo: any;
  onPrimaryAction: () => void;
  onSecondaryAction: () => void;
}

export const ResultCard: React.FC<ResultCardProps> = ({
  status,
  orderInfo,
  onPrimaryAction,
  onSecondaryAction,
}) => {
  const getStatusConfig = (status: string) => {
    const configs = {
      'SUCCESS': {
        icon: '<â',
        title: '/ÿü',
        message: '®Ñ¢UÚü/ÿ˜IÖF∂—'',
        color: 'success',
        primaryText: 'Â¢UÊ≈',
        secondaryText: 'ÁÌ-i',
        primaryColor: 'bg-green-600 hover:bg-green-700',
        secondaryColor: 'bg-gray-600 hover:bg-gray-700',
      },
      'FAILED': {
        icon: '=',
        title: '/ÿ1%',
        message: '/ÿ«-˙∞Óò˜Õ’	Èv÷/ÿπ',
        color: 'danger',
        primaryText: 'Õ∞/ÿ',
        secondaryText: 'T˚¢',
        primaryColor: 'bg-blue-600 hover:bg-blue-700',
        secondaryColor: 'bg-gray-600 hover:bg-gray-700',
      },
      'TIMEOUT': {
        icon: '',
        title: '/ÿÖˆ',
        message: '/ÿˆÙÚ«¢UÚÍ®÷à',
        color: 'warning',
        primaryText: 'Õ∞U',
        secondaryText: 'T˚¢',
        primaryColor: 'bg-yellow-600 hover:bg-yellow-700',
        secondaryColor: 'bg-gray-600 hover:bg-gray-700',
      },
    };

    return configs[status] || configs['FAILED'];
  };

  const config = getStatusConfig(status);

  return (
    <div className={`bg-white rounded-lg shadow-lg p-8 ${
      status === 'SUCCESS' ? 'border-green-200' :
      status === 'FAILED' ? 'border-red-200' :
      'border-yellow-200'
    } border-2`}>
      {/* ”ú˛åò */}
      <div className="text-center mb-6">
        <div className="text-6xl mb-4">{config.icon}</div>
        <h2 className={`text-2xl font-bold mb-2 ${
          status === 'SUCCESS' ? 'text-green-600' :
          status === 'FAILED' ? 'text-red-600' :
          'text-yellow-600'
        }`}>
          {config.title}
        </h2>
        <p className="text-gray-600">{config.message}</p>
      </div>

      {/* ¢U·o */}
      <div className="bg-gray-50 rounded-lg p-6 mb-6">
        <h3 className="font-semibold mb-4 text-gray-900">¢U·o</h3>
        <div className="space-y-3">
          <div className="flex justify-between">
            <span className="text-gray-600">¢U˜</span>
            <span className="font-mono text-sm">{orderInfo?.orderNo}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">¢U—ù</span>
            <span className="font-bold text-lg text-blue-600">
              {formatCurrency(orderInfo?.actualAmount || '0.00')}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">F¡</span>
            <span className="text-right max-w-xs truncate">
              {orderInfo?.goodsTitle}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-600">UˆÙ</span>
            <span>{orderInfo?.createTime ? formatDateTime(orderInfo.createTime) : '-'}</span>
          </div>
          {orderInfo?.paymentTime && (
            <div className="flex justify-between">
              <span className="text-gray-600">/ÿˆÙ</span>
              <span>{formatDateTime(orderInfo.paymentTime)}</span>
            </div>
          )}
        </div>
      </div>

      {/* Õ\	Æ */}
      <div className="flex flex-col sm:flex-row gap-4">
        <button
          onClick={onPrimaryAction}
          className={`flex-1 py-3 px-6 rounded-lg font-semibold transition-colors text-white ${config.primaryColor}`}
        >
          {config.primaryText}
        </button>

        <button
          onClick={onSecondaryAction}
          className="flex-1 py-3 px-6 border-2 border-gray-300 rounded-lg font-semibold hover:bg-gray-50 transition-colors"
        >
          {config.secondaryText}
        </button>
      </div>

      {/* /ÿ∂Ê≈ */}
      <div className="mt-6 pt-6 border-t border-gray-200">
        <div className="flex items-center justify-center space-x-2 text-sm text-gray-500">
          <StatusIcon status={status} size="small" />
          <span>/ÿ∂{getPaymentStatusText(status)}</span>
        </div>
      </div>
    </div>
  );
};