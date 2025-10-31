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

// 主要页面
const Home = lazy(() => import('../pages/Home'));
const GoodsDetail = lazy(() => import('../pages/GoodsDetail'));
const Publish = lazy(() => import('../pages/Publish'));
const Chat = lazy(() => import('../pages/Chat'));
const Orders = lazy(() => import('../pages/Orders'));
const Profile = lazy(() => import('../pages/Profile'));
const Community = lazy(() => import('../pages/Community'));

// ==================== 路由守卫组件 ====================

/**
 * 需要认证的路由守卫
 */
const RequireAuth = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  if (!isAuthenticated) {
    // 未登录，重定向到登录页
    return <Navigate to="/login" replace />;
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
export const router = createBrowserRouter([
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
]);

export default router;
