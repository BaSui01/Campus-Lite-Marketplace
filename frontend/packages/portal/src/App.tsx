/**
 * 应用根组件
 * @author BaSui 😎
 * @description Portal 应用入口组件
 */

import { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider, App as AntdApp } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { useWebSocketService } from '@campus/shared';
import { useAuthStore } from './store';
import { router } from './router';
import { useTheme } from './hooks/useTheme';
import ErrorBoundary from './components/ErrorBoundary';
import './styles/theme.css';
import './App.css';
// 🔧 BaSui 修复：加载认证调试工具（开发环境）
import './utils/authDebug';

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

  // 初始化主题（自动应用保存的主题偏好）
  useTheme();

  // 初始化认证状态
  useEffect(() => {
    initAuth();
  }, [initAuth]);

  // 初始化 WebSocket 服务（只有登录后且启用时才连接！🎯）
  const websocketEnabled = import.meta.env.VITE_ENABLE_WEBSOCKET !== 'false';
  
  useWebSocketService({
    autoConnect: isAuthenticated && websocketEnabled, // ✅ 根据登录状态和配置决定是否连接
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
    <ConfigProvider locale={zhCN}>
      <AntdApp>
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
          </QueryClientProvider>
        </ErrorBoundary>
      </AntdApp>
    </ConfigProvider>
  );
}

export default App;
