package com.campus.marketplace.controller;

import com.campus.marketplace.common.support.SpringContextHolder;
import com.campus.marketplace.common.utils.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
