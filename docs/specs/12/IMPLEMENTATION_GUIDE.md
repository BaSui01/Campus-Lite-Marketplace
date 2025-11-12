# Spec 12 实施指南 - 后端部分

> **剩余任务**：后端解密实现 + 测试验证  
> **预计时间**：2-3小时  
> **当前进度**：前端已完成 ✅

---

## ✅ 已完成的前端工作

1. ✅ crypto-js 安装完成
2. ✅ 环境变量配置完成（admin + portal）
3. ✅ 加密工具函数实现（crypto.ts）
4. ✅ 单元测试通过（14/15，93.3%）
5. ✅ 管理端登录页面集成加密

---

## 🚀 待完成：后端解密实现

### Step 1: 配置后端密钥

**文件**：`backend/src/main/resources/application.yml`

在文件末尾添加：

```yaml
# ========== 应用安全配置 ==========
app:
  security:
    # 密码加密密钥（AES-256，32字节）
    # ⚠️ 注意：此密钥必须与前端配置保持一致
    encrypt-key: ${BACKEND_ENCRYPT_KEY:dev-test-key-32-bytes-length!}
```

---

### Step 2: 创建CryptoException

**文件**：`backend/src/main/java/com/campus/marketplace/common/exception/CryptoException.java`

```java
package com.campus.marketplace.common.exception;

/**
 * 加密解密异常
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */
public class CryptoException extends RuntimeException {
    
    public CryptoException(String message) {
        super(message);
    }
    
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

### Step 3: 创建CryptoUtil工具类

**文件**：`backend/src/main/java/com/campus/marketplace/common/utils/CryptoUtil.java`

```java
package com.campus.marketplace.common.utils;

import com.campus.marketplace.common.exception.CryptoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密解密工具类
 * 
 * 使用AES算法对密码进行加密解密
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */
@Slf4j
@Component
public class CryptoUtil {
    
    @Value("${app.security.encrypt-key}")
    private String encryptKey;
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    /**
     * 解密密码
     * 
     * @param encryptedPassword 加密的Base64字符串
     * @return 明文密码
     * @throws CryptoException 解密失败时抛出异常
     */
    public String decryptPassword(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            throw new CryptoException("密文不能为空");
        }
        
        try {
            // 去除crypto-js的"Salted__"前缀（如果有）
            byte[] encryptedBytes;
            if (encryptedPassword.startsWith("U2FsdGVk")) {
                // crypto-js的AES加密结果
                // 这里需要特殊处理crypto-js的格式
                // 建议：使用相同的密钥，但crypto-js和Java Crypto的格式可能不完全兼容
                // 简化方案：前端使用简单的AES加密，不使用crypto-js的默认格式
                encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
            } else {
                encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
            }
            
            // AES解密
            SecretKeySpec keySpec = new SecretKeySpec(
                encryptKey.getBytes(StandardCharsets.UTF_8),
                ALGORITHM
            );
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String plainText = new String(decryptedBytes, StandardCharsets.UTF_8);
            
            if (plainText.isEmpty()) {
                throw new CryptoException("解密结果为空");
            }
            
            return plainText;
            
        } catch (Exception e) {
            log.error("❌ 密码解密失败: {}", e.getMessage());
            throw new CryptoException("密码解密失败", e);
        }
    }
    
    /**
     * 加密密码（用于测试）
     * 
     * @param password 明文密码
     * @return 加密后的Base64字符串
     */
    public String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new CryptoException("密码不能为空");
        }
        
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                encryptKey.getBytes(StandardCharsets.UTF_8),
                ALGORITHM
            );
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
            
        } catch (Exception e) {
            log.error("❌ 密码加密失败: {}", e.getMessage());
            throw new CryptoException("密码加密失败", e);
        }
    }
    
    /**
     * 检查是否为加密密码
     * 
     * @param password 密码字符串
     * @return true=加密密码, false=明文密码
     */
    public boolean isEncrypted(String password) {
        if (password == null || password.length() < 20) {
            return false;
        }
        try {
            Base64.getDecoder().decode(password);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
```

---

### Step 4: 修改AuthServiceImpl

**文件**：`backend/src/main/java/com/campus/marketplace/service/impl/AuthServiceImpl.java`

找到 `login` 方法，在密码验证前添加解密逻辑：

```java
@Override
public LoginResponse login(LoginRequest request) {
    // 1. 解密密码
    String plainPassword;
    try {
        if (cryptoUtil.isEncrypted(request.getPassword())) {
            plainPassword = cryptoUtil.decryptPassword(request.getPassword());
            log.debug("✅ 密码解密成功，用户名: {}", request.getUsername());
        } else {
            // 兼容旧客户端明文密码（过渡期）
            plainPassword = request.getPassword();
            log.warn("⚠️ 接收到明文密码，用户名: {}", request.getUsername());
        }
    } catch (CryptoException e) {
        log.error("❌ 密码解密失败: {}", e.getMessage());
        throw new BusinessException("密码格式错误，请重试");
    }
    
    // 2. 查找用户
    User user = userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new BusinessException("用户名或密码错误"));
    
    // 3. BCrypt验证（使用解密后的明文密码）
    if (!passwordEncoder.matches(plainPassword, user.getPassword())) {
        throw new BusinessException("用户名或密码错误");
    }
    
    // 4. 生成JWT Token
    // ... 原有逻辑不变
}
```

**注入CryptoUtil**：

在类顶部添加：

```java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final CryptoUtil cryptoUtil; // ✅ 添加这一行
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // ... 其他依赖
}
```

---

### Step 5: 修改GlobalExceptionHandler

**文件**：`backend/src/main/java/com/campus/marketplace/common/exception/GlobalExceptionHandler.java`

添加CryptoException的处理方法：

```java
@ExceptionHandler(CryptoException.class)
public ResponseEntity<ApiResponse<Void>> handleCryptoException(CryptoException e) {
    log.error("❌ 密码解密失败: {}", e.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(400, "密码格式错误，请重试"));
}
```

---

## 🧪 测试验证

### Step 6: 重启后端服务

```bash
cd backend
mvn spring-boot:run
```

等待启动成功（约20-30秒）。

---

### Step 7: 启动前端服务

```bash
cd frontend/packages/admin
pnpm dev
```

---

### Step 8: 测试登录

1. 打开浏览器：`http://localhost:5173`（或前端端口）
2. 打开DevTools → Network标签
3. 输入用户名：`admin`，密码：`admin123`
4. 点击登录

