package com.campus.marketplace.common.utils;

import com.campus.marketplace.common.exception.CryptoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
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
 * ⚠️ 注意：与前端crypto-js保持兼容
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
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
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
     * @param encryptedPassword 加密的Base64字符串
     * @param key 密钥
     * @return 明文密码
     * @throws Exception 解密失败时抛出异常
     */
    private String doDecrypt(String encryptedPassword, String key) throws Exception {
        // 1. Base64解码
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPassword);
        
        // 2. 生成密钥
        SecretKeySpec keySpec = generateKey(key);
        
        // 3. AES解密
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);
        
        if (decryptedText.isEmpty()) {
            throw new CryptoException("解密结果为空");
        }
        
        // 4. 🛡️ 验证时间戳（防重放攻击）
        return validateTimestamp(decryptedText);
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
     * 加密密码（用于测试）
     * 
     * @param password 明文密码
     * @return 加密后的Base64字符串
     * @throws CryptoException 加密失败时抛出异常
     */
    public String encryptPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new CryptoException("密码不能为空");
        }
        
        try {
            // 生成密钥
            SecretKeySpec keySpec = generateKey(encryptKey);
            
            // AES加密
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
            // 尝试Base64解码
            Base64.getDecoder().decode(password);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 生成AES密钥
     * 
     * ⚠️ 使用MD5哈希将任意长度的密钥转换为128位（16字节）
     * 注意：crypto-js默认使用MD5派生密钥
     * 
     * @param key 原始密钥字符串
     * @return SecretKeySpec
     */
    private SecretKeySpec generateKey(String key) {
        try {
            // 使用MD5哈希生成16字节密钥（与crypto-js保持一致）
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] keyBytes = md.digest(key.getBytes(StandardCharsets.UTF_8));
            
            // 取前16字节作为AES-128密钥
            byte[] aesKey = Arrays.copyOf(keyBytes, 16);
            
            return new SecretKeySpec(aesKey, ALGORITHM);
        } catch (Exception e) {
            throw new CryptoException("密钥生成失败", e);
        }
    }
}
