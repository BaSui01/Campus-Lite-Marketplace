# 📋 Services 层重构计划

> **作者**: BaSui 😎
> **日期**: 2025-11-07
> **目标**: 按照"谁用谁拥有"原则重构 services 层，减少 shared 包体积

---

## 🎯 重构原则

**核心理念：谁用谁拥有，不要为了"可能复用"而放 shared！**

1. ✅ **真正两端都用的服务** → 留在 `shared/services`
2. 🔧 **仅管理端用的服务** → 移到 `admin/services`
3. 👥 **仅客户端用的服务** → 移到 `portal/services`
4. 🔨 **混合服务拆分** → 基础类留 shared，管理员功能移到 admin

---

## 📊 当前现状分析

### Shared Services 目录结构
```
frontend/packages/shared/src/services/
├── goods.ts                    ✅ 两端都用（需拆分）
├── order.ts                    ✅ 两端都用
├── user.ts                     ✅ 两端都用（需拆分）
├── auth.ts                     ✅ 两端都用
├── category.ts                 ✅ 两端都用（需拆分）
├── message.ts                  ✅ 两端都用
├── logistics.ts                ✅ 两端都用
├── goods/
│   ├── review.ts              ✅ 两端都用
│   └── types.ts               ✅ 类型定义
├── statistics.ts              🔧 仅管理端（需移动）
├── adminUser.ts               🔧 仅管理端（需移动）
├── monitor.ts                 🔧 仅管理端（需移动）
├── compliance.ts              🔧 仅管理端（需移动）
├── dispute.ts                 🔧 仅管理端（需移动）
├── disputeStatistics.ts       🔧 仅管理端（需移动）
├── appeal.ts                  🔧 仅管理端（需移动）
├── blacklist.ts               🔧 仅管理端（需移动）
├── report.ts                  🔧 仅管理端（需移动）
├── featureFlag.ts             🔧 仅管理端（需移动）
├── role.ts                    🔧 仅管理端（需移动）
├── favorite.ts                👥 仅客户端（需移动）
├── follow.ts                  👥 仅客户端（需移动）
├── credit.ts                  👥 仅客户端（需移动）
├── recommend.ts               👥 仅客户端（需移动）
├── marketing.ts               👥 仅客户端（需移动）
├── sellerStatistics.ts        👥 仅客户端（需移动）
├── subscription.ts            👥 仅客户端（需移动）
├── upload.ts                  🎯 通用工具（保留）
├── tag.ts                     🎯 通用工具（保留）
├── refund.ts                  🎯 通用工具（保留）
├── campus.ts                  🎯 通用工具（保留）
├── community.ts               🎯 通用工具（保留）
├── post.ts                    🎯 通用工具（保留）
├── topic.ts                   🎯 通用工具（保留）
├── task.ts                    🎯 通用工具（保留）
├── softDelete.ts              🎯 通用工具（保留）
├── revert.ts                  🎯 通用工具（保留）
├── rateLimit.ts               🎯 通用工具（保留）
├── notificationPreference.ts  🎯 通用工具（保留）
├── notificationTemplate.ts    🎯 通用工具（保留）
└── index.ts                   📦 统一导出
```

### 引用分析
- **Admin 包引用**: 17 个文件引用 shared/services
- **Portal 包引用**: 43 个文件引用 shared/services

---

## 📋 重构任务清单

### 阶段 1: 移动管理端专属服务 (11个文件)

#### 创建目标目录
```bash
mkdir -p frontend/packages/admin/src/services
```

#### 移动文件清单
| 源文件 (shared/services) | 目标文件 (admin/services) | 影响范围 |
|-------------------------|--------------------------|---------|
| `statistics.ts` | `statistics.ts` | `SystemMonitor.tsx`, `Dashboard.tsx` |
| `adminUser.ts` | `adminUser.ts` | `UserManagement.tsx` |
| `monitor.ts` | `monitor.ts` | `SystemMonitor.tsx` |
| `compliance.ts` | `compliance.ts` | `ComplianceCheck.tsx` |
| `dispute.ts` | `dispute.ts` | `DisputeList.tsx`, `DisputeDetail.tsx` |
| `disputeStatistics.ts` | `disputeStatistics.ts` | `DisputeStatistics.tsx` |
| `appeal.ts` | `appeal.ts` | `AppealList.tsx`, `AppealDetail.tsx` |
| `blacklist.ts` | `blacklist.ts` | `BlacklistManagement.tsx` |
| `report.ts` | `report.ts` | `ReportList.tsx` |
| `featureFlag.ts` | `featureFlag.ts` | `FeatureFlagList.tsx` |
| `role.ts` | `role.ts` | `RoleManagement.tsx` |

#### 更新引用路径
```typescript
// 旧引用（需修改）
import { statisticsService } from '@campus/shared/services/statistics';

// 新引用
import { statisticsService } from '../services/statistics';
// 或使用别名配置
import { statisticsService } from '@admin/services/statistics';
```

