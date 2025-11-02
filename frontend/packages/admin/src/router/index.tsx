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
import UserList from '@/pages/Users/UserList';
import UserDetail from '@/pages/Users/UserDetail';
import { ReportList } from '@/pages/Content';
import { RoleList } from '@/pages/Roles';
import { RateLimit, RecycleBin, Notifications, Compliance } from '@/pages/System';
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
      {
        path: 'content/reports',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_REPORT_HANDLE}>
            <ReportList />
          </PermissionGuard>
        ),
      },
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
      // TODO: 后续添加更多路由
    ],
  },
]);
