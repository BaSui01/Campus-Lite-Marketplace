/**
 * App 根组件
 *
 * @author BaSui 😎
 * @date 2025-11-01
 * @updated 2025-11-08 - 集成全局拦截器和 Tab 同步
 */

import React, { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider, App as AntdApp } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { router } from './router';
import { useAuthStore } from './stores/auth';
import { setupInterceptors } from './utils/setupInterceptors';
import { useTheme } from './hooks';
import './styles/global.css';
import './styles/theme.css';

// ===== 创建 React Query 客户端 =====
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

// ===== Ant Design 主题配置 =====
const antdTheme = {
  token: {
    colorPrimary: '#667eea',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    colorInfo: '#1890ff',
    borderRadius: 8,
    fontSize: 14,
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
  },
  components: {
    Card: {
      borderRadiusLG: 16,
    },
    Button: {
      borderRadius: 8,
    },
    Input: {
      borderRadius: 8,
    },
    Table: {
      borderRadiusLG: 16,
    },
  },
};

const App: React.FC = () => {
  const initFromStorage = useAuthStore((state: any) => state.initFromStorage);
  
  // ===== 🎨 主题切换 Hook =====
  // 会自动应用主题到 DOM（data-theme 属性和 body 类名）
  useTheme();

  // ===== 初始化：全局拦截器 + 登录状态恢复 =====
  useEffect(() => {
    // 1. 初始化全局拦截器（Token 刷新、错误处理、Tab 同步）
    setupInterceptors();
    console.log('[App] ✅ 全局拦截器初始化完成');

    // 2. 从 LocalStorage 恢复登录状态
    initFromStorage();
    console.log('[App] ✅ 登录状态恢复完成');

    // 清理函数：组件卸载时销毁 Tab 同步
    return () => {
      // React 严格模式会导致双重调用，这里不需要清理
      // 因为 tabSync 会自动处理重复初始化
    };
  }, []); // 移除 initFromStorage 依赖，只在组件挂载时执行一次

  return (
    <ConfigProvider locale={zhCN} theme={antdTheme}>
      <AntdApp>
        <QueryClientProvider client={queryClient}>
          <RouterProvider
            router={router}
            future={{
              v7_startTransition: true,
            }}
          />
        </QueryClientProvider>
      </AntdApp>
    </ConfigProvider>
  );
};

export default App;
