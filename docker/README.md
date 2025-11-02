# 🐳 校园轻享集市 - Docker 部署指南

> **一键启动**，告别环境配置地狱！🚀
> **BaSui 温馨提示**：看完这份文档，你就是 Docker 老司机了！😎

---

## 📋 目录

- [快速开始](#-快速开始)
- [环境要求](#-环境要求)
- [启动方式](#-启动方式)
- [服务说明](#-服务说明)
- [常用命令](#-常用命令)
- [环境变量配置](#-环境变量配置)
- [高可用部署](#-高可用部署)
- [故障排查](#-故障排查)

---

## 🚀 快速开始

### 1️⃣ 最小化启动（开发/测试）

```bash
# 在项目根目录执行
docker compose --project-directory . -f docker/docker-compose.yml up -d

# 或者进入 docker 目录执行
cd docker && docker compose up -d
```

**启动服务**：PostgreSQL + Redis + 后端 + Nginx

**访问地址**：
- 🌐 前端/API：http://localhost
- 🔧 后端健康检查：http://localhost:8080/api/actuator/health

---

### 2️⃣ 完整启动（包含开发工具）

```bash
# 启动核心服务 + MailHog（邮件测试工具）
docker compose --project-directory . -f docker/docker-compose.yml --profile devtools up -d
```

**额外服务**：
- 📧 MailHog：http://localhost:8025（邮件预览界面）

---

### 3️⃣ 生产级启动（包含监控）

```bash
# 启动核心服务 + Prometheus + Grafana
docker compose --project-directory . -f docker/docker-compose.yml --profile observability up -d
```

**额外服务**：
- 📊 Prometheus：http://localhost:9090
- 📈 Grafana：http://localhost:3000（默认账号：admin/admin）

---

## 🛠️ 环境要求

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| Docker | ≥ 20.10 | 推荐最新稳定版 |
| Docker Compose | ≥ 2.0 | 支持 `docker compose` 命令 |
| 可用内存 | ≥ 4GB | 推荐 8GB 以上 |
| 磁盘空间 | ≥ 10GB | 用于镜像和数据卷 |

---

## 📦 服务说明

### 核心服务（默认启动）

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| **postgres** | `postgres:16` | 5432 | PostgreSQL 数据库 |
| **redis** | `redis:7.4` | 6379 | Redis 缓存/会话存储 |
| **backend** | 本地构建 | 8080 | Spring Boot 后端服务 |
| **nginx** | `nginx:1.27-alpine` | 80 | 反向代理/静态资源 |

---

### 开发工具（`--profile devtools`）

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| **mailhog** | `mailhog/mailhog:v1.0.1` | 1025/8025 | 邮件测试工具 |

---

### 监控工具（`--profile observability`）

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| **prometheus** | `prom/prometheus:v2.53.0` | 9090 | 指标收集 |
| **grafana** | `grafana/grafana:11.1.0` | 3000 | 可视化监控 |

---

## 🎮 常用命令

### 启动与停止

```bash
# 启动所有服务（后台运行）
docker compose -f docker/docker-compose.yml up -d

# 启动指定服务
docker compose -f docker/docker-compose.yml up -d postgres redis

# 停止所有服务
docker compose -f docker/docker-compose.yml down

# 停止并删除数据卷（⚠️ 危险操作：会丢失所有数据！）
docker compose -f docker/docker-compose.yml down -v
```

---

### 查看状态

```bash
# 查看服务状态
docker compose -f docker/docker-compose.yml ps

# 查看服务日志
docker compose -f docker/docker-compose.yml logs -f

# 查看指定服务日志
docker compose -f docker/docker-compose.yml logs -f backend

# 查看最近 100 行日志
docker compose -f docker/docker-compose.yml logs --tail=100 backend
```

---

### 重启与重建

```bash
# 重启所有服务
docker compose -f docker/docker-compose.yml restart

# 重启指定服务
docker compose -f docker/docker-compose.yml restart backend

# 重建并启动（代码有更新时使用）
docker compose -f docker/docker-compose.yml up -d --build

# 仅重建后端服务
docker compose -f docker/docker-compose.yml build backend
docker compose -f docker/docker-compose.yml up -d backend
```

---

### 横向扩容

```bash
# 启动 3 个后端实例（需配置 Nginx 负载均衡）
docker compose -f docker/docker-compose.yml up -d --scale backend=3

# 查看扩容后的服务
docker compose -f docker/docker-compose.yml ps backend
```

---

### 进入容器调试

```bash
# 进入后端容器
docker compose -f docker/docker-compose.yml exec backend bash

# 进入数据库容器
docker compose -f docker/docker-compose.yml exec postgres psql -U postgres -d campus_marketplace_prod

# 进入 Redis 容器
docker compose -f docker/docker-compose.yml exec redis redis-cli
```

---

## ⚙️ 环境变量配置

### 1️⃣ 创建 `.env` 文件

在 `docker` 目录下创建 `.env` 文件（参考 `.env.example`）：

```bash
cd docker && cp .env.example .env
```

---

### 2️⃣ 核心配置项

```dotenv
# Spring 配置
SPRING_PROFILES_ACTIVE=prod

# 数据库配置
DB_HOST=postgres
DB_PORT=5432
DB_NAME=campus_marketplace_prod
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password_here  # ⚠️ 生产环境必须修改！

# Redis 配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=  # 留空表示不启用密码

# JWT 配置
JWT_SECRET=*************************************  # ⚠️ 必须设置！

# 后端端口
BACKEND_PORT=8080

# Grafana 配置（监控模式）
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=******************  # ⚠️ 生产环境必须修改！
```

---

### 3️⃣ 高级配置

```dotenv
# Redis 哨兵模式（高可用）
REDIS_SENTINEL_MASTER=mymaster
REDIS_SENTINEL_NODES=sentinel1:26379,sentinel2:26379,sentinel3:26379

# PostgreSQL 复制（读写分离）
REPLICATION_PASSWORD=your_replication_password
```

---

## 🏆 高可用部署

### 1️⃣ PostgreSQL 主从复制

编辑 `docker-compose.yml`，取消注释 `postgres-replica-1` 服务：

```yaml
postgres-replica-1:
  image: postgres:16
  container_name: campus-postgres-replica-1
  # ... 其他配置
```

配置后端读写分离（需在后端代码中实现）。

---

### 2️⃣ Redis 哨兵模式

1. **取消注释** `redis-sentinel-1/2/3` 服务
2. **配置 `.env`**：
   ```dotenv
   REDIS_SENTINEL_MASTER=mymaster
   REDIS_SENTINEL_NODES=redis-sentinel-1:26379,redis-sentinel-2:26379,redis-sentinel-3:26379
   ```
3. **更新后端配置**：修改 `backend/src/main/resources/redisson.yaml` 为哨兵模式

---

### 3️⃣ 后端横向扩容

```bash
# 启动 3 个后端实例
docker compose -f docker/docker-compose.yml up -d --scale backend=3
```

**⚠️ 注意**：需配置 Nginx 负载均衡（修改 `nginx/conf.d/default.conf`）：

```nginx
upstream backend_cluster {
    server backend:8080;
    # Docker Compose 会自动创建多个实例
}

server {
    location /api/ {
        proxy_pass http://backend_cluster;
    }
}
```

---

## 🔧 故障排查

### 问题1：服务启动失败

```bash
# 查看详细错误日志
docker compose -f docker/docker-compose.yml logs backend

# 检查服务健康状态
docker compose -f docker/docker-compose.yml ps
```

**常见原因**：
- ❌ JWT_SECRET 未配置 → 在 `.env` 中设置
- ❌ 端口被占用 → 修改 `.env` 中的端口配置
- ❌ 数据库未就绪 → 等待健康检查通过（约 30s）

---

### 问题2：后端无法连接数据库

```bash
# 进入后端容器测试连接
docker compose -f docker/docker-compose.yml exec backend bash
apt-get update && apt-get install -y postgresql-client
psql -h postgres -U postgres -d campus_marketplace_prod
```

**常见原因**：
- ❌ 环境变量配置错误 → 检查 `.env` 和 `docker-compose.yml`
- ❌ 数据库未启动 → `docker compose ps` 查看状态

---

### 问题3：Redis 连接失败

```bash
# 测试 Redis 连接
docker compose -f docker/docker-compose.yml exec redis redis-cli ping
# 预期输出：PONG

# 带密码测试
docker compose -f docker/docker-compose.yml exec redis redis-cli -a your_password ping
```

---

### 问题4：Nginx 502 错误

```bash
# 查看 Nginx 日志
docker compose -f docker/docker-compose.yml logs nginx

# 查看后端健康状态
curl http://localhost:8080/api/actuator/health
```

**常见原因**：
- ❌ 后端服务未启动 → `docker compose ps backend`
- ❌ 后端启动慢 → 等待健康检查通过
- ❌ Nginx 配置错误 → 检查 `nginx/conf.d/default.conf`

---

### 问题5：数据丢失

```bash
# 查看数据卷
docker volume ls | grep campus

# 备份数据卷
docker run --rm -v campus_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup.tar.gz /data

# 恢复数据卷
docker run --rm -v campus_postgres_data:/data -v $(pwd):/backup alpine tar xzf /backup/postgres_backup.tar.gz -C /
```

---

## 🎯 生产部署检查清单

在生产环境部署前，请确认以下事项：

- [ ] 已修改默认密码（DB_PASSWORD、REDIS_PASSWORD、GRAFANA_ADMIN_PASSWORD）
- [ ] 已设置强随机 JWT_SECRET（至少 256 位）
- [ ] 已配置数据卷备份策略
- [ ] 已启用 HTTPS（配置 Nginx SSL 证书）
- [ ] 已配置防火墙规则（仅暴露必要端口）
- [ ] 已启用日志聚合（ELK/Loki）
- [ ] 已配置监控告警（Prometheus + AlertManager）
- [ ] 已测试故障恢复流程
- [ ] 已配置定时备份任务

---

## 💡 BaSui 的最后叮嘱

1. **🚨 生产环境必须修改默认密码**：别让你的数据库裸奔！
2. **💾 定期备份数据卷**：数据无价，备份有道！
3. **📊 启用监控**：问题早发现，事故少一半！
4. **📝 查看日志**：遇到问题先看日志，别瞎猜！
5. **🔄 滚动更新**：代码有变化记得重建镜像！

---

**BaSui 温馨提示**：Docker 用得好，下班回家早！😎✨

**有问题？** 欢迎提 Issue 或查看 [官方文档](https://docs.docker.com/)
