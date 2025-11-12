/**
 * 业务实体类型定义
 * @author BaSui 😎
 * @description 前端业务实体类型，与后端实体对应
 */

// ==================== 基础实体类型 ====================

/**
 * 基础实体接口（包含通用字段）
 */
export interface BaseEntity {
  /**
   * 实体ID
   */
  id: number;

  /**
   * 创建时间
   */
  createdAt: string;

  /**
   * 更新时间
   */
  updatedAt: string;

  /**
   * 是否已删除（软删除标记）
   */
  deleted?: boolean;
}

// ==================== 用户相关实体 ====================

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
 * 用户实体
 */
export interface User extends BaseEntity {
  /**
   * 用户名
   */
  username: string;

  /**
   * 邮箱
   */
  email?: string;

  /**
   * 手机号
   */
  phone?: string;

  /**
   * 头像URL
   */
  avatar?: string;

  /**
   * 昵称
   */
  nickname?: string;

  /**
   * 用户状态
   */
  status: UserStatus;

  /**
   * 积分
   */
  points: number;

  /**
   * 所属校区ID
   */
  campusId?: number;

  /**
   * 角色列表
   */
  roles?: Role[];
}

/**
 * 角色实体
 */
export interface Role {
  /**
   * 角色ID
   */
  id: number;

  /**
   * 角色名称
   */
  name: string;

  /**
   * 角色描述
   */
  description?: string;

  /**
   * 权限列表
   */
  permissions?: Permission[];
}

/**
 * 权限实体
 */
export interface Permission {
  /**
   * 权限ID
   */
  id: number;

  /**
   * 权限名称
   */
  name: string;

  /**
   * 权限描述
   */
  description?: string;
}

/**
 * 封禁记录实体
 */
export interface BanLog extends BaseEntity {
  /**
   * 被封禁用户ID
   */
  userId: number;

  /**
   * 封禁原因
   */
  reason: string;

  /**
   * 操作管理员ID
   */
  adminId: number;

  /**
   * 封禁开始时间
   */
  bannedAt: string;

  /**
   * 封禁结束时间（永久封禁为 null）
   */
  bannedUntil?: string;
}

/**
 * 积分记录实体
 */
export interface PointsLog extends BaseEntity {
  /**
   * 用户ID
   */
  userId: number;

  /**
   * 积分变化量（正数为增加，负数为减少）
   */
  points: number;

  /**
   * 积分类型
   */
  type: 'EARN' | 'SPEND';

  /**
   * 原因描述
   */
  reason: string;

  /**
   * 关联订单ID（可选）
   */
  orderId?: number;
}

// ==================== 物品相关实体 ====================

/**
 * 物品状态枚举
 */
export enum GoodsStatus {
  /** 待审核 */
  PENDING = 'PENDING',
  /** 已上架 */
  APPROVED = 'APPROVED',
  /** 已锁定（待支付占用） */
  LOCKED = 'LOCKED',
  /** 已拒绝 */
  REJECTED = 'REJECTED',
  /** 已售出 */
  SOLD = 'SOLD',
  /** 已下架 */
  OFF_SHELF = 'OFF_SHELF',
}

/**
 * 物品实体
 */
export interface Goods extends BaseEntity {
  /**
   * 物品标题
   */
  title: string;

  /**
   * 物品描述
   */
  description: string;

  /**
   * 价格（单位：分）
   */
  price: number;

  /**
   * 原价（可选，用于显示折扣）
   */
  originalPrice?: number;

  /**
   * 分类ID
   */
  categoryId: number;

  /**
   * 分类信息（懒加载）
   */
  category?: Category;

  /**
   * 卖家ID
   */
  sellerId: number;

  /**
   * 卖家信息（懒加载）
   */
  seller?: User;

  /**
   * 校区ID
   */
  campusId: number;

  /**
   * 校区信息（懒加载）
   */
  campus?: Campus;

  /**
   * 物品状态
   */
  status: GoodsStatus;

  /**
   * 图片URL列表（JSON数组）
   */
  images: string[];

  /**
   * 库存数量
   */
  stock: number;

  /**
   * 已售数量
   */
  soldCount: number;

  /**
   * 浏览次数
   */
  viewCount: number;

  /**
   * 收藏次数
   */
  favoriteCount: number;

  /**
   * 扩展属性（JSON对象，如品牌、成色、尺寸等）
   */
  extraAttrs?: Record<string, any>;

  /**
   * 标签列表（懒加载）
   */
  tags?: Tag[];
}

/**
 * 分类实体
 */
export interface Category extends BaseEntity {
  /**
   * 分类名称
   */
  name: string;

  /**
   * 父分类ID（顶级分类为 null）
   */
  parentId?: number;

