# 前端 Monorepo 架构交付报告（更新于 2025-10-31）

> **作者**: BaSui 😎  
> **状态**: ✅ 架构基线稳定，可进入功能开发阶段

---

## 1. 交付概览
- Monorepo 基于 pnpm workspace 搭建完成，含 `@campus/shared`、`@campus/portal`、`@campus/admin` 三个包。
- 公共层已内置组件库（18 个基础组件）、10 大领域服务、13 个高频 Hook、OpenAPI 客户端、工具与类型定义。
- Portal 完成路由/状态/WebSocket/布局骨架，可直接接入真实接口开发页面。
- Admin 完成 React + Vite + Ant Design 脚手架，与共享层联通，等待业务模块落地。
- 根目录脚本提供开发、构建、Lint、类型检查、API 生成等全套流程。

---

## 2. 关键能力明细
### 2.1 公共层（@campus/shared）
| 分类 | 代表实现 | 说明 |
| ---- | -------- | ---- |
| 组件 | Button、Card、Table、ImageUpload、RichTextEditor、Toast 等 | 覆盖表单、列表、上传、富文本、通知等场景，可在 Portal/Admin 直接复用 |
| Hook | `useAuth`、`useRequest`、`useWebSocketService`、`useUpload`、`useNotification` 等 | 封装登录态恢复、请求节流、消息订阅、上传进度、全局提示 |
| 服务 | `auth.ts`、`goods.ts`、`order.ts`、`post.ts`、`message.ts`、`websocket.ts` 等 | 对 REST / WebSocket 进行二次封装，统一错误处理、重试与缓存 key |
| 工具 | `http.ts`、`storage.ts`、`validator.ts`、`format.ts` | Axios 拦截器、Token & Refresh 管理、输入校验、日期/货币格式化 |
| 常量/类型 | `constants/config.ts`、`types/api.ts`、`types/entity.ts` | 与后端枚举、DTO 对齐，辅助 IDE 智能提示 |
| OpenAPI | `api/` 目录 | 由 `pnpm run api:generate` 自动生成，保证接口契约一致性 |

### 2.2 Portal（@campus/portal）
- 路由：`createBrowserRouter` + 惰性加载 + `RequireAuth` 守卫，已配置 404 页面。
- 状态：`useAuthStore`（Token 持久化 / 刷新）、`useNotificationStore`（Toast 队列）。
- WebSocket：应用启动即调用 `useWebSocketService` 建立长连接，日志反馈连接状态。
- 页面：`pages/*` 目录准备完毕，待填充 UI 与接口。
- 样式：Tailwind + 全局 CSS，默认响应式栅格已调优。

### 2.3 Admin（@campus/admin）
- 脚手架：React 18 + Vite + Ant Design + React Query + Zustand。
- 待办：接入共享组件、搭建布局/导航、实现模块化路由与权限守卫。

---

## 3. 工具链与脚本
| 命令 | 作用 |
| ---- | ---- |
| `pnpm install` | 安装全部依赖（根目录执行） |
| `pnpm run dev:portal` / `pnpm run dev:admin` | 启动开发服务器（HMR） |
| `pnpm run build:all` | 构建 shared + portal + admin |
| `pnpm -r lint` / `pnpm -r type-check` | 多包 Lint / TypeScript 检查 |
| `pnpm run api:generate` | 从后端导出 OpenAPI 并更新客户端 |

> 所有脚本在 CI 中均可复用，建议在 PR 合并前手动执行一次 `lint + type-check + build`。

---

## 4. 与后端的契约
- 后端 `pom.xml` 配置已更新至 `target/openapi-frontend.json` 导出路径，`SecurityConfig`、`PermissionCodes` 等枚举通过共享层常量对齐。
- `application.yml` 内的 API 网关、WebSocket 路径、支付回调等均在共享常量中预设，保持“约定优于配置”。
- Dev/Prod 环境切换可通过 `.env` 注入 `VITE_API_BASE_URL`（参考 `constants/config.ts`）。

---

## 5. 悬而未决事项
| 类型 | 描述 | 建议时间 |
| ---- | ---- | -------- |
| 功能 | Portal 页面仍为占位，需要按策略文档（docs/前端开发策略.md）实现 UI 与接口联调 | 2025-11 起 |
| 功能 | Admin 仅有脚手架，需要补齐仪表盘/审核等核心模块 | 2025-12 起 |
| 测试 | 组件与服务单元测试尚未编写 | 与功能开发同步 |
| 文档 | 共享层组件 Storybook / 示例页待补充 | Stage 1 |
| 自动化 | Cypress/Playwright E2E 测试尚未搭建 | Stage 4 |

---

## 6. 下一步建议
1. 按 `docs/前端开发策略.md` 的阶段划分执行迭代，保证每个阶段都有可运行成果。
2. 在 shared 中补充测试与 Storybook，减少 Portal/Admin 的样式工作量。
3. 约定 API 迭代流程：后端改动 → 导出 OpenAPI → `pnpm run api:generate` → `pnpm -r type-check`。
4. 将 `pnpm run build:all` 集成进 CI，阻止有问题的变更进入主干。
5. 规划埋点/监控需求，避免上线后缺乏观测手段。

---

## 7. 验收结论
- ✅ 架构层面已满足「共享 → Portal → Admin」三层复用与解耦需求。
- ✅ 公共层输出完整 API/组件/工具，为业务开发提供可靠基座。
- ✅ 依赖与脚本齐备，可支撑后续自动化流程。
- ⏳ 业务页面尚未落地，需要按照策略文档逐步实现。

> 当前 Monorepo 架构已具备可持续演进能力，建议立即投入业务模块开发；若需支持小程序或微前端，可在共享层基础上追加适配层，无需推倒重来。
