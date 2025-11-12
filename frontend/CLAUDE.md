# 前端开发规范 - AI 上下文文档

> **技术栈**: React 18 + TypeScript + Vite + Monorepo (Turborepo)
> **架构**: Monorepo 多包架构 + OpenAPI 代码生成
> **作者**: BaSui 😎 | **更新**: 2025-11-01

---

---

## 🌍 环境变量管理规范（重要！）

### 📦 统一配置原则

**🎯 核心规则：所有前端包的环境变量统一在项目根目录 `.env` 文件中管理！**

#### **为什么要统一管理？**
- ✅ **避免配置分散**：不同包的配置集中在一处，便于管理
- ✅ **防止冲突**：避免不同包的配置不一致
- ✅ **方便维护**：修改一次配置，所有包生效
- ✅ **版本控制**：`.env.example` 作为模板提交，`.env` 不提交

---

### 📍 配置文件位置

```
项目根目录/
├── .env                # ✅ 实际配置（本地开发，不提交）
├── .env.example        # ✅ 配置模板（提交到 Git）
├── .env.prod.example   # ✅ 生产环境模板（提交到 Git）
│
└── frontend/
    └── packages/
        ├── portal/
        │   ├── vite.config.ts   # ✅ 已配置读取根目录 .env
        │   └── ❌ 禁止创建：.env.development / .env.production
        │
        ├── admin/
        │   ├── vite.config.ts   # ✅ 已配置读取根目录 .env
        │   └── ❌ 禁止创建：.env.development / .env.production
        │
        └── shared/
            └── ❌ 禁止创建任何 .env 文件
```

---

### 🔧 Vite 配置（已完成）

每个包的 `vite.config.ts` 已配置为读取根目录的环境变量：

```typescript
import { defineConfig, loadEnv } from 'vite';
import path from 'path';

export default defineConfig(({ mode }) => {
  // 🎯 从项目根目录加载环境变量（向上三级：packages/portal/ -> packages/ -> frontend/ -> 根目录）
  const env = loadEnv(mode, path.resolve(__dirname, '../../..'), 'VITE_');

  return {
    server: {
      port: parseInt(env.VITE_PORTAL_PORT || '3001'),
      host: true,
    },
    // ...其他配置
  };
});
```

---

### 📝 环境变量命名规范

**前端环境变量必须以 `VITE_` 开头！** （Vite 要求）

#### **根目录 `.env` 示例：**
```bash
# ==================== 前端配置 ====================
# API 基础 URL（指向后端服务）
VITE_API_BASE_URL=http://localhost:8200

# WebSocket URL（实时通讯）
VITE_WS_URL=ws://localhost:8200/ws

# 静态资源基础 URL（图片/文件）
VITE_STATIC_BASE_URL=http://localhost:8200

# 应用标题
VITE_APP_TITLE_ADMIN=校园轻享集市 - 管理端
VITE_APP_TITLE_PORTAL=校园轻享集市

# 开发服务器端口
VITE_ADMIN_PORT=3000
VITE_PORTAL_PORT=3001
```

---

### 🚀 如何使用环境变量

#### **在代码中访问：**
```typescript
// ✅ 使用 import.meta.env.VITE_XXX
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8200';
const WS_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8200/ws';
```

#### **TypeScript 类型定义：**
```typescript
// src/vite-env.d.ts（Vite 自动生成）
/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
  readonly VITE_WS_URL: string;
  readonly VITE_STATIC_BASE_URL: string;
  readonly VITE_APP_TITLE_ADMIN: string;
  readonly VITE_APP_TITLE_PORTAL: string;
  readonly VITE_ADMIN_PORT: string;
  readonly VITE_PORTAL_PORT: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
```

---

### 🚨 禁止事项（铁律）

❌ **禁止在任何包内创建独立的 `.env` 文件：**
```bash
# ❌ 错误示例 - 禁止创建这些文件
frontend/packages/portal/.env
frontend/packages/portal/.env.development
frontend/packages/portal/.env.production

frontend/packages/admin/.env
frontend/packages/admin/.env.development
frontend/packages/admin/.env.production

frontend/packages/shared/.env
```

❌ **禁止在代码中硬编码配置：**
```typescript
// ❌ 错误 - 硬编码 API 地址
const API_BASE_URL = 'http://localhost:8200';

// ✅ 正确 - 使用环境变量
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
```

---

### 📋 新包开发 Checklist

**当开发新的前端包时（如 `@campus/mobile`）：**

1. [ ] ✅ 在根目录 `.env` 添加包专属配置（如 `VITE_MOBILE_PORT=3002`）
2. [ ] ✅ 在包的 `vite.config.ts` 中配置读取根目录环境变量
   ```typescript
   const env = loadEnv(mode, path.resolve(__dirname, '../../..'), 'VITE_');
   ```
3. [ ] ❌ **禁止**在包内创建独立的 `.env` 文件
4. [ ] ✅ 使用 `import.meta.env.VITE_XXX` 访问环境变量
5. [ ] ✅ 在 `.env.example` 中添加对应的模板配置

---

## 🚀 API 集成规范（核心铁律！）

