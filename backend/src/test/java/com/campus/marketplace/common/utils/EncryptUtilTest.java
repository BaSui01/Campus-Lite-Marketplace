package com.campus.marketplace.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("加密工具测试")
class EncryptUtilTest {

    private EncryptUtil encryptUtil;

    @BeforeEach
    void setUp() {
        encryptUtil = new EncryptUtil();
        ReflectionTestUtils.setField(encryptUtil, "aesKey", "1234567890123456");
    }

    @Test
    @DisplayName("AES 加密解密往返成功")
    void aesEncryptDecrypt_roundTrip() {
        String cipher = encryptUtil.aesEncrypt("HelloWorld");

        assertNotEquals("HelloWorld", cipher);
        assertEquals("HelloWorld", encryptUtil.aesDecrypt(cipher));
    }

    @Test
    @DisplayName("AES 解密失败时返回原文")
    void aesDecrypt_invalidCipher_returnsOriginal() {
        assertEquals("invalid", encryptUtil.aesDecrypt("invalid"));
    }

    @Test
    @DisplayName("手机号脱敏按规则输出")
    void maskPhone_shouldMask() {
        assertEquals("138****1234", encryptUtil.maskPhone("13812341234"));
        assertEquals("123", encryptUtil.maskPhone("123"));
    }

    @Test
    @DisplayName("邮箱脱敏兼容短用户名")
    void maskEmail_shouldMask() {
        assertEquals("z***n@campus.edu", encryptUtil.maskEmail("zhangsan@campus.edu"));
        assertEquals("a***@campus.edu", encryptUtil.maskEmail("ab@campus.edu"));
        assertEquals("plain", encryptUtil.maskEmail("plain"));
    }

    @Test
    @DisplayName("身份证与姓名脱敏输出预期")
    void maskIdCardAndName_shouldMask() {
        assertEquals("110***********123", encryptUtil.maskIdCard("110987654321123"));
        assertEquals("id", encryptUtil.maskIdCard("id"));
        assertThat(encryptUtil.maskName("张三丰")).isEqualTo("张**");
        assertThat(encryptUtil.maskName("李")).isEqualTo("李");
    }
}
