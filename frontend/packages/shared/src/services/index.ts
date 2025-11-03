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

// ==================== 订单服务 ====================
export { orderService } from './order';
export type { OrderService } from './order';

// ==================== 退款服务 ====================
export { refundService, RefundStatus } from './refund';
export type { RefundService, RefundRequest, ApplyRefundRequest, RefundListQuery } from './refund';

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
