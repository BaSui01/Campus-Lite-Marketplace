/**
 * /ض�Hook
 * @author BaSui =
 * @description /ض��WebSocket����
 */

import { useState, useEffect, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Services } from '@campus/shared';
import { validateOrderNo } from '../utils/paymentUtils';

interface UsePaymentOptions {
  orderNo: string;
  autoPoll?: boolean;
  websocketEnabled?: boolean;
}

export const usePayment = (options: UsePaymentOptions) => {
  const [status, setStatus] = useState<string>('PENDING');
  const [error, setError] = useState<string | null>(null);

  // /ض��
  const {
    data: paymentStatus,
    isLoading,
    refetch,
    error: queryError
  } = useQuery({
    queryKey: ['payment-status', options.orderNo],
    queryFn: () => Services.orderService.queryPaymentStatus(options.orderNo),
    refetchInterval: options.autoPoll ? 3000 : false,
    enabled: !!options.orderNo && validateOrderNo(options.orderNo),
    staleTime: 1000,
  });

  // WebSocket����
  useEffect(() => {
    // TODO: WebSocket功能暂时禁用，等待 websocketService 实现
    if (!options.websocketEnabled) return;

    // const handleOrderUpdate = (data: any) => {
    //   if (data.orderNo === options.orderNo) {
    //     setStatus(data.status);
    //     refetch();
    //   }
    // };

    // Services.websocketService.onOrderUpdate(handleOrderUpdate);

    // // n�WebSocket�ޥ
    // if (!Services.websocketService.isConnected()) {
    //   Services.websocketService.connect();
    // }

    // return () => {
    //   Services.websocketService.offOrderUpdate(handleOrderUpdate);
    // };
  }, [options.orderNo, options.websocketEnabled, refetch]);

  // �
  useEffect(() => {
    if (queryError) {
      setError(queryError.message || '��/ض1%');
    }
  }, [queryError]);

  // K�7��
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