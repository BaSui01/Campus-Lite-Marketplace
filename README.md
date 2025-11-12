# 校园轻享集市系统（Campus Lite Marketplace）

> **企业级校园二手交易与社区互动平台** - 基于 Java 21 + Spring Boot 3 + React 18 构建的全栈解决方案

## 🎯 项目定位

一体化校园交易与社区平台，涵盖商品交易、订单支付、即时通讯、论坛社区、运营管理等完整业务链路，支持生产环境高可用部署。

## 🌟 核心能力

### 业务功能
- **用户体系** - JWT + RBAC 权限控制、注册登录、黑白名单、风控系统
- **商品交易** - 发布/搜索/推荐、订单管理、物流跟踪、评价系统
- **支付能力** - 支付宝/微信支付集成、统一支付门面、异步回调、退款管理
- **社区互动** - 论坛帖子、话题讨论、点赞收藏、WebSocket 实时消息
- **运营管理** - 统计看板、任务调度、举报申诉、隐私合规

### 技术特性
- **高性能** - Redis 缓存、Redisson 分布式锁、连接池优化
- **高可用** - Docker 集群部署、Nginx 负载均衡、Redis Sentinel
- **可观测** - Prometheus + Grafana 监控、Logback 日志、健康检查
- **工程化** - TDD 测试、CI/CD、代码覆盖率 85%+、依赖安全扫描

## 🧱 技术架构

### 后端技术栈
```
Java 21 + Spring Boot 3.2.12
├── 核心框架：Spring Web、Spring Security、Spring Data JPA
├── 数据存储：PostgreSQL 16、Redis 7.x
├── 缓存/分布式：Redisson、Spring Cache
├── 通信协议：WebSocket、REST API、OpenAPI 3.0
├── 工具库：Lombok、MapStruct、Flyway、JWT
└── 测试框架：JUnit 5、Mockito、Testcontainers
```

### 前端技术栈
```
React 18 + TypeScript + Vite（Monorepo）
├── 架构模式：pnpm Workspace + 三包分层
│   ├── shared：公共组件、API 客户端、类型定义、工具函数
│   ├── portal：用户端（Tailwind CSS + React Query + Zustand）
│   └── admin：管理端（Ant Design + React Query + Zustand）
├── 状态管理：Zustand（轻量）+ React Query（服务端状态）
├── 样式方案：Tailwind CSS（用户端）+ Ant Design（管理端）
└── 构建工具：Vite 5.x + TypeScript 5.4+
```

### 基础设施
- **容器化**：Docker 24+ / Docker Compose 2.20+
- **反向代理**：Nginx（负载均衡 + SSL）
- **监控告警**：Prometheus + Grafana
- **数据库**：PostgreSQL 16（主库）+ Redis 7.x（缓存/会话）

## 📁 项目结构

