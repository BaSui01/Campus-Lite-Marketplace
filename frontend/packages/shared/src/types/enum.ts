/**
 * 枚举类型定义
 * @author BaSui 😎
 * @description 前端枚举类型，用于类型约束和值映射
 */

// ==================== 用户相关枚举 ====================

/**
 * 用户状态枚举
 */
export enum UserStatus {
  /** 活跃 */
  ACTIVE = 'ACTIVE',
  /** 已封禁 */
  BANNED = 'BANNED',
}

/**
 * 用户角色枚举
 */
export enum UserRole {
  /** 普通用户 */
  USER = 'USER',
  /** 管理员 */
  ADMIN = 'ADMIN',
  /** 超级管理员 */
  SUPER_ADMIN = 'SUPER_ADMIN',
}

/**
 * 性别枚举
 */
export enum Gender {
  /** 男 */
  MALE = 'MALE',
  /** 女 */
  FEMALE = 'FEMALE',
  /** 未知 */
  UNKNOWN = 'UNKNOWN',
}

// ==================== 物品相关枚举 ====================

/**
 * 物品状态枚举
 */
export enum GoodsStatus {
  /** 待审核 */
  PENDING = 'PENDING',
  /** 已上架 */
  APPROVED = 'APPROVED',
  /** 已拒绝 */
  REJECTED = 'REJECTED',
  /** 已售出 */
  SOLD = 'SOLD',
  /** 已下架 */
  OFF_SHELF = 'OFF_SHELF',
}

/**
 * 物品成色枚举
 */
export enum GoodsCondition {
  /** 全新 */
  NEW = 'NEW',
  /** 9成新 */
  LIKE_NEW = 'LIKE_NEW',
  /** 8成新 */
  EXCELLENT = 'EXCELLENT',
  /** 7成新 */
  GOOD = 'GOOD',
  /** 6成新及以下 */
  FAIR = 'FAIR',
}

/**
 * 物品排序字段枚举
 */
export enum GoodsSortField {
  /** 创建时间 */
  CREATED_AT = 'createdAt',
  /** 价格 */
  PRICE = 'price',
  /** 浏览次数 */
  VIEW_COUNT = 'viewCount',
  /** 收藏次数 */
  FAVORITE_COUNT = 'favoriteCount',
  /** 已售数量 */
  SOLD_COUNT = 'soldCount',
}

// ==================== 订单相关枚举 ====================

/**
 * 订单状态枚举
 */
export enum OrderStatus {
  /** 待支付 */
  PENDING_PAYMENT = 'PENDING_PAYMENT',
  /** 已支付 */
  PAID = 'PAID',
  /** 待发货 */
  PENDING_DELIVERY = 'PENDING_DELIVERY',
  /** 待收货 */
  PENDING_RECEIPT = 'PENDING_RECEIPT',
  /** 已完成 */
  COMPLETED = 'COMPLETED',
  /** 已取消 */
  CANCELLED = 'CANCELLED',
  /** 退款中 */
  REFUNDING = 'REFUNDING',
  /** 已退款 */
  REFUNDED = 'REFUNDED',
}

/**
 * 支付方式枚举
 */
export enum PaymentMethod {
  /** 微信支付 */
  WECHAT = 'WECHAT',
  /** 支付宝 */
  ALIPAY = 'ALIPAY',
  /** 积分支付 */
  POINTS = 'POINTS',
}

/**
 * 支付状态枚举
 */
export enum PaymentStatus {
  /** 待支付 */
  PENDING = 'PENDING',
  /** 支付成功 */
  SUCCESS = 'SUCCESS',
  /** 支付失败 */
  FAILED = 'FAILED',
  /** 已退款 */
  REFUNDED = 'REFUNDED',
}

// ==================== 消息相关枚举 ====================

/**
 * 消息类型枚举
 */
export enum MessageType {
  /** 文本消息 */
  TEXT = 'TEXT',
  /** 图片消息 */
  IMAGE = 'IMAGE',
  /** 系统消息 */
  SYSTEM = 'SYSTEM',
  /** 语音消息 */
  VOICE = 'VOICE',
  /** 视频消息 */
  VIDEO = 'VIDEO',
  /** 文件消息 */
  FILE = 'FILE',
}

