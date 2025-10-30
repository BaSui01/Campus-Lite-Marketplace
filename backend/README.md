# 校园轻享集市系统 - 后端服务

> 基于 Java 21 + Spring Boot 3.x 的企业级单体应用

## 技术栈

- **Java 21** - 使用 Virtual Threads、Record、Pattern Matching 等新特性
- **Spring Boot 3.2.0** - 企业级应用框架
- **Spring Data JPA** - ORM 框架
- **PostgreSQL 16** - 主数据库
- **Redis 7.x** - 缓存 + 会话管理
- **Spring Security + JWT** - 认证授权
- **WebSocket** - 实时通讯
- **Redisson** - 分布式锁 + 二级缓存
- **Lombok** - 减少样板代码
- **MapStruct** - 对象映射
- **Testcontainers** - 集成测试

## 项目结构

```
backend/
├── src/main/java/com/campus/marketplace/
│   ├── common/                    # 公共层：配置、DTO、异常、工具、切面
│   │   ├── component/            # 通用组件（缓存预热、调度器等）
│   │   ├── config/               # 应用配置（异步、国际化、支付等）
│   │   ├── dto/                  # 数据传输对象
│   │   │   ├── request/         # 请求 DTO
│   │   │   └── response/        # 响应 DTO
│   │   ├── entity/              # JPA 实体
│   │   ├── enums/               # 业务枚举
│   │   ├── exception/           # 异常定义
│   │   └── utils/               # 工具类、转换器
│   ├── controller/              # 控制器层（含 e2e/perf 专用子包）
│   ├── service/                 # 领域服务接口
│   │   ├── impl/               # 服务实现
│   │   └── perf/               # 性能专用服务实现
│   ├── integration/             # 外部系统集成（支付、消息网关等）
│   ├── repository/              # 数据访问层（JPA / QueryDSL）
│   └── websocket/               # WebSocket 通道处理
├── src/main/resources/
│   ├── application.yml          # 主配置文件
│   ├── application-dev.yml      # 开发环境配置
│   ├── application-prod.yml     # 生产环境配置
│   └── logback-spring.xml       # 日志配置
└── pom.xml                      # Maven 配置

```

## 架构总览

```
┌─────────────┐      ┌────────────────┐      ┌──────────┐
│  Client/前端 │ ──→ │  REST Controller │ ──→ │ Service层 │
└─────────────┘      └────────────────┘      └────┬─────┘
                                                  │
                                       ┌──────────▼──────────┐
                                       │  Domain & Application│
                                       │  - 交易、消息、支付  │
                                       │  - 事件驱动/调度任务 │
                                       └──────────┬──────────┘
                                                  │
                        ┌────────────┬────────────┴────────────┬────────────┐
                        │ Persistence│    Cache & Lock         │  External  │
                        │ JPA/Query  │ Redis/Redisson          │ 支付/短信等 │
                        └────────────┴─────────────────────────┴────────────┘
```

- **分层设计**：采用 Controller → Service → Repository 三层架构，公共逻辑下沉到 `common` 包，复杂业务通过领域服务与策略模式拆分，确保可维护性。
- **跨模块通信**：业务事件通过 Spring ApplicationEvent + `@Async` 异步执行，关键任务采用 `@Scheduled`+`TaskScheduler`，避免阻塞主流程。
- **数据访问**：核心表使用 Spring Data JPA，组合 QueryDSL 与原生 SQL 处理复杂报表；所有 Repository 统一封装事务边界并启用乐观锁。
- **缓存与并发**：Redis 负责热点数据、消息队列与限流；Redisson 提供分布式锁、延时队列、二级缓存，配合 `@Cacheable` 提升读写性能。
- **安全体系**：Spring Security + JWT 构建多角色（RBAC）控制；整合自定义注解完成数据级别鉴权、审计日志和风控拦截。
- **可观测性**：通过 Logback + Sleuth 记录链路，并输出 Prometheus 指标；异常统一由 `GlobalExceptionHandler` 处理，保证 API 语义一致。
- **外部集成**：支付统一门面封装支付宝/微信，消息服务对接 WebSocket、通知模板、短信/邮箱，所有第三方调用均封装超时与重试策略。

## 快速开始

### 前置要求

- JDK 21+
- Maven 3.8+
- PostgreSQL 16+
- Redis 7.x+

### 1. 启动数据库

```bash
# 启动 PostgreSQL
docker run -d --name postgres \
  -e POSTGRES_DB=campus_marketplace_dev \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16

# 启动 Redis
docker run -d --name redis \
  -p 6379:6379 \
  redis:7-alpine
```

### 2. 编译项目

