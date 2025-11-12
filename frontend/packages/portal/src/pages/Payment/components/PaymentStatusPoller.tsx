/**
 * 支付状态轮询组件 🔄
 * @author BaSui 😎
 */

import React, { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { orderService } from '@campus/shared/services';

interface Props {
  orderNo: string;
  onSuccess: () => void;
  onFailed: () => void;
}

export const PaymentStatusPoller: React.FC<Props> = ({
  orderNo,
  onSuccess,
  onFailed,
}) => {
  // 每3秒轮询一次订单状态
  const { data: orderDetail } = useQuery({
    queryKey: ['order-payment-status', orderNo],
    queryFn: () => orderService.getOrderDetail(orderNo),
    refetchInterval: 3000, // 3秒轮询
    enabled: !!orderNo,
  });

  useEffect(() => {
    if (!orderDetail) return;

    // 检查订单状态
    if (orderDetail.status === 'PAID' || orderDetail.paid) {
      onSuccess();
    } else if (orderDetail.status === 'CANCELLED' || orderDetail.status === 'FAILED') {
      onFailed();
    }
  }, [orderDetail, onSuccess, onFailed]);

  return null; // 无需渲染任何UI
};
