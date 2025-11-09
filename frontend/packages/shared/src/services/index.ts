/**
 * API 服务导出入口
 * @author BaSui 😎
 * @description 只导出真正两端共享的服务和通用工具服务
 *
 * ⚠️ 重要说明：
 * - 此文件只导出 Service 类，不导出类型定义
 * - 所有类型定义应从 '@campus/shared/api' 导入（OpenAPI 生成）
 * - 避免与 API 自动生成的类型冲突
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
export type { ReviewService, ReviewListQuery, ReviewDetail, ReviewStatistics } from './goods/review';
// 注意：CreateReviewRequest 应从 '@campus/shared/api' 导入，避免冲突

// ==================== 订单服务（两端共享）====================
export { orderService } from './order';
export type { OrderService } from './order';

// ==================== 消息服务（两端共享）====================
export { messageService } from './message';
export type { MessageService } from './message';

// ==================== 支付服务（两端共享）====================
export { paymentService } from './payment';
export type { PaymentService, PaymentRecord, PaymentStatistics, PaymentListParams } from './payment';

// ==================== 导出服务（两端共享）====================
export { exportService, ExportType } from './export';
export type { ExportService, ExportRequest } from './export';
// 注意：ExportJob 应从 '@campus/shared/api' 导入，避免冲突

// ==================== 分类服务（两端共享）====================
export { categoryService, CategoryStatus } from './category';
export type { CategoryService, CategoryTreeNode, CategoryListParams, CategoryRequest, CategorySortRequest, CategoryStatistics } from './category';
// 注意：Category 应从 '@campus/shared/api' 导入，避免冲突

// ==================== 物流服务（两端共享）====================
export { logisticsService } from './logistics';
export type { LogisticsService, LogisticsTrack, LogisticsStatistics } from './logistics';
// 注意：Logistics 应从 '@campus/shared/api' 导入，避免冲突

// ==================== 通用工具服务 ====================

// 文件上传服务
export { uploadService } from './upload';
export type { UploadService } from './upload';

// 退款服务
export { refundService, RefundStatus } from './refund';
export type { RefundService, RefundListParams, RefundReviewRequest } from './refund';
// 注意：Refund 应从 '@campus/shared/api' 导入，避免冲突

// 帖子服务
export { postService } from './post';
export type { PostService } from './post';

// 校园服务
export { campusService, CampusStatus } from './campus';
export type { CampusService, CampusListParams, CampusStatistics, CampusRequest } from './campus';
// 注意：Campus 应从 '@campus/shared/api' 导入，避免冲突

// 标签服务
export { tagService, TagType, TagStatus } from './tag';
export type { TagService, TagListParams, TagRequest, TagMergeRequest, HotTag } from './tag';
// 注意：Tag 应从 '@campus/shared/api' 导入，避免冲突

// 社区服务
export { communityService } from './community';
export type { CommunityService, PostTopicTag, PostInteractionStats } from './community';
// 注意：UserFeed 应从 '@campus/shared/api' 导入，避免冲突

// 话题服务
export { topicService } from './topic';
export type { TopicService, TopicStatistics, CreateTopicRequest, UpdateTopicRequest } from './topic';
// 注意：Topic 应从 '@campus/shared/api' 导入，避免冲突

// 任务服务
export { taskService, TaskStatus } from './task';
export type { TaskService, TaskExecutionLog, TaskStatistics } from './task';
// 注意：ScheduledTask 应从 '@campus/shared/api' 导入，避免冲突

// 通知服务
export { notificationService, NotificationStatus } from './notification';
export type { NotificationService, NotificationListParams } from './notification';
// 注意：PageNotificationResponse 应从 '@campus/shared/api' 导入，避免冲突

// 通知偏好服务
export { notificationPreferenceService, NotificationChannel, NotificationType } from './notificationPreference';
export type { NotificationPreference, QuietHoursConfig, NotificationTypeInfo } from './notificationPreference';

// 通知模板服务
export { notificationTemplateService } from './notificationTemplate';
export type { NotificationTemplateService } from './notificationTemplate';

// 软删除服务
export { softDeleteService } from './softDelete';
export type { SoftDeleteService } from './softDelete';

// 撤销操作服务
export { revertService, RevertService } from './revert';
export type { RevertRequestParams } from './revert';
// 注意：CreateRevertRequest, RevertExecutionResult 应从 '@campus/shared/api' 导入，避免冲突

// 限流服务
export { rateLimitService } from './rateLimit';
export type { RateLimitService } from './rateLimit';

// 批量任务服务
export { batchService, BatchTaskStatus, BatchType } from './batch';
export type { BatchService, BatchTaskListParams } from './batch';
// 注意：PageBatchTaskResponse 应从 '@campus/shared/api' 导入，避免冲突

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
