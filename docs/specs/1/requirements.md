# 校园轻享集市系统 - 需求文档

## 引言

校园轻享集市系统是一个企业级完全单体应用，旨在为校园用户提供二手物品交易、论坛交流、订单管理等功能。系统采用分层架构设计，基于 Java 21 和 Spring Boot 3.x 技术栈，集成 PostgreSQL 数据库和 Redis 缓存，实现高内聚、低耦合的业务平台。

核心设计理念包括：
- 严格的分层架构（表现层、服务层、数据访问层、公共层）
- 基于 RBAC 的权限控制模型
- JWT 无状态认证机制
- Cache-Aside 缓存策略
- 统一 API 响应格式

## 术语表

- **System**: 校园轻享集市系统（Campus Marketplace System）
- **User**: 系统用户，包括学生、管理员等角色
- **RBAC**: 基于角色的访问控制（Role-Based Access Control）
- **JWT**: JSON Web Token，用于无状态认证
- **Entity**: JPA 实体类，与数据库表映射
- **Repository**: 数据访问接口，继承 JpaRepository
- **Service**: 业务逻辑服务层
- **Controller**: 控制器层，处理 HTTP 请求
- **DTO**: 数据传输对象（Data Transfer Object）
- **Cache-Aside**: 旁路缓存模式
- **ApiResponse**: 统一 API 响应格式
- **SecurityContext**: Spring Security 安全上下文
- **BCrypt**: 密码加密算法
- **JPQL**: Java 持久化查询语言

---

## 需求

### 需求 1：分层架构设计

**用户故事**: 作为系统架构师，我希望系统采用严格的分层架构，以便实现高内聚、低耦合，提升系统的可维护性和可测试性。

#### 验收标准

1. THE System SHALL 包含四个明确的架构层次：表现层、服务层、数据访问层和公共层
2. THE System SHALL 确保表现层仅依赖服务接口层和公共层
3. THE System SHALL 确保服务层仅依赖数据访问层和公共层
4. THE System SHALL 确保数据访问层仅依赖公共层的 Entity
5. THE System SHALL 确保公共层不依赖任何其他层

---

### 需求 2：用户认证与授权

**用户故事**: 作为系统用户，我希望通过用户名和密码登录系统，并获得基于角色的访问权限，以便安全地使用系统功能。

#### 验收标准

1. WHEN User 提交有效的用户名和密码，THE System SHALL 生成包含用户 ID、角色和权限的 JWT Token
2. WHEN User 在请求头中携带有效的 JWT Token，THE System SHALL 解析并验证 Token 的有效性
3. IF JWT Token 验证失败，THEN THE System SHALL 返回 401 未授权错误
4. THE System SHALL 使用 BCrypt 算法加密存储用户密码
5. THE System SHALL 将验证通过的用户信息存入 SecurityContext

---

### 需求 3：RBAC 权限模型

**用户故事**: 作为系统管理员，我希望通过角色和权限管理用户的访问控制，以便灵活地分配和管理系统权限。

#### 验收标准

1. THE System SHALL 支持 Permission 实体，包含 id、name 和 description 字段
2. THE System SHALL 支持 Role 实体，包含 id、name 和 description 字段
3. THE System SHALL 支持 User 实体，包含 id、username、password 和 status 字段
4. THE System SHALL 实现 Role 与 Permission 的多对多关联关系
5. THE System SHALL 实现 User 与 Role 的多对多关联关系
6. WHEN Controller 方法标记 @PreAuthorize 注解，THE System SHALL 根据 SecurityContext 中的权限信息决定是否允许访问

---

### 需求 4：数据持久化

**用户故事**: 作为开发人员，我希望使用 JPA 和 PostgreSQL 实现数据持久化，以便简化数据库操作并提升开发效率。

#### 验收标准

1. THE System SHALL 使用 JPA 注解（@Entity、@Table、@Column）将数据库表映射为 Java 实体类
2. THE System SHALL 提供继承 JpaRepository 的 Repository 接口，支持标准 CRUD、分页和排序操作
3. WHEN 需要简单查询时，THE System SHALL 使用方法名查询（如 findByUsername）
4. WHEN 需要复杂查询时，THE System SHALL 使用 @Query 注解定义 JPQL 或原生 SQL
5. THE System SHALL 使用 PostgreSQL 作为主数据库

---

### 需求 5：缓存策略

**用户故事**: 作为系统架构师，我希望使用 Redis 作为二级缓存，以便减轻数据库压力并提升系统性能。

#### 验收标准

1. THE System SHALL 使用 Redis 作为二级缓存存储
2. THE System SHALL 采用 Cache-Aside 旁路缓存模式
3. WHEN 读取数据时，THE System SHALL 先查询缓存，若未命中则查询数据库并将结果写回缓存
4. WHEN 更新或删除数据时，THE System SHALL 先操作数据库，然后主动删除或更新缓存
5. THE System SHALL 使用 Spring Cache 抽象，通过 @Cacheable、@CachePut、@CacheEvict 注解声明式管理缓存

