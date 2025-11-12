# Spec 12: 前端敏感数据加密传输 - 任务分解

> **功能**: 前端密码加密传输安全增强  
> **作者**: BaSui 😎  
> **日期**: 2025-11-06  
> **状态**: 待开发

---

## 📋 任务清单

### 🔍 阶段0：复用检查（必须优先执行）

- [ ] **Task 0.1**: 检查项目中是否已有加密工具
  - 搜索关键词：`encrypt`, `crypto`, `AES`, `RSA`
  - 检查 `utils/` 目录是否有相关工具函数
  - 检查 `pom.xml` / `package.json` 是否有加密库依赖

- [ ] **Task 0.2**: 检查后端是否已有解密逻辑
  - 搜索关键词：`decrypt`, `CryptoUtil`, `PasswordUtil`
  - 检查 `common/util/` 目录
  - 检查 `AuthService` 是否有相关代码

- [ ] **Task 0.3**: 检查是否有环境变量配置
  - 检查 `.env` 文件中是否有 `ENCRYPT_KEY`
  - 检查 `application.yml` 中是否有加密密钥配置

**预期结果**：
- ✅ 无现有加密工具 → 继续新建
- ⚠️ 有相似工具 → 复用/扩展现有工具

---

## 🎯 阶段1：前端加密实现（第1-2天）

### Task 1.1: 安装加密库依赖

**目标**：安装 `crypto-js` 加密库到 shared 包

**步骤**：
1. 进入 shared 包目录：
   ```bash
   cd frontend/packages/shared
   ```

2. 安装依赖：
   ```bash
   pnpm add crypto-js
   pnpm add -D @types/crypto-js
   ```

3. 验证安装：
   ```bash
   pnpm list crypto-js
   ```

**验收标准**：
- ✅ `package.json` 中包含 `crypto-js` 依赖
- ✅ `node_modules` 中存在 `crypto-js` 目录
- ✅ TypeScript 类型定义正常导入

**文件清单**：
- `frontend/packages/shared/package.json` (修改)

---

### Task 1.2: 配置环境变量

**目标**：配置前端加密密钥

**步骤**：
1. 创建开发环境配置：
   ```bash
   # admin/.env.development
   VITE_ENCRYPT_KEY=dev-test-key-32-bytes-length!
   
   # portal/.env.development
   VITE_ENCRYPT_KEY=dev-test-key-32-bytes-length!
   ```

2. 添加到 `.gitignore`：
   ```bash
   # .gitignore
   .env.local
   .env.production.local
   ```

3. 更新 `.env.example`（示例文件）：
   ```bash
   # 密码加密密钥（32字节，仅示例，生产环境请随机生成）
   VITE_ENCRYPT_KEY=your-32-bytes-random-key-here!
   ```

**验收标准**：
- ✅ `.env.development` 包含密钥配置
- ✅ `.gitignore` 忽略本地环境文件
- ✅ `.env.example` 提供示例配置

**文件清单**：
- `frontend/packages/admin/.env.development` (新建)
- `frontend/packages/portal/.env.development` (新建)
- `frontend/.gitignore` (修改)
- `frontend/packages/admin/.env.example` (新建)

---

### Task 1.3: 实现加密工具函数

**目标**：创建 `crypto.ts` 加密工具模块

**步骤**：
1. 创建文件：`frontend/packages/shared/src/utils/crypto.ts`

