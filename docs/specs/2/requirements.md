# 校园轻享集市系统 - Spec #2 需求文档（后端扩展）

> 编号范围：53-68（承接 Spec #1 的 1-52）
> 目标：在现有核心功能之上，完善多校区、搜索推荐、订单后处理、合规与可观测等能力。

---

## 阶段二交付小结（2025-10-28）

- T3 推荐与榜单：已达成验收标准（热度评分模型+Redis 缓存、个性化召回与回退、缓存降级、定时与手动刷新）。
- T4 售后与退款：已达成验收标准（退款状态机与审计、支付宝退款与回调幂等、失败重试、每日对账日志）。
- T5 订单取消与超时：已达成验收标准（显式取消/自动关闭、幂等与并发控制、优惠券回退、通知与审计）。
- 指标与SLO：推荐接口命中缓存 P95 < 100ms；退款回调处理幂等；调度任务均加分布式锁。
- 风险与预案：渠道波动通过重试与对账兜底；推荐可通过停任务+清键快速回退至直查。

## 需求 53：多校区与可见性隔离

**用户故事**：作为跨校部署的运维/管理员，我希望支持多校区隔离与跨校管理，以满足多校园场景的合规与管理需求。

**验收标准**：
1. THE System SHALL 引入 Campus 实体（id、code、name、status、created_at），并为 User/Goods/Post/Order 等核心实体增加 campus_id 外键（默认同用户所属校区）。
2. WHEN 普通用户查询/操作数据时，THE System SHALL 仅返回/允许操作与其 campus_id 相同的记录（强隔离）；管理员可跨校区操作需具备 `system:campus:cross` 权限。
3. THE System SHALL 为所有新增 campus_id 字段建立联合索引（如 status+campus_id、created_at+campus_id）以确保查询性能。
4. WHEN 新用户注册时，THE System SHALL 必须绑定 campus_id；若未显式提供，则按系统默认校区策略赋值并记录审计日志。
5. THE System SHALL 提供 Campus 管理接口（CRUD、启停用、用户迁移统计），并在删除前进行约束校验（禁止删除仍有关联数据的校区）。

---

## 需求 54：全文检索与高亮（PostgreSQL FTS）

**用户故事**：作为买家/论坛用户，我希望快速搜索到相关物品和帖子，并在结果中看到关键词高亮。

**验收标准**：
1. THE System SHALL 为 Goods(title, description) 与 Post(title, content) 建立 tsvector 列及触发器，使用中文分词配置，创建 GIN 索引并支持 campus_id 过滤。
2. WHEN 执行搜索时，THE System SHALL 支持关键词、分类、价格区间、时间范围等组合筛选，并按 ts_rank 排序；空查询返回 400。
3. THE System SHALL 返回高亮片段（title/content 片段中关键词 <em>...</em> 包裹），并限制片段长度与数量以防止响应过大。
4. THE System SHALL 提供统一搜索接口（/api/search）支持类型聚合（goods/posts）与单类型查询，默认分页 20 条；支持按校区隔离。
5. THE System SHALL 记录搜索日志（用户、关键词、耗时、命中数）用于后续推荐与监控。

---

## 需求 55：推荐与榜单（Redis 缓存 + 定时任务）

**用户故事**：作为用户，我希望在首页看到热门榜单与“你可能感兴趣”的推荐，且结果新鲜、响应快。

**验收标准**：
1. THE System SHALL 定义热度评分模型（如 view 0.5 + favorite 0.3 + recent_sold 0.2）按校区每日计算 TOP N，结果缓存至 Redis（Key 含 campus）。
2. THE System SHALL 提供个性化推荐的基础能力：基于用户最近浏览/收藏类目做类目近邻召回；无历史时回退至热门榜单。
3. WHEN 缓存失效或计算任务失败，THE System SHALL 回退至最新可用结果并记录告警日志；缓存 TTL 默认 5 分钟。
4. THE System SHALL 提供榜单/推荐接口，平均响应时间 < 100ms（命中缓存）。
5. THE System SHALL 提供定时任务刷新与手动重建接口，任务需加分布式锁防止并发执行。

---

## 需求 56：售后与退款流程（含渠道对账）

**用户故事**：作为买家/卖家/管理员，我希望订单在支付后可发起售后申请与退款流程，系统需与支付渠道完成状态对齐与对账。

**验收标准**：
1. THE System SHALL 支持退款申请（REFUND_REQUEST：id、order_no、reason、evidence、status、applicant、created_at、updated_at），状态机：APPLIED→APPROVED/REJECTED→REFUNDING→REFUNDED/FAILED。
2. WHEN 申请退款时，THE System SHALL 校验订单归属与状态（PAID/SHIPPED 可退的业务规则），并在 7 天内允许申请（阈值可配置）。
3. THE System SHALL 集成微信/支付宝退款 API，并实现回调处理、签名校验、幂等校验（基于 refund_request_no），一致性更新订单状态与退款单状态。
4. THE System SHALL 记录完整退款审计日志与支付网关交互日志，敏感字段脱敏存储；失败自动重试（指数退避），超过阈值告警。
5. THE System SHALL 提供对账任务：按日拉取渠道账单与本地订单/退款状态核对，生成差异报告与修复建议。

