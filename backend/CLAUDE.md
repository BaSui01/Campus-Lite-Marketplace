# Backend 模块文档

[根目录](../CLAUDE.md) > **backend**

---

## 变更记录 (Changelog)

### 2025-10-27 18:00:00 - 微信支付V3升级 💳
- **升级微信支付SDK**：从 WxJava V2 升级到官方 wechatpay-java V3
- **配置重构**：
  - 使用RSA证书签名替代MD5签名，安全性更高
  - 支持自动更新平台证书（RSAAutoCertificateConfig）
  - 新增配置项：privateKeyPath、merchantSerialNumber、apiV3Key
- **服务升级**：
  - 重构 `WechatPaymentService`：使用V3 Native支付API
  - 新增支付订单查询功能 `queryOrderStatus()`
  - 优化回调通知处理（JSON格式，自动验签解密）
- **接口变更**：
  - 新增 `PaymentController`：提供支付创建和回调处理接口
  - 新增接口：`POST /api/payment/create`、`POST /api/payment/wechat/notify`、`GET /api/payment/status/{orderNo}`
- **配置文件更新**：
  - 更新 `.env` 和 `.env.example`：添加V3所需配置项和详细说明
  - 更新 `application.yml`：映射V3配置参数
- **依赖版本**：com.github.wechatpay-apiv3:wechatpay-java:0.2.17

### 2025-10-27 11:32:48 - 初始化
- 完成后端模块架构扫描
- 识别核心服务、控制器、实体
- 生成 API 接口清单

---

## 模块职责

Backend 是校园轻享集市系统的核心后端模块，负责：
- **RESTful API**：提供所有业务接口
- **业务逻辑**：用户认证、物品管理、订单处理、支付等
- **数据持久化**：JPA + PostgreSQL
- **缓存管理**：Redis 二级缓存
- **实时通讯**：WebSocket 消息推送（待实现）

---

## 入口与启动

### 主启动类
- **文件**：`src/main/java/com/campus/marketplace/MarketplaceApplication.java`
- **注解**：`@SpringBootApplication`、`@EnableJpaAuditing`、`@EnableCaching`、`@EnableAsync`
- **端口**：8080（可通过 `SERVER_PORT` 环境变量修改）
- **上下文路径**：`/api`

### 启动命令
```bash
# 开发环境
mvn spring-boot:run

# 生产环境
java -jar target/marketplace-1.0.0-SNAPSHOT.jar

# Docker 启动依赖服务
docker-compose up -d
```

---

## 对外接口

### 认证接口 (`AuthController`)
| 端点 | 方法 | 描述 | 权限 |
|-----|------|-----|-----|
| `/auth/register` | POST | 用户注册 | 公开 |
| `/auth/login` | POST | 用户登录 | 公开 |
| `/auth/logout` | POST | 用户登出 | 认证用户 |
| `/auth/refresh` | POST | 刷新 Token | 认证用户 |

### 用户接口 (`UserController`)
| 端点 | 方法 | 描述 | 权限 |
|-----|------|-----|-----|
| `/users/profile` | GET | 获取当前用户资料 | 认证用户 |
| `/users/{userId}` | GET | 获取指定用户资料 | 认证用户 |
| `/users/profile` | PUT | 更新用户资料 | 认证用户 |
| `/users/password` | PUT | 修改密码 | 认证用户 |

### 物品接口 (`GoodsController`)
| 端点 | 方法 | 描述 | 权限 |
|-----|------|-----|-----|
| `/api/goods` | POST | 发布物品 | 认证用户 |
| `/api/goods` | GET | 查询物品列表 | 公开 |
| `/api/goods/{id}` | GET | 获取物品详情 | 公开 |
| `/api/goods/pending` | GET | 获取待审核物品 | 管理员 |
| `/api/goods/{id}/approve` | POST | 审核物品 | 管理员 |

### 订单接口 (`OrderController`)
| 端点 | 方法 | 描述 | 权限 |
|-----|------|-----|-----|
| `/api/orders` | POST | 创建订单 | 认证用户 |
| `/api/orders/buyer` | GET | 买家订单列表 | 认证用户 |
| `/api/orders/seller` | GET | 卖家订单列表 | 认证用户 |
| `/api/orders/{orderNo}` | GET | 订单详情 | 认证用户 |

