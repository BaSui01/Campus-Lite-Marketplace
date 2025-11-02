/**
 * 常量定义
 *
 * 包含：权限码、菜单配置、业务常量等
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */

import { PERMISSION_CODES } from '@campus/shared';

// ========== 权限码常量（与后端 PermissionCodes 保持一致）==========

export const PERMISSIONS = PERMISSION_CODES;

// ========== API 路径常量 ==========

export const API_PATHS = {
  // 认证
  LOGIN: '/auth/login',
  LOGOUT: '/auth/logout',
  REFRESH_TOKEN: '/auth/refresh',

  // 用户管理
  USERS: '/users',
  BAN_USER: '/admin/users/ban',
  UNBAN_USER: (userId: number) => `/admin/users/${userId}/unban`,

  // 统计数据
  STATISTICS_OVERVIEW: '/admin/statistics/overview',
  STATISTICS_USERS: '/admin/statistics/users',
  STATISTICS_GOODS: '/admin/statistics/goods',
  STATISTICS_ORDERS: '/admin/statistics/orders',
  STATISTICS_TODAY: '/admin/statistics/today',
  STATISTICS_TREND: '/admin/statistics/trend',
  STATISTICS_TOP_GOODS: '/admin/statistics/top-goods',
  STATISTICS_TOP_USERS: '/admin/statistics/top-users',
  STATISTICS_REVENUE: '/admin/statistics/revenue',

  // 举报管理
  REPORTS_PENDING: '/reports/pending',
  HANDLE_REPORT: (id: number) => `/reports/${id}/handle`,

  // 角色权限
  ROLES: '/admin/roles',
  ROLE_DETAIL: (id: number) => `/admin/roles/${id}`,
  UPDATE_USER_ROLES: (userId: number) => `/admin/users/${userId}/roles`,

  // 限流管理
  RATE_LIMIT_RULES: '/admin/rate-limit/rules',
  RATE_LIMIT_TOGGLE: (enabled: boolean) => `/admin/rate-limit/enabled/${enabled}`,
  RATE_LIMIT_WHITELIST_USER: (userId: number) => `/admin/rate-limit/whitelist/users/${userId}`,
  RATE_LIMIT_WHITELIST_IP: (ip: string) => `/admin/rate-limit/whitelist/ips/${ip}`,
  RATE_LIMIT_BLACKLIST_IP: (ip: string) => `/admin/rate-limit/blacklist/ips/${ip}`,

  // 回收站
  SOFT_DELETE_TARGETS: '/admin/soft-delete/targets',
  SOFT_DELETE_RESTORE: (entity: string, id: number) => `/admin/soft-delete/${entity}/${id}/restore`,
  SOFT_DELETE_PURGE: (entity: string, id: number) => `/admin/soft-delete/${entity}/${id}/purge`,

  // 通知模板
  NOTIFICATION_TEMPLATES: '/admin/notification/templates',
  NOTIFICATION_TEMPLATE_RENDER: (code: string) => `/admin/notification/templates/render/${code}`,

  // 合规管理
  COMPLIANCE_WHITELIST: '/admin/compliance/whitelist',
  COMPLIANCE_AUDIT: '/admin/compliance/audit',
} as const;

// ========== 菜单配置 ==========

export interface MenuItem {
  key: string;
  label: string;
  icon?: string;
  path?: string;
  permission?: string;
  children?: MenuItem[];
}

export const MENU_ITEMS: MenuItem[] = [
  {
    key: 'dashboard',
    label: '仪表盘',
    icon: 'DashboardOutlined',
    path: '/admin/dashboard',
    permission: PERMISSIONS.SYSTEM_STATISTICS_VIEW,
  },
  {
    key: 'users',
    label: '用户管理',
    icon: 'UserOutlined',
    permission: PERMISSIONS.SYSTEM_USER_VIEW,
    children: [
      {
        key: 'users-list',
        label: '用户列表',
        path: '/admin/users/list',
      },
      {
        key: 'users-banned',
        label: '封禁记录',
        path: '/admin/users/banned',
        permission: PERMISSIONS.SYSTEM_USER_BAN,
      },
    ],
  },
  {
    key: 'content',
    label: '内容管理',
    icon: 'FileTextOutlined',
    children: [
      {
        key: 'content-goods',
        label: '商品审核',
        path: '/admin/content/goods',
        permission: PERMISSIONS.SYSTEM_GOODS_APPROVE,
      },
      {
        key: 'content-posts',
        label: '帖子审核',
        path: '/admin/content/posts',
        permission: PERMISSIONS.SYSTEM_POST_APPROVE,
      },
      {
        key: 'content-reports',
        label: '举报处理',
        path: '/admin/content/reports',
        permission: PERMISSIONS.SYSTEM_REPORT_HANDLE,
      },
    ],
  },
  {
    key: 'roles',
    label: '角色权限',
    icon: 'SafetyOutlined',
    path: '/admin/roles',
    permission: PERMISSIONS.SYSTEM_ROLE_ASSIGN,
  },
  {
    key: 'system',
    label: '系统管理',
    icon: 'SettingOutlined',
    children: [
      {
        key: 'system-rate-limit',
        label: '限流管理',
        path: '/admin/system/rate-limit',
        permission: PERMISSIONS.SYSTEM_RATE_LIMIT_MANAGE,
      },
      {
        key: 'system-notifications',
        label: '通知模板',
        path: '/admin/system/notifications',
      },
      {
        key: 'system-compliance',
        label: '合规管理',
        path: '/admin/system/compliance',
        permission: PERMISSIONS.SYSTEM_COMPLIANCE_REVIEW,
      },
      {
        key: 'system-recycle-bin',
        label: '回收站',
        path: '/admin/system/recycle-bin',
      },
    ],
  },
  {
    key: 'logs',
    label: '日志管理',
    icon: 'FileSearchOutlined',
    permission: PERMISSIONS.SYSTEM_AUDIT_VIEW,
    children: [
      {
        key: 'logs-audit',
        label: '审计日志',
        path: '/admin/logs/audit',
      },
      {
        key: 'logs-operation',
        label: '操作日志',
        path: '/admin/logs/operation',
      },
    ],
  },
];

// ========== 其他常量 ==========

/**
 * 默认分页配置
 */
export const DEFAULT_PAGE_SIZE = 20;
export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];

/**
 * 日期格式
 */
export const DATE_FORMAT = 'YYYY-MM-DD';
export const DATETIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';
