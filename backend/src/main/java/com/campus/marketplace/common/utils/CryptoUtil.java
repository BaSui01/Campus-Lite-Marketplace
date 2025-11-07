package com.campus.marketplace.common.utils;

import com.campus.marketplace.common.exception.CryptoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * 加密解密工具类
 * 
 * 使用AES算法对密码进行加密解密
 * 
 * ⚠️ 注意：与前端crypto-js保持兼容（支持OpenSSL格式）
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */
@Slf4j
@Component
public class CryptoUtil {
    
    @Value("${app.security.encrypt-key}")
    private String encryptKey;
    
    @Value("${app.security.legacy-keys:}")
    private String legacyKeysString;
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final byte[] SALTED_PREFIX = "Salted__".getBytes(StandardCharsets.UTF_8);
    
    /**
     * 解密密码（带时间戳验证 + 多密钥支持）
     * 
     * 🔑 支持密钥轮换：优先使用当前密钥，失败后尝试历史密钥
     * 🛡️ 防重放攻击：验证时间戳（5分钟有效期）
     * 
     * @param encryptedPassword 加密的Base64字符串
     * @return 明文密码
     * @throws CryptoException 解密失败时抛出异常
     */
    public String decryptPassword(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.trim().isEmpty()) {
            throw new CryptoException("密文不能为空");
        }
        
        // 1. 先尝试当前密钥解密
        try {
            String plainPassword = doDecrypt(encryptedPassword, encryptKey);
            log.debug("✅ 使用当前密钥解密成功");
            return plainPassword;
        } catch (Exception e) {
            log.debug("⚠️ 当前密钥解密失败，尝试历史密钥: {}", e.getMessage());
        }
        
        // 2. 尝试历史密钥解密
        if (legacyKeysString != null && !legacyKeysString.trim().isEmpty()) {
            String[] legacyKeys = legacyKeysString.split(",");
            
            for (int i = 0; i < legacyKeys.length; i++) {
                String legacyKey = legacyKeys[i].trim();
                if (legacyKey.isEmpty()) {
                    continue;
                }
                
                try {
                    String plainPassword = doDecrypt(encryptedPassword, legacyKey);
                    log.info("✅ 使用历史密钥[{}]解密成功", i);
                    return plainPassword;
                } catch (Exception e) {
                    log.debug("⚠️ 历史密钥[{}]解密失败: {}", i, e.getMessage());
                }
            }
        }
        
