# 🚀 前端 API 集成快速指南

> **作者**: BaSui 😎 | **更新**: 2025-11-08
> **目的**: 确保所有前端页面正确使用 OpenAPI 自动生成的 API 代码

---

## 📋 快速检查清单

在开发新页面或修改现有页面时，请确保：

- [ ] ✅ **没有使用** `fetch()` 直接调用 API
- [ ] ✅ **没有使用** `axios` 直接调用 API
- [ ] ✅ **没有硬编码** API 路径（如 `/api/goods/list`）
- [ ] ✅ **使用 Service 层**（共享服务或管理端服务）
- [ ] ✅ **类型导入**正确（从 `@campus/shared/api` 导入）
- [ ] ✅ **使用 React Query** 管理异步状态
- [ ] ✅ **错误处理**由 apiClient 统一处理
- [ ] ✅ **Token 管理**由 apiClient 自动处理

---

## 🎯 三条黄金法则

### 1️⃣ 禁止手写 API 调用

```typescript
// ❌ 错误
const response = await fetch('/api/goods');
const response = await axios.get('/api/goods');

// ✅ 正确
import { goodsService } from '@campus/shared/services/goods';
const goods = await goodsService.listGoods();
```

### 2️⃣ 必须使用 Service 层

```typescript
// ✅ 共享服务（两端通用）
import { goodsService } from '@campus/shared/services/goods';
import { orderService } from '@campus/shared/services/order';

// ✅ 管理端专属服务
import { statisticsService } from '@/services';
import { adminUserService } from '@/services';
```

### 3️⃣ 定期更新 API 代码

```bash
# 后端接口变更后，执行此命令
cd frontend
pnpm run api:generate
```

---

## 📦 标准使用模板

### 查询数据（useQuery）

```typescript
import { useQuery } from '@tanstack/react-query';
import { goodsService } from '@campus/shared/services/goods';
import type { PageGoodsResponse } from '@campus/shared/api';

const { data, isLoading, error, refetch } = useQuery({
  queryKey: ['goods', 'list', params],
  queryFn: () => goodsService.listGoods(params),
  staleTime: 5 * 60 * 1000, // 缓存 5 分钟
});
```

### 修改数据（useMutation）

```typescript
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { goodsService } from '@campus/shared/services/goods';
import { message } from 'antd';

const queryClient = useQueryClient();

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

// 使用
mutation.mutate(formData);
```

---

## 🏗️ 架构层级

```
📦 三层架构
├── 第一层：OpenAPI 自动生成
│   └── frontend/packages/shared/src/api/
│
├── 第二层：API 客户端封装
│   └── frontend/packages/shared/src/utils/apiClient.ts
│
└── 第三层：Service 层封装
    ├── 共享服务：frontend/packages/shared/src/services/
    └── 管理端服务：frontend/packages/admin/src/services/
```

---

## 📚 常用服务导入

### 共享服务（两端通用）

```typescript
// 商品服务
import { goodsService } from '@campus/shared/services/goods';

// 订单服务
import { orderService } from '@campus/shared/services/order';

// 用户服务
import { userService } from '@campus/shared/services/user';

// 分类服务
import { categoryService } from '@campus/shared/services';

// 校园服务
import { campusService } from '@campus/shared/services';

// 标签服务
import { tagService } from '@campus/shared/services';

// 话题服务
import { topicService } from '@campus/shared/services/topic';

// 任务服务
import { taskService } from '@campus/shared/services/task';

// 通知服务
import { notificationService } from '@campus/shared/services';

// 消息服务
import { messageService } from '@campus/shared/services/message';

// 物流服务
import { logisticsService } from '@campus/shared/services/logistics';

// 退款服务
import { refundService } from '@campus/shared/services/refund';
```

### 管理端专属服务

