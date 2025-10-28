package com.campus.marketplace.common.config;

import com.campus.marketplace.common.mail.LoggingJavaMailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 可选的开发环境“日志邮件发送器”。仅在明确开启 mail.mock.enabled=true 时生效。
 */
@Configuration
@Profile("dev")
@ConditionalOnProperty(name = "mail.mock.enabled", havingValue = "true")
public class DevMailConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender() {
        return new LoggingJavaMailSender();
    }
}
