package com.campus.marketplace;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 生成 BCrypt 加密密码工具类
 * 用于生成数据库迁移脚本中的密码
 *
 * @author BaSui 😎
 * @date 2025-11-05
 */
public class GenerateBCryptPassword {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("=".repeat(60));
        System.out.println("BCrypt 密码生成工具 🔐");
        System.out.println("=".repeat(60));

        // 管理员密码
        String adminPassword = "admin123";
        String adminEncoded = encoder.encode(adminPassword);
        System.out.println("\n管理员密码:");
        System.out.println("原始密码: " + adminPassword);
        System.out.println("加密密码: " + adminEncoded);

        // 测试用户密码
        String testPassword = "password123";
        String testEncoded = encoder.encode(testPassword);
        System.out.println("\n测试用户密码:");
        System.out.println("原始密码: " + testPassword);
        System.out.println("加密密码: " + testEncoded);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ 密码生成完成！请复制到 SQL 脚本中使用！");
        System.out.println("=".repeat(60));
    }
}
