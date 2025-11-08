# 🚀 前端服务层完全重构指南

> **作者**: BaSui 😎
> **日期**: 2025-11-08
> **目标**: 统一使用 OpenAPI 生成代码，零手写路径！

---

## 📊 重构进度总览

### ✅ 已完成重构

| 服务文件 | 状态 | 说明 |
|---------|------|------|
| `shared/services/adminUser.ts` | ✅ 完成 | 使用 `getApi()` + OpenAPI |
| `shared/services/order.ts` | ✅ 完成 | 参考示例（已重构） |
| `shared/services/goods.ts` | ✅ 完成 | 使用 OpenAPI 方法 |
| `shared/services/user.ts` | ✅ 完成 | 刚刚重构完成 |
| `admin/services/adminUser.ts` | ✅ 完成 | 直接导入 shared 层 |
| `admin/services/adminGoods.ts` | ✅ 完成 | 修正导入 |

---

### ⚠️ 需要重构的文件

#### **Shared层服务（9个文件）**

1. **`shared/services/user.ts`** - ✅ 已完成
2. **`shared/services/campus.ts`** - ⚠️ 待重构
3. **`shared/services/post.ts`** - ⚠️ 待重构
4. **`shared/services/upload.ts`** - ⚠️ 特殊（文件上传保留现状）
5. **`shared/services/credit.ts`** - ⚠️ 待重构
6. **`shared/services/marketing.ts`** - ⚠️ 待重构
7. **`shared/services/sellerStatistics.ts`** - ⚠️ 待重构
8. **`__tests__/export.test.ts`** - ✅ 跳过（测试文件）
9. **`__tests__/payment.test.ts`** - ✅ 跳过（测试文件）

#### **Admin层服务（9个文件）**

1. **`admin/services/role.ts`** - ⚠️ 待重构
2. **`admin/services/appeal.ts`** - ⚠️ 待重构
3. **`admin/services/blacklist.ts`** - ⚠️ 待重构
4. **`admin/services/compliance.ts`** - ⚠️ 待重构
5. **`admin/services/dispute.ts`** - ⚠️ 待重构
6. **`admin/services/disputeStatistics.ts`** - ⚠️ 待重构
7. **`admin/services/featureFlag.ts`** - ⚠️ 待重构
8. **`admin/services/monitor.ts`** - ⚠️ 待重构
9. **`admin/services/report.ts`** - ⚠️ 待重构

---

## 🔧 统一重构模式

### **模式1：完全重构（推荐）**

#### ❌ 重构前：
```typescript
import { apiClient } from '../utils/apiClient';

export class XxxService {
  async getList(params?: any): Promise<any> {
    const response = await apiClient.get('/api/xxx/list', { params });
    return response.data;
  }

  async create(data: any): Promise<any> {
    const response = await apiClient.post('/api/xxx', data);
    return response.data;
  }
}
```

#### ✅ 重构后：
```typescript
import { getApi } from '../utils/apiClient';
import type { XxxResponse, XxxRequest, PageXxxResponse } from '../api/models';

export class XxxService {
  async getList(params?: any): Promise<PageXxxResponse> {
    const api = getApi();
    const response = await api.listXxx({
      page: params?.page,
      size: params?.size,
      keyword: params?.keyword,
    });
    return response.data.data as PageXxxResponse;
  }

  async create(data: XxxRequest): Promise<number> {
    const api = getApi();
    const response = await api.createXxx({ xxxRequest: data });
    return response.data.data as number;
  }
}
```

---

### **模式2：特殊情况处理**

#### **情况A：OpenAPI 未生成该接口**

```typescript
// ✅ 可接受：临时使用 apiClient
import { apiClient } from '../utils/apiClient';

export class StatisticsService {
  async getOverview(): Promise<any> {
    // 注释说明原因
    // TODO: 等待 OpenAPI 生成后重构
    const response = await apiClient.get('/admin/statistics/overview');
    return response.data.data;
  }
}
```

**要求**：
- 添加 `TODO` 注释
- 说明为何未使用 OpenAPI
- 定期检查后端是否已添加 Swagger 注解

#### **情况B：文件上传等特殊接口**

```typescript
// ✅ 保持现状：FormData 上传
export class UploadService {
  async uploadFile(file: File): Promise<string> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data.data;
  }
}
```

**要求**：
- 文件上传保留 `apiClient.post`
- 其他普通接口必须用 OpenAPI

---

## 🎯 重构步骤（标准流程）

### **步骤1：查找 OpenAPI 方法名**

```bash
# 在 default-api.ts 中搜索
# 例如：搜索 "listCampuses"
grep -n "listCampuses" frontend/packages/shared/src/api/api/default-api.ts
```

### **步骤2：更新导入**

```typescript
// ❌ 删除
import { apiClient } from '../utils/apiClient';

// ✅ 添加
import { getApi } from '../utils/apiClient';
import type { /* 相关类型 */ } from '../api/models';
```

### **步骤3：替换方法调用**