---

### 需求 6：统一 API 响应格式

**用户故事**: 作为前端开发人员，我希望所有 API 接口返回统一格式的 JSON 响应，以便简化前端数据处理逻辑。

#### 验收标准

1. THE System SHALL 确保所有 API 接口返回统一格式的 JSON 对象
2. THE System SHALL 在成功响应中包含 code（200）、message 和 data 字段
3. THE System SHALL 在失败响应中包含 code（非 200）、message 和 data（null）字段
4. THE System SHALL 使用全局异常处理器（@ControllerAdvice + @ExceptionHandler）统一处理异常
5. THE System SHALL 使用 ApiResponse<T> 泛型类封装所有响应数据

---

### 需求 7：事务管理

**用户故事**: 作为开发人员，我希望系统自动管理数据库事务，以便确保数据一致性和完整性。

#### 验收标准

1. THE System SHALL 在服务层方法上使用 @Transactional 注解管理事务
2. THE System SHALL 配置 @Transactional(rollbackFor = Exception.class) 确保所有异常触发回滚
3. WHEN 服务层方法执行成功时，THE System SHALL 自动提交事务
4. WHEN 服务层方法抛出异常时，THE System SHALL 自动回滚事务

---

### 需求 8：依赖注入

**用户故事**: 作为开发人员，我希望使用构造器注入管理依赖，以便提升代码的可测试性和不可变性。

#### 验收标准

1. THE System SHALL 使用构造器注入方式注入依赖
2. THE System SHALL 使用 Lombok 的 @RequiredArgsConstructor 注解简化构造器代码
3. THE System SHALL 避免使用字段注入（@Autowired 直接标注在字段上）
4. THE System SHALL 确保所有依赖在对象创建时完成注入

---

### 需求 9：参数校验

**用户故事**: 作为开发人员，我希望系统自动校验请求参数，以便减少手动校验代码并提升代码质量。

#### 验收标准

1. THE System SHALL 使用 JSR 303（jakarta.validation）注解在 DTO 类上定义校验规则
2. WHEN Controller 接收请求时，THE System SHALL 自动执行参数校验
3. IF 参数校验失败，THEN THE System SHALL 返回 400 错误和详细的校验错误信息
4. THE System SHALL 支持常见校验注解（@NotNull、@NotBlank、@Email、@Size 等）

---

### 需求 10：日志记录

**用户故事**: 作为运维人员，我希望系统记录关键操作日志，以便排查问题和审计操作。

#### 验收标准

1. THE System SHALL 在关键业务操作（登录、创建、更新、删除）中记录日志
2. THE System SHALL 使用 SLF4J + Logback 作为日志框架
3. THE System SHALL 记录日志级别（INFO、WARN、ERROR）
4. THE System SHALL 在日志中包含时间戳、用户 ID、操作类型和操作结果
5. THE System SHALL 将错误日志记录到独立的错误日志文件

---

### 需求 11：异常处理

**用户故事**: 作为开发人员，我希望系统统一处理异常，以便简化异常处理逻辑并提供友好的错误信息。

#### 验收标准

1. THE System SHALL 定义 BusinessException 自定义异常类
2. THE System SHALL 定义 ErrorCode 枚举类，包含错误码和错误消息
3. THE System SHALL 使用全局异常处理器捕获所有异常
4. WHEN 捕获 BusinessException 时，THE System SHALL 返回对应的错误码和消息
5. WHEN 捕获未知异常时，THE System SHALL 返回 500 错误和通用错误消息

---

### 需求 12：RESTful API 设计

**用户故事**: 作为 API 使用者，我希望系统遵循 RESTful 设计规范，以便直观地理解和使用 API。

#### 验收标准

1. THE System SHALL 使用 GET 方法处理查询操作
2. THE System SHALL 使用 POST 方法处理创建操作
3. THE System SHALL 使用 PUT 方法处理更新操作
4. THE System SHALL 使用 DELETE 方法处理删除操作
5. THE System SHALL 使用复数名词作为资源路径（如 /api/users）
6. THE System SHALL 在路径中使用资源 ID（如 /api/users/{id}）

---

### 需求 13：代码质量

**用户故事**: 作为开发团队成员，我希望代码遵循统一的编码规范，以便提升代码可读性和可维护性。

#### 验收标准

1. THE System SHALL 遵循 SOLID 原则（单一职责、开闭原则、里氏替换、接口隔离、依赖倒置）
2. THE System SHALL 遵循 KISS 原则（保持简单）
3. THE System SHALL 遵循 DRY 原则（不重复代码）
4. THE System SHALL 遵循 YAGNI 原则（不过度设计）
5. THE System SHALL 为所有公共方法编写 JavaDoc 注释，包含 @author 和 @date
6. THE System SHALL 使用 Lombok 减少样板代码（如 @Data、@Builder、@RequiredArgsConstructor）

---

### 需求 14：测试驱动开发

**用户故事**: 作为开发人员，我希望采用 TDD 开发模式，以便提升代码质量和测试覆盖率。

