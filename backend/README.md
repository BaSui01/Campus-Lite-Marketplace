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
│   ├── common/                    # 公共层
│   │   ├── config/               # 配置类
│   │   ├── entity/               # 实体类
│   │   ├── dto/                  # 数据传输对象
│   │   │   ├── request/         # 请求 DTO
│   │   │   └── response/        # 响应 DTO
│   │   ├── enums/               # 枚举类
│   │   ├── exception/           # 异常类
│   │   └── utils/               # 工具类
│   ├── controller/              # 控制器层
│   ├── service/                 # 服务层
│   │   └── impl/               # 服务实现
│   ├── repository/              # 数据访问层
│   └── websocket/               # WebSocket 处理
├── src/main/resources/
│   ├── application.yml          # 主配置文件
│   ├── application-dev.yml      # 开发环境配置
│   ├── application-prod.yml     # 生产环境配置
│   └── logback-spring.xml       # 日志配置
└── pom.xml                      # Maven 配置

```

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
mvn clean install
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
