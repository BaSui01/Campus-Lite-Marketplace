# 前端管理端完善 - 架构设计文档

> **功能编号**: 15  
> **功能名称**: 前端管理端完善（架构设计）  
> **作者**: BaSui 😎  
> **创建日期**: 2025-11-08  
> **版本**: v1.0.0  

---

## 📋 目录

- [一、系统架构设计](#一系统架构设计)
- [二、技术方案设计](#二技术方案设计)
- [三、数据流设计](#三数据流设计)
- [四、组件设计](#四组件设计)
- [五、API 设计](#五api-设计)
- [六、状态管理设计](#六状态管理设计)
- [七、路由设计](#七路由设计)
- [八、性能优化方案](#八性能优化方案)
- [九、安全设计](#九安全设计)
- [十、部署方案](#十部署方案)

---

## 一、系统架构设计

### 1.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      前端管理端（@campus/admin）               │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐   │
│  │  Pages Layer  │  │  Layout Layer │  │  Router Layer │   │
│  │  (页面层)      │  │  (布局层)      │  │  (路由层)      │   │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘   │
│          │                  │                  │            │
│  ┌───────┴──────────────────┴──────────────────┴───────┐   │
│  │              Components Layer (组件层)                │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │   │
│  │  │ Data    │ │ Form    │ │ Feedback│ │ Media   │   │   │
│  │  │ Display │ │ Input   │ │ Message │ │ Upload  │   │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘   │   │
│  └───────────────────────────────────────────────────┘   │
│          │                                                  │
│  ┌───────┴──────────────────────────────────────────┐     │
│  │           Hooks Layer (逻辑层)                     │     │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐         │     │
│  │  │ useAuth  │ │ useTable │ │ useForm  │  ...    │     │
│  │  └──────────┘ └──────────┘ └──────────┘         │     │
│  └───────────────────────────────────────────────────┘     │
│          │                                                  │
│  ┌───────┴──────────────────────────────────────────┐     │
│  │       State Management Layer (状态管理层)          │     │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐         │     │
│  │  │  Jotai   │ │  Zustand │ │React Query│        │     │
│  │  │ (原子状态)│ │(全局状态)│ │(服务器状态)│        │     │
│  │  └──────────┘ └──────────┘ └──────────┘         │     │
│  └───────────────────────────────────────────────────┘     │
│          │                                                  │
│  ┌───────┴──────────────────────────────────────────┐     │
│  │          Service Layer (服务层)                    │     │
│  │  基于 OpenAPI 生成的 API Client + 业务封装         │     │
│  └───────────────────────────────────────────────────┘     │
│          │                                                  │
└──────────┼──────────────────────────────────────────────────┘
           │
           ▼
  ┌─────────────────────────────────────────────────────┐
  │              Shared Layer (@campus/shared)          │
  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐  │
  │  │ Types   │ │ Utils   │ │ API     │ │Components│  │
  │  │ (类型)   │ │(工具函数)│ │(API客户端)│ │(共享组件)│  │
  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘  │
  └─────────────────────────────────────────────────────┘
           │
           ▼
  ┌─────────────────────────────────────────────────────┐
  │            Backend API (@campus/backend)            │
  │  Spring Boot REST API + WebSocket + PostgreSQL      │
  └─────────────────────────────────────────────────────┘
```

### 1.2 分层职责

| 层级 | 职责 | 技术栈 |
|------|------|--------|
| **Pages Layer** | 页面级组件，组装业务逻辑 | React Functional Components |
| **Layout Layer** | 布局组件（侧边栏、顶栏、面包屑） | Ant Design Layout |
| **Router Layer** | 路由配置和权限控制 | React Router 6 + 动态路由 |
| **Components Layer** | 可复用的UI组件 | Ant Design + 自定义组件 |
| **Hooks Layer** | 业务逻辑抽象和复用 | React Hooks |
| **State Management** | 全局状态和服务器状态管理 | Jotai + Zustand + React Query |
| **Service Layer** | API 调用和数据转换 | Axios + OpenAPI 生成代码 |
| **Shared Layer** | 跨包共享的代码 | TypeScript Library |

---

## 二、技术方案设计

### 2.1 登录状态管理优化

#### **2.1.1 Token 自动刷新机制**

```typescript
// src/utils/http/interceptors.ts
import axios from 'axios';
import { message } from 'antd';
import { useAuthStore } from '@/stores/auth';

// Token 刷新队列（避免并发刷新）
let isRefreshing = false;
let refreshQueue: Array<(token: string) => void> = [];

// 响应拦截器
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 401 错误且未重试过
    if (error.response?.status === 401 && !originalRequest._retry) {
      // 如果正在刷新，将请求加入队列
      if (isRefreshing) {
        return new Promise((resolve) => {
          refreshQueue.push((token: string) => {
            originalRequest.headers['Authorization'] = `Bearer ${token}`;
            resolve(axios(originalRequest));
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // 调用刷新 Token API
        const { data } = await authApi.refreshToken();
        const newToken = data.data.accessToken;

        // 更新本地 Token
        useAuthStore.getState().setToken(newToken);

        // 重试队列中的请求
        refreshQueue.forEach((callback) => callback(newToken));
        refreshQueue = [];

        // 重试原始请求
        originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
        return axios(originalRequest);
      } catch (refreshError) {
        // 刷新失败，清除登录状态，跳转登录页
        useAuthStore.getState().logout();
        window.location.href = '/login';
        message.error('登录已过期，请重新登录');
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // 403 无权限
    if (error.response?.status === 403) {
      message.error('您没有权限执行此操作');
    }

    // 500 服务器错误
    if (error.response?.status === 500) {
      message.error('服务器错误，请稍后重试');
    }

    return Promise.reject(error);
  }
);
```

#### **2.1.2 多 Tab 登录状态同步**

```typescript
// src/utils/auth/syncTab.ts
import { useAuthStore } from '@/stores/auth';

// 使用 BroadcastChannel 实现多 Tab 同步
const authChannel = new BroadcastChannel('auth-sync');

// 监听其他 Tab 的登录/登出事件
authChannel.onmessage = (event) => {
  const { type, payload } = event.data;

  switch (type) {
    case 'LOGIN':
      // 其他 Tab 登录，同步用户信息
      useAuthStore.getState().setUser(payload.user);
      useAuthStore.getState().setToken(payload.token);
      break;

    case 'LOGOUT':
      // 其他 Tab 登出，清除当前 Tab 状态
      useAuthStore.getState().logout();
      window.location.href = '/login';
      break;

    case 'TOKEN_REFRESH':
      // 其他 Tab 刷新 Token，同步更新
      useAuthStore.getState().setToken(payload.token);
      break;
  }
};

// 登录成功后广播
export const broadcastLogin = (user: User, token: string) => {
  authChannel.postMessage({ type: 'LOGIN', payload: { user, token } });
};

// 登出后广播
export const broadcastLogout = () => {
  authChannel.postMessage({ type: 'LOGOUT' });
};

// Token 刷新后广播
export const broadcastTokenRefresh = (token: string) => {
  authChannel.postMessage({ type: 'TOKEN_REFRESH', payload: { token } });
};
```

#### **2.1.3 权限数据缓存**

```typescript
// src/stores/auth.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  user: User | null;
  token: string | null;
  permissions: string[]; // 权限码列表
  setUser: (user: User) => void;
  setToken: (token: string) => void;
  setPermissions: (permissions: string[]) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      permissions: [],
      setUser: (user) => set({ user }),
      setToken: (token) => set({ token }),
      setPermissions: (permissions) => set({ permissions }),
      logout: () => {
        set({ user: null, token: null, permissions: [] });
        localStorage.clear();
        sessionStorage.clear();
      },
    }),
    {
      name: 'auth-storage', // LocalStorage key
      partialize: (state) => ({
        // 只持久化部分字段
        user: state.user,
        token: state.token,
        permissions: state.permissions,
      }),
    }
  )
);
```

---

### 2.2 缺失页面实现方案

#### **2.2.1 页面模板**

**所有新页面遵循统一模板：**

```typescript
// src/pages/[Module]/[PageName]/index.tsx
import React from 'react';
import { Card, Space, Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { PageHeader, DataTable, SearchBar } from '@/components';
import { use[Module]List } from '@/hooks';

const [PageName]Page: React.FC = () => {
  const {
    data,
    loading,
    pagination,
    handleSearch,
    handleCreate,
    handleEdit,
    handleDelete,
  } = use[Module]List();

  return (
    <div className="page-container">
      {/* 页面头部 */}
      <PageHeader
        title="[页面标题]"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新增
          </Button>
        }
      />

      {/* 搜索栏 */}
      <Card>
        <SearchBar onSearch={handleSearch} />
      </Card>

      {/* 数据表格 */}
      <Card style={{ marginTop: 16 }}>
        <DataTable
          dataSource={data}
          loading={loading}
          pagination={pagination}
          columns={columns}
          rowKey="id"
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      </Card>
    </div>
  );
};

export default [PageName]Page;
```

#### **2.2.2 Hooks 模板**

```typescript
// src/hooks/use[Module]List.ts
import { useState } from 'react';
import { message } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { [module]Api } from '@campus/shared/api';

export const use[Module]List = () => {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useState({});
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10 });

  // 查询列表
  const { data, isLoading } = useQuery({
    queryKey: ['[module]-list', searchParams, pagination],
    queryFn: () =>
      [module]Api.list({
        ...searchParams,
        page: pagination.current - 1,
        size: pagination.pageSize,
      }),
  });

  // 删除
  const deleteMutation = useMutation({
    mutationFn: (id: number) => [module]Api.delete(id),
    onSuccess: () => {
      message.success('删除成功');
      queryClient.invalidateQueries({ queryKey: ['[module]-list'] });
    },
    onError: () => {
      message.error('删除失败');
    },
  });

  const handleSearch = (values: any) => {
    setSearchParams(values);
    setPagination({ ...pagination, current: 1 });
  };

  const handleDelete = (id: number) => {
    deleteMutation.mutate(id);
  };

  return {
    data: data?.data?.records || [],
    loading: isLoading,
    pagination: {
      ...pagination,
      total: data?.data?.total || 0,
      onChange: (page: number, pageSize: number) => {
        setPagination({ current: page, pageSize });
      },
    },
    handleSearch,
    handleDelete,
  };
};
```

---

### 2.3 代码复用方案

#### **2.3.1 通用组件库**

| 组件名称 | 文件路径 | 功能描述 |
|---------|---------|---------|
| **DataTable** | `@/components/DataTable` | 通用数据表格（分页、排序、筛选、操作列） |
| **FormModal** | `@/components/FormModal` | 通用表单弹窗（新增/编辑） |
| **ConfirmDialog** | `@/components/ConfirmDialog` | 确认对话框（删除/操作确认） |
| **ImageUpload** | `@/components/ImageUpload` | 图片上传组件（支持裁剪、压缩） |
| **SearchBar** | `@/components/SearchBar` | 搜索栏（关键词搜索、高级筛选） |
| **PageHeader** | `@/components/PageHeader` | 页面头部（标题、面包屑、操作按钮） |
| **StatusBadge** | `@/components/StatusBadge` | 状态徽章（订单状态、审核状态） |
| **ExportButton** | `@/components/ExportButton` | 导出按钮（Excel/CSV） |

#### **2.3.2 通用 Hooks 库**

| Hook 名称 | 文件路径 | 功能描述 |
|---------|---------|---------|
| **useTable** | `@/hooks/useTable` | 表格状态管理（分页、排序、筛选） |
| **useForm** | `@/hooks/useForm` | 表单状态管理（校验、提交） |
| **useModal** | `@/hooks/useModal` | 弹窗状态管理（打开/关闭） |
| **usePermission** | `@/hooks/usePermission` | 权限判断（按钮级别权限） |
| **useDebounce** | `@/hooks/useDebounce` | 防抖（搜索输入） |
| **useThrottle** | `@/hooks/useThrottle` | 节流（滚动事件） |

---

### 2.4 UI 美化方案

#### **2.4.1 骨架屏实现**

```typescript
// src/components/TableSkeleton/index.tsx
import React from 'react';
import { Skeleton, Table } from 'antd';

interface TableSkeletonProps {
  rows?: number; // 骨架屏行数
  columns?: number; // 骨架屏列数
}

export const TableSkeleton: React.FC<TableSkeletonProps> = ({
  rows = 5,
  columns = 5,
}) => {
  return (
    <Table
      dataSource={Array.from({ length: rows }).map((_, index) => ({
        key: index,
      }))}
      columns={Array.from({ length: columns }).map((_, index) => ({
        key: index,
        dataIndex: `col${index}`,
        render: () => <Skeleton.Input active style={{ width: '100%' }} />,
      }))}
      pagination={false}
    />
  );
};
```

#### **2.4.2 虚拟滚动实现**

```typescript
// 使用 antd 的 virtual 属性
<Table
  dataSource={data}
  columns={columns}
  virtual // 启用虚拟滚动
  scroll={{ y: 600 }} // 固定高度
/>
```

#### **2.4.3 主题切换实现**

```typescript
// src/stores/theme.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { theme } from 'antd';

interface ThemeState {
  isDark: boolean;
  toggleTheme: () => void;
}

export const useThemeStore = create<ThemeState>()(
  persist(
    (set) => ({
      isDark: false,
      toggleTheme: () => set((state) => ({ isDark: !state.isDark })),
    }),
    {
      name: 'theme-storage',
    }
  )
);

// App.tsx
import { ConfigProvider } from 'antd';
import { useThemeStore } from '@/stores/theme';

const App = () => {
  const { isDark } = useThemeStore();

  return (
    <ConfigProvider
      theme={{
        algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
      }}
    >
      {/* 应用内容 */}
    </ConfigProvider>
  );
};
```

---

## 三、数据流设计

### 3.1 数据流向

```
┌────────────────────────────────────────────────────────────┐
│  User Interaction (用户操作)                                 │
└────────────────┬───────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────┐
│  Event Handler (事件处理)                                    │
│  - handleSearch / handleCreate / handleEdit / handleDelete  │
└────────────────┬───────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────┐
│  Custom Hook (业务逻辑)                                      │
│  - useQuery / useMutation (React Query)                     │
└────────────────┬───────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────┐
│  Service Layer (服务层)                                      │
│  - API 封装 (基于 OpenAPI 生成代码)                          │
└────────────────┬───────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────┐
│  HTTP Request (Axios)                                       │
│  - Interceptors (请求拦截、响应拦截、Token 刷新)             │
└────────────────┬───────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────┐
│  Backend API (后端接口)                                      │
│  - Spring Boot REST API                                     │
└────────────────┬───────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────┐
│  Response Data (响应数据)                                    │
└────────────────┬───────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────┐
│  React Query Cache (数据缓存)                               │
│  - 自动缓存、自动刷新、乐观更新                               │
└────────────────┬───────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────┐
│  Component Re-render (组件重新渲染)                          │
│  - 展示最新数据                                              │
└────────────────────────────────────────────────────────────┘
```

### 3.2 状态管理策略

| 状态类型 | 管理方式 | 使用场景 | 示例 |
|---------|---------|---------|------|
| **服务器状态** | React Query | API 数据、列表、详情 | 商品列表、订单详情 |
| **全局状态** | Zustand | 用户信息、Token、权限 | 登录状态、权限缓存 |
| **原子状态** | Jotai | 细粒度状态（管理端复杂表单） | 多步骤表单、筛选器 |
| **本地状态** | useState | 组件内部状态 | 弹窗打开/关闭、输入框值 |
| **URL 状态** | React Router | 路由参数、查询参数 | 分页参数、搜索关键词 |

---

## 四、组件设计

### 4.1 DataTable 组件

```typescript
// src/components/DataTable/index.tsx
import React from 'react';
import { Table, Button, Space, Popconfirm } from 'antd';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { TableProps, ColumnsType } from 'antd/es/table';

interface DataTableProps<T> extends TableProps<T> {
  onEdit?: (record: T) => void;
  onDelete?: (record: T) => void;
  editPermission?: string; // 编辑权限码
  deletePermission?: string; // 删除权限码
}

export const DataTable = <T extends { id: number }>({
  onEdit,
  onDelete,
  editPermission,
  deletePermission,
  columns,
  ...restProps
}: DataTableProps<T>) => {
  const { hasPermission } = usePermission();

  // 自动添加操作列
  const actionColumn: ColumnsType<T>[0] = {
    title: '操作',
    key: 'action',
    fixed: 'right',
    width: 150,
    render: (_, record) => (
      <Space size="small">
        {onEdit && (!editPermission || hasPermission(editPermission)) && (
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => onEdit(record)}
          >
            编辑
          </Button>
        )}
        {onDelete && (!deletePermission || hasPermission(deletePermission)) && (
          <Popconfirm
            title="确定要删除吗？"
            onConfirm={() => onDelete(record)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        )}
      </Space>
    ),
  };

  return (
    <Table
      columns={[...columns!, actionColumn]}
      scroll={{ x: 'max-content' }}
      {...restProps}
    />
  );
};
```

### 4.2 FormModal 组件

```typescript
// src/components/FormModal/index.tsx
import React from 'react';
import { Modal, Form, message } from 'antd';
import type { FormInstance } from 'antd';

interface FormModalProps {
  title: string;
  open: boolean;
  initialValues?: any;
  onCancel: () => void;
  onSubmit: (values: any) => Promise<void>;
  children: React.ReactNode;
}

export const FormModal: React.FC<FormModalProps> = ({
  title,
  open,
  initialValues,
  onCancel,
  onSubmit,
  children,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = React.useState(false);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);
      await onSubmit(values);
      message.success('操作成功');
      form.resetFields();
      onCancel();
    } catch (error) {
      console.error('表单提交失败', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title={title}
      open={open}
      onCancel={onCancel}
      onOk={handleOk}
      confirmLoading={loading}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={initialValues}
      >
        {children}
      </Form>
    </Modal>
  );
};
```

---

## 五、API 设计

### 5.1 API 服务封装

```typescript
// src/services/[module].service.ts
import { [Module]Api } from '@campus/shared/api';
import type { [Module]DTO } from '@campus/shared/api/models';

/**
 * [模块]服务层
 * 基于 OpenAPI 生成代码的二次封装
 */
export class [Module]Service {
  /**
   * 获取列表（带分页）
   */
  static async getList(params: {
    page: number;
    size: number;
    keyword?: string;
  }) {
    const response = await [Module]Api.list({
      page: params.page - 1, // 后端从 0 开始
      size: params.size,
      keyword: params.keyword,
    });
    return response.data;
  }

  /**
   * 获取详情
   */
  static async getDetail(id: number) {
    const response = await [Module]Api.getById(id);
    return response.data;
  }

  /**
   * 创建
   */
  static async create(data: Partial<[Module]DTO>) {
    const response = await [Module]Api.create(data);
    return response.data;
  }

  /**
   * 更新
   */
  static async update(id: number, data: Partial<[Module]DTO>) {
    const response = await [Module]Api.update(id, data);
    return response.data;
  }

  /**
   * 删除
   */
  static async delete(id: number) {
    const response = await [Module]Api.delete(id);
    return response.data;
  }
}
```

---

## 六、状态管理设计

### 6.1 Auth Store（全局状态）

```typescript
// src/stores/auth.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi } from '@/services/auth.service';

interface User {
  id: number;
  username: string;
  email: string;
  roles: string[];
}

interface AuthState {
  user: User | null;
  token: string | null;
  permissions: string[];
  isAuthenticated: boolean;

  // Actions
  setUser: (user: User) => void;
  setToken: (token: string) => void;
  setPermissions: (permissions: string[]) => void;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      permissions: [],
      isAuthenticated: false,

      setUser: (user) => set({ user, isAuthenticated: true }),
      setToken: (token) => set({ token }),
      setPermissions: (permissions) => set({ permissions }),

      login: async (username, password) => {
        const response = await authApi.login({ username, password });
        const { user, token, permissions } = response.data;
        set({ user, token, permissions, isAuthenticated: true });
      },

      logout: () => {
        set({ user: null, token: null, permissions: [], isAuthenticated: false });
        localStorage.clear();
        sessionStorage.clear();
      },

      refreshToken: async () => {
        const response = await authApi.refreshToken();
        const { token } = response.data;
        set({ token });
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        permissions: state.permissions,
      }),
    }
  )
);
```

---

## 七、路由设计

### 7.1 路由配置

```typescript
// src/router/routes.tsx
import { lazy } from 'react';
import type { RouteObject } from 'react-router-dom';

// 懒加载页面
const PaymentList = lazy(() => import('@/pages/Payment/List'));
const MessageList = lazy(() => import('@/pages/Message/List'));
const ExportCenter = lazy(() => import('@/pages/Export/Center'));
// ... 其他页面

export const routes: RouteObject[] = [
  {
    path: '/',
    element: <AdminLayout />,
    children: [
      // 支付管理
      {
        path: 'payments',
        children: [
          { path: 'list', element: <PaymentList />, meta: { permission: 'payment:view' } },
        ],
      },
      // 消息管理
      {
        path: 'messages',
        children: [
          { path: 'list', element: <MessageList />, meta: { permission: 'message:view' } },
        ],
      },
      // 导出中心
      {
        path: 'export',
        children: [
          { path: 'center', element: <ExportCenter />, meta: { permission: 'export:view' } },
        ],
      },
      // ... 其他路由
    ],
  },
];
```

### 7.2 路由守卫

```typescript
// src/router/guard.tsx
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/auth';
import { usePermission } from '@/hooks/usePermission';

export const RouteGuard: React.FC<{ children: React.ReactNode; permission?: string }> = ({
  children,
  permission,
}) => {
  const location = useLocation();
  const { isAuthenticated } = useAuthStore();
  const { hasPermission } = usePermission();

  // 未登录，跳转登录页
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 无权限，跳转403页面
  if (permission && !hasPermission(permission)) {
    return <Navigate to="/403" replace />;
  }

  return <>{children}</>;
};
```

---

## 八、性能优化方案

### 8.1 代码分割

```typescript
// 路由级别代码分割
import { lazy, Suspense } from 'react';
import { TableSkeleton } from '@/components';

const PaymentList = lazy(() => import('@/pages/Payment/List'));

// 使用 Suspense 包裹
<Suspense fallback={<TableSkeleton />}>
  <PaymentList />
</Suspense>
```

### 8.2 图片懒加载

```typescript
// src/components/LazyImage/index.tsx
import React, { useState, useEffect, useRef } from 'react';

export const LazyImage: React.FC<{ src: string; alt: string }> = ({ src, alt }) => {
  const [isVisible, setIsVisible] = useState(false);
  const imgRef = useRef<HTMLImageElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
          observer.disconnect();
        }
      },
      { threshold: 0.1 }
    );

    if (imgRef.current) {
      observer.observe(imgRef.current);
    }

    return () => observer.disconnect();
  }, []);

  return (
    <img
      ref={imgRef}
      src={isVisible ? src : ''}
      alt={alt}
      loading="lazy"
      style={{ opacity: isVisible ? 1 : 0, transition: 'opacity 0.3s' }}
    />
  );
};
```

### 8.3 虚拟滚动

```typescript
// 使用 antd Table 的 virtual 属性
<Table
  dataSource={data}
  columns={columns}
  virtual // 启用虚拟滚动
  scroll={{ y: 600 }} // 固定高度
  pagination={false} // 禁用分页
