/**
 * /Ø¶¡Hook
 * @author BaSui =
 * @description /Ø¶åâWebSocketžöô°
 */

import { useState, useEffect, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { orderService } from '../../../../shared/src/services/order';
import { websocketService } from '../../../../shared/src/services/websocket';
import { validateOrderNo } from '../utils/paymentUtils';

interface UsePaymentOptions {
  orderNo: string;
  autoPoll?: boolean;
  websocketEnabled?: boolean;
}

export const usePayment = (options: UsePaymentOptions) => {
  const [status, setStatus] = useState<string>('PENDING');
  const [error, setError] = useState<string | null>(null);

  // /Ø¶åâ
  const {
    data: paymentStatus,
    isLoading,
    refetch,
    error: queryError
  } = useQuery({
    queryKey: ['payment-status', options.orderNo],
    queryFn: () => orderService.queryPaymentStatus(options.orderNo),
    refetchInterval: options.autoPoll ? 3000 : false,
    enabled: !!options.orderNo && validateOrderNo(options.orderNo),
    staleTime: 1000,
  });

  // WebSocketžöô°
  useEffect(() => {
    if (!options.websocketEnabled) return;

    const handleOrderUpdate = (data: any) => {
      if (data.orderNo === options.orderNo) {
        setStatus(data.status);
        refetch();
      }
    };

    websocketService.onOrderUpdate(handleOrderUpdate);

    // nÝWebSocketòÞ¥
    if (!websocketService.isConnected()) {
      websocketService.connect();
    }

    return () => {
      websocketService.offOrderUpdate(handleOrderUpdate);
    };
  }, [options.orderNo, options.websocketEnabled, refetch]);

  // ï
  useEffect(() => {
    if (queryError) {
      setError(queryError.message || 'åâ/Ø¶1%');
    }
  }, [queryError]);

  // K¨7°¶
  const refreshStatus = useCallback(() => {
    setError(null);
    refetch();
  }, [refetch]);

  return {
    status: paymentStatus || status,
    isLoading,
    error,
    refreshStatus,
  };
};