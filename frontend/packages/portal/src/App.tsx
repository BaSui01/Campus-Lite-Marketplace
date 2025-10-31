/**
 * 应用根组件
 * @author BaSui 😎
 * @description Portal 应用入口组件
 */

import { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toast, useWebSocketService } from '@campus/shared';
import { useAuthStore, useNotificationStore } from './store';
import { router } from './router';
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
  const { init: initAuth } = useAuthStore();
  const { notifications, remove } = useNotificationStore();

  // 初始化 WebSocket 服务
  useWebSocketService({
    autoConnect: true,
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

  // 初始化认证状态
  useEffect(() => {
    initAuth();
  }, [initAuth]);

  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />

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
  );
}

export default App;
