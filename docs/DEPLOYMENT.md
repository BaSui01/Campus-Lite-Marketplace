# 校园轻享集市系统 - 生产环境部署指南

> **作者**: BaSui 😎  
> **更新**: 2025-10-27  
> **环境**: 生产环境（Production）

---

## 📋 目录

- [系统架构](#系统架构)
- [环境要求](#环境要求)
- [部署前准备](#部署前准备)
- [Docker Compose 部署](#docker-compose-部署)
- [数据库迁移](#数据库迁移)
- [监控和日志](#监控和日志)
- [备份和恢复](#备份和恢复)
- [常见问题](#常见问题)

---

## 🏗️ 系统架构

```
┌─────────────┐
│   用户端    │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│    Nginx    │ (负载均衡 + 反向代理)
└──────┬──────┘
       │
       ├────────────────┬────────────────┐
       ↓                ↓                ↓
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│   App-1     │  │   App-2     │  │   App-N     │ (后端应用集群)
└─────┬───────┘  └─────┬───────┘  └─────┬───────┘
      │                │                │
      └────────────────┴────────────────┘
                       │
       ┌───────────────┼───────────────┐
       ↓               ↓               ↓
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ PostgreSQL  │  │   Redis     │  │ Prometheus  │
│ 主从复制     │  │  Sentinel   │  │  + Grafana  │
└─────────────┘  └─────────────┘  └─────────────┘
```

---

## 💻 环境要求

### 硬件要求（推荐配置）

| 组件          | CPU   | 内存   | 磁盘   | 说明                |
|--------------|-------|--------|--------|---------------------|
| 应用服务器    | 4 核  | 8 GB   | 50 GB  | 运行后端应用         |
| 数据库服务器  | 4 核  | 16 GB  | 200 GB | PostgreSQL 主从      |
| Redis 服务器  | 2 核  | 4 GB   | 20 GB  | Redis Sentinel 集群  |
| Nginx 服务器  | 2 核  | 2 GB   | 10 GB  | 反向代理和静态资源   |

### 软件要求

- **操作系统**: Linux (Ubuntu 22.04 / CentOS 8+ / Debian 11+)
- **Docker**: 24.0+
- **Docker Compose**: 2.20+
- **Git**: 2.30+

---

## 🔧 部署前准备

### 1. 安装 Docker 和 Docker Compose

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo apt-get install docker-compose-plugin

# CentOS/RHEL
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo systemctl start docker
sudo systemctl enable docker
```

### 2. 克隆项目代码

```bash
git clone https://github.com/your-org/campus-lite-marketplace.git
cd campus-lite-marketplace
git checkout main
```

### 3. 配置环境变量

创建 `.env` 文件（生产环境必须使用独立配置）：

```bash
cp .env.example .env.prod
```

编辑 `.env.prod` 文件，填写真实配置：

```bash
# 数据库配置
DB_USERNAME=campus_admin
DB_PASSWORD=<STRONG_PASSWORD>
REPLICATION_PASSWORD=<REPLICATION_PASSWORD>

# Redis 配置
REDIS_PASSWORD=<REDIS_PASSWORD>

# JWT 配置（256 位以上强密钥）
JWT_SECRET=<JWT_SECRET>

# 支付宝配置
ALIPAY_APP_ID=<YOUR_APP_ID>
ALIPAY_PRIVATE_KEY=<YOUR_PRIVATE_KEY>
ALIPAY_PUBLIC_KEY=<ALIPAY_PUBLIC_KEY>

# 微信支付配置
WECHAT_APP_ID=<YOUR_APP_ID>
WECHAT_MCH_ID=<YOUR_MCH_ID>
WECHAT_MERCHANT_SERIAL_NUMBER=<YOUR_SERIAL_NUMBER>
WECHAT_API_V3_KEY=<YOUR_API_V3_KEY>

# Grafana 配置
GRAFANA_ADMIN_PASSWORD=<GRAFANA_PASSWORD>
```

### 4. 创建必要的目录

```bash
mkdir -p logs nginx/ssl prometheus grafana/dashboards redis db
chmod -R 755 logs nginx prometheus grafana redis db
```

---

## 🚀 Docker Compose 部署

### 1. 构建应用镜像

```bash
# 构建后端镜像
docker build -t campus-marketplace-backend:latest ./backend

# 验证镜像
docker images | grep campus-marketplace
```

### 2. 启动所有服务

```bash
# 使用生产环境配置启动
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d

# 查看服务状态
docker-compose -f docker-compose.prod.yml ps

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f app-1
```

### 3. 验证服务健康

```bash
# 检查后端健康
curl http://localhost/api/actuator/health

# 检查 Nginx
curl http://localhost/health

# 检查数据库
docker exec -it campus-postgres-master psql -U campus_admin -d campus_marketplace_prod -c "SELECT 1;"

# 检查 Redis
docker exec -it campus-redis-master redis-cli -a <REDIS_PASSWORD> ping
```

---

## 🗄️ 数据库迁移

### 1. 初始化数据库

```bash
# 执行初始化脚本
docker exec -i campus-postgres-master psql -U campus_admin -d campus_marketplace_prod < db/init.sql

# 验证表结构
docker exec -it campus-postgres-master psql -U campus_admin -d campus_marketplace_prod -c "\dt"
```

### 2. 数据迁移脚本

创建 `db/migrations/V1__initial_schema.sql` 文件：

```sql
-- 创建用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建物品表
CREATE TABLE IF NOT EXISTS t_goods (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    seller_id BIGINT REFERENCES t_user(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_goods_seller ON t_goods(seller_id);
CREATE INDEX idx_goods_status ON t_goods(status);
```

---

## 📊 监控和日志

### 1. Grafana 访问

- **地址**: http://localhost:3000
- **用户名**: admin
- **密码**: 在 `.env.prod` 中配置的 `GRAFANA_ADMIN_PASSWORD`

### 2. Prometheus 访问

- **地址**: http://localhost:9090

### 3. 查看应用日志

```bash
# 实时查看日志
docker-compose -f docker-compose.prod.yml logs -f app-1

# 查看错误日志
docker exec campus-app-1 cat /var/log/campus-marketplace/campus-marketplace-error.log

# 查看慢查询日志
docker exec campus-app-1 cat /var/log/campus-marketplace/campus-marketplace-slow-query.log
```

### 4. 日志收集（推荐）

安装 ELK Stack（Elasticsearch + Logstash + Kibana）进行日志聚合分析。

---

## 💾 备份和恢复

### 1. 数据库备份

```bash
# 创建备份脚本 backup.sh
#!/bin/bash
BACKUP_DIR=/backups
DATE=$(date +%Y%m%d_%H%M%S)
docker exec campus-postgres-master pg_dump -U campus_admin campus_marketplace_prod > $BACKUP_DIR/db_backup_$DATE.sql
```

### 2. 数据库恢复

```bash
# 恢复数据
docker exec -i campus-postgres-master psql -U campus_admin -d campus_marketplace_prod < /backups/db_backup_20251027_120000.sql
```

### 3. Redis 备份

```bash
# Redis RDB 备份
docker exec campus-redis-master redis-cli -a <REDIS_PASSWORD> BGSAVE
docker cp campus-redis-master:/data/dump.rdb /backups/redis_backup_$(date +%Y%m%d).rdb
```

---

## 🛠️ 常见问题

### Q1: 容器启动失败怎么办？

```bash
# 查看详细错误日志
docker-compose -f docker-compose.prod.yml logs <service_name>

# 检查端口占用
netstat -tuln | grep <port>

# 重启服务
docker-compose -f docker-compose.prod.yml restart <service_name>
```

### Q2: 数据库连接失败？

- 检查环境变量是否正确
- 验证数据库容器状态：`docker ps | grep postgres`
- 检查网络连接：`docker network inspect campus-network`

### Q3: Redis Sentinel 集群无法连接？

- 验证 Sentinel 配置文件
- 检查 Redis 主节点状态：`docker exec redis-master redis-cli -a <PASSWORD> INFO replication`

### Q4: 内存溢出（OOM）？

- 调整 JVM 参数：修改 `Dockerfile` 中的 `JAVA_OPTS`
- 增加 Docker 容器内存限制：在 `docker-compose.prod.yml` 中添加 `mem_limit`

### Q5: 如何滚动更新应用？

```bash
# 1. 构建新镜像
docker build -t campus-marketplace-backend:v2 ./backend

# 2. 更新应用实例 1
docker stop campus-app-1
docker rm campus-app-1
docker-compose -f docker-compose.prod.yml up -d app-1

# 3. 验证健康后更新实例 2
curl http://localhost/api/actuator/health
docker stop campus-app-2
docker rm campus-app-2
docker-compose -f docker-compose.prod.yml up -d app-2
```

---

## 📞 技术支持

- **文档**: https://docs.campus-marketplace.com
- **Issue**: https://github.com/your-org/campus-lite-marketplace/issues
- **Email**: support@campus-marketplace.com

---

**祝部署顺利！🎉**
