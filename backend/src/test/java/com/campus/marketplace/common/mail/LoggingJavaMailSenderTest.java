package com.campus.marketplace.common.mail;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessagePreparator;

import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("LoggingJavaMailSender 测试")
class LoggingJavaMailSenderTest {

    private final LoggingJavaMailSender mailSender = new LoggingJavaMailSender();

    @Test
    @DisplayName("发送简单邮件不会抛出异常")
    void sendSimpleMessage_shouldNotThrow() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("user@example.com");
        message.setSubject("sub");
        message.setText("body");

        assertThatCode(() -> mailSender.send(message)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("MimeMessage 和预处理器发送流程正常")
    void sendMimeMessage_shouldNotThrow() {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        assertThatCode(() -> mailSender.send(mimeMessage)).doesNotThrowAnyException();

        MimeMessagePreparator preparator = mm -> mm.setSubject("hello");
        assertThatCode(() -> mailSender.send(preparator)).doesNotThrowAnyException();
    }
}
