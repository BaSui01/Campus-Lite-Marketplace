/**
 * 订单列表页 📦
 * @author BaSui 😎
 * @description 买家订单、卖家订单、订单状态筛选
 */

import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Skeleton, Empty, OrderCard } from '@campus/shared/components';
import { orderService } from '@campus/shared/services';
import './Orders.css';

type OrderType = 'buyer' | 'seller';
type OrderStatus = 'all' | 'PENDING_PAYMENT' | 'PENDING_SHIPMENT' | 'SHIPPED' | 'COMPLETED' | 'CANCELLED';

const ORDER_TABS = [
  { value: 'buyer' as OrderType, label: '我买到的' },
  { value: 'seller' as OrderType, label: '我卖出的' },
];

const STATUS_TABS = [
  { value: 'all' as OrderStatus, label: '全部' },
  { value: 'PENDING_PAYMENT' as OrderStatus, label: '待支付' },
  { value: 'PENDING_SHIPMENT' as OrderStatus, label: '待发货' },
  { value: 'SHIPPED' as OrderStatus, label: '已发货' },
  { value: 'COMPLETED' as OrderStatus, label: '已完成' },
  { value: 'CANCELLED' as OrderStatus, label: '已取消' },
];

export const Orders: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [orderType, setOrderType] = useState<OrderType>(
    (searchParams.get('type') as OrderType) || 'buyer'
  );
  const [orderStatus, setOrderStatus] = useState<OrderStatus>(
    (searchParams.get('status') as OrderStatus) || 'all'
  );
  const [page, setPage] = useState(0);
  const pageSize = 10;

  // 查询订单列表
  const { data: ordersData, isLoading } = useQuery({
    queryKey: ['orders', orderType, orderStatus, page],
    queryFn: async () => {
      const params = {
        status: orderStatus === 'all' ? undefined : orderStatus,
        page,
        pageSize,
      };

      const response = orderType === 'buyer'
        ? await orderService.getBuyerOrders(params)
        : await orderService.getSellerOrders(params);

      return response.data;
    },
    staleTime: 1 * 60 * 1000, // 1分钟缓存
  });

  // 切换订单类型
  const handleTypeChange = (type: OrderType) => {
    setOrderType(type);
    setOrderStatus('all');
    setPage(0);
    setSearchParams({ type });
  };

  // 切换订单状态
  const handleStatusChange = (status: OrderStatus) => {
    setOrderStatus(status);
    setPage(0);
    const params: Record<string, string> = { type: orderType };
    if (status !== 'all') {
      params.status = status;
    }
    setSearchParams(params);
  };

  // 查看订单详情
  const handleViewOrder = (orderNo: string) => {
    navigate(`/order/${orderNo}`);
  };

  const orderList = ordersData?.content || [];
  const totalPages = ordersData?.totalPages || 0;

  return (
    <div className="orders-page">
      <div className="orders-container">
        <h1 className="orders-title">我的订单</h1>

        {/* 订单类型切换 */}
        <div className="orders-type-tabs">
          {ORDER_TABS.map((tab) => (
            <button
              key={tab.value}
              className={`orders-type-tab ${orderType === tab.value ? 'active' : ''}`}
              onClick={() => handleTypeChange(tab.value)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* 订单状态筛选 */}
        <div className="orders-status-tabs">
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.value}
              className={`orders-status-tab ${orderStatus === tab.value ? 'active' : ''}`}
              onClick={() => handleStatusChange(tab.value)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Loading状态 */}
        {isLoading && (
          <div className="orders-loading">
            {Array.from({ length: 3 }).map((_, index) => (
              <Skeleton key={index} type="card" animation="wave" />
            ))}
          </div>
        )}

        {/* 订单列表 */}
        {!isLoading && orderList.length > 0 && (
          <>
            <div className="orders-list">
              {orderList.map((order) => (
                <OrderCard
                  key={order.orderNo}
                  order={order}
                  onViewDetail={() => handleViewOrder(order.orderNo!)}
                />
              ))}
            </div>

            {/* 分页 */}
            {totalPages > 1 && (
              <div className="orders-pagination">
                <button
                  className="pagination-btn"
                  disabled={page === 0}
                  onClick={() => setPage(page - 1)}
                >
                  上一页
                </button>
                <span className="pagination-info">
                  第 {page + 1} / {totalPages} 页
                </span>
                <button
                  className="pagination-btn"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage(page + 1)}
                >
                  下一页
                </button>
              </div>
            )}
          </>
        )}

        {/* 空状态 */}
        {!isLoading && orderList.length === 0 && (
          <Empty
            icon="📦"
            title="暂无订单"
            description={
              orderStatus === 'all'
                ? orderType === 'buyer'
                  ? '您还没有购买过商品'
                  : '您还没有卖出过商品'
                : `暂无${STATUS_TABS.find(t => t.value === orderStatus)?.label}订单`
            }
            action={
              orderType === 'buyer' && (
                <button onClick={() => navigate('/goods')}>
                  去逛逛
                </button>
              )
            }
          />
        )}
      </div>
    </div>
  );
};

export default Orders;
