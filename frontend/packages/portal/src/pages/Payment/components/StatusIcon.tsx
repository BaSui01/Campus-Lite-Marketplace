/**
 * /Ø¶þÄö
 * @author BaSui =
 * @description >:/Ø¶„þ
 */

import React from 'react';
import { getPaymentStatusIcon } from '../utils/paymentUtils';

interface StatusIconProps {
  status: string;
  size?: 'small' | 'medium' | 'large';
  className?: string;
}

export const StatusIcon: React.FC<StatusIconProps> = ({
  status,
  size = 'medium',
  className = ''
}) => {
  const icon = getPaymentStatusIcon(status);

  const sizeClasses = {
    small: 'text-xl',
    medium: 'text-2xl',
    large: 'text-4xl',
  };

  return (
    <span className={`${sizeClasses[size]} ${className}`}>
      {icon}
    </span>
  );
};