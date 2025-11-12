/**
 * useOrderUpdate Hook - 订单状态更新订阅大师！📦
 * @author BaSui 😎
 * @description 订阅 WebSocket 订单状态更新，实时跟踪订单变化
 */

import { useState, useEffect, useCallback } from 'react';
import { websocketService, type OrderUpdateData } from '../utils/websocket';
// ✅ 使用 API 生成的类型
import { OrderResponseStatusEnum as OrderStatus } from '../api/models/order-response';

/**
 * 订单状态更新记录
 */
export interface OrderUpdateRecord extends OrderUpdateData {
  /**
   * 更新时间
   */
  updatedAt: string;
}

/**
 * useOrderUpdate 配置选项
 */
export interface UseOrderUpdateOptions {
  /**
   * 订单ID（可选，用于过滤特定订单的更新）
   */
  orderId?: number;

  /**
   * 订单号（可选，用于过滤特定订单的更新）
   */
  orderNo?: string;

  /**
   * 收到订单更新回调
   */
  onOrderUpdate?: (data: OrderUpdateData) => void;

  /**
   * 是否自动订阅
   * @default true
   */
  autoSubscribe?: boolean;

  /**
   * 最大更新记录数量
   * @default 20
   */
  maxRecords?: number;
}

/**
 * useOrderUpdate 返回值
 */
export interface UseOrderUpdateResult {
  /**
   * 订单更新记录列表
   */
  updates: OrderUpdateRecord[];

  /**
   * 最新更新
   */
  lastUpdate: OrderUpdateRecord | null;

  /**
   * 订单状态映射（orderId -> status）
   */
  orderStatusMap: Map<number, OrderStatus>;

  /**
   * 获取指定订单的最新状态
   */
  getOrderStatus: (orderId: number) => OrderStatus | undefined;

  /**
   * 清空更新记录
   */
  clearUpdates: () => void;

  /**
   * 手动订阅
   */
  subscribe: () => void;

  /**
   * 手动取消订阅
   */
  unsubscribe: () => void;
}

/**
 * useOrderUpdate Hook
 *
 * @description
 * 订阅 WebSocket 订单状态更新，实时跟踪订单变化。
 * 可以过滤特定订单的更新，并维护订单状态映射表。
 *
 * @param options 配置选项
 * @returns 订单状态更新订阅结果
 *
 * @example
 * ```tsx
 * // 基础用法（监听所有订单更新）
 * function OrderList() {
 *   const { updates, orderStatusMap } = useOrderUpdate({
 *     onOrderUpdate: (data) => {
 *       console.log('订单状态更新:', data);
 *       // 可以在这里刷新订单列表或显示提示
 *     },
 *   });
 *
 *   return (
 *     <div>
 *       {orders.map((order) => (
 *         <div key={order.id}>
 *           <p>订单号: {order.orderNo}</p>
 *           <p>状态: {orderStatusMap.get(order.id) || order.status}</p>
 *         </div>
 *       ))}
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 监听特定订单的更新
 * function OrderDetail({ orderId }: { orderId: number }) {
 *   const { lastUpdate, getOrderStatus } = useOrderUpdate({
 *     orderId,
 *     onOrderUpdate: (data) => {
 *       // 订单状态发生变化
 *       message.success(`订单状态已更新为: ${data.status}`);
 *     },
 *   });
 *
 *   const currentStatus = getOrderStatus(orderId);
 *
 *   return (
 *     <div>
 *       <p>当前状态: {currentStatus}</p>
 *       {lastUpdate && (
 *         <p>最新更新: {lastUpdate.message} ({lastUpdate.updatedAt})</p>
 *       )}
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 手动订阅/取消订阅
 * function MyOrders() {
 *   const { updates, subscribe, unsubscribe } = useOrderUpdate({
 *     autoSubscribe: false,
 *   });
 *
 *   useEffect(() => {
 *     // 只在"我的订单"页面订阅
 *     subscribe();
 *
 *     return () => {
 *       unsubscribe();
 *     };
 *   }, [subscribe, unsubscribe]);
 *
 *   return (
 *     <div>
 *       <h3>订单更新记录</h3>
 *       {updates.map((update) => (
 *         <div key={update.orderId + update.updatedAt}>
 *           订单 {update.orderNo} 状态更新为 {update.status}
 *         </div>
 *       ))}
 *     </div>
 *   );
 * }
 * ```
 */
export const useOrderUpdate = (
  options: UseOrderUpdateOptions = {}
): UseOrderUpdateResult => {
  const {
    orderId,
    orderNo,
    onOrderUpdate,
    autoSubscribe = true,
    maxRecords = 20,
  } = options;

  // 订单更新记录列表
  const [updates, setUpdates] = useState<OrderUpdateRecord[]>([]);

  // 最新更新
  const [lastUpdate, setLastUpdate] = useState<OrderUpdateRecord | null>(null);

  // 订单状态映射（orderId -> status）
  const [orderStatusMap, setOrderStatusMap] = useState<Map<number, OrderStatus>>(
    new Map()
  );

  /**
   * 处理收到的订单更新
   */
  const handleOrderUpdate = useCallback(
    (data: OrderUpdateData) => {
      // 如果指定了 orderId 或 orderNo，只处理匹配的订单
      if (orderId !== undefined && data.orderId !== orderId) {
        return;
      }
      if (orderNo !== undefined && data.orderNo !== orderNo) {
        return;
      }

      // 创建更新记录
      const record: OrderUpdateRecord = {
        ...data,
        updatedAt: new Date().toISOString(),
      };

      // 更新记录列表
      setUpdates((prev) => {
        const newList = [record, ...prev];
        // 如果超过最大数量，删除最旧的记录
        if (newList.length > maxRecords) {
          return newList.slice(0, maxRecords);
        }
        return newList;
      });

      setLastUpdate(record);

      // 更新订单状态映射
      setOrderStatusMap((prev) => {
        const newMap = new Map(prev);
        // ✅ data.status 是字符串，需要类型断言为 OrderStatus
        newMap.set(data.orderId, data.status as typeof OrderStatus[keyof typeof OrderStatus]);
        return newMap;
      });

      // 触发回调
      onOrderUpdate?.(data);
    },
    [orderId, orderNo, maxRecords, onOrderUpdate]
  );

  /**
   * 获取指定订单的最新状态
   */
  const getOrderStatus = useCallback(
    (orderIdParam: number): OrderStatus | undefined => {
      return orderStatusMap.get(orderIdParam);
    },
    [orderStatusMap]
  );

  /**
   * 清空更新记录
   */
  const clearUpdates = useCallback(() => {
    setUpdates([]);
    setLastUpdate(null);
    setOrderStatusMap(new Map());
  }, []);

  /**
   * 订阅订单状态更新
   */
  const subscribe = useCallback(() => {
    websocketService.onOrderUpdate(handleOrderUpdate);
  }, [handleOrderUpdate]);

  /**
   * 取消订阅订单状态更新
   */
  const unsubscribe = useCallback(() => {
    websocketService.offOrderUpdate(handleOrderUpdate);
  }, [handleOrderUpdate]);

  /**
   * 自动订阅/取消订阅
   */
  useEffect(() => {
    if (autoSubscribe) {
      subscribe();

      // 清理函数
      return () => {
        unsubscribe();
      };
    }
  }, [autoSubscribe, subscribe, unsubscribe]);

  return {
    updates,
    lastUpdate,
    orderStatusMap,
    getOrderStatus,
    clearUpdates,
    subscribe,
    unsubscribe,
  };
};

export default useOrderUpdate;