2. 实现加密函数：
   ```typescript
   import CryptoJS from 'crypto-js';

   /**
    * 获取加密密钥
    */
   const getEncryptKey = (): string => {
     const key = import.meta.env.VITE_ENCRYPT_KEY;
     if (!key) {
       throw new Error('加密密钥未配置，请检查环境变量 VITE_ENCRYPT_KEY');
     }
     return key;
   };

   /**
    * 加密密码
    */
   export function encryptPassword(password: string): string {
     if (!password) {
       throw new Error('密码不能为空');
     }
     
     try {
       const key = getEncryptKey();
       const encrypted = CryptoJS.AES.encrypt(password, key).toString();
       return encrypted;
     } catch (error) {
       console.error('密码加密失败:', error);
       throw new Error('密码加密失败，请重试');
     }
   }

   /**
    * 解密密码（仅用于测试）
    */
   export function decryptPassword(encryptedPassword: string): string {
     if (!encryptedPassword) {
       throw new Error('密文不能为空');
     }
     
     try {
       const key = getEncryptKey();
       const decrypted = CryptoJS.AES.decrypt(encryptedPassword, key);
       return decrypted.toString(CryptoJS.enc.Utf8);
     } catch (error) {
       console.error('密码解密失败:', error);
       throw new Error('密码解密失败');
     }
   }

   /**
    * 生成随机密钥（用于初始化）
    */
   export function generateKey(): string {
     return CryptoJS.lib.WordArray.random(32).toString(CryptoJS.enc.Base64);
   }
   ```

3. 导出到 `index.ts`：
   ```typescript
   // frontend/packages/shared/src/utils/index.ts
   export * from './crypto';
   ```

**验收标准**：
- ✅ `crypto.ts` 文件创建完成
- ✅ 加密/解密函数正常工作
- ✅ 异常处理完善
- ✅ TypeScript 类型检查通过

**文件清单**：
- `frontend/packages/shared/src/utils/crypto.ts` (新建)
- `frontend/packages/shared/src/utils/index.ts` (修改)

---

### Task 1.4: 编写前端单元测试

**目标**：为加密工具编写单元测试

**步骤**：
1. 创建测试文件：`frontend/packages/shared/src/utils/crypto.test.ts`

2. 编写测试用例：
   ```typescript
   import { describe, it, expect, beforeEach } from 'vitest';
   import { encryptPassword, decryptPassword, generateKey } from './crypto';

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
       expect(() => encryptPassword('')).toThrow('密码不能为空');
     });

     it('无效密文应该抛出异常', () => {
       expect(() => decryptPassword('invalid-base64')).toThrow();
     });

     it('应该生成32字节的随机密钥', () => {
       const key = generateKey();
       expect(key).toBeTruthy();
       expect(key.length).toBeGreaterThan(30);
     });
   });
   ```

3. 运行测试：
   ```bash
   cd frontend/packages/shared
   pnpm test crypto.test.ts
   ```

**验收标准**：
- ✅ 所有测试用例通过
- ✅ 测试覆盖率 ≥ 90%
- ✅ 边界条件测试完善

**文件清单**：
- `frontend/packages/shared/src/utils/crypto.test.ts` (新建)

---

### Task 1.5: 修改管理端登录页面

**目标**：管理端登录页面集成密码加密

**步骤**：
1. 修改文件：`frontend/packages/admin/src/pages/Login/index.tsx`

2. 导入加密函数：
   ```typescript
   import { encryptPassword } from '@campus/shared/utils/crypto';
   ```

3. 修改提交逻辑：
   ```typescript
   const handleSubmit = async (values: LoginRequest) => {
     setLoading(true);

     try {
       // 🔐 加密密码
       const encryptedPassword = encryptPassword(values.password);
       
       // 发送加密后的密码
       await login({
         username: values.username,
         password: encryptedPassword,
       });
       
       message.success('欢迎回来，管理员！😎');
       navigate('/admin/dashboard');
     } catch (error: any) {
       console.error('❌ 登录失败:', error);
       message.error(error?.message || '登录失败，请稍后再试');
     } finally {
       setLoading(false);
     }
   };
   ```

**验收标准**：
- ✅ 登录时密码被加密
- ✅ 网络请求中密码为密文
- ✅ 登录功能正常

**文件清单**：
- `frontend/packages/admin/src/pages/Login/index.tsx` (修改)

---

### Task 1.6: 修改用户端登录页面（如果存在）

**目标**：用户端登录页面集成密码加密

**步骤**：与 Task 1.5 类似，修改 `frontend/packages/portal/src/pages/Login/index.tsx`

**验收标准**：
- ✅ 登录时密码被加密
- ✅ 网络请求中密码为密文

