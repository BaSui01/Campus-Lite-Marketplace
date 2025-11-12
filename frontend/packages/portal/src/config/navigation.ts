/**
 * 门户端导航配置
 * @author BaSui 😎
 * @description 定义顶部导航栏的菜单结构
 */

export interface NavItem {
  key: string;
  label: string;
  path?: string;
  icon?: string;
  auth?: boolean; // 是否需要登录
  children?: NavItem[];
}

/**
 * 主导航菜单配置（优化版 - 调整分组、优化icon、增强体验）🎨
 * @updated 2025-11-08 - BaSui 优化：重新设计菜单结构，提升用户体验
 */
export const MAIN_NAV_ITEMS: NavItem[] = [
  // ==================== 核心功能 ====================
  {
    key: 'home',
    label: '首页',
    path: '/',
    icon: '🏠',
  },
  {
    key: 'goods',
    label: '商品市场',
    icon: '🛍️',
    children: [
      {
        key: 'goods-all',
        label: '🔍 全部商品',
        path: '/goods',
      },
      {
        key: 'goods-publish',
        label: '📝 我要卖',
        path: '/publish',
        auth: true,
      },
      {
        key: 'favorites',
        label: '⭐ 我的收藏',
        path: '/favorites',
        auth: true,
      },
    ],
  },
  {
    key: 'community',
    label: '校园社区',
    path: '/community',
    icon: '👥',
  },

  // ==================== 活动&话题 ====================
  {
    key: 'activities',
    label: '活动&话题',
    icon: '🎉',
    children: [
      {
        key: 'activities-list',
        label: '🎊 活动列表',
        path: '/seller/activities',
      },
      {
        key: 'topics',
        label: '💬 热门话题',
        path: '/topics',
      },
      {
        key: 'activities-create',
        label: '✨ 发起活动',
        path: '/seller/activities/create',
        auth: true,
      },
    ],
  },

  // ==================== 卖家中心（登录后显示）====================
  {
    key: 'seller',
    label: '卖家中心',
    icon: '📊',
    auth: true,
    children: [
      {
        key: 'seller-dashboard',
        label: '📈 数据看板',
        path: '/seller/dashboard',
      },
      {
        key: 'seller-goods',
        label: '📦 我的商品',
        path: '/profile?tab=goods',
      },
      {
        key: 'seller-reviews',
        label: '⭐ 我的评价',
        path: '/reviews/my',
      },
      {
        key: 'seller-credit',
        label: '💯 信用分',
        path: '/credit',
      },
    ],
  },

  // ==================== 订单管理（登录后显示）====================
  {
    key: 'orders',
    label: '订单管理',
    icon: '📦',
    auth: true,
    children: [
      {
        key: 'orders-all',
        label: '📋 全部订单',
        path: '/orders',
      },
      {
        key: 'orders-refunds',
        label: '💰 退款/售后',
        path: '/refunds',
      },
      {
        key: 'orders-subscriptions',
        label: '📬 订阅管理',
        path: '/subscriptions',
      },
    ],
  },
];

/**
 * 用户下拉菜单配置（优化版 - 精简重复、优化分组）🎨
 * @updated 2025-11-08 - BaSui 优化：去除重复菜单项，优化分组结构
 */
export const USER_MENU_ITEMS: NavItem[] = [
  // ==================== 个人信息组 ====================
  {
    key: 'profile',
    label: '个人中心',
    path: '/profile',
    icon: '👤',
  },
  {
    key: 'credit',
    label: '信用分',
    path: '/credit',
    icon: '💯',
  },
  {
    key: 'divider1',
    label: '',
  },

  // ==================== 订单管理组 ====================
  {
    key: 'orders',
    label: '我的订单',
    path: '/orders',
    icon: '📦',
  },
  {
    key: 'refunds',
    label: '退款/售后',
    path: '/refunds',
    icon: '💰',
  },
  {
    key: 'divider2',
    label: '',
  },

  // ==================== 设置组 ====================
  {
    key: 'settings',
    label: '账号设置',
    path: '/settings',
    icon: '⚙️',
  },
  {
    key: 'notifications-settings',
    label: '通知设置',
    path: '/settings/notifications',
    icon: '🔔',
  },
  {
    key: 'divider3',
    label: '',
  },

  // ==================== 数据操作组 ====================
  {
    key: 'revert',
    label: '数据撤销',
    path: '/revert/operations',
    icon: '↩️',
  },
];

/**
 * 移动端底部导航栏配置
 */
export const MOBILE_TAB_BAR: NavItem[] = [
  {
    key: 'home',
    label: '首页',
    path: '/',
    icon: '🏠',
  },
  {
    key: 'goods',
    label: '市场',
    path: '/goods',
    icon: '🛍️',
  },
  {
    key: 'publish',
    label: '发布',
    path: '/publish',
    icon: '➕',
    auth: true,
  },
  {
    key: 'community',
    label: '社区',
    path: '/community',
    icon: '👥',
  },
  {
    key: 'profile',
    label: '我的',
    path: '/profile',
    icon: '👤',
    auth: true,
  },
];
