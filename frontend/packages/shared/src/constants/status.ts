/**
 * 状态常量定义
 * @author BaSui 😎
 * @description 各种业务状态的常量定义和映射
 */

import {
  UserStatus,
  GoodsStatus,
  OrderStatus,
  PaymentMethod,
  PaymentStatus,
  MessageStatus,
  MessageType,
  NotificationType,
  AuditStatus,
  ReportStatus,
  RefundStatus,
  PostStatus,
  GoodsCondition,
} from '../types/enum';

// ==================== 用户状态 ====================

/**
 * 用户状态选项列表
 */
export const USER_STATUS_OPTIONS = [
  { label: '活跃', value: UserStatus.ACTIVE, color: 'green' },
  { label: '已封禁', value: UserStatus.BANNED, color: 'red' },
] as const;

/**
 * 用户状态标签映射
 */
export const USER_STATUS_LABELS: Record<UserStatus, string> = {
  [UserStatus.ACTIVE]: '活跃',
  [UserStatus.BANNED]: '已封禁',
};

/**
 * 用户状态颜色映射
 */
export const USER_STATUS_COLORS: Record<UserStatus, string> = {
  [UserStatus.ACTIVE]: 'green',
  [UserStatus.BANNED]: 'red',
};

// ==================== 物品状态 ====================

/**
 * 物品状态选项列表
 */
export const GOODS_STATUS_OPTIONS = [
  { label: '待审核', value: GoodsStatus.PENDING, color: 'orange' },
  { label: '已上架', value: GoodsStatus.APPROVED, color: 'green' },
  { label: '已拒绝', value: GoodsStatus.REJECTED, color: 'red' },
  { label: '已售出', value: GoodsStatus.SOLD, color: 'gray' },
  { label: '已下架', value: GoodsStatus.OFF_SHELF, color: 'default' },
] as const;

/**
 * 物品状态标签映射
 */
export const GOODS_STATUS_LABELS: Record<GoodsStatus, string> = {
  [GoodsStatus.PENDING]: '待审核',
  [GoodsStatus.APPROVED]: '已上架',
  [GoodsStatus.REJECTED]: '已拒绝',
  [GoodsStatus.SOLD]: '已售出',
  [GoodsStatus.OFF_SHELF]: '已下架',
};

/**
 * 物品状态颜色映射
 */
export const GOODS_STATUS_COLORS: Record<GoodsStatus, string> = {
  [GoodsStatus.PENDING]: 'orange',
  [GoodsStatus.APPROVED]: 'green',
  [GoodsStatus.REJECTED]: 'red',
  [GoodsStatus.SOLD]: 'gray',
  [GoodsStatus.OFF_SHELF]: 'default',
};

/**
 * 物品成色选项列表
 */
export const GOODS_CONDITION_OPTIONS = [
  { label: '全新', value: GoodsCondition.NEW },
  { label: '9成新', value: GoodsCondition.LIKE_NEW },
  { label: '8成新', value: GoodsCondition.EXCELLENT },
  { label: '7成新', value: GoodsCondition.GOOD },
  { label: '6成新及以下', value: GoodsCondition.FAIR },
] as const;

/**
 * 物品成色标签映射
 */
export const GOODS_CONDITION_LABELS: Record<GoodsCondition, string> = {
  [GoodsCondition.NEW]: '全新',
  [GoodsCondition.LIKE_NEW]: '9成新',
  [GoodsCondition.EXCELLENT]: '8成新',
  [GoodsCondition.GOOD]: '7成新',
  [GoodsCondition.FAIR]: '6成新及以下',
};

// ==================== 订单状态 ====================

/**
 * 订单状态选项列表
 */
export const ORDER_STATUS_OPTIONS = [
  { label: '待支付', value: OrderStatus.PENDING_PAYMENT, color: 'orange' },
  { label: '已支付', value: OrderStatus.PAID, color: 'blue' },
  { label: '待发货', value: OrderStatus.PENDING_DELIVERY, color: 'cyan' },
  { label: '待收货', value: OrderStatus.PENDING_RECEIPT, color: 'purple' },
  { label: '已完成', value: OrderStatus.COMPLETED, color: 'green' },
  { label: '已取消', value: OrderStatus.CANCELLED, color: 'gray' },
  { label: '退款中', value: OrderStatus.REFUNDING, color: 'orange' },
  { label: '已退款', value: OrderStatus.REFUNDED, color: 'red' },
] as const;

/**
 * 订单状态标签映射
 */
