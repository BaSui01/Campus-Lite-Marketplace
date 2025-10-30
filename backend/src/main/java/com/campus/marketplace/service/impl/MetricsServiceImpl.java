package com.campus.marketplace.service.impl;

import com.campus.marketplace.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 系统指标采集服务实现类
 * 
 * 功能：
 * 1. JVM 内存指标采集
 * 2. CPU 使用率统计
 * 3. 线程信息采集
 * 4. 垃圾回收统计
 * 5. 系统健康状态检测
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    /**
     * 内存使用率警告阈值（80%）
     */
    private static final double MEMORY_WARNING_THRESHOLD = 0.8;

    /**
     * CPU 使用率警告阈值（80%）
     */
    private static final double CPU_WARNING_THRESHOLD = 0.8;

    @Override
    public Map<String, Object> getJvmMemoryMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            
            // 堆内存
            long heapMemoryUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
            long heapMemoryMax = memoryMXBean.getHeapMemoryUsage().getMax();
            long heapMemoryCommitted = memoryMXBean.getHeapMemoryUsage().getCommitted();
            
            // 非堆内存
            long nonHeapMemoryUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();
            long nonHeapMemoryMax = memoryMXBean.getNonHeapMemoryUsage().getMax();
            
            // 计算使用率
            double heapMemoryUsagePercent = heapMemoryMax > 0 
                    ? (double) heapMemoryUsed / heapMemoryMax : 0.0;
            
            metrics.put("heapMemoryUsed", heapMemoryUsed);
            metrics.put("heapMemoryMax", heapMemoryMax);
            metrics.put("heapMemoryCommitted", heapMemoryCommitted);
            metrics.put("nonHeapMemoryUsed", nonHeapMemoryUsed);
            metrics.put("nonHeapMemoryMax", nonHeapMemoryMax);
            metrics.put("heapMemoryUsagePercent", heapMemoryUsagePercent);
            
            log.debug("✅ JVM 内存指标采集成功: heapUsed={}MB, heapMax={}MB, usage={}%", 
                    heapMemoryUsed / 1024 / 1024, 
                    heapMemoryMax / 1024 / 1024, 
                    String.format("%.2f", heapMemoryUsagePercent * 100));
        } catch (Exception e) {
            log.error("❌ JVM 内存指标采集失败: {}", e.getMessage());
        }
        
        return metrics;
    }

    @Override
    public Map<String, Object> getCpuMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();

            double processCpuLoad = -1.0;
            double systemCpuLoad = -1.0;
            int availableProcessors = osMXBean.getAvailableProcessors();

            // 优先通过 com.sun.management.OperatingSystemMXBean 获取（反射，避免编译期依赖）
            boolean loadedViaSunMxBean = false;
            try {
                Class<?> sunOsMxBeanClass = Class.forName("com.sun.management.OperatingSystemMXBean");
                Object sunOsMxBean = ManagementFactory.class
                        .getMethod("getPlatformMXBean", Class.class)
                        .invoke(null, sunOsMxBeanClass);
                if (sunOsMxBean != null) {
                    Object v1 = sunOsMxBeanClass.getMethod("getProcessCpuLoad").invoke(sunOsMxBean);
                    Object v2 = sunOsMxBeanClass.getMethod("getSystemCpuLoad").invoke(sunOsMxBean);
                    if (v1 instanceof Number n1) processCpuLoad = n1.doubleValue();
                    if (v2 instanceof Number n2) systemCpuLoad = n2.doubleValue();
                    loadedViaSunMxBean = true;
                }
            } catch (Throwable ignored) { }

            // 回退：直接在实现类上尝试
            if (!loadedViaSunMxBean) {
                try {
                    var m1 = osMXBean.getClass().getMethod("getProcessCpuLoad");
                    Object v1 = m1.invoke(osMXBean);
                    if (v1 instanceof Number n) processCpuLoad = n.doubleValue();
                } catch (Throwable ignored2) { }

                try {
                    var m2 = osMXBean.getClass().getMethod("getSystemCpuLoad");
                    Object v2 = m2.invoke(osMXBean);
                    if (v2 instanceof Number n) systemCpuLoad = n.doubleValue();
                } catch (Throwable ignored3) { }
            }
            
            metrics.put("processCpuLoad", processCpuLoad);
            metrics.put("systemCpuLoad", systemCpuLoad);
            metrics.put("availableProcessors", availableProcessors);
            
            log.debug("✅ CPU 指标采集成功: processCpu={}%, systemCpu={}%, processors={}", 
                    String.format("%.2f", processCpuLoad * 100),
                    String.format("%.2f", systemCpuLoad * 100),
                    availableProcessors);
        } catch (Exception e) {
            log.error("❌ CPU 指标采集失败: {}", e.getMessage());
        }
        
        return metrics;
    }

    @Override
    public Map<String, Object> getThreadMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            
            int threadCount = threadMXBean.getThreadCount();
            int peakThreadCount = threadMXBean.getPeakThreadCount();
            int daemonThreadCount = threadMXBean.getDaemonThreadCount();
            long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();
            
            metrics.put("threadCount", threadCount);
            metrics.put("peakThreadCount", peakThreadCount);
            metrics.put("daemonThreadCount", daemonThreadCount);
            metrics.put("totalStartedThreadCount", totalStartedThreadCount);
            
            log.debug("✅ 线程指标采集成功: threadCount={}, peakThreadCount={}, daemonThreadCount={}", 
                    threadCount, peakThreadCount, daemonThreadCount);
        } catch (Exception e) {
            log.error("❌ 线程指标采集失败: {}", e.getMessage());
        }
        
        return metrics;
    }

    @Override
    public Map<String, Object> getGarbageCollectionMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
            
            long totalGcCollectionCount = 0;
            long totalGcCollectionTime = 0;
            
            for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
                totalGcCollectionCount += gcMXBean.getCollectionCount();
                totalGcCollectionTime += gcMXBean.getCollectionTime();
            }
            
            metrics.put("gcCollectionCount", totalGcCollectionCount);
            metrics.put("gcCollectionTime", totalGcCollectionTime);
            
            log.debug("✅ GC 指标采集成功: gcCount={}, gcTime={}ms", 
                    totalGcCollectionCount, totalGcCollectionTime);
        } catch (Exception e) {
            log.error("❌ GC 指标采集失败: {}", e.getMessage());
        }
        
        return metrics;
    }

    @Override
    public Map<String, Object> getJvmRuntimeMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            
            String jvmVersion = System.getProperty("java.version");
            String jvmVendor = System.getProperty("java.vendor");
            long uptimeMillis = runtimeMXBean.getUptime();
            long startTime = runtimeMXBean.getStartTime();
            
            // 格式化运行时长
            String uptimeFormatted = formatUptime(uptimeMillis);
            
            metrics.put("jvmVersion", jvmVersion);
            metrics.put("jvmVendor", jvmVendor);
            metrics.put("uptimeMillis", uptimeMillis);
            metrics.put("startTime", startTime);
            metrics.put("uptimeFormatted", uptimeFormatted);
            
            log.debug("✅ JVM 运行时指标采集成功: jvmVersion={}, uptime={}", jvmVersion, uptimeFormatted);
        } catch (Exception e) {
            log.error("❌ JVM 运行时指标采集失败: {}", e.getMessage());
        }
        
        return metrics;
    }

    @Override
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String osArch = System.getProperty("os.arch");
            
            metrics.put("osName", osName);
            metrics.put("osVersion", osVersion);
            metrics.put("osArch", osArch);
            
            log.debug("✅ 系统指标采集成功: osName={}, osVersion={}, osArch={}", osName, osVersion, osArch);
        } catch (Exception e) {
            log.error("❌ 系统指标采集失败: {}", e.getMessage());
        }
        
        return metrics;
    }

    @Override
    public Map<String, Object> getAllMetrics() {
        Map<String, Object> allMetrics = new HashMap<>();
        
        allMetrics.put("jvm", getJvmMemoryMetrics());
        allMetrics.put("cpu", getCpuMetrics());
        allMetrics.put("threads", getThreadMetrics());
        allMetrics.put("gc", getGarbageCollectionMetrics());
        allMetrics.put("runtime", getJvmRuntimeMetrics());
        allMetrics.put("system", getSystemMetrics());
        
        log.debug("✅ 全部系统指标采集成功");
        return allMetrics;
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        Map<String, Object> details = new HashMap<>();
        
        try {
            // 获取内存指标
            Map<String, Object> memoryMetrics = getJvmMemoryMetrics();
            double memoryUsagePercent = (double) memoryMetrics.get("heapMemoryUsagePercent");
            
            // 获取 CPU 指标
            Map<String, Object> cpuMetrics = getCpuMetrics();
            double processCpuLoad = (double) cpuMetrics.get("processCpuLoad");
            
            // 判断健康状态
            String status = "UP";
            if (memoryUsagePercent > MEMORY_WARNING_THRESHOLD || processCpuLoad > CPU_WARNING_THRESHOLD) {
                status = "WARNING";
            }
            
            details.put("memoryUsagePercent", memoryUsagePercent);
            details.put("processCpuLoad", processCpuLoad);
            
            health.put("status", status);
            health.put("details", details);
            
            log.debug("✅ 健康状态检测完成: status={}, memoryUsage={}%, cpuLoad={}%", 
                    status, 
                    String.format("%.2f", memoryUsagePercent * 100),
                    String.format("%.2f", processCpuLoad * 100));
        } catch (Exception e) {
            log.error("❌ 健康状态检测失败: {}", e.getMessage());
            health.put("status", "DOWN");
            health.put("details", details);
        }
        
        return health;
    }

    /**
     * 格式化运行时长
     */
    private String formatUptime(long uptimeMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(uptimeMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMillis) % 60;
        
        return String.format("%d天 %d小时 %d分钟 %d秒", days, hours, minutes, seconds);
    }
}