### 收藏接口 (`FavoriteController`)
| 端点 | 方法 | 描述 | 权限 |
|-----|------|-----|-----|
| `/api/favorites/{goodsId}` | POST | 添加收藏 | 认证用户 |
| `/api/favorites/{goodsId}` | DELETE | 取消收藏 | 认证用户 |
| `/api/favorites` | GET | 我的收藏列表 | 认证用户 |
| `/api/favorites/{goodsId}/check` | GET | 检查是否收藏 | 认证用户 |

### 支付接口 (`PaymentController`) 💳
| 端点 | 方法 | 描述 | 权限 |
|-----|------|-----|-----|
| `/api/payment/create` | POST | 创建支付订单 | 认证用户 |
| `/api/payment/wechat/notify` | POST | 微信支付回调通知 | 公开（微信服务器） |
| `/api/payment/status/{orderNo}` | GET | 查询支付状态 | 认证用户 |

---

## 关键依赖与配置

### Maven 依赖（pom.xml）
- **Spring Boot**：3.2.0（Web、JPA、Security、Redis、WebSocket、Validation）
- **PostgreSQL Driver**：runtime
- **Redisson**：3.24.3（Redis 客户端 + 分布式锁 + 二级缓存）
- **JWT**：jjwt 0.12.3
- **支付宝SDK**：alipay-sdk-java 4.40.483.ALL
- **微信支付SDK**：wechatpay-java 0.2.17（官方APIv3 Java SDK）
- **Lombok**：编译时
- **JUnit 5 + Mockito**：测试

### 应用配置（application.yml）
- **数据源**：HikariCP 连接池（最小 10，最大 50）
- **JPA**：Hibernate + PostgreSQL，懒加载、批量操作、二级缓存
- **Redis**：Lettuce 客户端，连接池配置
- **JWT**：密钥、过期时间、请求头配置
- **日志**：Logback，控制台 + 文件输出
- **监控**：Actuator（health、info、metrics）

### 核心配置类
- `SecurityConfig`：Spring Security + JWT 过滤器
- `RedisConfig`：Redis 序列化配置
- `JpaConfig`：审计、事务管理
- `VirtualThreadConfig`：Java 21 虚拟线程
- `AsyncConfig`：异步任务线程池
- `OpenApiConfig`：Swagger 文档配置

---

## 数据模型

### 核心实体（位于 `common/entity/`）

#### 用户相关
- **User**：用户表（id、username、password、email、phone、status、points）
- **Role**：角色表（id、name、description）
- **Permission**：权限表（id、name、description）
- **BanLog**：封禁记录（新增，用户管理功能）
- **PointsLog**：积分记录（新增，积分系统）

#### 物品相关
- **Goods**：物品表（id、title、description、price、category_id、seller_id、status、images、extra_attrs）
- **Category**：分类表（id、name、parent_id、sort_order）
- **Favorite**：收藏表（user_id、goods_id）

#### 订单相关
- **Order**：订单表（id、order_no、goods_id、buyer_id、seller_id、amount、status、payment_method）
- **Review**：订单评价（新增，order_id、rating、content）

#### 消息相关
- **Conversation**：会话表（id、user1_id、user2_id）
- **Message**：消息表（id、conversation_id、sender_id、content、type、status）
- **Post**：帖子表（id、user_id、title、content）

### 枚举类型（位于 `common/enums/`）
- `UserStatus`：ACTIVE, BANNED
- `GoodsStatus`：PENDING, APPROVED, REJECTED, SOLD
- `OrderStatus`：PENDING_PAYMENT, PAID, COMPLETED, CANCELLED
- `MessageStatus`：UNREAD, READ
- `MessageType`：TEXT, IMAGE, SYSTEM
- `PaymentMethod`：WECHAT, ALIPAY, POINTS（新增）
- `PointsType`：EARN, SPEND（新增）

---

## 测试与质量

