# 项目结构与命名规范

> **校园轻享集市系统** - 全局结构标准文档  
> **架构**: Spring Boot 单体 + 前后端分离 | 分层清晰 | 模块化设计  
> **更新**: 2025-11-03

---

## 📋 目录

- [项目整体结构](#项目整体结构)
- [后端项目结构](#后端项目结构)
- [前端项目结构](#前端项目结构)
- [命名规范](#命名规范)
- [目录组织原则](#目录组织原则)
- [文件组织规范](#文件组织规范)

---

## 📁 项目整体结构

```
campus-lite-marketplace/           # 项目根目录
├── backend/                       # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/             # Java 源码
│   │   │   └── resources/        # 配置文件
│   │   └── test/                 # 测试代码
│   ├── pom.xml                   # Maven 配置
│   └── README.md                 # 后端说明
│
├── frontend/                      # 前端 Monorepo
│   ├── packages/
│   │   ├── admin/                # 管理端应用
│   │   ├── portal/               # 用户端应用
│   │   └── shared/               # 共享层
│   ├── package.json              # 根配置
│   ├── pnpm-workspace.yaml       # Workspace 配置
│   └── README.md                 # 前端说明
│
├── docs/                          # 项目文档
│   ├── specs/                    # Specs 规范文档
│   │   ├── tech.md              # 技术栈规范（本文档姊妹篇）
│   │   ├── structure.md         # 结构规范（本文档）
│   │   └── N/                   # 功能规范目录（N为编号）
│   │       ├── requirements.md  # 需求文档
│   │       ├── design.md        # 设计文档
│   │       └── tasks.md         # 任务分解
│   └── *.md                     # 其他文档
│
├── docker/                        # Docker 相关配置
├── scripts/                       # 构建/部署脚本
├── .env.example                  # 环境变量模板
├── .gitignore                    # Git 忽略文件
├── CLAUDE.md                     # AI 开发规范
└── README.md                     # 项目说明
```

---

## 🔧 后端项目结构

### 主目录结构

```
backend/src/main/java/com/campus/marketplace/
├── common/                        # 🌟 通用层（所有业务共用）
│   ├── annotation/               # 自定义注解
│   ├── aspect/                   # AOP 切面
│   ├── component/                # 通用组件
│   ├── config/                   # 配置类
│   ├── context/                  # 上下文（ThreadLocal等）
│   ├── dto/                      # 通用 DTO
│   ├── entity/                   # 基础实体
│   ├── enums/                    # 枚举类
│   ├── exception/                # 异常定义
│   ├── lock/                     # 分布式锁
│   ├── mail/                     # 邮件服务
│   ├── schedule/                 # 定时任务
│   ├── security/                 # 安全配置
│   ├── support/                  # 支持类（BaseService等）
│   ├── utils/                    # 工具类
│   └── web/                      # Web 通用（拦截器、过滤器）
│
├── controller/                    # 🎮 控制器层（API 接口）
│   ├── AuthController.java
│   ├── UserController.java
│   ├── GoodsController.java
│   ├── OrderController.java
│   └── ...Controller.java
│
├── service/                       # 💼 服务层（业务逻辑）
│   ├── AuthService.java          # 接口定义
│   ├── UserService.java
│   ├── impl/                     # 实现类子目录
│   │   ├── AuthServiceImpl.java
│   │   ├── UserServiceImpl.java
│   │   └── ...ServiceImpl.java
│   └── ...Service.java
│
├── repository/                    # 🗄️ 数据访问层（JPA Repository）
│   ├── UserRepository.java
│   ├── GoodsRepository.java
│   └── ...Repository.java
│
├── revert/                        # 🔄 功能模块：撤销功能（示例）
│   ├── strategy/                 # 撤销策略
│   │   ├── RevertStrategy.java  # 策略接口
│   │   └── impl/                # 策略实现
│   ├── RevertStrategyFactory.java
│   └── ...
│
├── event/                         # 📢 事件处理
│   ├── listener/                 # 事件监听器
│   └── publisher/                # 事件发布器
│
├── task/                          # ⏰ 定时任务
│   └── ...Task.java
│
├── websocket/                     # 🔌 WebSocket
│   ├── handler/
│   └── config/
│
└── MarketplaceApplication.java    # 🚀 启动类
```

### 资源文件结构

```
backend/src/main/resources/
├── application.yml                # 主配置文件
├── application-dev.yml            # 开发环境
├── application-test.yml           # 测试环境
├── application-prod.yml           # 生产环境
├── db/migration/                  # Flyway 数据库迁移
│   ├── V1__init_schema.sql
│   ├── V2__add_user_table.sql
│   └── V{N}__{description}.sql
├── static/                        # 静态资源
└── templates/                     # 邮件模板等
```

### 测试目录结构

```
backend/src/test/java/com/campus/marketplace/
├── controller/                    # Controller 测试
│   └── UserControllerTest.java
├── service/                       # Service 测试
│   ├── UserServiceTest.java
│   └── impl/
│       └── UserServiceImplTest.java
├── repository/                    # Repository 测试
│   └── UserRepositoryTest.java
├── integration/                   # 集成测试（*IT.java）
│   ├── UserRegistrationIT.java
│   └── OrderFlowIT.java
├── enums/                        # 枚举测试
└── ...
```

---

## 🎨 前端项目结构

### Monorepo 整体结构

```
frontend/
├── packages/
│   ├── admin/                     # 📊 管理端应用
│   │   ├── src/
│   │   ├── public/
│   │   ├── package.json
│   │   └── vite.config.ts
│   │
│   ├── portal/                    # 🏪 用户端应用
│   │   ├── src/
│   │   ├── public/
│   │   ├── package.json
│   │   └── vite.config.ts
│   │
│   └── shared/                    # 🔗 共享层
│       ├── src/
│       ├── package.json
│       └── tsup.config.ts
│
├── package.json                   # 根 package.json
├── pnpm-workspace.yaml            # pnpm workspace 配置
├── .prettierrc                    # Prettier 配置
└── tsconfig.json                  # 根 TypeScript 配置
```

### 管理端 (admin) 结构

```
packages/admin/src/
├── api/                           # API 请求封装（可选，复用shared）
├── assets/                        # 静态资源
│   ├── images/
│   ├── styles/
│   └── icons/
├── components/                    # 业务组件
│   ├── Layout/                   # 布局组件
│   ├── Header/
│   ├── Sidebar/
│   └── ...
├── pages/                         # 页面组件
│   ├── Dashboard/
│   ├── User/
│   ├── Goods/
│   ├── Order/
│   ├── System/                   # 系统管理
│   │   ├── RevertManagement/    # 撤销管理
│   │   └── index.ts
│   └── index.ts
├── config/                        # 配置文件
│   ├── menu.ts                   # 菜单配置
│   └── routes.ts                 # 路由配置（可选）
├── router/                        # 路由定义
│   └── index.tsx
├── store/                         # 状态管理（Zustand/Jotai）
│   ├── userStore.ts
│   ├── globalStore.ts
│   └── ...
├── hooks/                         # 自定义 Hooks
│   ├── useAuth.ts
│   └── ...
├── utils/                         # 工具函数（业务相关）
│   └── ...
├── types/                         # TypeScript 类型（业务相关）
│   └── ...
├── App.tsx                        # 应用根组件
├── main.tsx                       # 应用入口
└── vite-env.d.ts                 # Vite 类型声明
```

### 用户端 (portal) 结构

```
packages/portal/src/
├── assets/                        # 静态资源
├── components/                    # 业务组件
│   ├── Layout/
│   ├── ProductCard/
│   └── ...
├── pages/                         # 页面组件
│   ├── Home/
│   ├── ProductDetail/
│   ├── Cart/
│   ├── Order/
│   ├── Profile/
│   ├── RevertOperations/         # 撤销操作页面
│   └── index.ts
├── router/                        # 路由定义
│   └── index.tsx
├── store/                         # 状态管理（Zustand）
├── hooks/                         # 自定义 Hooks
├── utils/                         # 工具函数
├── types/                         # TypeScript 类型
├── styles/                        # 全局样式（Tailwind）
│   └── globals.css
├── App.tsx
└── main.tsx
```

### 共享层 (shared) 结构

```
packages/shared/src/
├── api/                           # 🌐 API 层（OpenAPI自动生成）
│   ├── api/                      # API 请求方法
│   │   ├── auth-api.ts
│   │   ├── user-api.ts
│   │   └── ...
│   ├── models/                   # DTO 类型定义
│   │   ├── user-dto.ts
│   │   └── ...
│   ├── base.ts                   # Axios 基础配置
│   └── index.ts
│
├── components/                    # 🧩 共享组件
│   ├── RevertOperationsList/    # 撤销操作列表
│   ├── RevertPreviewModal/       # 撤销预览弹窗
│   └── index.ts
│
├── services/                      # 🔧 业务服务层（二次封装API）
│   ├── revert.ts                # 撤销服务
│   ├── auth.ts                  # 认证服务
│   └── index.ts
│
├── types/                         # 📝 共享类型定义
│   ├── revert.ts                # 撤销相关类型
│   ├── common.ts                # 通用类型
│   └── index.ts
│
├── utils/                         # 🛠️ 工具函数
│   ├── request.ts               # Axios 封装
│   ├── storage.ts               # LocalStorage 封装
│   ├── format.ts                # 格式化工具
│   └── index.ts
│
├── hooks/                         # 🪝 共享 Hooks
│   ├── useAuth.ts
│   └── index.ts
│
├── constants/                     # 📌 常量定义
│   ├── api.ts                   # API 常量
│   └── index.ts
│
└── index.ts                       # 统一导出
```

---

## 📝 命名规范

### 后端命名规范

#### Java 类命名

| 类型 | 命名规则 | 示例 | 说明 |
|------|---------|------|------|
| **Entity** | 名词 | `User`, `Goods`, `Order` | 单数形式 |
| **DTO** | 名词 + DTO | `UserDTO`, `CreateOrderDTO` | 区分输入/输出：`CreateXxxDTO`, `XxxResponseDTO` |
| **Controller** | 名词 + Controller | `UserController` | RESTful 风格 |
| **Service** | 名词 + Service | `UserService` | 接口定义 |
| **ServiceImpl** | 名词 + ServiceImpl | `UserServiceImpl` | 实现类 |
| **Repository** | 名词 + Repository | `UserRepository` | 单数名词 |
| **Enum** | 名词 | `OrderStatus`, `UserRole` | 单数形式 |
| **Exception** | 名词 + Exception | `UserNotFoundException` | 业务异常 |
| **Utils** | 名词 + Utils | `StringUtils`, `DateUtils` | 复数形式 |
| **Config** | 名词 + Config | `RedisConfig`, `SecurityConfig` | 配置类 |
| **Aspect** | 名词 + Aspect | `LoggingAspect` | 切面类 |

#### 方法命名

| 类型 | 命名规则 | 示例 | 说明 |
|------|---------|------|------|
| **Controller** | HTTP动词 + 资源 | `getUser()`, `createOrder()` | RESTful 风格 |
| **Service** | 动词 + 名词 | `findById()`, `saveUser()`, `deleteOrder()` | 业务语义 |
| **Repository** | 动词 + By + 条件 | `findByUsername()`, `existsByEmail()` | JPA 规范 |
| **Boolean方法** | is/has/can + 形容词 | `isValid()`, `hasPermission()` | 返回布尔值 |

#### 变量命名

```java
// ✅ 正确：驼峰命名
private String userName;
private List<Order> orderList;
private UserService userService;

// ❌ 错误：下划线命名
private String user_name;

// ✅ 常量：全大写 + 下划线
public static final String DEFAULT_ROLE = "USER";
public static final int MAX_PAGE_SIZE = 100;
```

#### 包命名

```java
// ✅ 正确：全小写，层级清晰
com.campus.marketplace.controller
com.campus.marketplace.service.impl
com.campus.marketplace.common.enums

// ❌ 错误：驼峰或大写
com.campus.marketplace.Controller
com.campus.marketplace.Service.Impl
```

### 前端命名规范

#### 文件命名

| 文件类型 | 命名规则 | 示例 | 说明 |
|---------|---------|------|------|
| **组件** | PascalCase | `UserCard.tsx`, `ProductList.tsx` | 大驼峰 |
| **页面** | PascalCase | `Dashboard.tsx`, `UserProfile.tsx` | 大驼峰 |
| **Hooks** | camelCase (use开头) | `useAuth.ts`, `useDebounce.ts` | 小驼峰 |
| **工具** | camelCase | `request.ts`, `format.ts` | 小驼峰 |
| **类型** | camelCase | `user.ts`, `order.ts` | 小驼峰（或PascalCase） |
| **常量** | camelCase | `constants.ts`, `api.ts` | 小驼峰 |
| **配置** | camelCase | `menu.ts`, `routes.ts` | 小驼峰 |
| **样式** | camelCase | `index.css`, `globals.css` | 小驼峰 |

#### 组件命名

```tsx
// ✅ 正确：PascalCase
export const UserCard: React.FC<UserCardProps> = (props) => { ... }
export default function ProductList() { ... }

// ❌ 错误：camelCase
export const userCard = () => { ... }
```

#### 变量/函数命名

```typescript
// ✅ 正确：camelCase
const userName = 'Alice';
const orderList = [];
function fetchUserData() { ... }

// ✅ Boolean 变量：is/has/can 前缀
const isLoading = true;
const hasPermission = false;
const canEdit = true;

// ✅ 事件处理：handle 前缀
const handleClick = () => { ... }
const handleSubmit = () => { ... }

// ✅ 常量：UPPER_SNAKE_CASE
const API_BASE_URL = '/api';
const MAX_RETRY_COUNT = 3;
```

#### TypeScript 类型命名

```typescript
// ✅ Interface：PascalCase（I前缀可选）
interface UserProps { ... }
interface IUserService { ... }  // 可选I前缀

// ✅ Type：PascalCase
type OrderStatus = 'pending' | 'completed';
type ApiResponse<T> = { data: T; code: number };

// ✅ Enum：PascalCase
enum UserRole {
  ADMIN = 'ADMIN',
  USER = 'USER',
}
```

### 数据库命名规范

#### 表命名

```sql
-- ✅ 正确：全小写 + 下划线
CREATE TABLE users (...);
CREATE TABLE order_items (...);
CREATE TABLE user_addresses (...);

-- ❌ 错误：驼峰或大写
CREATE TABLE UserAddress (...);
```

#### 字段命名

```sql
-- ✅ 正确：全小写 + 下划线
user_id, created_at, order_status

-- ❌ 错误：驼峰
userId, createdAt
```

#### 索引命名

```sql
-- ✅ 命名规则：idx_{table}_{column}
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_orders_user_id ON orders(user_id);

-- ✅ 唯一索引：uk_{table}_{column}
CREATE UNIQUE INDEX uk_users_username ON users(username);

-- ✅ 外键：fk_{table}_{ref_table}
ALTER TABLE orders ADD CONSTRAINT fk_orders_users 
  FOREIGN KEY (user_id) REFERENCES users(id);
```

---

## 🎯 目录组织原则

### 后端组织原则

#### 1. 分层清晰

```
Controller → Service → Repository → Entity
    ↓          ↓          ↓
   DTO    ← Mapper →    Entity
```

#### 2. 功能模块化

```
revert/                            # 功能模块目录
├── strategy/                     # 策略模式
│   ├── RevertStrategy.java
│   └── impl/
│       ├── OrderRevertStrategy.java
│       └── UserRevertStrategy.java
├── RevertStrategyFactory.java
└── ...
```

**原则**：
- 独立功能 → 独立模块目录
- 模块内保持完整性（策略、工厂、辅助类）
- 避免跨模块直接调用（通过Service层交互）

#### 3. 通用层复用

```
common/                            # 通用层
├── annotation/                   # 所有业务共用的注解
├── enums/                        # 所有业务共用的枚举
├── exception/                    # 所有业务共用的异常
└── utils/                        # 所有业务共用的工具
```

**原则**：
- 3次以上复用 → 提升到 common
- 业务相关 → 留在模块内
- 避免 common 过度膨胀

### 前端组织原则

#### 1. Monorepo 包隔离

```
packages/
├── admin/      → 管理端独立，不依赖 portal
├── portal/     → 用户端独立，不依赖 admin
└── shared/     → 两端共用，被admin和portal依赖
```

**原则**：
- **admin** 和 **portal** 之间零依赖
- 共用代码必须放 **shared**
- 避免循环依赖

#### 2. 功能模块化

```
pages/
├── User/                          # 用户管理模块
│   ├── UserList.tsx
│   ├── UserDetail.tsx
│   ├── UserEdit.tsx
│   ├── components/               # 模块内组件
│   │   └── UserForm.tsx
│   └── index.ts                  # 统一导出
└── ...
```

**原则**：
- 页面 + 组件 + Hooks 放在同一目录
- `index.ts` 统一导出
- 跨模块复用 → 提升到 `components/` 或 `shared/`

#### 3. 扁平化 vs 深层嵌套

```
✅ 正确：扁平化（2-3层）
components/
├── UserCard.tsx
├── ProductCard.tsx
└── OrderCard.tsx

❌ 错误：过度嵌套（4+层）
components/
└── business/
    └── user/
        └── card/
            └── UserCard.tsx
```

**原则**：
- 目录深度 ≤3 层
- 避免过度分类
- 按功能分组，不按类型分组

---

## 📂 文件组织规范

### 后端文件组织

#### 单一职责原则

```java
// ✅ 正确：一个类一个文件
// UserController.java
public class UserController { ... }

// ❌ 错误：多个类在一个文件
// Controllers.java
public class UserController { ... }
public class OrderController { ... }  // 应该独立文件
```

#### 内部类使用

```java
// ✅ 正确：简单内部类可接受
public class OrderService {
    @Data
    private static class OrderSummary {
        private Long totalAmount;
        private Integer totalCount;
    }
}

// ❌ 错误：复杂内部类应独立
public class OrderService {
    // 300行代码的内部类 → 应该独立成文件
    public static class ComplexOrderProcessor { ... }
}
```

#### DTO 组织

```
dto/
├── request/                       # 请求 DTO
│   ├── CreateUserRequestDTO.java
│   └── UpdateOrderRequestDTO.java
├── response/                      # 响应 DTO
│   ├── UserResponseDTO.java
│   └── OrderResponseDTO.java
└── ...DTO.java                   # 通用 DTO
```

### 前端文件组织

#### 组件文件结构

```
components/UserCard/
├── UserCard.tsx                   # 组件主文件
├── UserCard.test.tsx             # 测试文件（可选）
├── UserCard.module.css           # 样式文件（可选）
└── index.ts                       # 导出文件

// index.ts
export { UserCard } from './UserCard';
export type { UserCardProps } from './UserCard';
```

#### 页面文件结构

```
pages/Dashboard/
├── Dashboard.tsx                  # 页面主文件
├── components/                   # 页面专属组件
│   ├── StatCard.tsx
│   └── ChartPanel.tsx
├── hooks/                        # 页面专属 Hooks
│   └── useDashboardData.ts
├── types.ts                      # 页面类型定义
└── index.ts                       # 导出文件
```

#### index.ts 规范

```typescript
// ✅ 正确：统一导出
export { UserCard } from './UserCard';
export { ProductList } from './ProductList';
export type { UserCardProps } from './UserCard';

// ❌ 错误：不要用 export * from
export * from './UserCard';  // 可能导致命名冲突
```

---

## 🔄 模块依赖规则

### 后端依赖规则

```
Controller → Service → Repository → Entity
     ↓          ↓          ↓
   DTO        DTO      Entity
```

**禁止的依赖方向**：
- ❌ Repository → Service
- ❌ Service → Controller
- ❌ Entity → DTO（应该 DTO → Entity）

### 前端依赖规则

```
Pages → Components → Shared
  ↓          ↓          ↓
Hooks     Utils      API
```

**禁止的依赖方向**：
- ❌ Shared → Admin/Portal（shared 不能依赖业务包）
- ❌ Components → Pages（组件不能依赖页面）
- ❌ Admin ↔ Portal（两端互相依赖）

---

## 📐 代码文件大小限制

| 文件类型 | 最大行数 | 说明 |
|---------|---------|------|
| **Controller** | 300行 | 超过拆分成多个 Controller |
| **Service** | 500行 | 超过拆分成多个 Service |
| **Component** | 300行 | 超过拆分成子组件 |
| **Utils** | 200行 | 超过按功能拆分 |
| **页面** | 400行 | 超过拆分逻辑到 Hooks |

**拆分策略**：
- 提取子模块
- 抽取通用逻辑到 utils
- 使用组合模式
- 使用策略模式

---

## 📚 参考文档

- [CLAUDE.md](../../CLAUDE.md) - AI 开发规范
- [tech.md](./tech.md) - 技术栈规范
- [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)
- [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)
- [Google TypeScript Style Guide](https://google.github.io/styleguide/tsguide.html)

---

## 🚀 最佳实践检查清单

### 开发前检查

- [ ] 复用检查：相似功能是否已存在？
- [ ] 目录选择：应该放在哪个模块/包？
- [ ] 命名规范：是否符合项目规范？
- [ ] 依赖方向：是否符合分层架构？

### 开发中检查

- [ ] 文件大小：是否超过限制？
- [ ] 单一职责：是否需要拆分？
- [ ] 代码复用：是否有重复代码？
- [ ] 注释清晰：关键逻辑是否有注释？

### 提交前检查

- [ ] 测试覆盖：是否达到85%？
- [ ] 代码格式：是否通过Lint检查？
- [ ] 文档同步：是否更新相关文档？
- [ ] 构建成功：是否通过CI/CD？

---

**文档维护者**: BaSui 😎  
**最后更新**: 2025-11-03  
**下次审查**: 每月第一个工作日