export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  [OrderStatus.PENDING_PAYMENT]: '待支付',
  [OrderStatus.PAID]: '已支付',
  [OrderStatus.PENDING_DELIVERY]: '待发货',
  [OrderStatus.PENDING_RECEIPT]: '待收货',
  [OrderStatus.COMPLETED]: '已完成',
  [OrderStatus.CANCELLED]: '已取消',
  [OrderStatus.REFUNDING]: '退款中',
  [OrderStatus.REFUNDED]: '已退款',
};

/**
 * 订单状态颜色映射
 */
export const ORDER_STATUS_COLORS: Record<OrderStatus, string> = {
  [OrderStatus.PENDING_PAYMENT]: 'orange',
  [OrderStatus.PAID]: 'blue',
  [OrderStatus.PENDING_DELIVERY]: 'cyan',
  [OrderStatus.PENDING_RECEIPT]: 'purple',
  [OrderStatus.COMPLETED]: 'green',
  [OrderStatus.CANCELLED]: 'gray',
  [OrderStatus.REFUNDING]: 'orange',
  [OrderStatus.REFUNDED]: 'red',
};

// ==================== 支付方式 ====================

/**
 * 支付方式选项列表
 */
export const PAYMENT_METHOD_OPTIONS = [
  { label: '微信支付', value: PaymentMethod.WECHAT, icon: 'wechat' },
  { label: '支付宝', value: PaymentMethod.ALIPAY, icon: 'alipay' },
  { label: '积分支付', value: PaymentMethod.POINTS, icon: 'points' },
] as const;

/**
 * 支付方式标签映射
 */
export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  [PaymentMethod.WECHAT]: '微信支付',
  [PaymentMethod.ALIPAY]: '支付宝',
  [PaymentMethod.POINTS]: '积分支付',
};

/**
 * 支付状态选项列表
 */
export const PAYMENT_STATUS_OPTIONS = [
  { label: '待支付', value: PaymentStatus.PENDING, color: 'orange' },
  { label: '支付成功', value: PaymentStatus.SUCCESS, color: 'green' },
  { label: '支付失败', value: PaymentStatus.FAILED, color: 'red' },
  { label: '已退款', value: PaymentStatus.REFUNDED, color: 'gray' },
] as const;

/**
 * 支付状态标签映射
 */
export const PAYMENT_STATUS_LABELS: Record<PaymentStatus, string> = {
  [PaymentStatus.PENDING]: '待支付',
  [PaymentStatus.SUCCESS]: '支付成功',
  [PaymentStatus.FAILED]: '支付失败',
  [PaymentStatus.REFUNDED]: '已退款',
};

// ==================== 消息状态 ====================

/**
 * 消息状态标签映射
 */
export const MESSAGE_STATUS_LABELS: Record<MessageStatus, string> = {
  [MessageStatus.UNREAD]: '未读',
  [MessageStatus.READ]: '已读',
  [MessageStatus.RECALLED]: '已撤回',
};

/**
 * 消息类型标签映射
 */
export const MESSAGE_TYPE_LABELS: Record<MessageType, string> = {
  [MessageType.TEXT]: '文本消息',
  [MessageType.IMAGE]: '图片消息',
  [MessageType.SYSTEM]: '系统消息',
  [MessageType.VOICE]: '语音消息',
  [MessageType.VIDEO]: '视频消息',
  [MessageType.FILE]: '文件消息',
};

/**
 * 消息类型图标映射
 */
export const MESSAGE_TYPE_ICONS: Record<MessageType, string> = {
  [MessageType.TEXT]: '📝',
  [MessageType.IMAGE]: '🖼️',
  [MessageType.SYSTEM]: '🔔',
  [MessageType.VOICE]: '🎤',
  [MessageType.VIDEO]: '🎬',
  [MessageType.FILE]: '📎',
};

// ==================== 通知类型 ====================

/**
 * 通知类型标签映射
 */
export const NOTIFICATION_TYPE_LABELS: Record<NotificationType, string> = {
  [NotificationType.ORDER]: '订单通知',
  [NotificationType.PAYMENT]: '支付通知',
  [NotificationType.MESSAGE]: '站内消息',
  [NotificationType.LIKE]: '点赞通知',
  [NotificationType.COMMENT]: '评论通知',
  [NotificationType.FOLLOW]: '关注通知',
  [NotificationType.PRICE_ALERT]: '价格提醒',
  [NotificationType.SYSTEM]: '系统公告',
};

