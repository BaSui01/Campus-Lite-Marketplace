# 🚀 校园轻享集市 - 管理端开发规划（复用导向版）

> **文档版本**: v1.1.0  
> **修订说明**: 根据团队反馈，强调管理端对 `@campus/shared` 与用户端（portal）的复用策略，避免重复造轮子  
> **撰写日期**: 2025-11-02  
> **适用范围**: `frontend/packages/admin`

---

## ♻️ 一、现有资源复用全景

### 1.1 公共包 `@campus/shared` 复用要点

| 分类 | 主要资源 | 复用方式 | 必要改造 |
|------|----------|----------|-----------|
| 组件库 | `src/components/*`（Button、Form、Table、Modal、Tabs、UserAvatar 等） | 直接引入 `@campus/shared`，统一 UI 与交互 | 如需管理端特化样式，采用 `className` 扩展或局部样式覆盖，禁止复制组件源码 |
| Hooks | `src/hooks/useAuth`、`useNotification`、`usePagination`、`useForm` 等 | 以组合方式复用，优先通过参数定制行为 | 若存在管理端专属逻辑，先在 shared 中增加可选配置，再在 admin 包中调用 |
| 服务层 | `src/services/*.ts`（auth、user、goods、order、upload 等） | 作为 REST 调用入口，复用鉴权、错误处理 | 管理端新增接口优先补齐 shared 服务，确保 portal 与 admin 同步受益 |
| OpenAPI SDK | `src/api/DefaultApi` 及 `utils/http` | 直接调用统一的 axios 实例、拦截器、类型定义 | 若后端新增 `/api/admin/**`，更新 OpenAPI 描述后统一生成 |
| 工具函数 | `src/utils/storage`、`format`、`validator`、`highlight` | 统一处理本地存储、格式化、校验逻辑 | 管理端需要的 Token/用户信息字段如果不同，可在 shared 常量中增加命名空间配置 |
| 常量定义 | `src/constants/config`、`permissionCodes`、`statusMap` | 共享权限编码、状态枚举，避免硬编码 | 若新增权限，先补充 shared 常量再落地页面权限判断 |

> 📌 结论：管理端禁止自建重复的组件、服务、工具。优先在 shared 中补齐能力，再由 admin、portal 共同消费。

### 1.2 用户端 `@campus/portal` 可复用能力

| 资源 | 位置 | 管理端复用姿势 | 说明 |
|------|------|----------------|------|
| 认证状态管理 | `src/store/useAuthStore.ts` | 抽离为 shared 层的 `createAuthStore(appScope)` 工厂，支持 portal/admin 共用 | 管理端比用户端多「权限集」字段，可通过扩展类型泛化 |
| Token 存储工具 | `src/store/useAuthStore.ts` + `@campus/shared` storage | 直接沿用现有持久化策略，保持跨端一致的登录体验 | 仅需为管理端补充权限数组的序列化与恢复 |
| 登录/注册页面交互 | `src/pages/Login`、`src/pages/Register` | 复制 UI 布局思路，换用 Ant Design + shared Form 控件 | 表单校验规则、错误提示、成功跳转逻辑可直接复用 |
| 布局骨架 | `src/layouts/MainLayout` | 迁移布局结构（顶部导航、侧边栏、内容区），替换为管理端菜单数据 | 导航组件继续复用 `Dropdown`、`UserAvatar` 等 shared 组件 |
| 通知与消息 | `src/store/useNotificationStore.ts`、`@campus/shared` 的 `useNotification` | 同步复用 WebSocket 订阅与 Toast 体系 | 管理端只需替换频道名称与通知列表路由 |
| 路由守卫 | `src/router`（守卫逻辑） | 复用权限校验模式，在 admin 中按权限编码做导航守卫 | 管理端需额外处理 403 兜底与多角色切换 |

### 1.3 复用落地三步曲

1. **盘点并抽象**：梳理 shared 与 portal 已有能力，标记必须上升到 shared 层的逻辑（如 Auth Store 工厂、权限常量）。  
2. **模块化提取**：在 shared 新增可配置的导出，保证 portal 不受破坏后再迁移 admin。  
3. **双端回归验证**：每次抽取后同时跑通 portal 与 admin 的关键流程，确保统一能力不回归。

