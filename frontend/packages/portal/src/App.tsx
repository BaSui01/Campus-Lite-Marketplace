/**
 * 应用根组件
 * @author BaSui 😎
 * @description Portal 应用入口组件
 */

import { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { toast, useWebSocketService } from '@campus/shared';
import { useAuthStore, useNotificationStore } from './store';
import { router } from './router';
import ErrorBoundary from './components/ErrorBoundary';
import './App.css';

// 创建 React Query 客户端
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 分钟
    },
  },
});

/**
 * 应用根组件
 */
function App() {
  const { init: initAuth, isAuthenticated } = useAuthStore();
  const { notifications, remove } = useNotificationStore();

  // 初始化认证状态
  useEffect(() => {
    initAuth();
  }, [initAuth]);

  // 初始化 WebSocket 服务（只有登录后才连接！🎯）
  useWebSocketService({
    autoConnect: isAuthenticated, // ✅ 改为根据登录状态决定是否连接
    onOpen: () => {
      console.log('✅ WebSocket 已连接');
    },
    onClose: () => {
      console.warn('⚠️ WebSocket 已断开');
    },
    onError: (error) => {
      console.error('❌ WebSocket 错误:', error);
    },
  });

  return (
    <ErrorBoundary
      onError={(error, errorInfo) => {
        // 可选:将错误发送到错误监控服务（如 Sentry）
        console.error('🚨 全局错误捕获:', error);
        console.error('📍 错误详情:', errorInfo);
        // TODO: 集成 Sentry 或其他错误追踪服务
        // 示例: Sentry.captureException(error, { extra: errorInfo });
      }}
    >
      <QueryClientProvider client={queryClient}>
        <RouterProvider
          router={router}
          future={{
            v7_startTransition: true
          }}
        />

        {/* 全局通知组件 */}
        <div className="toast-container">
          {notifications.map((notification) => (
            <Toast
              key={notification.id}
              type={notification.type}
              message={notification.message}
              duration={notification.duration}
              onClose={() => remove(notification.id)}
            />
          ))}
        </div>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}

export default App;
