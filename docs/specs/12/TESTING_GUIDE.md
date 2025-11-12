# Spec 12 完整测试指南

> **前后端密码加密传输 + 高级功能测试**  
> **作者**: BaSui 😎  
> **日期**: 2025-11-06  
> **状态**: ✅ 全部实现完成

---

## 📊 功能完成度

| 功能模块 | 状态 | 说明 |
|---------|------|------|
| **基础加密** | ✅ 100% | AES-256加密 + 前后端集成 |
| **防重放攻击** | ✅ 100% | 时间戳验证（5分钟有效期）|
| **多密钥轮换** | ✅ 100% | 支持历史密钥解密 |
| **登录加密** | ✅ 100% | 已集成 |
| **注册加密** | ✅ 100% | 已集成 |
| **重置密码加密** | ✅ 100% | 已集成 |

---

## 🚀 快速测试（10分钟验证）

### Step 1：启动后端服务（2分钟）

```bash
cd backend
mvn spring-boot:run
```

**等待日志**：
```
应用已启动 name=campus-marketplace profiles=dev port=8200
```

---

### Step 2：启动前端服务（1分钟）

```bash
cd frontend/packages/admin
pnpm dev
```

**访问**：`http://localhost:5173`

---

### Step 3：测试基础登录（2分钟）

#### 3.1 打开浏览器DevTools

- Windows/Linux: `F12`
- Mac: `Cmd + Option + I`

#### 3.2 切换到Network标签

#### 3.3 输入登录信息

- **用户名**：`admin`
- **密码**：`admin123`

#### 3.4 点击登录，查看请求

**Network → /api/auth/login → Payload**

**✅ 成功标志**：
```json
{
  "username": "admin",
  "password": "U2FsdGVkX1+8xZq..."  // ✅ 加密的Base64字符串
}
```

**❌ 失败标志**：
```json
{
  "username": "admin",
  "password": "admin123"  // ❌ 明文密码
}
```

---

### Step 4：测试防重放攻击（3分钟）

#### 4.1 复制登录请求的密文

在Network中，右键 `/api/auth/login` → **Copy as cURL**

#### 4.2 等待6分钟后重放

**macOS/Linux**：
```bash
# 粘贴刚才复制的cURL命令
curl 'http://localhost:8200/api/auth/login' \
  -H 'Content-Type: application/json' \
  --data-raw '{"username":"admin","password":"U2FsdGVkX1+..."}'
```

**Windows (PowerShell)**：
```powershell
# 使用Invoke-RestMethod
Invoke-RestMethod -Uri "http://localhost:8200/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"U2FsdGVkX1+..."}'
```

**✅ 预期结果**：
```json
{
  "code": 400,
  "message": "密码已过期，请重新登录"
}
```

---

### Step 5：测试多密钥轮换（2分钟）

#### 5.1 修改后端配置

**文件**：`backend/src/main/resources/application.yml`

```yaml
app:
  security:
    # 当前密钥（新）
    encrypt-key: new-key-32-bytes-length-2025!
    
    # 历史密钥（旧）
    legacy-keys: dev-test-key-32-bytes-length!
```

#### 5.2 重启后端

```bash
# Ctrl+C 停止
mvn spring-boot:run
```

#### 5.3 使用旧密文登录

**前端保持旧配置**（`.env.development`）：
```env
VITE_ENCRYPT_KEY=dev-test-key-32-bytes-length!
```

**再次登录**，查看后端日志：

**✅ 预期日志**：
```
⚠️ 当前密钥解密失败，尝试历史密钥
✅ 使用历史密钥[0]解密成功
```

---

## 🧪 完整测试场景

### 场景1：正常登录流程

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 输入用户名和密码 | 密码已加密 |
| 2 | 点击登录 | 密文传输到后端 |
| 3 | 后端解密 | 日志显示"✅ 密码解密成功" |
| 4 | 验证时间戳 | 时间戳有效，登录成功 |

---

### 场景2：时间戳过期

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 获取旧密文（5分钟前） | - |
| 2 | 重放请求 | 后端返回"密码已过期" |
| 3 | 查看日志 | "❌ 密码已过期" |

---

### 场景3：密钥轮换

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 更新后端密钥 | 新密钥配置生效 |
| 2 | 配置历史密钥 | legacy-keys包含旧密钥 |
| 3 | 使用旧密文登录 | 历史密钥解密成功 |
| 4 | 查看日志 | "✅ 使用历史密钥[N]解密成功" |

---

### 场景4：注册功能

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 打开注册页面 | - |
| 2 | 输入用户信息和密码 | 密码已加密 |
| 3 | 点击注册 | 密文传输 |
| 4 | 后端解密 | 日志显示"✅ 注册密码解密成功" |
| 5 | 注册成功 | 返回用户ID |