#### 验收标准

1. THE System SHALL 在编写实现代码前先编写测试用例
2. THE System SHALL 确保测试覆盖率达到 85% 以上
3. THE System SHALL 使用 JUnit 5 作为单元测试框架
4. THE System SHALL 使用 Mockito 进行 Mock 测试
5. THE System SHALL 使用 Testcontainers 进行集成测试
6. THE System SHALL 为每个 Service 方法编写单元测试
7. THE System SHALL 为每个 Controller 接口编写集成测试

---

### 需求 15：包结构设计

**用户故事**: 作为开发人员，我希望项目采用清晰的包结构，以便快速定位代码和理解项目组织。

#### 验收标准

1. THE System SHALL 在 common 包中存放公共模块（config、entity、dto、utils）
2. THE System SHALL 在 controller 包中存放控制器类
3. THE System SHALL 在 service 包中存放服务接口和实现类
4. THE System SHALL 在 repository 包中存放数据访问接口
5. THE System SHALL 确保包结构与分层架构一致

---

## 核心业务功能需求

### 需求 16：用户注册

**用户故事**: 作为新用户，我希望通过校园邮箱注册账号，以便使用校园集市系统。

#### 验收标准

1. WHEN User 提交注册请求时，THE System SHALL 验证邮箱格式为校园邮箱（如 @campus.edu）
2. WHEN User 提交注册请求时，THE System SHALL 验证用户名长度在 3-20 个字符之间
3. WHEN User 提交注册请求时，THE System SHALL 验证密码强度（至少 8 位，包含大小写字母和数字）
4. IF 邮箱已被注册，THEN THE System SHALL 返回错误码 1001 和错误消息"邮箱已存在"
5. IF 用户名已被占用，THEN THE System SHALL 返回错误码 1002 和错误消息"用户名已被占用"
6. WHEN 注册成功时，THE System SHALL 使用 BCrypt 加密密码并存储到数据库
7. WHEN 注册成功时，THE System SHALL 发送验证邮件到用户邮箱
8. THE System SHALL 为新注册用户分配默认角色 ROLE_STUDENT

---

### 需求 17：用户登录

**用户故事**: 作为注册用户，我希望通过用户名和密码登录系统，以便访问个人功能。

#### 验收标准

1. WHEN User 提交有效的用户名和密码时，THE System SHALL 验证密码是否匹配
2. IF 密码验证失败，THEN THE System SHALL 返回错误码 1003 和错误消息"用户名或密码错误"
3. IF 用户账号被封禁，THEN THE System SHALL 返回错误码 1004 和错误消息"账号已被封禁"
4. WHEN 登录成功时，THE System SHALL 生成包含用户 ID、角色和权限的 JWT Token
5. WHEN 登录成功时，THE System SHALL 记录登录日志（时间、IP 地址）
6. THE System SHALL 设置 JWT Token 有效期为 2 小时
7. WHEN 连续登录失败 5 次时，THE System SHALL 锁定账号 15 分钟

---

### 需求 18：用户资料管理

**用户故事**: 作为登录用户，我希望查看和修改个人资料，以便保持信息准确。

#### 验收标准

1. WHEN User 请求查看个人资料时，THE System SHALL 返回用户名、邮箱、学号、头像、创建时间等信息
2. WHEN User 请求修改个人资料时，THE System SHALL 验证用户身份（JWT Token）
3. WHEN User 修改邮箱时，THE System SHALL 发送验证邮件到新邮箱
4. WHEN User 修改密码时，THE System SHALL 验证旧密码是否正确
5. WHEN User 修改密码时，THE System SHALL 验证新密码强度
6. THE System SHALL 对邮箱、手机号、学号使用 AES 加密存储
7. THE System SHALL 在展示时对敏感信息脱敏（如 138****5678）

---

### 需求 19：物品发布

**用户故事**: 作为卖家，我希望发布二手物品信息，以便出售闲置物品。

#### 验收标准

1. WHEN User 提交物品发布请求时，THE System SHALL 验证用户已登录
2. WHEN User 提交物品发布请求时，THE System SHALL 验证物品标题长度在 5-100 个字符之间
3. WHEN User 提交物品发布请求时，THE System SHALL 验证物品描述长度在 10-2000 个字符之间
4. WHEN User 提交物品发布请求时，THE System SHALL 验证物品价格大于 0
5. WHEN User 提交物品发布请求时，THE System SHALL 验证物品图片数量在 1-9 张之间
6. WHEN 物品发布成功时，THE System SHALL 设置物品状态为 PENDING（待审核）
7. WHEN 物品发布成功时，THE System SHALL 记录发布者 ID 和发布时间
8. THE System SHALL 对物品标题和描述进行敏感词过滤

---

### 需求 20：物品审核

**用户故事**: 作为管理员，我希望审核待发布的物品，以便确保内容合规。

#### 验收标准