  /**
   * 排序顺序
   */
  sortOrder: number;

  /**
   * 图标（可选）
   */
  icon?: string;

  /**
   * 子分类列表（懒加载）
   */
  children?: Category[];
}

/**
 * 标签实体
 */
export interface Tag extends BaseEntity {
  /**
   * 标签名称
   */
  name: string;

  /**
   * 标签颜色（CSS颜色值）
   */
  color?: string;

  /**
   * 使用次数
   */
  useCount: number;
}

/**
 * 收藏实体
 */
export interface Favorite extends BaseEntity {
  /**
   * 用户ID
   */
  userId: number;

  /**
   * 物品ID
   */
  goodsId: number;

  /**
   * 物品信息（懒加载）
   */
  goods?: Goods;
}

/**
 * 校区实体
 */
export interface Campus extends BaseEntity {
  /**
   * 校区名称
   */
  name: string;

  /**
   * 校区地址
   */
  address?: string;

  /**
   * 校区代码（唯一）
   */
  code: string;
}

// ==================== 订单相关实体 ====================

/**
 * 订单状态枚举
 *
 * ⚠️ 重要：与后端 OrderStatus.java 保持完全一致！
 *
 * @author BaSui 😎
 * @date 2025-11-10
 */
export enum OrderStatus {
  /** 待支付 */
  PENDING_PAYMENT = 'PENDING_PAYMENT',
  /** 已支付 */
  PAID = 'PAID',
  /** 已发货 */
  SHIPPED = 'SHIPPED',
  /** 已送达（待确认收货） */
  DELIVERED = 'DELIVERED',
  /** 已完成 */
  COMPLETED = 'COMPLETED',
  /** 已取消 */
  CANCELLED = 'CANCELLED',
  /** 已评价 */
  REVIEWED = 'REVIEWED',
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
 * 订单实体
 */
export interface Order extends BaseEntity {
  /**
   * 订单号（唯一）
   */
  orderNo: string;

  /**
   * 物品ID
   */
  goodsId: number;

  /**
   * 物品信息（懒加载）
   */
  goods?: Goods;

  /**
   * 买家ID
   */
  buyerId: number;

  /**
   * 买家信息（懒加载）
   */
  buyer?: User;

  /**
   * 卖家ID
   */
  sellerId: number;

  /**
   * 卖家信息（懒加载）
   */
  seller?: User;

  /**
   * 校区ID
   */
  campusId: number;

  /**
   * 校区信息（懒加载）
   */
  campus?: Campus;

  /**
   * 订单金额（单位：分）
   */
  amount: number;

  /**
   * 订单状态
   */
  status: OrderStatus;

  /**
   * 支付方式
   */
  paymentMethod?: PaymentMethod;

  /**
   * 支付时间
   */
  paidAt?: string;

  /**
   * 发货时间
   */
  deliveredAt?: string;

  /**
   * 完成时间
   */
  completedAt?: string;

  /**
   * 取消时间
   */
  cancelledAt?: string;

  /**
   * 备注
   */
  remark?: string;

  /**
   * 评价信息（懒加载）
   */
  review?: Review;

  /**
   * 退款请求（懒加载）
   */
  refundRequest?: RefundRequest;
}

/**
 * 评价实体
 */
export interface Review extends BaseEntity {
  /**
   * 订单ID
   */
  orderId: number;

  /**
   * 评分（1-5星）
   */
  rating: number;

  /**
   * 评价内容
   */
  content?: string;

  /**
   * 评价图片URL列表
   */
  images?: string[];

  /**
   * 评价人ID
   */
  reviewerId: number;

  /**
   * 评价人信息（懒加载）
   */
  reviewer?: User;

  /**
   * 被评价人ID
   */
  reviewedUserId: number;

  /**
   * 被评价人信息（懒加载）
   */
  reviewedUser?: User;
}

/**
 * 退款请求实体
 */
export interface RefundRequest extends BaseEntity {
  /**
   * 订单ID
   */
  orderId: number;

  /**
   * 退款原因
   */
  reason: string;

  /**
   * 退款金额（单位：分）
   */
  amount: number;

  /**
   * 退款状态
   */
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED';

  /**
   * 申请人ID
   */
  applicantId: number;

  /**
   * 处理人ID
   */
  handlerId?: number;

  /**
   * 处理时间
   */
  handledAt?: string;

  /**
   * 处理备注
   */
  handlerRemark?: string;
}

// ==================== 消息相关实体 ====================

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
}

/**
 * 消息状态枚举
 */
export enum MessageStatus {
  /** 未读 */
  UNREAD = 'UNREAD',
  /** 已读 */
  READ = 'READ',
}

/**
 * 会话实体
 */
