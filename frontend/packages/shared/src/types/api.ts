/**
 * API 请求/响应类型定义
 * @author BaSui 😎
 * @description 前端 API 接口的 Request 和 Response 类型
 */

import type {
  User,
  Goods,
  Order,
  Category,
  Message,
  Conversation,
  Notification,
  Post,
  Reply,
  Review,
  Favorite,
  Report,
  PointsLog,
  BanLog,
} from './entity';
import type {
  GoodsStatus,
  OrderStatus,
  PaymentMethod,
  GoodsSortField,
  PostSortField,
  SortOrder,
} from './enum';

// ==================== 通用 API 类型 ====================

/**
 * API 响应基础结构
 */
export interface ApiResponse<T = any> {
  /**
   * 响应状态码
   */
  code: number;

  /**
   * 响应消息
   */
  message: string;

  /**
   * 响应数据
   */
  data: T;

  /**
   * 时间戳
   */
  timestamp?: number;
}

/**
 * 分页参数
 */
export interface PageParams {
  /**
   * 当前页码（从 1 开始）
   */
  page?: number;

  /**
   * 每页条数
   */
  pageSize?: number;

  /**
   * 排序字段
   */
  sortField?: string;

  /**
   * 排序方向
   */
  sortOrder?: SortOrder;
}

/**
 * 分页响应数据
 */
export interface PageInfo<T = any> {
  /**
   * 数据列表
   */
  list: T[];

  /**
   * 总记录数
   */
  total: number;

  /**
   * 当前页码
   */
  page: number;

  /**
   * 每页条数
   */
  pageSize: number;

  /**
   * 总页数
   */
  totalPages: number;

  /**
   * 是否有下一页
   */
  hasNext: boolean;

  /**
   * 是否有上一页
   */
  hasPrev: boolean;
}

/**
 * ID 参数类型
 */
export interface IdParam {
  id: number;
}

/**
 * ID 列表参数类型
 */
export interface IdsParam {
  ids: number[];
}

// ==================== 认证相关 API 类型 ====================

/**
 * 注册请求参数
 */
export interface RegisterRequest {
  /**
   * 用户名
   */
  username: string;

  /**
   * 密码
   */
  password: string;

  /**
   * 邮箱
   */
  email?: string;

  /**
   * 手机号
   */
  phone?: string;

  /**
   * 验证码
   */
  code?: string;
}

/**
 * 注册响应数据
 */
export interface RegisterResponse {
  /**
   * 用户ID
   */
  userId: number;

  /**
   * 用户名
   */
  username: string;

  /**
   * 访问令牌
   */
  accessToken: string;

  /**
   * 刷新令牌
   */
  refreshToken: string;
}

/**
 * 登录请求参数
 */
export interface LoginRequest {
  /**
   * 用户名或邮箱或手机号
   */
  username: string;

  /**
   * 密码
   */
  password: string;

  /**
   * 记住我
   */
  remember?: boolean;
}

/**
 * 登录响应数据
 */
export interface LoginResponse {
  /**
   * 用户信息
   */
  user: User;

  /**
   * 访问令牌
   */
  accessToken: string;

  /**
   * 刷新令牌
   */
  refreshToken: string;

  /**
   * Token 过期时间（秒）
   */
  expiresIn: number;
}

/**
 * Token 刷新请求参数
 */
export interface RefreshTokenRequest {
  /**
   * 刷新令牌
   */
  refreshToken: string;
}

/**
 * Token 刷新响应数据
 */
export interface RefreshTokenResponse {
  /**
   * 新的访问令牌
   */
  accessToken: string;

  /**
   * 新的刷新令牌
   */
  refreshToken: string;

  /**
   * Token 过期时间（秒）
   */
  expiresIn: number;
}

// ==================== 用户相关 API 类型 ====================

/**
 * 更新用户资料请求参数
 */
export interface UpdateProfileRequest {
  /**
   * 昵称
   */
  nickname?: string;

  /**
   * 头像URL
   */
  avatar?: string;

  /**
   * 邮箱
   */
  email?: string;

  /**
   * 手机号
   */
  phone?: string;
}

/**
 * 修改密码请求参数
 */
export interface ChangePasswordRequest {
  /**
   * 旧密码
   */
  oldPassword: string;