1. WHEN Admin 请求查看待审核物品列表时，THE System SHALL 返回所有状态为 PENDING 的物品
2. WHEN Admin 审核通过物品时，THE System SHALL 更新物品状态为 APPROVED（已通过）
3. WHEN Admin 审核拒绝物品时，THE System SHALL 更新物品状态为 REJECTED（已拒绝）
4. WHEN Admin 审核拒绝物品时，THE System SHALL 记录拒绝原因
5. WHEN 物品状态变更时，THE System SHALL 发送通知给发布者
6. THE System SHALL 记录审核日志（审核人、审核时间、审核结果）
7. THE System SHALL 验证审核人拥有 system:goods:approve 权限

---

### 需求 21：物品查询

**用户故事**: 作为买家，我希望搜索和浏览物品，以便找到需要的商品。

#### 验收标准

1. WHEN User 请求查看物品列表时，THE System SHALL 返回所有状态为 APPROVED 的物品
2. WHEN User 使用关键词搜索时，THE System SHALL 在物品标题和描述中进行模糊匹配
3. WHEN User 按分类筛选时，THE System SHALL 返回指定分类的物品
4. WHEN User 按价格区间筛选时，THE System SHALL 返回价格在指定区间的物品
5. THE System SHALL 支持分页查询，默认每页 20 条记录
6. THE System SHALL 支持按发布时间、价格、浏览量排序
7. THE System SHALL 缓存热门物品列表到 Redis，缓存时间 5 分钟

---

### 需求 22：物品详情

**用户故事**: 作为买家，我希望查看物品详细信息，以便了解商品详情。

#### 验收标准

1. WHEN User 请求查看物品详情时，THE System SHALL 返回物品标题、描述、价格、图片、发布者信息、发布时间等
2. WHEN User 查看物品详情时，THE System SHALL 增加物品浏览量
3. WHEN User 查看物品详情时，THE System SHALL 记录浏览日志（用户 ID、物品 ID、浏览时间）
4. THE System SHALL 对发布者的敏感信息脱敏（如手机号）
5. THE System SHALL 缓存物品详情到 Redis，缓存时间 10 分钟

---

### 需求 23：订单创建

**用户故事**: 作为买家，我希望下单购买物品，以便完成交易。

#### 验收标准

1. WHEN User 提交订单创建请求时，THE System SHALL 验证用户已登录
2. WHEN User 提交订单创建请求时，THE System SHALL 验证物品状态为 APPROVED
3. WHEN User 提交订单创建请求时，THE System SHALL 验证物品未被其他用户购买
4. IF 物品已被购买，THEN THE System SHALL 返回错误码 3001 和错误消息"物品已售出"
5. WHEN 订单创建成功时，THE System SHALL 更新物品状态为 SOLD（已售出）
6. WHEN 订单创建成功时，THE System SHALL 设置订单状态为 PENDING_PAYMENT（待支付）
7. WHEN 订单创建成功时，THE System SHALL 生成唯一订单号
8. THE System SHALL 使用数据库事务确保订单创建和物品状态更新的原子性

---

### 需求 24：订单支付

**用户故事**: 作为买家，我希望通过支付宝或微信支付订单，以便完成交易。

#### 验收标准

1. WHEN User 请求支付订单时，THE System SHALL 验证订单状态为 PENDING_PAYMENT
2. WHEN User 请求支付订单时，THE System SHALL 验证订单归属于当前用户
3. WHEN User 选择支付宝支付时，THE System SHALL 调用支付宝沙箱支付接口
4. WHEN User 选择微信支付时，THE System SHALL 调用微信沙箱支付接口
5. WHEN 支付成功时，THE System SHALL 更新订单状态为 PAID（已支付）
6. WHEN 支付成功时，THE System SHALL 发送支付成功通知给买家和卖家
7. WHEN 支付超时（30 分钟）时，THE System SHALL 自动取消订单并恢复物品状态
8. THE System SHALL 记录支付日志（支付方式、支付时间、支付金额）

---

### 需求 25：订单评价

**用户故事**: 作为买家，我希望对完成的订单进行评价，以便分享购物体验。

#### 验收标准

1. WHEN User 提交订单评价时，THE System SHALL 验证订单状态为 COMPLETED（已完成）
2. WHEN User 提交订单评价时，THE System SHALL 验证订单归属于当前用户
3. WHEN User 提交订单评价时，THE System SHALL 验证评价内容长度在 10-500 个字符之间
4. WHEN User 提交订单评价时，THE System SHALL 验证评分在 1-5 星之间
5. WHEN 评价提交成功时，THE System SHALL 更新订单状态为 REVIEWED（已评价）
6. WHEN 评价提交成功时，THE System SHALL 更新卖家的信用评分
7. THE System SHALL 对评价内容进行敏感词过滤

---

### 需求 26：论坛发帖

**用户故事**: 作为用户，我希望在论坛发布帖子，以便与其他用户交流。

#### 验收标准

