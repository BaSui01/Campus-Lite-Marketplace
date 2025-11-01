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

### 🔍 验证配置生效

#### **检查环境变量是否正确加载：**
```typescript
// 在任意组件中打印环境变量
console.log('API Base URL:', import.meta.env.VITE_API_BASE_URL);
console.log('WS URL:', import.meta.env.VITE_WS_URL);
console.log('Static Base URL:', import.meta.env.VITE_STATIC_BASE_URL);
```

#### **启动服务验证：**
```bash
# 1. 确认根目录 .env 配置正确
cat .env | grep VITE_

# 2. 启动前端服务
cd frontend
pnpm dev

# 3. 检查控制台输出的端口是否匹配 VITE_PORTAL_PORT
```

---

## 🎯 核心原则（必须遵守）

### 1. ✅ 使用 OpenAPI 生成的代码（强制）

**❌ 禁止手动维护 API 路径：**
```typescript
// ❌ 错误示例 - 手动写死路径
async login(data: LoginRequest) {
  return http.post('/api/auth/login', data);  // 路径容易出错
}
```

**✅ 必须使用 OpenAPI 生成的 DefaultApi：**
```typescript
// ✅ 正确示例 - 使用生成的代码
class AuthService {
  private api: DefaultApi;

  constructor() {
    this.api = new DefaultApi(createApiConfig(), undefined, axiosInstance);
  }

  async login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    const response = await this.api.login({ loginRequest: data });
    return response.data as ApiResponse<LoginResponse>;
  }
}
```

**优势：**
- ✅ 类型安全：完整的 TypeScript 类型定义
- ✅ 路径统一：所有路径由 OpenAPI 规范管理
- ✅ 自动同步：后端 API 变更后重新生成即可
- ✅ 减少维护：无需手动维护路径和参数

---

### 2. 📁 Monorepo 包结构规范

```
frontend/
├── packages/
│   ├── portal/          # 前台用户界面
│   │   ├── src/
│   │   │   ├── pages/          # 页面组件
│   │   │   ├── components/     # 页面级组件
│   │   │   ├── layouts/        # 布局组件
│   │   │   └── router/         # 路由配置
│   │   └── package.json
│   │
│   ├── admin/           # 后台管理界面
│   │   └── (同 portal 结构)
│   │
│   └── shared/          # 共享代码库 ⭐ 核心包
│       ├── src/
│       │   ├── api/            # OpenAPI 生成的代码（自动生成）
│       │   ├── services/       # 业务服务层（基于 api/）
│       │   ├── components/     # 共享 UI 组件
│       │   ├── utils/          # 工具函数
│       │   ├── hooks/          # React Hooks
│       │   ├── types/          # 类型定义
│       │   └── constants/      # 常量定义
│       └── package.json
```

---

### 3. 🔧 API 调用规范

#### **配置层** (`shared/src/utils/http.ts`)
```typescript
// ⚠️ 注意：baseURL 不加 /api，因为 OpenAPI 生成的代码已包含
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8200';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,  // 不要加 /api
  timeout: 30000,
});
```

#### **服务层** (`shared/src/services/*.ts`)
```typescript
// ✅ 基于 OpenAPI 生成的 DefaultApi
class GoodsService {
  private api: DefaultApi;

  constructor() {
    this.api = new DefaultApi(createApiConfig(), undefined, axiosInstance);
  }

  async listGoods(params?: GoodsListParams): Promise<PageGoodsResponse> {
    const response = await this.api.listGoods(
      params?.keyword,
      params?.categoryId,
      // ...其他参数
    );
    return response.data.data as PageGoodsResponse;
  }
}

export const goodsService = new GoodsService();
```

#### **页面层** (`portal/src/pages/*.tsx`)
```typescript
import { goodsService } from '@campus/shared/services';

const GoodsPage: React.FC = () => {
  const loadGoods = async () => {
    const data = await goodsService.listGoods({ page: 0, size: 10 });
    // 使用数据...
  };
};
```

---

### 4. 🎨 UI 组件规范

#### **组件分类：**
- **P0 基础组件** (`shared/src/components/`): Button, Input, Modal 等
- **P1 表单组件** (`shared/src/components/`): Form, Select, DatePicker 等
- **P2 业务组件** (`shared/src/components/`): GoodsCard, OrderCard, UserAvatar 等
- **页面组件** (`portal/src/components/`): 页面特有组件

