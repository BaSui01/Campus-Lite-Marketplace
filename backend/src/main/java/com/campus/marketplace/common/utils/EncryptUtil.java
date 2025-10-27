package com.campus.marketplace.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密工具类
 * 
 * 提供 AES 加密解密和数据脱敏功能
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
public class EncryptUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_KEY = "CampusMarket2025"; // 16字节密钥（生产环境应从配置文件读取）

    /**
     * AES 加密
     * 
     * @param plainText 明文
     * @return 加密后的 Base64 字符串
     */
    public static String aesEncrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES 加密失败", e);
            return plainText;
        }
    }

    /**
     * AES 解密
     * 
     * @param cipherText 密文（Base64 字符串）
     * @return 解密后的明文
     */
    public static String aesDecrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        
        try {
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES 解密失败", e);
            return cipherText;
        }
    }

    /**
     * 手机号脱敏
     * 
     * @param phone 手机号
     * @return 脱敏后的手机号（138****5678）
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏
     * 
     * @param email 邮箱
     * @return 脱敏后的邮箱（z***@campus.edu）
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        
        if (username.length() <= 2) {
            return username.charAt(0) + "***@" + parts[1];
        }
        
        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + "@" + parts[1];
    }

    /**
     * 身份证号脱敏
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号（110***********123）
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        
        int length = idCard.length();
        return idCard.substring(0, 3) + "***********" + idCard.substring(length - 3);
    }

    /**
     * 姓名脱敏
     * 
     * @param name 姓名
     * @return 脱敏后的姓名（张*、李**）
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        
        if (name.length() == 1) {
            return name;
        }
        
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
}
