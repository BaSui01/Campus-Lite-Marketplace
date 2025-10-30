import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 🎯 BaSui优化：从根目录加载环境变量
  const env = loadEnv(mode, path.resolve(__dirname, '../../..'), 'VITE_');

  return {
    plugins: [react()],

    // 🌐 开发服务器配置
    server: {
      port: parseInt(env.VITE_ADMIN_PORT || '3000'),
      host: true, // 允许外部访问
      open: false, // 不自动打开浏览器
    },

    // 📦 构建配置
    build: {
      outDir: 'dist',
      sourcemap: mode === 'development',
      rollupOptions: {
        output: {
          manualChunks: {
            'react-vendor': ['react', 'react-dom', 'react-router-dom'],
            'antd-vendor': ['antd', '@ant-design/icons'],
            'utils-vendor': ['axios', 'zustand'],
          },
        },
      },
    },

    // 🔧 路径别名
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
        '@campus/shared': path.resolve(__dirname, '../shared/src'),
      },
    },

    // 🌍 环境变量注入到客户端
    define: {
      __APP_VERSION__: JSON.stringify(process.env.npm_package_version || '1.0.0'),
    },
  };
});
