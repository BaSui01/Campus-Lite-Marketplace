# Spec 12: 前端敏感数据加密传输 - 设计文档

> **功能**: 前端密码加密传输安全增强  
> **作者**: BaSui 😎  
> **日期**: 2025-11-06  
> **状态**: 待开发

---

## 📋 目录

- [架构设计](#架构设计)
- [技术选型](#技术选型)
- [模块设计](#模块设计)
- [数据流设计](#数据流设计)
- [安全设计](#安全设计)
- [异常处理](#异常处理)

---

## 🏗️ 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      前端（React + TypeScript）                │
├─────────────────────────────────────────────────────────────┤
│  登录页面                    注册页面               修改密码页面   │
│    ↓                          ↓                       ↓      │
│  [输入密码] → [表单验证] → [密码加密] → [发送请求]             │
│                                ↑                              │
│                          加密工具函数                          │
│                      (crypto.ts - AES-256)                   │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS
                           │ 密文传输
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                       后端（Spring Boot）                      │
├─────────────────────────────────────────────────────────────┤
│  [接收请求] → [密码解密] → [BCrypt验证] → [返回JWT]            │
│                    ↑                                         │
│              解密工具类                                        │
│         (CryptoUtil.java - AES-256)                         │
└─────────────────────────────────────────────────────────────┘
```

### 加密流程

```
用户输入密码
    ↓
前端表单验证（长度、复杂度）
    ↓
前端AES-256加密（固定密钥）
    ↓
Base64编码
    ↓
HTTPS传输（TLS 1.3）
    ↓
后端接收密文
    ↓
Base64解码
    ↓
后端AES-256解密（相同密钥）
    ↓
BCrypt验证
    ↓
返回JWT Token
```

---

## 🔧 技术选型

### 加密算法对比

| 算法 | 优点 | 缺点 | 是否采用 |
|------|------|------|---------|
| **AES-256-CBC** | 性能好、标准成熟、库支持广 | 需要密钥管理 | ✅ **推荐** |
| **RSA-2048** | 非对称加密、无需共享密钥 | 性能差、实现复杂 | ❌ |
| **3DES** | 兼容性好 | 性能差、已不推荐 | ❌ |
| **ChaCha20** | 移动端性能好 | 浏览器支持不佳 | ❌ |

### 选择理由

**选择 AES-256-CBC**：
1. **性能**：加密/解密速度快（<10ms）
2. **安全**：256位密钥，符合FIPS 140-2标准
3. **兼容**：所有现代浏览器支持
4. **成熟**：crypto-js库稳定可靠
5. **简单**：对称加密实现简单

---

## 📦 模块设计

### 前端模块

#### 1. 加密工具模块 (`crypto.ts`)

**位置**：`frontend/packages/shared/src/utils/crypto.ts`

**职责**：
- 提供密码加密函数
- 提供密码解密函数（用于测试）
- 管理加密密钥

**接口设计**：

```typescript
/**
 * 密码加密工具
 * @module utils/crypto
 */

/**
 * 加密密码
 * @param password 明文密码
 * @returns 加密后的Base64字符串
 * @throws Error 加密失败时抛出异常
 */
export function encryptPassword(password: string): string;

/**
 * 解密密码（仅用于测试）
 * @param encryptedPassword 加密的Base64字符串
 * @returns 明文密码
 * @throws Error 解密失败时抛出异常
 */
export function decryptPassword(encryptedPassword: string): string;

/**
 * 生成随机密钥（用于初始化）
 * @returns 32字节的随机密钥（Base64编码）
 */
export function generateKey(): string;
```

**依赖**：
```json
{
  "dependencies": {
    "crypto-js": "^4.2.0"
  },
  "devDependencies": {
    "@types/crypto-js": "^4.2.0"
  }
}
```

#### 2. 环境变量配置

**开发环境** (`.env.development`):
```bash
# 前端加密密钥（开发环境）
VITE_ENCRYPT_KEY=dev-test-key-32-bytes-length!
```

**生产环境** (`.env.production`):
```bash
# 生产环境使用环境变量注入
VITE_ENCRYPT_KEY=${FRONTEND_ENCRYPT_KEY}
```

#### 3. 登录页面集成

**修改文件**：
- `frontend/packages/admin/src/pages/Login/index.tsx`
- `frontend/packages/portal/src/pages/Login/index.tsx`

**集成方式**：
```typescript
import { encryptPassword } from '@campus/shared/utils/crypto';

const handleSubmit = async (values: LoginRequest) => {
  // 加密密码
  const encryptedPassword = encryptPassword(values.password);
  
  // 发送加密后的密码
  await login({
    username: values.username,
    password: encryptedPassword,
  });
};
```

---

### 后端模块

#### 1. 解密工具类 (`CryptoUtil.java`)

**位置**：`backend/src/main/java/com/campus/marketplace/common/util/CryptoUtil.java`

**职责**：
- 提供密码解密功能
- 异常处理和日志记录
- 密钥管理

**接口设计**：

```java
/**
 * 加密解密工具类
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */
@Slf4j
@Component
public class CryptoUtil {
    
    @Value("${app.security.encrypt-key}")
    private String encryptKey;
    
    /**
     * 解密密码
     * 
     * @param encryptedPassword 加密的Base64字符串
     * @return 明文密码
     * @throws CryptoException 解密失败时抛出异常
     */
    public String decryptPassword(String encryptedPassword) throws CryptoException;
    
    /**
     * 加密密码（用于测试）
     * 
     * @param password 明文密码
     * @return 加密后的Base64字符串
     */
    public String encryptPassword(String password);
    
    /**
     * 检查是否为加密密码
     * 
     * @param password 密码字符串
     * @return true=加密密码, false=明文密码
     */
    public boolean isEncrypted(String password);
}
```

#### 2. 配置文件

**application.yml**:
```yaml
app:
  security:
    # 密码加密密钥（AES-256，32字节）
    encrypt-key: ${BACKEND_ENCRYPT_KEY:dev-test-key-32-bytes-length!}
```

**application-prod.yml**:
```yaml
app:
  security:
    # 生产环境从环境变量读取
    encrypt-key: ${BACKEND_ENCRYPT_KEY}
```

#### 3. AuthService 集成

**修改文件**：`backend/src/main/java/com/campus/marketplace/service/impl/AuthServiceImpl.java`

**集成方式**：
```java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final CryptoUtil cryptoUtil;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 解密密码
        String plainPassword;
        try {
            // 检查是否为加密密码
            if (cryptoUtil.isEncrypted(request.getPassword())) {
                plainPassword = cryptoUtil.decryptPassword(request.getPassword());
            } else {
                // 兼容旧客户端明文密码（过渡期）
                plainPassword = request.getPassword();
                log.warn("接收到明文密码，用户名: {}", request.getUsername());
            }
        } catch (CryptoException e) {
            log.error("密码解密失败: {}", e.getMessage());
            throw new BusinessException("密码格式错误，请重试");
        }
        
        // 2. BCrypt验证
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        
        if (!passwordEncoder.matches(plainPassword, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 3. 返回JWT Token
        // ...
    }
}
```

---

## 🔄 数据流设计

### 登录流程

```
┌────────────────────────────────────────────────────────────┐
│ 1. 用户输入                                                  │
│    username: "admin"                                        │
│    password: "admin123"                                     │
└────────────────────┬───────────────────────────────────────┘
                     ↓
┌────────────────────────────────────────────────────────────┐
│ 2. 前端验证                                                  │
│    - 用户名长度 ≥ 3                                          │
│    - 密码长度 ≥ 6                                            │
└────────────────────┬───────────────────────────────────────┘
                     ↓
┌────────────────────────────────────────────────────────────┐
│ 3. 前端加密 (AES-256-CBC)                                   │
│    password: "U2FsdGVkX1+8xZq..." (Base64)                  │
└────────────────────┬───────────────────────────────────────┘
                     ↓
┌────────────────────────────────────────────────────────────┐
│ 4. HTTPS传输                                                │
│    POST /api/auth/login                                     │
│    {                                                        │
│      "username": "admin",                                   │
│      "password": "U2FsdGVkX1+8xZq..."                       │
│    }                                                        │
└────────────────────┬───────────────────────────────────────┘
                     ↓
┌────────────────────────────────────────────────────────────┐
│ 5. 后端接收并解密                                            │
│    encryptedPassword: "U2FsdGVkX1+8xZq..."                  │
│    → Base64解码                                             │
│    → AES-256解密                                            │
│    → plainPassword: "admin123"                              │
└────────────────────┬───────────────────────────────────────┘
                     ↓
┌────────────────────────────────────────────────────────────┐
│ 6. BCrypt验证                                               │
│    plainPassword: "admin123"                                │
│    storedHash: "$2a$10$..."                                 │
│    → BCrypt.matches() → true                                │
└────────────────────┬───────────────────────────────────────┘
                     ↓
┌────────────────────────────────────────────────────────────┐
│ 7. 生成JWT Token                                            │
│    {                                                        │
│      "token": "eyJhbGciOiJIUzI1NiIs...",                    │
│      "user": { "id": 1, "username": "admin" }               │
│    }                                                        │
└────────────────────────────────────────────────────────────┘
```

### 注册流程

```
用户输入 → 前端验证 → 前端加密 → HTTPS传输
  ↓
后端接收 → 解密 → BCrypt哈希 → 存入数据库
  ↓
返回注册成功 → 用户可登录
```

---

## 🔒 安全设计

### 密钥管理

#### 开发环境

**前端密钥**：
```bash
# .env.development
VITE_ENCRYPT_KEY=dev-test-key-32-bytes-length!
```

**后端密钥**：
```yaml
# application-dev.yml
app.security.encrypt-key: dev-test-key-32-bytes-length!
```

#### 生产环境

**前端密钥**（构建时注入）：
```bash
# CI/CD环境变量
FRONTEND_ENCRYPT_KEY=<生产环境随机密钥>
```

**后端密钥**（运行时注入）：
```bash
# Kubernetes Secret / Docker环境变量
BACKEND_ENCRYPT_KEY=<生产环境随机密钥>
```

**密钥生成方法**：
```bash
# 生成32字节随机密钥
openssl rand -base64 32
# 输出示例: 7xK9mP4nL2vB8qW5tR6uY3sA1cD0fE2g
```

### 防止密钥泄露

1. **Git忽略**：
   ```gitignore
   # .gitignore
   .env.production
   .env.local
   ```

2. **环境变量**：
   - ✅ 使用环境变量注入
   - ❌ 禁止硬编码在代码中

3. **密钥轮换**：
   - 每季度更换一次
   - 支持多密钥（新旧兼容）

### 日志脱敏

**禁止记录**：
```java
// ❌ 错误示例
log.info("用户登录: username={}, password={}", username, password);

// ✅ 正确示例
log.info("用户登录: username={}", username);
```

**脱敏工具**：
```java
public class SensitiveUtil {
    public static String maskPassword(String password) {
        return password == null ? null : "******";
    }
}
```

---

## ⚠️ 异常处理

### 前端异常

| 异常场景 | 处理方式 | 用户提示 |
|---------|---------|---------|
| **加密失败** | 捕获异常 + 日志记录 | "系统繁忙，请稍后重试" |
| **密钥未配置** | 控制台警告 | "系统配置错误，请联系管理员" |
| **密码为空** | 表单验证 | "请输入密码" |

**示例代码**：
```typescript
try {
  const encryptedPassword = encryptPassword(password);
  // 发送请求...
} catch (error) {
  console.error('密码加密失败:', error);
  message.error('系统繁忙，请稍后重试');
  return;
}
```

### 后端异常

| 异常场景 | 异常类型 | HTTP状态码 | 错误信息 |
|---------|---------|-----------|---------|
| **解密失败** | `CryptoException` | 400 | "密码格式错误，请重试" |
| **密钥未配置** | `IllegalStateException` | 500 | "系统配置错误" |
| **密文格式错误** | `IllegalArgumentException` | 400 | "密码格式错误" |
| **BCrypt验证失败** | `BusinessException` | 400 | "用户名或密码错误" |

**示例代码**：
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CryptoException.class)
    public ApiResponse<Void> handleCryptoException(CryptoException e) {
        log.error("密码解密失败: {}", e.getMessage());
        return ApiResponse.error(400, "密码格式错误，请重试");
    }
}
```

---

## 🧪 测试设计

### 单元测试

#### 前端测试 (`crypto.test.ts`)

```typescript
describe('crypto', () => {
  it('应该正确加密密码', () => {
    const password = 'admin123';
    const encrypted = encryptPassword(password);
    expect(encrypted).toBeTruthy();
    expect(encrypted).not.toBe(password);
  });

  it('应该正确解密密码', () => {
    const password = 'admin123';
    const encrypted = encryptPassword(password);
    const decrypted = decryptPassword(encrypted);
    expect(decrypted).toBe(password);
  });

  it('空密码应该抛出异常', () => {
    expect(() => encryptPassword('')).toThrow();
  });
});
```

#### 后端测试 (`CryptoUtilTest.java`)

```java
@SpringBootTest
class CryptoUtilTest {
    
    @Autowired
    private CryptoUtil cryptoUtil;
    
    @Test
    void shouldDecryptPassword() {
        String password = "admin123";
        String encrypted = cryptoUtil.encryptPassword(password);
        String decrypted = cryptoUtil.decryptPassword(encrypted);
        assertEquals(password, decrypted);
    }
    
    @Test
    void shouldThrowExceptionForInvalidCiphertext() {
        assertThrows(CryptoException.class, () -> {
            cryptoUtil.decryptPassword("invalid-base64");
        });
    }
}
```

### 集成测试

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerEncryptionTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private CryptoUtil cryptoUtil;
    
    @Test
    void shouldLoginWithEncryptedPassword() throws Exception {
        String password = "admin123";
        String encrypted = cryptoUtil.encryptPassword(password);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"" + encrypted + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists());
    }
}
```

---

## 📊 性能优化

### 前端优化

1. **缓存加密实例**：
   ```typescript
   // 复用crypto-js实例，避免重复创建
   const AES = CryptoJS.AES;
   ```

2. **异步加密**（可选）：
   ```typescript
   async function encryptPasswordAsync(password: string): Promise<string> {
     return new Promise((resolve) => {
       setTimeout(() => {
         resolve(encryptPassword(password));
       }, 0);
     });
   }
   ```

### 后端优化

1. **密钥预加载**：
   ```java
   @PostConstruct
   public void init() {
     // 启动时验证密钥格式
     if (encryptKey == null || encryptKey.length() < 32) {
       throw new IllegalStateException("加密密钥配置错误");
     }
   }
   ```

2. **使用Caffeine缓存**：
   ```java
   @Cacheable(value = "decryptedPasswords", unless = "#result == null")
   public String decryptPassword(String encrypted) {
     // 缓存解密结果（短时间，5分钟）
   }
   ```

---

## 📚 参考文档

- [crypto-js GitHub](https://github.com/brix/crypto-js)
- [AES加密标准](https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.197.pdf)
- [OWASP 密码存储指南](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [Spring Security 加密](https://docs.spring.io/spring-security/reference/features/integrations/cryptography.html)

---

**维护者**: BaSui 😎  
**最后更新**: 2025-11-06