#### **组件开发原则：**
```typescript
// ✅ 组件必须有 TypeScript 类型
interface ButtonProps {
  type?: 'primary' | 'default' | 'danger';
  size?: 'small' | 'medium' | 'large';
  loading?: boolean;
  disabled?: boolean;
  onClick?: () => void;
  children?: React.ReactNode;
}

// ✅ 使用 FC 类型 + Props 接口
export const Button: React.FC<ButtonProps> = ({
  type = 'default',
  size = 'medium',
  loading = false,
  disabled = false,
  onClick,
  children,
}) => {
  // 实现...
};
```

---

### 5. 🔒 认证与路由规范

#### **Token 管理** (`shared/src/utils/http.ts`)
```typescript
// Token 存储
export const setTokens = (accessToken: string, refreshToken?: string): void => {
  localStorage.setItem('auth_token', accessToken);
  if (refreshToken) {
    localStorage.setItem('refresh_token', refreshToken);
  }
};

// 自动注入 Token
axiosInstance.interceptors.request.use(config => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

#### **路由守卫** (`portal/src/router/index.tsx`)
```typescript
import { Navigate } from 'react-router-dom';
import { hasToken } from '@campus/shared/utils/http';

// 需要登录的路由
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  if (!hasToken()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};
```

---

### 6. 📝 命名规范

#### **文件命名：**
- 组件文件：`PascalCase.tsx` (例：`Button.tsx`, `GoodsCard.tsx`)
- 工具文件：`camelCase.ts` (例：`http.ts`, `storage.ts`)
- 样式文件：与组件同名 (例：`Button.css`, `GoodsCard.css`)

#### **变量命名：**
- 组件：`PascalCase` (例：`Button`, `GoodsCard`)
- 函数/变量：`camelCase` (例：`handleClick`, `isLoading`)
- 常量：`UPPER_SNAKE_CASE` (例：`API_BASE_URL`, `TOKEN_KEY`)
- 接口/类型：`PascalCase` (例：`LoginRequest`, `ApiResponse`)

---

### 7. 🔄 OpenAPI 代码生成流程（重要！）

#### **生成命令：**
```bash
# 1. 确保后端服务已启动（http://localhost:8200）
# 2. 在后端目录运行生成命令
cd backend
mvn -P openapi openapi-generator:generate

# 或者使用前端根目录的快捷命令（需要后端启动）
cd frontend
pnpm api:generate
```

#### **生成流程：**
1. 📡 从后端 `/v3/api-docs` 获取 OpenAPI JSON 规范
2. 📄 保存到 `backend/target/openapi-frontend.json`
3. 🔧 使用 OpenAPI Generator Maven 插件生成前端代码
4. 📦 输出到 `frontend/packages/shared/src/api/`

#### **生成的文件：**
```
frontend/packages/shared/src/api/
├── api/
│   └── default-api.ts      # ⭐ API 客户端（所有接口）
├── models/
│   ├── login-request.ts    # 请求模型
│   ├── login-response.ts   # 响应模型
│   └── ...                 # 其他模型
├── base.ts                 # 基础配置
├── common.ts               # 公共工具
└── configuration.ts        # 配置接口
```

#### **🚨 铁律：禁止手写 API 路径！**

**❌ 错误示例（手写路径）：**
```typescript
// ❌ 直接使用 axios 手写路径
async checkUsername(username: string) {
  const response = await axiosInstance.get('/api/auth/check-username', {
    params: { username },
  });
  return response.data;
}
```

**✅ 正确示例（使用生成的 API）：**
```typescript
// ✅ 使用 OpenAPI 生成的 DefaultApi
async checkUsername(username: string): Promise<ApiResponse<boolean>> {
  const response = await this.api.checkUsername({ username });
  return response.data as ApiResponse<boolean>;
}
```

#### **违规检查：**
```bash
# 检查是否有手写的 API 路径调用
grep -r "axiosInstance.get\|axiosInstance.post" frontend/packages/shared/src/services/

