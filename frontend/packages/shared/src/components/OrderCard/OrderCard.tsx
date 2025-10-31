/**
 * OrderCard 组件 - 订单卡片专家！📦
 * @author BaSui 😎
 * @description 订单卡片组件，用于订单列表展示，整合 Card、Tag、Badge 等基础组件
 */

import React from 'react';
import { Card, type CardProps } from '../Card';
import { Tag } from '../Tag';
import { Badge } from '../Badge';
import './OrderCard.css';

/**
 * 订单状态
 */
export type OrderStatus =
  | 'pending_payment'    // 待支付
  | 'pending_delivery'   // 待发货
  | 'pending_receipt'    // 待收货
  | 'completed'          // 已完成
  | 'cancelled'          // 已取消
  | 'refunding'          // 退款中
  | 'refunded';          // 已退款

/**
 * 订单商品项接口
 */
export interface OrderItem {
  /**
   * 商品 ID
   */
  goodsId: string;

  /**
   * 商品名称
   */
  goodsName: string;

  /**
   * 商品图片
   */
  goodsImage: string;

  /**
   * 商品价格
   */
  price: number;

  /**
   * 购买数量
   */
  quantity: number;
}

/**
 * 订单数据接口
 */
export interface OrderData {
  /**
   * 订单 ID
   */
  id: string;

  /**
   * 订单编号
   */
  orderNo: string;

  /**
   * 订单状态
   */
  status: OrderStatus;

  /**
   * 订单商品列表
   */
  items: OrderItem[];

  /**
   * 订单总金额
   */
  totalAmount: number;

  /**
   * 买家信息
   */
  buyer?: {
    id: string;
    name: string;
    avatar?: string;
  };

  /**
   * 卖家信息
   */
  seller?: {
    id: string;
    name: string;
    avatar?: string;
  };

  /**
   * 创建时间
   */
  createdAt: string;

  /**
   * 更新时间
   */
  updatedAt?: string;

  /**
   * 备注
   */
  remark?: string;
}

/**
 * OrderCard 组件的 Props 接口
 */
export interface OrderCardProps extends Omit<CardProps, 'children'> {
  /**
   * 订单数据
   */
  order: OrderData;

  /**
   * 是否显示买家信息
   * @default false
   */
  showBuyer?: boolean;

  /**
   * 是否显示卖家信息
   * @default false
   */
  showSeller?: boolean;

  /**
   * 卡片点击回调
   */
  onCardClick?: (order: OrderData) => void;

  /**
   * 支付按钮点击回调
   */
  onPayClick?: (order: OrderData) => void;

  /**
   * 取消按钮点击回调
   */
  onCancelClick?: (order: OrderData) => void;

  /**
   * 确认收货按钮点击回调
   */
  onConfirmClick?: (order: OrderData) => void;

  /**
   * 查看详情按钮点击回调
   */
  onDetailClick?: (order: OrderData) => void;

  /**
   * 申请退款按钮点击回调
   */
  onRefundClick?: (order: OrderData) => void;
}

/**
 * 获取状态配置
 */
const getStatusConfig = (status: OrderStatus) => {
  const configs = {
    pending_payment: { label: '待支付', color: 'warning' as const },
    pending_delivery: { label: '待发货', color: 'processing' as const },
    pending_receipt: { label: '待收货', color: 'processing' as const },
    completed: { label: '已完成', color: 'success' as const },
    cancelled: { label: '已取消', color: 'default' as const },
    refunding: { label: '退款中', color: 'warning' as const },
    refunded: { label: '已退款', color: 'error' as const },
  };
  return configs[status] || configs.pending_payment;
};

/**
 * 格式化价格
 */
const formatPrice = (price: number): string => {
  return `¥${price.toFixed(2)}`;
};

/**
 * 格式化时间
 */