/**
 * 消息状态枚举
 */
export enum MessageStatus {
  /** 未读 */
  UNREAD = 'UNREAD',
  /** 已读 */
  READ = 'READ',
  /** 已撤回 */
  RECALLED = 'RECALLED',
}

/**
 * 通知类型枚举
 */
export enum NotificationType {
  /** 系统通知 */
  SYSTEM = 'SYSTEM',
  /** 订单通知 */
  ORDER = 'ORDER',
  /** 消息通知 */
  MESSAGE = 'MESSAGE',
  /** 评价通知 */
  REVIEW = 'REVIEW',
  /** 收藏通知 */
  FAVORITE = 'FAVORITE',
  /** 举报通知 */
  REPORT = 'REPORT',
}

// ==================== 审核相关枚举 ====================

/**
 * 审核状态枚举
 */
export enum AuditStatus {
  /** 待审核 */
  PENDING = 'PENDING',
  /** 审核中 */
  PROCESSING = 'PROCESSING',
  /** 审核通过 */
  APPROVED = 'APPROVED',
  /** 审核拒绝 */
  REJECTED = 'REJECTED',
}

/**
 * 举报状态枚举
 */
export enum ReportStatus {
  /** 待处理 */
  PENDING = 'PENDING',
  /** 处理中 */
  PROCESSING = 'PROCESSING',
  /** 已解决 */
  RESOLVED = 'RESOLVED',
  /** 已驳回 */
  REJECTED = 'REJECTED',
}

/**
 * 举报类型枚举
 */
export enum ReportType {
  /** 违规商品 */
  GOODS = 'GOODS',
  /** 违规订单 */
  ORDER = 'ORDER',
  /** 违规用户 */
  USER = 'USER',
  /** 违规帖子 */
  POST = 'POST',
  /** 违规评论 */
  REPLY = 'REPLY',
}

// ==================== 积分相关枚举 ====================

/**
 * 积分类型枚举
 */
export enum PointsType {
  /** 获得积分 */
  EARN = 'EARN',
  /** 消费积分 */
  SPEND = 'SPEND',
}

/**
 * 积分来源枚举
 */
export enum PointsSource {
  /** 注册奖励 */
  REGISTER = 'REGISTER',
  /** 签到奖励 */
  SIGN_IN = 'SIGN_IN',
  /** 发布物品 */
  PUBLISH_GOODS = 'PUBLISH_GOODS',
  /** 完成交易 */
  COMPLETE_ORDER = 'COMPLETE_ORDER',
  /** 评价订单 */
  REVIEW_ORDER = 'REVIEW_ORDER',
  /** 积分支付 */
  POINTS_PAYMENT = 'POINTS_PAYMENT',
  /** 系统调整 */
  SYSTEM_ADJUST = 'SYSTEM_ADJUST',
}

// ==================== 社区相关枚举 ====================

/**
 * 帖子状态枚举
 */
export enum PostStatus {
  /** 正常 */
  NORMAL = 'NORMAL',
  /** 已删除 */
  DELETED = 'DELETED',
  /** 已隐藏 */
  HIDDEN = 'HIDDEN',
  /** 已置顶 */
  PINNED = 'PINNED',
}

/**
 * 帖子排序字段枚举
 */
export enum PostSortField {
  /** 创建时间 */
  CREATED_AT = 'createdAt',
  /** 浏览次数 */
  VIEW_COUNT = 'viewCount',
  /** 点赞次数 */
  LIKE_COUNT = 'likeCount',
  /** 评论次数 */
  COMMENT_COUNT = 'commentCount',
}

// ==================== 通用枚举 ====================

/**
 * 排序方向枚举
 */
export enum SortOrder {
  /** 升序 */
  ASC = 'ASC',
  /** 降序 */
  DESC = 'DESC',
}

/**
 * 日期范围枚举
 */