---

## 🏛️ 二、前端架构与分层策略

```
frontend
├─ packages
│  ├─ shared        # 公共组件、hooks、服务、类型
│  ├─ portal        # 用户端（已上线）
│  └─ admin         # 管理端（当前开发重点）
```

1. **应用壳（App Shell）**：admin 仅保留路由、权限守卫、菜单配置等轻量逻辑；所有可共享的业务能力尽量放入 shared。  
2. **模块分层**：`modules/*` 与 `pages/*` 只负责组合 shared 能力与页面布局，不承载底层实现。  
3. **状态管理**：统一使用 `createAuthStore`、`createNotificationStore` 等工厂方法，避免多个包之间的 Zustand Store 复制。  
4. **网络层**：所有请求经由 shared 的 `axiosInstance` 与统一拦截器，确保 Token 续签、错误提示一致。  
5. **样式主题**：保留 Ant Design 主题自定义，但公共基础色值、字号通过 shared 的 design token（后续补齐）统一管理。

---

## 🎯 三、核心功能模块方案

### 3.1 认证与权限体系

| 方案 | 核心思路 | 优点 | 成本 / 风险 | 适用判定 |
|------|----------|------|-------------|-----------|
| 方案 A：共享化 Store | 将 portal 的 `useAuthStore` 抽象为 `createAuthStore({ scope })`，shared 导出；admin 传入 `scope: 'admin'` | 代码共同维护、逻辑一致、权限校验统一 | 需要改造 portal，引入 scope 概念，测试量较大 | 同步迭代 portal，应付后续多端场景 |
| 方案 B：轻量封装 | admin 新建 `useAdminAuthStore`，内部仍调用 shared 的 `authService`、`tokenStorage` | 实现最快，风险最小 | 仍存在重复的权限方法、持久化逻辑 | 管理端抢先交付、后续再统一 |

> ✅ **推荐**：优先落地方案 A，在 shared 中新增 `createAuthStore` 与 `createPermissionGuard`，一次性解决多端复用问题。若排期紧，可先实现方案 B，后续 Phase 3 再回收。

**落地步骤**
1. 在 `@campus/shared` 添加 `createAuthStore(options)`（泛型化用户信息、权限字段）。  
2. portal 与 admin 同时改为调用新工厂，验证登录/登出、权限守卫是否一致。  
3. admin 补充权限菜单：`hasPermission` 判断使用 shared 常量，缺失的权限编码需同步补齐。

### 3.2 布局与导航

- **导航骨架**：复用 portal `MainLayout` 的结构，拆分为 `Header`、`Sidebar`、`Content` 三个子组件，组件内部使用 shared 的 `Dropdown`、`UserAvatar`、`Badge`。  
- **菜单数据**：通过配置文件驱动（建议放置 `src/config/menu.ts`），菜单项绑定权限编码，复用 shared 的权限工具做显示控制。  
- **主题切换**：沿用 portal 的明暗主题切换逻辑（若已实现），将主题状态移动到 shared 的 hooks，admin 仅负责 UI。  
- **响应式处理**：继承 portal 的移动端适配经验，确保管理端在窄屏下自动折叠菜单。

### 3.3 数据可视化与仪表盘

1. **统计卡片**：基于 shared 的 `Card`、`Skeleton`、`Badge` 组合实现，无需重复造 `StatCard`。  
2. **图表库**：portal 若已接入 ECharts/Recharts，则提炼公共 Chart 包装组件至 shared（如 `ChartLine`、`ChartBar`），admin 直接复用。  
3. **数据请求**：统一通过 shared 的服务，例如 `statisticsService.getOverview()`（若缺失则新增到 shared），并在 admin 内使用 `useQuery`。  
4. **权限控制**：仪表盘所有模块以 `SYSTEM_STATISTICS_VIEW` 权限为开关，复用 shared 的权限枚举。

### 3.4 管理业务模块复用策略

