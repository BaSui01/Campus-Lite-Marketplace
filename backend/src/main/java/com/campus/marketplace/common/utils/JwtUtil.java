package com.campus.marketplace.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 工具类
 * 
 * 负责 JWT Token 的生成、解析和验证
 * 使用 HS256 算法进行签名
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 默认7天
    private Long refreshExpiration;

    /**
     * 生成访问令牌（Access Token）- 短期有效
     *
     * @param userId 用户 ID
     * @param username 用户名
     * @param roles 角色列表
     * @param permissions 权限列表
     * @return Access Token
     */
    public String generateToken(Long userId, String username, List<String> roles, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        return createToken(claims, username, expiration);
    }

    /**
     * 生成刷新令牌（Refresh Token）- 长期有效
     *
     * @param userId 用户 ID
     * @param username 用户名
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh"); // 标记为刷新令牌

        return createToken(claims, username, refreshExpiration);
    }

    /**
     * 生成临时令牌（Temp Token）- 用于 2FA 验证（5分钟有效）
     *
     * @param userId 用户 ID
     * @return Temp Token
     */
    public String generateTempToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "temp"); // 标记为临时令牌

        return createToken(claims, userId.toString(), 300000L); // 5分钟 = 300000ms
    }

    /**
     * 创建 Token（支持自定义过期时间）
     *
     * @param claims 载荷数据
     * @param subject 主题（通常是用户名）
     * @param expirationTime 过期时间（毫秒）
     * @return JWT Token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTime);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key)  // 移除过时的 SignatureAlgorithm 参数，自动使用 HS256
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中获取角色列表（类型安全）
     */
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object obj = claims.get("roles");
        if (obj instanceof List<?> list) {
            List<String> out = new java.util.ArrayList<>(list.size());
            for (Object e : list) {
                if (e != null) out.add(String.valueOf(e));
            }
            return out;
        }
        return java.util.Collections.emptyList();
    }

    /**
     * 从 Token 中获取权限列表（类型安全）
     */
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object obj = claims.get("permissions");
        if (obj instanceof List<?> list) {
            List<String> out = new java.util.ArrayList<>(list.size());
            for (Object e : list) {
                if (e != null) out.add(String.valueOf(e));
            }
            return out;
        }
        return java.util.Collections.emptyList();
    }

    /**
     * 从 Token 中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * 解析 Token 获取 Claims
     */
    private Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Token 过期验证失败", e);
            return true;
        }
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @param username 用户名
     * @return 是否有效
     */
    public Boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token 验证失败", e);
            return false;
        }
    }

    /**
     * 验证刷新令牌是否有效
     *
     * @param refreshToken Refresh Token
     * @return 是否有效
     */
    public Boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = getClaimsFromToken(refreshToken);
            String type = claims.get("type", String.class);

            // 检查是否是刷新令牌
            if (!"refresh".equals(type)) {
                log.warn("Token 类型不是 refresh");
                return false;
            }

            // 检查是否过期
            return !isTokenExpired(refreshToken);
        } catch (Exception e) {
            log.error("Refresh Token 验证失败", e);
            return false;
        }
    }

    /**
     * 刷新 Token
     * 
     * @param token 旧 Token
     * @return 新 Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            
            // 创建新的 claims map
            Map<String, Object> newClaims = new HashMap<>(claims);
            
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            
            return Jwts.builder()
                    .claims(newClaims)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(key)  // 移除过时的 SignatureAlgorithm 参数
                    .compact();
        } catch (Exception e) {
            log.error("Token 刷新失败", e);
            return null;
        }
    }
}
