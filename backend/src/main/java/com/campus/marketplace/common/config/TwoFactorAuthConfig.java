package com.campus.marketplace.common.config;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 双因素认证配置类
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Configuration
public class TwoFactorAuthConfig {

    /**
     * 配置 Google Authenticator
     *
     * @return GoogleAuthenticator 实例
     */
    @Bean
    public GoogleAuthenticator googleAuthenticator() {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30)) // 时间步长 30 秒
                .setWindowSize(3) // 允许前后 3 个时间窗口（容错）
                .setCodeDigits(6) // 6 位数字验证码
                .build();

        return new GoogleAuthenticator(config);
    }
}