| 模块 | 对应后端接口 | 复用资源 | 补充工作 |
|------|--------------|----------|----------|
| 用户管理 | `/api/admin/users/**`、`/api/users/**` | `userService`（列表、封禁、解封）、shared `Table`/`Form`、portal 的用户详情抽屉交互 | 若接口路径为 admin 独有，先在 shared 中扩展 `userService`；封禁原因、角色分配等交互直接复用 portal 表单逻辑 |
| 举报处理 | `/api/reports/**` | 新增 `reportService` 至 shared，列表表格重用 shared `usePagination` | Portal 里的举报提交表单可复用字段定义，管理端增加审核操作按钮 |
| 角色与权限 | `/api/admin/roles/**`、`/api/admin/permission-codes` | shared 常量 `permissionCodes`、`Form`、`Tree` 组件 | 提前把权限树数据放进 shared 的常量，便于 portal 后续展示 |
| 限流管理 | `/api/admin/rate-limit/**` | 在 shared 新增 `rateLimitService`，使用 shared 的 `Form` + `Switch` 控件 | portal 暂无此功能，需新增服务但依旧放在 shared，保证接口类型统一 |
| 回收站 | `/api/admin/soft-delete/**` | 复用 shared 的 `Table`、`Modal`、批量操作模式 | 若后端返回结构与 portal 的 `softDeleteService` 共通，可直接复用；否则在 shared 调整类型定义 |
| 合规/审计/通知模板 | `/api/admin/compliance/**` 等 | shared 的 `Modal`、`RichTextEditor`、`ImageUpload` | 复用 portal 的模板编辑体验（如富文本、上传组件），保留同一校验逻辑 |

---

## 🔄 四、前端重构总体策略

### 4.1 重构目标定位

- 聚焦 `frontend/packages/admin`，以“复用共享层 + 渐进式重构”为核心原则。  
- 目标是替换掉旧有的管理端页面实现，统一技术栈（React + Ant Design + Zustand + React Query），同时最大化利用 `@campus/shared` 与 portal 已有能力。  
- 保持后端接口不做大改动，通过前端架构调整提升可维护性、可扩展性与交互一致性。

### 4.2 重构方案对比

| 方案 | 范围 | 优点 | 风险/成本 | 适用场景 |
|------|------|------|-----------|-----------|
| 方案 A：完全重写 | 全面推倒旧管理端代码，按新架构从零搭建 | 架构最干净，便于统一规范 | 工期长、上线风险高，需要一次性交付大量页面 | 旧版本难以维护或功能缺失严重 |
| 方案 B：渐进式重构（推荐） | 逐模块迁移到新架构（先基础设施，再核心模块，再高级模块） | 可平滑迭代、边迁移边验证、复用成本最低 | 需要双栈并行一段时间，需管理好上下游依赖 | 现有版本可维持，重构目标是提升质量与体验 |
| 方案 C：微前端拆分 | 使用微前端框架保留旧模块，逐步替换 | 影响范围可控，可同时运行新旧模块 | 引入额外的运行时复杂度，需要脚手架支撑 | 管理端需要长期保持兼容多个技术栈时考虑 |

> ✅ **结论**：优先执行方案 B。按模块化计划逐步替换旧页面，同时保证新旧版本的路由、数据、权限逻辑一致。

### 4.3 渐进式前端重构步骤

1. **基础设施先行**：先落地 shared 抽象（Auth Store、权限工具、公共服务），以及新的布局与路由守卫，使新旧页面共享同一套底层能力。  
2. **模块迁移节奏**：按照 Phase 1-3 的顺序迁移登录/导航、用户管理、统计与举报、角色与限流等模块，完成一个模块即下线旧实现。  
3. **界面一致性校验**：迁移过程中对照 portal 的交互规范，统一按钮、表单、提示语的视觉与文案。  
4. **前后端契约校验**：每次迁移前确认接口契约是否满足需求；若接口缺失，再与后端协作补齐或使用组合调用。  
5. **双版本回归**：在旧版未完全下线前，保留关键路由入口备份，确保发生问题时可快速回退；完成整模块迁移后再删除老代码。  
6. **文档与脚本更新**：同步更新开发文档、环境配置、运行脚本，确保团队成员能快速切换到新架构开发。

### 4.4 技术治理与质量保障