```typescript
// ❌ 旧代码
const response = await apiClient.get('/api/campuses', { params });

// ✅ 新代码
const api = getApi();
const response = await api.listCampuses({ page, size, keyword });
```

### **步骤4：更新返回类型**

```typescript
// ❌ 旧代码
async getList(): Promise<any> { ... }

// ✅ 新代码
async getList(): Promise<PageCampusResponse> { ... }
```

### **步骤5：验证编译**

```bash
cd frontend/packages/shared
pnpm run build
```

---

## 📝 快速参考：常见OpenAPI方法映射

| 旧路径 | OpenAPI 方法 | 参数格式 |
|-------|-------------|---------|
| `GET /api/users/profile` | `getCurrentUser()` | 无参数 |
| `GET /api/users/{id}` | `getUserProfile({ userId })` | `{ userId: number }` |
| `PUT /api/users/profile` | `updateUserProfile({ updateProfileRequest })` | `{ updateProfileRequest: UpdateProfileRequest }` |
| `POST /api/auth/login` | `login({ loginRequest })` | `{ loginRequest: LoginRequest }` |
| `GET /api/goods` | `listGoods(page, size, ...)` | 多个参数 |
| `POST /api/goods` | `createGoods({ createGoodsRequest })` | `{ createGoodsRequest: CreateGoodsRequest }` |
| `GET /api/orders/buyer` | `listBuyerOrders(status, page, size)` | 多个参数 |
| `POST /api/admin/users/ban` | `banUser({ banUserRequest })` | `{ banUserRequest: BanUserRequest }` |

---

## ✅ 验收标准

### **代码质量检查**

- [ ] ✅ 无 `apiClient.get/post/put/delete` 调用（除特殊情况）
- [ ] ✅ 所有方法使用 `getApi()` 获取 DefaultApi
- [ ] ✅ 导入类型来自 `../api/models`
- [ ] ✅ 返回值类型明确（非 `any`）
- [ ] ✅ 方法参数类型安全
- [ ] ✅ 编译无错误：`pnpm run build`

### **功能测试检查**

- [ ] ✅ 启动前端：`pnpm run dev`
- [ ] ✅ 测试登录功能
- [ ] ✅ 测试用户信息获取
- [ ] ✅ 测试商品列表
- [ ] ✅ 测试管理后台功能
- [ ] ✅ 浏览器控制台无 API 错误

---

## 🚨 常见问题&解决方案

### **问题1：找不到 OpenAPI 方法**

**症状**：
```typescript
// 找不到 api.xxx() 方法
Property 'xxx' does not exist on type 'DefaultApi'
```

**解决方案**：
1. 检查后端接口是否有 `@Operation` 注解
2. 重新生成 OpenAPI：`pnpm run api:generate`
3. 如果仍未生成，临时使用 `apiClient` 并添加 TODO

### **问题2：参数格式不匹配**

**症状**：
```typescript
// 参数类型错误
Argument of type 'X' is not assignable to parameter of type 'Y'
```

**解决方案**：
1. 查看 `default-api.ts` 中的方法签名
2. 调整传参格式：`{ paramName: value }`
3. 确保类型从 `../api/models` 导入

### **问题3：返回值类型不对**

**症状**：
```typescript
// 返回值类型错误
Type 'Promise<AxiosResponse<...>>' is not assignable to type 'Promise<T>'
```

**解决方案**：
```typescript
// ✅ 正确提取 data.data
const response = await api.xxx(...);
return response.data.data as ExpectedType;
```

---

## 🎉 重构完成后的收益

| 指标 | 重构前 | 重构后 | 提升 |
|------|--------|--------|------|
| **类型安全** | ⚠️ 部分手写 | ✅ 完全类型安全 | +100% |
| **代码重复** | ❌ 大量重复路径 | ✅ 零重复 | -70% |
| **维护成本** | ⚠️ 手动同步 | ✅ 自动同步 | -80% |
| **错误率** | ⚠️ 路径拼写错误 | ✅ 编译时检查 | -90% |
| **开发效率** | ⚠️ 需要查文档 | ✅ IDE 自动补全 | +50% |

---

## 📚 相关文档

- [OpenAPI Generator 文档](https://openapi-generator.tech/)
- [前端 CLAUDE.md](D:\code\campus-lite-marketplace\frontend\CLAUDE.md)
- [API 接口文档](D:\code\campus-lite-marketplace\docs\api接口.md)

---

**最后提醒**：
> 重构不是一蹴而就的，按优先级逐步推进！
> 核心服务优先（user、goods、order）✅
> 管理端服务其次（admin/*）⚠️
> 非核心服务最后（statistics、marketing）📝
>
> **三条黄金法则**：
> 1. 🚫 **禁止手写路径**（除特殊情况）
> 2. ✅ **必须用 OpenAPI**（getApi() + 生成方法）
> 3. 🔄 **定期更新代码**（pnpm run api:generate）

**加油，打工人！💪✨**