**文件清单**：
- `frontend/packages/portal/src/pages/Login/index.tsx` (修改，如果存在)

---

### Task 1.7: 修改注册页面（可选）

**目标**：注册页面集成密码加密

**步骤**：
1. 查找注册页面文件
2. 导入 `encryptPassword`
3. 修改提交逻辑加密密码

**验收标准**：
- ✅ 注册时密码被加密
- ✅ 注册成功后可以登录

**文件清单**：
- `frontend/packages/portal/src/pages/Register/index.tsx` (修改，如果存在)

---

## 🔧 阶段2：后端解密实现（第3-4天）

### Task 2.1: 配置后端加密密钥

**目标**：在后端配置加密密钥

**步骤**：
1. 修改 `application.yml`：
   ```yaml
   app:
     security:
       # 密码加密密钥（AES-256，32字节）
       encrypt-key: ${BACKEND_ENCRYPT_KEY:dev-test-key-32-bytes-length!}
   ```

2. 修改 `application-dev.yml`（开发环境）：
   ```yaml
   app:
     security:
       encrypt-key: dev-test-key-32-bytes-length!
   ```

3. 修改 `application-prod.yml`（生产环境）：
   ```yaml
   app:
     security:
       encrypt-key: ${BACKEND_ENCRYPT_KEY}
   ```

**验收标准**：
- ✅ 配置文件正确配置
- ✅ 开发环境可以读取密钥
- ✅ 生产环境使用环境变量

**文件清单**：
- `backend/src/main/resources/application.yml` (修改)
- `backend/src/main/resources/application-dev.yml` (修改)
- `backend/src/main/resources/application-prod.yml` (修改)

---

### Task 2.2: 创建自定义异常类

**目标**：创建 `CryptoException` 异常类

**步骤**：
1. 创建文件：`backend/src/main/java/com/campus/marketplace/common/exception/CryptoException.java`

2. 实现异常类：
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

**验收标准**：
- ✅ 异常类创建完成
- ✅ 编译通过

**文件清单**：
- `backend/src/main/java/com/campus/marketplace/common/exception/CryptoException.java` (新建)

---

### Task 2.3: 实现解密工具类

**目标**：创建 `CryptoUtil` 解密工具类

**步骤**：
1. 创建文件：`backend/src/main/java/com/campus/marketplace/common/util/CryptoUtil.java`

2. 实现解密逻辑：
   ```java
   package com.campus.marketplace.common.util;

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
    * @author BaSui 😎
    * @date 2025-11-06
    */
   @Slf4j
   @Component
   public class CryptoUtil {
       
       @Value("${app.security.encrypt-key}")
       private String encryptKey;
       
       private static final String ALGORITHM = "AES";
       
       /**
        * 解密密码
        */
       public String decryptPassword(String encryptedPassword) {
           if (encryptedPassword == null || encryptedPassword.isEmpty()) {
               throw new CryptoException("密文不能为空");
           }
           
           try {
               // Base64解码
               byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
               
               // AES解密
               SecretKeySpec keySpec = new SecretKeySpec(
                   encryptKey.getBytes(StandardCharsets.UTF_8), 
                   ALGORITHM
               );
               Cipher cipher = Cipher.getInstance(ALGORITHM);
               cipher.init(Cipher.DECRYPT_MODE, keySpec);
               
               byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
               return new String(decryptedBytes, StandardCharsets.UTF_8);
               
           } catch (Exception e) {
               log.error("密码解密失败: {}", e.getMessage());
               throw new CryptoException("密码解密失败", e);
           }
       }
       
       /**
        * 加密密码（用于测试）
        */
       public String encryptPassword(String password) {
           // 实现加密逻辑（与前端对应）
       }
       
       /**
        * 检查是否为加密密码（简单判断：Base64格式 + 长度）
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

**验收标准**：
- ✅ 工具类创建完成
- ✅ 解密逻辑正确
- ✅ 异常处理完善
- ✅ 编译通过

**文件清单**：
- `backend/src/main/java/com/campus/marketplace/common/util/CryptoUtil.java` (新建)

---

### Task 2.4: 修改 AuthService 集成解密

**目标**：在 `AuthServiceImpl` 中集成密码解密

**步骤**：
1. 修改文件：`backend/src/main/java/com/campus/marketplace/service/impl/AuthServiceImpl.java`

2. 注入 `CryptoUtil`：
   ```java
   @Service
   @RequiredArgsConstructor
   public class AuthServiceImpl implements AuthService {
       
       private final CryptoUtil cryptoUtil;
       private final UserRepository userRepository;
       private final PasswordEncoder passwordEncoder;
       
       // ...
   }
   ```

3. 修改登录方法：
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
       
       // 3. BCrypt验证
       if (!passwordEncoder.matches(plainPassword, user.getPassword())) {
           throw new BusinessException("用户名或密码错误");
       }
       
       // 4. 生成JWT Token
       // ... 原有逻辑
   }
   ```