### 📦 架构总览

```
📦 API 集成架构（三层架构）
├── 第一层：OpenAPI 自动生成（frontend/packages/shared/src/api/）
│   ├── api.ts                    # API 导出入口
│   ├── api/default-api.ts        # DefaultApi 类（所有接口）
│   ├── models/                   # 类型定义（DTO/Response）
│   └── base.ts                   # 基础配置
│
├── 第二层：API 客户端封装（frontend/packages/shared/src/utils/apiClient.ts）
│   ├── getApi()                  # 获取 DefaultApi 单例
│   ├── Token 管理                # JWT Token 自动注入
│   ├── 请求/响应拦截器           # 统一错误处理
│   └── Token 自动刷新            # 401 自动刷新 Token
│
└── 第三层：Service 层封装（业务逻辑）
    ├── 共享服务（frontend/packages/shared/src/services/）
    │   ├── goods.ts              # 商品服务
    │   ├── order.ts              # 订单服务
    │   ├── user.ts               # 用户服务
    │   └── ...                   # 其他共享服务
    │
    └── 管理端专属服务（frontend/packages/admin/src/services/）
        ├── statistics.ts         # 统计服务
        ├── adminUser.ts          # 管理员用户服务
        ├── dispute.ts            # 纠纷服务
        └── ...                   # 其他管理端服务
```

---

### 🎯 核心规则（必须遵守！）

#### **1️⃣ 禁止手写 API 调用（铁律）**

❌ **绝对禁止**：
```typescript
// ❌ 错误 - 禁止直接使用 fetch()
const response = await fetch(`${API_BASE_URL}/api/goods`, {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  },
});

// ❌ 错误 - 禁止直接使用 axios
import axios from 'axios';
const response = await axios.get('/api/goods');

// ❌ 错误 - 禁止硬编码 API 路径
const API_PATH = '/api/goods/list';
```

✅ **正确做法**：
```typescript
// ✅ 正确 - 使用 Service 层
import { goodsService } from '@campus/shared/services/goods';

const goods = await goodsService.listGoods({ page: 0, size: 20 });
```

---

#### **2️⃣ 使用 Service 层（标准流程）**

**共享服务（两端通用）**：
```typescript
// ✅ 从共享层导入
import { goodsService } from '@campus/shared/services/goods';
import { orderService } from '@campus/shared/services/order';
import { userService } from '@campus/shared/services/user';
import type { GoodsResponse, PageGoodsResponse } from '@campus/shared/api';

// ✅ 使用 React Query
const { data, isLoading } = useQuery({
  queryKey: ['goods', 'list', params],
  queryFn: () => goodsService.listGoods(params),
});
```

**管理端专属服务**：
```typescript
// ✅ 从管理端服务导入
import { statisticsService } from '@/services';
import { adminUserService } from '@/services';
import { disputeService } from '@/services';

// ✅ 使用 React Query
const { data } = useQuery({
  queryKey: ['statistics', 'overview'],
  queryFn: () => statisticsService.getOverview(),
});
```

---

#### **3️⃣ API 更新流程（定期执行）**

**当后端接口变更时，必须执行以下步骤：**

```bash
# 步骤 1：确保后端服务运行
# 访问 http://localhost:8200/api/actuator/health 检查状态

# 步骤 2：生成前端 API 代码
cd frontend
pnpm run api:generate

# 步骤 3：检查生成的代码
# 查看 frontend/packages/shared/src/api/ 目录

# 步骤 4：更新 Service 层（如有需要）
# 如果新增了接口，需要在对应的 Service 中添加方法
```

**自动化脚本**：
```json
// package.json
{
  "scripts": {
    "api:generate": "cd ../backend && mvn clean && mvn -Dspring-boot.run.arguments=\"--openapi.export.enabled=true,--openapi.export.path=target/openapi-frontend.json\" spring-boot:run && mvn -P openapi openapi-generator:generate"
  }
}
```

---

#### **4️⃣ Service 层开发规范**

**创建新 Service 的标准模板**：

```typescript
/**
 * XXX API 服务
 * @author BaSui 😎
 * @description XXX 相关接口（基于 OpenAPI 生成代码）
 */

import { getApi } from '../utils/apiClient';
import type {
  XxxResponse,
  XxxRequest,
  PageXxxResponse,
} from '../api/models';

/**
 * XXX 列表查询参数
 */
export interface XxxListParams {
  keyword?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}

/**
 * XXX API 服务类
 */
export class XxxService {
  /**
   * 获取 XXX 列表（分页）
   * @param params 查询参数
   * @returns XXX 列表（分页）
   */
  async list(params?: XxxListParams): Promise<PageXxxResponse> {
    const api = getApi();
    const response = await api.listXxx(
      params?.keyword,
      params?.page,
      params?.size,
      params?.sortBy,
      params?.sortDirection
    );
    return response.data.data as PageXxxResponse;
  }

  /**
   * 获取 XXX 详情
   * @param id XXX ID
   * @returns XXX 详情
   */
  async getDetail(id: number): Promise<XxxResponse> {
    const api = getApi();
    const response = await api.getXxxDetail(id);
    return response.data.data as XxxResponse;
  }

  /**
   * 创建 XXX
   * @param data XXX 信息
   * @returns 创建的 XXX ID
   */
  async create(data: XxxRequest): Promise<number> {
    const api = getApi();
    const response = await api.createXxx(data);
    return response.data.data as number;
  }

  /**
   * 更新 XXX
   * @param id XXX ID
   * @param data XXX 信息
   */
  async update(id: number, data: XxxRequest): Promise<void> {
    const api = getApi();
    await api.updateXxx(id, data);
  }

  /**
   * 删除 XXX
   * @param id XXX ID
   */
  async delete(id: number): Promise<void> {
    const api = getApi();
    await api.deleteXxx(id);
  }
}

// 导出单例
export const xxxService = new XxxService();
export default xxxService;
```

