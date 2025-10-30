package com.campus.marketplace.common.web;

import com.campus.marketplace.common.utils.SecurityUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 请求 Trace 与 MDC 透传过滤器
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Component
public class TraceMdcFilter extends OncePerRequestFilter {

    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader(HEADER_TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        String userId = null;
        try {
            if (SecurityUtil.isAuthenticated()) {
                // 避免抛异常影响主流程
                userId = String.valueOf(SecurityUtil.getCurrentUsername());
            }
        } catch (Exception ignored) { }

        try {
            MDC.put("traceId", traceId);
            if (userId != null) {
                MDC.put("user", userId);
            }
            MDC.put("method", request.getMethod());
            MDC.put("path", request.getRequestURI());

            response.setHeader(HEADER_TRACE_ID, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
            MDC.remove("user");
            MDC.remove("method");
            MDC.remove("path");
        }
    }
}
