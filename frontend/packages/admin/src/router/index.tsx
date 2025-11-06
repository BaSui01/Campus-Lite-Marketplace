/**
 * 路由配置
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */

import React from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AdminLayout } from '@/components/Layout';
import Login from '@/pages/Login';
import Dashboard from '@/pages/Dashboard';
// import DashboardTest from '@/pages/Dashboard/Dashboard.test'; // 临时测试用
import UserList from '@/pages/Users/UserList';
import UserDetail from '@/pages/Users/UserDetail';
// import { ReportList } from '@/pages/Content'; // TODO: 实现 ReportList 组件
import { RoleList } from '@/pages/Roles';
import { RateLimit, RecycleBin, Notifications, Compliance, RevertManagement, CampusList, CategoryList, TagList, FeatureFlagList, SystemMonitor, TaskList } from '@/pages/System';
import { GoodsList, GoodsDetail, GoodsAudit } from '@/pages/Goods';
import { OrderList, OrderDetail, RefundManagement } from '@/pages/Orders';
import { AppealList, AppealDetail } from '@/pages/Appeals';
import { ReviewList } from '@/pages/Reviews';
import { BatchTaskList } from '@/pages/Batch';
import { DisputeList, DisputeStatistics } from '@/pages/Disputes';
import { AuditLogList } from '@/pages/Logs';
import { BannedUserList } from '@/pages/Users';
import { PostAuditList } from '@/pages/Content';
import { TopicList, CommunityList } from '@/pages/Community';
import { PermissionGuard } from '@/components';
import { PERMISSION_CODES } from '@campus/shared';

// ===== 路由配置 =====
export const router = createBrowserRouter([
  {
    path: '/',
    element: <Navigate to="/admin/login" replace />,
  },
  {
    path: '/admin/login',
    element: <Login />,
  },
  {
    path: '/admin',
    element: <AdminLayout />,
    children: [
      {
        index: true,
        element: <Navigate to="/admin/dashboard" replace />,
      },
      {
        path: 'dashboard',
        element: <Dashboard />,
      },
      {
        path: 'goods/list',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_GOODS_VIEW}>
            <GoodsList />
          </PermissionGuard>
        ),
      },
      {
        path: 'goods/:id',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_GOODS_VIEW}>
            <GoodsDetail />
          </PermissionGuard>
        ),
      },
      {
        path: 'content/goods',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_GOODS_APPROVE}>
            <GoodsAudit />
          </PermissionGuard>
        ),
      },
      {
        path: 'orders/list',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_ORDER_VIEW}>
            <OrderList />
          </PermissionGuard>
        ),
      },
      {
        path: 'orders/:orderNo',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_ORDER_VIEW}>
            <OrderDetail />
          </PermissionGuard>
        ),
      },
      {
        path: 'orders/refunds',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_ORDER_MANAGE}>
            <RefundManagement />
          </PermissionGuard>
        ),
      },
      {
        path: 'users/banned',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_BAN}>
            <BannedUserList />
          </PermissionGuard>
        ),
      },
      {
        path: 'content/posts',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_POST_APPROVE}>
            <PostAuditList />
          </PermissionGuard>
        ),
      },
      {
        path: 'logs/audit',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_AUDIT_VIEW}>
            <AuditLogList />
          </PermissionGuard>
        ),
      },
      {
        path: 'appeals/list',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_APPEAL_VIEW}>
            <AppealList />
          </PermissionGuard>
        ),
      },
      {
        path: 'appeals/:id',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_APPEAL_VIEW}>
            <AppealDetail />
          </PermissionGuard>
        ),
      },
      {
        path: 'reviews/list',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_REVIEW_MANAGE}>
            <ReviewList />
          </PermissionGuard>
        ),
      },
      {
        path: 'batch/tasks',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_BATCH_MANAGE}>
            <BatchTaskList />
          </PermissionGuard>
        ),
      },
      {
        path: 'disputes/list',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_DISPUTE_MANAGE}>
            <DisputeList />
          </PermissionGuard>
        ),
      },
      {
        path: 'users/list',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_VIEW}>
            <UserList />
          </PermissionGuard>
        ),
      },
      {
        path: 'users/:id',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_VIEW}>
            <UserDetail />
          </PermissionGuard>
        ),
      },
      // TODO: 实现 ReportList 组件后取消注释
      // {
      //   path: 'content/reports',
      //   element: (
      //     <PermissionGuard permission={PERMISSION_CODES.SYSTEM_REPORT_HANDLE}>
      //       <ReportList />
      //     </PermissionGuard>
      //   ),
      // },
      {
        path: 'roles',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_ROLE_ASSIGN}>
            <RoleList />
          </PermissionGuard>
        ),
      },
      {
        path: 'system/rate-limit',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_RATE_LIMIT_MANAGE}>
            <RateLimit />
          </PermissionGuard>
        ),
      },
      {
        path: 'system/recycle-bin',
        element: <RecycleBin />,
      },
      {
        path: 'system/notifications',
        element: <Notifications />,
      },
      {
        path: 'system/compliance',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_COMPLIANCE_REVIEW}>
            <Compliance />
          </PermissionGuard>
        ),
      },
      {
        path: 'system/revert',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_DATA_REVERT}>
            <RevertManagement />
          </PermissionGuard>
        ),
      },
      {
        path: 'system/campuses',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_CAMPUS_MANAGE}>
            <CampusList />
          </PermissionGuard>
        ),
      },
      {
        path: 'system/categories',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_CATEGORY_MANAGE}>
            <CategoryList />
          </PermissionGuard>
        ),
      },
      {
        path: 'system/tags',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_TAG_MANAGE}>
            <TagList />
          </PermissionGuard>
        ),
      },
      {
        path: 'system/features',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_CONFIG_UPDATE}>
            <FeatureFlagList />
          </PermissionGuard>
        ),
      },
      {
        path: 'system/monitor',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_MONITOR_VIEW}>
            <SystemMonitor />
          </PermissionGuard>
        ),
      },
      {
        path: 'system/tasks',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_TASK_MANAGE}>
            <TaskList />
          </PermissionGuard>
        ),
      },
      {
        path: 'community/topics',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_TOPIC_MANAGE}>
            <TopicList />
          </PermissionGuard>
        ),
      },
      {
        path: 'community/list',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_COMMUNITY_MANAGE}>
            <CommunityList />
          </PermissionGuard>
        ),
      },
      {
        path: 'disputes/statistics',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_DISPUTE_STATISTICS}>
            <DisputeStatistics />
          </PermissionGuard>
        ),
      },
      // 所有路由已配置完成
    ],
  },
], {
  // ===== React Router v7 兼容性配置 =====
  future: {
    v7_startTransition: true, // 启用 v7 的 React.startTransition 包裹状态更新
  },
});
