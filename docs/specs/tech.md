# 技术栈与依赖规范

> **校园轻享集市系统** - 全局技术标准文档  
> **架构**: Spring Boot 单体 + 前后端分离 | TDD 开发 | 测试覆盖率 ≥85%  
> **更新**: 2025-11-03

---

## 📋 目录

- [后端技术栈](#后端技术栈)
- [前端技术栈](#前端技术栈)
- [依赖管理规范](#依赖管理规范)
- [性能标准](#性能标准)
- [测试标准](#测试标准)
- [安全标准](#安全标准)

---

## 🔧 后端技术栈

### 核心框架

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **Java** | 21 | 编程语言 | LTS版本，强制使用 |
| **Spring Boot** | 3.2.12 | 应用框架 | Spring 6.1.x |
| **Maven** | 3.x+ | 构建工具 | 依赖管理 + 插件生态 |

### 数据访问层

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **Spring Data JPA** | 3.2.x | ORM框架 | Hibernate 6.x |
| **PostgreSQL** | 14+ | 主数据库 | 生产环境 |
| **H2** | 2.x | 测试数据库 | 仅测试环境 |
| **Flyway** | 10.15.0 | 数据库迁移 | 版本化管理 SQL |
| **Hypersistence Utils** | 3.7.3 | JSONB支持 | PostgreSQL JSON类型 |

### 缓存与分布式

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **Redis** | 6.x+ | 分布式缓存 | 会话 + 业务缓存 |
| **Redisson** | 3.24.3 | Redis客户端 | 分布式锁 + 数据结构 |
| **Caffeine** | 3.1.8 | 本地缓存 | Feature Flags 等 |

### 安全与认证

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **Spring Security** | 6.2.x | 安全框架 | 认证 + 授权 |
| **JJWT** | 0.12.3 | JWT库 | 无状态认证 |

### 第三方服务集成

| 服务类型 | 技术栈 | 版本 | 说明 |
|---------|--------|------|------|
| **支付宝SDK** | alipay-sdk-java | 4.40.483.ALL | 官方SDK（必须带.ALL后缀） |
| **微信支付SDK** | wechatpay-java | 0.2.17 | 官方APIv3 SDK |
| **微信支付SDK V2** | weixin-java-pay | 4.7.0 | 沙箱测试用 |
| **阿里云OSS** | aliyun-sdk-oss | 3.17.4 | 对象存储 |
| **阿里云短信** | dysmsapi20170525 | 2.0.24 | 短信服务 |
| **邮件发送** | spring-boot-starter-mail | 3.2.x | 邮件通知 |

### 工具库

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **Lombok** | 1.18.32 | 代码生成 | 减少样板代码 |
| **MapStruct** | 1.5.5.Final | 对象映射 | DTO ↔ Entity |
| **Commons IO** | 2.15.1 | 文件操作 | 文件上传下载 |
| **Thumbnailator** | 0.4.20 | 图片处理 | 缩略图 + 压缩 |

### 文档与监控

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **SpringDoc OpenAPI** | 2.6.0 | API文档 | Swagger UI（等Spring Boot 3.3+后升级2.7.0） |
| **Spring Boot Actuator** | 3.2.x | 监控端点 | 健康检查 + 指标 |
| **Micrometer** | 1.12.x | 指标收集 | Prometheus格式导出 |
| **Logstash Logback Encoder** | 7.4 | 日志格式 | JSON结构化日志 |

### 测试工具

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **JUnit** | 5.10.x | 单元测试 | Spring Boot内置 |
| **Testcontainers** | 1.19.3 | 集成测试 | Docker容器测试 |
| **JaCoCo** | 0.8.11 | 代码覆盖率 | 强制≥85%覆盖率 |
| **SpotBugs** | 4.8.3.0 | 静态分析 | Bug检测 |
| **PIT** | 1.15.5 | 突变测试 | 测试质量≥60% |
| **OWASP Dependency-Check** | 12.1.0 | 安全扫描 | CVSS≥7 阻断构建 |

---

## 🎨 前端技术栈

### 核心框架

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **Node.js** | ≥18.0.0 | 运行环境 | LTS版本 |
| **pnpm** | ≥8.0.0 | 包管理器 | Monorepo + workspace |
| **TypeScript** | 5.4.5 | 编程语言 | 类型安全 |
| **React** | 18.3.1 | UI框架 | 并发特性 |
| **Vite** | 5.4.1 | 构建工具 | ESM + HMR |

### Monorepo 结构

| 包名 | 技术栈 | 用途 | 说明 |
|------|--------|------|------|
| **@campus/admin** | Ant Design 5.27.6 | 管理端 | PC Web后台管理 |
| **@campus/portal** | Tailwind CSS 3.4.7 | 用户端 | 响应式Web (PC+移动) |
| **@campus/shared** | tsup 8.1.0 | 共享层 | 组件 + API + 类型 + 工具 |

### 状态管理

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **Zustand** | 4.5.7 | 全局状态 | 轻量级状态管理 |
| **Jotai** | 2.8.0 | 原子状态 | 管理端用（细粒度） |
| **React Query** | 5.51.0 | 服务端状态 | 数据请求 + 缓存 |

### UI与样式

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **Ant Design** | 5.27.6 | 管理端UI | 企业级组件库 |
| **@ant-design/icons** | 6.1.0 | 图标库 | 管理端图标 |
| **Emotion** | 11.11.5 | CSS-in-JS | 管理端样式 |
| **Tailwind CSS** | 3.4.7 | 用户端样式 | 原子化CSS |
| **ECharts** | 5.4.3 | 数据可视化 | 管理端图表 |

### 工具库

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **Axios** | 1.7.2 | HTTP客户端 | API请求 |
| **React Router** | 6.26.0 | 路由 | 客户端路由 |
| **Day.js** | 1.11.12 | 日期时间 | 轻量级时间库 |
| **React Transition Group** | 4.4.5 | 动画 | 页面过渡效果 |

### 开发工具

| 技术栈 | 版本 | 用途 | 说明 |
|--------|------|------|------|
| **ESLint** | 9.9.0 | 代码检查 | 统一代码风格 |
| **Prettier** | 3.3.3 | 代码格式化 | 自动格式化 |
| **Vitest** | 2.1.1 | 单元测试 | 共享层测试 |
| **Husky** | 9.1.6 | Git Hooks | 提交前检查 |
| **lint-staged** | 15.2.10 | 暂存区检查 | 只检查变更文件 |

---

## 📦 依赖管理规范

### 后端依赖规范

#### Maven Repository 优先级

```xml
1. Maven Central (repo1.maven.org) - 官方仓库
2. Aliyun Maven Public - 阿里云镜像（加速）
3. JitPack - GitHub项目构建
```

#### 依赖版本管理

- **统一管理**: `<dependencyManagement>` 统一版本
- **安全升级**: CVE修复优先级最高
- **传递依赖**: 显式覆盖高危版本
- **版本策略**: 
  - Spring Boot: 跟随3.2.x稳定版
  - 第三方库: 使用最新稳定版
  - 安全补丁: 立即升级

#### 禁止重复依赖

❌ **禁止引入功能重复的依赖：**
```xml
<!-- ❌ 错误：重复引入JSON库 -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
</dependency>
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
</dependency>
```

✅ **正确：统一使用Jackson（Spring Boot默认）：**
```xml
<!-- ✅ 正确：只使用一种JSON库 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

### 前端依赖规范

#### pnpm Workspace 配置

```yaml
# pnpm-workspace.yaml
packages:
  - 'packages/*'
```

#### 版本锁定策略

- **pnpm-lock.yaml**: 强制提交到Git
- **packageManager**: 锁定pnpm版本（8.15.0）
- **engines**: 限制Node.js版本（≥18.0.0）

#### 依赖分类

| 类型 | 位置 | 说明 |
|------|------|------|
| **公共依赖** | 根目录 package.json | TypeScript、Prettier、Husky |
| **业务依赖** | packages/*/package.json | React、UI库、状态管理 |
| **共享依赖** | @campus/shared | 多个包共用的工具库 |

#### 禁止重复依赖

- **检查工具**: `pnpm why <package>` 检查依赖来源
- **合并版本**: 相同功能库统一版本
- **Peer Dependencies**: 正确声明对等依赖

---

## ⚡ 性能标准

### 后端性能标准

| 指标 | 目标值 | 测量方法 |
|------|--------|---------|
| **API响应时间** | P95 < 200ms | Actuator + Prometheus |
| **数据库查询** | P95 < 100ms | Hibernate日志 + SQL分析 |
| **缓存命中率** | ≥80% | Redis监控 |
| **单元测试执行** | <1秒/类 | Surefire报告 |
| **集成测试执行** | <10秒/类 | Failsafe报告 |
| **构建时间** | <5分钟 | Maven构建日志 |
| **内存占用** | <512MB (空载) | JVM监控 |
| **启动时间** | <30秒 | Spring Boot日志 |

#### JVM调优参数

```bash
# 生产环境推荐配置
-Xms512m -Xmx2g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError
```

### 前端性能标准

| 指标 | 目标值 | 测量方法 |
|------|--------|---------|
| **首屏加载** | <2秒 | Lighthouse |
| **白屏时间** | <1秒 | Performance API |
| **TTI** | <3秒 | Lighthouse |
| **FCP** | <1.5秒 | Web Vitals |
| **LCP** | <2.5秒 | Web Vitals |
| **CLS** | <0.1 | Web Vitals |
| **Bundle大小** | <500KB (gzip) | Vite构建分析 |
| **开发热更新** | <200ms | Vite HMR |

#### 性能优化策略

- **代码分割**: React.lazy + Suspense
- **路由懒加载**: 按需加载页面组件
- **图片优化**: WebP格式 + 懒加载
- **资源压缩**: Gzip/Brotli压缩
- **CDN加速**: 静态资源CDN部署
- **缓存策略**: Service Worker + HTTP缓存

---

## 🧪 测试标准

### TDD测试流程

**遵循十步流程法**（详见 CLAUDE.md）：

```
🔍 第0步：复用检查 → 先复用，再创造
🔴 第1步：编写测试 → 定义预期行为
🟢 第2-7步：分层实现 → Entity → DTO → Mapper → Service → Controller
🔵 第8-9步：运行测试 → 验证 + 重构
```

### 后端测试标准

| 测试类型 | 覆盖率要求 | 工具 | 说明 |
|---------|-----------|------|------|
| **单元测试** | ≥85% | JUnit 5 | Controller + Service必须85% |
| **集成测试** | 核心流程100% | Testcontainers | 真实数据库环境 |
| **突变测试** | ≥60% | PIT | 测试质量检测 |
| **契约测试** | API 100% | Swagger Parser | OpenAPI规范验证 |

#### 覆盖率强制标准

```xml
<!-- JaCoCo强制覆盖率配置 -->
<rule>
    <element>PACKAGE</element>
    <includes>
        <include>com.campus.marketplace.controller.*</include>
        <include>com.campus.marketplace.common.aspect.*</include>
    </includes>
    <limits>
        <limit>
            <counter>LINE</counter>
            <value>COVEREDRATIO</value>
            <minimum>0.85</minimum> <!-- 强制85% -->
        </limit>
    </limits>
</rule>
```

#### 测试执行策略

- **并行执行**: 类级别并行（2线程）
- **输出重定向**: 避免通道冲突
- **测试隔离**: 每个测试独立数据
- **快速反馈**: 单元测试<1s，集成测试<10s

### 前端测试标准

| 测试类型 | 覆盖率要求 | 工具 | 说明 |
|---------|-----------|------|------|
| **单元测试** | ≥80% | Vitest | 共享层工具函数 |
| **组件测试** | 核心组件100% | React Testing Library | 交互逻辑测试 |
| **E2E测试** | 关键路径100% | Playwright | 端到端测试 |

---

## 🔒 安全标准

### 依赖安全扫描

| 工具 | 用途 | 触发时机 | 阻断标准 |
|------|------|---------|---------|
| **OWASP Dependency-Check** | CVE扫描 | mvn verify | CVSS ≥7 |
| **SpotBugs** | 静态分析 | mvn verify | 任何High级别 |
| **npm audit** | 前端依赖 | CI/CD | Critical级别 |

### 已修复高危漏洞

| 组件 | CVE编号 | 原版本 | 修复版本 |
|------|---------|--------|---------|
| dom4j | CVE-2020-10683 | 2.1.1 | 2.1.4 |
| Bouncy Castle | CVE-2020-15522 | 1.70 | 1.78.1 |
| Angus Mail | CVE-2025-7962 | 2.0.1 | 2.0.3 |
| Logback | CVE-2023-6378 | 1.4.x | 1.5.6 |
| Netty | Multiple 2024/2025 | 4.1.x | 4.1.113 |
| PostgreSQL JDBC | CVE-2024-1597 | 42.5.x | 42.7.4 |

### 代码安全规范

- **SQL注入**: 强制使用参数化查询
- **XSS防护**: 前端输出转义 + CSP
- **CSRF防护**: Spring Security CSRF Token
- **敏感信息**: 禁止日志打印密码/密钥
- **权限控制**: 方法级别@PreAuthorize
- **API鉴权**: JWT + 角色权限验证

---

## 🚀 性能优化清单

### 后端优化

- [ ] 数据库索引优化（N+1查询消除）
- [ ] Redis缓存策略（热点数据缓存）
- [ ] JPA批量操作（批量插入/更新）
- [ ] 异步处理（@Async + 线程池）
- [ ] 连接池调优（HikariCP配置）
- [ ] JVM参数调优（G1GC + 堆大小）

### 前端优化

- [ ] 路由懒加载（React.lazy）
- [ ] 组件按需加载（Tree Shaking）
- [ ] 图片懒加载（Intersection Observer）
- [ ] 虚拟滚动（长列表优化）
- [ ] 防抖节流（高频事件优化）
- [ ] 缓存策略（React Query配置）

---

## 📝 版本升级计划

### 待升级项

| 组件 | 当前版本 | 目标版本 | 阻塞原因 | 预计时间 |
|------|---------|---------|---------|---------|
| **Spring Boot** | 3.2.12 | 3.3.x | 生态兼容性测试 | Q2 2026 |
| **SpringDoc OpenAPI** | 2.6.0 | 2.7.0 | 需Spring Boot 3.3+ | 跟随Spring Boot |
| **React** | 18.3.1 | 19.x | 稳定性观察 | 等待正式版 |

### 升级策略

1. **安全补丁**: 立即升级（当天）
2. **小版本**: 1周内评估 → 2周内升级
3. **大版本**: 3周评估 → 灰度发布 → 全量升级

---

## 📚 参考文档

- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [React 官方文档](https://react.dev/)
- [Vite 官方文档](https://vitejs.dev/)
- [Ant Design 官方文档](https://ant.design/)
- [项目CLAUDE.md](../../CLAUDE.md) - 开发规范
- [项目README.md](../../README.md) - 项目说明

---

**文档维护者**: BaSui 😎  
**最后更新**: 2025-11-03  
**下次审查**: 每月第一个工作日
