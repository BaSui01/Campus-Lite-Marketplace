/**
 * 订单列表页 📦
 * @author BaSui 😎
 * @description 买家订单、卖家订单、订单状态筛选
 */

import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Skeleton, Empty, OrderCard } from '@campus/shared/components';
import { orderService } from '@campus/shared/services';
import { 
  preferredBackendStatusForStage, 
  isStatusInStage, 
  toUiStage, 
  type UiOrderStage 
} from '@campus/shared/utils';
import { OrderStatus as BackendOrderStatus } from '@campus/shared/types/enum';
import './Orders.css';

type OrderType = 'buyer' | 'seller';
// UI 标签的筛选值（聚合态），与后端枚举解耦
type OrderStatus = 'all' | 'PENDING_PAYMENT' | 'PENDING_SHIPMENT' | 'PENDING_RECEIPT' | 'COMPLETED' | 'CANCELLED' | 'AFTER_SALES';

const ORDER_TABS = [
  { value: 'buyer' as OrderType, label: '我买到的' },
  { value: 'seller' as OrderType, label: '我卖出的' },
];

const STATUS_TABS = [
  { value: 'all' as OrderStatus, label: '全部' },
  { value: 'PENDING_PAYMENT' as OrderStatus, label: '待支付' },
  { value: 'PENDING_SHIPMENT' as OrderStatus, label: '待发货' },
  { value: 'PENDING_RECEIPT' as OrderStatus, label: '待收货' },
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
  const queryClient = useQueryClient();

  // 查询订单列表
  const { data: ordersData, isLoading } = useQuery({
    queryKey: ['orders', orderType, orderStatus, page],
    queryFn: async () => {
      // 将 UI 阶段映射为后端枚举（只能传单值时取首选）
      let backendStatus: BackendOrderStatus | undefined = undefined;
      if (orderStatus !== 'all') {
        const uiStage = orderStatus as UiOrderStage;
        backendStatus = preferredBackendStatusForStage(uiStage);
      }

      const params = {
        status: backendStatus,
        page,
        size: pageSize,
      } as any;

      const pageResp = orderType === 'buyer'
        ? await orderService.listBuyerOrders(params)
        : await orderService.listSellerOrders(params);

      // 如果选择“待收货/售后”等需要匹配多个后端状态的阶段，这里做一次前端归并过滤，避免后端多状态查询不支持
      if (orderStatus === 'PENDING_RECEIPT' || orderStatus === 'AFTER_SALES') {
        const filtered = (pageResp.content || []).filter((o) => isStatusInStage(o.status as BackendOrderStatus, orderStatus as UiOrderStage));
        return { ...pageResp, content: filtered };
      }

      return pageResp;
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
    navigate(`/orders/${orderNo}`);
  };

  // 立即支付：跳转到支付页选择支付方式
  const handlePay = (o: { orderNo: string }) => {
    if (!o?.orderNo) return;
    navigate(`/payment?orderNo=${encodeURIComponent(o.orderNo)}`);
  };

  // 取消订单（待支付）
  const handleCancel = async (o: { orderNo: string }) => {
    if (!o?.orderNo) return;
    if (!window.confirm('确定要取消该订单吗？')) return;
    try {
      await orderService.cancelOrder(o.orderNo);
      // 刷新列表
      await queryClient.invalidateQueries({ queryKey: ['orders'] });
    } catch (e: any) {
      const msg = e?.response?.data?.message || e?.message || '取消失败，请稍后重试';
      window.alert(msg);
    }
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
                  order={{
                    // 将后端返回的订单转为 OrderCard 需要的结构与状态（UI 阶段）
                    id: String(order.id),
                    orderNo: order.orderNo!,
                    status: ((): any => {
                      const backendStatus = order.status as BackendOrderStatus;
                      // AFTER_SALES 细分：优先依据后端原始状态判断
                      if (backendStatus === 'REFUNDING') return 'refunding';
                      if (backendStatus === 'REFUNDED') return 'refunded';
                      const stage = toUiStage(backendStatus);
                      switch (stage) {
                        case 'PENDING_PAYMENT': return 'pending_payment';
                        case 'PENDING_SHIPMENT': return 'pending_delivery';
                        case 'PENDING_RECEIPT': return 'pending_receipt';
                        case 'COMPLETED': return 'completed';
                        case 'CANCELLED': return 'cancelled';
                        default: return 'pending_payment';
                      }
                    })(),
                    items: [{
                      goodsId: String(order.goodsId),
                      goodsName: order.goodsTitle || '—',
                      goodsImage: order.goodsImage || '',
                      price: Number(order.actualAmount || order.amount || 0),
                      quantity: 1,
                    }],
                    totalAmount: Number(order.actualAmount || order.amount || 0),
                    buyer: order.buyerUsername ? { id: String(order.buyerId), name: order.buyerUsername } : undefined,
                    seller: order.sellerUsername ? { id: String(order.sellerId), name: order.sellerUsername } : undefined,
                    createdAt: order.createdAt as unknown as string,
                  }}
                  onDetailClick={() => handleViewOrder(order.orderNo!)}
                  onPayClick={() => handlePay({ orderNo: order.orderNo! })}
                  onCancelClick={() => handleCancel({ orderNo: order.orderNo! })}
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