1. WHEN User 提交发帖请求时，THE System SHALL 验证用户已登录
2. WHEN User 提交发帖请求时，THE System SHALL 验证帖子标题长度在 5-100 个字符之间
3. WHEN User 提交发帖请求时，THE System SHALL 验证帖子内容长度在 10-5000 个字符之间
4. WHEN User 提交发帖请求时，THE System SHALL 验证帖子图片数量不超过 9 张
5. WHEN 发帖成功时，THE System SHALL 设置帖子状态为 PENDING（待审核）
6. WHEN 发帖成功时，THE System SHALL 记录发帖者 ID 和发帖时间
7. THE System SHALL 对帖子标题和内容进行敏感词过滤
8. THE System SHALL 限制用户每天发帖数量不超过 10 条

---

### 需求 27：论坛回复

**用户故事**: 作为用户，我希望回复帖子，以便参与讨论。

#### 验收标准

1. WHEN User 提交回复请求时，THE System SHALL 验证用户已登录
2. WHEN User 提交回复请求时，THE System SHALL 验证帖子状态为 APPROVED
3. WHEN User 提交回复请求时，THE System SHALL 验证回复内容长度在 5-1000 个字符之间
4. WHEN 回复成功时，THE System SHALL 增加帖子回复数
5. WHEN 回复成功时，THE System SHALL 发送通知给帖子作者
6. THE System SHALL 对回复内容进行敏感词过滤
7. THE System SHALL 限制用户每分钟回复数量不超过 5 条

---

### 需求 28：用户封禁

**用户故事**: 作为管理员，我希望封禁违规用户，以便维护平台秩序。

#### 验收标准

1. WHEN Admin 封禁用户时，THE System SHALL 验证管理员拥有 system:user:ban 权限
2. WHEN Admin 封禁用户时，THE System SHALL 更新用户状态为 BANNED
3. WHEN Admin 封禁用户时，THE System SHALL 记录封禁原因和封禁时长
4. WHEN 被封禁用户尝试登录时，THE System SHALL 返回错误码 1004 和错误消息"账号已被封禁"
5. WHEN 封禁时长到期时，THE System SHALL 自动解封用户
6. THE System SHALL 记录封禁日志（封禁人、被封禁人、封禁时间、封禁原因）
7. THE System SHALL 发送封禁通知给被封禁用户

---

### 需求 29：系统配置管理

**用户故事**: 作为超级管理员，我希望动态修改系统配置，以便灵活调整系统行为。

#### 验收标准

1. WHEN Admin 修改系统配置时，THE System SHALL 验证管理员拥有 system:config:update 权限
2. WHEN Admin 修改系统配置时，THE System SHALL 验证配置值的合法性
3. WHEN 配置修改成功时，THE System SHALL 更新 Redis 缓存中的配置
4. WHEN 配置修改成功时，THE System SHALL 记录配置变更日志（修改人、修改时间、修改内容）
5. THE System SHALL 支持配置热更新，无需重启服务
6. THE System SHALL 支持配置回滚到历史版本

---

### 需求 30：审计日志

**用户故事**: 作为系统管理员，我希望查看系统审计日志，以便追踪关键操作。

#### 验收标准

1. THE System SHALL 记录所有封禁、解封、删除、恢复操作
2. THE System SHALL 记录所有配置变更操作
3. THE System SHALL 记录所有敏感资料访问操作
4. THE System SHALL 在审计日志中包含操作人、操作时间、操作类型、操作对象、操作结果
5. WHEN Admin 查询审计日志时，THE System SHALL 支持按时间范围、操作人、操作类型筛选
6. THE System SHALL 将审计日志持久化到数据库
7. THE System SHALL 保留审计日志至少 1 年

---

## 非功能性需求

### 需求 31：系统性能要求

**用户故事**: 作为用户，我希望系统响应迅速，以便获得良好的使用体验。

#### 验收标准

1. THE System SHALL 确保 API 接口平均响应时间小于 200ms
2. THE System SHALL 确保 API 接口 P95 响应时间小于 500ms
3. THE System SHALL 支持至少 1000 并发用户同时在线
4. THE System SHALL 确保数据库查询响应时间小于 100ms
5. THE System SHALL 确保 Redis 缓存命中率大于 80%

---

### 需求 32：系统安全要求

**用户故事**: 作为系统管理员，我希望系统具备完善的安全防护，以便保护用户数据安全。

#### 验收标准

1. THE System SHALL 使用 HTTPS 协议加密所有网络传输
2. THE System SHALL 对所有 SQL 查询使用参数化查询防止 SQL 注入
3. THE System SHALL 对所有用户输入进行 XSS 过滤
4. THE System SHALL 实现 CSRF Token 验证机制
5. THE System SHALL 对敏感数据（密码、手机号、身份证）使用 AES-256 加密存储
6. THE System SHALL 限制单个 IP 每分钟 API 调用次数不超过 100 次

---

### 需求 33：系统可用性要求

**用户故事**: 作为运维人员，我希望系统具备高可用性，以便减少服务中断。

#### 验收标准

1. THE System SHALL 确保系统可用性达到 99.9%（年停机时间小于 8.76 小时）
2. THE System SHALL 实现数据库主从复制，确保数据冗余
3. THE System SHALL 实现 Redis 哨兵模式，确保缓存高可用
4. WHEN 系统异常时，THE System SHALL 在 5 分钟内发送告警通知
5. THE System SHALL 每天自动备份数据库，保留最近 30 天备份