  /**
   * 新密码
   */
  newPassword: string;
}

/**
 * 用户列表查询参数
 */
export interface UserListQuery extends PageParams {
  /**
   * 关键词搜索（用户名、昵称、邮箱）
   */
  keyword?: string;

  /**
   * 用户状态
   */
  status?: string;

  /**
   * 校区ID
   */
  campusId?: number;
}

// ==================== 物品相关 API 类型 ====================

/**
 * 发布物品请求参数
 */
export interface PublishGoodsRequest {
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
   * 分类ID
   */
  categoryId: number;

  /**
   * 校区ID
   */
  campusId: number;

  /**
   * 图片URL列表
   */
  images: string[];

  /**
   * 库存数量
   */
  stock?: number;

  /**
   * 扩展属性（如品牌、成色、尺寸等）
   */
  extraAttrs?: Record<string, any>;

  /**
   * 标签ID列表
   */
  tagIds?: number[];
}

/**
 * 更新物品请求参数
 */
export interface UpdateGoodsRequest extends Partial<PublishGoodsRequest> {
  /**
   * 物品ID
   */
  id: number;
}

/**
 * 物品列表查询参数
 */
export interface GoodsListQuery extends PageParams {
  /**
   * 关键词搜索（标题、描述）
   */
  keyword?: string;

  /**
   * 分类ID
   */
  categoryId?: number;

  /**
   * 校区ID
   */
  campusId?: number;

  /**
   * 物品状态
   */
  status?: GoodsStatus;

  /**
   * 卖家ID
   */
  sellerId?: number;

  /**
   * 最低价格（单位：分）
   */
  minPrice?: number;

  /**
   * 最高价格（单位：分）
   */
  maxPrice?: number;

  /**
   * 标签ID列表
   */
  tagIds?: number[];

  /**
   * 排序字段
   */
  sortField?: GoodsSortField;
}

/**
 * 审核物品请求参数
 */
export interface AuditGoodsRequest {
  /**
   * 物品ID
   */
  id: number;

  /**
   * 是否通过
   */
  approved: boolean;

  /**
   * 拒绝原因（审核不通过时必填）
   */
  reason?: string;
}

// ==================== 订单相关 API 类型 ====================

/**
 * 创建订单请求参数
 */
export interface CreateOrderRequest {
  /**
   * 物品ID
   */
  goodsId: number;

  /**
   * 数量
   */
  quantity?: number;

  /**
   * 备注
   */
  remark?: string;
}

/**
 * 创建订单响应数据
 */
export interface CreateOrderResponse {
  /**
   * 订单号
   */
  orderNo: string;

  /**
   * 订单金额（单位：分）
   */
  amount: number;

  /**
   * 订单ID
   */
  orderId: number;
}

/**
 * 订单列表查询参数
 */
export interface OrderListQuery extends PageParams {
  /**
   * 订单状态
   */
  status?: OrderStatus;

  /**
   * 关键词搜索（订单号、物品名称）
   */
  keyword?: string;

  /**
   * 开始日期
   */
  startDate?: string;

  /**
   * 结束日期
   */
  endDate?: string;
}

/**
 * 支付订单请求参数
 */
export interface PayOrderRequest {
  /**
   * 订单号
   */
  orderNo: string;

  /**
   * 支付方式
   */
  paymentMethod: PaymentMethod;
}

/**
 * 支付订单响应数据
 */
export interface PayOrderResponse {
  /**
   * 支付跳转URL（微信、支付宝）
   */
  payUrl?: string;

  /**
   * 二维码内容（扫码支付）
   */
  qrCode?: string;

  /**
   * 支付状态
   */
  status: string;
}

/**
 * 取消订单请求参数
 */
export interface CancelOrderRequest {
  /**
   * 订单号
   */
  orderNo: string;

  /**
   * 取消原因
   */
  reason?: string;
}

/**
 * 确认收货请求参数
 */
export interface ConfirmReceiptRequest {
  /**
   * 订单号
   */
  orderNo: string;
}

/**
 * 申请退款请求参数
 */
export interface RequestRefundRequest {
  /**
   * 订单号
   */
  orderNo: string;

  /**
   * 退款原因
   */
  reason: string;

  /**
   * 退款金额（单位：分，默认为订单金额）
   */
  amount?: number;
}

