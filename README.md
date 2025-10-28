# 校园轻享集市系统（Campus Lite Marketplace）

> 基于 **Java 21 + Spring Boot 3** 打造的一体化校园二手交易与社区互动平台，涵盖支付、即时通讯、论坛、任务调度等业务模块，可按需部署到生产环境。

## 🌟 核心特性
- 用户体系：注册登录、JWT 鉴权、黑白名单与风控。
- 商品与交易：商品发布/搜索/推荐、订单生命周期管理、优惠券与积分。
- 支付能力：接入支付宝、微信支付（V2/V3），统一支付门面，异步通知与退款占位。
- 消息中心：WebSocket 实时通信、消息推送、通知模板。
- 运营支撑：任务调度、统计看板、隐私合规、举报申诉。
- 基础设施：PostgreSQL、Redis、Nginx、Prometheus+Grafana 监控，可扩展至高可用部署。

## 🧱 技术栈
- **后端**：Java 21、Spring Boot 3.2、Spring Data JPA、Spring Security、MapStruct、Lombok、Redisson、Testcontainers。
- **基础设施**：PostgreSQL 16、Redis 7、Nginx、Prometheus、Grafana、Docker / Docker Compose。
- **构建工具**：Maven 3.8+、Docker BuildKit。
- **前端**：预留 `frontend/` 目录（需求文档详见 `docs/frontend-architecture-requirements.md`，当前未包含实现）。

## 📁 目录结构
```
├── backend/                     # 后端服务（Spring Boot）
│   ├── src/main/java/com/campus/marketplace/
│   │   ├── common/              # 公共配置、异常、DTO、工具
│   │   ├── controller/          # REST 控制器
│   │   ├── service/             # 服务接口与实现（包含支付、消息等）
│   │   ├── repository/          # JPA Repository
│   │   └── websocket/           # WebSocket 处理
│   └── src/main/resources/      # 应用配置、日志、SQL 等
├── docker/                      # 生产部署 compose 文件与配置模板
│   ├── docker-compose.dev.yml        # 本地开发依赖（PostgreSQL / Redis / Mailhog）
│   ├── docker-compose.prod.min.yml   # 最小部署：PostgreSQL + Redis + 双实例后端 + Nginx
│   └── docker-compose.prod.ha.yml    # 高可用部署（含监控、哨兵等）
├── docs/                        # 运维/需求文档（部署指南、支付指南等）
├── db/, grafana/, nginx/, prometheus/, redis/  # 容器挂载配置
├── .env*.example                # 环境变量模板
└── README.md                    # 本自述文件
```

## ⚙️ 环境要求
| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 21+ | 建议使用 Temurin/Oracle OpenJDK |
| Maven | 3.8+ | 构建后端 |
| Docker | 24+ | 可选，用于容器化部署 |
| Docker Compose | 2.20+ | 可选，管理多容器环境 |
| PostgreSQL | 16 | 本地或容器均可 |
| Redis | 7 | 本地或容器均可 |

## 🚀 快速开始
1. **克隆项目**
   ```bash
   git clone https://github.com/your-org/campus-lite-marketplace.git
   cd campus-lite-marketplace
   ```

2. **准备环境变量**
   ```bash
   cp .env.example .env
   ```
   - 关键字段：数据库 (`DB_*`)、Redis (`REDIS_PASSWORD`)、JWT (`JWT_SECRET`)、支付相关（支付宝 / 微信）等。
   - 生产环境请使用 `.env.prod.example` 并设置强随机密钥。

3. **启动依赖（可选）**
   - 本地已有 PostgreSQL / Redis 时可跳过。
   - 使用开发 compose（提供 PostgreSQL、Redis、Mailhog）：
     ```bash
     docker compose -f docker/docker-compose.dev.yml up -d
     ```

4. **构建与运行后端**
   ```bash
   cd backend
   mvn clean package
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   # 或 java -jar target/marketplace-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
   ```

5. **访问接口**
   - API 根路径：`http://localhost:8080/api`
   - 健康检查：`http://localhost:8080/api/actuator/health`

## 🔐 常用配置说明
| 分类 | 变量 | 说明 |
|------|------|------|
| 数据库 | `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | JDBC 连接 |
| Redis | `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | 缓存 & 会话 |
| JWT | `JWT_SECRET` / `JWT_EXPIRE_HOURS` | 认证与 Token 有效期 |
| 支付宝 | `ALIPAY_*` | 详见 `docs/支付宝沙箱配置指南.md` |
| 微信支付 | `WECHAT_*` | 默认使用 V3，需具备商户号和证书；如需沙箱测试改为 `v2` 并填写对应密钥 |
| 日志 | `LOG_DIR` / `LOG_LEVEL` | 可在 `.env` 或 `application-*.yml` 中覆盖 |

## 🧪 测试与质量
- 单元测试 / 集成测试：
  ```bash
  mvn test
  ```
- 推荐使用 Testcontainers 驱动的集成测试（确保 Docker 可用）。
- 代码规范：遵循 `backend/README.md` 中的编码与 Git 提交规范。

## 🐳 Docker 化部署
- **开发依赖**：`docker/docker-compose.dev.yml`，一键拉起 PostgreSQL / Redis / Mailhog。
- **最小部署**：`docker/docker-compose.prod.min.yml`，适合单节点环境。
- **高可用部署**：`docker/docker-compose.prod.ha.yml`，包含 App 集群、Nginx、Redis Sentinel、Prometheus、Grafana 等。
- 详细步骤、数据备份与故障排查请参考 `docs/DEPLOYMENT.md`。

## 📚 文档与资源
- `backend/README.md`：后端服务详细说明。
- `docs/DEPLOYMENT.md`：生产部署、监控、备份。
- `docs/frontend-architecture-requirements.md`：前端规划需求。
- `docs/支付宝沙箱配置指南.md`：支付接入配置流程。

## 🤝 贡献指南
1. Fork & 创建分支：`feat/xxx`、`fix/xxx`。
2. 遵循提交消息规范（如 `feat: 新增订单导出`）。
3. 提交前确保 `mvn test` 通过，并更新相关文档。
4. 提 PR 前同步与主干分支并解决冲突。

## 📄 许可证
本项目默认遵循 [MIT License](https://opensource.org/licenses/MIT)。如需商用或定制合作，请联系原作者。

---
💡 **提示**：若继续扩展前端或移动端，请在 `frontend/` 目录中初始化项目并补充此 README，对跨端协议、API 约定进行统一维护。