---

### 阶段 2: 移动客户端专属服务 (7个文件)

#### 创建目标目录
```bash
mkdir -p frontend/packages/portal/src/services
```

#### 移动文件清单
| 源文件 (shared/services) | 目标文件 (portal/services) | 影响范围 |
|-------------------------|---------------------------|---------|
| `favorite.ts` | `favorite.ts` | `Favorites.tsx`, `GoodsDetail.tsx` |
| `follow.ts` | `follow.ts` | `Following.tsx`, `UserProfile.tsx` |
| `credit.ts` | `credit.ts` | `Credit.tsx`, `CreditHistory.tsx` |
| `recommend.ts` | `recommend.ts` | `Home.tsx`, `RecommendGoods.tsx` |
| `marketing.ts` | `marketing.ts` | `Activities.tsx`, `Create.tsx` |
| `sellerStatistics.ts` | `sellerStatistics.ts` | `Dashboard.tsx` (卖家中心) |
| `subscription.ts` | `subscription.ts` | `Subscriptions.tsx`, `SubscriptionFeed.tsx` |

#### 更新引用路径
```typescript
// 旧引用（需修改）
import { favoriteService } from '@campus/shared/services/favorite';

// 新引用
import { favoriteService } from '../services/favorite';
// 或使用别名配置
import { favoriteService } from '@portal/services/favorite';
```

---

### 阶段 3: 拆分混合服务 (3个文件)

#### 3.1 拆分 goods.ts

**现状问题**：goods.ts 混杂了管理员审核功能

**拆分方案**：
```typescript
// ✅ shared/services/goods.ts - 保留通用功能
export class GoodsService {
  // 客户端 + 管理端通用方法
  async listGoods(params?: GoodsListParams) { /* 商品列表 */ }
  async getGoodsDetail(id: number) { /* 商品详情 */ }
  async searchGoods(keyword: string) { /* 搜索 */ }
  async createGoods(data: CreateGoodsRequest) { /* 发布商品 */ }
  async getMyGoods(params?: {...}) { /* 我的商品 */ }

  // 收藏功能（保留，客户端用）
  async favoriteGoods(goodsId: number) { /* 收藏 */ }
  async unfavoriteGoods(goodsId: number) { /* 取消收藏 */ }
  async getMyFavorites(page?: number, size?: number) { /* 收藏列表 */ }

  // 分类/标签（保留，两端都用）
  async getCategoryTree() { /* 分类树 */ }
  async getHotTags(limit?: number) { /* 热门标签 */ }
}

// ✅ admin/services/adminGoods.ts - 管理员专属功能
export class AdminGoodsService {
  /**
   * 获取待审核商品列表（管理员）
   */
  async listPendingGoods(params?: {
    page?: number;
    size?: number;
  }): Promise<PageGoodsResponse> {
    const api = getApi();
    const response = await api.getPendingGoods(params?.page, params?.size);
    return response.data.data as PageGoodsResponse;
  }

  /**
   * 审核商品（管理员）
   */
  async approveGoods(
    id: number,
    request: { approved: boolean; reason?: string }
  ): Promise<void> {
    const api = getApi();
    await api.approveGoods(id, request);
  }

  /**
   * 更新商品状态（管理员）
   */
  async updateGoodsStatus(id: number, status: string): Promise<void> {
    const api = getApi();
    await api.updateGoodsStatus(id, { status });
  }

  /**
   * 删除商品（管理员）
   */
  async deleteGoods(id: number): Promise<void> {
    const api = getApi();
    await api.deleteGoods1(id);
  }

  /**
   * 批量更新商品（管理员）
   */
  async batchUpdateGoods(request: {
    goodsIds: number[];
    targetStatus: string;
  }): Promise<void> {
    const api = getApi();
    await api.batchUpdateGoodsStatus(request);
  }
}

export const adminGoodsService = new AdminGoodsService();
export default adminGoodsService;
```

**影响文件**：
- `admin/src/pages/Goods/GoodsAudit.tsx` - 改用 `adminGoodsService`
- `admin/src/pages/Goods/GoodsList.tsx` - 改用 `adminGoodsService`
- `admin/src/pages/Goods/GoodsDetail.tsx` - 改用 `adminGoodsService`

---

#### 3.2 拆分 user.ts

**现状问题**：user.ts 包含管理员的用户管理功能

