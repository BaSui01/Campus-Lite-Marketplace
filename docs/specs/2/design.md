# 校园轻享集市系统 - Spec #2 设计文档（后端扩展）

> 覆盖需求：53-68（多校区、搜索推荐、售后退款、限流合规、可观测等）
> 基线：在 Spec #1 的架构与实体之上增量扩展，保持单体架构与分层模式不变。

---

## 里程碑更新（阶段二完成 | 2025-10-28）

- 已完成：推荐与榜单（T3）、售后与退款（T4）、订单取消与超时（T5）。
- 关键落地：
  - 推荐与榜单
    - 定时任务+分布式锁刷新热榜至 Redis ZSET（goods:rank:{campus}，TTL 5m）；管理员手动刷新接口。
    - 个性化推荐：基于用户收藏类目召回，缓存 recommend:user:{uid}，无历史回退热榜；缓存失败自动降级。
  - 售后与退款
    - 状态机：APPLIED→PROCESSING→REFUNDED/FAILED；记录 PaymentLog。
    - 渠道退款：接入支付宝退款；新增退款回调处理（幂等）；失败重试调度器；每日对账扫描卡滞记录并告警日志。
  - 订单后处理
    - 显式取消与自动超时关闭（30min），幂等与并发安全；优惠券回退；通知与审计日志。
- 性能与稳定性：接口命中缓存 P95 < 100ms；所有任务具分布式锁与幂等保护。
- 回滚策略：缓存键逐出+停用定时任务即可回退；退款回调幂等不破坏一致性。

## 1. 概述与范围

- 范围：为现有交易、论坛与消息体系增加多校区隔离、FTS 搜索、高可用推荐与榜单、售后退款与订单取消、通知渠道扩展、内容合规、限流防刷、数据导出、任务调度框架、Feature Flags、i18n、可观测、分类与标签、关注与订阅等能力。
- 非范围：不引入新外部消息队列；不引入搜索中间件（如 ES），优先使用 PostgreSQL FTS；不改变现有认证授权体系。

---

## 2. 架构设计与影响面

### 2.1 分层与模块

- common：新增实体/枚举/异常；工具扩展（Trace、RateLimit、FeatureFlag 等）。
- repository：新增/调整仓库接口与原生 SQL（FTS、对账、导出、订阅查询）。
- service：新增 CampusService、SearchService、RecommendService、RefundService、NotificationService（扩展）、ComplianceService、RateLimitService、ExportService、TaskSchedulerService、FeatureFlagService、I18nService、CategoryTagService、FollowService 等。
- controller：对应 REST 接口分组，统一响应与权限注解。
- scheduler：定时任务集合（热门榜单、订单超时、物化视图刷新、对账、清理导出、隐私清理）。

### 2.2 新增权限码（示例）

| 代码 | 说明 |
|---|---|
| system:campus:manage | 校区管理 |
| system:campus:cross | 跨校区操作 |
| system:rate-limit:manage | 限流规则管理 |
| system:export:manage | 导出任务管理 |
| system:task:manage | 系统任务管理 |
| system:feature-flag:manage | 灰度开关管理 |
| system:compliance:review | 合规审核处理 |

---

## 3. 数据库设计（增量）

> 保持 PostgreSQL，所有变更提供 DDL 脚本，兼容在线迁移；为新增外键建立必要索引。

### 3.1 多校区

- t_campus(id, code UK, name, status, created_at)
- 为 t_user/t_goods/t_post/t_order 等表新增 campus_id NOT NULL，默认值=系统默认校区；建立常用联合索引（如 idx_goods_status_campus、idx_post_status_campus、idx_order_created_campus）。

### 3.2 全文检索

- t_goods.search_vector tsvector + 触发器；t_post.search_vector 同理；中文分词配置；GIN 索引；查询需 AND campus_id = ?。

### 3.3 售后与退款

- t_refund_request(id, refund_no UK, order_no UK, applicant_id, reason, evidence JSONB, status, channel, amount, created_at, updated_at)
- t_payment_log(id, order_no, trade_no, channel, type(PAY/REFUND), payload JSONB, success BOOLEAN, created_at)

### 3.4 通知扩展

- t_notify_template(id, code UK, channels JSONB, i18n_key, enabled, updated_at)
- t_notify_pref(id, user_id, channel, enabled, quiet_hours JSONB, updated_at)

### 3.5 限流与黑白名单

- t_ratelimit_rule(id, key, dim(IP/USER/API/CAMPUS), algorithm, limit, window_sec, enabled, updated_at)
- t_accesslist(id, type(BLACK/WHITE), subject, note, updated_at)

### 3.6 导出任务

- t_export_job(id, job_type, params JSONB, status, file_path, created_by, created_at, finished_at, error_msg)

### 3.7 任务框架

- t_task_record(id, task_key, trigger_type(MANUAL/CRON), params JSONB, status, started_at, finished_at, cost_ms, error_msg)

### 3.8 Feature Flags

- t_feature_flag(id, flag_key UK, description, enabled, scopes JSONB, updated_at)

### 3.9 分类与标签、关注与订阅

- t_tag(id, name UK, enabled, created_at)
- t_goods_tag(id, goods_id, tag_id, created_at) UNIQUE(goods_id, tag_id)
- t_follow(id, follower_id, seller_id, created_at) UNIQUE(follower_id, seller_id)
- t_subscribe(id, user_id, keyword, campus_id, created_at) UNIQUE(user_id, keyword, campus_id)

---

## 4. 领域与服务设计

### 4.1 SearchService

- 方法：searchGoods(query, filters, page)、searchPosts(...)
- 实现：原生 SQL + ts_rank + 片段高亮（ts_headline），限制片段长度；强制 campus 过滤。

