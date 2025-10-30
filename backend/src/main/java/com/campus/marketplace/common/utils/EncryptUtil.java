package com.campus.marketplace.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密工具类
 *
 * 提供 AES 加密解密和数据脱敏功能
 *
 * 🔐 密钥从配置文件读取（encrypt.aes.key），生产环境通过环境变量配置
 *
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
@Component
public class EncryptUtil {

    private static final String AES_ALGORITHM = "AES";

    /**
     * AES 密钥（从配置文件读取，支持环境变量 AES_KEY）
     */
    @Value("${encrypt.aes.key}")
    private String aesKey;

    /**
     * AES 加密
     *
     * 🔐 使用配置的 AES 密钥进行加密
     *
     * @param plainText 明文
     * @return 加密后的 Base64 字符串
     */
    public String aesEncrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
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
     * 🔓 使用配置的 AES 密钥进行解密
     *
     * @param cipherText 密文（Base64 字符串）
     * @return 解密后的明文
     */
    public String aesDecrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
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
     * 📱 格式：138****5678
     *
     * @param phone 手机号
     * @return 脱敏后的手机号（138****5678）
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏
     *
     * 📧 格式：z***@campus.edu 或 z***d@campus.edu
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱（z***@campus.edu）
     */
    public String maskEmail(String email) {
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
     * 🆔 格式：110***********123
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号（110***********123）
     */
    public String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }

        int length = idCard.length();
        return idCard.substring(0, 3) + "***********" + idCard.substring(length - 3);
    }

    /**
     * 姓名脱敏
     *
     * 👤 格式：张* 或 李**
     *
     * @param name 姓名
     * @return 脱敏后的姓名（张*、李**）
     */
    public String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        if (name.length() == 1) {
            return name;
        }

        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
}
