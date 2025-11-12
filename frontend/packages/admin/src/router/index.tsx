/**
 * 路由配置
 *
 * @author BaSui 😎
 * @date 2025-11-01
 * @updated 2025-11-08 - 添加路由懒加载和代码分割
 */

import React, { Suspense, lazy } from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { Spin } from 'antd';
import { AdminLayout } from '@/components/Layout';
import { PermissionGuard } from '@/components';
import { PERMISSION_CODES } from '@campus/shared';

// ===== 页面加载组件 =====
const PageLoading: React.FC = () => (
  <div style={{ padding: '24px' }}>
    <Spin size="large" tip="加载中...">
      <div style={{ minHeight: '400px' }} />
    </Spin>
  </div>
);

// ===== 核心页面（不懒加载，保证首屏速度）=====
import Login from '@/pages/Login';
import Dashboard from '@/pages/Dashboard';

// ===== 懒加载页面（按需加载）=====

// 用户管理
const UserList = lazy(() => import('@/pages/Users/UserList'));
const UserDetail = lazy(() => import('@/pages/Users/UserDetail'));
const BannedUserList = lazy(() => import('@/pages/Users/BannedUserList'));
const BlacklistManagement = lazy(() => import('@/pages/Users/BlacklistManagement'));

// 个人中心
const ProfilePage = lazy(() => import('@/pages/Profile'));

// 角色权限
const RoleList = lazy(() => import('@/pages/Roles/RoleList'));

// 系统管理
const RateLimit = lazy(() => import('@/pages/System/RateLimit'));
const RecycleBin = lazy(() => import('@/pages/System/RecycleBin'));
const Notifications = lazy(() => import('@/pages/System/Notifications'));
const Compliance = lazy(() => import('@/pages/System/Compliance'));
const RevertManagement = lazy(() => import('@/pages/System/RevertManagement'));
const CampusList = lazy(() => import('@/pages/System/CampusList'));
const CategoryList = lazy(() => import('@/pages/System/CategoryList'));
const TagList = lazy(() => import('@/pages/System/TagList'));
const FeatureFlagList = lazy(() => import('@/pages/System/FeatureFlagList'));
const SystemMonitor = lazy(() => import('@/pages/System/SystemMonitor'));
const TaskList = lazy(() => import('@/pages/System/TaskList'));

// 商品管理
const GoodsList = lazy(() => import('@/pages/Goods/GoodsList'));
const GoodsDetail = lazy(() => import('@/pages/Goods/GoodsDetail'));
const GoodsAudit = lazy(() => import('@/pages/Goods/GoodsAudit'));

// 订单管理
const OrderList = lazy(() => import('@/pages/Orders/OrderList'));
const OrderDetail = lazy(() => import('@/pages/Orders/OrderDetail'));
const RefundManagement = lazy(() => import('@/pages/Orders/RefundManagement'));

// 支付管理
const PaymentList = lazy(() => import('@/pages/Payment/PaymentList'));
const PaymentDetail = lazy(() => import('@/pages/Payment/PaymentDetail'));

// 消息管理
const MessageList = lazy(() => import('@/pages/Messages/MessageList'));
const MessageDetail = lazy(() => import('@/pages/Messages/MessageDetail'));

// 导出中心
const ExportCenter = lazy(() => import('@/pages/Export/ExportCenter'));

// 物流管理
const LogisticsList = lazy(() => import('@/pages/Logistics/LogisticsList'));

// 行为分析
const BehaviorDashboard = lazy(() => import('@/pages/Behavior/BehaviorDashboard'));

// 推荐管理
const RecommendConfig = lazy(() => import('@/pages/Recommend/RecommendConfig'));

// 搜索管理
const SearchStatistics = lazy(() => import('@/pages/Search/SearchStatistics'));

// 申诉管理
const AppealList = lazy(() => import('@/pages/Appeals/AppealList'));
const AppealDetail = lazy(() => import('@/pages/Appeals/AppealDetail'));

// 评价管理
const ReviewList = lazy(() => import('@/pages/Reviews/ReviewList'));

// 批量任务
const BatchTaskList = lazy(() => import('@/pages/Batch/BatchTaskList'));

// 纠纷管理
const DisputeList = lazy(() => import('@/pages/Disputes/DisputeList'));
const DisputeDetail = lazy(() => import('@/pages/Disputes/DisputeDetail'));
const DisputeStatistics = lazy(() => import('@/pages/Disputes/DisputeStatistics'));

