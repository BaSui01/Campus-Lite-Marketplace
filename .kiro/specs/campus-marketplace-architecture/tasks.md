# 校园轻享集市系统 - 实现任务列表

## 任务说明

本任务列表按照依赖顺序组织，每个任务都是可独立执行的编码任务。任务标记说明：
- `[ ]` - 未开始
- `[-]` - 进行中
- `[x]` - 已完成
- `*` - 可选任务（如单元测试）

---

## 第一阶段：项目基础设施搭建

### [x] 1. 初始化 Spring Boot 项目结构

创建基于 Java 21 + Spring Boot 3.x 的项目骨架，配置基础依赖和包结构。

- 创建 Maven/Gradle 项目配置文件
- 配置 Java 21 编译选项
- 创建分层包结构（common、controller、service、repository、websocket）
- 配置 application.yml（开发环境）
- 添加核心依赖：Spring Boot Starter Web、JPA、Security、Redis、WebSocket、Lombok、Validation
- _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 15.1, 15.2, 15.3, 15.4, 15.5_

### [x] 2. 配置数据库和 Redis 连接

配置 PostgreSQL 和 Redis 的连接和基础设置。

- 配置 PostgreSQL 数据源（HikariCP 连接池）
- 配置 JPA/Hibernate 属性（懒加载、批量操作、二级缓存）
- 配置 Redis Sentinel 连接
- 配置 Redisson 用于分布式锁和二级缓存
- 创建数据库初始化脚本（schema.sql）
- _需求: 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.3, 5.4, 5.5_

### [x] 3. 配置 Spring Security 和 JWT

实现基于 JWT 的无状态认证机制。

- 创建 SecurityConfig 配置类
- 实现 JwtUtil 工具类（生成、解析、验证 Token）
- 实现 JwtAuthenticationFilter 过滤器
- 配置 BCrypt 密码加密器
- 配置 CORS 和 CSRF 防护
- _需求: 2.1, 2.2, 2.3, 2.4, 2.5_

---

## 第二阶段：核心实体和数据访问层

### [x] 4. 创建用户和权限实体

实现 RBAC 权限模型的核心实体类。

- 创建 User 实体（用户表）
- 创建 Role 实体（角色表）
- 创建 Permission 实体（权限表）
- 配置多对多关联关系（User-Role、Role-Permission）
- 添加 JPA 审计字段（createdAt、updatedAt）
- 配置 Hibernate 二级缓存注解
- _需求: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

### [x] 5. 创建业务核心实体

实现物品、订单、消息等核心业务实体。

- 创建 Goods 实体（物品表，包含 JSONB 扩展属性）
- 创建 Category 实体（分类表）
- 创建 Order 实体（订单表，配置分区）
- 创建 Conversation 实体（会话表）
- 创建 Message 实体（消息表）
- 创建 Post 实体（帖子表）
- 配置实体间的关联关系和索引
- _需求: 19.1-19.8, 21.1-21.7, 23.1-23.8, 26.1-26.8, 43.1-43.7_

### [x] 6. 创建 Repository 接口

实现数据访问层接口，包含自定义查询方法。

- 创建 UserRepository（包含用户名、邮箱查询）
- 创建 GoodsRepository（包含全文搜索、JOIN FETCH 查询）
- 创建 OrderRepository（包含买家、卖家订单查询）
- 创建 MessageRepository（包含会话消息查询）
- 创建 PostRepository（包含帖子列表查询）
- 使用 @Query 和 @EntityGraph 优化查询
- _需求: 4.1, 4.2, 4.3, 4.4_

---

## 第三阶段：公共组件和工具类

### [x] 7. 实现统一响应和异常处理

创建统一的 API 响应格式和全局异常处理器。

- 创建 ApiResponse 泛型响应类
- 创建 ErrorCode 错误码枚举
- 创建 BusinessException 自定义异常
- 实现 GlobalExceptionHandler 全局异常处理器
- 处理参数校验异常、业务异常、系统异常
- _需求: 6.1, 6.2, 6.3, 6.4, 6.5, 11.1, 11.2, 11.3, 11.4, 11.5_

### [x] 8. 实现工具类

创建加密、Redis、日期等常用工具类。

