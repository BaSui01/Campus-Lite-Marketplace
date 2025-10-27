# 环境变量配置说明

## 快速开始

1. 复制环境变量模板：`cp .env.example .env`
2. 修改 `.env` 文件中的配置
3. 启动应用：`mvn spring-boot:run`

## 配置文件结构

- `application.yml` - 主配置文件（使用环境变量）
- `.env` - 本地环境变量（不提交到 Git）
- `.env.example` - 环境变量模板（提交到 Git）

## 重要提示

⚠️ **永远不要提交 `.env` 文件到 Git！**

## 主要配置项

### 数据库
- DB_HOST, DB_PORT, DB_NAME
- DB_USERNAME, DB_PASSWORD

### Redis
- REDIS_HOST, REDIS_PORT
- REDIS_PASSWORD (可选)

### JWT
- JWT_SECRET (至少 256 位)
- JWT_EXPIRATION (毫秒)

---
Author: BaSui 😎
