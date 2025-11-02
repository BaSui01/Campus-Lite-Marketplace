/**
 * 菜单配置
 * @author BaSui 😎
 * @date 2025-11-02
 */

import { PERMISSION_CODES } from '@campus/shared';

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
    permission: PERMISSION_CODES.SYSTEM_STATISTICS_VIEW,
  },
  {
    key: 'users',
    label: '用户管理',
    icon: 'UserOutlined',
    permission: PERMISSION_CODES.SYSTEM_USER_VIEW,
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
        permission: PERMISSION_CODES.SYSTEM_USER_BAN,
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
        permission: PERMISSION_CODES.SYSTEM_GOODS_APPROVE,
      },
      {
        key: 'content-posts',
        label: '帖子审核',
        path: '/admin/content/posts',
        permission: PERMISSION_CODES.SYSTEM_POST_APPROVE,
      },
      {
        key: 'content-reports',
        label: '举报处理',
        path: '/admin/content/reports',
        permission: PERMISSION_CODES.SYSTEM_REPORT_HANDLE,
      },
    ],
  },
  {
    key: 'roles',
    label: '角色权限',
    icon: 'SafetyOutlined',
    path: '/admin/roles',
    permission: PERMISSION_CODES.SYSTEM_ROLE_ASSIGN,
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
        permission: PERMISSION_CODES.SYSTEM_RATE_LIMIT_MANAGE,
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
        permission: PERMISSION_CODES.SYSTEM_COMPLIANCE_REVIEW,
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
    permission: PERMISSION_CODES.SYSTEM_AUDIT_VIEW,
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
