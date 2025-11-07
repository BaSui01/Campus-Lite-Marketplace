# 🚀 API接口快速索引

> **版本**: v2.0 (100%覆盖率)
> **更新时间**: 2025-11-07
> **维护者**: BaSui 😎

---

## 📊 概览统计

```
✅ 接口覆盖率: 100% (96/96)
✅ 管理端接口: ~50 个
✅ 门户端接口: ~46 个
✅ 路径规范化: 完成
```

---

## 🔗 快速跳转

- 📋 [完整接口清单](./api接口.md) - 详细的接口列表、参数、返回值
- 📚 [接口分类文档](./API_INTERFACE_CLASSIFICATION.md) - 按功能分类、前端对接情况
- 🏗️ [后端接口与权限](./后端接口与权限一览表.md) - 权限配置参考

---

## ⚡ 管理端接口快速查找

### 路径前缀规则
所有管理端接口使用：`/api/admin/*`

### 核心模块

| 模块 | 路径前缀 | Controller | 文档链接 |
|-----|---------|-----------|---------|
| 用户管理 | `/admin/users` | AdminController | [查看详情](./api接口.md#1️⃣-用户管理模块) |
| 申诉管理 | `/admin/appeals` | AppealAdminController | [查看详情](./api接口.md#2️⃣-申诉管理模块) |
| 纠纷管理 | `/admin/disputes` | AdminDisputeController | [查看详情](./api接口.md#3️⃣-纠纷管理模块) |
| 统计分析 | `/admin/statistics` | AdminStatisticsController | [查看详情](./api接口.md#4️⃣-统计分析模块) |
| 角色权限 | `/admin/roles` | RoleAdminController | [查看详情](./api接口.md#5️⃣-角色权限管理模块) |
| 限流管理 | `/admin/rate-limit` | RateLimitAdminController | [查看详情](./api接口.md#6️⃣-限流管理模块) |
| 合规管理 | `/admin/compliance` | ComplianceAdminController | [查看详情](./api接口.md#7️⃣-合规管理模块) |
| 软删除管理 | `/admin/soft-delete` | SoftDeleteAdminController | [查看详情](./api接口.md#8️⃣-软删除管理模块) |
| 校园管理 | `/admin/campuses` | CampusController | [查看详情](./api接口.md#9️⃣-校园管理模块) |
| 分类管理 | `/admin/categories` | CategoryController | [查看详情](./api接口.md#🔟-分类管理模块) |
| 标签管理 | `/admin/tags` | TagController | [查看详情](./api接口.md#1️⃣1️⃣-标签管理模块) |
| 功能开关 | `/admin/feature-flags` | FeatureFlagController | [查看详情](./api接口.md#1️⃣2️⃣-功能开关管理模块) |
| 系统监控 | `/admin/monitor` | SystemMonitorController | [查看详情](./api接口.md#1️⃣3️⃣-系统监控模块) |
| 通知模板 | `/admin/notification-templates` | NotificationTemplateAdminController | [查看详情](./api接口.md#1️⃣4️⃣-通知模板管理模块) |
| 黑名单管理 | `/admin/blacklist` | BlacklistAdminController | [查看详情](./api接口.md#1️⃣5️⃣-黑名单管理模块) |

---

## 🌐 门户端接口快速查找

### 路径前缀规则
门户端接口直接使用：`/api/*`（无admin前缀）

| 模块 | 路径前缀 | Controller | 接口数 |
|-----|---------|-----------|--------|
| 认证 | `/auth` | AuthController | 5 |
| 用户 | `/users` | UserController | 4 |
| 商品 | `/goods` | GoodsController | 3 |
| 订单 | `/orders` | OrderController | 4 |
| 收藏 | `/favorites` | FavoriteController | 4 |
| 支付 | `/payment` | PaymentController | 3 |
| 黑名单（用户） | `/blacklist` | BlacklistController | 4 |
| 分类（公开） | `/categories` | CategoryController | 1 |
| 标签（公开） | `/tags` | TagController | 1 |

---

## 🆕 最近更新（2025-11-07）

### 路径修复
- ✅ FeatureFlagController: `/feature-flags` → `/admin/feature-flags`
- ✅ SystemMonitorController: `/monitor` → `/admin/monitor`

### 新增接口
- ✅ BlacklistAdminController: 管理员黑名单批量操作（6个新接口）
  - GET `/admin/blacklist` - 查询所有黑名单
  - GET `/admin/blacklist/by-user/{userId}` - 查询用户黑名单
  - GET `/admin/blacklist/blocked-by/{blockedUserId}` - 查询谁拉黑了用户
  - POST `/admin/blacklist/batch-unblock` - 批量解除黑名单
  - GET `/admin/blacklist/statistics` - 黑名单统计
  - GET `/admin/blacklist/check-relation` - 检查黑名单关系

### 前端同步
- ✅ `featureFlag.ts` - 8处路径更新
- ✅ `monitor.ts` - BASE_PATH更新

---

## 🔍 常用接口速查

### Dashboard 统计
```
GET /api/admin/statistics/overview    # 系统概览
GET /api/admin/statistics/trend       # 趋势数据
GET /api/admin/statistics/top-goods   # 热门商品
GET /api/admin/statistics/top-users   # 活跃用户
```

### 系统监控
```
GET /api/admin/monitor/health         # 健康检查
GET /api/admin/monitor/metrics        # 系统指标
GET /api/admin/monitor/api/statistics # API统计
GET /api/admin/monitor/report         # 性能报表
```

### 用户管理
```
POST /api/admin/users/ban             # 封禁用户
POST /api/admin/users/{userId}/unban  # 解封用户
POST /api/admin/users/auto-unban      # 自动解封过期用户
```

### 申诉管理
```
GET /api/admin/appeals                # 申诉列表
GET /api/admin/appeals/pending        # 待审核申诉
POST /api/admin/appeals/{id}/review   # 审核申诉
POST /api/admin/appeals/batch-review  # 批量审核
```

### 纠纷管理
```
GET /api/admin/disputes               # 纠纷列表
GET /api/admin/disputes/pending       # 待处理纠纷
POST /api/admin/disputes/{id}/claim   # 认领纠纷
POST /api/admin/disputes/{id}/arbitrate # 仲裁纠纷
```

---

## 📝 接口命名规范

### RESTful 规范
- `GET /resource` - 查询列表
- `GET /resource/{id}` - 查询详情
- `POST /resource` - 创建资源
- `PUT /resource/{id}` - 更新资源
- `DELETE /resource/{id}` - 删除资源

### 批量操作
- `POST /resource/batch-{action}` - 批量操作
- 示例：`POST /admin/appeals/batch-review`

### 统计查询
- `GET /resource/statistics` - 统计数据
- `GET /resource/count` - 数量统计

---

## 🛠️ 开发工具

### OpenAPI 文档生成
```bash
cd backend
mvn -P openapi openapi-generator:generate
```

### 前端 API 类型更新
```bash
cd frontend
pnpm api:generate
```

### Swagger 文档访问
```
开发环境: http://localhost:8200/swagger-ui.html
生产环境: https://api.example.com/swagger-ui.html
```

---

## 💡 最佳实践

### 前端调用
```typescript
// ✅ 正确：使用 OpenAPI 生成的服务
import { disputeService } from '@campus/shared/services';
const data = await disputeService.getList();

// ❌ 错误：直接使用 axios
const response = await axios.get('/api/admin/disputes');
```

### 权限检查
```typescript
// 前端权限守卫
<PermissionGuard permission={PERMISSION_CODES.SYSTEM_DISPUTE_MANAGE}>
  <DisputeList />
</PermissionGuard>
```

```java
// 后端权限注解
@PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_DISPUTE_MANAGE)")
```

---

## 📞 联系方式

- 📧 **技术支持**: 项目 Issues
- 📖 **详细文档**: [完整接口清单](./api接口.md)
- 🔧 **后端文档**: [Backend CLAUDE.md](../backend/CLAUDE.md)
- 🎨 **前端文档**: [Frontend CLAUDE.md](../frontend/CLAUDE.md)

---

**维护者**: BaSui 😎
**最后更新**: 2025-11-07
**版本**: v2.0
