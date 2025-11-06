/**
 * 路由配置
 * @author BaSui 😎
 * @description React Router 路由配置
 */

import { createBrowserRouter, Navigate } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { Loading } from '@campus/shared';
import MainLayout from '../layouts/MainLayout';
import AuthLayout from '../layouts/AuthLayout';
import { useAuthStore } from '../store';

// ==================== 懒加载页面组件 ====================

// 认证页面
const Login = lazy(() => import('../pages/Login'));
const Register = lazy(() => import('../pages/Register'));
const ForgotPassword = lazy(() => import('../pages/ForgotPassword'));

// 主要页面
const Home = lazy(() => import('../pages/Home'));
const GoodsList = lazy(() => import('../pages/Goods/List'));
const GoodsDetail = lazy(() => import('../pages/Goods/Detail'));
const Publish = lazy(() => import('../pages/Publish'));
const Chat = lazy(() => import('../pages/Chat'));
const Orders = lazy(() => import('../pages/Orders'));
const OrderCreate = lazy(() => import('../pages/Order/Create'));
const OrderDetail = lazy(() => import('../pages/OrderDetail'));
const Profile = lazy(() => import('../pages/Profile'));
const Community = lazy(() => import('../pages/Community'));
const Search = lazy(() => import('../pages/Search'));
const Settings = lazy(() => import('../pages/Settings'));
const NotificationSettings = lazy(() => import('../pages/Settings/NotificationSettings'));
const Notifications = lazy(() => import('../pages/Notifications'));
const Favorites = lazy(() => import('../pages/Favorites'));
const Following = lazy(() => import('../pages/Following'));
const Report = lazy(() => import('../pages/Report'));
const UserProfile = lazy(() => import('../pages/UserProfile'));
const RefundApply = lazy(() => import('../pages/RefundApply'));
const RefundList = lazy(() => import('../pages/RefundList'));
const RefundDetail = lazy(() => import('../pages/RefundDetail'));
const Subscriptions = lazy(() => import('../pages/Subscriptions'));
const SubscriptionFeed = lazy(() => import('../pages/Subscriptions/SubscriptionFeed'));
const Credit = lazy(() => import('../pages/Credit'));

// 数据撤销相关页面
const RevertOperations = lazy(() => import('../pages/RevertOperations'));

// 评价相关页面
const ReviewCreate = lazy(() => import('../pages/Review/Create'));
const MyReviews = lazy(() => import('../pages/Review/MyReviews'));

// ==================== 路由守卫组件 ====================

/**
 * 需要认证的路由守卫
 */
const RequireAuth = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  
  if (!isAuthenticated) {
    // 未登录，保存当前路径并重定向到登录页
    const currentPath = window.location.pathname + window.location.search;
    const loginPath = `/login?redirect=${encodeURIComponent(currentPath)}`;
    
    console.log('[RequireAuth] 未登录，重定向到登录页:', loginPath);
    
    return <Navigate to={loginPath} replace />;
  }

  return <>{children}</>;
};

/**
 * 已登录时重定向（登录/注册页）
 */
const RedirectIfAuth = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  if (isAuthenticated) {
    // 已登录，重定向到首页
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

/**
 * 懒加载包装组件
 */
const LazyLoadWrapper = ({ children }: { children: React.ReactNode }) => {
  return (
    <Suspense
      fallback={
        <div className="flex items-center justify-center h-screen">
          <Loading size="large" />
        </div>
      }
    >
      {children}
    </Suspense>
  );
};

// ==================== 路由配置 ====================

/**
 * 应用路由配置
 */
export const router = createBrowserRouter(
  [
    // ==================== 主布局路由 ====================
    {
      path: '/',
      element: <MainLayout />,
      children: [
        {
          index: true,
          element: (
            <LazyLoadWrapper>
              <Home />
            </LazyLoadWrapper>
          ),
        },
        {
          path: 'goods',
          element: (
            <LazyLoadWrapper>
              <GoodsList />
            </LazyLoadWrapper>
          ),
        },
        {
          path: 'goods/:id',
          element: (
            <LazyLoadWrapper>
              <GoodsDetail />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'publish',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Publish />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'chat',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Chat />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'orders',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Orders />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'order/create',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <OrderCreate />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'orders/:orderNo',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <OrderDetail />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'profile',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Profile />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'community',
        element: (
          <LazyLoadWrapper>
            <Community />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'search',
        element: (
          <LazyLoadWrapper>
            <Search />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'settings',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Settings />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'settings/notifications',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <NotificationSettings />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'credit',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Credit />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'notifications',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Notifications />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'favorites',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Favorites />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'following',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Following />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'report',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Report />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'users/:userId',
        element: (
          <LazyLoadWrapper>
            <UserProfile />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'refunds/apply/:orderNo',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <RefundApply />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'refunds',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <RefundList />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'refunds/:refundNo',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <RefundDetail />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'subscriptions',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Subscriptions />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'subscriptions/feed',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <SubscriptionFeed />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'revert/operations',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <RevertOperations />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'review/create',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <ReviewCreate />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'reviews/my',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <MyReviews />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
    ],
  },

  // ==================== 认证布局路由 ====================
  {
    path: '/',
    element: <AuthLayout />,
    children: [
      {
        path: 'login',
        element: (
          <RedirectIfAuth>
            <LazyLoadWrapper>
              <Login />
            </LazyLoadWrapper>
          </RedirectIfAuth>
        ),
      },
      {
        path: 'register',
        element: (
          <RedirectIfAuth>
            <LazyLoadWrapper>
              <Register />
            </LazyLoadWrapper>
          </RedirectIfAuth>
        ),
      },
      {
        path: 'forgot-password',
        element: (
          <RedirectIfAuth>
            <LazyLoadWrapper>
              <ForgotPassword />
            </LazyLoadWrapper>
          </RedirectIfAuth>
        ),
      },
    ],
  },

  // ==================== 404 页面 ====================
  {
    path: '*',
    element: (
      <div className="flex flex-col items-center justify-center h-screen">
        <h1 className="text-6xl font-bold text-gray-800">404</h1>
        <p className="text-xl text-gray-600 mt-4">页面不存在</p>
        <a href="/" className="mt-8 px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600">
          返回首页
        </a>
      </div>
    ),
  },
],
  // ==================== React Router v7 兼容性配置 ====================
  {
    future: {
      // ✅ 启用 v7 的 React.startTransition 包裹状态更新
      v7_startTransition: true,
    },
  }
);

export default router;
