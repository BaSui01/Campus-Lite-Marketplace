/**
 * 支付方式卡片组件
 * @author BaSui 😎
 * @description 显示单个支付方式的卡片
 */

import React from 'react';

interface PaymentMethod {
  id: string;
  name: string;
  icon: string;
  description: string;
  isDefault: boolean;
  usageCount: number;
  isEnabled: boolean;
}

interface PaymentMethodCardProps {
  method: PaymentMethod;
  onSetDefault: (methodId: string) => void;
  disabled?: boolean;
}

export const PaymentMethodCard: React.FC<PaymentMethodCardProps> = ({
  method,
  onSetDefault,
  disabled = false
}) => {
  const handleSetDefault = () => {
    if (!disabled && !method.isDefault) {
      onSetDefault(method.id);
    }
  };

  return (
    <div className={`bg-white rounded-lg shadow-md p-6 border-2 transition-all ${
      method.isDefault
        ? 'border-blue-500 bg-blue-50'
        : 'border-gray-200 hover:border-gray-300'
    } ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
         onClick={handleSetDefault}>
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center space-x-3">
          <div className="text-3xl">{method.icon}</div>
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              {method.name}
            </h3>
            <p className="text-sm text-gray-600">{method.description}</p>
          </div>
        </div>

        {method.isDefault && (
          <div className="bg-blue-500 text-white text-xs px-2 py-1 rounded-full">
            默认
          </div>
        )}
      </div>

      <div className="flex items-center justify-between">
        <div className="text-sm text-gray-500">
          使用次数：{method.usageCount}
        </div>

        <button
          className={`text-sm px-4 py-2 rounded-lg transition-colors ${
            method.isDefault
              ? 'bg-blue-500 text-white'
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          } ${disabled ? 'cursor-not-allowed' : ''}`}
          disabled={disabled}
        >
          {method.isDefault ? '已设为默认' : '设为默认'}
        </button>
      </div>

      {!method.isEnabled && (
        <div className="mt-4 p-2 bg-gray-100 rounded text-center text-sm text-gray-500">
          暂时不可用
        </div>
      )}
    </div>
  );
};