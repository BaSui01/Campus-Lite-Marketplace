# 🌐 Phase 0 共享层梳理报告

> **目标**：盘点 `@campus/shared` 当前能力，识别管理端重构可复用资源与待抽象项，为 Phase 1 公共能力抽象奠定基础。  
> **范围**：`frontend/packages/shared`

---

## 1. 目录结构快照

```
src
├─ api            # OpenAPI 生成代码与 axios 配置（待统一入口）
├─ components     # 公共 UI 组件库（Button、Form、Table、Tabs 等）
├─ constants      # 全局常量与配置（Token 键、WebSocket 配置等）
├─ hooks          # 自定义 Hook（useAuth、useNotification、useWebSocket 等）
├─ services       # REST 服务封装（auth、user、goods、order、refund 等）
├─ types          # OpenAPI 类型定义 & 手工补充类型
└─ utils          # 工具函数（storage、apiClient、websocket、format 等）
```

---

## 2. 现有能力清单

### 2.1 UI 组件
- **基础组件**：Button、Input、Form、Modal、Tabs、Badge、Avatar 等，满足管理端大部分表单/展示需求。
- **业务组件**：GoodsCard、OrderCard、UserAvatar、ImageUpload、RichTextEditor 等，可在管理端配置页或内容审核场景复用。

### 2.2 自定义 Hook
- **认证与权限**：`useAuth`、`useAuthGuard`（需在 Phase 1 提炼为工厂方法，支持 admin/portal 共用）。
- **通知与消息**：`useNotification`、`useChatMessage`、`useOrderUpdate`、`useWebSocketService`，可直接服务于管理端的实时场景。
- **表单与分页**：`useForm`、`usePagination`、`useRequest` 提供了基础交互模式。

### 2.3 服务层
- **已提供**：auth、user、goods、order、refund、post、message、upload。
- **缺失/待补齐**：举报（reports）、角色权限（roles/permissions）、限流（rate-limit）、审计（audit）、通知模板（notification-template）等管理端专属接口。

### 2.4 工具与常量
- **存储工具**：`utils/storage` + `tokenStorage`/`userStorage`，可直接复用。
- **网络层**：`utils/apiClient`（axios 实例）、`utils/websocket`（新统一封装），后续需要将 admin 私有的 `request.ts` 切换为共享实现。
- **常量**：Token Key、WebSocket 配置已存在；权限编码仍散落在后端，需在 Phase 1 统一导出。

---

## 3. 管理端可直接复用项

- UI：Form、Table、Modal、Dropdown、Tabs、Badge 等组件组合即可搭建登录、仪表盘、用户列表页面。
- Hook：`useNotification` + `useWebSocketService` 直接支持管理端通知中心、订单实时更新。
- 服务：auth、user、goods、order、refund 可覆盖 Phase 2 用户管理与统计页面的核心数据需求。
- 工具：`tokenStorage`、`userStorage`、`apiClient`、`websocket` 支撑统一的鉴权与实时通信。

---

## 4. 待抽象与补齐任务（进入 Phase 1 的输入）

1. **认证 Store 抽象**  
   - 现状：portal 独享 `useAuthStore`。  
   - 行动：在 shared 新增 `createAuthStore({ scope, fields })` 工厂，支持传入权限字段、持久化键名。

2. **权限工具统一**  
   - 现状：admin 私有 `usePermission`、portal 直接在 store 中判断。  
  - 行动：在 shared 提供 `createPermissionHelpers(userSelector)`，输出 `hasPermission`、`hasAnyPermission`、`hasRole` 等方法。

3. **HTTP 客户端统一**  
   - 现状：admin 使用 `utils/request.ts`，portal 使用 shared `apiClient`。  
   - 行动：在 Phase 1 将 admin 请求层切换为 `apiClient` + `axiosInstance`，并补充拦截器配置（错误提示、Token 注入）。

4. **服务层补充**  
   - 新增 `reportService`、`roleService`、`rateLimitService`、`auditService` 等管理端接口封装，保持与后端契约一致。

5. **常量与类型同步**  
   - 收敛权限编码、角色枚举、菜单配置字段，添加到 shared `constants/permissions.ts`、`types/admin.ts`。

6. **脚手架/文档**  
   - 输出共享层使用指南（示例：如何接入 `createAuthStore`、如何使用 WebSocket 工具），便于团队成员迁移。

---

## 5. 风险与提醒

- **服务缺口**：管理端专属接口尚未统一封装，Phase 1 需要与后端确认最新契约。  
- **多端兼容**：shared 改动需同步验证 portal，以免影响现有用户端功能。  
- **WebSocket 依赖 Token**：需确保 `tokenStorage` 与 WebSocket 工具对接无缝，否则管理端实时功能会受阻。

---

## 6. 下一步（Phase 1 准备就绪）

- 在 shared 新建 `factories/` 目录，承载 `createAuthStore`、`createNotificationStore` 等工厂方法。  
- 编写 `MIGRATION_GUIDE.md` 条目，指导如何将旧的 admin 请求层、权限判断迁移到共享层实现。  
- 与后端确认管理端新增接口清单，生成或更新 OpenAPI 描述，确保 Phase 1 能直接对接。  
- 完成以上准备后，即可进入 Phase 1 的公共能力抽象与代码改造。

---

## 7. 阶段进展（2025-11-02）

- ✅ 新增 `factories/createAuthStore`，完成通用认证 Store 工厂并已通过包入口导出。  
- ✅ 补齐 `PERMISSION_CODES`、`ADMIN_DEFAULT_PERMISSIONS`，统一前后端权限编码来源。  
- ✅ 重建 `utils/http`，所有共享服务统一走 `apiClient`。  
- ✅ 新增管理端专属服务：statistics、report、role、rateLimit、notificationTemplate、compliance、softDelete、adminUser 等。  
- ✅ 管理端应用接入共享 store/服务，移除本地 `auth`、`statistics`、`request` 等重复实现。  
- ✅ 用户治理页面改用共享服务封禁/解封逻辑，与后端契约对齐。  
- ✅ shared 包新增 `zustand` 依赖，满足工厂方法运行需求。

---

> 📌 本报告将随 Phase 1-3 的推进持续更新，确保共享层能力与管理端需求保持对齐。
