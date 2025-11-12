/**
 * 支付方式选择器 💳
 * @author BaSui 😎
 */

import React from 'react';
import './PaymentMethodSelector.css';

type PaymentMethod = 'WECHAT' | 'ALIPAY' | 'BANK_CARD';

interface PaymentMethodOption {
  value: PaymentMethod;
  label: string;
  icon: string;
  disabled?: boolean;
  badge?: string;
}

const PAYMENT_METHODS: PaymentMethodOption[] = [
  {
    value: 'WECHAT',
    label: '微信支付',
    icon: '💚',
  },
  {
    value: 'ALIPAY',
    label: '支付宝',
    icon: '💙',
  },
  {
    value: 'BANK_CARD',
    label: '银行卡支付',
    icon: '💳',
    disabled: true,
    badge: '开发中',
  },
];

interface Props {
  selectedMethod: PaymentMethod | null;
  onSelect: (method: PaymentMethod) => void;
}

export const PaymentMethodSelector: React.FC<Props> = ({
  selectedMethod,
  onSelect,
}) => {
  return (
    <div className="payment-method-selector">
      <h3 className="selector-title">选择支付方式</h3>
      <div className="method-list">
        {PAYMENT_METHODS.map((method) => (
          <div
            key={method.value}
            className={`method-item ${
              selectedMethod === method.value ? 'selected' : ''
            } ${method.disabled ? 'disabled' : ''}`}
            onClick={() => !method.disabled && onSelect(method.value)}
          >
            <div className="method-icon">{method.icon}</div>
            <div className="method-info">
              <div className="method-label">{method.label}</div>
              {method.badge && <span className="method-badge">{method.badge}</span>}
            </div>
            <div className="method-check">
              {selectedMethod === method.value && <span className="check-icon">✓</span>}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
