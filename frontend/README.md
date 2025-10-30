# 校园轻享集市系统 - 前端 Monorepo 🎨

> **架构**: React 18 + TypeScript + Vite + pnpm Workspace
> **作者**: BaSui 😎
> **创建日期**: 2025-10-29
> **状态**: 🚧 开发中

---

## 📋 项目结构

```
frontend/
├── packages/
│   ├── shared/              # 🔧 公共层（核心复用层）
│   │   ├── src/
│   │   │   ├── api/         # 🤖 OpenAPI 自动生成的 API 客户端
│   │   │   ├── components/  # 📦 公共 React 组件库
│   │   │   ├── utils/       # 🛠️ 工具函数（format, validator, storage...）
│   │   │   ├── types/       # 📝 TypeScript 类型定义
│   │   │   ├── constants/   # 🔢 常量定义
│   │   │   ├── hooks/       # 🎣 自定义 React Hooks
│   │   │   └── index.ts     # 导出入口
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   └── tsup.config.ts   # 构建配置（ESM + CJS）
│   │
│   ├── admin/               # 📊 管理端（PC Web 后台）
│   │   └── 🚧 待初始化
│   │
│   └── portal/              # 🛍️ 用户端（响应式 Web）
│       └── 🚧 待初始化
│
├── pnpm-workspace.yaml      # pnpm Workspace 配置
├── package.json             # 根 package.json
└── README.md                # 本文件
```

---

## 🚀 快速开始

### 1. 安装依赖

```bash
# 安装 pnpm（如果还没安装）
npm install -g pnpm

# 安装所有依赖
pnpm install
```

### 2. 生成 API 客户端

```bash
# 方式 1：使用 pnpm 脚本（推荐）
pnpm run api:generate

# 方式 2：手动执行后端命令
cd backend
mvn clean
mvn -Dspring-boot.run.arguments="--openapi.export.enabled=true,--openapi.export.path=target/openapi-frontend.json" spring-boot:run
mvn -P openapi openapi-generator:generate
```

**生成的 API 文件位置**：`packages/shared/src/api/`

### 3. 开发调试

```bash
# 开发管理端
pnpm run dev:admin

# 开发用户端
pnpm run dev:portal
```

### 4. 构建打包

```bash
# 构建所有项目
pnpm run build:all

# 构建单个项目
pnpm run build:admin
pnpm run build:portal
pnpm run build:shared
```

---

## 🎯 技术栈

### 🔧 公共层（@campus/shared）

| 技术 | 版本 | 用途 |
|-----|------|------|
| TypeScript | ^5.4.5 | 类型安全 |
| Axios | ^1.7.2 | HTTP 客户端 |
| tsup | ^8.1.0 | 构建工具（ESM + CJS） |
| React | ^18.3.1 | UI 组件库基础 |

### 📊 管理端（@campus/admin）

| 技术 | 版本 | 用途 |
|-----|------|------|
| React | ^18.3.1 | UI 框架 |
| Ant Design | ^5.x | UI 组件库 |
| Vite | ^5.x | 构建工具 |
| React Router | ^6.x | 路由管理 |
| Zustand | 最新 | 状态管理 |
| React Query | ^5.x | 服务端状态管理 |

### 🛍️ 用户端（@campus/portal）

| 技术 | 版本 | 用途 |
|-----|------|------|
| React | ^18.3.1 | UI 框架 |
| Tailwind CSS | ^3.x | 样式框架 |
| Vite | ^5.x | 构建工具 |
| React Router | ^6.x | 路由管理 |
| Zustand | 最新 | 状态管理 |
| React Query | ^5.x | 服务端状态管理 |

---

## 📦 Monorepo 架构优势

### 1️⃣ 代码复用率高
- ✅ 公共组件只写一次，管理端和用户端共享
- ✅ API 接口定义统一，类型安全
- ✅ 工具函数不重复造轮子

### 2️⃣ 类型安全
- ✅ TypeScript 类型定义集中管理
- ✅ 接口变更时，所有端同步提示错误
- ✅ 减少前后端联调问题

### 3️⃣ 开发效率高
- ✅ 修改公共组件，所有端同步生效
- ✅ 可独立开发各端，互不影响
- ✅ 支持增量构建（Turborepo）

### 4️⃣ 维护成本低
- ✅ 依赖版本集中管理
- ✅ 升级库时一次性更新
- ✅ 避免版本冲突

---

## 🛠️ 开发规范

### API 客户端生成规范

**🚫 禁止手动修改生成的 API 文件！**

- ✅ 所有 API 接口由后端 OpenAPI 文档自动生成
- ✅ 后端接口变更后，运行 `pnpm run api:generate` 重新生成
- ✅ 如需封装 API，请在 `packages/shared/src/utils/` 中创建

