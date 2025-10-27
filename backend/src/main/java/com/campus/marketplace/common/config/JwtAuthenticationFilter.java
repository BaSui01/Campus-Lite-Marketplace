package com.campus.marketplace.common.config;

import com.campus.marketplace.common.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/**
 * JWT 认证过滤器
 * 
 * 拦截所有请求，从请求头中提取 JWT Token 并验证
 * 验证通过后将用户信息存入 SecurityContext
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_NAME = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求头中获取 Token
            String token = extractToken(request);
            
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 验证 Token 是否在 Redis 中（检查是否已登出）
                String redisKey = "token:" + token;
                Boolean hasKey = redisTemplate.hasKey(redisKey);
                
                if (Boolean.TRUE.equals(hasKey)) {
                    // 从 Token 中提取用户信息
                    String username = jwtUtil.getUsernameFromToken(token);
                    
                    // 验证 Token 有效性
                    if (jwtUtil.validateToken(token, username)) {
                        // 获取角色和权限
                        List<String> roles = jwtUtil.getRolesFromToken(token);
                        List<String> permissions = jwtUtil.getPermissionsFromToken(token);
                        
                        // 合并角色和权限作为 authorities
                        List<SimpleGrantedAuthority> authorities = Stream.concat(
                                roles.stream(),
                                permissions.stream()
                        ).map(SimpleGrantedAuthority::new).toList();
                        
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 将认证信息存入 SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("用户 {} 认证成功", username);
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT 认证失败", e);
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_NAME);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        
        return null;
    }
}