---

### 场景5：重置密码

| 步骤 | 操作 | 预期结果 |
|-----|------|---------|
| 1 | 请求重置密码验证码 | 邮件发送成功 |
| 2 | 输入新密码 | 密码已加密 |
| 3 | 提交重置 | 密文传输 |
| 4 | 后端解密 | 日志显示"✅ 重置密码解密成功" |
| 5 | 密码更新 | 使用新密码登录成功 |

---

## 📋 后端日志检查清单

### 正常登录（成功）

```log
INFO  用户登录: credential=admin
DEBUG ✅ 密码解密成功（已验证时间戳）
DEBUG ✅ 时间戳验证通过: diff=123ms
DEBUG ✅ 使用当前密钥解密成功
INFO  用户登录成功: userId=1, username=admin
```

---

### 时间戳过期（失败）

```log
INFO  用户登录: credential=admin
ERROR ❌ 密码已过期: timestamp=1730000000000, now=1730000300000, diff=300001ms
ERROR ❌ 密码解密失败: 密码已过期，请重新登录
```

---

### 密钥轮换（成功）

```log
INFO  用户登录: credential=admin
DEBUG ⚠️ 当前密钥解密失败，尝试历史密钥
INFO  ✅ 使用历史密钥[0]解密成功
DEBUG ✅ 时间戳验证通过: diff=45ms
INFO  用户登录成功: userId=1, username=admin
```

---

### 兼容明文密码（过渡期）

```log
INFO  用户登录: credential=admin
WARN  ⚠️ 接收到明文密码，用户名: admin
INFO  用户登录成功: userId=1, username=admin
```

---

## ⚠️ 常见问题排查

### 问题1：密文格式错误

**症状**：
```
❌ Base64解码失败
```

**原因**：前端加密失败或密文被截断

**解决**：
1. 检查前端 `crypto-js` 是否正确安装
2. 检查 `.env.development` 密钥配置
3. 重启前端服务：`pnpm dev`

---

### 问题2：密码已过期

**症状**：
```
密码已过期，请重新登录
```

**原因**：时间戳超过5分钟

**解决**：
- 正常情况：这是**预期行为**，防重放攻击生效
- 异常情况：检查服务器时间是否正确

---

### 问题3：所有密钥解密失败

**症状**：
```
❌ 所有密钥解密失败
```

**原因**：前后端密钥不一致

**解决**：
1. **前端配置**（`.env.development`）
   ```env
   VITE_ENCRYPT_KEY=dev-test-key-32-bytes-length!
   ```

2. **后端配置**（`application.yml`）
   ```yaml
   app:
     security:
       encrypt-key: dev-test-key-32-bytes-length!
   ```

3. 确保密钥**完全一致**（包括大小写、符号）

---

### 问题4：前端加密失败

**症状**：
```
❌ 密码加密失败
```

**原因**：环境变量未加载

**解决**：
```bash
# 1. 检查.env.development文件存在
ls frontend/packages/admin/.env.development

# 2. 重新构建shared包
cd frontend/packages/shared
pnpm build

# 3. 重启前端
cd ../admin
pnpm dev
```

---

## 🎯 测试优先级

### P0（必测）

- ✅ 登录密码加密传输
- ✅ 注册密码加密传输
- ✅ 防重放攻击（时间戳验证）

### P1（建议测试）

- ⏳ 多密钥轮换
- ⏳ 重置密码加密传输
- ⏳ 兼容明文密码

### P2（可选测试）

- 修改密码加密传输
- 异常边界测试
- 性能测试

---

## 📝 测试报告模板

```markdown
## Spec 12 测试报告

**测试人员**: [你的名字]  
**测试日期**: 2025-11-06  
**测试环境**: Windows/macOS/Linux

### 测试结果

| 功能 | 状态 | 说明 |
|-----|------|------|
| 登录加密 | ✅/❌ | [备注] |
| 注册加密 | ✅/❌ | [备注] |
| 防重放攻击 | ✅/❌ | [备注] |
| 多密钥轮换 | ✅/❌ | [备注] |

### 发现的问题

1. [问题描述]
   - **复现步骤**: [...]
   - **预期结果**: [...]
   - **实际结果**: [...]

### 建议

[你的建议]
```

---

## 🎉 完成标志

当以下所有项都通过，说明功能完全正常：

- ✅ 登录密文传输（Network查看）
- ✅ 后端日志显示"✅ 密码解密成功"
- ✅ 时间戳验证生效（5分钟后重放失败）
- ✅ 登录成功，跳转到Dashboard
- ✅ 没有任何错误日志

---

**维护者**: BaSui 😎  
**最后更新**: 2025-11-06