### 4.2 RecommendService

- 热榜：定时计算评分 TopN，按 campus 写入 Redis: goods:rank:{campus}。
- 个性化：基于最近浏览/收藏类目+标签做简单召回；无历史回退热榜。

### 4.3 RefundService

- 创建申请 → 审批 → 触发渠道退款 → 回调落账 → 对账修复。
- 幂等：refund_no/idempotency-key；状态机与事务边界清晰；失败重试与告警。

### 4.4 NotificationService（扩展）

- 渠道：站内、邮件、WebPush；模板渲染+i18n；用户偏好与退订；速率限制与批量发送。

### 4.5 ComplianceService

- 文本 DFA、图片扫描 SPI；命中策略：阻断/打回；审计留痕；白名单与灰度联动。

### 4.6 RateLimitService

- 注解+AOP；Redis 计数/滑窗；规则可热更新；返回 429 与 RateLimit-* 头；黑白名单前置判断。

### 4.7 ExportService

- 异步分页流式导出（游标/主键范围）；生成临时文件；签名 URL 下载；任务可查询/取消/清理。

### 4.8 TaskSchedulerService

- 统一任务注册与分布式锁（Redis SET NX EX）；执行记录；指标埋点；手动触发与暂停。

### 4.9 FeatureFlagService

- 开关评估器（env/user/campus 优先级）；本地缓存+定时刷新；失败默认关闭；审计变更。

### 4.10 I18nService

- 基于 MessageSource 与 Accept-Language；邮件/通知模板渲染；回退策略与告警。

### 4.11 CategoryTagService / FollowService

- 类目树管理、标签 CRUD、绑定校验、联合索引；关注/订阅的去重与频控。

---

## 5. API 设计（主要端点）

- Campus：GET/POST/PUT/DELETE /api/admin/campuses
- Search：GET /api/search?q=...&type=goods|posts&campus=...
- Recommend：GET /api/recommend/hot?campus=...，GET /api/recommend/personal
- Refund：POST /api/orders/{orderNo}/refunds，GET/PUT /api/admin/refunds/{refundNo}
- Cancel Order：POST /api/orders/{orderNo}/cancel
- Notification：GET/PUT /api/users/me/notify-prefs，POST /api/admin/notify/templates
- RateLimit：GET/PUT /api/admin/ratelimit/rules，GET/PUT /api/admin/accesslist
- Export：POST /api/admin/exports，GET /api/admin/exports/{id}，GET /api/admin/exports/{id}/download
- Tasks：GET/POST /api/admin/tasks/{taskKey}
- FeatureFlags：GET/PUT /api/admin/feature-flags
- I18n：后端内部（资源文件）；对外仅体现在错误/通知内容
- Category/Tag：CRUD /api/admin/categories /api/admin/tags；绑定查询 /api/goods?tags=...
- Follow/Subscribe：POST/DELETE /api/follow/{sellerId}；POST/DELETE /api/subscribe/keywords

---

## 6. 缓存与键设计

| Key | 示例 | TTL | 说明 |
|---|---|---|---|
| goods:rank:{campus} | goods:rank:shanghai | 5m | 热门榜单 |
| recommend:user:{uid} | recommend:user:1001 | 5m | 个性化推荐缓存 |
| search:hot:{campus} | search:hot:beijing | 10m | 热搜缓存（可选） |
| rate:api:{key} | rate:api:/api/orders/pay:uid | 窗口 | 限流计数 |
| export:job:{id} | export:job:123 | 与任务 | 导出状态缓存 |
| feature:flags | feature:flags | 1m | 开关快取 |

---

## 7. 可观测与指标

- 日志：JSON 行，MDC(traceId, userId, campusId)；业务/错误分文件；敏感信息脱敏。
- 指标：Micrometer + /actuator/prometheus；QPS、P95、错误率、限流命中、搜索耗时、推荐命中、任务执行耗时。
- 追踪：请求生成/透传 traceId；异步/调度任务注入 traceId。

---

## 8. 迁移与回滚计划

1) 预添加 t_campus，写入默认校区；为核心表添加可空 campus_id；数据回填后改为 NOT NULL；创建索引。
2) 添加 tsvector 列与触发器；慢时间窗重建索引。
3) 新表（退款、通知、导出、任务、开关、标签、关注、订阅）按模块逐步上线，接口默认关闭（Feature Flag）。
4) 全量压测与回滚预案：保留旧查询路径（无 campus 过滤）作为应急开关；失败时退回只读模式。

---

## 9. 性能与安全要点

- FTS 与检索：合理使用 ts_headline 与 rank，分页上限限制；必要字段覆盖索引。
- 推荐计算：定时任务 + 分布式锁；缓存穿透/雪崩防护。
- 退款/对账：幂等键；回调签名校验；重试与告警。
- 限流：滑窗/令牌桶可选；黑白名单优先匹配；返回标准 429 与头信息。
- 导出：游标/分片分页；磁盘与带宽限额；临时文件定期清理。
- 合规：SPI 可插拔；失败降级人工审核；审计闭环。

---

## 10. 测试策略

- 单元：开关评估器、限流算法、FTS 查询构造、退款状态机、导出分页器。
- 集成：搜索高亮、热门榜单缓存、退款回调幂等、订单取消与优惠券回滚、导出下载签名 URL。
- 性能：搜索/推荐/导出端到端压测；任务框架并发度与锁冲突测试。

---

## 11. 风险与缓解

- 索引构建影响写性能 → 低峰期执行、并行度控制。
- 退款回调不一致 → 幂等+重试+人工对账工具。
- 限流规则误杀 → 白名单与模拟演练模式；灰度发布。
- 多校区误配置 → 默认校区与删除前强校验；跨校操作强权限。