export enum DateRange {
  /** 今天 */
  TODAY = 'TODAY',
  /** 最近7天 */
  LAST_7_DAYS = 'LAST_7_DAYS',
  /** 最近30天 */
  LAST_30_DAYS = 'LAST_30_DAYS',
  /** 最近3个月 */
  LAST_3_MONTHS = 'LAST_3_MONTHS',
  /** 最近6个月 */
  LAST_6_MONTHS = 'LAST_6_MONTHS',
  /** 最近1年 */
  LAST_YEAR = 'LAST_YEAR',
  /** 自定义 */
  CUSTOM = 'CUSTOM',
}

/**
 * 文件类型枚举
 */
export enum FileType {
  /** 图片 */
  IMAGE = 'IMAGE',
  /** 视频 */
  VIDEO = 'VIDEO',
  /** 音频 */
  AUDIO = 'AUDIO',
  /** 文档 */
  DOCUMENT = 'DOCUMENT',
  /** 其他 */
  OTHER = 'OTHER',
}

/**
 * 操作类型枚举（审计日志）
 */
export enum ActionType {
  /** 创建 */
  CREATE = 'CREATE',
  /** 更新 */
  UPDATE = 'UPDATE',
  /** 删除 */
  DELETE = 'DELETE',
  /** 查询 */
  READ = 'READ',
  /** 登录 */
  LOGIN = 'LOGIN',
  /** 登出 */
  LOGOUT = 'LOGOUT',
  /** 导出 */
  EXPORT = 'EXPORT',
  /** 导入 */
  IMPORT = 'IMPORT',
}

// ==================== 退款相关枚举 ====================

/**
 * 退款状态枚举
 */
export enum RefundStatus {
  /** 待处理 */
  PENDING = 'PENDING',
  /** 已批准 */
  APPROVED = 'APPROVED',
  /** 已拒绝 */
  REJECTED = 'REJECTED',
  /** 已完成 */
  COMPLETED = 'COMPLETED',
}

/**
 * 退款原因枚举
 */
export enum RefundReason {
  /** 不想要了 */
  DONT_WANT = 'DONT_WANT',
  /** 商品质量问题 */
  QUALITY_ISSUE = 'QUALITY_ISSUE',
  /** 商品与描述不符 */
  NOT_AS_DESCRIBED = 'NOT_AS_DESCRIBED',
  /** 卖家发错货 */
  WRONG_ITEM = 'WRONG_ITEM',
  /** 其他原因 */
  OTHER = 'OTHER',
}

// ==================== 导出枚举映射对象 ====================

/**
 * 用户状态中文映射
 */
export const UserStatusLabel: Record<UserStatus, string> = {
  [UserStatus.ACTIVE]: '活跃',
  [UserStatus.BANNED]: '已封禁',
};

/**
 * 物品状态中文映射
 */
export const GoodsStatusLabel: Record<GoodsStatus, string> = {
  [GoodsStatus.PENDING]: '待审核',
  [GoodsStatus.APPROVED]: '已上架',
  [GoodsStatus.REJECTED]: '已拒绝',
  [GoodsStatus.SOLD]: '已售出',
  [GoodsStatus.OFF_SHELF]: '已下架',
};

/**
 * 订单状态中文映射
 */
export const OrderStatusLabel: Record<OrderStatus, string> = {
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
 * 支付方式中文映射
 */
export const PaymentMethodLabel: Record<PaymentMethod, string> = {
  [PaymentMethod.WECHAT]: '微信支付',
  [PaymentMethod.ALIPAY]: '支付宝',
  [PaymentMethod.POINTS]: '积分支付',
};

/**
 * 消息类型中文映射
 */
export const MessageTypeLabel: Record<MessageType, string> = {
  [MessageType.TEXT]: '文本消息',
  [MessageType.IMAGE]: '图片消息',
  [MessageType.SYSTEM]: '系统消息',
  [MessageType.VOICE]: '语音消息',
  [MessageType.VIDEO]: '视频消息',
  [MessageType.FILE]: '文件消息',
};

/**
 * 通知类型中文映射
 */
export const NotificationTypeLabel: Record<NotificationType, string> = {
  [NotificationType.SYSTEM]: '系统通知',
  [NotificationType.ORDER]: '订单通知',
  [NotificationType.MESSAGE]: '消息通知',
  [NotificationType.REVIEW]: '评价通知',
  [NotificationType.FAVORITE]: '收藏通知',
  [NotificationType.REPORT]: '举报通知',
};