```
campus-lite-marketplace/
├── backend/                           # 后端服务（Spring Boot 单体）
│   ├── src/main/java/.../marketplace/
│   │   ├── common/                   # 公共层：配置、DTO、异常、工具、切面
│   │   ├── controller/               # REST API 控制器
│   │   ├── service/                  # 业务服务层（接口 + 实现）
│   │   ├── repository/               # JPA 数据访问层
│   │   ├── entity/                   # JPA 实体类
│   │   ├── websocket/                # WebSocket 实时通信
│   │   ├── scheduler/                # 定时任务调度
│   │   └── logistics/                # 物流系统集成
│   ├── src/main/resources/
│   │   ├── application*.yml          # 应用配置（多环境）
│   │   ├── db/migration/             # Flyway 数据库迁移
│   │   └── logback-spring.xml        # 日志配置
│   └── pom.xml                       # Maven 依赖管理
│
├── frontend/                          # 前端 Monorepo（pnpm workspace）
│   ├── packages/
│   │   ├── shared/                   # 公共层
│   │   │   ├── src/api/             # OpenAPI 自动生成的 API 客户端
│   │   │   ├── src/components/      # 公共 UI 组件
│   │   │   ├── src/hooks/           # 自定义 React Hooks
│   │   │   └── src/utils/           # 工具函数、类型定义
│   │   ├── portal/                   # 用户端（Tailwind CSS）
│   │   │   ├── src/pages/           # 页面组件
│   │   │   ├── src/components/      # 业务组件
│   │   │   └── src/stores/          # Zustand 状态管理
│   │   └── admin/                    # 管理端（Ant Design）
│   │       ├── src/pages/           # 后台管理页面
│   │       └── src/services/        # 业务服务层
│   ├── pnpm-workspace.yaml           # Workspace 配置
│   └── package.json                  # 根 package（统一脚本）
│
├── docker/                            # Docker 部署配置
│   ├── docker-compose.dev.yml        # 开发环境（PostgreSQL/Redis/Mailhog）
│   ├── docker-compose.prod.min.yml   # 生产最小配置
│   ├── docker-compose.prod.ha.yml    # 生产高可用集群
│   ├── nginx/                        # Nginx 配置
│   ├── prometheus/                   # Prometheus 监控配置
│   └── grafana/                      # Grafana 仪表板
│
├── docs/                              # 项目文档
│   ├── 生产环境部署指南.md              # Docker 部署手册
│   ├── 安全扫描使用指南.md              # OWASP 依赖扫描
│   ├── 支付宝沙箱配置指南.md            # 支付宝集成说明
│   └── integration/                 # 集成测试文档
│
├── scripts/                           # 工具脚本
│   ├── generate-openapi.js           # OpenAPI 客户端生成
│   └── generate-ppt.mjs              # PPT 生成工具
│
├── .env.example                       # 环境变量模板
└── README.md                          # 项目主文档
```

## ⚙️ 环境要求

### 开发环境
| 组件 | 版本 | 必选 | 说明 |
|------|------|------|------|
| JDK | 21+ | ✅ | 后端编译运行（推荐 Temurin/Oracle OpenJDK） |
| Maven | 3.8+ | ✅ | 后端依赖管理与构建 |
| Node.js | 18+ | ✅ | 前端开发环境 |
| pnpm | 8+ | ✅ | 前端 Monorepo 包管理器 |
| PostgreSQL | 16 | ✅ | 主数据库（本地安装或 Docker） |
| Redis | 7 | ✅ | 缓存与会话（本地安装或 Docker） |
| Docker | 24+ | 🔧 | 可选，用于容器化部署 |
| Docker Compose | 2.20+ | 🔧 | 可选，多容器编排 |

### 生产环境
- **最小配置**：2 核 CPU / 4GB 内存 / 40GB 磁盘
- **推荐配置**：4 核 CPU / 8GB 内存 / 100GB SSD
- **高可用集群**：详见 `docs/生产环境部署指南.md`

## 🚀 快速开始

### 方式一：Docker Compose 一键启动（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/BaSui01/campus-lite-marketplace.git
cd campus-lite-marketplace

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件，配置数据库密码、JWT密钥等

# 3. 启动开发环境（PostgreSQL + Redis + Mailhog）
docker compose -f docker/docker-compose.dev.yml up -d

# 4. 启动后端（首次运行会自动初始化数据库）
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 5. 启动前端（新终端）
cd frontend
pnpm install
pnpm run dev:portal    # 用户端：http://localhost:8220
pnpm run dev:admin     # 管理端：http://localhost:8221
```

### 方式二：本地环境开发

**前置条件**：已安装 PostgreSQL 16 和 Redis 7

```bash
# 1. 克隆项目
git clone https://github.com/BaSui01/campus-lite-marketplace.git
cd campus-lite-marketplace

# 2. 配置环境变量
cp .env.example .env
# 修改 DB_HOST、REDIS_HOST 等配置为本地地址

# 3. 启动后端
cd backend
mvn clean package -DskipTests
java -jar target/marketplace-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev

