# 前端门户开发 - 技术设计

> **编号**: Spec-001  
> **功能**: 校园轻享集市 - 用户端门户  
> **作者**: BaSui 😎  
> **日期**: 2025-11-05  
> **状态**: 🏗️ 设计阶段

---

## 📋 目录

- [概述](#概述)
- [技术架构](#技术架构)
- [目录结构](#目录结构)
- [路由设计](#路由设计)
- [状态管理](#状态管理)
- [API对接](#api对接)
- [组件设计](#组件设计)
- [性能优化](#性能优化)
- [安全设计](#安全设计)

---

## 1. 概述

### 1.1 设计目标

- 🎯 **模块化**: 页面和组件高度模块化，易于维护和扩展
- ⚡ **高性能**: 首屏加载<2秒，页面切换<300ms，流畅60fps
- 📱 **响应式**: 完美适配PC、Tablet、Mobile三端
- 🔐 **安全可靠**: 完善的权限控制、XSS/CSRF防护
- 🧪 **可测试**: 单元测试覆盖率≥80%

### 1.2 技术栈

**参考**: [docs/specs/tech.md](../tech.md#前端技术栈)

**核心框架**:
- React 18.3.1（并发特性、Suspense）
- TypeScript 5.5.3（类型安全）
- Vite 5.3.3（构建工具）

**状态管理**:
- Zustand 4.5.4（轻量级状态管理）
- TanStack React Query 5.51.0（服务端状态）

**UI框架**:
- Tailwind CSS 3.4.6（原子化CSS）
- Headless UI（无样式组件库）

**工具库**:
- Axios（HTTP客户端）
- React Router DOM 6.24.1（路由）
- React Hook Form（表单）
- Zod（表单验证）

### 1.3 架构原则

1. **单向数据流**: 数据从父组件流向子组件
2. **关注点分离**: UI逻辑、业务逻辑、数据访问分离
3. **DRY原则**: 复用组件、Hooks、工具函数
4. **KISS原则**: 保持简单，避免过度设计
5. **渐进式增强**: 基础功能优先，逐步增加高级特性

---

## 2. 技术架构

### 2.1 Monorepo架构

**参考**: [docs/specs/structure.md](../structure.md#前端结构)

```
frontend/
├── packages/
│   ├── portal/      # 📱 用户端门户（本项目）
│   ├── admin/       # 📊 管理端（独立项目）
│   └── shared/      # 🔗 共享层
│       ├── api/     # 自动生成的API（252个models）
│       ├── services/# 业务服务层
│       ├── utils/   # 工具函数
│       ├── hooks/   # 共享Hooks
│       └── components/ # 共享组件
```

**依赖关系**:
```
portal ─┐
        ├──> shared
admin ──┘
```

### 2.2 分层架构

```
┌─────────────────────────────────────┐
│  Pages（页面层）                      │
│  - 路由组件                          │
│  - 页面级状态                        │
│  - 数据获取                          │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Components（组件层）                │
│  - 业务组件                          │
│  - 通用组件                          │
│  - UI组件                            │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Hooks（逻辑层）                     │
│  - 数据Hooks                         │
│  - 业务Hooks                         │
│  - 工具Hooks                         │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Services（服务层）                  │
│  - API封装                           │
│  - 业务逻辑                          │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  API（数据层）                       │
│  - DefaultApi（自动生成）            │
│  - 252个TypeScript类型               │
└─────────────────────────────────────┘
```

---

## 3. 目录结构

**参考**: [docs/specs/structure.md](../structure.md#frontend-packages-portal)

```
frontend/packages/portal/src/
├── pages/              # 📄 页面组件
│   ├── Home/          # 首页
│   │   ├── index.tsx
│   │   ├── Hero.tsx
│   │   ├── HotGoods.tsx
│   │   └── Categories.tsx
│   ├── Goods/         # 商品相关
│   │   ├── List/
│   │   ├── Detail/
│   │   └── Publish/
│   ├── Order/         # 订单相关
│   ├── Community/     # 社区相关
│   ├── Profile/       # 个人中心
│   └── Auth/          # 认证相关
│       ├── Login/
│       └── Register/
│
├── components/         # 🧩 组件
│   ├── Layout/        # 布局组件
│   │   ├── Header.tsx
│   │   ├── Footer.tsx
│   │   └── Sidebar.tsx
│   ├── Goods/         # 商品组件
│   │   ├── GoodsCard.tsx
│   │   ├── GoodsGrid.tsx
│   │   └── GoodsFilter.tsx
│   ├── Order/         # 订单组件
│   └── Common/        # 通用组件
│       ├── Button.tsx
│       ├── Input.tsx
│       ├── Modal.tsx
│       └── Loading.tsx
│
├── hooks/              # 🪝 自定义Hooks
│   ├── useGoods.ts    # 商品Hooks
│   ├── useOrder.ts    # 订单Hooks
│   ├── useAuth.ts     # 认证Hooks
│   └── useWebSocket.ts # WebSocket Hooks
│
├── store/              # 📦 状态管理
│   ├── auth.ts        # 认证状态
│   ├── cart.ts        # 购物车状态
│   └── ui.ts          # UI状态
│
├── router/             # 🛤️ 路由配置
│   ├── index.tsx      # 路由定义
│   └── guards.tsx     # 路由守卫
│
├── styles/             # 🎨 样式
│   ├── index.css      # 全局样式
│   └── tailwind.css   # Tailwind配置
│
├── types/              # 📝 类型定义
│   ├── index.ts
│   └── custom.d.ts
│
└── utils/              # 🛠️ 工具函数
    ├── format.ts      # 格式化
    └── validator.ts   # 验证
```

---

## 4. 路由设计

### 4.1 路由表

```typescript
// router/index.tsx
import { lazy, Suspense } from 'react';
import { createBrowserRouter } from 'react-router-dom';
import Layout from '../components/Layout';
import RequireAuth from './guards/RequireAuth';
import Loading from '../components/Common/Loading';

// 懒加载组件
const Home = lazy(() => import('../pages/Home'));
const GoodsList = lazy(() => import('../pages/Goods/List'));
const GoodsDetail = lazy(() => import('../pages/Goods/Detail'));
const GoodsPublish = lazy(() => import('../pages/Goods/Publish'));
const OrderList = lazy(() => import('../pages/Order/List'));
const OrderDetail = lazy(() => import('../pages/Order/Detail'));
const Community = lazy(() => import('../pages/Community'));
const PostDetail = lazy(() => import('../pages/Community/PostDetail'));
const Profile = lazy(() => import('../pages/Profile'));
const Login = lazy(() => import('../pages/Auth/Login'));
const Register = lazy(() => import('../pages/Auth/Register'));

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      // 公共路由（无需登录）
      {
        index: true,
        element: (
          <Suspense fallback={<Loading />}>
            <Home />
          </Suspense>
        )
      },
      {
        path: 'goods',
        children: [
          {
            index: true,
            element: (
              <Suspense fallback={<Loading />}>
                <GoodsList />
              </Suspense>
            )
          },
          {
            path: ':id',
            element: (
              <Suspense fallback={<Loading />}>
                <GoodsDetail />
              </Suspense>
            )
          }
        ]
      },
      {
        path: 'community',
        children: [
          {
            index: true,
            element: (
              <Suspense fallback={<Loading />}>
                <Community />
              </Suspense>
            )
          },
          {
            path: ':id',
            element: (
              <Suspense fallback={<Loading />}>
                <PostDetail />
              </Suspense>
            )
          }
        ]
      },
      
      // 受保护路由（需要登录）
      {
        path: 'goods/publish',
        element: (
          <RequireAuth>
            <Suspense fallback={<Loading />}>
              <GoodsPublish />
            </Suspense>
          </RequireAuth>
        )
      },
      {
        path: 'orders',
        element: (
          <RequireAuth>
            <Suspense fallback={<Loading />}>
              <OrderList />
            </Suspense>
          </RequireAuth>
        )
      },
      {
        path: 'orders/:id',
        element: (
          <RequireAuth>
            <Suspense fallback={<Loading />}>
              <OrderDetail />
            </Suspense>
          </RequireAuth>
        )
      },
      {
        path: 'profile',
        element: (
          <RequireAuth>
            <Suspense fallback={<Loading />}>
              <Profile />
            </Suspense>
          </RequireAuth>
        )
      },
      
      // 认证路由
      {
        path: 'login',
        element: (
          <Suspense fallback={<Loading />}>
            <Login />
          </Suspense>
        )
      },
      {
        path: 'register',
        element: (
          <Suspense fallback={<Loading />}>
            <Register />
          </Suspense>
        )
      }
    ]
  }
]);
```

### 4.2 路由守卫

```typescript
// router/guards/RequireAuth.tsx
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/auth';

interface RequireAuthProps {
  children: React.ReactNode;
}

export default function RequireAuth({ children }: RequireAuthProps) {
  const { isAuthenticated } = useAuthStore();
  const location = useLocation();

  if (!isAuthenticated) {
    // 保存当前路径到redirect参数
    const redirect = encodeURIComponent(location.pathname + location.search);
    return <Navigate to={`/login?redirect=${redirect}`} replace />;
  }

  return <>{children}</>;
}
```

**实现文档**: [docs/specs/AUTH_REDIRECT.md](../AUTH_REDIRECT.md)

---

## 5. 状态管理

### 5.1 Zustand（客户端状态）

```typescript
// store/auth.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authService } from '@campus/shared/services';
import type { User } from '@campus/shared/api/models';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  updateUser: (user: User) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      login: async (credentials) => {
        const result = await authService.login(credentials);
        set({
          user: result.user,
          token: result.token,
          isAuthenticated: true
        });
        localStorage.setItem('token', result.token);
      },

      logout: () => {
        set({
          user: null,
          token: null,
          isAuthenticated: false
        });
        localStorage.removeItem('token');
      },

      updateUser: (user) => {
        set({ user });
      }
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated
      })
    }
  )
);
```

### 5.2 React Query（服务端状态）

```typescript
// hooks/useGoods.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { DefaultApi } from '@campus/shared/api';
import type { Goods, CreateGoodsRequest } from '@campus/shared/api/models';

const api = new DefaultApi();

// 商品列表
export function useGoodsList(params: GoodsListParams) {
  return useQuery({
    queryKey: ['goods', 'list', params],
    queryFn: async () => {
      const response = await api.listGoods(params);
      return response.data.data || [];
    },
    staleTime: 5 * 60 * 1000, // 5分钟内缓存有效
    gcTime: 10 * 60 * 1000 // 10分钟后垃圾回收
  });
}

// 商品详情
export function useGoodsDetail(id: number) {
  return useQuery({
    queryKey: ['goods', 'detail', id],
    queryFn: async () => {
      const response = await api.getGoodsDetail({ id });
      return response.data.data;
    },
    enabled: !!id
  });
}

// 创建商品
export function useCreateGoods() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: CreateGoodsRequest) => {
      const response = await api.createGoods({ createGoodsRequest: data });
      return response.data.data;
    },
    onSuccess: () => {
      // 刷新商品列表缓存
      queryClient.invalidateQueries({ queryKey: ['goods', 'list'] });
    }
  });
}
```

---

## 6. API对接

### 6.1 推荐方式：使用Services层 ⭐⭐⭐

**重要**: 优先使用Services而不是直接调用API！

**Services的优势**:
1. ✅ **更简洁** - 自动处理response.data.data提取
2. ✅ **类型安全** - 完整的TypeScript类型推导
3. ✅ **业务封装** - 包含业务逻辑和错误处理
4. ✅ **统一维护** - API变更只需改一处

**推荐用法**:
```typescript
// ✅ 推荐：使用Services层
import { goodsService } from '@campus/shared/services';

// 简洁的API调用，直接返回数据
const goods = await goodsService.getList({ 
  page: 0, 
  size: 20,
  campusId: 1 
});
// goods 已经是 Goods[] 类型，无需提取response.data.data

// 创建商品
const newGoods = await goodsService.create({
  title: '商品标题',
  price: 99.99,
  // ...
});

// 更新商品
await goodsService.update(id, { title: '新标题' });
```

**Services清单**:
```typescript
import {
  authService,      // 登录、注册、登出
  goodsService,     // 商品CRUD
  orderService,     // 订单管理
  postService,      // 帖子管理
  messageService,   // 消息管理
  refundService,    // 退款管理
  uploadService,    // 文件上传
  userService,      // 用户管理
  reportService     // 举报管理
} from '@campus/shared/services';
```

### 6.2 备选方式：直接使用API

**仅在Services不满足需求时使用**:

```typescript
// ❌ 不推荐：直接使用API（繁琐）
import { DefaultApi } from '@campus/shared/api';
import type { Goods } from '@campus/shared/api/models';

const api = new DefaultApi();
const response = await api.listGoods({
  page: 0,
  size: 20,
  campusId: 1
});

// 需要手动提取data
const goods: Goods[] = response.data.data || [];
```

### 6.3 配合React Query使用

```typescript
// hooks/useGoods.ts
import { useQuery } from '@tanstack/react-query';
import { goodsService } from '@campus/shared/services';

export function useGoodsList(params) {
  return useQuery({
    queryKey: ['goods', 'list', params],
    queryFn: () => goodsService.getList(params), // 使用Service
    staleTime: 5 * 60 * 1000
  });
}

// 使用
const { data: goods, isLoading } = useGoodsList({ page: 0, size: 20 });
```

### 6.4 API自动化（底层实现）

**完整说明**: [docs/FRONTEND_ARCHITECTURE.md#api-自动化工作流](../../FRONTEND_ARCHITECTURE.md#🤖-api-自动化工作流)

```typescript
// @campus/shared/api 自动生成（252个models）
import { DefaultApi, Configuration } from '@campus/shared/api';
import type { Goods, User, Order } from '@campus/shared/api/models';

// Services内部使用DefaultApi
const api = new DefaultApi();
```

### 6.5 Axios拦截器（已实现）

```typescript
// @campus/shared/utils/apiClient.ts（已实现，无需修改）
import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
});

// 请求拦截器：自动添加Token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：401自动跳转登录
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 保存当前路径
      const currentPath = window.location.pathname + window.location.search;
      const redirect = encodeURIComponent(currentPath);
      window.location.href = `/login?redirect=${redirect}`;
    }
    return Promise.reject(error);
  }
);
```

---

## 7. 组件设计

### 7.1 组件复用策略 ⭐⭐⭐

**重要原则**: 优先使用@campus/shared/components，避免重复开发！

**共享层已有24个组件**:
- ✅ 基础组件：Button, Input, Select, Modal, Loading, Skeleton, Empty等
- ✅ 业务组件：**GoodsCard, OrderCard, ImageUpload, RichTextEditor**

**复用方式**:
1. **直接使用** - 功能完全满足需求
2. **包装扩展** - 在共享组件基础上添加门户特定逻辑
3. **新建组件** - 仅当共享层无法满足时

### 7.2 组件分类

| 类型 | 说明 | 示例 | 来源 |
|------|------|------|------|
| **页面组件** | 路由组件 | Home, GoodsList | portal/pages |
| **容器组件** | 业务逻辑 | GoodsListContainer | portal/components |
| **共享组件** | 通用组件 | GoodsCard, Button | @campus/shared |
| **布局组件** | 页面布局 | Header, Footer | portal/components/Layout |

### 7.3 组件示例：使用共享组件

```typescript
// ✅ 推荐：直接使用共享层的GoodsCard
import { GoodsCard } from '@campus/shared/components';
import type { Goods } from '@campus/shared/api/models';

function GoodsList({ goods }: { goods: Goods[] }) {
  return (
    <div className="grid grid-cols-4 gap-4">
      {goods.map(item => (
        <GoodsCard 
          key={item.id}
          data={item}
          onClick={(id) => navigate(`/goods/${id}`)}
        />
      ))}
    </div>
  );
}
```

### 7.4 组件示例：包装扩展（如需门户特定逻辑）

```typescript
// components/Goods/PortalGoodsCard.tsx
// 如果共享组件不满足需求，可以包装扩展
import { GoodsCard } from '@campus/shared/components';
import type { Goods } from '@campus/shared/api/models';
import { useNavigate } from 'react-router-dom';

interface PortalGoodsCardProps {
  data: Goods;
  showCampus?: boolean; // 门户特定：显示校区
}

export function PortalGoodsCard({ data, showCampus }: PortalGoodsCardProps) {
  const navigate = useNavigate();

  return (
    <div className="relative">
      <GoodsCard 
        data={data}
        onClick={(id) => navigate(`/goods/${id}`)}
      />
      {/* 门户特定：显示校区标签 */}
      {showCampus && data.campus && (
        <div className="absolute top-2 right-2 bg-blue-500 text-white px-2 py-1 rounded text-xs">
          {data.campus.name}
        </div>
      )}
    </div>
  );
}
```

**注意**: 如果共享层的GoodsCard完全满足需求，直接使用即可，无需包装！

### 7.5 实战案例：Home页面模块化重构 ✅ 已完成

**背景**: 原Home页面574行，职责过多，难以维护。

**重构方案**: 拆分为3个独立子组件，充分利用共享层

#### 组件架构
```
Home (43行 - 组合器)
├── Hero (194行)           # 轮播图、搜索、快捷入口
├── HotGoods (120行)       # 热门商品列表
└── Categories (93行)      # 分类导航
```

#### 1. Home主组件（组合器模式）

```typescript
// pages/Home/index.tsx (43行)
import React from 'react';
import Hero from './Hero';
import HotGoods from './HotGoods';
import Categories from './Categories';
import './Home.css';
import './Hero.css';
import './HotGoods.css';
import './Categories.css';

const Home: React.FC = () => {
  return (
    <div className="home-page">
      <Hero />
      <div className="home-container">
        <Categories />
        <HotGoods />
      </div>
    </div>
  );
};

export default Home;
```

**设计亮点**:
- ✅ **单一职责**: Home只负责组合，不含业务逻辑
- ✅ **模块解耦**: 各子组件独立开发、测试、维护
- ✅ **代码精简**: 从574行 → 43行（减少92%）

#### 2. Hero组件（轮播图+搜索）

```typescript
// pages/Home/Hero.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input } from '@campus/shared/components';  // ✅ 使用共享组件

export const Hero: React.FC = () => {
  const navigate = useNavigate();
  const [currentIndex, setCurrentIndex] = useState(0);
  const [searchKeyword, setSearchKeyword] = useState('');

  // 自动轮播（5秒间隔）
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % CAROUSEL_DATA.length);
    }, 5000);
    return () => clearInterval(timer);
  }, []);

  const handleSearch = () => {
    if (searchKeyword.trim()) {
      navigate(`/search?keyword=${encodeURIComponent(searchKeyword.trim())}`);
    }
  };

  return (
    <section className="hero">
      {/* 轮播图背景 */}
      <div className="hero__carousel">
        {/* 轮播内容、箭头、指示器 */}
        
        {/* ✅ 使用共享层Input组件 */}
        <Input
          size="large"
          placeholder="搜索你想要的商品..."
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          onPressEnter={handleSearch}
          prefix={<span className="hero__search-icon">🔍</span>}
          allowClear
        />
        
        {/* 热门搜索、快捷入口 */}
      </div>
    </section>
  );
};
```

**设计亮点**:
- ✅ **响应式设计**: Desktop/Tablet/Mobile自适应
- ✅ **用户体验**: 自动轮播、键盘快捷键、Loading状态
- ✅ **复用共享组件**: Input组件直接使用

#### 3. HotGoods组件（热门商品）

```typescript
// pages/Home/HotGoods.tsx
import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { GoodsCard, Skeleton, Empty } from '@campus/shared/components';  // ✅ 共享组件
import { goodsService } from '@campus/shared/services';  // ✅ 共享服务

export const HotGoods: React.FC = () => {
  const navigate = useNavigate();

  // ✅ 使用goodsService而非直接API调用
  const { data: hotGoods, isLoading, error } = useQuery({
    queryKey: ['goods', 'hot'],
    queryFn: async () => {
      const response = await goodsService.getRecommendGoods(12);
      return response;
    },
    staleTime: 5 * 60 * 1000, // 5分钟缓存
  });

  return (
    <section className="hot-goods">
      <div className="hot-goods__header">
        <h2>🔥 热门商品</h2>
      </div>

      {/* ✅ 使用共享层Skeleton组件 */}
      {isLoading && (
        <div className="hot-goods__grid">
          {Array.from({ length: 12 }).map((_, i) => (
            <Skeleton key={i} type="card" animation="wave" />
          ))}
        </div>
      )}

      {/* ✅ 使用共享层Empty组件 */}
      {error && (
        <Empty
          icon="❌"
          title="加载失败"
          description="无法加载热门商品，请稍后重试"
        />
      )}

      {/* ✅ 使用共享层GoodsCard组件 */}
      {!isLoading && !error && hotGoods && (
        <div className="hot-goods__grid">
          {hotGoods.map((goods) => (
            <GoodsCard
              key={goods.id}
              goods={transformGoodsData(goods)}
              onCardClick={(data) => navigate(`/goods/${data.id}`)}
              showSeller={true}
              showTags={true}
              hoverable={true}
            />
          ))}
        </div>
      )}
    </section>
  );
};
```

**设计亮点**:
- ✅ **充分复用**: GoodsCard、Skeleton、Empty全部来自共享层
- ✅ **数据缓存**: React Query自动缓存，减少API调用
- ✅ **用户体验**: Loading骨架屏、错误提示、空状态

#### 4. Categories组件（分类导航）

```typescript
// pages/Home/Categories.tsx
import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Skeleton, Empty } from '@campus/shared/components';  // ✅ 共享组件
import { goodsService } from '@campus/shared/services';  // ✅ 共享服务

export const Categories: React.FC = () => {
  const navigate = useNavigate();

  // ✅ 使用goodsService.getCategoryTree
  const { data: categories, isLoading } = useQuery({
    queryKey: ['categories', 'tree'],
    queryFn: async () => {
      const response = await goodsService.getCategoryTree();
      return response;
    },
    staleTime: 30 * 60 * 1000, // 30分钟缓存（分类不常变）
  });

  const handleCategoryClick = (id: number, name: string) => {
    navigate(`/goods?categoryId=${id}&categoryName=${encodeURIComponent(name)}`);
  };

  return (
    <section className="categories">
      {/* 分类图标网格 */}
      <div className="categories__grid">
        {categories?.map((category) => (
          <button
            key={category.id}
            className="categories__item"
            onClick={() => handleCategoryClick(category.id!, category.name!)}
          >
            <div className="categories__item-icon">
              {CATEGORY_ICONS[category.name!] || '📦'}
            </div>
            <div className="categories__item-name">{category.name}</div>
          </button>
        ))}
      </div>
    </section>
  );
};
```

**设计亮点**:
- ✅ **长缓存策略**: 分类数据30分钟缓存（不常变）
- ✅ **响应式布局**: 8列 → 4列 → 4列自适应
- ✅ **图标映射**: 分类名称自动匹配emoji图标

#### 重构成果总结

| 指标 | 重构前 | 重构后 | 改进 |
|------|--------|--------|------|
| **代码行数** | 574行 | 43行 | ↓ 92% |
| **组件数量** | 1个巨型组件 | 4个小组件 | 模块化 |
| **共享层复用** | 部分 | 完全 | 100% |
| **可维护性** | ⭐⭐ | ⭐⭐⭐⭐⭐ | 显著提升 |
| **可测试性** | ⭐⭐ | ⭐⭐⭐⭐⭐ | 独立测试 |

**技术亮点**:
1. ✅ **充分利用共享层**: goodsService、GoodsCard、Skeleton、Empty、Input
2. ✅ **React Query缓存**: 5分钟（商品）、30分钟（分类）
3. ✅ **响应式设计**: 完美适配Desktop、Tablet、Mobile
4. ✅ **单一职责**: 每个组件只做一件事
5. ✅ **零模拟数据**: 遵循TDD原则，真实API调用

**文件结构**:
```
pages/Home/
├── index.tsx          # 43行 - 组合器
├── Hero.tsx           # 194行 - 轮播图+搜索
├── HotGoods.tsx       # 120行 - 热门商品
├── Categories.tsx     # 93行 - 分类导航
├── Home.css           # 主容器样式
├── Hero.css           # 327行 - 轮播图样式
├── HotGoods.css       # 84行 - 商品网格样式
└── Categories.css     # 120行 - 分类网格样式
```

**Git提交**: 
- Commit: `40562b7`
- Message: `refactor(portal): 重构Home页面为模块化组件架构`

### 7.6 组件示例：Loading

```typescript
// components/Common/Loading.tsx
interface LoadingProps {
  size?: 'small' | 'medium' | 'large';
  text?: string;
}

export default function Loading({ size = 'medium', text }: LoadingProps) {
  const sizeClasses = {
    small: 'w-4 h-4',
    medium: 'w-8 h-8',
    large: 'w-12 h-12'
  };

  return (
    <div className="flex flex-col items-center justify-center p-8">
      <div
        className={`
          ${sizeClasses[size]}
          border-4 border-gray-200 border-t-blue-500
          rounded-full animate-spin
        `}
      />
      {text && <p className="mt-4 text-gray-600">{text}</p>}
    </div>
  );
}
```

---

## 8. 性能优化

### 8.1 代码分割

```typescript
// 路由懒加载
const Home = lazy(() => import('../pages/Home'));

// 组件懒加载
const HeavyComponent = lazy(() => import('./HeavyComponent'));

// 使用
<Suspense fallback={<Loading />}>
  <HeavyComponent />
</Suspense>
```

### 8.2 图片懒加载

```typescript
// hooks/useImageLazyLoad.ts
import { useEffect, useRef } from 'react';

export function useImageLazyLoad() {
  const imgRef = useRef<HTMLImageElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const img = entry.target as HTMLImageElement;
            const src = img.dataset.src;
            if (src) {
              img.src = src;
              observer.unobserve(img);
            }
          }
        });
      },
      { rootMargin: '50px' }
    );

    if (imgRef.current) {
      observer.observe(imgRef.current);
    }

    return () => observer.disconnect();
  }, []);

  return imgRef;
}
```

### 8.3 虚拟滚动

```typescript
// 长列表使用react-window
import { FixedSizeList } from 'react-window';

function GoodsList({ items }) {
  const Row = ({ index, style }) => (
    <div style={style}>
      <GoodsCard data={items[index]} />
    </div>
  );

  return (
    <FixedSizeList
      height={600}
      itemCount={items.length}
      itemSize={300}
      width="100%"
    >
      {Row}
    </FixedSizeList>
  );
}
```

### 8.4 缓存策略

```typescript
// React Query缓存配置
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5分钟内缓存有效
      gcTime: 10 * 60 * 1000, // 10分钟后垃圾回收
      retry: 1, // 失败重试1次
      refetchOnWindowFocus: false // 窗口聚焦不自动刷新
    }
  }
});
```

---

## 9. 安全设计

### 9.1 XSS防护

```typescript
// 使用DOMPurify过滤用户输入
import DOMPurify from 'dompurify';

function SafeHTML({ html }: { html: string }) {
  const clean = DOMPurify.sanitize(html);
  return <div dangerouslySetInnerHTML={{ __html: clean }} />;
}
```

### 9.2 CSRF防护

```typescript
// Axios自动添加CSRF Token
apiClient.interceptors.request.use((config) => {
  const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
  if (csrfToken) {
    config.headers['X-CSRF-Token'] = csrfToken;
  }
  return config;
});
```

### 9.3 敏感信息脱敏

```typescript
// utils/format.ts
export function maskPhone(phone: string): string {
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
}

export function maskIdCard(idCard: string): string {
  return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2');
}
```

---

## 10. 测试策略

### 10.1 单元测试

```typescript
// __tests__/components/GoodsCard.test.tsx
import { render, screen } from '@testing-library/react';
import { GoodsCard } from '../components/Goods/GoodsCard';

describe('GoodsCard', () => {
  const mockGoods = {
    id: 1,
    title: '测试商品',
    price: 99.99,
    coverImage: '/test.jpg',
    viewCount: 100,
    favoriteCount: 10
  };

  it('renders goods information correctly', () => {
    render(<GoodsCard data={mockGoods} />);
    
    expect(screen.getByText('测试商品')).toBeInTheDocument();
    expect(screen.getByText('¥99.99')).toBeInTheDocument();
    expect(screen.getByText('👁 100')).toBeInTheDocument();
  });
});
```

### 10.2 集成测试

```typescript
// __tests__/pages/GoodsList.test.tsx
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import GoodsList from '../pages/Goods/List';

describe('GoodsList', () => {
  const queryClient = new QueryClient();

  it('fetches and displays goods list', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <GoodsList />
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(screen.getByText(/商品列表/i)).toBeInTheDocument();
    });
  });
});
```

---

## 11. 部署配置

### 11.1 环境变量

```bash
# .env.production
VITE_API_BASE_URL=https://api.campus-marketplace.com
VITE_WS_URL=wss://api.campus-marketplace.com/ws
VITE_STATIC_BASE_URL=https://cdn.campus-marketplace.com
VITE_PORTAL_PORT=8220
```

### 11.2 Nginx配置

```nginx
server {
    listen 80;
    server_name campus-marketplace.com;
    
    # 前端静态资源
    location / {
        root /var/www/portal/dist;
        try_files $uri $uri/ /index.html;
        
        # 缓存策略
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # API代理
    location /api/ {
        proxy_pass http://localhost:8200;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    # WebSocket代理
    location /ws {
        proxy_pass http://localhost:8200;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

---

## 📋 总结

### 设计亮点

1. ✅ **完善的类型安全**: TypeScript + 自动生成API类型
2. ✅ **高效的状态管理**: Zustand + React Query
3. ✅ **优秀的性能**: 代码分割 + 懒加载 + 缓存
4. ✅ **清晰的架构**: 分层设计 + 模块化
5. ✅ **完善的测试**: 单元测试 + 集成测试

### 下一步

- [ ] 审批通过后进入实现阶段
- [ ] 参考 [tasks-frontend.md](./tasks-frontend.md) 执行开发任务
- [ ] 遵循 [TDD十步流程](../开发工作流规范.md) 开发

---

**最后更新**: 2025-11-05  
**维护者**: BaSui 😎  
**状态**: 🏗️ 待审批