### 公共组件开发规范

**位置**：`packages/shared/src/components/`

**命名规范**：
```tsx
// ✅ 推荐：大驼峰命名
export const Button = () => { ... };

// ❌ 错误：小驼峰或其他命名
export const button = () => { ... };
```

**导出规范**：
```tsx
// components/Button/index.tsx
export { Button } from './Button';

// components/index.ts
export { Button } from './Button';
```

### 类型定义规范

**位置**：`packages/shared/src/types/`

**命名规范**：
```typescript
// ✅ 推荐：使用 type 或 interface
export type User = { ... };
export interface GoodsDTO { ... };

// ✅ 推荐：枚举使用大驼峰
export enum OrderStatus { ... }
```

---

## 📚 脚本命令说明

### 开发命令

```bash
# 开发管理端（启动 dev server）
pnpm run dev:admin

# 开发用户端（启动 dev server）
pnpm run dev:portal
```

### 构建命令

```bash
# 构建所有项目
pnpm run build:all

# 构建单个项目
pnpm run build:admin
pnpm run build:portal
pnpm run build:shared
```

### 代码质量

```bash
# 代码检查（ESLint）
pnpm run lint

# 类型检查（TypeScript）
pnpm run type-check

# 代码格式化（Prettier）
pnpm run format

# 检查代码格式
pnpm run format:check

# 清理所有构建产物
pnpm run clean
```

### API 生成

```bash
# 重新生成 API 客户端
pnpm run api:generate
```

### Git 钩子

项目已配置 Husky + lint-staged，在提交代码时会自动：
- ✅ 格式化代码（Prettier）
- ✅ 检查代码规范（ESLint）

```bash
# 提交代码会自动触发
git add .
git commit -m "feat: 新功能"
```

---

## 🚧 开发计划

### ✅ 已完成

- [x] 创建 Monorepo 项目结构（pnpm workspace）
- [x] 配置公共层 @campus/shared 基础框架
- [x] 更新后端 pom.xml 的 API 生成路径
- [x] 搭建公共层目录结构（api、components、utils、types...）
- [x] 重新生成 API 到公共层（130+ TypeScript 文件）
- [x] 配置根目录统一环境变量管理（.env）
- [x] 更新 Vite 配置读取根目录环境变量
- [x] 完善 Axios 封装（拦截器 + JWT + 自动刷新）
- [x] 完善公共层工具函数（format、validator、storage）
- [x] 完善公共层类型定义（通用类型、枚举）
- [x] 配置 Tailwind CSS（Portal 端）
- [x] 配置 Prettier + Husky + lint-staged
- [x] 初始化管理端 @campus/admin 项目
- [x] 初始化用户端 @campus/portal 项目

### 🚀 进行中

- [ ] 公共组件库开发（Button、Form、Table、Modal...）
- [ ] 自定义 Hooks 开发（useAuth、useRequest、useWebSocket...）
- [ ] 管理端功能开发
- [ ] 用户端功能开发

### 📋 待开发

- [ ] 配置 Turborepo 增量构建（可选）
- [ ] 添加单元测试（Jest + React Testing Library）
- [ ] 添加 E2E 测试（Playwright）
- [ ] 性能优化（代码分割、懒加载）
- [ ] SEO 优化（Portal 端）

---

## 🌍 环境变量配置

**位置**：项目根目录 `.env` 文件

项目使用统一的环境变量管理，前端和后端共享同一个 `.env` 文件。

### 前端环境变量（需要 VITE_ 前缀）

```bash
# API 基础 URL（指向后端服务）
VITE_API_BASE_URL=http://localhost:8200/api

# WebSocket URL（实时通讯）
VITE_WS_URL=ws://localhost:8200/ws

# 应用标题
VITE_APP_TITLE_ADMIN=校园轻享集市 - 管理端
VITE_APP_TITLE_PORTAL=校园轻享集市

# 开发服务器端口
VITE_ADMIN_PORT=3000
VITE_PORTAL_PORT=3001
```

### 使用方式

```typescript
// 在代码中访问环境变量
const apiUrl = import.meta.env.VITE_API_BASE_URL;
const wsUrl = import.meta.env.VITE_WS_URL;
const appTitle = import.meta.env.VITE_APP_TITLE_ADMIN;
```

---

## 💡 使用示例

### 1. 调用后端 API

