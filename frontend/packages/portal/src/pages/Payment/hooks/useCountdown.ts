/**
 * ¡öHook
 * @author BaSui =
 * @description /Ø¡ö…ö
 */

import { useState, useEffect } from 'react';

interface UseCountdownOptions {
  targetTime: string;
  onExpire?: () => void;
}

export const useCountdown = ({ targetTime, onExpire }: UseCountdownOptions) => {
  const [remainingTime, setRemainingTime] = useState(0);
  const [isExpired, setIsExpired] = useState(false);

  useEffect(() => {
    const calculateRemaining = () => {
      const now = new Date().getTime();
      const target = new Date(targetTime).getTime();
      const remaining = Math.max(0, target - now);

      setRemainingTime(remaining);

      if (remaining === 0 && !isExpired) {
        setIsExpired(true);
        onExpire?.();
      }
    };

    calculateRemaining();
    const timer = setInterval(calculateRemaining, 1000);

    return () => clearInterval(timer);
  }, [targetTime, isExpired, onExpire]);

  const formatTime = (milliseconds: number): string => {
    const totalSeconds = Math.floor(milliseconds / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }

    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  return {
    remainingTime,
    isExpired,
    formattedTime: formatTime(remainingTime),
  };
};