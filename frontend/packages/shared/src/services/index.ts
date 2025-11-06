/**
 * API 服务导出入口
 * @author BaSui 😎
 * @description 导出所有 API 服务
 */

// ==================== 认证服务 ====================
export { authService } from './auth';
export type { AuthService } from './auth';

// ==================== 用户服务 ====================
export { userService } from './user';
export type { UserService } from './user';

// ==================== 物品服务 ====================
export { goodsService } from './goods';
export type { GoodsService } from './goods';

// ==================== 评价服务 ====================
export { reviewService } from './goods/review';
export type {
  ReviewService,
  CreateReviewRequest,
  ReviewListQuery,
  ReviewListResponse,
  ReviewDetail,
  ReviewStatistics,
} from './goods/review';

// ==================== 订单服务 ====================
export { orderService } from './order';
export type { OrderService } from './order';

// ==================== 退款服务 ====================
export { refundService, RefundStatus } from './refund';
export type { 
  RefundService, 
  Refund,
  RefundListParams,
  RefundReviewRequest
} from './refund';

// ==================== 消息服务 ====================
export { messageService } from './message';
export type { MessageService } from './message';

// ==================== 帖子服务 ====================
export { postService } from './post';
export type { PostService } from './post';

// ==================== 文件上传服务 ====================
export { uploadService } from './upload';
export type { UploadService } from './upload';

// ==================== 管理端统计服务 ====================
export { statisticsService } from './statistics';
export type { StatisticsService } from './statistics';

// ==================== 举报管理服务 ====================
export { reportService } from './report';
export type { ReportService } from './report';

// ==================== 角色权限服务 ====================
export { roleService } from './role';
export type { RoleService } from './role';

// ==================== 限流管理服务 ====================
export { rateLimitService } from './rateLimit';
export type { RateLimitService } from './rateLimit';

// ==================== 通知模板服务 ====================
export { notificationTemplateService } from './notificationTemplate';
export type { NotificationTemplateService } from './notificationTemplate';

// ==================== 合规审计服务 ====================
export { complianceService } from './compliance';
export type { ComplianceService } from './compliance';

// ==================== 软删除治理服务 ====================
export { softDeleteService } from './softDelete';
export type { SoftDeleteService } from './softDelete';

// ==================== 管理端用户服务 ====================
export { adminUserService } from './adminUser';
export type { AdminUserService } from './adminUser';

// ==================== 撤销操作服务 ====================
export { revertService, RevertService } from './revert';
export type { 
  CreateRevertRequest, 
  RevertExecutionResult, 
  RevertRequestParams 
} from './revert';

// ==================== 申诉服务 ====================
export { appealService } from './appeal';
export type { 
  AppealService,
  Appeal,
  AppealDetail,
  AppealMaterial,
  AppealListParams,
  AppealReviewRequest,
  BatchAppealReviewRequest,
  AppealStatistics
} from './appeal';

// ==================== 校园管理服务 ====================
export { campusService, CampusStatus } from './campus';
export type {
  CampusService,
  Campus,
  CampusListParams,
  CampusStatistics,
  CampusRequest
} from './campus';

// ==================== 分类管理服务 ====================
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

// ==================== 标签管理服务 ====================
export { tagService, TagType, TagStatus } from './tag';
export type {
  TagService,
  Tag,
  TagListParams,
  TagRequest,
  TagMergeRequest,
  HotTag
} from './tag';

// ==================== 功能开关服务 ====================
export { featureFlagService, FeatureFlagStatus, GrayStrategy, Environment } from './featureFlag';
export type {
  FeatureFlagService,
  FeatureFlag,
  FeatureFlagListParams,
  FeatureFlagRequest,
  GrayRuleConfig,
  FeatureFlagLog
} from './featureFlag';

// ==================== 系统监控服务 ====================
export { monitorService, ErrorSeverity } from './monitor';
export type {
  MonitorService,
  HealthCheckResponse,
  SystemMetrics,
  HealthCheckRecord,
  ApiPerformanceLog,
  EndpointStats,
  QpsData,
  ErrorLog,
  PerformanceReport
} from './monitor';

// ==================== 任务调度服务 ====================
export { taskService, TaskStatus } from './task';
export type {
  TaskService,
  ScheduledTask,
  TaskExecutionLog,
  TaskStatistics
} from './task';

// ==================== 话题管理服务 ====================
export { topicService } from './topic';
export type {
  TopicService,
  Topic,
  TopicStatistics,
  CreateTopicRequest,
  UpdateTopicRequest
} from './topic';

// ==================== 纠纷统计服务 ====================
export { disputeStatisticsService } from './disputeStatistics';
export type {
  DisputeStatisticsService,
  DisputeStatistics
} from './disputeStatistics';

// ==================== 社区广场服务 ====================
export { communityService } from './community';
export type {
  CommunityService,
  UserFeed,
  PostTopicTag,
  PostInteractionStats
} from './community';

// ==================== 物流服务 ====================
export { logisticsService } from './logistics';
export type {
  LogisticsService,
  Logistics,
  LogisticsTrack,
  LogisticsStatistics
} from './logistics';

// ==================== 推荐服务 ====================
export { recommendService } from './recommend';
export type {
  RecommendService,
  RecommendParams,
  RecommendResult
} from './recommend';

// ==================== 收藏服务 ====================
export { favoriteService } from './favorite';
export type {
  FavoriteService,
  FavoriteListParams,
  FavoriteStatistics
} from './favorite';

// ==================== 通知服务 ====================
export { notificationService, NotificationStatus, NotificationType } from './notification';
export type {
  NotificationService,
  NotificationListParams,
  PageNotificationResponse
} from './notification';

// ==================== 关注服务 ====================
export { followService } from './follow';
export type {
  FollowService,
  FollowingActivity
} from './follow';

// ==================== 订阅服务 ====================
export { subscriptionService, SubscriptionType } from './subscription';
export type {
  SubscriptionService,
  CreateSubscriptionParams,
  SubscriptionMatch
} from './subscription';


export { creditService, CreditLevel, CREDIT_LEVEL_CONFIG } from './credit';
export type { 
  UserCreditInfo, 
  CreditLevelInfo, 
  CreditHistoryItem, 
  CreditStatistics 
} from './credit';
