package com.campus.marketplace.common.config;

import com.campus.marketplace.common.security.PermissionCodes;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Spring Security 配置类
 *
 * 配置基于 JWT 的无状态认证机制
 * 配置 CORS、CSRF 防护和权限控制
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final ServerProperties serverProperties;

    /**
     * 配置 Security 过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（使用 JWT 不需要 CSRF 保护）
                .csrf(AbstractHttpConfigurer::disable)
                
                // 配置 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // 配置会话管理（无状态）
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 公开接口（不需要认证）
                        .requestMatchers(matchersWithContext("/api/auth/**")).permitAll()
                        .requestMatchers(matchersWithContext("/actuator/health")).permitAll()
                        
                        // 公共查询接口
                        .requestMatchers(HttpMethod.GET, matchersWithContext("/api/search", "/api/search/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, matchersWithContext("/api/recommend/hot")).permitAll()
                        .requestMatchers(HttpMethod.GET, matchersWithContext("/api/replies/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, matchersWithContext("/api/users/**")).permitAll()
                        
                        // 支付回调
                        .requestMatchers(HttpMethod.POST, matchersWithContext("/api/payment/wechat/notify")).permitAll()
                        .requestMatchers(HttpMethod.POST, matchersWithContext("/api/payment/alipay/refund/notify")).permitAll()
                        
                        // Swagger UI 和 API 文档
                        .requestMatchers(matchersWithContext("/swagger-ui.html")).permitAll()
                        .requestMatchers(matchersWithContext("/swagger-ui/**")).permitAll()
                        .requestMatchers(matchersWithContext("/v3/api-docs/**")).permitAll()

                        // 管理后台必须具备明确权限
                        .requestMatchers(matchersWithContext("/api/admin/**"))
                        .hasAuthority(PermissionCodes.SYSTEM_STATISTICS_VIEW)
                        
                        // 物品查询接口（公开）
                        .requestMatchers(HttpMethod.GET, matchersWithContext("/api/goods/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, matchersWithContext("/api/categories/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, matchersWithContext("/api/tags/**")).permitAll()
                        
                        // 帖子查询接口（公开）
                        .requestMatchers(HttpMethod.GET, matchersWithContext("/api/posts/**")).permitAll()
                        
                        // WebSocket 连接
                        .requestMatchers(matchersWithContext("/ws/**")).permitAll()
                        
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                
                // 配置异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                
                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * 配置 CORS（跨域资源共享）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源（开发环境允许所有，生产环境需要配置具体域名）
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // 允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(List.of("*"));
        
        // 允许携带凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求的有效期（1 小时）
        configuration.setMaxAge(3600L);
        
        // 暴露的响应头
        configuration.setExposedHeaders(List.of("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * 配置密码加密器（BCrypt）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 根据上下文路径动态生成匹配路径，兼容不同环境下的 context-path 设置。
     */
    private String[] matchersWithContext(String... paths) {
        String contextPath = Optional.ofNullable(serverProperties.getServlet().getContextPath())
                .filter(path -> !path.isBlank() && !"/".equals(path))
                .map(this::normalizeContextPath)
                .orElse("");

        if (contextPath.isEmpty()) {
            return paths;
        }

        return Stream.concat(
                Arrays.stream(paths),
                Arrays.stream(paths).map(path -> contextPath + path)
        ).distinct().toArray(String[]::new);
    }

    private String normalizeContextPath(String path) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        return normalized.endsWith("/") && normalized.length() > 1
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }
}
