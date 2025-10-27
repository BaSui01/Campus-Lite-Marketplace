package com.campus.marketplace.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 慢查询监控切面
 * 
 * 功能：
 * 1. 监控 Repository 层方法执行时间
 * 2. 记录慢查询日志（> 500ms）
 * 3. 统计慢查询次数和平均耗时
 * 4. 提供慢查询详情查询
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Aspect
@Component
public class SlowQueryMonitorAspect {

    /**
     * 慢查询阈值（毫秒）
     */
    private static final long SLOW_QUERY_THRESHOLD = 500L;

    /**
     * 最大慢查询记录数（防止内存溢出）
     */
    private static final int MAX_SLOW_QUERY_RECORDS = 100;

    /**
     * 慢查询统计数据
     */
    private final AtomicLong totalSlowQueries = new AtomicLong(0);
    private final AtomicLong totalSlowQueryTime = new AtomicLong(0);

    /**
     * 慢查询详情列表（最近的 100 条）
     */
    private final List<SlowQueryRecord> slowQueryRecords = new CopyOnWriteArrayList<>();

    /**
     * 监控 Repository 层所有方法
     */
    @Around("execution(* com.campus.marketplace.repository.*.*(..))")
    public Object monitorSlowQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toString();
        Object[] args = joinPoint.getArgs();
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
            
            // 记录慢查询
            if (executionTime > SLOW_QUERY_THRESHOLD) {
                recordSlowQuery(methodName, args, executionTime, hasException);
                
                log.warn("⚠️ 慢查询检测: 方法={}, 参数={}, 耗时={}ms, 异常={}", 
                        methodName, 
                        Arrays.toString(args), 
                        executionTime, 
                        hasException);
            }
        }
    }

    /**
     * 记录慢查询
     */
    private void recordSlowQuery(String methodName, Object[] args, long executionTime, boolean hasException) {
        totalSlowQueries.incrementAndGet();
        totalSlowQueryTime.addAndGet(executionTime);
        
        // 创建慢查询记录
        SlowQueryRecord record = new SlowQueryRecord(
                methodName,
                Arrays.toString(args),
                executionTime,
                hasException,
                System.currentTimeMillis()
        );
        
        // 添加到记录列表（保持最近 100 条）
        slowQueryRecords.add(record);
        if (slowQueryRecords.size() > MAX_SLOW_QUERY_RECORDS) {
            slowQueryRecords.remove(0); // 移除最老的记录
        }
    }

    /**
     * 获取慢查询统计信息
     */
    public Map<String, Object> getSlowQueryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalQueries = totalSlowQueries.get();
        long totalTime = totalSlowQueryTime.get();
        long averageTime = totalQueries > 0 ? totalTime / totalQueries : 0;
        
        stats.put("totalSlowQueries", totalQueries);
        stats.put("totalSlowQueryTime", totalTime);
        stats.put("averageSlowQueryTime", averageTime);
        stats.put("slowQueryThreshold", SLOW_QUERY_THRESHOLD);
        stats.put("recentSlowQueries", slowQueryRecords.size());
        
        return stats;
    }

    /**
     * 获取慢查询详情列表
     */
    public List<SlowQueryRecord> getSlowQueryRecords() {
        return new ArrayList<>(slowQueryRecords);
    }

    /**
     * 清除统计数据
     */
    public void clearStatistics() {
        totalSlowQueries.set(0);
        totalSlowQueryTime.set(0);
        slowQueryRecords.clear();
        log.info("✅ 慢查询统计数据已清除");
    }

    /**
     * 慢查询记录（内部类）
     */
    public static class SlowQueryRecord {
        private final String methodName;
        private final String parameters;
        private final long executionTime;
        private final boolean hasException;
        private final long timestamp;

        public SlowQueryRecord(String methodName, String parameters, long executionTime, 
                             boolean hasException, long timestamp) {
            this.methodName = methodName;
            this.parameters = parameters;
            this.executionTime = executionTime;
            this.hasException = hasException;
            this.timestamp = timestamp;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getParameters() {
            return parameters;
        }

        public long getExecutionTime() {
            return executionTime;
        }

        public boolean isHasException() {
            return hasException;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format("SlowQueryRecord{method='%s', params=%s, time=%dms, exception=%s, timestamp=%d}",
                    methodName, parameters, executionTime, hasException, timestamp);
        }
    }
}

