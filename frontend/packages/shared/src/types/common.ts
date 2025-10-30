/**
 * 🎯 BaSui 的通用类型定义
 * @description 整个应用通用的类型定义
 */

// ==================== 分页相关 ====================

/** 分页参数 */
export interface PageParams {
  /** 页码（从0开始） */
  page: number;
  /** 每页数量 */
  size: number;
  /** 排序字段 */
  sort?: string;
  /** 排序方向 */
  direction?: 'ASC' | 'DESC';
}

/** 分页响应 */
export interface PageResponse<T> {
  /** 数据列表 */
  content: T[];
  /** 总元素数 */
  totalElements: number;
  /** 总页数 */
  totalPages: number;
  /** 当前页码 */
  number: number;
  /** 每页数量 */
  size: number;
  /** 是否首页 */
  first: boolean;
  /** 是否末页 */
  last: boolean;
  /** 是否为空 */
  empty: boolean;
}

// ==================== 选项相关 ====================

/** 选项类型 */
export interface Option<V = any> {
  label: string;
  value: V;
  disabled?: boolean;
  icon?: string;
}

// ==================== 表单相关 ====================

/** 表单字段错误 */
export interface FieldError {
  field: string;
  message: string;
}

/** 表单验证结果 */
export interface ValidationResult {
  valid: boolean;
  errors: FieldError[];
}

// ==================== 文件上传相关 ====================

/** 文件信息 */
export interface FileInfo {
  /** 文件名 */
  name: string;
  /** 文件大小（字节） */
  size: number;
  /** 文件类型 */
  type: string;
  /** 文件 URL */
  url: string;
  /** 文件 ID */
  id?: string;
  /** 上传状态 */
  status?: 'uploading' | 'done' | 'error';
  /** 上传进度 */
  percent?: number;
}

// ==================== 组件通用 Props ====================

/** 基础组件 Props */
export interface BaseComponentProps {
  /** 样式类名 */
  className?: string;
  /** 内联样式 */
  style?: React.CSSProperties;
  /** 数据测试 ID */
  'data-testid'?: string;
}

// ==================== 用户相关 ====================

/** 用户状态 */
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  BANNED = 'BANNED',
}

/** 用户角色 */
export enum UserRole {
  USER = 'USER',
  ADMIN = 'ADMIN',
  SUPER_ADMIN = 'SUPER_ADMIN',
}

// ==================== 物品相关 ====================

/** 物品状态 */
export enum GoodsStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  SOLD = 'SOLD',
}

/** 物品分类 */
export enum GoodsCategory {
  ELECTRONICS = 'ELECTRONICS',
  BOOKS = 'BOOKS',
  CLOTHING = 'CLOTHING',
  FURNITURE = 'FURNITURE',
  SPORTS = 'SPORTS',
  OTHER = 'OTHER',
}

// ==================== 订单相关 ====================

/** 订单状态 */
export enum OrderStatus {
  PENDING_PAYMENT = 'PENDING_PAYMENT',
  PAID = 'PAID',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

/** 支付方式 */
export enum PaymentMethod {
  WECHAT = 'WECHAT',
  ALIPAY = 'ALIPAY',
  POINTS = 'POINTS',
}

// ==================== 消息相关 ====================

/** 消息类型 */
export enum MessageType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  SYSTEM = 'SYSTEM',
}

/** 消息状态 */
export enum MessageStatus {
  UNREAD = 'UNREAD',
  READ = 'READ',
}

// ==================== 工具类型 ====================

/** 将所有属性变为可选 */
export type DeepPartial<T> = {
  [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P];
};

/** 提取 Promise 的返回类型 */
export type PromiseType<T> = T extends Promise<infer U> ? U : T;

/** 排除 null 和 undefined */
export type NonNullableFields<T> = {
  [P in keyof T]-?: NonNullable<T[P]>;
};
