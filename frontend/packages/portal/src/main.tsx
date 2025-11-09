import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import { suppressEChartsResizeObserverError } from '@campus/shared/utils';

// 🛡️ 启用 ECharts ResizeObserver 错误抑制器（开发环境）
suppressEChartsResizeObserverError();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>
);
