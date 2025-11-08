/**
 * API 服务导出入口
 * @author BaSui 😎
 * @description 只导出真正两端共享的服务和通用工具服务
 *
 * ⚠️ 重构说明：
 * - 管理端专属服务已移至 admin/src/services
 * - 客户端专属服务已移至 portal/src/services
 * - 此处仅保留两端都使用的核心服务
 */

// ==================== 认证服务（两端共享）====================
export { authService } from './auth';
export type { AuthService } from './auth';

// ==================== 用户服务（两端共享）====================
export { userService } from './user';
export type { UserService } from './user';

// ==================== 商品服务（两端共享）====================
export { goodsService } from './goods';
export type { GoodsService } from './goods';

// ==================== 评价服务（两端共享）====================
export { reviewService } from './goods/review';
export type {
  ReviewService,
  CreateReviewRequest,
  ReviewListQuery,
  ReviewListResponse,
  ReviewDetail,
  ReviewStatistics,
} from './goods/review';

// ==================== 订单服务（两端共享）====================
export { orderService } from './order';
export type { OrderService } from './order';

// ==================== 消息服务（两端共享）====================
export { messageService } from './message';
export type { MessageService } from './message';

// ==================== 支付服务（两端共享）====================
export { paymentService } from './payment';
export type {
  PaymentService,
  PaymentRecord,
  PaymentStatistics,
  PaymentListParams
} from './payment';

// ==================== 导出服务（两端共享）====================
export { exportService, ExportType } from './export';
export type {
  ExportService,
  ExportJob,
  ExportRequest
} from './export';

// ==================== 分类服务（两端共享）====================
export { categoryService, CategoryStatus } from './category';
export type {
  CategoryService,
  Category,
  CategoryTreeNode,
  CategoryListParams,
  CategoryRequest,
  CategorySortRequest,
  CategoryStatistics
} from './category';

// ==================== 物流服务（两端共享）====================
export { logisticsService } from './logistics';
export type {
  LogisticsService,
  Logistics,
  LogisticsTrack,
  LogisticsStatistics
} from './logistics';

// ==================== 通用工具服务 ====================

// 文件上传服务
export { uploadService } from './upload';
export type { UploadService } from './upload';

// 退款服务
export { refundService, RefundStatus } from './refund';
export type {
  RefundService,
  Refund,
  RefundListParams,
  RefundReviewRequest
} from './refund';

// 帖子服务
export { postService } from './post';
export type { PostService } from './post';

// 校园服务
export { campusService, CampusStatus } from './campus';
export type {
  CampusService,
  Campus,
  CampusListParams,
  CampusStatistics,
  CampusRequest
} from './campus';

// 标签服务
export { tagService, TagType, TagStatus } from './tag';
export type {
  TagService,
  Tag,
  TagListParams,
  TagRequest,
  TagMergeRequest,
  HotTag
} from './tag';

// 社区服务
export { communityService } from './community';
export type {
  CommunityService,
  UserFeed,
  PostTopicTag,
  PostInteractionStats
} from './community';

// 话题服务
export { topicService } from './topic';
export type {
  TopicService,
  Topic,
  TopicStatistics,
  CreateTopicRequest,
  UpdateTopicRequest
} from './topic';

// 任务服务
export { taskService, TaskStatus } from './task';
export type {
  TaskService,
  ScheduledTask,
  TaskExecutionLog,
  TaskStatistics
} from './task';

// 通知服务
export { notificationService, NotificationStatus } from './notification';
export type {
  NotificationService,
  NotificationListParams,
  PageNotificationResponse
} from './notification';

// 通知偏好服务
export { notificationPreferenceService, NotificationChannel, NotificationType } from './notificationPreference';
export type {
  NotificationPreference,
  QuietHoursConfig,
  NotificationTypeInfo
} from './notificationPreference';

// 通知模板服务
export { notificationTemplateService } from './notificationTemplate';
export type { NotificationTemplateService } from './notificationTemplate';

// 软删除服务
export { softDeleteService } from './softDelete';
export type { SoftDeleteService } from './softDelete';

// 撤销操作服务
export { revertService, RevertService } from './revert';
export type {
  CreateRevertRequest,
  RevertExecutionResult,
  RevertRequestParams
} from './revert';

// 限流服务
export { rateLimitService } from './rateLimit';
export type { RateLimitService } from './rateLimit';

// ==================== ❌ 已移除的服务导出 ====================
//
// 以下服务已移至对应的包，不再从 shared 导出：
//
// 📦 移至 admin/services:
// - statistics (管理端统计)
// - adminUser (管理员用户管理)
// - adminGoods (管理员商品管理)
// - adminCategory (管理员分类管理)
// - monitor (系统监控)
// - compliance (合规审计)
// - dispute (纠纷管理)
// - disputeStatistics (纠纷统计)
// - appeal (申诉管理)
// - blacklist (黑名单管理)
// - report (举报管理)
// - featureFlag (功能开关)
// - role (角色权限)
//
// 📦 移至 portal/services:
// - favorite (收藏)
// - follow (关注)
// - credit (信用)
// - recommend (推荐)
// - marketing (营销活动)
// - sellerStatistics (卖家统计)
// - subscription (订阅)
//