**验收标准**：
- ✅ 登录方法集成解密
- ✅ 兼容明文密码（过渡期）
- ✅ 异常处理完善
- ✅ 日志记录清晰

**文件清单**：
- `backend/src/main/java/com/campus/marketplace/service/impl/AuthServiceImpl.java` (修改)

---

### Task 2.5: 添加全局异常处理

**目标**：在 `GlobalExceptionHandler` 中处理 `CryptoException`

**步骤**：
1. 修改文件：`backend/src/main/java/com/campus/marketplace/common/exception/GlobalExceptionHandler.java`

2. 添加异常处理方法：
   ```java
   @ExceptionHandler(CryptoException.class)
   public ResponseEntity<ApiResponse<Void>> handleCryptoException(CryptoException e) {
       log.error("❌ 密码解密失败: {}", e.getMessage());
       return ResponseEntity
           .status(HttpStatus.BAD_REQUEST)
           .body(ApiResponse.error(400, "密码格式错误，请重试"));
   }
   ```

**验收标准**：
- ✅ 异常处理方法添加完成
- ✅ 返回统一错误格式
- ✅ 日志记录完善

**文件清单**：
- `backend/src/main/java/com/campus/marketplace/common/exception/GlobalExceptionHandler.java` (修改)

---

### Task 2.6: 编写后端单元测试

**目标**：为 `CryptoUtil` 编写单元测试

**步骤**：
1. 创建测试文件：`backend/src/test/java/com/campus/marketplace/common/util/CryptoUtilTest.java`

