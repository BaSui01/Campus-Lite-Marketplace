package com.campus.marketplace;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密与匹配性测试
 */
public class PasswordEncoderTest {

    @Test
    @DisplayName("BCrypt 加密后应能正确匹配原始密码")
    void bcryptEncodeAndMatch() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String encodedPassword = encoder.encode(password);
        assertTrue(encoder.matches(password, encodedPassword));
    }
}
