# 🎨 前端架构全解析

> **作者**: BaSui 😎  
> **更新**: 2025-11-05  
> **状态**: ✅ 已完成

---

## 📋 目录

- [Monorepo 架构](#monorepo-架构)
- [共享层 (@campus/shared)](#共享层-campusshared)
- [API 自动化工作流](#api-自动化工作流)
- [前端开发路线图](#前端开发路线图)
- [最佳实践](#最佳实践)

---

## 🏗️ Monorepo 架构

### 项目结构

```
frontend/
├── packages/
│   ├── admin/              # 📊 管理端（Ant Design）
│   │   ├── src/
│   │   │   ├── pages/     # 管理页面
│   │   │   ├── components/# 管理组件
│   │   │   ├── store/     # Jotai状态
│   │   │   └── router/    # 路由配置
│   │   └── package.json
│   │
│   ├── portal/             # 🏪 用户端（Tailwind CSS）
│   │   ├── src/
│   │   │   ├── pages/     # 用户页面
│   │   │   ├── components/# 用户组件
│   │   │   ├── store/     # Zustand状态
│   │   │   └── router/    # 路由配置
│   │   └── package.json
│   │
│   └── shared/             # 🔗 共享层
│       ├── src/
│       │   ├── api/       # 🤖 自动生成的API（252个models）
│       │   ├── services/  # 🔧 业务服务层
│       │   ├── utils/     # 🛠️ 工具函数
│       │   ├── types/     # 📝 TypeScript类型
│       │   ├── hooks/     # 🪝 共享Hooks
│       │   ├── components/# 🧩 共享组件
│       │   └── constants/ # 📌 常量定义
│       └── package.json
│
├── package.json            # 根配置
├── pnpm-workspace.yaml     # pnpm workspace
└── tsconfig.json           # TypeScript配置
```

### 依赖关系

```
┌─────────────┐      ┌─────────────┐
│   admin     │      │   portal    │
│  (管理端)   │      │  (用户端)   │
└──────┬──────┘      └──────┬──────┘
       │                    │
       │  依赖               │  依赖
       │                    │
       ▼                    ▼
   ┌─────────────────────────────┐
   │        @campus/shared       │
   │         (共享层)             │
   │  api/ services/ utils/      │
   │  hooks/ components/         │
   └─────────────────────────────┘
```

**核心原则**：
- ✅ admin 和 portal **完全隔离**，零依赖
- ✅ 共享代码**必须**放 shared
- ✅ shared **不能**依赖 admin/portal

---

## 🔗 共享层 (@campus/shared)

### 什么是共享层？

共享层是一个**独立的npm包**，被admin和portal同时依赖。它包含：

1. **自动生成的API客户端**（252个TypeScript类型）
2. **业务服务层**（二次封装API）
3. **工具函数**（http、storage、format等）
4. **共享组件**（Loading、Button、Input等）
5. **共享Hooks**（useAuth、useDebounce等）
6. **TypeScript类型**（业务类型定义）
7. **常量定义**（API_URL、TOKEN_KEY等）

### 共享层目录结构

```
packages/shared/src/
├── api/                           # 🤖 自动生成（OpenAPI Generator）
│   ├── api/                      # API接口方法
│   │   ├── default-api.ts        # 主API（31244行）
│   │   └── dispute-statistics-api.ts
│   ├── models/                   # TypeScript类型（252个文件）
│   │   ├── user.ts
│   │   ├── goods.ts
│   │   ├── order.ts
│   │   └── ...
│   ├── base.ts                   # Axios基础配置
│   ├── common.ts                 # 工具函数
│   ├── configuration.ts          # API配置
│   ├── index.ts                  # 统一导出
│   └── README.md                 # API使用文档
│
├── services/                      # 🔧 业务服务层（手写）
│   ├── auth.ts                   # 认证服务
│   ├── user.ts                   # 用户服务
│   ├── goods.ts                  # 商品服务
│   ├── order.ts                  # 订单服务
│   ├── revert.ts                 # 撤销服务
│   └── index.ts                  # 统一导出
│
├── utils/                         # 🛠️ 工具函数（手写）
│   ├── apiClient.ts              # Axios实例 + 拦截器
│   ├── http.ts                   # HTTP封装
│   ├── storage.ts                # LocalStorage封装
│   ├── format.ts                 # 格式化工具
│   ├── validator.ts              # 表单验证
│   ├── websocket.ts              # WebSocket封装
│   └── index.ts                  # 统一导出
│
├── components/                    # 🧩 共享组件（手写）
│   ├── Loading/
│   ├── Button/
│   ├── Input/
│   └── index.ts
│
├── hooks/                         # 🪝 共享Hooks（手写）
│   ├── useAuth.ts
│   ├── useDebounce.ts
│   └── index.ts
│
├── types/                         # 📝 TypeScript类型（手写）
│   ├── common.ts
│   ├── revert.ts
│   └── index.ts
│
├── constants/                     # 📌 常量定义（手写）
│   ├── api.ts                    # API常量
│   ├── storage.ts                # Storage Key
│   └── index.ts
│
└── index.ts                       # 统一导出
```

### 共享层的作用

#### 1. **API自动化** 🤖

```typescript
// ✅ 自动生成，带完整类型提示
import { DefaultApi, User, Goods } from '@campus/shared/api';

const api = new DefaultApi();

// 类型安全的API调用
const response = await api.getUser({ id: 1 });
const user: User = response.data.data; // 完整类型推导
```

#### 2. **业务服务层** 🔧

```typescript
// ✅ 二次封装API，添加业务逻辑
import { authService, goodsService } from '@campus/shared/services';

// 简化的登录调用
const result = await authService.login({
  username: 'test',
  password: '123456'
});

// 简化的商品列表
const goods = await goodsService.getList({
  page: 0,
  size: 20,
  campusId: 1
});
```

#### 3. **工具函数复用** 🛠️

```typescript
// ✅ 统一的工具函数
import { storage, format, validator } from '@campus/shared/utils';

// LocalStorage封装
storage.setItem('token', 'xxx');
const token = storage.getItem('token');

// 格式化工具
const date = format.formatDate(new Date());
const price = format.formatPrice(9999);

// 表单验证
const isValid = validator.validateEmail('test@example.com');
```

#### 4. **共享组件** 🧩

```typescript
// ✅ 两端通用的组件
import { Loading, Button, Input } from '@campus/shared/components';

<Loading size="large" />
<Button type="primary" onClick={handleClick}>点击</Button>
<Input placeholder="请输入" value={value} onChange={handleChange} />
```

#### 5. **共享Hooks** 🪝

```typescript
// ✅ 通用的React Hooks
import { useAuth, useDebounce } from '@campus/shared/hooks';

const { user, login, logout } = useAuth();
const debouncedValue = useDebounce(searchValue, 500);
```

---

## 🤖 API 自动化工作流

### 完整流程图

```
┌─────────────────────────────────────────────────────────────┐
│  第1步：后端开发                                             │
│  ↓ 创建/修改 Controller                                     │
│  ↓ 添加 @Operation 注解（Swagger）                         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  第2步：监听器自动检测                                       │
│  ↓ watch-api.js 监听 *Controller*.java 变更                │
│  ↓ 防抖2秒后触发                                            │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  第3步：OpenAPI导出                                          │
│  ↓ 启动后端应用                                             │
│  ↓ 访问 /v3/api-docs?group=前台接口                        │
│  ↓ 生成 openapi-frontend.json（379KB）                     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  第4步：TypeScript代码生成                                   │
│  ↓ openapi-generator-maven-plugin                          │
│  ↓ 输出到 frontend/packages/shared/src/api/                │
│  ↓ 生成 252个 models + 2个 apis                            │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  第5步：前端直接使用                                         │
│  ↓ import { DefaultApi } from '@campus/shared/api'         │
│  ↓ 类型安全的API调用                                        │
│  ↓ IDE自动补全 + 错误检查                                   │
└─────────────────────────────────────────────────────────────┘
```

### 使用方式

#### **方式1：自动监听（开发推荐）** ⭐

```bash
# 终端1：启动监听器
cd frontend
pnpm api:watch

# 终端2：启动后端
cd backend
mvn spring-boot:run

# 终端3：启动前端
cd frontend
pnpm dev:portal
```

**效果**：修改任何Controller，2秒后自动重新生成前端API！

#### **方式2：手动生成**

```bash
cd frontend
pnpm api:generate
```

**适用场景**：
- 首次克隆项目
- 长时间未更新API
- 需要一次性同步所有API

### 生成的文件

```
frontend/packages/shared/src/api/
├── api/
│   ├── default-api.ts           # 31244行，包含所有API方法
│   └── dispute-statistics-api.ts
├── models/
│   ├── user.ts                  # User类型定义
│   ├── goods.ts                 # Goods类型定义
│   ├── order.ts                 # Order类型定义
│   ├── create-goods-request.ts  # 创建商品请求
│   ├── update-goods-request.ts  # 更新商品请求
│   └── ...（252个类型文件）
├── base.ts                       # Axios基础配置
├── common.ts                     # 通用工具
├── configuration.ts              # API配置
└── index.ts                      # 统一导出
```

### 前端使用示例

#### **基础使用**

```typescript
import { DefaultApi, Configuration } from '@campus/shared/api';

// 创建API实例
const config = new Configuration({
  basePath: 'http://localhost:8200/api',
  accessToken: localStorage.getItem('token') || undefined
});

const api = new DefaultApi(config);

// 调用API
const response = await api.listGoods({
  page: 0,
  size: 20,
  campusId: 1
});

console.log(response.data.data); // 商品列表
```

#### **配合React Query（推荐）**

```typescript
import { useQuery } from '@tanstack/react-query';
import { DefaultApi } from '@campus/shared/api';

const api = new DefaultApi();

function useGoodsList(campusId: number) {
  return useQuery({
    queryKey: ['goods', 'list', campusId],
    queryFn: () => api.listGoods({ campusId, page: 0, size: 20 })
  });
}

// 在组件中使用
function GoodsList() {
  const { data, isLoading } = useGoodsList(1);
  
  if (isLoading) return <Loading />;
  
  return (
    <div>
      {data?.data.data?.map(goods => (
        <GoodsCard key={goods.id} data={goods} />
      ))}
    </div>
  );
}
```

---

## 🚀 前端开发路线图

### 阶段1：基础功能开发（当前）✅

**已完成**：
- ✅ 项目脚手架搭建（Monorepo + Vite）
- ✅ 路由配置（React Router v6）
- ✅ 状态管理（Zustand + 持久化）
- ✅ 登录重定向功能
- ✅ API自动化工作流
- ✅ 共享层架构

**进行中**：
- 🚧 门户首页开发
- 🚧 商品列表页面
- 🚧 商品详情页面

### 阶段2：核心功能开发

#### **2.1 商品模块** 🛍️

```
pages/
├── Home/                    # 首页
│   ├── Hero.tsx            # 轮播图
│   ├── Categories.tsx      # 分类导航
│   ├── HotGoods.tsx        # 热门商品
│   └── index.tsx
├── GoodsList/               # 商品列表
│   ├── FilterBar.tsx       # 筛选栏
│   ├── SortBar.tsx         # 排序栏
│   ├── GoodsGrid.tsx       # 商品网格
│   └── index.tsx
├── GoodsDetail/             # 商品详情
│   ├── ImageGallery.tsx    # 图片画廊
│   ├── GoodsInfo.tsx       # 商品信息
│   ├── SellerCard.tsx      # 卖家信息
│   └── index.tsx
└── Publish/                 # 发布商品
    ├── UploadImages.tsx    # 图片上传
    ├── BasicInfo.tsx       # 基本信息
    └── index.tsx
```

#### **2.2 用户模块** 👤

```
pages/
├── Profile/                 # 个人中心
│   ├── ProfileCard.tsx     # 用户卡片
│   ├── MyGoods.tsx         # 我的商品
│   ├── MySales.tsx         # 我的卖出
│   └── index.tsx
├── Settings/                # 设置
│   ├── AccountSetting.tsx  # 账号设置
│   ├── SecuritySetting.tsx # 安全设置
│   └── index.tsx
└── UserProfile/             # 他人主页
    ├── UserInfo.tsx        # 用户信息
    ├── UserGoods.tsx       # 用户商品
    └── index.tsx
```

#### **2.3 订单模块** 📦

```
pages/
├── Orders/                  # 订单列表
│   ├── OrderCard.tsx       # 订单卡片
│   ├── OrderFilter.tsx     # 订单筛选
│   └── index.tsx
└── OrderDetail/             # 订单详情
    ├── OrderInfo.tsx       # 订单信息
    ├── LogisticsTrack.tsx  # 物流追踪
    └── index.tsx
```

#### **2.4 社区模块** 💬

```
pages/
├── Community/               # 社区首页
│   ├── PostList.tsx        # 帖子列表
│   ├── HotTopics.tsx       # 热门话题
│   └── index.tsx
└── PostDetail/              # 帖子详情
    ├── PostContent.tsx     # 帖子内容
    ├── CommentList.tsx     # 评论列表
    └── index.tsx
```

### 阶段3：高级功能开发

- 🔔 实时通知（WebSocket）
- 💬 即时聊天（WebSocket）
- 📊 数据分析（ECharts）
- 🔍 搜索优化（防抖 + 搜索建议）
- 📱 响应式优化（移动端适配）

### 阶段4：性能优化

- ⚡ 代码分割（React.lazy）
- 🖼️ 图片懒加载
- 📦 虚拟滚动（长列表）
- 🔄 缓存策略（React Query配置）
- 🚀 SEO优化（SSR/SSG）

---

## 📝 最佳实践

### 1. **组件开发**

```typescript
// ✅ 正确：使用TypeScript类型
interface GoodsCardProps {
  data: Goods;
  onClick?: (id: number) => void;
}

export const GoodsCard: React.FC<GoodsCardProps> = ({ data, onClick }) => {
  return (
    <div onClick={() => onClick?.(data.id)}>
      <img src={data.coverImage} alt={data.title} />
      <h3>{data.title}</h3>
      <p>{format.formatPrice(data.price)}</p>
    </div>
  );
};
```

### 2. **状态管理**

```typescript
// ✅ 正确：使用Zustand + 持久化
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface CartState {
  items: CartItem[];
  addItem: (item: CartItem) => void;
  removeItem: (id: number) => void;
}

export const useCartStore = create<CartState>()(
  persist(
    (set) => ({
      items: [],
      addItem: (item) => set((state) => ({
        items: [...state.items, item]
      })),
      removeItem: (id) => set((state) => ({
        items: state.items.filter(item => item.id !== id)
      }))
    }),
    { name: 'cart-storage' }
  )
);
```

### 3. **API调用**

```typescript
// ✅ 正确：使用React Query
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { goodsService } from '@campus/shared/services';

// 查询
function useGoodsList(params: GoodsListParams) {
  return useQuery({
    queryKey: ['goods', 'list', params],
    queryFn: () => goodsService.getList(params)
  });
}

// 变更
function useCreateGoods() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: goodsService.create,
    onSuccess: () => {
      // 刷新列表
      queryClient.invalidateQueries({ queryKey: ['goods', 'list'] });
    }
  });
}
```

### 4. **路由配置**

```typescript
// ✅ 正确：使用懒加载 + 路由守卫
import { lazy, Suspense } from 'react';
import { Navigate } from 'react-router-dom';

const GoodsDetail = lazy(() => import('../pages/GoodsDetail'));

const routes = [
  {
    path: '/goods/:id',
    element: (
      <Suspense fallback={<Loading />}>
        <GoodsDetail />
      </Suspense>
    )
  },
  {
    path: '/publish',
    element: (
      <RequireAuth>
        <Suspense fallback={<Loading />}>
          <Publish />
        </Suspense>
      </RequireAuth>
    )
  }
];
```

---

## 🎉 总结

### 核心优势

1. **API自动化** 🤖
   - OpenAPI → TypeScript 零手写
   - 类型安全，零运行时错误
   - 监听器自动同步

2. **共享层架构** 🔗
   - 代码复用，减少重复
   - 统一工具函数和组件
   - admin/portal完全隔离

3. **Monorepo管理** 📦
   - pnpm workspace
   - 统一依赖版本
   - 独立构建部署

4. **现代化技术栈** 🚀
   - React 18 + TypeScript 5
   - Vite 5 + pnpm 8
   - React Query + Zustand

---

**最后更新**: 2025-11-05  
**维护者**: BaSui 😎  
**状态**: ✅ 生产就绪