---

## 需求 57：订单取消与超时增强（显式取消/自动关闭/幂等）

**用户故事**：作为买家或卖家，我希望在明确条件下可取消订单，系统应自动关闭超时未支付订单且处理并发与重复请求。

**验收标准**：
1. THE System SHALL 支持买家在 PENDING_PAYMENT 时显式取消；卖家在未发货前可取消（规则可配置），并以事件驱动恢复物品可售状态。
2. THE System SHALL 通过定时任务自动关闭超过 30 分钟未支付的订单，确保幂等（基于 order_no 并发控制）。
3. WHEN 取消/关闭订单时，THE System SHALL 发送站内/邮件通知给双方，并记录审计日志。
4. THE System SHALL 提供取消接口幂等保障（Idempotency-Key 请求头），重复请求返回同一结果。
5. THE System SHALL 对于使用了优惠券的订单，取消时恢复优惠券可用状态，事务一致。

---

## 需求 58：通知渠道扩展（邮件/WebPush，模板与退订）

**用户故事**：作为用户，我希望能以站内信、邮件或浏览器推送接收事件通知，并可自定义偏好与退订。

**验收标准**：
1. THE System SHALL 定义通知模板（code、title、content、i18nKey、channel 可用性）与用户通知偏好（user_id、channel、enabled、quiet_hours）。
2. THE System SHALL 支持邮件与 WebPush 发送接口，具备速率限制与失败重试；邮件需支持批量与队列异步发送。
3. THE System SHALL 支持用户退订与静默时段设置；退订后相关事件不再触达对应渠道。
4. THE System SHALL 在关键业务事件（订单状态变更、帖子回复、@ 提及等）触发通知分发，失败记录并可重试。
5. THE System SHALL 提供统一通知查询与追踪接口（状态、渠道、失败原因）。

---

## 需求 59：内容合规模块（敏感词库 + 图片安全扫描，可插拔）

**用户故事**：作为管理员，我希望系统在发布物品/帖子/消息时进行文本与图片合规检查，并可按环境启用/禁用。

**验收标准**：
1. THE System SHALL 在发布与更新流程中接入文本敏感词检测（DFA），命中阈值时阻断或打回 PENDING_REVIEW 状态；规则可配置。
2. THE System SHALL 预留图片安全扫描接口（抽象 SPI），支持对接第三方服务；开发/测试可禁用或降级为异步告警。
3. THE System SHALL 记录合规审计日志（对象、字段片段、命中规则、策略动作、操作者）。
4. THE System SHALL 支持白名单与灰度策略配置（Feature Flag 关联）。
5. WHEN 外部扫描超时/失败时，THE System SHALL 降级为人工审核并生成待处理队列。

---

## 需求 60：限流与反滥用（滑动窗口，标准 429）

**用户故事**：作为平台方，我希望对高风险接口进行限流与防刷，保护系统稳定与公平使用。

**验收标准**：
1. THE System SHALL 提供注解式限流（@RateLimit）支持滑动窗口与令牌桶模式，支持维度：IP、用户、接口、校区。
2. WHEN 触发限流时，THE System SHALL 返回 429 状态码与统一错误体，并返回 RateLimit-* 响应头（limit、remaining、reset）。
3. THE System SHALL 提供黑白名单机制与管理接口，支持精确/前缀匹配；管理员可临时豁免。
4. THE System SHALL 限流规则可动态刷新（无需重启），并记录命中统计与告警。
5. THE System SHALL 对登录、发帖、私信发送等关键接口默认启用限流策略。

---

## 需求 61：数据导出与报表（CSV 流式导出，异步）

**用户故事**：作为管理员/卖家，我希望导出订单、物品、用户等数据生成 CSV 报表，且不阻塞请求。

**验收标准**：
1. THE System SHALL 提供异步导出任务（EXPORT_JOB：id、type、params、status、file_path、created_by、created_at、finished_at）。
2. THE System SHALL 采用流式写出（分批分页、内存可控），生成临时文件并提供签名下载链接（有效期可配置）。
3. THE System SHALL 校验导出权限与校区隔离；导出参数需校验范围上限（如时间窗口、最大行数）。
4. THE System SHALL 记录导出审计日志，包含筛选条件与条数；导出文件名包含掩码化的参数摘要。
5. THE System SHALL 提供任务查询、取消与清理接口；失败可重试并记录原因。

---

## 需求 62：定时任务与系统任务框架（分布式锁、可观测）

**用户故事**：作为运维/管理员，我希望系统的周期性任务可配置、可观测且不会重复执行。

**验收标准**：
1. THE System SHALL 提供统一任务框架：任务注册、执行记录、重试次数、状态查询；任务执行前获取分布式锁（Redis）。
2. THE System SHALL 记录任务指标（执行耗时、成功/失败次数），并暴露到监控系统；失败发送告警。
3. THE System SHALL 支持手动触发、暂停与恢复任务；支持参数化运行。
4. THE System SHALL 对关键任务（热门榜单刷新、订单超时关闭、物化视图刷新、对账）进行内置实现。
5. THE System SHALL 每个任务具备幂等保护与并发度限制配置。