```typescript
import { api } from '@campus/shared';

// 登录
const loginResponse = await api.login({
  loginRequest: {
    username: 'admin',
    password: '123456',
  },
});

// 获取当前用户信息
const userProfile = await api.getCurrentUserProfile();

// 获取物品列表（分页）
const goodsList = await api.listGoods({
  page: 0,
  size: 10,
  sort: 'createdAt,desc',
});

// 创建订单
const order = await api.createOrder({
  createOrderRequest: {
    goodsId: 123,
    paymentMethod: 'WECHAT',
  },
});
```

### 2. 使用工具函数

```typescript
import {
  formatDate,
  formatMoney,
  formatPhone,
  isValidEmail,
  isValidPhone,
  storage,
  tokenStorage,
} from '@campus/shared';

// 格式化日期
const dateStr = formatDate(new Date(), 'YYYY-MM-DD HH:mm:ss');

// 格式化金额
const priceStr = formatMoney(12345.67); // ¥12,345.67

// 格式化手机号（隐藏中间4位）
const phoneStr = formatPhone('13800138000'); // 138****8000

// 验证邮箱
const isValid = isValidEmail('admin@example.com'); // true

// 验证手机号
const isPhoneValid = isValidPhone('13800138000'); // true

// 存储数据（支持过期时间）
storage.set('user', { name: 'BaSui' }, 30); // 30分钟后过期
const user = storage.get('user');

// Token 管理
tokenStorage.setTokens('access_token', 'refresh_token');
const token = tokenStorage.getAccessToken();
```

### 3. 使用类型定义

```typescript
import type {
  PageParams,
  PageResponse,
  UserStatus,
  GoodsStatus,
  OrderStatus,
} from '@campus/shared';

// 分页参数
const params: PageParams = {
  page: 0,
  size: 10,
  sort: 'createdAt',
  direction: 'DESC',
};

// 分页响应
const response: PageResponse<Goods> = await api.listGoods(params);

// 枚举类型
const status: GoodsStatus = GoodsStatus.APPROVED;
```

---

## 🐛 常见问题

### Q1: 为什么选择 pnpm workspace？

**A:**
- ✅ pnpm 磁盘空间效率高，安装速度快
- ✅ workspace 原生支持 Monorepo，配置简单
- ✅ 适合管理多个前端项目

### Q2: 公共层如何被其他项目引用？

**A:**
```json
// packages/admin/package.json
{
  "dependencies": {
    "@campus/shared": "workspace:*"
  }
}
```

```typescript
// packages/admin/src/App.tsx
import { Button } from '@campus/shared/components';
import { authApi } from '@campus/shared/api';
```

### Q3: API 生成失败怎么办？

**A:**
1. 确保后端启动成功
2. 确保后端 `target/openapi-frontend.json` 文件存在
3. 检查 `backend/pom.xml` 的 `<output>` 路径是否正确
4. 查看生成日志，定位错误原因

### Q4: 如何添加新的工具函数？

**A:**
1. 在 `packages/shared/src/utils/` 创建文件（如 `upload.ts`）
2. 编写工具函数并导出
3. 在 `packages/shared/src/utils/index.ts` 中导出
4. 在其他项目中使用：`import { upload } from '@campus/shared';`

### Q5: Husky 钩子不生效怎么办？

**A:**
1. 确保已执行 `pnpm install`（会自动运行 `husky install`）
2. 检查 `.husky/pre-commit` 文件是否存在
3. 在 Windows 上，确保 Git Bash 已安装
4. 如果仍不生效，手动运行：
   ```bash
   cd frontend
   pnpm format
   pnpm lint
   ```

### Q6: Tailwind CSS 样式不生效怎么办？

**A:**
1. 确保 `src/index.css` 中引入了 Tailwind 指令：
   ```css
   @tailwind base;
   @tailwind components;
   @tailwind utilities;
   ```
2. 检查 `tailwind.config.js` 的 `content` 配置是否包含所有组件文件
3. 重启开发服务器：`pnpm run dev:portal`

---

## 🎉 总结

这套 **Monorepo 架构** 为校园轻享集市系统的前端开发提供了：

✅ **代码复用** - 公共层统一管理组件、API、工具
✅ **类型安全** - TypeScript 类型定义集中管理
✅ **开发效率** - 修改公共层，所有端同步生效
✅ **维护简单** - 依赖版本统一管理，避免冲突

**BaSui 提示**：Monorepo 架构初期搭建会稍微复杂一点，但长期来看绝对值得！代码复用、类型安全、统一管理，香得很！😎

---

**让我们一起，用专业的态度写出优雅的代码，用快乐的心情创造美好的产品！💪🚀**

---

**文档维护**：
- 创建时间：2025-10-29
- 最后更新：2025-10-31
- 作者：BaSui 😎
- 状态：✅ 基础架构完成，进入功能开发阶段
