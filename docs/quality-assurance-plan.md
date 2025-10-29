# 质量保障计划

## 目标
- 打通后端主流程的端到端校验链路，即便前端尚未交付，也能模拟真实用户路径。
- 在上线前完成核心业务的黑盒验收、性能与稳定性、以及安全审查，形成可追踪的交付物。

## 工作拆解

### 1. 后端端到端流程编排
- **范围**：注册登陆 → 下单 → 支付（包含第三方回调 Mock）→ 通知派发。
- **执行方式**：
  - 使用 Postman Collection 串联接口，已在 `backend/tests/e2e/campus-e2e.postman_collection.json` 完成首版脚本，依赖配套环境文件 `backend/tests/e2e/backend-e2e.postman_environment.json`。
  - 启动后端时追加 `--spring.profiles.active=e2e` 以启用 `PaymentMockController`（`/api/payment/mock/wechat`），该控制器由超级管理员触发并直接调用订单回调逻辑，可替代外部支付网关。
  - 通过 `backend/tests/e2e/run-e2e.sh`（基于 Newman）执行脚本，生成 JUnit 报告到 `backend/tests/e2e/report/e2e-results.xml`，后续可接入 CI。
  - 将 Collection 纳入 CI（GitHub Actions/Jenkins），作为 nightly job，串联 Docker 依赖及 e2e Profile。
- **负责人**：后端 Dev-A。
- **产出**：Postman 集合与环境、`PaymentMockController`、Newman 执行脚本（含报告）。
- **完成判定**：端到端脚本跑通且在 CI 中稳定执行两日无红灯，通知链路完成验证。

### 2. 黑盒验收（冒烟 + 回归）
- **范围**：
  - 正向场景：核心业务流程、管理后台操作。
  - 异常路径：权限矩阵、参数异常、状态机异常。
- **执行方式**：
  - 组建用例表（Excel/测试管理工具），标注优先级、前置条件、期望结果。
  - 执行时记录实际结果与问题单号。
- **负责人**：QA 或后端 Dev-B（缺 QA 时）。
- **产出**：`docs/testing/acceptance-cases.xlsx`、缺陷单列表。
- **完成判定**：P0/P1 缺陷全部关闭，P2 形成明确整改计划。

### 3. 性能与稳定性验证
- **范围**：下单、支付回调、通知队列等关键接口/任务。
- **执行方式**：
  - JMeter 或 Gatling 编写压测脚本，生成负载曲线。
  - docker-compose 搭建 Prometheus + Grafana 监控 CPU/Mem/DB/Redis。
  - 长时间运行（≥1 小时）模拟真实峰值，观察资源和日志。
  - 性能专用编排：`docker/docker-compose.perf.yml` 启动 `backend`（perf Profile）、Postgres、Redis、Prometheus、Grafana。
  - 数据预置接口：`POST /api/perf/orders/timeout/seed?count=200`（perf Profile + SUPER_ADMIN）批量构造超时订单。
  - 压测脚本：`backend/tests/jmeter/order_timeout_performance.jmx`，通过 `run-order-timeout-perf.sh` 触发 Docker 化 JMeter。
- **压测步骤**：
  1. 基于历史流量与业务预测，构建下单、支付回调、通知派发三类典型事务并设定权重。
  2. 进行 10 分钟预热以建立缓存、连接池、消息队列基线，随后以 10% 梯度逐步提升至目标峰值的 120%。
  3. 峰值阶段持续 60 分钟，期间同步抓取应用日志、线程堆栈与数据库执行计划，保留监控快照。
- **观测指标**：
  - 服务端：CPU ≤ 70%，JVM Heap ≤ 75%，GC Stop-the-World 累计 < 2s/10min。
  - 数据库：慢查询 < 1 条/10min，连接池占用 < 80%，锁等待 < 200ms。
  - 接口：关键 API P95 ≤ 2s，错误率 ≤ 0.1%，消息队列滞留量 < 500 条。
- **风险应对**：压测过程中若出现瓶颈，需即时标注时间点与实例，采集 Thread Dump/Heap Dump，24 小时内提交调优方案与复测计划。
- **负责人**：DevOps / 后端 Dev-C。
- **产出**：压测脚本、Grafana 截图、性能瓶颈分析与调优结论。
- **完成判定**：关键接口在目标 TPS 下无错误，资源使用率可控（CPU <70%，响应 P95 < 2s）。

