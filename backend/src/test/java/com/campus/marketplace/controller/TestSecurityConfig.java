package com.campus.marketplace.controller;

import com.campus.marketplace.common.support.SpringContextHolder;
import com.campus.marketplace.common.utils.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test Security Configuration
 *
 * Simplified security config for controller tests
 * Mock JwtUtil Bean to avoid ApplicationContext loading failure
 *
 * @author BaSui 😎 - 测试环境要Mock掉依赖,避免加载失败!
 * @since 2025-11-03
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {

    /**
     * Mock JwtUtil Bean
     *
     * 🔧 解决问题：JwtAuthenticationFilter 需要 JwtUtil Bean
     * 在测试环境中，我们不需要真正的 JWT 验证逻辑
     * 使用 @Bean + Mockito.mock() 创建一个 Mock 对象
     */
    @Bean
    public JwtUtil jwtUtil() {
        return org.mockito.Mockito.mock(JwtUtil.class);
    }

    /**
     * Mock RedisTemplate Bean
     *
     * 🔧 解决问题：JwtAuthenticationFilter 的构造函数还需要 RedisTemplate
     * 在测试环境中，我们不需要真正的 Redis 连接
     * 使用 @Bean + Mockito.mock() 创建一个 Mock 对象
     */
    @Bean
    @SuppressWarnings("rawtypes")
    public org.springframework.data.redis.core.RedisTemplate redisTemplate() {
        return org.mockito.Mockito.mock(org.springframework.data.redis.core.RedisTemplate.class);
    }

    /**
     * Mock StringRedisTemplate Bean
     *
     * 🔧 解决问题：RateLimitAspect 需要 StringRedisTemplate
     * 在测试环境中，我们不需要真正的 Redis 连接
     * 使用 @Bean + Mockito.mock() 创建一个 Mock 对象
     */
    @Bean
    public org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate() {
        return org.mockito.Mockito.mock(org.springframework.data.redis.core.StringRedisTemplate.class);
    }

    /**
     * SpringContextHolder Bean
     *
     * 🔧 解决问题：SecurityUtil.getCurrentUserId() 内部调用 SpringContextHolder.getBean()
     * 在 @WebMvcTest 环境下，SpringContextHolder 不会自动扫描
     * 需要手动注册这个 Bean，让 ApplicationContext 能够被注入
     */
    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

    /**
     * Security Filter Chain (测试专用配置，优先级高于生产配置)
     *
     * 🔧 解决问题：避免与 SecurityConfig.filterChain 冲突
     * 使用 @Primary 注解，让测试配置优先生效
     * 测试环境下，所有请求都允许访问（permitAll）
     */
    @Bean
    @Primary  // 💡 关键：测试配置优先！
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
