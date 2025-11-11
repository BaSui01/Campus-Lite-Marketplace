/**
 * 路由配置
 * @author BaSui 😎
 * @description React Router 路由配置
 */

import { createBrowserRouter, Navigate } from 'react-router-dom';
import { lazy, Suspense, useEffect, useState } from 'react';
import { Loading } from '@campus/shared';
import { isTokenValid, getAccessToken } from '@campus/shared/utils';
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
const PostDetail = lazy(() => import('../pages/Post/Detail'));
const Search = lazy(() => import('../pages/Search'));
const Settings = lazy(() => import('../pages/Settings'));
const NotificationSettings = lazy(() => import('../pages/Settings/NotificationSettings'));
const NotificationTypes = lazy(() => import('../pages/Settings/NotificationTypes'));
const BlacklistSettings = lazy(() => import('../pages/Settings/BlacklistSettings'));
const Notifications = lazy(() => import('../pages/Notifications'));
const Favorites = lazy(() => import('../pages/Favorites'));
const Following = lazy(() => import('../pages/Following'));
const Points = lazy(() => import('../pages/Points'));
const Report = lazy(() => import('../pages/Report'));
const UserProfile = lazy(() => import('../pages/UserProfile'));
const RefundApply = lazy(() => import('../pages/RefundApply'));
const RefundList = lazy(() => import('../pages/RefundList'));
const RefundDetail = lazy(() => import('../pages/RefundDetail'));
const Subscriptions = lazy(() => import('../pages/Subscriptions'));
const SubscriptionFeed = lazy(() => import('../pages/Subscriptions/SubscriptionFeed'));
const Credit = lazy(() => import('../pages/Credit'));
const SellerDashboard = lazy(() => import('../pages/Seller/Dashboard'));
const Activities = lazy(() => import('../pages/Seller/Activities'));
const CreateActivity = lazy(() => import('../pages/Seller/Activities/Create'));

// 数据撤销相关页面
const RevertOperations = lazy(() => import('../pages/RevertOperations'));

// 校园活动和学习资源页面
const Events = lazy(() => import('../pages/Events'));
const Resources = lazy(() => import('../pages/Resources'));

// 评价相关页面
const ReviewCreate = lazy(() => import('../pages/Review/Create'));
const MyReviews = lazy(() => import('../pages/Review/MyReviews'));

// 话题相关页面
const Topics = lazy(() => import('../pages/Topics'));
const TopicDetail = lazy(() => import('../pages/Topics/Detail'));

// 物流相关页面
const Logistics = lazy(() => import('../pages/Logistics'));

// 校区相关页面
const Campuses = lazy(() => import('../pages/Campuses'));
const CampusDetail = lazy(() => import('../pages/Campuses/Detail'));

// 申诉相关页面
const AppealList = lazy(() => import('../pages/Appeals'));
const AppealCreate = lazy(() => import('../pages/Appeals/AppealCreate'));
const AppealDetail = lazy(() => import('../pages/Appeals/AppealDetail'));

// 支付相关页面
const Payment = lazy(() => import('../pages/Payment'));
const PaymentStatus = lazy(() => import('../pages/Payment/PaymentStatus'));
const PaymentResult = lazy(() => import('../pages/Payment/PaymentResult'));
const PaymentMethods = lazy(() => import('../pages/Payment/PaymentMethods'));

// 法律相关页面
const AboutUs = lazy(() => import('../pages/AboutUs'));
const PrivacyPolicy = lazy(() => import('../pages/PrivacyPolicy'));
const TermsOfService = lazy(() => import('../pages/TermsOfService'));

// ==================== 路由守卫组件 ====================

/**
 * 需要认证的路由守卫
 * @description 检查用户是否已登录且 Token 有效，如果未登录则重定向到登录页
 * @author BaSui 😎
 */
const RequireAuth = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const accessToken = useAuthStore((state) => state.accessToken);
  const logout = useAuthStore((state) => state.logout);
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      console.log('[RequireAuth] 🔍 开始检查认证状态...');
      console.log('[RequireAuth] isAuthenticated:', isAuthenticated);
      console.log('[RequireAuth] accessToken:', accessToken ? '存在' : '不存在');

      // 🔧 BaSui 修复：添加延迟，等待 Zustand persist 恢复状态
      // ⚠️ 解决时序竞态：确保 init() 完成后再检查
      await new Promise(resolve => setTimeout(resolve, 100));

      // 重新读取最新状态（可能已被 init() 更新）
      const latestAuth = useAuthStore.getState().isAuthenticated;
      const latestToken = useAuthStore.getState().accessToken;

      console.log('[RequireAuth] 🔄 延迟后重新检查 - isAuthenticated:', latestAuth);
      console.log('[RequireAuth] 🔄 延迟后重新检查 - accessToken:', latestToken ? '存在' : '不存在');

      // 1. 检查 Zustand 状态
      if (!latestAuth) {
        console.log('[RequireAuth] ⚠️ 未认证状态，需要登录');
        setIsChecking(false);
        return;
      }

      // 2. 检查 Token 是否存在
      if (!latestToken) {
        console.log('[RequireAuth] ⚠️ Token 不存在，需要登录');
        setIsChecking(false);
        return;
      }

      // 3. 检查 Token 是否过期
      const isValid = isTokenValid(latestToken);
      if (!isValid) {
        console.log('[RequireAuth] ⏰ Token 已过期，清除认证状态');
        await logout(); // 清除过期状态
        setIsChecking(false);
        return;
      }

      // 4. Token 有效，允许访问
      console.log('[RequireAuth] ✅ Token 有效，允许访问受保护页面');
      setIsChecking(false);
    };

    checkAuth();
  }, []); // 🔧 BaSui 修复：只在组件挂载时检查一次，避免状态变化时重复检查导致闪烁

  // 显示加载状态
  if (isChecking) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loading size="large" />
      </div>
    );
  }

  // 🔧 BaSui 修复：直接使用 isAuthenticated 和 accessToken 判断
  // ⚠️ 防止无限重定向：如果已经在登录页，不再重定向
  if (!isAuthenticated || !accessToken || !isTokenValid(accessToken)) {
    const currentPath = window.location.pathname + window.location.search;
    
    // 防止循环：如果当前就是登录页，不重定向
    if (currentPath.startsWith('/login')) {
      console.log('[RequireAuth] ⚠️ 已在登录页，跳过重定向');
      return <>{children}</>;
    }
    
    const loginPath = `/login?redirect=${encodeURIComponent(currentPath)}`;
    console.log('[RequireAuth] 🚀 重定向到登录页:', loginPath);
    return <Navigate to={loginPath} replace />;
  }

  // 允许访问受保护页面
  return <>{children}</>;
};