---

## 需求 63：Feature Flags 灰度开关（环境/用户/校区）

**用户故事**：作为产品与运维，我希望在不发布代码的情况下灰度开启/关闭功能，并按环境、用户、校区精准控制。

**验收标准**：
1. THE System SHALL 定义 FeatureFlag 实体（key、description、enabled、scopes：env/users/campuses、updated_at），并提供管理接口与审计。
2. THE System SHALL 在业务路径中集成开关评估器，默认关闭；支持本地缓存与动态刷新。
3. THE System SHALL 在接口层返回实验标识响应头以便前端埋点；评估逻辑需可测试与可回放。
4. THE System SHALL 与合规、通知、推荐等模块联动，实现灰度发布与 A/B 实验能力。
5. THE System SHALL 开关读取失败时采用安全回退（默认关闭）。

---

## 需求 64：国际化 i18n（后端消息与回退）

**用户故事**：作为多语言用户，我希望后端返回的错误与通知文本按照请求语言返回，并在缺失时有合理回退。

**验收标准**：
1. THE System SHALL 基于 Accept-Language 解析语言，支持至少 zh-CN 与 en-US，并可配置默认语言与回退顺序。
2. THE System SHALL 将错误码与通知模板抽取为资源文件，按语言加载；缺失键使用默认语言并记录警告。
3. THE System SHALL 在审计/日志中记录语言上下文，便于问题追踪。
4. THE System SHALL 为邮件/WebPush 渠道提供 i18n 模板渲染能力。
5. THE System SHALL 为测试提供固化快照用例，验证关键错误信息的多语言输出。

---

## 需求 65：可观测性与指标（结构化日志、追踪、Prometheus）

**用户故事**：作为运维，我希望系统具备统一追踪 ID、结构化日志与关键业务指标，并对外暴露监控端点。

**验收标准**：
1. THE System SHALL 为每个请求生成/透传 traceId，写入 MDC，并在所有日志中输出；跨线程/异步场景需保持上下文。
2. THE System SHALL 采用结构化日志输出（JSON 行），区分业务日志与错误日志；敏感数据脱敏。
3. THE System SHALL 暴露 Prometheus 指标（QPS、P95、错误率、任务执行、限流命中、搜索耗时等）。
4. THE System SHALL 为关键接口添加计数/直方图指标，提供标签维度：campus、status、result。
5. THE System SHALL 对异常链路记录错误堆栈与关键信息，支持错误分级告警。

---

## 需求 66：数据隐私与合规（导出与注销、最小化留存）

**用户故事**：作为用户，我希望能导出/删除我的个人数据；作为平台方，我希望满足数据最小化和合规要求。

**验收标准**：
1. THE System SHALL 提供用户数据导出（打包 JSON/CSV）与账户注销流程（延迟删除+冷静期），并进行身份强校验。
2. THE System SHALL 对敏感数据最小化留存（仅保留合规要求的账务与审计数据），其余进行匿名化/脱敏。
3. THE System SHALL 在导出与删除过程中记录审计日志与操作原因；删除后相关访问应返回 404/匿名化数据。
4. THE System SHALL 提供数据保留策略配置与定期清理任务。
5. THE System SHALL 为隐私相关操作提供二次确认与撤销窗口（配置化）。

---

## 需求 67：分类与标签体系（多级类目、标签检索）

**用户故事**：作为卖家/买家，我希望商品/帖子具备多级分类与标签，支持基于标签的检索与推荐。

**验收标准**：
1. THE System SHALL 支持多级类目（Category 树）与多标签（Tag）模型；Goods/Post 与 Tag 多对多关联。
2. THE System SHALL 在查询与搜索中支持按类目与标签筛选，联合索引保障性能；标签上限与校验可配置。
3. THE System SHALL 提供类目与标签管理接口（CRUD、排序、合并、禁用）。
4. THE System SHALL 将标签纳入推荐召回特征，纳入热度计算权重。
5. THE System SHALL 为标签/类目返回 i18n 名称与路径信息。

---

## 需求 68：关注与订阅（关注卖家/关键词订阅）

**用户故事**：作为买家，我希望关注感兴趣的卖家或订阅关键词，当有新发布时收到通知。

**验收标准**：
1. THE System SHALL 支持关注卖家（Follow：follower_id、seller_id、created_at）与关键词订阅（Subscribe：user_id、keyword、campus_id、created_at）。
2. WHEN 关注的卖家发布新商品/帖子或匹配关键词时，THE System SHALL 触发通知（站内/邮件/WebPush，尊重偏好与退订）。
3. THE System SHALL 提供我的关注与订阅管理接口（列表、取消、去重与限额校验）。
4. THE System SHALL 提供冷启动策略：新用户默认推荐关注项与关键词建议。
5. THE System SHALL 对订阅通知进行去重与频控（如每小时最多 N 条/每关键词每天最多一次）。

