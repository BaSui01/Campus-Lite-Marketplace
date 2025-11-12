/**
 * ¡öÄö
 * @author BaSui =
 * @description /Ø¡ö>:
 */

import React from 'react';
import { useCountdown } from '../hooks/useCountdown';
import { formatCountdown } from '../utils/formatUtils';

interface CountdownTimerProps {
  expireTime: string;
  onExpire?: () => void;
  className?: string;
}

export const CountdownTimer: React.FC<CountdownTimerProps> = ({
  expireTime,
  onExpire,
  className = ''
}) => {
  const { formattedTime, isExpired } = useCountdown({
    targetTime: expireTime,
    onExpire,
  });

  if (isExpired) {
    return (
      <div className={`text-red-500 font-semibold ${className}`}>
        ð /Øò…ö
      </div>
    );
  }

  return (
    <div className={`text-gray-600 ${className}`}>
      ñ /ØiYöô{formattedTime}
    </div>
  );
};