        // 3. 所有密钥都失败
        log.error("❌ 所有密钥解密失败");
        throw new CryptoException("密码解密失败，所有密钥均无效");
    }
    
    /**
     * 执行解密操作（内部方法）
     * 
     * 🔑 兼容CryptoJS的OpenSSL格式：
     *   - 格式：Salted__[8字节salt][密文]
     *   - 密钥派生：EVP_BytesToKey (MD5)
     *   - 模式：AES-256-CBC
     * 
     * @param encryptedPassword 加密的Base64字符串
     * @param key 密钥
     * @return 明文密码
     * @throws Exception 解密失败时抛出异常
     */
    private String doDecrypt(String encryptedPassword, String key) throws Exception {
        // 1. Base64解码
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
        
        // 2. 检查是否为OpenSSL格式（CryptoJS默认格式）
        if (encryptedBytes.length < 16) {
            throw new CryptoException("密文长度不足");
        }
        
        // 3. 验证"Salted__"前缀
        byte[] prefix = Arrays.copyOfRange(encryptedBytes, 0, 8);
        if (!Arrays.equals(prefix, SALTED_PREFIX)) {
            throw new CryptoException("密码格式错误，请重试");
        }
        
        // 4. 提取Salt（8字节）
        byte[] salt = Arrays.copyOfRange(encryptedBytes, 8, 16);
        
        // 5. 提取密文
        byte[] ciphertext = Arrays.copyOfRange(encryptedBytes, 16, encryptedBytes.length);
        
        // 6. 使用EVP_BytesToKey派生密钥和IV（与CryptoJS兼容）
        byte[][] keyAndIV = deriveKeyAndIV(key.getBytes(StandardCharsets.UTF_8), salt, 32, 16);
        SecretKeySpec keySpec = new SecretKeySpec(keyAndIV[0], ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(keyAndIV[1]);
        
        // 7. AES-CBC解密
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        
        byte[] decryptedBytes = cipher.doFinal(ciphertext);
        String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);
        
        if (decryptedText.isEmpty()) {
            throw new CryptoException("解密结果为空");
        }
        
        log.debug("✅ 解密成功: decryptedText length={}", decryptedText.length());
        
        // 8. 🛡️ 验证时间戳（防重放攻击）
        return validateTimestamp(decryptedText);
    }
    
    /**
     * EVP_BytesToKey密钥派生函数（兼容OpenSSL和CryptoJS）
     * 
     * 算法：key = MD5(password + salt)
     *      如果key长度不足，则：key += MD5(key + password + salt)
     * 
     * @param password 密码字节
     * @param salt 盐值
     * @param keyLen 密钥长度（字节）
     * @param ivLen IV长度（字节）
     * @return [密钥, IV]
     */
    private byte[][] deriveKeyAndIV(byte[] password, byte[] salt, int keyLen, int ivLen) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        int digestLength = 16; // MD5输出16字节
        int requiredLength = keyLen + ivLen;
        byte[] derived = new byte[requiredLength];
        int offset = 0;
        byte[] lastDigest = null;
        
        while (offset < requiredLength) {
            md.reset();
            
            if (lastDigest != null) {
                md.update(lastDigest);
            }
            
            md.update(password);
            md.update(salt);
            
            lastDigest = md.digest();
            int bytesToCopy = Math.min(digestLength, requiredLength - offset);
            System.arraycopy(lastDigest, 0, derived, offset, bytesToCopy);
            offset += bytesToCopy;
        }
        
        // 分割为密钥和IV
        byte[] key = Arrays.copyOfRange(derived, 0, keyLen);
        byte[] iv = Arrays.copyOfRange(derived, keyLen, keyLen + ivLen);
        
        return new byte[][]{key, iv};
    }
    
    /**
     * 验证时间戳（防重放攻击）
     * 
     * @param decryptedText 解密后的文本（格式: timestamp|password）
     * @return 真实密码
     * @throws CryptoException 时间戳验证失败时抛出异常
     */
    private String validateTimestamp(String decryptedText) {
        // 检查是否包含时间戳
        if (!decryptedText.contains("|")) {
            // 兼容旧版本（无时间戳）
            log.warn("⚠️ 接收到无时间戳的密文（兼容模式）");
            return decryptedText;
        }
        
        String[] parts = decryptedText.split("\\|", 2);
        
        if (parts.length != 2) {
            throw new CryptoException("密文格式错误：时间戳解析失败");
        }
        
        try {
            long timestamp = Long.parseLong(parts[0]);
            long now = System.currentTimeMillis();
            long diff = Math.abs(now - timestamp);
            
            // 时间戳有效期：5分钟（300秒）
            long maxAge = 5 * 60 * 1000;
            
            if (diff > maxAge) {
                log.error("❌ 密码已过期: timestamp={}, now={}, diff={}ms", timestamp, now, diff);
                throw new CryptoException("密码已过期，请重新登录");
            }
            
            log.debug("✅ 时间戳验证通过: diff={}ms", diff);
            return parts[1]; // 返回真实密码
            
        } catch (NumberFormatException e) {
            log.error("❌ 时间戳格式错误: {}", parts[0]);
            throw new CryptoException("密文格式错误：时间戳无效", e);
        }
    }
    
    /**
     * 加密密码（用于测试，兼容CryptoJS格式）
     * 
     * ⚠️ 注意：此方法仅用于测试，实际使用应该由前端加密
     * 
     * @param password 明文密码
     * @return 加密后的Base64字符串
     * @throws CryptoException 加密失败时抛出异常
     */
    public String encryptPassword(String password) {
        log.warn("⚠️ encryptPassword 方法仅用于测试，生产环境应由前端加密");
        
        if (password == null || password.trim().isEmpty()) {
            throw new CryptoException("密码不能为空");
        }
        
        try {
            // 添加时间戳（与前端保持一致）
            long timestamp = System.currentTimeMillis();
            String payload = timestamp + "|" + password;
            
            // 生成随机Salt（8字节）
            byte[] salt = new byte[8];
            new java.security.SecureRandom().nextBytes(salt);
            
            // 使用EVP_BytesToKey派生密钥和IV
            byte[][] keyAndIV = deriveKeyAndIV(encryptKey.getBytes(StandardCharsets.UTF_8), salt, 32, 16);
            SecretKeySpec keySpec = new SecretKeySpec(keyAndIV[0], ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(keyAndIV[1]);
            
            // AES-CBC加密
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] ciphertext = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            // 拼接OpenSSL格式：Salted__ + salt + ciphertext
            byte[] result = new byte[SALTED_PREFIX.length + salt.length + ciphertext.length];
            System.arraycopy(SALTED_PREFIX, 0, result, 0, SALTED_PREFIX.length);
            System.arraycopy(salt, 0, result, SALTED_PREFIX.length, salt.length);
            System.arraycopy(ciphertext, 0, result, SALTED_PREFIX.length + salt.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(result);
            
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
            // 尝试Base64解码
            Base64.getDecoder().decode(password);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
}
