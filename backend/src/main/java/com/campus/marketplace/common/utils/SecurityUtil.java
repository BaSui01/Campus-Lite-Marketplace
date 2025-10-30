package com.campus.marketplace.common.utils;

import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.support.SpringContextHolder;
import com.campus.marketplace.repository.UserRepository;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Security 工具类
 *
 * 提供从 Spring Security 上下文中获取当前用户信息的便捷方法
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@UtilityClass
public class SecurityUtil {

    /**
     * 获取当前登录用户的用户名
     *
     * 🔐 从 SecurityContextHolder 中获取认证信息
     * ⚠️ 如果未登录则抛出异常
     *
     * @return 用户名
     * @throws BusinessException 用户未登录
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("尝试获取当前用户名，但用户未登录");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        if (principal instanceof String username) {
            return username;
        }

        log.error("无法识别的 Principal 类型：{}", principal.getClass());
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "无法获取用户信息");
    }

    /**
     * 获取当前登录用户的用户ID
     *
     * ⚠️ 注意：这个方法依赖于自定义的 UserDetails 实现
     * 如果使用标准的 UserDetails，需要在 UserDetails 实现类中添加 userId 字段
     *
     * @return 用户ID
     * @throws BusinessException 用户未登录或无法获取用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            log.warn("尝试获取当前用户ID，但用户未登录");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return resolveUserIdByUsername(userDetails.getUsername());
        }

        if (principal instanceof String username) {
            return resolveUserIdByUsername(username);
        }

        if (principal instanceof Number number) {
            return number.longValue();
        }

        log.error("无法识别的 Principal 类型：{}", principal.getClass());
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "无法直接获取用户ID，请使用 UserRepository 查询");
    }

    /**
     * 获取当前认证对象
     *
     * @return Authentication 认证对象，可能为 null
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return true-已登录，false-未登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 检查当前用户是否拥有指定角色
     *
     * @param role 角色名（不需要 ROLE_ 前缀）
     * @return true-拥有，false-不拥有
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String roleWithPrefix = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(roleWithPrefix));
    }

    /**
     * 检查当前用户是否拥有指定权限（Authority）
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    private static Long resolveUserIdByUsername(String username) {
        if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户未登录");
        }
        try {
            UserRepository userRepository = SpringContextHolder.getBean(UserRepository.class);
            return userRepository.findByUsername(username)
                    .map(User::getId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("解析用户ID失败, username={}", username, ex);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取用户ID失败");
        }
    }
}