- **组件复用度评估**：每完成一个模块，统计复用 shared 组件/Hooks/服务的覆盖率，持续优化重复代码。  
- **性能与可用性测试**：针对关键页面（仪表盘、用户列表）进行性能 Profiling，确保新版本不退化；移动端/小屏兼容保持 portal 同标准。  
- **可观察性**：在管理端加入基础的埋点或错误监控（如 Sentry），验证重构后稳定性。  
- **知识沉淀**：将重构过程中沉淀的最佳实践记录在团队 Wiki，便于后续迭代与新成员上手。

---

## 📅 四、迭代阶段划分（含复用任务）

| 阶段 | 用时 | 目标 | 关键产物 |
|------|------|------|----------|
| Phase 0：能力梳理 | 0.5 天 | 列出 shared、portal 可复用模块，确定抽象方案 | 复用清单、抽象计划、改造 Issue 列表 |
| Phase 1：基础设施 | 1 天 | 完成 `createAuthStore`、菜单配置、权限守卫、全局布局 | 登录流程、导航骨架、权限拦截中间件 |
| Phase 2：核心模块 | 2 天 | 用户管理、举报处理、仪表盘统计 | 复用 shared 服务的页面 + 列表操作 + 图表 |
| Phase 3：高级功能 | 1.5 天 | 角色权限、限流管理、回收站、通知模板 | 角色权限树、限流配置表单、回收站批量恢复 |
| Phase 4：测试与优化 | 1 天 | 跨端回归、性能优化、文档补齐、交互润色 | 单元/接口联调记录、性能指标、组件复用示例 |

> 每个阶段完成后需要同步验证 portal 是否受影响，必要时在 shared 发布预发布版本供双端联调。

---

## ⚠️ 五、风险评估与对策

| 风险 | 影响 | 应对策略 |
|------|------|-----------|
| shared 改造影响 portal | 用户端潜在回归 | 所有 shared 改动先走 PR，portal 与 admin 同时执行关键用例（登录、发帖、下单） |
| 管理端独有接口无法直接复用 | 需要新建服务、类型 | 优先在 shared 增加 `admin` 前缀的服务类，保持统一 http 客户端 |
| 权限编码不一致 | 页面权限控制失效 | 建立单一来源：所有权限常量维护在 shared，admin/portal 只引用 |
| UI 主题割裂 | 双端风格不统一 | 统一在 shared 定义主题 Token，admin 仅覆盖必要的布局样式 |
| 迭代节奏紧，方案 A 来不及落地 | 认证模块重复实现 | 先实施方案 B 快速上线，并创建技术债任务，在 Phase 4 归并 |

---

## ✅ 六、开发规范与最佳实践（复用版）

1. **优先搜索再编码**：在实现任何模块前，先在 shared/portal 中搜索是否已有类似实现。  
2. **复用驱动**：新增能力默认写在 shared，若暂时只服务 admin，需在文档中标注后续迁移计划。  
3. **中文注释与文档**：所有新增注释、接口文档、README 更新均需使用中文，遵循全局约定。  
4. **组件组合优先**：允许在 admin 中组合 shared 组件形成复合组件，但禁止复制 shared 源码进行修改。  
5. **权限校验统一**：页面入口、路由守卫、操作按钮的权限检查统一调用 shared 提供的方法，禁止硬编码字符串。  
6. **测试同步**：每个阶段至少执行一次 portal + admin 的冒烟流程，保证共享代码质量。

---

## 🧭 七、下一步行动清单

- [ ] 在 `@campus/shared` 创建 `createAuthStore` 与权限工具，portal/admin 双端接入  
- [ ] 补全 shared `services` 中的管理端接口（report、rateLimit、softDelete 等）  
- [ ] 拆分 portal `MainLayout`，抽出可复用的导航与头像组件  
- [ ] 整理权限常量与菜单配置，形成单一来源  
- [ ] 完成阶段性自测清单：登录、权限拦截、仪表盘、用户操作、角色配置、限流开关、回收站恢复

---

**结语**：本方案已转向“以复用为先”的交付模式。请在开发过程中持续检查 shared / portal 的现有实现，确保管理端不再重复造轮子，并在每次抽象后同步验证双端行为。只有建立统一的共享层，才能支撑校园轻享集市的多端协同与长期演进。💪