- 实现 EncryptUtil（AES 加密、数据脱敏）
- 实现 RedisUtil（封装常用 Redis 操作）
- 实现 JwtUtil（JWT Token 生成和解析）
- 实现 SensitiveWordFilter（DFA 敏感词过滤）
- 实现 IdGenerator（分布式 ID 生成器）
- _需求: 2.4, 5.1, 5.2, 5.3, 5.4, 5.5_

### [x] 9. 配置 Java 21 特性

配置虚拟线程和异步任务执行器。

- 创建 VirtualThreadConfig（配置虚拟线程执行器）
- 配置 Tomcat 使用虚拟线程处理请求
- 创建 AsyncConfig（配置异步任务线程池）
- 启用 @Async 注解支持
- _需求: 7.1, 7.2, 7.3, 7.4_

---

## 第四阶段：用户认证和授权功能

### [x] 10. 实现用户注册功能

实现用户注册的完整流程。

- 创建 RegisterRequest DTO（使用 Record 类型）
- 实现 AuthService.register() 方法
- 验证用户名和邮箱唯一性
- 使用 BCrypt 加密密码
- 分配默认角色 ROLE_STUDENT
- 创建 AuthController.register() 接口
- _需求: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8_

### [x] 10.1 编写用户注册单元测试

为用户注册功能编写单元测试（关键功能，必须测试）。

- 测试注册成功场景
- 测试用户名已存在场景
- 测试邮箱已存在场景
- 测试参数校验失败场景
- _需求: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

### [x] 11. 实现用户登录功能

实现基于 JWT 的用户登录。

- 创建 LoginRequest 和 LoginResponse DTO
- 实现 AuthService.login() 方法
- 验证用户名和密码
- 检查用户状态（是否被封禁）
- 生成 JWT Token 并缓存到 Redis
- 记录登录日志
- 创建 AuthController.login() 接口
- _需求: 17.1, 17.2, 17.3, 17.4, 17.5, 17.6, 17.7_

### [x] 12. 实现用户资料管理

实现用户资料查询和修改功能。

- 实现 UserService.getUserProfile() 方法
- 实现 UserService.updateProfile() 方法
- 实现敏感信息脱敏（手机号、邮箱）
- 实现密码修改功能（验证旧密码）
- 创建 UserController 相关接口
- 使用 @PreAuthorize 进行权限控制
- _需求: 18.1, 18.2, 18.3, 18.4, 18.5, 18.6, 18.7_

---

## 第五阶段：物品管理功能

### [x] 13. 实现物品发布功能

实现物品发布和审核流程。

- 创建 CreateGoodsRequest DTO
- 实现 GoodsService.createGoods() 方法
- 验证物品标题、描述、价格、图片
- 进行敏感词过滤
- 设置物品状态为 PENDING（待审核）
- 创建 GoodsController.createGoods() 接口
- _需求: 19.1, 19.2, 19.3, 19.4, 19.5, 19.6, 19.7, 19.8_

### [x] 14. 实现物品查询功能

实现物品列表查询和详情查询。

- 实现 GoodsService.listGoods() 方法（支持关键词、分类、价格筛选）
- 使用 PostgreSQL 全文搜索
- 实现分页和排序
- 实现 GoodsService.getGoodsDetail() 方法
- 增加浏览量计数
- 使用 Redis 缓存物品详情和列表
- 创建 GoodsController 相关接口
- _需求: 21.1, 21.2, 21.3, 21.4, 21.5, 21.6, 21.7, 22.1, 22.2, 22.3, 22.4, 22.5_

### [x] 15. 实现物品审核功能

实现管理员审核物品功能。

- 实现 GoodsService.approveGoods() 方法
- 实现 GoodsService.rejectGoods() 方法
- 更新物品状态（APPROVED/REJECTED）
- 记录审核日志
- 发送审核结果通知
- 创建 GoodsController 审核接口
- 使用 @PreAuthorize 限制管理员权限
- _需求: 20.1, 20.2, 20.3, 20.4, 20.5, 20.6, 20.7_

### [x] 16. 实现物品收藏功能

实现用户收藏物品功能。