### 单元测试（9 个测试类）
- `AuthServiceTest`：注册测试
- `AuthServiceLoginTest`：登录测试
- `AuthServiceImplTest`：认证服务测试
- `OrderServiceTest`：订单服务测试
- `GoodsServiceTest`：物品服务测试
- `FavoriteServiceTest`：收藏服务测试
- `PointsServiceTest`：积分服务测试（新增）
- `UserBanServiceTest`：用户封禁测试（新增）
- `PasswordEncoderTest`：密码加密测试

### 测试覆盖
- **服务层**：约 80% 覆盖（关键业务有测试）
- **控制器**：部分集成测试（通过 RestAssured）
- **Repository**：间接测试（通过服务层测试）

### 代码质量工具
- **Lombok**：减少样板代码
- **Spring Validation**：参数校验
- **SLF4J + Logback**：日志记录
- **Actuator**：监控与健康检查

---

## 常见问题 (FAQ)

### 1. 如何添加新的 API 接口？
1. 在 `controller/` 创建或修改 Controller
2. 定义 `@RequestMapping` 和具体的 HTTP 方法
3. 在 `service/` 实现业务逻辑
4. 在 `repository/` 添加数据访问方法（如需）
5. 编写单元测试（`test/service/`）
6. 更新 Swagger 注解（`@Operation`、`@Tag`）

### 2. 如何修改数据库表结构？
- **开发环境**：修改 Entity 类，JPA 会自动更新表结构（ddl-auto: update）
- **生产环境**：编写迁移脚本（`db/migration/`），手动执行或使用 Flyway

### 3. 如何配置 Redis 缓存？
- 在 Service 方法上添加 `@Cacheable`、`@CachePut`、`@CacheEvict`
- 配置缓存键（`key = "#userId"`）和过期时间
- 使用 `RedisUtil` 进行手动缓存操作

### 4. 如何实现分布式锁？
```java
RLock lock = redissonClient.getLock("lock:order:" + orderId);
try {
    if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
        // 业务逻辑
    }
} finally {
    lock.unlock();
}
```

### 5. 如何处理异常？
- **业务异常**：抛出 `BusinessException`（会被全局异常处理器捕获）
- **参数校验**：使用 `@Valid` 和 JSR 303 注解
- **系统异常**：由 `GlobalExceptionHandler` 统一处理

---

## 相关文件清单

### 核心源码
```
backend/src/main/java/com/campus/marketplace/
├── MarketplaceApplication.java (主启动类)
├── controller/ (表现层)
│   ├── AuthController.java
│   ├── UserController.java
│   ├── GoodsController.java
│   ├── OrderController.java
│   └── FavoriteController.java
├── service/ (服务层接口)
│   ├── AuthService.java
│   ├── UserService.java
│   ├── GoodsService.java
│   ├── OrderService.java
│   ├── PaymentService.java
│   └── PointsService.java
├── service/impl/ (服务层实现)
│   ├── AuthServiceImpl.java
│   ├── UserServiceImpl.java
│   ├── GoodsServiceImpl.java
│   ├── OrderServiceImpl.java
│   ├── PaymentServiceImpl.java
│   └── PointsServiceImpl.java
├── repository/ (数据访问层)
│   ├── UserRepository.java
│   ├── GoodsRepository.java
│   ├── OrderRepository.java
│   ├── FavoriteRepository.java
│   ├── ReviewRepository.java
│   ├── PointsLogRepository.java
│   └── BanLogRepository.java
└── common/ (公共层)
    ├── config/ (配置类)
    ├── entity/ (实体类)
    ├── dto/ (数据传输对象)
    ├── enums/ (枚举)
    ├── exception/ (异常)
    └── utils/ (工具类)
```

### 配置文件
```
backend/src/main/resources/
├── application.yml (应用配置)
├── logback-spring.xml (日志配置)
├── redisson.yaml (Redisson 配置)
└── db/
    ├── schema.sql (数据库结构)
    └── data.sql (初始数据)
```

### 测试代码
```
backend/src/test/java/com/campus/marketplace/
├── service/
│   ├── AuthServiceTest.java
│   ├── OrderServiceTest.java
│   ├── GoodsServiceTest.java
│   └── ... (其他测试)
└── PasswordEncoderTest.java
```

---

**最后更新**：2025-10-27 11:32:48