/**
 * 已登录时重定向（登录/注册页）
 * @description 检查用户是否已登录且 Token 有效，如果是则重定向到首页
 * @author BaSui 😎
 */
const RedirectIfAuth = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const accessToken = useAuthStore((state) => state.accessToken);
  const logout = useAuthStore((state) => state.logout);
  const [isChecking, setIsChecking] = useState(true);
  const [shouldRedirect, setShouldRedirect] = useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      console.log('[RedirectIfAuth] 🔍 开始检查认证状态...');
      console.log('[RedirectIfAuth] isAuthenticated:', isAuthenticated);
      console.log('[RedirectIfAuth] accessToken:', accessToken ? '存在' : '不存在');

      // 🔧 BaSui 修复：添加延迟，等待 Zustand persist 恢复状态
      await new Promise(resolve => setTimeout(resolve, 100));

      // 重新读取最新状态
      const latestAuth = useAuthStore.getState().isAuthenticated;
      const latestToken = useAuthStore.getState().accessToken;

      console.log('[RedirectIfAuth] 🔄 延迟后重新检查 - isAuthenticated:', latestAuth);
      console.log('[RedirectIfAuth] 🔄 延迟后重新检查 - accessToken:', latestToken ? '存在' : '不存在');

      // 1. 如果未认证，允许访问登录页
      if (!latestAuth || !latestToken) {
        console.log('[RedirectIfAuth] ✅ 未认证，允许访问登录/注册页');
        setIsChecking(false);
        return;
      }

      // 2. 检查 Token 是否过期
      const isValid = isTokenValid(latestToken);
      if (!isValid) {
        console.log('[RedirectIfAuth] ⏰ Token 已过期，清除认证状态，允许访问登录页');
        await logout(); // 清除过期状态
        setIsChecking(false);
        return;
      }

      // 3. 已认证且 Token 有效，重定向到首页
      console.log('[RedirectIfAuth] ✅ 已认证且 Token 有效，重定向到首页');
      setShouldRedirect(true);
      setIsChecking(false);
    };

    checkAuth();
  }, []); // 🔧 BaSui 修复：只在组件挂载时检查一次

  // 显示加载状态
  if (isChecking) {
    return (
      <div className="flex flex-col items-center justify-center h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
        <Loading size="large" />
        <p className="mt-4 text-gray-600 animate-pulse">正在检查登录状态...</p>
      </div>
    );
  }

  // 重定向到首页
  if (shouldRedirect) {
    console.log('[RedirectIfAuth] 🚀 重定向到首页');
    return <Navigate to="/" replace />;
  }

  // 允许访问登录页
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
        path: 'points',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Points />
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
        path: 'posts/:id',
        element: (
          <LazyLoadWrapper>
            <PostDetail />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'events',
        element: (
          <LazyLoadWrapper>
            <Events />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'resources',
        element: (
          <LazyLoadWrapper>
            <Resources />
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
        path: 'settings/notifications/types',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <NotificationTypes />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'settings/blacklist',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <BlacklistSettings />
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
        path: 'seller/dashboard',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <SellerDashboard />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'seller/activities',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Activities />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'seller/activities/create',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <CreateActivity />
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
      {
        path: 'topics',
        element: (
          <LazyLoadWrapper>
            <Topics />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'topics/:id',
        element: (
          <LazyLoadWrapper>
            <TopicDetail />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'logistics/track',
        element: (
          <LazyLoadWrapper>
            <Logistics />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'campuses',
        element: (
          <LazyLoadWrapper>
            <Campuses />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'campuses/:id',
        element: (
          <LazyLoadWrapper>
            <CampusDetail />
          </LazyLoadWrapper>
        ),
      },
      // ==================== 申诉管理 ====================
      {
        path: 'appeals',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <AppealList />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'appeals/create',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <AppealCreate />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'appeals/:id',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <AppealDetail />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      // ==================== 支付管理 ====================
      {
        path: 'payment',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <Payment />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'payment/status',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <PaymentStatus />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'payment/result',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <PaymentResult />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'payment/methods',
        element: (
          <RequireAuth>
            <LazyLoadWrapper>
              <PaymentMethods />
            </LazyLoadWrapper>
          </RequireAuth>
        ),
      },
      {
        path: 'about',
        element: (
          <LazyLoadWrapper>
            <AboutUs />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'privacy',
        element: (
          <LazyLoadWrapper>
            <PrivacyPolicy />
          </LazyLoadWrapper>
        ),
      },
      {
        path: 'terms',
        element: (
          <LazyLoadWrapper>
            <TermsOfService />
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