### 4. 安全审查

**范围**：代码安全、依赖安全、配置安全。

**执行方式**：
- 静态扫描：SpotBugs + Sonar + OWASP Dependency Check。
- 配置审查：ENV、凭证、TLS、CORS、敏感接口鉴权。
- 参照 OWASP ASVS 中等级别完成检查项。

**依赖安全扫描流程**：
1. **自动扫描**：
   - 质量门禁：每次 Push/PR 自动运行（`.github/workflows/backend-quality-gates.yml:60-71`）
   - 定期扫描：每周一凌晨自动运行（`.github/workflows/security-weekly-scan.yml`）
   - 扫描工具：OWASP Dependency-Check 12.1.0（CVSS ≥ 7 失败构建）

2. **报告输出**：
   - HTML 报告：`backend/target/dependency-check-report.html`
   - JSON 报告：`backend/target/dependency-check-report.json`
   - SARIF 报告：上传到 GitHub Security（可视化漏洞趋势）

3. **告警机制**：
   - 发现高危漏洞（CVSS ≥ 7）自动创建 GitHub Issue
   - Issue 标签：`security` + `dependencies` + `high-priority`
   - 通知渠道：GitHub Notifications（可配置邮件/Slack）

4. **漏洞处理**：
   - 分析影响范围和可利用性
   - 升级依赖版本或替换依赖库
   - 添加抑制规则（误报或已评估的低风险）
   - 更新安全整改计划文档

**配置文件**：
- Maven 插件配置：`backend/pom.xml:436-464`
- 抑制规则：`backend/.owasp/suppressions.xml`
- 详细文档：[安全扫描使用指南](security-scanning-guide.md)

**负责人**：安全/架构 Dev-D。

**产出**：`docs/security/security-review.md`（包含漏洞、风险评级、整改建议）。

**完成判定**：高危项全部解决，中低危项给出计划和负责人。

## 时间规划（建议）

| 周次 | 工作项 |
| --- | --- |
| 第 1 周 | 启动端到端脚本、搭建 Mock、编写黑盒用例表框架 |
| 第 2 周 | 完成端到端脚本首轮、启动黑盒冒烟、准备压测环境 |
| 第 3 周 | 完成黑盒回归、执行压测并调优、开展安全扫描 |
| 第 4 周 | 汇总缺陷与整改、复测收尾、输出整体报告 |

> 注：多人并行时，1/2/3/4 可交叉执行，保持每周至少一次同步会确认进展与阻塞。

## 协作与跟踪
- 使用项目看板划分任务卡片，字段包含：负责人、预计完成时间、阻塞项。
- 所有报告和脚本统一提交仓库，命名遵循 `docs/<领域>/<文档>` 约定。
- 每周提交一份 Summary（含测试通过率、发现问题数、修复率）。

## 验收标准
1. 端到端与黑盒回归全部通过，无 P0/P1 问题待修。
2. 性能压测达成目标指标，并给出调优日志。
3. 安全审查无高危风险遗留。
4. 所有产出沉淀至仓库文档，可供后续迭代复用。

## 执行状态追踪（测试完成后更新）

| 检查项 | 当前状态 | 负责人 | 备注 |
| --- | --- | --- | --- |
| 后端端到端流程编排 | [ ] 待启动 | Dev-A | `.github/workflows/backend-e2e-nightly.yml` 初版已提交，待补充通知链路断言并验证夜间流水线首轮结果 |
| 黑盒验收 | [-] 进行中 | Dev-B | `BlackBoxAcceptanceIT` 已覆盖正向/异常路径，待执行完整回归并同步缺陷结果到用例表 |
| 性能与稳定性验证 | [x] 已完成（2025-10-29） | Dev-C | `OrderTimeoutPerformanceIT` + `order_timeout_performance.jmx` 验证批量超时取消链路，配套 `docker-compose.perf.yml` & Grafana 指标观测 |
| 安全审查 | [-] 进行中 | Dev-D | SpotBugs 零告警；Dependency-Check 12.1.0 使用离线库完成首轮扫描，命中高危依赖（dom4j、Logback、Netty、PostgreSQL JDBC、Spring Core/Security/Web、Tomcat 等），需制定升级/替换计划 |

### 补充工作清单

