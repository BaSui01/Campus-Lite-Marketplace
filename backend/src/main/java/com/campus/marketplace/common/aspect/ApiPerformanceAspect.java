package com.campus.marketplace.common.aspect;

import com.campus.marketplace.common.entity.ApiPerformanceLog;
import com.campus.marketplace.repository.ApiPerformanceLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * API性能监控切面（持久化版本）
 * 
 * 功能：
 * 1. 记录所有API请求的性能数据
 * 2. 持久化到数据库
 * 3. 异步保存，不影响主线程性能
 * 4. 记录慢查询（> 1000ms）
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiPerformanceAspect {

    private final ApiPerformanceLogRepository apiPerformanceLogRepository;

    /**
     * 慢查询阈值（毫秒）
     */
    private static final long SLOW_API_THRESHOLD = 1000L;

    /**
     * 监控所有Controller方法
     */
    @Around("execution(* com.campus.marketplace.controller..*.*(..))")
    public Object monitorApiPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = getHttpServletRequest();
        
        Object result = null;
        Throwable exception = null;
        Integer httpStatus = 200;
        
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            httpStatus = 500;
            throw e;
        } finally {
            // 计算执行时间
            long durationMs = System.currentTimeMillis() - startTime;
            boolean isSlow = durationMs > SLOW_API_THRESHOLD;
            boolean isSuccess = exception == null;

            // 异步保存性能日志
            if (request != null) {
                savePerformanceLog(request, joinPoint, durationMs, httpStatus, isSuccess, isSlow, exception);
            }

            // 记录慢查询
            if (isSlow) {
                log.warn("⚠️ 慢API检测: 端点={} {}, 耗时={}ms", 
                    request != null ? request.getMethod() : "UNKNOWN",
                    request != null ? request.getRequestURI() : joinPoint.getSignature().toString(),
                    durationMs);
            }
        }
    }

    /**
     * 异步保存性能日志
     */
    @Async
    protected void savePerformanceLog(
        HttpServletRequest request,
        ProceedingJoinPoint joinPoint,
        long durationMs,
        Integer httpStatus,
        boolean isSuccess,
        boolean isSlow,
        Throwable exception
    ) {
        try {
            ApiPerformanceLog log = ApiPerformanceLog.builder()
                .httpMethod(request.getMethod())
                .endpoint(request.getRequestURI())
                .requestTime(LocalDateTime.now())
                .durationMs(durationMs)
                .httpStatus(httpStatus)
                .isSuccess(isSuccess)
                .isSlow(isSlow)
                .clientIp(getClientIp(request))
                .requestParams(extractRequestParams(request))
                .errorMessage(exception != null ? exception.getMessage() : null)
                .userAgent(request.getHeader("User-Agent"))
                .build();

            apiPerformanceLogRepository.save(log);
        } catch (Exception e) {
            // 保存失败不影响主流程
            log.error("❌ 保存API性能日志失败", e);
        }
    }

    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 提取请求参数
     */
    private Map<String, Object> extractRequestParams(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (value.length == 1) {
                params.put(key, value[0]);
            } else {
                params.put(key, value);
            }
        });
        return params;
    }
}
