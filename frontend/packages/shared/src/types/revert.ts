/**
 * 撤销系统类型定义
 * @author BaSui 😎
 * @description 数据撤销相关的类型定义（临时手动创建，后续api:generate会自动生成）
 */

/**
 * 撤销请求状态枚举
 */
export enum RevertRequestStatus {
  PENDING = 'PENDING',      // 待处理
  APPROVED = 'APPROVED',    // 已批准
  REJECTED = 'REJECTED',    // 已拒绝
  EXECUTED = 'EXECUTED',    // 已执行
  FAILED = 'FAILED',        // 执行失败
  CANCELLED = 'CANCELLED'   // 已取消
}

/**
 * 撤销请求状态文本映射
 */
export const REVERT_STATUS_TEXT: Record<RevertRequestStatus, string> = {
  [RevertRequestStatus.PENDING]: '待处理',
  [RevertRequestStatus.APPROVED]: '已批准',
  [RevertRequestStatus.REJECTED]: '已拒绝',
  [RevertRequestStatus.EXECUTED]: '已执行',
  [RevertRequestStatus.FAILED]: '执行失败',
  [RevertRequestStatus.CANCELLED]: '已取消'
};

/**
 * 创建撤销请求DTO
 */
export interface CreateRevertRequest {
  reason: string;  // 撤销原因（必填，最多500字符）
}

/**
 * 撤销执行结果
 */
export interface RevertExecutionResult {
  success: boolean;          // 是否执行成功
  message: string;           // 结果消息
  entityId?: number;         // 撤销的实体ID
  entityType?: string;       // 撤销的实体类型
  executionTime?: number;    // 执行耗时（毫秒）
  additionalData?: any;      // 附加数据
}

/**
 * 撤销请求实体
 */
export interface RevertRequest {
  id: number;
  auditLogId: number;
  requesterId: number;
  requesterName?: string;
  reason: string;
  status: RevertRequestStatus;
  approvedBy?: number;
  approvedByName?: string;
  approvedAt?: string;
  approvalComment?: string;
  revertLogId?: number;
  executedAt?: string;
  errorMessage?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 撤销请求响应（带分页）
 */
export interface RevertRequestResponse {
  content: RevertRequest[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

