/**
 * App 根组件
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */

import React, { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider, App as AntdApp } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { router } from './router';
import { useAuthStore } from './stores/auth';

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
    borderRadius: 6,
  },
};

const App: React.FC = () => {
  const initFromStorage = useAuthStore((state) => state.initFromStorage);

  // ===== 初始化：从 LocalStorage 恢复登录状态 =====
  useEffect(() => {
    initFromStorage();
  }, [initFromStorage]);

  return (
    <ConfigProvider locale={zhCN} theme={antdTheme}>
      <AntdApp>
        <QueryClientProvider client={queryClient}>
          <RouterProvider router={router} />
        </QueryClientProvider>
      </AntdApp>
    </ConfigProvider>
  );
};

export default App;