- 实现 FavoriteService.addFavorite() 方法
- 实现 FavoriteService.removeFavorite() 方法
- 实现 FavoriteService.listFavorites() 方法
- 更新物品收藏数
- 使用 Redis 缓存用户收藏列表
- 创建 FavoriteController 相关接口
- _需求: 39.1, 39.2, 39.3, 39.4, 39.5, 39.6, 39.7_

---

## 第六阶段：订单和支付功能

### [ ] 17. 实现订单创建功能

实现订单创建和库存锁定。

- 创建 CreateOrderRequest DTO
- 实现 OrderService.createOrder() 方法
- 验证物品状态（是否已售出）
- 生成唯一订单号
- 更新物品状态为 SOLD
- 使用 @Transactional 确保事务一致性
- 创建 OrderController.createOrder() 接口
- _需求: 23.1, 23.2, 23.3, 23.4, 23.5, 23.6, 23.7, 23.8_

### [ ] 18. 实现支付功能（沙箱模式）

集成微信支付和支付宝支付（沙箱环境）。

- 创建 PaymentService 接口
- 实现 WechatPaymentService（微信支付）
- 实现 AlipayPaymentService（支付宝支付）
- 实现支付回调处理
- 验证支付签名
- 更新订单状态为 PAID
- 实现支付超时自动取消订单（使用定时任务）
- 创建 OrderController.payOrder() 接口
- _需求: 24.1, 24.2, 24.3, 24.4, 24.5, 24.6, 24.7, 24.8, 34.1-34.7, 36.1-36.5_

### [ ] 19. 实现订单查询功能

实现订单列表和详情查询。

- 实现 OrderService.listOrders() 方法（买家和卖家订单）
- 实现 OrderService.getOrderDetail() 方法
- 支持按状态筛选
- 使用分页查询
- 创建 OrderController 相关接口
- _需求: 24.1, 24.2, 24.3, 24.4_

### [ ] 20. 实现订单评价功能

实现订单完成后的评价功能。

- 实现 OrderService.reviewOrder() 方法
- 验证订单状态（已完成）
- 进行敏感词过滤
- 更新卖家信用评分
- 创建 OrderController.reviewOrder() 接口
- _需求: 25.1, 25.2, 25.3, 25.4, 25.5, 25.6, 25.7_

---

## 第七阶段：即时通讯功能

### [ ] 21. 实现 WebSocket 连接管理

配置 WebSocket 并实现连接管理。

- 创建 WebSocketConfig 配置类
- 实现 MessageWebSocketHandler 处理器
- 实现 WebSocketSessionManager（使用虚拟线程）
- 实现 JWT Token 验证
- 实现心跳检测机制
- 实现在线状态管理
- _需求: 45.1, 45.2, 45.3, 45.4, 45.5, 45.6, 45.7_

### [ ] 22. 实现私信发送功能

实现用户之间的私信功能。

- 实现 MessageService.sendMessage() 方法
- 创建或获取会话
- 进行敏感词过滤
- 保存消息到数据库
- 通过 WebSocket 实时推送消息
- 更新未读消息数（Redis）
- 创建 MessageController.sendMessage() 接口
- _需求: 44.1, 44.2, 44.3, 44.4, 44.5, 44.6, 44.7, 44.8_

### [ ] 23. 实现消息查询功能

实现会话列表和聊天记录查询。

- 实现 MessageService.listConversations() 方法
- 实现 MessageService.listMessages() 方法
- 使用分页查询
- 使用 Redis 缓存会话列表
- 创建 MessageController 相关接口
- _需求: 43.1, 43.2, 43.3, 43.4, 43.5, 43.6, 43.7, 47.1-47.7_

### [ ] 24. 实现消息已读和撤回功能

实现消息已读状态和撤回功能。

- 实现 MessageService.markAsRead() 方法
- 实现 MessageService.recallMessage() 方法
- 验证撤回时限（2 分钟内）
- 通过 WebSocket 通知对方
- 更新未读消息数
- 创建 MessageController 相关接口
- _需求: 46.1, 46.2, 46.3, 46.4, 46.5, 46.6, 48.1-48.7_

### [ ] 25. 实现黑名单功能

实现用户拉黑和解除拉黑功能。