# 4. 启动前端
cd frontend
pnpm install
pnpm run dev:portal    # 用户端
pnpm run dev:admin     # 管理端
```

### 访问地址
- **后端 API**：http://localhost:8080/api
- **健康检查**：http://localhost:8080/api/actuator/health
- **用户端前端**：http://localhost:8220
- **管理端前端**：http://localhost:8221
- **API 文档**：http://localhost:8080/swagger-ui.html

## 🔐 核心配置说明

### 环境变量（.env 文件）

| 配置项 | 说明 | 示例值 |
|--------|------|--------|
| **数据库配置** |
| `DB_HOST` | PostgreSQL 主机地址 | `localhost` / `postgres` |
| `DB_PORT` | PostgreSQL 端口 | `5432` |
| `DB_NAME` | 数据库名称 | `campus_marketplace` |
| `DB_USERNAME` | 数据库用户名 | `campus_user` |
| `DB_PASSWORD` | 数据库密码 | `your_password` |
| **Redis 配置** |
| `REDIS_HOST` | Redis 主机地址 | `localhost` / `redis` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | `your_redis_password` |
| **JWT 配置** |
| `JWT_SECRET` | JWT 签名密钥 | 强随机字符串（至少 32 字符） |
| `JWT_EXPIRE_HOURS` | Token 有效期（小时） | `24` |
| **支付配置** |
| `ALIPAY_*` | 支付宝配置 | 详见 `docs/支付宝沙箱配置指南.md` |
| `WECHAT_*` | 微信支付配置（V3） | 商户号、证书路径等 |

**⚠️ 生产环境安全提示**：
- 使用强随机密钥生成工具：`openssl rand -base64 32`
- 定期轮换 JWT_SECRET
- 数据库密码至少 16 字符，包含大小写字母、数字、特殊字符

## 🧪 测试与质量保证

### 后端测试

```bash
# 单元测试
cd backend
mvn test

# 完整测试（含集成测试）
mvn verify

# 生成覆盖率报告
mvn clean verify
# 报告位置：target/site/jacoco/index.html
```

**测试指标**：
- ✅ 代码覆盖率：≥85%（Jacoco）
- ✅ 静态代码分析：SpotBugs
- ✅ 依赖安全扫描：OWASP Dependency-Check
- ✅ 集成测试：Testcontainers（自动管理 Docker 容器）

### 前端测试

```bash
cd frontend
pnpm run lint         # ESLint 代码检查
pnpm run type-check   # TypeScript 类型检查
pnpm run format:check # Prettier 格式检查
```

## 🐳 生产部署

### Docker Compose 部署（推荐）

```bash
# 最小配置（单节点）
docker compose -f docker/docker-compose.prod.min.yml up -d

# 高可用集群（Redis Sentinel + Nginx + 监控）
docker compose -f docker/docker-compose.prod.ha.yml up -d
```

**部署架构选择**：
- **开发环境**：`docker-compose.dev.yml` - PostgreSQL + Redis + Mailhog
- **单机部署**：`docker-compose.prod.min.yml` - 适合个人或小团队
- **高可用集群**：`docker-compose.prod.ha.yml` - 生产环境推荐

详细部署步骤、监控配置、备份策略请参考 **[生产环境部署指南](docs/生产环境部署指南.md)**

## 📚 项目文档

### 核心文档
- **[后端开发指南](backend/README.md)** - 后端架构、编码规范、测试流程
- **[前端开发指南](frontend/README.md)** - Monorepo 架构、组件开发、状态管理
- **[生产环境部署指南](docs/生产环境部署指南.md)** - Docker 部署、监控、备份
- **[支付宝沙箱配置指南](docs/支付宝沙箱配置指南.md)** - 支付宝集成与调试

### 技术文档
- **[安全扫描使用指南](docs/安全扫描使用指南.md)** - OWASP 依赖扫描、CI/CD 集成
- **[API 接口文档](docs/api接口.md)** - RESTful API 接口说明

## 🤝 开发规范

### Git 提交规范
```
feat: 新增功能
fix: 修复 Bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具链相关
perf: 性能优化
```

### 分支管理
- `dev` - 开发主分支（默认）
- `feature/*` - 功能开发分支
- `fix/*` - Bug 修复分支
- `release/*` - 发布分支

### 提交流程
1. Fork 项目并创建功能分支
2. 编写代码并确保测试通过（`mvn verify`）
3. 遵循提交规范提交代码
4. 发起 Pull Request 到 `dev` 分支

## 📄 许可证

本项目采用 [MIT License](https://opensource.org/licenses/MIT) 开源协议。

---

**作者**: BaSui 😎  
**版本**: v1.0.0  
**最后更新**: 2025-11-12