```bash
# 🚀 推荐：使用多核并行编译（自动检测 CPU 核心数）
mvn clean install

# 🐌 传统单线程编译（慢，不推荐）
mvn clean install -T 1

# 🧪 跳过测试快速编译
mvn clean install -DskipTests

# 🔍 只运行单元测试
mvn test

# 🔬 运行集成测试
mvn verify -DskipTests=false -DskipITs=false

# 📊 生成覆盖率报告
mvn clean verify
```

> ✅ `2025-10-30`：`mvn verify -DskipITs` 已在本地跑通，Jacoco 针对 `com.campus.marketplace.controller.e2e` 与 `com.campus.marketplace.controller.perf` 的 85% 行覆盖阈值全部满足，同时生成了最新的 SpotBugs 报告与可执行 JAR。

#### ⚡ 性能优化说明

项目已配置 **Maven 多核并行编译**，自动提升构建速度：

- ✅ **编译器并行化**：`maven-compiler-plugin` 启用 `fork=true` + `threads=0`（自动检测 CPU 核心）
- ✅ **测试并行化**：`surefire-plugin` 配置 `forkCount=1C`（每核心 1 进程）+ `parallel=classes`
- ✅ **全局配置**：`.mvn/maven.config` 自动应用 `-T 1C`（无需每次手敲！）
- ✅ **增量编译**：只编译变更的文件
- ✅ **依赖并行下载**：8 线程并行下载依赖

**性能对比（4 核 CPU 示例）**：

| 编译模式 | 构建时间 | 提升 |
|---------|---------|-----|
| 🐌 单线程 `-T 1` | ~60s | - |
| 🚀 多核并行（默认） | ~25s | **~60% ↑** |

**查看实际并行度**：
```bash
# 查看 Maven 配置
cat .mvn/maven.config

# 查看 CPU 核心数
echo %NUMBER_OF_PROCESSORS%  # Windows
nproc                         # Linux/Mac
```

**自定义并行度**：
```bash
# 强制 4 线程
mvn clean install -T 4

# 2 倍 CPU 核心数（激进模式，适合 CI/CD）
mvn clean install -T 2C

# 单线程调试模式
mvn clean install -T 1
```

### 3. 运行应用

```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或者运行 JAR
java -jar target/marketplace-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### 4. 访问应用

- API 地址: http://localhost:8080/api
- 健康检查: http://localhost:8080/api/actuator/health

### 默认账号（初始密码均为 `password123`）

| 用户名 | 角色 |
| --- | --- |
| `admin` | `ROLE_SUPER_ADMIN`, `ROLE_ADMIN` |
| `student1` / `student2` | `ROLE_STUDENT`, `ROLE_USER` |
| `seller_north` / `seller_south` / `buyer_grad` | `ROLE_STUDENT`, `ROLE_USER` |
| `security_manager` | `ROLE_SECURITY_MANAGER`, `ROLE_USER` |
| `content_manager` | `ROLE_CONTENT_MANAGER`, `ROLE_USER` |
| `operation_manager` | `ROLE_OPERATION_MANAGER`, `ROLE_USER` |
| `compliance_officer` | `ROLE_COMPLIANCE_OFFICER`, `ROLE_USER` |
| `campus_manager` | `ROLE_CAMPUS_MANAGER`, `ROLE_USER` |
| `category_manager` | `ROLE_CATEGORY_MANAGER`, `ROLE_USER` |
| `rate_limit_manager` | `ROLE_RATE_LIMIT_MANAGER`, `ROLE_USER` |
| `analyst` | `ROLE_ANALYST`, `ROLE_USER` |
| `support_agent` | `ROLE_SUPPORT_AGENT`, `ROLE_USER` |

## 开发规范

### 代码规范

- 遵循 SOLID、KISS、DRY、YAGNI 原则
- 所有公共方法必须有 JavaDoc 注释
- 使用 Lombok 减少样板代码
- 使用构造器注入（@RequiredArgsConstructor）

### 测试规范

- 采用 TDD 开发模式
- 单元测试覆盖率 ≥ 85%
- 使用 JUnit 5 + Mockito
- 集成测试使用 Testcontainers

### Git 提交规范

```
feat: 新功能
fix: 修复 Bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试相关
chore: 构建/工具链相关
```

## 核心功能模块

- ✅ 用户认证授权（JWT + RBAC）
- ✅ 物品发布与交易
- ✅ 订单管理与支付
- ✅ 即时通讯（WebSocket）
- ✅ 论坛社区
- ✅ 积分与优惠券系统

## 作者

BaSui 😎 - 技术硬核但说话贼有意思的搞笑专业工程师！

## 许可证

MIT License
