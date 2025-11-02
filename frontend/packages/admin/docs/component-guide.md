# 🧩 管理端组件使用指南

> **版本**: v1.0.0  
> **撰写**: BaSui 😎  
> **更新**: 2025-11-02

---

## 📋 目录

1. [权限组件](#权限组件)
2. [布局组件](#布局组件)
3. [图表组件](#图表组件)
4. [业务组件](#业务组件)
5. [工具组件](#工具组件)

---

## 🔐 权限组件

### PermissionGuard
权限守卫组件，用于控制页面和功能的访问权限。

```tsx
import { PermissionGuard } from '@/components';

<PermissionGuard permission={PERMISSION_CODES.SYSTEM_USER_VIEW}>
  <UserList />
</PermissionGuard>

// 多个权限（OR关系）
<PermissionGuard permissions={[
  PERMISSION_CODES.SYSTEM_USER_BAN,
  PERMISSION_CODES.SYSTEM_USER_UNBAN
]}>
  <UserManagementActions />
</PermissionGuard>

// 多个权限（AND关系）
<PermissionGuard 
  permissions={[PERMISSION_CODES.USER_VIEW, PERMISSION_CODES.USER_EDIT]}
  requireAll={true}
>
  <UserDetailedActions />
</PermissionGuard>
```

**Props**:
```typescript
interface PermissionGuardProps {
  // 单个权限
  permission?: string;
  // 多个权限（OR关系）
  permissions?: string[];
  // 是否需要同时拥有所有权限（AND关系）
  requireAll?: boolean;
  // children: React.ReactNode;
  // 无权限时的替代内容
  fallback?: React.ReactNode;
}
```

---

## 📐 布局组件

### AdminLayout
管理端主布局组件，提供侧边栏导航和顶部操作栏。

```tsx
import { AdminLayout } from '@/components';

<AdminLayout>
  <Router>
    <Routes>
      <Route path="/dashboard" element={<Dashboard />} />
    </Routes>
  </Router>
</AdminLayout>
```

**特性**:
- 响应式侧边栏（桌面/移动端自适应）
- 用户信息展示和退出功能
- 权限控制的动态菜单渲染
- 当前页面高亮

### 菜单配置
```tsx
// src/config/menu.ts
export const MENU_ITEMS: MenuItem[] = [
  {
    key: 'dashboard',
    label: '仪表盘',
    icon: 'DashboardOutlined',
    path: '/admin/dashboard',
    permission: PERMISSION_CODES.SYSTEM_STATISTICS_VIEW,
  },
  // ...
];
```

---

## 📊 图表组件

### StatCard
统计卡片组件，用于展示核心业务指标。

```tsx
import { StatCard } from '@/components';

<StatCard
  title="总用户数"
  value={1234}
  icon={<UserOutlined />}
  color="#1890ff"
  trend={12}
  trendLabel="较上月"
  loading={isLoading}
/>
```

**Props**:
```typescript
interface StatCardProps {
  title: string;           // 卡片标题
  value: number | string;  // 显示数值
  icon?: React.ReactNode;   // 图标
  color?: string;           // 主题色
  prefix?: string;          // 数值前缀（如货币符号）
  suffix?: string;          // 数值后缀
  trend?: number;           // 趋势百分比
  trendLabel?: string;      // 趋势描述
  loading?: boolean;        // 加载状态
}
```

### LineChart / BarChart
基于ECharts的图表组件，提供数据可视化能力。

```tsx
import { LineChart, BarChart } from '@/components';

// 折线图
<LineChart 
  data={trendData}
  title="用户增长趋势"
  height={300}
  color="#52c41a"
/>

// 柱状图
<BarChart 
  data={categoryData}
  title="商品类别分布"
  height={350}
  color="#1890ff"
/>
```

**Props**:
```typescript
interface ChartProps {
  data: Array<{ time: string; value: number }> | 
        Array<{ name: string; value: number }>;
  title?: string;     // 图表标题
  height?: number;    // 图表高度
  color?: string;     // 主题色
}
```

---

## 🏢 业务组件

### UserTable
用户表格组件，集成用户管理的常用操作。

```tsx
import { UserTable } from '@/components';

<UserTable
  onUserSelect={(user) => setSelectedUser(user)}
  onUserBan={(user) => handleBanUser(user)}
  onUserExport={() => handleExport()}
  showActions={hasPermission}
/>
```

### ReportCard
举报处理卡片，展示举报信息和处理操作。

```tsx
import { ReportCard } from '@/components';

<ReportCard
  report={reportData}
  onApprove={(report, result) => handleApprove(report, result)}
  onReject={(report, reason) => handleReject(report, reason)}
  loading={isProcessing}
/>
```

---

## 🛠️ 工具组件

### VirtualizedTable
虚拟化表格组件，用于高效展示大量数据。

```tsx
import { VirtualizedTable } from '@/components';

<VirtualizedTable
  dataSource={largeDataSet}
  columns={columns}
  itemHeight={60}
  containerHeight={500}
  overscan={10}
  onSelect={(selectedRows) => setSelectedRows(selectedRows)}
/>
```

### LazyImage
图片懒加载组件，优化图片加载性能。

```tsx
import { LazyImage } from '@/components';

<LazyImage
  src={user.avatar}
  alt={user.nickname}
  placeholder={<Avatar>{user.nickname[0]}</Avatar>}
  loadStrategy="lazy"
  cacheTime={3600000} // 1小时缓存
/>
```

### PerformancePanel
性能监控面板，开发环境下显示系统性能状态。

```tsx
import { PerformancePanel } from '@/components';

<PerformancePanel visible={process.env.NODE_ENV === 'development'} />
```

---

## 🔧 自定义Hooks

### usePermission
权限检查Hook，提供便捷的权限判断功能。

```tsx
const { 
  hasPermission, 
  hasAnyPermission, 
  hasAllPermissions 
} = usePermission();

// 单个权限检查
if (hasPermission(PERMISSION_CODES.SYSTEM_USER_VIEW)) {
  // 有权限的逻辑
}

// 多个权限检查（OR）
if (hasAnyPermission([...permissions])) {
  // 拥有任一权限
}

// 多个权限检查（AND）
if (hasAllPermissions([...permissions])) {
  // 拥有所有权限
}
```

### useVirtualList
虚拟化列表Hook，优化长列表性能。

```tsx
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

### useLazyImage
图片懒加载Hook，自动处理图片加载状态。

```tsx
const { loaded, error, imageSrc } = useLazyImage(avatarUrl);
```

### useBatchProcessor
批量处理Hook，用于优化大批量数据处理。

```tsx
const { addToQueue, isProcessing, queueLength } = useBatchProcessor(
  async (batch) => await processUsers(batch),
  batchSize: 10,
  delay: 100
);
```

---

## 🎨 样式主题

### Ant Design主题定制
```tsx
// src/theme.ts
export const antdTheme = {
  token: {
    colorPrimary: '#667eea',
    borderRadius: 6,
    fontSize: 14,
  },
  components: {
    Button: {
      borderRadius: 4,
    },
    Table: {
      // 表格定制
    },
  },
};
```

### CSS变量
```css
:root {
  --admin-primary-color: #667eea;
  --admin-text-color: #333333;
  --admin-border-color: #e8e8e8;
  --admin-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
```

---

## 📱 响应式设计

### 断点配置
```tsx
// src/constants/responsive.ts
export const breakpoints = {
  xs: '480px',
  sm: '576px',
  md: '768px',
  lg: '992px',
  xl: '1200px',
  xxl: '1600px',
};
```

### 响应式Hook
```tsx
import { useBreakpoint } from '@/hooks';

const { breakpoint, isMobile, isTablet, isDesktop } = useBreakpoint();

if (isMobile) {
  // 移动端逻辑
}
```

---

## 🚀 最佳实践

### 1. 组件设计原则
- **单一职责**: 每个组件只负责一个功能
- **可配置性**: 通过props控制组件行为
- **可测试性**: 组件应该易于测试
- **性能优先**: 考虑大列表、大图片等场景

### 2. 内存管理
```tsx
// 正确的做法
React.useEffect(() => {
  const timer = setInterval(() => {
    // 定时逻辑
  }, 1000);

  return () => clearInterval(timer); // 清理定时器
}, []);
```

### 3. 错误边界
```tsx
// ErrorBoundary组件
<ErrorBoundary
  fallback={<div>加载失败，请重试</div>}
>
  <Suspense fallback={<Skeleton active />}>
    <LazyComponent />
  </Suspense>
</ErrorBoundary>
```

### 4. 缓存策略
```tsx
// React Query缓存配置
useQuery({
  queryKey: ['users', pagination],
  queryFn: fetchUsers,
  staleTime: 30000,    // 30秒内使用缓存
  cacheTime: 300000,   // 5分钟后清理
  refetchOnWindowFocus: false,
});
```

---

## ❓ 常见问题

### Q: 如何自定义表格样式？
A: 使用 `rowClassName` 和 `className` 属性，或者通过CSS变量全局覆盖。

### Q: 如何处理大文件上传？
A: 使用分片上传 + 进度显示的 `LargeUpload` 组件。

### Q: 如何实现组件懒加载？
A: 使用 `React.lazy()` 配合 `Suspense` 组件。

---

## 🔗 相关链接

- [Ant Design文档](https://ant.design/docs/react/introduce)
- [React Query文档](https://tanstack.com/query/latest)
- [Zustand文档](https://docs.pmnd.rs/zustand/getting-started/introduction)

---

> 💡 **提示**: 更多组件示例请查看 `src/examples` 目录！