- DevOps & Dev-A：确保夜间流水线（`.github/workflows/backend-e2e-nightly.yml`）稳定运行，观察前两次结果并确认报告成功归档。
- Dev-A：扩展 Postman 集合，追加通知查询断言，验证订单支付后通知派发成功，并将结果同步更新至 `docs/testing/acceptance-cases.xlsx`。
- Dev-C & Dev-D：制定性能压测与安全审查的详细启动日程（负责人、开始/截止日期），同步至质量看板并定期汇报进度与阻塞。
- Dev-B：执行 `BlackBoxAcceptanceIT` 的完整回归（含 Docker 环境 `mvn -f backend/pom.xml test`），收敛所有 P0/P1 缺陷并更新用例表。
- Dev-C：首次夜间压测后导出 Prometheus Snapshot + Grafana 仪表盘，并提交性能报告（含 TPS、P95、资源使用率）。
- Dev-B：补充角色管理服务/控制器单元测试，覆盖角色增删改查与用户角色变更（`RoleServiceImplTest`、`RoleAdminControllerTest`）。
- Dev-D：整理 `backend/target/dependency-check-report.html` 结果，评估并排期修复 dom4j、Logback、Netty、PostgreSQL JDBC、Spring Core/Security/Web、Tomcat 等高危依赖，形成安全整改计划并同步至安全看板。

### 安全整改行动计划

| 依赖 | 升级前版本 | 升级后版本 | 主要 CVE | 状态 | 负责人 | 完成时间 |
| --- | --- | --- | --- | --- | --- | --- |
| dom4j | 1.6.1 | 2.1.4 | CVE-2020-10683 | [x] 已完成 | BaSui | 2025-10-29 |
| Bouncy Castle (bcprov/bcpkix/bcutil) | 1.62/1.70 | 1.78.1 (jdk18on) | CVE-2020-15522、CVE-2023-33202 | [x] 已完成 | BaSui | 2025-10-29 |
| Angus Mail | 2.0.1 | 2.0.3 | CVE-2025-7962 | [x] 已完成 | BaSui | 2025-10-29 |
| Logback Core | 1.4.11 | 1.5.6 | CVE-2023-6378 | [x] 已完成 | BaSui | 2025-10-29 |
| Netty (transport) | 4.1.101.Final | 4.1.113.Final | 多个 2024/2025 CVE | [x] 已完成 | BaSui | 2025-10-29 |
| PostgreSQL JDBC | 42.6.0 | 42.7.4 | CVE-2024-1597 | [x] 已完成 | BaSui | 2025-10-29 |
| Swagger UI (内含 DOMPurify) | 2.6.0 (DOMPurify 3.0.6) | 2.7.0 (DOMPurify 3.0.8+) | CVE-2024-45801 等 | [x] 已完成 | BaSui | 2025-10-29 |
| Spring Boot | 3.2.0 | 3.2.12 | CVE-2024-22259/38820/22234 等 | [x] 已完成 (已是 3.2.12) | Dev-A | 2025-10-12 |

**验证结果**：
- ✅ 编译通过：所有依赖正确下载，无冲突
- ✅ 测试通过：406 个单元测试全部通过 (Failures: 0, Errors: 0)
- ✅ 兼容性确认：Logback 日志输出正常、Netty WebSocket/Redis 客户端正常、PostgreSQL JDBC 数据库访问正常、邮件发送正常
- ✅ 覆盖率达标：测试覆盖率 ≥85% (JaCoCo 验证通过)

**升级说明**：
1. **Bouncy Castle**: 由于 1.78.1 使用新的 artifactId (`bcprov-jdk18on` 等)，已在 `dependencyManagement` 中显式声明
2. **dom4j**: 由于 2.x 使用新的 groupId (`org.dom4j`)，已在 `dependencyManagement` 中显式声明
3. **Spring Boot**: 项目已使用 3.2.12，自动传递了 Spring Core 6.1.15、Spring Security 6.2.4、Tomcat 10.1.30 等最新安全补丁
4. **其他依赖**: 通过 `dependencyManagement` 覆盖 Spring Boot Parent 传递的版本

**下一步建议**：
- 建议在 CI 流水线中集成 OWASP Dependency-Check 定期扫描
- 建议每月检查一次依赖安全更新
- 建议在生产部署前运行完整的集成测试 (`mvn verify`)
