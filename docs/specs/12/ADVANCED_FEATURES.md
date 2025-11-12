# Spec 12 高级功能实施指南

> **阶段2：高级增强功能**  
> **作者**: BaSui 😎  
> **日期**: 2025-11-06  
> **前置条件**: 基础加密功能已完成

---

## 📋 高级功能清单

| 功能 | 优先级 | 预计时间 | 安全等级 |
|-----|--------|---------|---------|
| **1. 防重放攻击** | P0 | 2小时 | 🔒🔒🔒 高 |
| **2. 多密钥轮换** | P1 | 2小时 | 🔒🔒 中 |
| **3. 注册加密** | P0 | 1小时 | 🔒🔒🔒 高 |
| **4. 修改密码加密** | P1 | 1小时 | 🔒🔒 中 |

---

## 🛡️ 功能1：防重放攻击（时间戳验证）

### 原理

在密文中嵌入时间戳，后端验证时间戳是否在有效期内（如5分钟），防止密文被截获后重复使用。

### 实现方案

**前端**：密文格式 = `timestamp|encryptedPassword`

```typescript
// 加密时添加时间戳
export function encryptPasswordWithTimestamp(password: string): string {
  const timestamp = Date.now();
  const payload = `${timestamp}|${password}`;
  return encryptPassword(payload);
}
```

**后端**：验证时间戳

```java
public String decryptPasswordWithTimestamp(String encryptedPassword) {
    String decrypted = decryptPassword(encryptedPassword);
    String[] parts = decrypted.split("\\|");
    
    if (parts.length != 2) {
        throw new CryptoException("密文格式错误");
    }
    
    long timestamp = Long.parseLong(parts[0]);
    long now = System.currentTimeMillis();
    
    // 验证时间戳（5分钟有效期）
    if (Math.abs(now - timestamp) > 5 * 60 * 1000) {
        throw new CryptoException("密码已过期，请重新登录");
    }
    
    return parts[1]; // 返回真实密码
}
```

---

## 🔑 功能2：多密钥轮换

### 原理

支持多个密钥并存，新密文使用新密钥加密，旧密文仍可用旧密钥解密，实现无缝切换。

### 实现方案

**配置文件**：

```yaml
app:
  security:
    # 当前使用的密钥（新密文用此加密）
    encrypt-key: ${BACKEND_ENCRYPT_KEY:new-key-32-bytes-length-here!}
    
    # 历史密钥（用于解密旧密文）
    legacy-keys:
      - old-key-1-32-bytes-length-here!
      - old-key-2-32-bytes-length-here!
```

**后端工具类**：

```java
@Value("${app.security.encrypt-key}")
private String currentKey;

@Value("${app.security.legacy-keys:}")
private List<String> legacyKeys;

public String decryptPassword(String encryptedPassword) {
    // 先尝试当前密钥
    try {
        return doDecrypt(encryptedPassword, currentKey);
    } catch (Exception e) {
        log.debug("当前密钥解密失败，尝试历史密钥");
    }
    
    // 尝试历史密钥
    for (String legacyKey : legacyKeys) {
        try {
            return doDecrypt(encryptedPassword, legacyKey);
        } catch (Exception ignored) {
        }
    }
    
    throw new CryptoException("所有密钥解密失败");
}
```

---

## 📝 功能3：注册功能集成加密

### 前端

**文件**：`frontend/packages/portal/src/pages/Register/index.tsx`

```typescript
const handleSubmit = async (values: RegisterRequest) => {
  try {
    // 🔐 加密密码
    const encryptedPassword = encryptPasswordWithTimestamp(values.password);
    
    await register({
      ...values,
      password: encryptedPassword,
    });
    
    message.success('注册成功！');
  } catch (error) {
    message.error('注册失败');
  }
};
```

### 后端

**文件**：`AuthServiceImpl.java` - `register` 方法

```java
@Override
@Transactional
public Long register(RegisterRequest request) {
    // 1. 🔐 解密密码
    String plainPassword = cryptoUtil.decryptPasswordWithTimestamp(request.password());
    
    // 2. 创建用户（使用解密后的密码）
    User user = User.builder()
        .username(request.username())
        .password(passwordEncoder.encode(plainPassword))
        .email(request.email())
        .build();
    
    // ... 保存用户
}
```

---

## 🔧 功能4：修改密码集成加密

### 前端

**文件**：`frontend/packages/portal/src/pages/Profile/ChangePassword.tsx`

```typescript
const handleChangePassword = async (values) => {
  try {
    // 🔐 加密旧密码和新密码
    const encryptedOldPassword = encryptPasswordWithTimestamp(values.oldPassword);
    const encryptedNewPassword = encryptPasswordWithTimestamp(values.newPassword);
    
    await changePassword({
      oldPassword: encryptedOldPassword,
      newPassword: encryptedNewPassword,
    });
    
    message.success('密码修改成功！');
  } catch (error) {
    message.error('密码修改失败');
  }
};
```

### 后端

**文件**：`UserService.java` - `changePassword` 方法

```java
public void changePassword(Long userId, String oldPassword, String newPassword) {
    // 1. 🔐 解密密码
    String plainOldPassword = cryptoUtil.decryptPasswordWithTimestamp(oldPassword);
    String plainNewPassword = cryptoUtil.decryptPasswordWithTimestamp(newPassword);
    
    // 2. 验证旧密码
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException("用户不存在"));
    
    if (!passwordEncoder.matches(plainOldPassword, user.getPassword())) {
        throw new BusinessException("旧密码错误");
    }
    
    // 3. 更新密码
    user.setPassword(passwordEncoder.encode(plainNewPassword));
    userRepository.save(user);
}
```

---

## 📊 实施优先级

### 立即实施（P0）

1. ✅ **防重放攻击** - 最高优先级，防止密文被盗用
2. ✅ **注册加密** - 保护新用户密码

### 第二优先级（P1）

3. ⏳ **多密钥轮换** - 便于未来密钥更换
4. ⏳ **修改密码加密** - 保护密码修改流程

---

## 🧪 测试清单

### 防重放攻击测试

- [ ] 正常登录（时间戳有效）
- [ ] 过期密文登录（时间戳超过5分钟）
- [ ] 篡改时间戳登录

### 多密钥轮换测试

- [ ] 使用新密钥加密的密文可以解密
- [ ] 使用旧密钥加密的密文可以解密
- [ ] 无效密钥加密的密文解密失败

### 注册/修改密码测试

- [ ] 注册时密码加密传输
- [ ] 修改密码时新旧密码都加密传输
- [ ] 密文格式错误时正确提示

---

**维护者**: BaSui 😎  
**最后更新**: 2025-11-06