---

## 支付与通知功能需求

### 需求 34：微信支付集成（黑盒模式）

**用户故事**: 作为买家，我希望使用微信扫码支付，以便快速完成交易。

#### 验收标准

1. WHEN User 选择微信支付时，THE System SHALL 调用微信统一下单 API 生成支付二维码
2. WHEN User 扫码支付成功时，THE System SHALL 接收微信支付回调通知
3. WHEN 接收到支付回调时，THE System SHALL 验证签名确保请求来自微信服务器
4. WHEN 支付成功时，THE System SHALL 更新订单状态为 PAID
5. IF 支付回调验证失败，THEN THE System SHALL 记录异常日志并拒绝处理
6. THE System SHALL 使用微信沙箱环境进行测试
7. THE System SHALL 支持支付超时自动关闭订单（30 分钟）

---

### 需求 35：微信支付集成（白盒模式）

**用户故事**: 作为开发人员，我希望使用微信支付白盒测试，以便在开发环境快速验证支付流程。

#### 验收标准

1. WHEN 开发环境启用白盒模式时，THE System SHALL 模拟微信支付回调
2. WHEN 白盒模式下提交支付时，THE System SHALL 在 3 秒后自动触发支付成功回调
3. THE System SHALL 通过配置文件控制白盒/黑盒模式切换
4. THE System SHALL 在白盒模式下记录详细的支付流程日志
5. THE System SHALL 确保白盒模式仅在开发和测试环境可用

---

### 需求 36：支付宝支付集成

**用户故事**: 作为买家，我希望使用支付宝支付，以便选择更多支付方式。

#### 验收标准

1. WHEN User 选择支付宝支付时，THE System SHALL 调用支付宝统一下单 API
2. WHEN User 完成支付时，THE System SHALL 接收支付宝异步通知
3. WHEN 接收到支付通知时，THE System SHALL 验证签名确保请求来自支付宝服务器
4. THE System SHALL 支持支付宝沙箱环境测试
5. THE System SHALL 记录所有支付请求和响应日志

---

### 需求 37：消息通知系统

**用户故事**: 作为用户，我希望及时收到系统通知，以便了解订单状态和互动消息。

#### 验收标准

1. WHEN 订单状态变更时，THE System SHALL 发送站内消息通知给买家和卖家
2. WHEN 帖子被回复时，THE System SHALL 发送站内消息通知给帖子作者
3. WHEN 用户被@提及时，THE System SHALL 发送站内消息通知
4. THE System SHALL 支持消息已读/未读状态管理
5. THE System SHALL 支持消息批量标记已读
6. THE System SHALL 将未读消息数量缓存到 Redis
7. WHERE 用户开启邮件通知，THE System SHALL 同时发送邮件通知

---

### 需求 38：文件上传功能

**用户故事**: 作为用户，我希望上传图片和文件，以便发布物品和帖子。

#### 验收标准

1. WHEN User 上传文件时，THE System SHALL 验证文件类型为图片格式（jpg、png、gif、webp）
2. WHEN User 上传文件时，THE System SHALL 验证单个文件大小不超过 5MB
3. WHEN User 上传文件时，THE System SHALL 生成唯一文件名防止覆盖
4. WHEN 文件上传成功时，THE System SHALL 返回文件访问 URL
5. THE System SHALL 对上传的图片进行压缩处理（宽度大于 1920px 时压缩）
6. THE System SHALL 生成缩略图（200x200）用于列表展示
7. THE System SHALL 使用本地文件系统存储文件（开发环境）
8. WHERE 生产环境，THE System SHALL 使用对象存储服务（如阿里云 OSS）

---

## 扩展业务功能需求

### 需求 39：物品收藏功能

**用户故事**: 作为买家，我希望收藏感兴趣的物品，以便后续查看。

#### 验收标准

1. WHEN User 收藏物品时，THE System SHALL 验证用户已登录
2. WHEN User 收藏物品时，THE System SHALL 验证物品状态为 APPROVED
3. IF 物品已被收藏，THEN THE System SHALL 返回错误码 4001 和错误消息"已收藏该物品"
4. WHEN 收藏成功时，THE System SHALL 增加物品收藏数
5. WHEN User 取消收藏时，THE System SHALL 减少物品收藏数
6. WHEN User 查看收藏列表时，THE System SHALL 返回所有已收藏的物品
7. THE System SHALL 支持按收藏时间排序

---

### 需求 40：内容举报功能

**用户故事**: 作为用户，我希望举报违规内容，以便维护平台秩序。

#### 验收标准