const formatTime = (time: string): string => {
  const date = new Date(time);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

/**
 * OrderCard 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <OrderCard
 *   order={{
 *     id: '1',
 *     orderNo: 'ORDER202501010001',
 *     status: 'pending_payment',
 *     items: [
 *       {
 *         goodsId: '1',
 *         goodsName: 'MacBook Pro',
 *         goodsImage: '/macbook.jpg',
 *         price: 12999,
 *         quantity: 1,
 *       },
 *     ],
 *     totalAmount: 12999,
 *     createdAt: '2025-01-01T10:00:00Z',
 *   }}
 *   onPayClick={(order) => console.log('支付', order)}
 * />
 * ```
 */
export const OrderCard: React.FC<OrderCardProps> = ({
  order,
  showBuyer = false,
  showSeller = false,
  onCardClick,
  onPayClick,
  onCancelClick,
  onConfirmClick,
  onDetailClick,
  onRefundClick,
  ...cardProps
}) => {
  const statusConfig = getStatusConfig(order.status);

  /**
   * 获取操作按钮
   */
  const getActions = () => {
    const actions: React.ReactNode[] = [];

    // 查看详情（所有状态都显示）
    if (onDetailClick) {
      actions.push(
        <button
          key="detail"
          className="campus-order-card__action-btn campus-order-card__action-btn--secondary"
          onClick={(e) => {
            e.stopPropagation();
            onDetailClick(order);
          }}
        >
          查看详情
        </button>
      );
    }

    // 待支付：支付、取消
    if (order.status === 'pending_payment') {
      if (onCancelClick) {
        actions.push(
          <button
            key="cancel"
            className="campus-order-card__action-btn campus-order-card__action-btn--secondary"
            onClick={(e) => {
              e.stopPropagation();
              onCancelClick(order);
            }}
          >
            取消订单
          </button>
        );
      }
      if (onPayClick) {
        actions.push(
          <button
            key="pay"
            className="campus-order-card__action-btn campus-order-card__action-btn--primary"
            onClick={(e) => {
              e.stopPropagation();
              onPayClick(order);
            }}
          >
            立即支付
          </button>
        );
      }
    }

    // 待收货：确认收货、申请退款
    if (order.status === 'pending_receipt') {
      if (onRefundClick) {
        actions.push(
          <button
            key="refund"
            className="campus-order-card__action-btn campus-order-card__action-btn--secondary"
            onClick={(e) => {
              e.stopPropagation();
              onRefundClick(order);
            }}
          >
            申请退款
          </button>
        );
      }
      if (onConfirmClick) {
        actions.push(
          <button
            key="confirm"
            className="campus-order-card__action-btn campus-order-card__action-btn--primary"
            onClick={(e) => {
              e.stopPropagation();
              onConfirmClick(order);
            }}
          >
            确认收货
          </button>
        );
      }
    }

    return actions.length > 0 ? actions : undefined;
  };

  return (
    <Card
      {...cardProps}
      className={`campus-order-card ${cardProps.className || ''}`}
      onClick={() => onCardClick?.(order)}
      actions={getActions()}
    >
      <div className="campus-order-card__content">
        {/* 订单头部 */}
        <div className="campus-order-card__header">
          <div className="campus-order-card__header-left">
            <span className="campus-order-card__order-no">订单号：{order.orderNo}</span>
            <Tag color={statusConfig.color} size="small">
              {statusConfig.label}
            </Tag>
          </div>
          <span className="campus-order-card__time">{formatTime(order.createdAt)}</span>
        </div>

        {/* 商品列表 */}
        <div className="campus-order-card__items">
          {order.items.map((item, index) => (
            <div key={index} className="campus-order-card__item">
              <img
                src={item.goodsImage}
                alt={item.goodsName}
                className="campus-order-card__item-image"
              />
              <div className="campus-order-card__item-info">
                <span className="campus-order-card__item-name" title={item.goodsName}>
                  {item.goodsName}
                </span>
                <div className="campus-order-card__item-bottom">
                  <span className="campus-order-card__item-price">{formatPrice(item.price)}</span>
                  <span className="campus-order-card__item-quantity">x{item.quantity}</span>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* 订单底部 */}
        <div className="campus-order-card__footer">
          {/* 用户信息 */}
          {(showBuyer && order.buyer) || (showSeller && order.seller) ? (
            <div className="campus-order-card__user-info">
              {showBuyer && order.buyer && (
                <div className="campus-order-card__user">
                  <span className="campus-order-card__user-label">买家：</span>
                  <span className="campus-order-card__user-name">{order.buyer.name}</span>
                </div>
              )}
              {showSeller && order.seller && (
                <div className="campus-order-card__user">
                  <span className="campus-order-card__user-label">卖家：</span>
                  <span className="campus-order-card__user-name">{order.seller.name}</span>
                </div>
              )}
            </div>
          ) : null}

          {/* 总金额 */}
          <div className="campus-order-card__total">
            <span className="campus-order-card__total-label">合计：</span>
            <span className="campus-order-card__total-amount">
              {formatPrice(order.totalAmount)}
            </span>
          </div>
        </div>

        {/* 备注 */}
        {order.remark && (
          <div className="campus-order-card__remark">
            <span className="campus-order-card__remark-label">备注：</span>
            <span className="campus-order-card__remark-content">{order.remark}</span>
          </div>
        )}
      </div>
    </Card>
  );
};

export default OrderCard;
