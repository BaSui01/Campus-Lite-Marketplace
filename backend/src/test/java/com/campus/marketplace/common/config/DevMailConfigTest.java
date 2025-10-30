package com.campus.marketplace.common.config;

import com.campus.marketplace.common.mail.LoggingJavaMailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DevMailConfig 测试")
class DevMailConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(DevMailConfig.class)
            .withInitializer(ctx -> ctx.getEnvironment().setActiveProfiles("dev"));

    @Test
    @DisplayName("开启 mail.mock.enabled 时注册 LoggingJavaMailSender")
    void javaMailSender_enabled() {
        contextRunner.withPropertyValues("mail.mock.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(JavaMailSender.class);
                    assertThat(context.getBean(JavaMailSender.class)).isInstanceOf(LoggingJavaMailSender.class);
                });
    }

    @Test
    @DisplayName("未开启开关时不注册邮件发送器")
    void javaMailSender_disabled() {
        contextRunner.withPropertyValues("mail.mock.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(JavaMailSender.class));
    }
}
