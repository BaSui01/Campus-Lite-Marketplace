package com.campus.marketplace.service;

import com.campus.marketplace.common.config.SmsProperties;
import com.campus.marketplace.service.impl.VerificationCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("验证码服务实现测试")
class VerificationCodeServiceImplTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private JavaMailSender mailSender;
    @Mock private SmsService smsService;
    @Mock private SmsProperties smsProperties;
    @Mock private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private VerificationCodeServiceImpl verificationCodeService;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(true);
        lenient().when(valueOperations.increment(anyString())).thenReturn(1L);
        lenient().when(smsProperties.getTemplateRegister()).thenReturn("T_REGISTER");
        lenient().when(smsProperties.getTemplateReset()).thenReturn("T_RESET");
    }

    @Test
    @DisplayName("发送邮箱验证码写入Redis并发送邮件")
    void sendEmailCode_success() {
        String email = "user@example.com";

        verificationCodeService.sendEmailCode(email, "REGISTER");

        verify(valueOperations).set(startsWith("verif:email:"), anyString(), eq(Duration.ofMinutes(10)));
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).contains(email);
        assertThat(captor.getValue().getText()).contains("验证码");
    }

    @Test
    @DisplayName("邮箱验证码触发频率限制抛出异常")
    void sendEmailCode_rateLimited() {
        when(valueOperations.increment("verif:rate:email:user@example.com")).thenReturn(4L);

        assertThatThrownBy(() -> verificationCodeService.sendEmailCode("user@example.com", "REGISTER"))
                .isInstanceOf(IllegalStateException.class);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("发送短信验证码根据场景选择模板")
    void sendSmsCode_success() {
        verificationCodeService.sendSmsCode("13800000000", "REGISTER");

        verify(valueOperations).set(startsWith("verif:sms:"), anyString(), eq(Duration.ofMinutes(10)));
        verify(smsService).send(eq("13800000000"), eq("T_REGISTER"), argThat(map -> map.get("code") != null));
    }

    @Test
    @DisplayName("校验邮箱验证码匹配返回真")
    void validateEmailCode() {
        when(valueOperations.get("verif:email:RESET:test@example.com")).thenReturn("123456");

        boolean result = verificationCodeService.validateEmailCode("test@example.com", "RESET", "123456");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("校验短信验证码不匹配返回假")
    void validateSmsCode() {
        when(valueOperations.get("verif:sms:REGISTER:13800000000")).thenReturn("654321");

        boolean result = verificationCodeService.validateSmsCode("13800000000", "REGISTER", "000000");

        assertThat(result).isFalse();
    }
}
