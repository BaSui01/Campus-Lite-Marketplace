package com.campus.marketplace.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.mockito.Mockito.mock;

/**
 * 测试环境Redis配置 - 使用Mock替代真实Redis连接
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@TestConfiguration
@Profile("test")
public class TestRedisConfig {

    /**
     * 提供Mock的RedisConnectionFactory，避免连接真实Redis
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }
}
