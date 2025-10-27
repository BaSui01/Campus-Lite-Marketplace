package com.campus.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密测试工具
 * 
 * 用于生成 BCrypt 加密后的密码
 * 
 * @author BaSui
 * @date 2025-10-25
 */
public class PasswordEncoderTest {

    @Test
    public void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "password123";
        String encodedPassword = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("验证结果: " + encoder.matches(password, encodedPassword));
    }
}
