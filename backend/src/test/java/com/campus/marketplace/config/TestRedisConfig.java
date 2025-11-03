package com.campus.marketplace.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.mock;

/**
 * 测试环境Redis配置
 * 提供Mock的Redis相关Bean，避免测试时连接真实Redis
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@TestConfiguration
public class TestRedisConfig {

    /**
     * 提供Mock的RedissonClient
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public RedissonClient redissonClient() {
        return mock(RedissonClient.class);
    }

    /**
     * 提供Mock的RedisConnectionFactory
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }

    /**
     * 提供Mock的StringRedisTemplate
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate() {
        return mock(StringRedisTemplate.class);
    }

    /**
     * 提供Mock的RedisTemplate
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> redisTemplate() {
        return mock(RedisTemplate.class);
    }
}
