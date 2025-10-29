package com.campus.marketplace.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控切面
 * 
 * 功能：
 * 1. 记录方法执行时间
 * 2. 记录慢查询（> 1000ms）
 * 3. 统计方法调用次数（QPS）
 * 4. 异常也记录性能数据
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Aspect
@Component
public class PerformanceMonitorAspect {

    /**
     * 慢查询阈值（毫秒）
     */
    private static final long SLOW_QUERY_THRESHOLD = 1000L;

    /**
     * 性能统计数据（方法名 -> 统计信息）
     */
    private final Map<String, MethodStats> performanceStats = new ConcurrentHashMap<>();

    /**
     * 监控 Service 层所有方法性能
     */
    @Around("execution(* com.campus.marketplace.service.impl.*.*(..))")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toString();
        long startTime = System.currentTimeMillis();
        
        Object result = null;
        boolean hasException = false;
        
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            hasException = true;
            throw e;
        } finally {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            boolean isSlow = executionTime > SLOW_QUERY_THRESHOLD;

            // 记录性能数据
            recordPerformance(methodName, executionTime, hasException, isSlow);
            
            // 记录慢查询
            if (isSlow) {
                log.warn("⚠️ 慢查询检测: 方法={}, 耗时={}ms", methodName, executionTime);
            }
            
            // 记录正常日志
            if (!hasException) {
                log.debug("✅ 方法执行: 方法={}, 耗时={}ms", methodName, executionTime);
            } else {
                log.error("❌ 方法异常: 方法={}, 耗时={}ms", methodName, executionTime);
            }
        }
    }

    /**
     * 记录性能数据
     */
    private void recordPerformance(String methodName, long executionTime, boolean hasException, boolean isSlow) {
        performanceStats.computeIfAbsent(methodName, k -> new MethodStats())
                .record(executionTime, hasException, isSlow);
    }

    /**
     * 获取性能统计数据
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> result = new ConcurrentHashMap<>();
        
        performanceStats.forEach((methodName, stats) -> {
            Map<String, Object> statMap = new ConcurrentHashMap<>();
            statMap.put("totalCalls", stats.totalCalls.get());
            statMap.put("totalErrors", stats.totalErrors.get());
            statMap.put("totalExecutionTime", stats.totalExecutionTime.get());
            statMap.put("avgExecutionTime", 
                    stats.totalCalls.get() > 0 ? stats.totalExecutionTime.get() / stats.totalCalls.get() : 0);
            statMap.put("maxExecutionTime", stats.maxExecutionTime.get());
            statMap.put("slowCount", stats.slowCount.get());
            result.put(methodName, statMap);
        });
        
        return result;
    }

    /**
     * 方法统计信息（内部类）
     */
    private static class MethodStats {
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong totalErrors = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);

        public void record(long executionTime, boolean hasException, boolean isSlow) {
            totalCalls.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
            maxExecutionTime.accumulateAndGet(executionTime, Math::max);
            if (hasException) {
                totalErrors.incrementAndGet();
            }
            if (isSlow) {
                slowCount.incrementAndGet();
            }
        }

        private final AtomicLong maxExecutionTime = new AtomicLong(0);
        private final AtomicLong slowCount = new AtomicLong(0);
    }
}
