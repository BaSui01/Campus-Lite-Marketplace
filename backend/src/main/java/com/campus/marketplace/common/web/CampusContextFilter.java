package com.campus.marketplace.common.web;

import com.campus.marketplace.common.context.CampusContextHolder;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 每次请求解析并注入当前用户的 campusId 到 ThreadLocal 与 MDC
 */
@Slf4j
@Component
@ConditionalOnBean(name = "entityManagerFactory")
@RequiredArgsConstructor
public class CampusContextFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Long campusId = null;
            if (SecurityUtil.isAuthenticated()) {
                try {
                    String username = SecurityUtil.getCurrentUsername();
                    User user = userRepository.findByUsername(username).orElse(null);
                    campusId = user != null ? user.getCampusId() : null;
                } catch (Exception e) {
                    log.debug("解析校园信息失败（可忽略未登录场景）：{}", e.getMessage());
                }
            }
            if (campusId != null) {
                CampusContextHolder.setCampusId(campusId);
                MDC.put("campusId", String.valueOf(campusId));
            }
            filterChain.doFilter(request, response);
        } finally {
            CampusContextHolder.clear();
            MDC.remove("campusId");
        }
    }
}
