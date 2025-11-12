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
    key: 'statistics',
    label: '统计分析',
    icon: 'BarChartOutlined',
    permission: PERMISSION_CODES.SYSTEM_STATISTICS_VIEW,
    children: [
      {
        key: 'statistics-dashboard',
        label: '数据统计',
        path: '/admin/statistics',
      },
      {
        key: 'behavior-dashboard',
        label: '行为分析',
        path: '/admin/behavior/dashboard',
        permission: PERMISSION_CODES.SYSTEM_USER_VIEW,
      },
      {
        key: 'search-statistics',
        label: '搜索统计',
        path: '/admin/search/statistics',
      },
    ],
  },
  {
    key: 'goods',
    label: '商品管理',
    icon: 'ShoppingOutlined',
    permission: PERMISSION_CODES.SYSTEM_GOODS_VIEW,
    children: [
      {
        key: 'goods-list',
        label: '商品列表',
        path: '/admin/goods/list',
      },
      {
        key: 'goods-audit',
        label: '商品审核',
        path: '/admin/content/goods',
        permission: PERMISSION_CODES.SYSTEM_GOODS_APPROVE,
      },
    ],
  },
  {
    key: 'orders',
    label: '订单管理',
    icon: 'ShoppingCartOutlined',
    permission: PERMISSION_CODES.SYSTEM_ORDER_VIEW,
    children: [
      {
        key: 'orders-list',
        label: '订单列表',
        path: '/admin/orders/list',
      },
      {
        key: 'orders-refunds',
        label: '退款管理',
        path: '/admin/orders/refunds',
        permission: PERMISSION_CODES.SYSTEM_ORDER_MANAGE,
      },
      {
        key: 'payments-list',
        label: '支付管理',
        path: '/admin/payments/list',
      },
      {
        key: 'logistics-list',
        label: '物流管理',
        path: '/admin/logistics/list',
      },
    ],
  },
  {
    key: 'appeals',
    label: '申诉管理',
    icon: 'FileProtectOutlined',
    children: [
      {
        key: 'appeals-list',
        label: '申诉列表',
        path: '/admin/appeals/list',
      },
    ],
  },
  {
    key: 'disputes',
    label: '纠纷仲裁',
    icon: 'SafetyCertificateOutlined',
    children: [
      {
        key: 'disputes-list',
        label: '纠纷列表',
        path: '/admin/disputes/list',
      },
      {
        key: 'disputes-statistics',
        label: '纠纷统计',
        path: '/admin/disputes/statistics',
        permission: PERMISSION_CODES.SYSTEM_DISPUTE_STATISTICS,
      },
    ],
  },
  {
    key: 'reviews',
    label: '评价管理',
    icon: 'StarOutlined',
    children: [
      {
        key: 'reviews-list',
        label: '评价列表',
        path: '/admin/reviews/list',
      },
    ],
  },
  {
    key: 'batch',
    label: '批量操作',
    icon: 'ThunderboltOutlined',
    children: [
      {
        key: 'batch-tasks',
        label: '任务列表',
        path: '/admin/batch/tasks',
      },
    ],
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
      {
        key: 'users-blacklist',
        label: '黑名单管理',
        path: '/admin/users/blacklist',
        permission: PERMISSION_CODES.SYSTEM_USER_VIEW,
      },
    ],
  },
  {
    key: 'messages',
    label: '消息管理',
    icon: 'MessageOutlined',
    permission: PERMISSION_CODES.SYSTEM_USER_VIEW,
    children: [
      {
        key: 'messages-list',
        label: '消息列表',
        path: '/admin/messages/list',
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
        key: 'system-campuses',
        label: '校园管理',
        path: '/admin/system/campuses',
        permission: PERMISSION_CODES.SYSTEM_CAMPUS_MANAGE,
      },
      {
        key: 'system-categories',
        label: '分类管理',
        path: '/admin/system/categories',
        permission: PERMISSION_CODES.SYSTEM_CATEGORY_MANAGE,
      },
      {
        key: 'system-tags',
        label: '标签管理',
        path: '/admin/system/tags',
        permission: PERMISSION_CODES.SYSTEM_TAG_MANAGE,
      },
      {
        key: 'system-features',
        label: '功能开关',
        path: '/admin/system/features',
        permission: PERMISSION_CODES.SYSTEM_CONFIG_UPDATE,
      },
      {
        key: 'system-monitor',
        label: '系统监控',
        path: '/admin/system/monitor',
        permission: PERMISSION_CODES.SYSTEM_MONITOR_VIEW,
      },
      {
        key: 'system-tasks',
        label: '任务管理',
        path: '/admin/system/tasks',
        permission: PERMISSION_CODES.SYSTEM_TASK_MANAGE,
      },
      {
        key: 'system-rate-limit',
        label: '限流管理',
        path: '/admin/system/rate-limit',
        permission: PERMISSION_CODES.SYSTEM_RATE_LIMIT_MANAGE,
      },
      {
        key: 'system-notification-templates',
        label: '通知模板',
        path: '/admin/system/notification-templates',
        permission: PERMISSION_CODES.SYSTEM_RATE_LIMIT_MANAGE,
      },
      {
        key: 'recommend-config',
        label: '推荐管理',
        path: '/admin/recommend/config',
        permission: PERMISSION_CODES.SYSTEM_CONFIG_UPDATE,
      },
      {
        key: 'export-center',
        label: '导出中心',
        path: '/admin/export/center',
        permission: PERMISSION_CODES.SYSTEM_USER_VIEW,
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
      {
        key: 'system-revert',
        label: '数据撤销',
        path: '/admin/system/revert',
        permission: PERMISSION_CODES.SYSTEM_DATA_REVERT,
      },
    ],
  },
  {
    key: 'community',
    label: '社区管理',
    icon: 'TeamOutlined',
    children: [
      {
        key: 'community-topics',
        label: '话题管理',
        path: '/admin/community/topics',
        permission: PERMISSION_CODES.SYSTEM_TOPIC_MANAGE,
      },
      {
        key: 'community-list',
        label: '社区广场',
        path: '/admin/community/list',
        permission: PERMISSION_CODES.SYSTEM_COMMUNITY_MANAGE,
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