/>
```

---

## 九、安全设计

### 9.1 XSS 防护

```typescript
// 使用 DOMPurify 清理 HTML
import DOMPurify from 'dompurify';

const SafeHTML: React.FC<{ html: string }> = ({ html }) => {
  const cleanHTML = DOMPurify.sanitize(html);
  return <div dangerouslySetInnerHTML={{ __html: cleanHTML }} />;
};
```

### 9.2 CSRF 防护

```typescript
// Axios 自动携带 CSRF Token
axios.interceptors.request.use((config) => {
  const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
  if (csrfToken) {
    config.headers['X-CSRF-Token'] = csrfToken;
  }
  return config;
});
```

### 9.3 权限控制

```typescript
// 按钮级别权限控制
import { usePermission } from '@/hooks/usePermission';

const MyComponent = () => {
  const { hasPermission } = usePermission();

  return (
    <>
      {hasPermission('user:delete') && (
        <Button danger onClick={handleDelete}>
          删除用户
        </Button>
      )}
    </>
  );
};
```

---

## 十、部署方案

### 10.1 构建配置

```typescript
// vite.config.ts
export default defineConfig({
  build: {
    outDir: 'dist',
    sourcemap: false, // 生产环境关闭 source map
    rollupOptions: {
      output: {
        manualChunks: {
          // 代码分割
          vendor: ['react', 'react-dom', 'react-router-dom'],
          antd: ['antd', '@ant-design/icons'],
          charts: ['echarts'],
        },
      },
    },
    chunkSizeWarningLimit: 1000, // 增大警告阈值
  },
});
```

### 10.2 Nginx 配置

```nginx
server {
  listen 80;
  server_name admin.campus-marketplace.com;

  root /var/www/admin/dist;
  index index.html;

  # Gzip 压缩
  gzip on;
  gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

  # SPA 路由
  location / {
    try_files $uri $uri/ /index.html;
  }

  # API 代理
  location /api/ {
    proxy_pass http://localhost:8200;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }

  # 静态资源缓存
  location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
  }
}
```

---

## 📚 参考文档

- [全局技术栈文档](../tech.md)
- [全局项目结构文档](../structure.md)
- [前端开发规范](../../../frontend/CLAUDE.md)
- [React 官方文档](https://react.dev/)
- [Ant Design 官方文档](https://ant.design/)
- [React Query 官方文档](https://tanstack.com/query/latest)

---

**文档版本**: v1.0.0  
**最后更新**: 2025-11-08  
**审批状态**: 待审批 ⏳