**拆分方案**：
```typescript
// ✅ shared/services/user.ts - 保留通用功能
export class UserService {
  async getProfile() { /* 获取个人资料 */ }
  async getUserById(userId: number) { /* 获取用户信息 */ }
  async updateProfile(data: UpdateProfileRequest) { /* 更新资料 */ }
  async changePassword(data: ChangePasswordRequest) { /* 修改密码 */ }
  async getPointsLogs(params?: {...}) { /* 积分记录 */ }
  async signIn() { /* 签到 */ }
}

// ✅ admin/services/adminUser.ts - 管理员专属功能（已存在，需补充）
export class AdminUserService {
  // 已有功能
  async banUser(payload: BanUserPayload) { /* 封禁用户 */ }
  async unbanUser(userId: number) { /* 解封用户 */ }
  async autoUnbanExpired() { /* 自动解封 */ }

  // 补充功能（从 user.ts 移过来）
  async getUserList(params: UserListQuery) { /* 用户列表 */ }
}
```

**影响文件**：
- `admin/src/pages/Users/UserList.tsx` - 改用 `adminUserService`
- `admin/src/pages/Users/UserDetail.tsx` - 改用 `adminUserService`

---

#### 3.3 拆分 category.ts

**现状问题**：category.ts 包含管理员的分类管理功能

**拆分方案**：
```typescript
// ✅ shared/services/category.ts - 保留查询功能
export class CategoryService {
  async tree() { /* 分类树 */ }
  async list(params?: CategoryListParams) { /* 分类列表 */ }
  async getDetail(id: number) { /* 分类详情 */ }
  async statistics(id: number) { /* 分类统计 */ }
  async getChildren(parentId: number) { /* 子分类 */ }

  // 前端工具方法
  toTree(categories: Category[]): CategoryTreeNode[] { /* ... */ }
  flatten(tree: Category[]): Category[] { /* ... */ }
}

// ✅ admin/services/adminCategory.ts - 管理员专属功能
export class AdminCategoryService {
  async create(data: CategoryRequest) { /* 添加分类 */ }
  async update(id: number, data: Partial<CategoryRequest>) { /* 更新 */ }
  async delete(id: number) { /* 删除 */ }
  async batchSort(items: CategorySortRequest[]) { /* 批量排序 */ }
  async move(id: number, newParentId: number | null) { /* 移动 */ }
  async updateStatus(id: number, status: CategoryStatus) { /* 状态 */ }
}
```

**影响文件**：
- `admin/src/pages/System/CategoryList.tsx` - 改用 `adminCategoryService`

---

### 阶段 4: 更新导出文件

#### 4.1 更新 shared/services/index.ts
```typescript
// ✅ 只导出真正共享的服务
export * from './auth';
export * from './goods';
export * from './goods/review';
export * from './order';
export * from './user';
export * from './category';
export * from './message';
export * from './logistics';

// ✅ 通用工具服务
export * from './upload';
export * from './tag';
export * from './refund';
export * from './campus';
export * from './community';
export * from './post';
export * from './topic';
export * from './task';
export * from './softDelete';
export * from './revert';
export * from './rateLimit';
export * from './notificationPreference';
export * from './notificationTemplate';

// ❌ 移除管理端专属服务导出
// export * from './statistics';        // 移到 admin
// export * from './adminUser';         // 移到 admin
// export * from './monitor';           // 移到 admin
// ... 等等

// ❌ 移除客户端专属服务导出
// export * from './favorite';          // 移到 portal
// export * from './follow';            // 移到 portal
// export * from './credit';            // 移到 portal
// ... 等等
```

#### 4.2 创建 admin/services/index.ts
```typescript
// ✅ 管理端服务统一导出
export * from './statistics';
export * from './adminUser';
export * from './adminGoods';
export * from './adminCategory';
export * from './monitor';
export * from './compliance';
export * from './dispute';
export * from './disputeStatistics';
export * from './appeal';
export * from './blacklist';
export * from './report';
export * from './featureFlag';
export * from './role';
```

#### 4.3 创建 portal/services/index.ts
```typescript
// ✅ 客户端服务统一导出
export * from './favorite';
export * from './follow';
export * from './credit';
export * from './recommend';
export * from './marketing';
export * from './sellerStatistics';
export * from './subscription';
```

---

### 阶段 5: 配置路径别名（可选）

#### 修改 admin/tsconfig.json
```json
{
  "compilerOptions": {
    "paths": {
      "@admin/*": ["./src/*"],
      "@admin/services/*": ["./src/services/*"]
    }
  }
}
```

#### 修改 portal/tsconfig.json
```json
{
  "compilerOptions": {
    "paths": {
      "@portal/*": ["./src/*"],
      "@portal/services/*": ["./src/services/*"]
    }
  }
}
```

---

## 🔥 重构影响分析

### 影响的文件数量
- **Admin 包**: 17 个文件需要更新 import 路径
- **Portal 包**: 43 个文件需要更新 import 路径
- **Shared 包**: 1 个文件需要更新（index.ts）