- 实现 BlacklistService.addToBlacklist() 方法
- 实现 BlacklistService.removeFromBlacklist() 方法
- 实现 BlacklistService.listBlacklist() 方法
- 在发送消息时检查黑名单
- 创建 BlacklistController 相关接口
- _需求: 51.1, 51.2, 51.3, 51.4, 51.5, 51.6, 51.7_

---

## 第八阶段：论坛功能

### [ ] 26. 实现论坛发帖功能

实现用户发帖和审核流程。

- 实现 PostService.createPost() 方法
- 验证帖子标题、内容、图片
- 进行敏感词过滤
- 限制每日发帖数量（使用 Redis）
- 设置帖子状态为 PENDING
- 创建 PostController.createPost() 接口
- _需求: 26.1, 26.2, 26.3, 26.4, 26.5, 26.6, 26.7, 26.8_

### [ ] 27. 实现论坛回复功能

实现帖子回复和楼中楼功能。

- 实现 PostService.replyPost() 方法
- 验证帖子状态（已审核）
- 进行敏感词过滤
- 限制回复频率（使用 Redis）
- 增加帖子回复数
- 发送通知给帖子作者
- 创建 PostController.replyPost() 接口
- _需求: 27.1, 27.2, 27.3, 27.4, 27.5, 27.6, 27.7_

### [ ] 28. 实现帖子查询功能

实现帖子列表和详情查询。

- 实现 PostService.listPosts() 方法
- 实现 PostService.getPostDetail() 方法
- 支持分页和排序
- 增加浏览量
- 使用 Redis 缓存热门帖子
- 创建 PostController 相关接口
- _需求: 21.1, 21.2, 21.3, 21.4, 21.5, 21.6, 21.7_

---

## 第九阶段：系统管理功能

### [ ] 29. 实现用户封禁功能

实现管理员封禁用户功能。

- 实现 UserService.banUser() 方法
- 实现 UserService.unbanUser() 方法
- 更新用户状态为 BANNED
- 记录封禁日志
- 发送封禁通知
- 实现定时解封任务
- 创建 UserController 相关接口
- 使用 @PreAuthorize 限制管理员权限
- _需求: 28.1, 28.2, 28.3, 28.4, 28.5, 28.6, 28.7_

### [ ] 30. 实现内容举报功能

实现用户举报违规内容功能。

- 实现 ReportService.createReport() 方法
- 实现 ReportService.handleReport() 方法
- 验证举报原因和类型
- 限制每日举报次数
- 记录举报日志
- 创建 ReportController 相关接口
- _需求: 40.1, 40.2, 40.3, 40.4, 40.5, 40.6, 40.7_

### [ ] 31. 实现审计日志功能

实现系统操作审计日志记录。

- 创建 AuditLogService
- 使用 AOP 拦截关键操作
- 记录操作人、操作类型、操作对象、操作结果
- 实现审计日志查询接口
- 创建 AuditLogController
- _需求: 30.1, 30.2, 30.3, 30.4, 30.5, 30.6, 30.7_

---

## 第十阶段：扩展功能

### [ ] 32. 实现积分系统

实现用户积分获取和消费功能。

- 实现 PointsService.addPoints() 方法
- 实现 PointsService.deductPoints() 方法
- 实现 PointsService.getPointsLog() 方法
- 在关键操作时赠送积分（注册、登录、发布、交易）
- 记录积分流水
- 创建 PointsController 相关接口
- _需求: 41.1, 41.2, 41.3, 41.4, 41.5, 41.6, 41.7_

### [ ] 33. 实现优惠券系统

实现优惠券创建、领取和使用功能。

- 实现 CouponService.createCoupon() 方法
- 实现 CouponService.receiveCoupon() 方法
- 实现 CouponService.useCoupon() 方法
- 验证优惠券有效期和使用条件
- 在订单中应用优惠券
- 实现优惠券退还逻辑
- 创建 CouponController 相关接口
- _需求: 42.1, 42.2, 42.3, 42.4, 42.5, 42.6, 42.7, 42.8_

### [ ] 34. 实现文件上传功能

实现图片上传和存储功能。

