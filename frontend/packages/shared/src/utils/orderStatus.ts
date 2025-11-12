/**
 * 订单状态映射工具（UI阶段 → 后端状态）
 * @author BaSui 😎
 * @description
 * - 后端没有 WAITING_DELIVERY 等“阶段型”状态，这里提供 UI 阶段到后端枚举的映射。
 * - 统一前端文案：
 *   待支付：PENDING_PAYMENT
 *   待发货：PAID
 *   待收货：SHIPPED、DELIVERED
 *   已完成：COMPLETED
 *   已取消：CANCELLED
 *   售后：REFUNDING、REFUNDED
 */

import { OrderStatus } from '../types/enum';

/** UI 阶段状态（聚合态） */
export type UiOrderStage =
  | 'PENDING_PAYMENT'   // 待支付
  | 'PENDING_SHIPMENT'  // 待发货（已支付）
  | 'PENDING_RECEIPT'   // 待收货（已发货/已送达）
  | 'COMPLETED'         // 已完成
  | 'CANCELLED'         // 已取消
  | 'AFTER_SALES';      // 售后（退款中/已退款）

/** UI 阶段中文文案 */
export const UiOrderStageLabel: Record<UiOrderStage, string> = {
  PENDING_PAYMENT: '待支付',
  PENDING_SHIPMENT: '待发货',
  PENDING_RECEIPT: '待收货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  AFTER_SALES: '售后',
};

/** UI 阶段 → 后端状态集合 */
export const UiStageToBackendStatuses: Record<UiOrderStage, OrderStatus[]> = {
  PENDING_PAYMENT: [OrderStatus.PENDING_PAYMENT],
  PENDING_SHIPMENT: [OrderStatus.PAID],
  PENDING_RECEIPT: [OrderStatus.SHIPPED, OrderStatus.DELIVERED],
  COMPLETED: [OrderStatus.COMPLETED],
  CANCELLED: [OrderStatus.CANCELLED],
  AFTER_SALES: [OrderStatus.REFUNDING, OrderStatus.REFUNDED],
};

/** 后端状态 → UI 阶段（单值映射） */
export const toUiStage = (status: OrderStatus): UiOrderStage => {
  switch (status) {
    case OrderStatus.PENDING_PAYMENT:
      return 'PENDING_PAYMENT';
    case OrderStatus.PAID:
      return 'PENDING_SHIPMENT';
    case OrderStatus.SHIPPED:
    case OrderStatus.DELIVERED:
      return 'PENDING_RECEIPT';
    case OrderStatus.COMPLETED:
      return 'COMPLETED';
    case OrderStatus.CANCELLED:
      return 'CANCELLED';
    case OrderStatus.REFUNDING:
    case OrderStatus.REFUNDED:
      return 'AFTER_SALES';
    default:
      return 'PENDING_PAYMENT';
  }
};

/** 展示用文案（把已支付显示为“待发货”，发货/送达显示为“待收货”） */
export const displayLabelForStatus = (status: OrderStatus): string => {
  return UiOrderStageLabel[toUiStage(status)];
};

/** 判断后端状态是否属于某个 UI 阶段 */
export const isStatusInStage = (status: OrderStatus, stage: UiOrderStage): boolean => {
  return UiStageToBackendStatuses[stage].includes(status);
};

/** 根据 UI 阶段返回首选的后端状态（用于只能传单值的后端筛选接口） */
export const preferredBackendStatusForStage = (stage: UiOrderStage): OrderStatus | undefined => {
  const list = UiStageToBackendStatuses[stage];
  return list?.length ? list[0] : undefined;
};