### 预计工作量
- **阶段 1**: 移动 11 个文件 + 更新 ~10 个引用 ≈ **2 小时**
- **阶段 2**: 移动 7 个文件 + 更新 ~30 个引用 ≈ **2 小时**
- **阶段 3**: 拆分 3 个文件 + 更新 ~10 个引用 ≈ **3 小时**
- **阶段 4**: 更新导出文件 ≈ **0.5 小时**
- **阶段 5**: 测试验证 ≈ **1 小时**

**总计**: 约 **8.5 小时**

---

## ✅ 验证清单

### 功能验证
- [ ] Admin 包编译通过
- [ ] Portal 包编译通过
- [ ] Shared 包编译通过
- [ ] 所有单元测试通过
- [ ] 手动测试关键功能（商品审核、用户管理、收藏、关注）

### 性能验证
- [ ] Shared 包体积减少（预计减少 30-40%）
- [ ] Admin 包首屏加载时间无明显增加
- [ ] Portal 包首屏加载时间无明显增加

### 代码质量验证
- [ ] 无 TypeScript 类型错误
- [ ] 无 ESLint 警告
- [ ] 所有 import 路径正确
- [ ] 无循环依赖

---

## 🚨 风险评估

### 高风险项
1. **import 路径批量修改** - 可能遗漏某些文件
   - **缓解措施**: 使用 grep/搜索工具全局检查

2. **动态 import** - 可能存在字符串形式的动态导入
   - **缓解措施**: 搜索 `import(` 和 `require(` 关键字

3. **测试文件遗漏** - `__tests__` 目录的引用可能被忽略
   - **缓解措施**: 同步更新测试文件的 import

### 中风险项
1. **类型定义丢失** - 移动后可能导致类型推导失败
   - **缓解措施**: 确保 `types.ts` 一起移动，并在 index.ts 中导出

2. **别名配置不生效** - tsconfig 配置可能不生效
   - **缓解措施**: 使用相对路径 `../services/xxx` 作为备选

### 低风险项
1. **构建缓存问题** - 可能需要清理缓存
   - **缓解措施**: 执行 `pnpm clean && pnpm install && pnpm build`

---

## 📝 执行步骤

### 准备阶段
```bash
# 1. 创建功能分支
git checkout -b refactor/services-layer

# 2. 备份当前代码
git add .
git commit -m "chore: 重构前备份"

# 3. 创建目标目录
mkdir -p frontend/packages/admin/src/services
mkdir -p frontend/packages/portal/src/services
```

### 执行阶段
```bash
# 阶段 1: 移动管理端服务
# (BaSui 可以自动化执行)

# 阶段 2: 移动客户端服务
# (BaSui 可以自动化执行)

# 阶段 3: 拆分混合服务
# (BaSui 可以自动化执行)

# 阶段 4: 更新导出文件
# (BaSui 可以自动化执行)

# 阶段 5: 测试验证
pnpm clean
pnpm install
pnpm build
pnpm test
```

### 验证阶段
```bash
# 1. 检查编译错误
pnpm --filter @campus/admin build
pnpm --filter @campus/portal build
pnpm --filter @campus/shared build

# 2. 运行测试
pnpm test

# 3. 检查包体积
pnpm --filter @campus/admin build --analyze
pnpm --filter @campus/portal build --analyze
```

---

## 🎉 预期收益

### 包体积优化
- **Shared 包**: 减少 ~30-40%（移除 18 个专属服务）
- **Admin 包**: 增加 ~15%（接收 11 个管理端服务）
- **Portal 包**: 增加 ~10%（接收 7 个客户端服务）

### 代码可维护性
- ✅ 职责清晰：每个包只包含自己需要的服务
- ✅ 减少耦合：Portal 不再依赖管理端服务
- ✅ 易于扩展：新功能直接加到对应包，不污染 shared

### 开发体验
- ✅ 更快的编译速度（shared 包变小）
- ✅ 更小的首屏加载（按需加载服务）
- ✅ 更清晰的依赖关系

---

## 🤔 待决策事项

### 1. 是否保留通用工具服务在 shared？
**建议**: 保留，但定期审查，发现单端使用的及时移走

### 2. 是否使用路径别名？
**建议**:
- 优先使用相对路径 `../services/xxx`（兼容性好）
- 可选配置别名 `@admin/services/xxx`（可读性好）

### 3. 是否一次性重构还是分步进行？
**建议**: 分步进行
- 第一步: 移动明确的专属服务（阶段 1 + 阶段 2）
- 第二步: 拆分混合服务（阶段 3）
- 第三步: 优化通用工具层（可选）

---

## 📞 联系 BaSui

如果你对这个计划有任何疑问或需要调整，请告诉我！

我可以：
- ✅ 自动化执行所有重构任务
- ✅ 逐步执行并实时反馈进度
- ✅ 提供更详细的某个阶段的执行细节

你决定吧，老铁！😎

---

**最后更新**: 2025-11-07
**状态**: 等待审批 ⏳