// ==================== 评价相关 API 类型 ====================

/**
 * 创建评价请求参数
 */
export interface CreateReviewRequest {
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
}

// ==================== 收藏相关 API 类型 ====================

/**
 * 我的收藏列表查询参数
 */
export interface FavoriteListQuery extends PageParams {
  /**
   * 分类ID
   */
  categoryId?: number;
}

// ==================== 消息相关 API 类型 ====================

/**
 * 发送消息请求参数
 */
export interface SendMessageRequest {
  /**
   * 接收者ID
   */
  receiverId: number;

  /**
   * 消息内容
   */
  content: string;

  /**
   * 消息类型
   */
  type?: string;
}

/**
 * 会话列表查询参数
 */
export interface ConversationListQuery extends PageParams {
  /**
   * 关键词搜索（用户名）
   */
  keyword?: string;
}

/**
 * 消息列表查询参数
 */
export interface MessageListQuery extends PageParams {
  /**
   * 会话ID
   */
  conversationId: number;
}

/**
 * 标记消息已读请求参数
 */
export interface MarkMessageReadRequest {
  /**
   * 消息ID列表
   */
  messageIds: number[];
}

// ==================== 通知相关 API 类型 ====================

/**
 * 通知列表查询参数
 */
export interface NotificationListQuery extends PageParams {
  /**
   * 通知类型
   */
  type?: string;

  /**
   * 是否已读
   */
  read?: boolean;
}

/**
 * 标记通知已读请求参数
 */
export interface MarkNotificationReadRequest {
  /**
   * 通知ID列表
   */
  notificationIds: number[];
}

// ==================== 帖子相关 API 类型 ====================

/**
 * 创建帖子请求参数
 */
export interface CreatePostRequest {
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
}

/**
 * 更新帖子请求参数
 */
export interface UpdatePostRequest extends Partial<CreatePostRequest> {
  /**
   * 帖子ID
   */
  id: number;
}

/**
 * 帖子列表查询参数
 */
export interface PostListQuery extends PageParams {
  /**
   * 关键词搜索（标题、内容）
   */
  keyword?: string;

  /**
   * 发帖用户ID
   */
  userId?: number;

  /**
   * 排序字段
   */
  sortField?: PostSortField;
}

/**
 * 创建回复请求参数
 */
export interface CreateReplyRequest {
  /**
   * 帖子ID
   */
  postId: number;

  /**
   * 回复内容
   */
  content: string;

  /**
   * 父回复ID（可选，用于多级回复）
   */
  parentId?: number;
}

// ==================== 举报相关 API 类型 ====================

/**
 * 创建举报请求参数
 */
export interface CreateReportRequest {
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
}

/**
 * 举报列表查询参数
 */
export interface ReportListQuery extends PageParams {
  /**
   * 举报状态
   */
  status?: string;

  /**
   * 被举报实体类型
   */
  targetType?: string;
}

/**
 * 处理举报请求参数
 */
export interface HandleReportRequest {
  /**
   * 举报ID
   */
  id: number;

  /**
   * 处理结果（RESOLVED / REJECTED）
   */
  status: 'RESOLVED' | 'REJECTED';

  /**
   * 处理备注
   */
  remark?: string;
}

// ==================== 文件上传相关 API 类型 ====================

/**
 * 文件上传响应数据
 */
export interface UploadResponse {
  /**
   * 文件URL
   */
  url: string;

  /**
   * 文件名
   */
  filename: string;

  /**
   * 文件大小（字节）
   */
  size: number;

  /**
   * 文件类型
   */
  mimeType: string;
}

// ==================== 统计相关 API 类型 ====================

/**
 * 统计数据响应
 */
export interface StatisticsResponse {
  /**
   * 用户总数
   */
  totalUsers?: number;

  /**
   * 物品总数
   */
  totalGoods?: number;

  /**
   * 订单总数
   */
  totalOrders?: number;

  /**
   * 今日新增用户
   */
  todayNewUsers?: number;

  /**
   * 今日新增物品
   */
  todayNewGoods?: number;

  /**
   * 今日新增订单
   */
  todayNewOrders?: number;

  /**
   * 交易总额（单位：分）
   */
  totalAmount?: number;
}
