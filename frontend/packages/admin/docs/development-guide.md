# 🚀 管理端开发指南

> **版本**: v1.0.0  
> **撰写**: BaSui 😎  
> **更新**: 2025-11-02
>
> 本文档为团队开发人员提供完整的开发规范、架构指导和最佳实践

---

## 📋 目录

1. [项目概述](#项目概述)
2. [技术栈](#技术栈)
3. [项目结构](#项目结构)
4. [开发环境](#开发环境)
5. [开发规范](#开发规范)
6. [性能优化](#性能优化)
7. [调试与测试](#调试与测试)
8. [部署说明](#部署说明)

---

## 🎯 项目概述

### 项目背景
校园轻享集市管理端是基于校园社区的一站式管理平台，提供用户管理、内容审核、系统配置、 数据统计等全方位管理能力。

### 核心特性
- 🔐 **权限驱动架构**：基于RBAC的细粒度权限控制
- 🔄 **响应式设计**：支持桌面端和移动端完美适配
- ⚡ **高性能**：虚拟化列表、懒加载、缓存优化
- 🧩 **组件化开发**：高复用的组件库和共享层
- 📊 **数据可视化**：丰富的图表和统计功能

---

## 🛠️ 技术栈

### 前端技术
- **框架**: React 18.3.1 + TypeScript 5.x
- **样式**: Ant Design 5.x + CSS-in-JS
- **状态管理**: Zustand 4.x + React Query 5.x
- **路由**: React Router 6.x
- **构建工具**: Vite 5.x
- **包管理**: pnpm Workspace

### 共享架构
- **共享层**: @campus/shared (UI组件、服务、工具)
- **权限系统**: 统一的权限编码和守卫
- **API层**: 统一的HTTP客户端和拦截器

---

## 📁 项目结构

```
frontend/packages/admin/
├─ src/
│  ├─ components/          # 管理端专用组件
│  │  ├─ Layout/          # 布局组件
│  │  ├─ Permission/      # 权限组件
│  │  └─ Charts/          # 图表组件
│  ├─ pages/              # 页面组件
│  │  ├─ Users/           # 用户管理
│  │  ├─ Roles/           # 角色管理
│  │  ├─ Content/         # 内容管理
│  │  └─ System/          # 系统管理
│  ├─ hooks/              # 自定义Hooks
│  ├─ stores/             # 状态管理
│  ├─ utils/              # 工具函数
│  ├─ config/             # 配置文件
│  └─ router/             # 路由配置
├─ docs/                  # 开发文档
└─ tests/                 # 测试文件
```

---

## 🌍 开发环境

### 环境要求
- **Node.js**: ≥18.0.0
- **pnpm**: ≥8.0.0
- **浏览器**: Chrome 90+, Firefox 88+, Safari 14+

### 启动开发环境
```bash
# 安装依赖
pnpm install

# 启动开发服务器
pnpm dev:admin

# 访问 http://localhost:8215
```

### 环境变量配置
```env
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api
VITE_ADMIN_PORT=8215
VITE_ENABLE_MOCK=true
VITE_LOG_LEVEL=debug
```

---

## 📝 开发规范

### 代码风格
- **ESLint**: 使用团队预设的ESLint配置
- **Prettier**: 统一的代码格式化
- **TypeScript**: 严格的类型检查
- **命名规范**:
  - 组件: PascalCase (UserList)
  - 变量/函数: camelCase (getUserList)
  - 常量: UPPER_SNAKE_CASE (API_BASE_URL)

### 文件命名
- **组件文件**: `ComponentName.tsx`
- **Hook文件**: `useHookName.ts`
- **工具文件**: `functionName.ts`
- **类型文件**: `types.ts`

### Git提交规范
```bash
# 提交格式
<type>(<scope>): <subject>
<body>

<footer>
```

- **type**: feat, fix, docs, style, refactor, test, chore
- **scope**: 影响范围
- **subject**: 简短描述
- **body**: 详细描述
- **footer**: 关闭的issue

### 组件开发规范
```tsx
import React from 'react';
import { Button } from 'antd';
import { usePermission } from '@/hooks';
import { PERMISSION_CODES } from '@campus/shared';

interface ComponentProps {
  title: string;
  onAction?: () => void;
}

/**
 * 组件描述
 * @author BaSui 😎
 * @date 2025-11-02
 */
const Component: React.FC<ComponentProps> = ({ title, onAction }) => {
  const { hasPermission } = usePermission();

  return (
    <div>
      {/* 实际实现 */}
    </div>
  );
};

export default Component;
```

---

## ⚡ 性能优化

### 1. 虚拟化列表
```tsx
import { useVirtualList } from '@/utils/performance';

const {
  containerRef,
  visibleItems,
  totalHeight,
  handleScroll,
} = useVirtualList({
  items: dataSource,
  containerHeight: 600,
  itemHeight: 80,
  overscan: 5,
});
```

### 2. 防抖和节流
```tsx
import { debounce, throttle } from '@/utils/performance';

const handleSearch = useMemo(
  () => debounce((keyword: string) => {
    setSearchParams(prev => ({ ...prev, keyword }));
  }, 300),
  [],
);
```

### 3. 懒加载
```tsx
import { useLazyImage } from '@/utils/performance';

const { loaded, error, imageSrc } = useLazyImage(user.avatar);
```

### 4. 批量处理
```tsx
import { useBatchProcessor } from '@/utils/performance';

const { addToQueue, isProcessing } = useBatchProcessor(
  async (batch) => await processUsers(batch),
  batchSize: 10,
  delay: 100
);
```

### 5. 缓存策略
```tsx
const { data } = useQuery({
  queryKey: ['users', searchParams],
  queryFn: fetchUsers,
  staleTime: 30000,   // 30秒内使用缓存
  cacheTime: 300000,  // 5分钟后清理缓存
});
```

---

## 🧪 调试与测试

### 开发调试
- **React Developer Tools**: 安装浏览器扩展
- **Redux DevTools**: 查看状态变化
- **性能面板**: `PerformancePanel` 组件监控内存

### 单元测试
```tsx
import { render, screen, fireEvent } from '@testing-library/react';
import Component from './Component';

describe('Component测试', () => {
  test('应该正确渲染', () => {
    render(<Component title="test" />);
    expect(screen.getByText('test')).toBeInTheDocument();
  });
});
```

### 性能测试
- **渲染测试**: 组件渲染时间 < 16ms
- **内存测试**: 内存使用率 < 80%
- **网络测试**: API请求 < 500ms

---

## 🚀 部署说明

### 构建环境配置
```env
# .env.production
VITE_API_BASE_URL=https://api.example.com
VITE_ADMIN_PORT=3000
VITE_ENABLE_MOCK=false
VITE_LOG_LEVEL=error
```

### 构建命令
```bash
# 构建生产版本
pnpm build:admin

# 预览构建结果
pnpm preview
```

### Docker部署
```dockerfile
# Dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/sites-available/default
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## 🔧 常用工具

### 1. 代码生成器
```bash
# 生成新页面模板
pnpm generate:page PageName

# 生成组件模板
pnpm generate:component ComponentName

# 生成Hook模板
pnpm generate:hook HookName
```

### 2. 调试工具
```tsx
import { performanceMonitor } from '@/utils/debug';

// 开始性能监控
performanceMonitor.start('component-render');

// 结束监控
performanceMonitor.end('component-render');
```

### 3. 日志工具
```tsx
import { logger } from '@/utils/logger';

logger.info('用户登录', { userId, timestamp });
logger.error('操作失败', error);
```

---

## 📚 相关文档

1. [API接口文档](./api-docs.md)
2. [权限系统说明](./permission-system.md)
3. [组件库文档](./components.md)
4. [部署指南](./deployment.md)

---

## 🆘 常见问题

### Q: 如何处理跨域问题？
A: 配置 `vite.config.ts` 的 proxy 选项。

### Q: 如何优化首屏加载？
A: 实现路由懒加载和代码分割。

### Q: 如何处理大数据表格？
A: 使用虚拟化列表和分页加载。

### Q: 如何处理权限控制？
A: 使用 `PermissionGuard` 组件和权限编码。

---

## 📞 技术支持

- **文档维护**: BaSui 😎
- **问题反馈**: 通过项目Issue提交
- **技术讨论**: 团队内部交流群

---

> 💡 **提示**: 本文档会随项目演进持续更新，请保持关注！
