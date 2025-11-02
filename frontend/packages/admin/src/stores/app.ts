/**
 * 应用全局状态管理（App Store）
 *
 * 功能：
 * - 管理菜单折叠状态
 * - 管理当前路由
 * - 管理面包屑
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */

import { create } from 'zustand';

// ========== 类型定义 ==========

export interface Breadcrumb {
  title: string;
  path?: string;
}

interface AppState {
  // 状态
  menuCollapsed: boolean;
  currentRoute: string;
  breadcrumbs: Breadcrumb[];

  // 方法
  toggleMenu: () => void;
  setMenuCollapsed: (collapsed: boolean) => void;
  setCurrentRoute: (route: string) => void;
  setBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
}

// ========== 创建 Store ==========

export const useAppStore = create<AppState>()((set) => ({
  // ===== 初始状态 =====
  menuCollapsed: false,
  currentRoute: '/admin/dashboard',
  breadcrumbs: [],

  // ===== 切换菜单折叠状态 =====
  toggleMenu: () => {
    set((state) => ({
      menuCollapsed: !state.menuCollapsed,
    }));
  },

  // ===== 设置菜单折叠状态 =====
  setMenuCollapsed: (collapsed: boolean) => {
    set({ menuCollapsed: collapsed });
  },

  // ===== 设置当前路由 =====
  setCurrentRoute: (route: string) => {
    set({ currentRoute: route });
  },

  // ===== 设置面包屑 =====
  setBreadcrumbs: (breadcrumbs: Breadcrumb[]) => {
    set({ breadcrumbs });
  },
}));