1. WHEN User 提交举报时，THE System SHALL 验证用户已登录
2. WHEN User 提交举报时，THE System SHALL 验证举报原因长度在 10-200 个字符之间
3. WHEN User 提交举报时，THE System SHALL 验证举报类型（违法信息、虚假信息、侵权内容、其他）
4. WHEN 举报提交成功时，THE System SHALL 设置举报状态为 PENDING（待处理）
5. WHEN Admin 处理举报时，THE System SHALL 更新举报状态为 PROCESSED（已处理）
6. WHEN 举报被确认时，THE System SHALL 对被举报内容进行下架处理
7. THE System SHALL 限制用户每天举报次数不超过 10 次

---

### 需求 41：用户积分系统

**用户故事**: 作为用户，我希望通过活跃行为获得积分，以便兑换奖励。

#### 验收标准

1. WHEN User 完成注册时，THE System SHALL 赠送 100 初始积分
2. WHEN User 每日首次登录时，THE System SHALL 赠送 10 签到积分
3. WHEN User 发布物品被审核通过时，THE System SHALL 赠送 20 积分
4. WHEN User 完成交易时，THE System SHALL 赠送 50 积分
5. WHEN User 发布帖子被审核通过时，THE System SHALL 赠送 10 积分
6. WHEN User 积分变动时，THE System SHALL 记录积分流水（变动类型、变动数量、变动时间）
7. THE System SHALL 支持查询积分余额和积分流水

---

### 需求 42：优惠券系统

**用户故事**: 作为买家，我希望使用优惠券抵扣订单金额，以便节省开支。

#### 验收标准

1. WHEN Admin 创建优惠券时，THE System SHALL 验证优惠券类型（满减券、折扣券）
2. WHEN Admin 创建优惠券时，THE System SHALL 设置优惠券有效期和使用条件
3. WHEN User 领取优惠券时，THE System SHALL 验证优惠券库存充足
4. WHEN User 领取优惠券时，THE System SHALL 验证用户未超过领取上限
5. WHEN User 下单时使用优惠券，THE System SHALL 验证优惠券有效期和使用条件
6. WHEN 订单使用优惠券时，THE System SHALL 计算优惠后的实付金额
7. WHEN 订单取消时，THE System SHALL 退还已使用的优惠券
8. THE System SHALL 限制每个订单只能使用一张优惠券


---

## 即时通讯功能需求

### 需求 43：私信会话管理

**用户故事**: 作为用户，我希望与其他用户进行私信交流，以便沟通交易细节或交流问题。

#### 验收标准

1. WHEN User 发起私信时，THE System SHALL 验证用户已登录
2. WHEN User 发起私信时，THE System SHALL 创建或获取与目标用户的会话
3. WHEN 会话创建时，THE System SHALL 生成唯一会话 ID
4. WHEN User 查看会话列表时，THE System SHALL 返回所有参与的会话，按最后消息时间倒序排列
5. THE System SHALL 在会话列表中显示对方用户信息、最后一条消息内容和未读消息数
6. THE System SHALL 支持删除会话（仅删除当前用户的会话记录）
7. THE System SHALL 将会话列表缓存到 Redis，缓存时间 5 分钟

---

### 需求 44：私信消息发送

**用户故事**: 作为用户，我希望发送文字和图片消息，以便与其他用户交流。

#### 验收标准

1. WHEN User 发送消息时，THE System SHALL 验证用户已登录
2. WHEN User 发送文字消息时，THE System SHALL 验证消息内容长度在 1-1000 个字符之间
3. WHEN User 发送图片消息时，THE System SHALL 验证图片格式和大小（jpg、png、gif，最大 5MB）
4. WHEN 消息发送成功时，THE System SHALL 记录消息 ID、发送者 ID、接收者 ID、消息类型、消息内容和发送时间
5. WHEN 消息发送成功时，THE System SHALL 更新会话的最后消息时间
6. WHEN 消息发送成功时，THE System SHALL 增加接收者的未读消息数
7. THE System SHALL 对消息内容进行敏感词过滤
8. THE System SHALL 限制用户每分钟发送消息数量不超过 20 条

---

### 需求 45：实时消息推送（WebSocket）

**用户故事**: 作为用户，我希望实时接收新消息，以便及时回复对方。

#### 验收标准

1. WHEN User 登录系统时，THE System SHALL 建立 WebSocket 连接
2. WHEN User 建立 WebSocket 连接时，THE System SHALL 验证 JWT Token 有效性
3. WHEN 用户收到新消息时，THE System SHALL 通过 WebSocket 实时推送消息
4. WHEN WebSocket 连接断开时，THE System SHALL 自动尝试重连（最多 3 次）
5. IF WebSocket 不可用，THEN THE System SHALL 降级为轮询模式（每 5 秒查询一次）
6. THE System SHALL 在 WebSocket 消息中包含消息 ID、发送者信息、消息类型、消息内容和发送时间
7. THE System SHALL 使用心跳机制保持 WebSocket 连接活跃（每 30 秒发送一次心跳）

---

### 需求 46：消息已读状态

**用户故事**: 作为用户，我希望看到消息的已读状态，以便知道对方是否看到了我的消息。

#### 验收标准