---

### 📋 代码审查 Checklist

**在提交代码前，必须检查以下项目：**

- [ ] ✅ 没有使用 `fetch()` 直接调用 API
- [ ] ✅ 没有使用 `axios` 直接调用 API
- [ ] ✅ 没有硬编码 API 路径
- [ ] ✅ 所有 API 调用都使用 Service 层
- [ ] ✅ 所有类型都从 `@campus/shared/api` 导入
- [ ] ✅ 使用 React Query 管理异步状态
- [ ] ✅ 错误处理统一使用 apiClient 的拦截器
- [ ] ✅ Token 管理由 apiClient 自动处理

---

### 🎯 最佳实践

#### **1. 使用 React Query 管理异步状态**

```typescript
// ✅ 查询数据
const { data, isLoading, error, refetch } = useQuery({
  queryKey: ['goods', 'list', params],
  queryFn: () => goodsService.listGoods(params),
  staleTime: 5 * 60 * 1000, // 缓存 5 分钟
});

// ✅ 修改数据
const mutation = useMutation({
  mutationFn: (data: GoodsRequest) => goodsService.create(data),
  onSuccess: () => {
    message.success('创建成功');
    queryClient.invalidateQueries({ queryKey: ['goods'] });
  },
  onError: (error: any) => {
    message.error(error?.message || '创建失败');
  },
});
```

#### **2. 统一错误处理**

```typescript
// ✅ apiClient 已经统一处理了错误
// 不需要在每个请求中重复处理 401、403、500 等错误

// ✅ 只需要处理业务错误
const mutation = useMutation({
  mutationFn: (data) => goodsService.create(data),
  onError: (error: any) => {
    // 业务错误提示
    message.error(error?.message || '操作失败');
  },
});
```

#### **3. Token 自动管理**

```typescript
// ✅ Token 由 apiClient 自动管理
// - 登录后自动保存 Token
// - 请求时自动注入 Token
// - 401 错误自动刷新 Token
// - 刷新失败自动跳转登录页

// 不需要手动处理 Token！
```

---

### 🚨 常见错误和解决方案

#### **错误 1：直接使用 fetch()**

```typescript
// ❌ 错误
const response = await fetch('/api/goods');

// ✅ 正确
import { goodsService } from '@campus/shared/services/goods';
const goods = await goodsService.listGoods();
```

#### **错误 2：硬编码 API 路径**

```typescript
// ❌ 错误
const API_PATH = '/api/goods/list';
const response = await api.get(API_PATH);

// ✅ 正确
const goods = await goodsService.listGoods();
```

#### **错误 3：重复处理 Token**

```typescript
// ❌ 错误 - 不需要手动处理 Token
const token = localStorage.getItem('token');
const response = await fetch('/api/goods', {
  headers: { Authorization: `Bearer ${token}` },
});

// ✅ 正确 - apiClient 自动处理
const goods = await goodsService.listGoods();
```

#### **错误 4：重复处理错误**

```typescript
// ❌ 错误 - 不需要重复处理 401、403、500
try {
  const response = await fetch('/api/goods');
  if (response.status === 401) {
    // 跳转登录...
  }
} catch (error) {
  // 错误处理...
}

// ✅ 正确 - apiClient 已统一处理
const { data, error } = useQuery({
  queryKey: ['goods'],
  queryFn: () => goodsService.listGoods(),
});
```

---

### 📊 架构优势

| 优势 | 说明 |
|------|------|
| **类型安全** | 完整的 TypeScript 类型定义，编译时检查 |
| **自动同步** | 后端接口变更自动同步到前端 |
| **统一管理** | 所有 API 调用统一管理，易于维护 |
| **错误处理** | 统一的错误处理和提示 |
| **Token 管理** | 自动注入和刷新 Token |
| **易于测试** | Service 层易于单元测试 |
| **代码复用** | Service 层可在多个组件中复用 |

---

**最后提醒：**
> 前端服务层必须继承或直接使用 OpenAPI 生成的代码，
> 而不是手动维护接口路径！这是铁律！💪✨
>
> **记住三条黄金法则：**
> 1. 🚫 **禁止手写 API 调用**（fetch/axios）
> 2. ✅ **必须使用 Service 层**（共享/管理端服务）
> 3. 🔄 **定期更新 API 代码**（pnpm run api:generate）