export interface Conversation extends BaseEntity {
  /**
   * 用户1 ID
   */
  user1Id: number;

  /**
   * 用户1信息（懒加载）
   */
  user1?: User;

  /**
   * 用户2 ID
   */
  user2Id: number;

  /**
   * 用户2信息（懒加载）
   */
  user2?: User;

  /**
   * 最后一条消息
   */
  lastMessage?: Message;

  /**
   * 最后消息时间
   */
  lastMessageAt?: string;

  /**
   * 未读消息数（当前用户）
   */
  unreadCount?: number;
}

/**
 * 消息实体
 */
export interface Message extends BaseEntity {
  /**
   * 会话ID
   */
  conversationId: number;

  /**
   * 发送者ID
   */
  senderId: number;

  /**
   * 发送者信息（懒加载）
   */
  sender?: User;

  /**
   * 接收者ID
   */
  receiverId: number;

  /**
   * 接收者信息（懒加载）
   */
  receiver?: User;

  /**
   * 消息内容
   */
  content: string;

  /**
   * 消息类型
   */
  type: MessageType;

  /**
   * 消息状态
   */
  status: MessageStatus;

  /**
   * 读取时间
   */
  readAt?: string;
}

/**
 * 通知实体
 */
export interface Notification extends BaseEntity {
  /**
   * 用户ID
   */
  userId: number;

  /**
   * 通知标题
   */
  title: string;

  /**
   * 通知内容
   */
  content: string;

  /**
   * 通知类型
   */
  type: 'SYSTEM' | 'ORDER' | 'MESSAGE' | 'REVIEW' | 'FAVORITE';

  /**
   * 是否已读
   */
  read: boolean;

  /**
   * 读取时间
   */
  readAt?: string;

  /**
   * 关联实体ID（可选）
   */
  relatedId?: number;

  /**
   * 跳转链接（可选）
   */
  link?: string;
}

// ==================== 社区相关实体 ====================

/**
 * 帖子实体
 */
export interface Post extends BaseEntity {
  /**
   * 发帖用户ID
   */
  userId: number;

  /**
   * 发帖用户信息（懒加载）
   */
  user?: User;

  /**
   * 帖子标题
   */
  title: string;

  /**
   * 帖子内容
   */
  content: string;

  /**
   * 帖子图片URL列表
   */
  images?: string[];

  /**
   * 浏览次数
   */
  viewCount: number;

  /**
   * 点赞次数
   */
  likeCount: number;

  /**
   * 评论次数
   */
  commentCount: number;

  /**
   * 是否置顶
   */
  pinned: boolean;

  /**
   * 回复列表（懒加载）
   */
  replies?: Reply[];
}

/**
 * 回复实体
 */
export interface Reply extends BaseEntity {
  /**
   * 帖子ID
   */
  postId: number;

  /**
   * 回复用户ID
   */
  userId: number;

  /**
   * 回复用户信息（懒加载）
   */
  user?: User;

  /**
   * 回复内容
   */
  content: string;

  /**
   * 父回复ID（顶级回复为 null）
   */
  parentId?: number;

  /**
   * 点赞次数
   */
  likeCount: number;
}

// ==================== 其他实体 ====================

/**
 * 举报实体
 */
export interface Report extends BaseEntity {
  /**
   * 举报人ID
   */
  reporterId: number;

  /**
   * 举报人信息（懒加载）
   */
  reporter?: User;

  /**
   * 被举报实体类型
   */
  targetType: 'GOODS' | 'ORDER' | 'USER' | 'POST' | 'REPLY';

  /**
   * 被举报实体ID
   */
  targetId: number;

  /**
   * 举报原因
   */
  reason: string;

  /**
   * 举报状态
   */
  status: 'PENDING' | 'PROCESSING' | 'RESOLVED' | 'REJECTED';

  /**
   * 处理人ID
   */
  handlerId?: number;

  /**
   * 处理时间
   */
  handledAt?: string;

  /**
   * 处理结果
   */
  handlerRemark?: string;
}

/**
 * 审计日志实体
 */
export interface AuditLog extends BaseEntity {
  /**
   * 操作用户ID
   */
  userId: number;

  /**
   * 操作用户信息（懒加载）
   */
  user?: User;

  /**
   * 操作类型
   */
  action: string;

  /**
   * 操作实体类型
   */
  entityType: string;

  /**
   * 操作实体ID
   */
  entityId: number;

  /**
   * 操作前数据（JSON）
   */
  oldValue?: string;

  /**
   * 操作后数据（JSON）
   */
  newValue?: string;

  /**
   * 操作IP地址
   */
  ipAddress?: string;

  /**
   * 操作User-Agent
   */
  userAgent?: string;
}
