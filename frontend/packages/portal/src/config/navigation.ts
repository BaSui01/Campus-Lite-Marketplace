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
 * 主导航菜单配置
 */
export const MAIN_NAV_ITEMS: NavItem[] = [
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
        label: '全部商品',
        path: '/goods',
      },
      {
        key: 'goods-publish',
        label: '我要卖',
        path: '/publish',
        auth: true,
      },
      {
        key: 'favorites',
        label: '我的收藏',
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
  {
    key: 'activities',
    label: '活动中心',
    icon: '🎉',
    children: [
      {
        key: 'activities-list',
        label: '活动列表',
        path: '/seller/activities',
      },
      {
        key: 'activities-create',
        label: '发起活动',
        path: '/seller/activities/create',
        auth: true,
      },
    ],
  },
  {
    key: 'seller',
    label: '卖家中心',
    icon: '📊',
    auth: true,
    children: [
      {
        key: 'seller-dashboard',
        label: '数据看板',
        path: '/seller/dashboard',
      },
      {
        key: 'seller-goods',
        label: '我的商品',
        path: '/profile?tab=goods',
      },
      {
        key: 'seller-reviews',
        label: '我的评价',
        path: '/reviews/my',
      },
      {
        key: 'seller-credit',
        label: '信用分',
        path: '/credit',
      },
    ],
  },
  {
    key: 'orders',
    label: '订单管理',
    icon: '📦',
    auth: true,
    children: [
      {
        key: 'orders-all',
        label: '全部订单',
        path: '/orders',
      },
      {
        key: 'orders-refunds',
        label: '退款/售后',
        path: '/refunds',
      },
      {
        key: 'orders-subscriptions',
        label: '订阅管理',
        path: '/subscriptions',
      },
    ],
  },
];

/**
 * 用户下拉菜单配置
 */
export const USER_MENU_ITEMS: NavItem[] = [
  {
    key: 'profile',
    label: '个人中心',
    path: '/profile',
    icon: '👤',
  },
  {
    key: 'following',
    label: '我的关注',
    path: '/following',
    icon: '❤️',
  },
  {
    key: 'subscriptions',
    label: '我的订阅',
    path: '/subscriptions',
    icon: '📬',
  },
  {
    key: 'divider1',
    label: '',
  },
  {
    key: 'orders',
    label: '我的订单',
    path: '/orders',
    icon: '📦',
  },
  {
    key: 'favorites',
    label: '我的收藏',
    path: '/favorites',
    icon: '⭐',
  },
  {
    key: 'credit',
    label: '信用分',
    path: '/credit',
    icon: '💯',
  },
  {
    key: 'divider2',
    label: '',
  },
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
    key: 'blacklist',
    label: '黑名单',
    path: '/settings/blacklist',
    icon: '🚫',
  },
  {
    key: 'divider3',
    label: '',
  },
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