// 日志管理
const AuditLogList = lazy(() => import('@/pages/Logs/AuditLogList'));
const OperationLogList = lazy(() => import('@/pages/Logs/OperationLogList'));

// 内容管理
const PostAuditList = lazy(() => import('@/pages/Content/PostAuditList'));
const ReportList = lazy(() => import('@/pages/Content/ReportList'));
const ReviewAuditList = lazy(() => import('@/pages/Content/ReviewAuditList'));

// 社区管理
const TopicList = lazy(() => import('@/pages/Community/TopicList'));
const CommunityList = lazy(() => import('@/pages/Community/CommunityList'));

// 统计分析
const StatisticsDashboard = lazy(() => import('@/pages/Statistics/StatisticsDashboard'));

// 通知模板
const NotificationTemplateList = lazy(() => import('@/pages/NotificationTemplates/NotificationTemplateList'));

// ===== 辅助函数：包裹 Suspense =====
const withSuspense = (Component: React.LazyExoticComponent<React.ComponentType<any>>) => (
  <Suspense fallback={<PageLoading />}>
    <Component />
  </Suspense>
);

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
        path: 'profile',
        element: withSuspense(ProfilePage),
      },
      {
        path: 'statistics',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_STATISTICS_VIEW}>
            <Suspense fallback={<PageLoading />}>
              <StatisticsDashboard />
            </Suspense>
          </PermissionGuard>
        ),
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
        path: 'payments/list',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_ORDER_VIEW}>
            <PaymentList />
          </PermissionGuard>
        ),
      },
      {
        path: 'payments/:orderNo',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_ORDER_VIEW}>
            <PaymentDetail />
          </PermissionGuard>
        ),
      },
      {
        path: 'messages/list',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_VIEW}>
            <MessageList />
          </PermissionGuard>
        ),
      },
      {
        path: 'messages/:conversationId',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_VIEW}>
            <MessageDetail />
          </PermissionGuard>
        ),
      },
      {
        path: 'export/center',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_VIEW}>
            <ExportCenter />
          </PermissionGuard>
        ),
      },
      {
        path: 'logistics/list',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_ORDER_VIEW}>
            <LogisticsList />
          </PermissionGuard>
        ),
      },
      {
        path: 'behavior/dashboard',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_VIEW}>
            <BehaviorDashboard />
          </PermissionGuard>
        ),
      },
      {
        path: 'recommend/config',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_CONFIG_UPDATE}>
            <RecommendConfig />
          </PermissionGuard>
        ),
      },
      {
        path: 'search/statistics',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_STATISTICS_VIEW}>
            <SearchStatistics />
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
        path: 'users/blacklist',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_VIEW}>
            <BlacklistManagement />
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
        path: 'logs/operation',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_AUDIT_VIEW}>
            <OperationLogList />
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
        path: 'disputes/:id',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_DISPUTE_MANAGE}>
            <DisputeDetail />
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
      {
        path: 'content/reports',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_REPORT_HANDLE}>
            <ReportList />
          </PermissionGuard>
        ),
      },
      {
        path: 'content/reviews',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_REVIEW_MANAGE}>
            <ReviewAuditList />
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
        path: 'system/notification-templates',
        element: (
          <PermissionGuard permission={PERMISSION_CODES.SYSTEM_RATE_LIMIT_MANAGE}>
            <NotificationTemplateList />
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
  // ===== 404 兜底路由（修复404问题 - BaSui 2025-11-10）=====
  {
    path: '*',
    element: (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', padding: '20px' }}>
        <h1 style={{ fontSize: '72px', fontWeight: 'bold', color: '#667eea', margin: 0 }}>404</h1>
        <p style={{ fontSize: '20px', color: '#666', marginTop: '16px' }}>页面不存在</p>
        <button
          onClick={() => window.location.href = '/admin/login'}
          style={{
            marginTop: '32px',
            padding: '12px 32px',
            fontSize: '16px',
            color: '#fff',
            backgroundColor: '#667eea',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
          }}
        >
          返回首页
        </button>
      </div>
    ),
  },
], {
  // ===== React Router v7 兼容性配置 =====
  future: {
    v7_startTransition: true, // 启用 v7 的 React.startTransition 包裹状态更新
  },
});