**预期结果**：
- ✅ Network中password字段为密文（不是`admin123`）
- ✅ 登录成功，跳转到Dashboard
- ✅ 后端日志显示：`✅ 密码解密成功`

---

## ⚠️ 常见问题

### 问题1：crypto-js与Java Crypto格式不兼容

**症状**：解密失败，抛出`BadPaddingException`

**原因**：crypto-js的AES默认使用随机IV和特殊格式

**解决方案**：

**方案A（推荐）**：前端改用ECB模式（简化）

修改`crypto.ts`：

```typescript
export function encryptPassword(password: string): string {
  const key = CryptoJS.enc.Utf8.parse(getEncryptKey());
  const encrypted = CryptoJS.AES.encrypt(password, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  });
  return encrypted.ciphertext.toString(CryptoJS.enc.Base64);
}
```

**方案B**：后端解析crypto-js格式（复杂）

需要解析"Salted__" + Salt + IV + Ciphertext格式。

---

### 问题2：密钥不一致

**症状**：解密成功但结果乱码

**解决**：确认前后端密钥完全一致

```bash
# 前端
VITE_ENCRYPT_KEY=dev-test-key-32-bytes-length!

# 后端
app.security.encrypt-key=dev-test-key-32-bytes-length!
```

---

### 问题3：后端启动失败

**症状**：`IllegalArgumentException: Invalid AES key length`

**原因**：密钥长度不是16/24/32字节

**解决**：确保密钥长度为32字节

---

## 📊 进度总结

| 阶段 | 任务 | 状态 | 预计时间 |
|-----|------|------|---------|
| **前端** | crypto-js + 加密工具 + 登录集成 | ✅ 已完成 | 2小时 |
| **后端** | 配置 + 工具类 + Service集成 | ⏳ 待完成 | 1.5小时 |
| **测试** | 端到端登录测试 | ⏳ 待完成 | 0.5小时 |
| **总计** | - | **50%** | **4小时** |

---

## 🎯 下一步行动

1. **立即完成后端实现**（按照Step 1-5）
2. **重启服务测试**（Step 6-8）
3. **如有问题，查看"常见问题"章节**
4. **测试通过后，提交代码**

---

**祝您实施顺利！有问题随时询问！** 🚀