# 如果有结果，说明有手写路径，必须改为使用 DefaultApi！
```

#### **注意事项：**
- ⚠️ **不要手动修改** `src/api/` 目录下的文件（自动生成）
- ✅ 服务层 (`src/services/`) **只能**基于 `DefaultApi` 封装
- ✅ 后端 API 变更后，**必须**重新生成前端代码
- 🚫 **禁止**在服务层直接使用 `axiosInstance.get/post/put/delete`
- ✅ 后端新增接口时，先添加 Swagger 注解，再生成前端代码

---

### 8. 🎯 开发最佳实践

#### **状态管理：**
```typescript
// ✅ 使用 React Hooks
const [loading, setLoading] = useState(false);
const [data, setData] = useState<GoodsResponse[]>([]);

// ✅ 封装自定义 Hook
const useGoodsList = () => {
  const [goods, setGoods] = useState<GoodsResponse[]>([]);
  const [loading, setLoading] = useState(false);

  const loadGoods = async () => {
    setLoading(true);
    try {
      const data = await goodsService.listGoods();
      setGoods(data.content);
    } finally {
      setLoading(false);
    }
  };

  return { goods, loading, loadGoods };
};
```

#### **错误处理：**
```typescript
// ✅ 统一错误处理
try {
  const response = await authService.login(data);
  setTokens(response.data.token);
  navigate('/');
} catch (error: any) {
  const errorMessage = error?.response?.data?.message || '登录失败';
  toast.error(errorMessage);
}
```

---

### 9. 🚀 性能优化

#### **代码分割：**
```typescript
// ✅ 懒加载路由
const Home = lazy(() => import('./pages/Home'));
const GoodsDetail = lazy(() => import('./pages/GoodsDetail'));

const routes = [
  {
    path: '/',
    element: <Suspense fallback={<Loading />}><Home /></Suspense>,
  },
];
```

#### **避免不必要的渲染：**
```typescript
// ✅ 使用 React.memo
export const GoodsCard = React.memo<GoodsCardProps>(({ goods }) => {
  return <div>{goods.title}</div>;
});

// ✅ 使用 useCallback
const handleClick = useCallback(() => {
  console.log('clicked');
}, []);
```

---

### 10. 📋 开发 Checklist

**开发新功能前：**
- [ ] 确认后端 API 已定义（Swagger 文档）
- [ ] 运行 `npm run generate:api` 生成最新代码
- [ ] 在 `shared/src/services/` 封装服务层
- [ ] 在页面中使用服务层（不直接调用 API）

**提交代码前：**
- [ ] 代码通过 ESLint 检查
- [ ] 组件有完整的 TypeScript 类型定义
- [ ] 没有 `console.log` 残留
- [ ] 没有手动维护的 API 路径
- [ ] 样式适配移动端（响应式设计）

---

### 11. 🐛 常见问题与解决

#### **问题 1：请求路径出现 `/api/api`**
**原因**：`http.ts` 的 `baseURL` 包含了 `/api`，但 OpenAPI 生成的代码也包含 `/api`
**解决**：`baseURL` 不要加 `/api` 前缀
```typescript
// ❌ 错误
const API_BASE_URL = 'http://localhost:8200/api';

// ✅ 正确
const API_BASE_URL = 'http://localhost:8200';
```

#### **问题 2：401 未授权错误**
**原因**：Token 未正确保存或未注入到请求头
**解决**：
1. 检查 `setTokens()` 是否被调用
2. 检查 Axios 拦截器是否正确注入 Token
3. 检查 localStorage 中是否有 `auth_token`

#### **问题 3：类型定义不匹配**
**原因**：后端 API 变更但前端代码未更新
**解决**：重新生成 OpenAPI 代码
```bash
cd frontend/packages/shared
npm run generate:api
```

---

## 📚 参考资源

- [React 官方文档](https://react.dev/)
- [TypeScript 官方文档](https://www.typescriptlang.org/)
- [Vite 官方文档](https://vitejs.dev/)
- [Axios 官方文档](https://axios-http.com/)
- [OpenAPI Generator](https://openapi-generator.tech/)

---

**最后提醒：**
> 前端服务层必须继承或直接使用 OpenAPI 生成的代码，
> 而不是手动维护接口路径！这是铁律！💪✨