1. WHEN User 查看消息时，THE System SHALL 自动标记消息为已读
2. WHEN 消息标记为已读时，THE System SHALL 减少接收者的未读消息数
3. WHEN 消息标记为已读时，THE System SHALL 通过 WebSocket 通知发送者
4. THE System SHALL 支持批量标记会话中所有消息为已读
5. THE System SHALL 在消息列表中显示消息已读/未读状态
6. THE System SHALL 显示消息的已读时间

---

### 需求 47：消息历史记录

**用户故事**: 作为用户，我希望查看历史聊天记录，以便回顾之前的交流内容。

#### 验收标准

1. WHEN User 查看聊天记录时，THE System SHALL 返回指定会话的所有消息
2. THE System SHALL 支持分页加载历史消息（每页 20 条）
3. THE System SHALL 支持按时间倒序或正序排列消息
4. THE System SHALL 在消息中显示发送者头像、昵称、消息内容和发送时间
5. THE System SHALL 区分显示自己发送和接收的消息
6. THE System SHALL 缓存最近 50 条消息到 Redis，缓存时间 10 分钟
7. THE System SHALL 保留消息历史记录至少 6 个月

---

### 需求 48：消息撤回功能

**用户故事**: 作为用户，我希望撤回发送错误的消息，以便纠正失误。

#### 验收标准

1. WHEN User 撤回消息时，THE System SHALL 验证消息归属于当前用户
2. WHEN User 撤回消息时，THE System SHALL 验证消息发送时间在 2 分钟内
3. IF 消息发送超过 2 分钟，THEN THE System SHALL 返回错误码 5001 和错误消息"消息已超过撤回时限"
4. WHEN 消息撤回成功时，THE System SHALL 更新消息状态为 RECALLED（已撤回）
5. WHEN 消息撤回成功时，THE System SHALL 通过 WebSocket 通知接收者
6. THE System SHALL 在聊天记录中显示"你撤回了一条消息"或"对方撤回了一条消息"
7. THE System SHALL 记录消息撤回日志

---

### 需求 49：消息搜索功能

**用户故事**: 作为用户，我希望搜索历史消息，以便快速找到需要的信息。

#### 验收标准

1. WHEN User 搜索消息时，THE System SHALL 验证用户已登录
2. WHEN User 搜索消息时，THE System SHALL 在用户参与的所有会话中进行全文搜索
3. THE System SHALL 支持按关键词模糊匹配消息内容
4. THE System SHALL 支持按时间范围筛选搜索结果
5. THE System SHALL 支持按会话筛选搜索结果
6. THE System SHALL 在搜索结果中高亮显示匹配的关键词
7. THE System SHALL 支持分页显示搜索结果（每页 20 条）

---

### 需求 50：消息通知提醒

**用户故事**: 作为用户，我希望收到新消息的通知提醒，以便及时查看消息。

#### 验收标准

1. WHEN User 收到新私信时，THE System SHALL 发送站内通知
2. WHEN User 收到新私信时，THE System SHALL 在页面标题显示未读消息数（如 "(3) 校园集市"）
3. WHEN User 收到新私信时，THE System SHALL 播放提示音（可在设置中关闭）
4. WHERE 用户开启浏览器通知，THE System SHALL 发送浏览器桌面通知
5. WHERE 用户开启邮件通知，THE System SHALL 发送邮件提醒（每小时最多 1 封）
6. THE System SHALL 在导航栏显示未读私信总数的红点提示
7. THE System SHALL 支持用户在设置中自定义通知方式

---

### 需求 51：黑名单功能

**用户故事**: 作为用户，我希望屏蔽骚扰用户，以便避免收到不想要的消息。

#### 验收标准

1. WHEN User 拉黑其他用户时，THE System SHALL 验证用户已登录
2. WHEN User 拉黑其他用户时，THE System SHALL 将目标用户添加到黑名单
3. WHEN 被拉黑用户尝试发送消息时，THE System SHALL 返回错误码 5002 和错误消息"无法发送消息"
4. WHEN User 查看黑名单时，THE System SHALL 返回所有被拉黑的用户列表
5. WHEN User 移除黑名单时，THE System SHALL 恢复与该用户的正常通讯
6. THE System SHALL 限制黑名单用户数量不超过 100 个
7. THE System SHALL 记录拉黑和移除黑名单的操作日志

---

### 需求 52：快捷回复功能

**用户故事**: 作为卖家，我希望使用快捷回复模板，以便快速回复买家的常见问题。

#### 验收标准

1. WHEN User 创建快捷回复时，THE System SHALL 验证回复内容长度在 5-200 个字符之间
2. WHEN User 创建快捷回复时，THE System SHALL 验证快捷回复数量不超过 20 个
3. THE System SHALL 支持为快捷回复设置标题（如"商品在吗"、"可以优惠吗"）
4. WHEN User 发送消息时，THE System SHALL 提供快捷回复选项
5. WHEN User 选择快捷回复时，THE System SHALL 自动填充回复内容到输入框
6. THE System SHALL 支持编辑和删除快捷回复
7. THE System SHALL 为新用户提供默认快捷回复模板