/**
 * 通知类型图标映射
 */
export const NOTIFICATION_TYPE_ICONS: Record<NotificationType, string> = {
  [NotificationType.ORDER]: '🛒',
  [NotificationType.PAYMENT]: '💰',
  [NotificationType.MESSAGE]: '💬',
  [NotificationType.LIKE]: '👍',
  [NotificationType.COMMENT]: '💬',
  [NotificationType.FOLLOW]: '👤',
  [NotificationType.PRICE_ALERT]: '💲',
  [NotificationType.SYSTEM]: '📢',
};

/**
 * 通知类型颜色映射
 */
export const NOTIFICATION_TYPE_COLORS: Record<NotificationType, string> = {
  [NotificationType.ORDER]: 'green',
  [NotificationType.PAYMENT]: 'blue',
  [NotificationType.MESSAGE]: 'purple',
  [NotificationType.LIKE]: 'orange',
  [NotificationType.COMMENT]: 'cyan',
  [NotificationType.FOLLOW]: 'magenta',
  [NotificationType.PRICE_ALERT]: 'gold',
  [NotificationType.SYSTEM]: 'blue',
};

// ==================== 审核状态 ====================

/**
 * 审核状态选项列表
 */
export const AUDIT_STATUS_OPTIONS = [
  { label: '待审核', value: AuditStatus.PENDING, color: 'orange' },
  { label: '审核中', value: AuditStatus.PROCESSING, color: 'blue' },
  { label: '审核通过', value: AuditStatus.APPROVED, color: 'green' },
  { label: '审核拒绝', value: AuditStatus.REJECTED, color: 'red' },
] as const;

/**
 * 审核状态标签映射
 */
export const AUDIT_STATUS_LABELS: Record<AuditStatus, string> = {
  [AuditStatus.PENDING]: '待审核',
  [AuditStatus.PROCESSING]: '审核中',
  [AuditStatus.APPROVED]: '审核通过',
  [AuditStatus.REJECTED]: '审核拒绝',
};

// ==================== 举报状态 ====================

/**
 * 举报状态选项列表
 */
export const REPORT_STATUS_OPTIONS = [
  { label: '待处理', value: ReportStatus.PENDING, color: 'orange' },
  { label: '处理中', value: ReportStatus.PROCESSING, color: 'blue' },
  { label: '已解决', value: ReportStatus.RESOLVED, color: 'green' },
  { label: '已驳回', value: ReportStatus.REJECTED, color: 'red' },
] as const;

/**
 * 举报状态标签映射
 */
export const REPORT_STATUS_LABELS: Record<ReportStatus, string> = {
  [ReportStatus.PENDING]: '待处理',
  [ReportStatus.PROCESSING]: '处理中',
  [ReportStatus.RESOLVED]: '已解决',
  [ReportStatus.REJECTED]: '已驳回',
};

// ==================== 退款状态 ====================

/**
 * 退款状态选项列表
 */
export const REFUND_STATUS_OPTIONS = [
  { label: '待处理', value: RefundStatus.PENDING, color: 'orange' },
  { label: '已批准', value: RefundStatus.APPROVED, color: 'blue' },
  { label: '已拒绝', value: RefundStatus.REJECTED, color: 'red' },
  { label: '已完成', value: RefundStatus.COMPLETED, color: 'green' },
] as const;

/**
 * 退款状态标签映射
 */
export const REFUND_STATUS_LABELS: Record<RefundStatus, string> = {
  [RefundStatus.PENDING]: '待处理',
  [RefundStatus.APPROVED]: '已批准',
  [RefundStatus.REJECTED]: '已拒绝',
  [RefundStatus.COMPLETED]: '已完成',
};

// ==================== 帖子状态 ====================

/**
 * 帖子状态选项列表
 */
export const POST_STATUS_OPTIONS = [
  { label: '正常', value: PostStatus.NORMAL, color: 'green' },
  { label: '已删除', value: PostStatus.DELETED, color: 'red' },
  { label: '已隐藏', value: PostStatus.HIDDEN, color: 'gray' },
  { label: '已置顶', value: PostStatus.PINNED, color: 'blue' },
] as const;

/**
 * 帖子状态标签映射
 */
export const POST_STATUS_LABELS: Record<PostStatus, string> = {
  [PostStatus.NORMAL]: '正常',
  [PostStatus.DELETED]: '已删除',
  [PostStatus.HIDDEN]: '已隐藏',
  [PostStatus.PINNED]: '已置顶',
};
