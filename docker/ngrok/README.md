# 🌉 ngrok 公网暴露配置指南

> 📅 更新: 2025-11-12  
> 👤 作者: BaSui 😎

---

## 🎯 功能说明

通过 ngrok 将本地前后端服务同时暴露到公网，实现：

- ✅ **支付回调**：支付宝/微信服务器回调本地后端
- ✅ **远程访问**：从任何地方访问前后端系统
- ✅ **多人测试**：分享公网地址给团队成员测试
- ✅ **演示展示**：快速展示项目给客户

---

## 📦 暴露的服务

| 服务 | 本地端口 | 说明 |
|------|---------|------|
| 🖥️ **后端API** | 8200 | Spring Boot后端，支付回调必需 |
| 📊 **管理端** | 8210 | React管理后台 |
| 👤 **用户端** | 8220 | React用户门户 |

---

## 🚀 快速开始

### 1️⃣ 获取 ngrok Token

1. 访问 https://dashboard.ngrok.com/signup （免费注册）
2. 登录后访问 https://dashboard.ngrok.com/get-started/your-authtoken
3. 复制你的 `authtoken`

### 2️⃣ 配置环境变量

编辑项目根目录的 `.env` 文件：

```bash
# ngrok 配置
NGROK_AUTHTOKEN=2oXXXXXXXXXXXXXXXXXXXXXXXXX  # 粘贴你的真实token
```

### 3️⃣ 启动 ngrok 服务

```bash
# 进入项目根目录
cd d:\code\campus-lite-marketplace

# 启动 ngrok 服务
docker compose --project-directory . -f docker/docker-compose.yml --profile tunnel up -d ngrok
```

### 4️⃣ 查看公网地址

访问 **http://localhost:4040**，你会看到三个隧道的公网地址：

```
┌─────────────────────────────────────────────────────┐
│ ngrok Web Interface                                 │
├─────────────────────────────────────────────────────┤
│ Status    online                                    │
│ Tunnels   3 tunnels                                 │
├─────────────────────────────────────────────────────┤
│ backend (http)                                      │
│   URL: https://abc123-backend.ngrok-free.app        │
│   → http://host.docker.internal:8200                │
├─────────────────────────────────────────────────────┤
│ admin (http)                                        │
│   URL: https://def456-admin.ngrok-free.app          │
│   → http://host.docker.internal:8210                │
├─────────────────────────────────────────────────────┤
│ portal (http)                                       │
│   URL: https://ghi789-portal.ngrok-free.app         │
│   → http://host.docker.internal:8220                │
└─────────────────────────────────────────────────────┘
```

### 5️⃣ 更新支付回调地址

复制 **backend** 的公网地址，更新 `.env` 配置：

```bash
# 支付宝回调配置（替换成你的 ngrok backend 地址）
ALIPAY_NOTIFY_URL=https://abc123-backend.ngrok-free.app/api/payment/alipay/notify
ALIPAY_RETURN_URL=http://localhost:8220/payment/result

# 微信回调配置
WECHAT_V2_NOTIFY_URL=https://abc123-backend.ngrok-free.app/api/payment/wechat/notify
WECHAT_NOTIFY_URL=https://abc123-backend.ngrok-free.app/api/payment/wechat/notify
```

### 6️⃣ 重启后端服务

```bash
# 如果后端在本地IDE运行，重启应用即可
# 如果后端在Docker运行：
docker compose --project-directory . -f docker/docker-compose.yml restart backend
```

---

## 🌐 访问地址

### 本地访问（开发常用）

```bash
# 后端API
http://localhost:8200/api

# 前端管理端
http://localhost:8210

# 前端用户端
http://localhost:8220
```

### 公网访问（测试/演示）

```bash
# 后端API（替换成你的实际地址）
https://abc123-backend.ngrok-free.app/api

# 前端管理端
https://def456-admin.ngrok-free.app

# 前端用户端
https://ghi789-portal.ngrok-free.app
```

---

## 📊 ngrok 管理界面功能

访问 http://localhost:4040 可以查看：

| 功能 | 说明 |
|------|------|
| 🌐 **Tunnels** | 查看所有隧道的公网URL |
| 📡 **Requests** | 实时查看HTTP请求/响应 |
| 🔍 **Inspect** | 查看请求详情（Header、Body等） |
| 🔄 **Replay** | 重放历史请求（调试利器） |
| 📈 **Stats** | 查看流量统计 |

---

## ⚙️ 高级配置

### 1. 自定义域名（需要付费版）

编辑 `docker/ngrok/ngrok.yml`：

```yaml
tunnels:
  backend:
    proto: http
    addr: host.docker.internal:8200
    hostname: api.your-domain.com  # 自定义域名
```

### 2. 添加基础认证保护

```yaml
tunnels:
  backend:
    proto: http
    addr: host.docker.internal:8200
    auth: "username:password"  # 访问需要输入用户名密码
```

### 3. 选择最近的区域（降低延迟）

```yaml
# 添加到 ngrok.yml 末尾
region: ap  # 亚太地区（中国大陆选这个）
```

可选区域：
- `us` - 美国
- `eu` - 欧洲
- `ap` - 亚太
- `au` - 澳大利亚
- `jp` - 日本
- `in` - 印度

---

## 🛑 停止服务

```bash
# 停止 ngrok 服务
docker compose --project-directory . -f docker/docker-compose.yml stop ngrok

# 停止并删除容器
docker compose --project-directory . -f docker/docker-compose.yml down ngrok
```

---

## ❓ 常见问题

### Q1: 为什么访问公网地址很慢？

**A:** ngrok 免费版会有延迟，付费版可以选择最近的区域节点。

### Q2: 浏览器提示不安全？

**A:** ngrok 免费版会有警告页，点击"Visit Site"继续访问即可。

### Q3: 支付回调收不到？

**A:** 检查以下几点：
1. ✅ ngrok 服务已启动（`docker ps | grep ngrok`）
2. ✅ `.env` 中 `ALIPAY_NOTIFY_URL` 已更新为 ngrok 公网地址
3. ✅ 后端服务已重启（加载新配置）
4. ✅ 防火墙没有拦截

### Q4: 公网地址经常变化怎么办？

**A:** 免费版重启后地址会变，付费版可以固定域名。临时方案：
1. 重启后在 ngrok 管理界面查看新地址
2. 更新 `.env` 中的回调地址
3. 重启后端服务

---

## 💰 ngrok 免费版 vs 付费版

| 功能 | 免费版 | 付费版 |
|------|--------|--------|
| 隧道数量 | 1个 | 3个以上 |
| 自定义域名 | ❌ | ✅ |
| 固定域名 | ❌ | ✅ |
| 访问速度 | 较慢 | 快 |
| 警告页面 | ✅ | ❌ |
| 价格 | 免费 | $8/月起 |

💡 **开发测试建议**：免费版足够  
💡 **生产部署建议**：使用云服务器 + 域名

---

## 🔗 相关链接

- ngrok 官网: https://ngrok.com
- ngrok 文档: https://ngrok.com/docs
- ngrok Dashboard: https://dashboard.ngrok.com

---

**更新记录**：
- 2025-11-12：初始版本，支持三端口多隧道配置