2. 编写测试用例：
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
       
       @Test
       void shouldDetectEncryptedPassword() {
           String encrypted = cryptoUtil.encryptPassword("admin123");
           assertTrue(cryptoUtil.isEncrypted(encrypted));
           assertFalse(cryptoUtil.isEncrypted("admin123"));
       }
   }
   ```

3. 运行测试：
   ```bash
   mvn test -Dtest=CryptoUtilTest
   ```

**验收标准**：
- ✅ 所有测试用例通过
- ✅ 测试覆盖率 ≥ 85%

**文件清单**：
- `backend/src/test/java/com/campus/marketplace/common/util/CryptoUtilTest.java` (新建)

---

## 🧪 阶段3：集成测试（第5天）

### Task 3.1: 端到端登录测试

**目标**：测试完整的登录加密流程

**步骤**：
1. 启动后端服务
2. 启动前端服务
3. 在登录页面输入密码
4. 打开浏览器DevTools → Network
5. 查看请求payload中的密码字段

**验收标准**：
- ✅ 密码字段为密文（如：`U2FsdGVkX1...`）
- ✅ 登录成功返回JWT Token
- ✅ 控制台无错误

---

### Task 3.2: 性能测试

**目标**：验证加密对性能的影响

**步骤**：
1. 使用JMeter或Postman进行压力测试
2. 测试场景：1000个并发登录请求
3. 对比加密前后的性能差异

**验收标准**：
- ✅ P95响应时间 < 200ms
- ✅ 加密性能损失 < 5%

---

### Task 3.3: 安全测试

**目标**：验证加密的安全性

**步骤**：
1. 使用抓包工具（Wireshark / Fiddler）
2. 捕获登录请求
3. 验证密码字段为密文
4. 尝试重放攻击（可选）

**验收标准**：
- ✅ 抓包无法获取明文密码
- ✅ 密文无法直接解密（无密钥）

---

### Task 3.4: 兼容性测试

**目标**：测试不同场景下的兼容性

**测试场景**：
1. 旧客户端明文密码登录（兼容性）
2. 新客户端加密密码登录（标准流程）
3. 错误密文登录（异常处理）

**验收标准**：
- ✅ 旧客户端可以登录（过渡期）
- ✅ 新客户端加密登录正常
- ✅ 错误密文返回友好错误信息

---

## 📊 总体进度跟踪

| 阶段 | 任务数 | 预计时间 | 状态 |
|-----|-------|---------|------|
| **阶段0：复用检查** | 3 | 0.5天 | ⏳ 待开始 |
| **阶段1：前端加密** | 7 | 2天 | ⏳ 待开始 |
| **阶段2：后端解密** | 6 | 2天 | ⏳ 待开始 |
| **阶段3：集成测试** | 4 | 0.5天 | ⏳ 待开始 |
| **总计** | **20** | **5天** | **0%** |

---

## 📁 文件清单汇总

### 前端文件

| 文件路径 | 操作 | 说明 |
|---------|------|------|
| `frontend/packages/shared/package.json` | 修改 | 添加crypto-js依赖 |
| `frontend/packages/shared/src/utils/crypto.ts` | 新建 | 加密工具函数 |
| `frontend/packages/shared/src/utils/crypto.test.ts` | 新建 | 单元测试 |
| `frontend/packages/shared/src/utils/index.ts` | 修改 | 导出加密函数 |
| `frontend/packages/admin/.env.development` | 新建 | 开发环境配置 |
| `frontend/packages/admin/.env.example` | 新建 | 配置示例 |
| `frontend/packages/admin/src/pages/Login/index.tsx` | 修改 | 登录页面集成 |
| `frontend/packages/portal/.env.development` | 新建 | 开发环境配置 |
| `frontend/packages/portal/src/pages/Login/index.tsx` | 修改 | 登录页面集成 |

### 后端文件

| 文件路径 | 操作 | 说明 |
|---------|------|------|
| `backend/src/main/resources/application.yml` | 修改 | 添加密钥配置 |
| `backend/src/main/resources/application-dev.yml` | 修改 | 开发环境密钥 |
| `backend/src/main/resources/application-prod.yml` | 修改 | 生产环境密钥 |
| `backend/src/main/java/.../CryptoException.java` | 新建 | 自定义异常 |
| `backend/src/main/java/.../CryptoUtil.java` | 新建 | 解密工具类 |
| `backend/src/main/java/.../AuthServiceImpl.java` | 修改 | 集成解密逻辑 |
| `backend/src/main/java/.../GlobalExceptionHandler.java` | 修改 | 异常处理 |
| `backend/src/test/java/.../CryptoUtilTest.java` | 新建 | 单元测试 |

---

## ⚠️ 注意事项

1. **密钥安全**：
   - ❌ 禁止将生产环境密钥提交到Git
   - ✅ 使用环境变量或密钥管理服务

2. **兼容性**：
   - 保留旧客户端明文密码支持（过渡期1个月）
   - 通过日志监控明文密码请求量

3. **性能监控**：
   - 监控加密解密耗时
   - 如果性能下降>5%，考虑优化方案

4. **日志安全**：
   - 禁止记录明文密码
   - 禁止记录密文密码
   - 禁止记录加密密钥

---

## 📚 参考文档

- [requirements.md](./requirements.md) - 功能需求文档
- [design.md](./design.md) - 设计文档
- [../tech.md](../tech.md) - 技术栈规范
- [../structure.md](../structure.md) - 项目结构规范
- [../../CLAUDE.md](../../CLAUDE.md) - 开发规范

---

**维护者**: BaSui 😎  
**最后更新**: 2025-11-06