- 实现 FileService.uploadFile() 方法
- 验证文件类型和大小
- 生成唯一文件名
- 实现图片压缩和缩略图生成
- 支持本地存储和阿里云 OSS
- 创建 FileController.upload() 接口
- _需求: 38.1, 38.2, 38.3, 38.4, 38.5, 38.6, 38.7, 38.8_

### [ ] 35. 实现消息通知系统

实现站内消息和邮件通知功能。

- 实现 NotificationService.sendNotification() 方法
- 实现 NotificationService.sendEmail() 方法（异步）
- 在关键操作时发送通知（订单状态变更、帖子回复、@提及）
- 实现消息已读/未读管理
- 使用 Redis 缓存未读消息数
- 创建 NotificationController 相关接口
- _需求: 37.1, 37.2, 37.3, 37.4, 37.5, 37.6, 37.7_

---

## 第十一阶段：性能优化和监控

### [ ] 36. 实现缓存预热和更新策略

实现系统启动时的缓存预热。

- 创建 CacheWarmer 组件
- 预热热门物品列表
- 预热分类列表
- 预热系统配置
- 实现缓存更新策略（定时刷新、主动失效）
- _需求: 5.1, 5.2, 5.3, 5.4, 5.5_

### [ ] 37. 实现接口频率限制

使用 Redis + AOP 实现接口限流。

- 创建 @RateLimit 注解
- 实现 RateLimitAspect 切面
- 使用 Redis 记录请求次数
- 实现滑动窗口限流算法
- 对关键接口添加限流保护
- _需求: 32.6_

### [ ] 38. 配置日志和监控

配置日志输出和系统监控。

- 配置 Logback 日志框架
- 配置日志级别和输出格式
- 配置错误日志独立文件
- 在关键操作中添加日志记录
- 配置 Spring Boot Actuator 健康检查
- _需求: 10.1, 10.2, 10.3, 10.4, 10.5_

---

## 第十二阶段：集成测试和部署

### [ ] 39. 编写集成测试

使用 Testcontainers 编写集成测试。

- 配置 Testcontainers（PostgreSQL、Redis）
- 编写用户注册登录集成测试
- 编写物品发布查询集成测试
- 编写订单创建支付集成测试
- 编写消息发送接收集成测试
- _需求: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

### [ ] 40. 配置生产环境部署

准备生产环境配置和部署脚本。

- 创建 application-prod.yml 配置文件
- 配置 PostgreSQL 主从复制
- 配置 Redis Sentinel 集群
- 创建 Docker Compose 部署文件
- 创建数据库迁移脚本
- 配置 Nginx 反向代理
- _需求: 33.1, 33.2, 33.3, 33.4, 33.5_

---

## 任务执行说明

1. **严格按照顺序执行**：每个任务都依赖前面的任务，必须按顺序完成
2. **TDD 开发模式**：先写测试，再写实现（可选任务标记为 *）
3. **完整实现**：每个任务必须真实完整地实现，禁止使用模拟数据
4. **代码质量**：遵循 SOLID、KISS、DRY、YAGNI 原则
5. **注释规范**：所有公共方法必须有 JavaDoc 注释
6. **测试覆盖率**：目标 85% 以上

**开始开发前，请确保**：
- ✅ 已安装 Java 21
- ✅ 已安装 PostgreSQL 16
- ✅ 已安装 Redis 7.x
- ✅ 已配置 Maven/Gradle

**准备好了吗？让我们开始 TDD 开发之旅！🚀**


### [x] 11.1 编写用户登录单元测试

为用户登录功能编写单元测试（关键功能，必须测试）。

- 测试登录成功场景
- 测试密码错误场景
- 测试用户被封禁场景
- 测试 JWT Token 生成
- _需求: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_


### [ ] 17.1 编写订单创建单元测试

为订单创建功能编写单元测试（关键功能，必须测试）。

- 测试订单创建成功场景
- 测试物品已售出场景
- 测试事务回滚场景
- _需求: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

### [ ] 22.1* 编写消息发送单元测试

为消息发送功能编写单元测试（次要功能，可选）。

- 测试消息发送成功场景
- 测试敏感词过滤
- 测试黑名单拦截
- _需求: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

