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

**最后提醒：**
> 前端服务层必须继承或直接使用 OpenAPI 生成的代码，
> 而不是手动维护接口路径！这是铁律！💪✨