```typescript
// 统计服务
import { statisticsService } from '@/services';

// 管理员用户服务
import { adminUserService } from '@/services';

// 纠纷服务
import { disputeService } from '@/services';

// 纠纷统计服务
import { disputeStatisticsService } from '@/services';

// 申诉服务
import { appealService } from '@/services';

// 黑名单服务
import { blacklistService } from '@/services';

// 举报服务
import { reportService } from '@/services';

// 合规审计服务
import { complianceService } from '@/services';

// 功能开关服务
import { featureFlagService } from '@/services';

// 角色权限服务
import { roleService } from '@/services';

// 系统监控服务
import { monitorService } from '@/services';
```

---

## 🚨 常见错误

### 错误 1：直接使用 fetch()

```typescript
// ❌ 错误
const response = await fetch('/api/goods');

// ✅ 正确
const goods = await goodsService.listGoods();
```

### 错误 2：硬编码 API 路径

```typescript
// ❌ 错误
const API_PATH = '/api/goods/list';

// ✅ 正确
const goods = await goodsService.listGoods();
```

### 错误 3：手动处理 Token

```typescript
// ❌ 错误
const token = localStorage.getItem('token');
const response = await fetch('/api/goods', {
  headers: { Authorization: `Bearer ${token}` },
});

// ✅ 正确（apiClient 自动处理）
const goods = await goodsService.listGoods();
```

### 错误 4：重复处理错误

```typescript
// ❌ 错误
try {
  const response = await fetch('/api/goods');
  if (response.status === 401) {
    // 跳转登录...
  }
} catch (error) {
  // 错误处理...
}

// ✅ 正确（apiClient 已统一处理）
const { data, error } = useQuery({
  queryKey: ['goods'],
  queryFn: () => goodsService.listGoods(),
});
```

---

## 🔄 API 更新流程

### 步骤 1：确保后端服务运行

```bash
# 检查后端服务状态
curl http://localhost:8200/api/actuator/health
```

### 步骤 2：生成前端 API 代码

```bash
cd frontend
pnpm run api:generate
```

### 步骤 3：检查生成的代码

```bash
# 查看生成的 API 代码
ls frontend/packages/shared/src/api/
```

### 步骤 4：更新 Service 层（如有需要）

如果新增了接口，需要在对应的 Service 中添加方法。

---

## 📊 架构优势

| 优势 | 说明 |
|------|------|
| **类型安全** | 完整的 TypeScript 类型定义 |
| **自动同步** | 后端接口变更自动同步 |
| **统一管理** | 所有 API 调用统一管理 |
| **错误处理** | 统一的错误处理和提示 |
| **Token 管理** | 自动注入和刷新 Token |
| **易于测试** | Service 层易于单元测试 |
| **代码复用** | Service 层可在多个组件中复用 |

---

## 💡 最佳实践

### 1. 使用 React Query 缓存

```typescript
const { data } = useQuery({
  queryKey: ['goods', 'list', params],
  queryFn: () => goodsService.listGoods(params),
  staleTime: 5 * 60 * 1000, // 缓存 5 分钟
});
```

### 2. 统一错误处理

```typescript
// apiClient 已经统一处理了 401、403、500 等错误
// 只需要处理业务错误
const mutation = useMutation({
  mutationFn: (data) => goodsService.create(data),
  onError: (error: any) => {
    message.error(error?.message || '操作失败');
  },
});
```

### 3. 自动刷新缓存

```typescript
const mutation = useMutation({
  mutationFn: (data) => goodsService.create(data),
  onSuccess: () => {
    // 自动刷新相关查询的缓存
    queryClient.invalidateQueries({ queryKey: ['goods'] });
  },
});
```

---

## 📞 遇到问题？

1. **查看完整文档**：`frontend/CLAUDE.md`
2. **查看示例代码**：`frontend/packages/admin/src/pages/Goods/GoodsList.tsx`
3. **联系团队**：在团队群里提问

---

**记住：**
> 🚫 **禁止手写 API 调用**（fetch/axios）
> ✅ **必须使用 Service 层**（共享/管理端服务）
> 🔄 **定期更新 API 代码**（pnpm run api:generate）

**BaSui 的座右铭：**
> 代码要写得漂亮，但过程可以很欢乐！
> OpenAPI 自动生成，类型安全又省心！💪✨
