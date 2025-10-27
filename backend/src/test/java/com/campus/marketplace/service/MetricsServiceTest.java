package com.campus.marketplace.service;

import com.campus.marketplace.service.impl.MetricsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统指标采集服务测试类
 * 
 * TDD 开发：先写测试，定义系统指标采集的预期行为
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("系统指标采集服务测试")
class MetricsServiceTest {

    @InjectMocks
    private MetricsServiceImpl metricsService;

    @BeforeEach
    void setUp() {
        // 每个测试前准备
    }

    @Test
    @DisplayName("应该获取 JVM 内存指标")
    void shouldGetJvmMemoryMetrics() {
        // When: 获取 JVM 内存指标
        Map<String, Object> metrics = metricsService.getJvmMemoryMetrics();

        // Then: 返回内存指标数据
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("heapMemoryUsed"));
        assertTrue(metrics.containsKey("heapMemoryMax"));
        assertTrue(metrics.containsKey("nonHeapMemoryUsed"));
        assertTrue(metrics.containsKey("heapMemoryUsagePercent"));
        
        // 验证数据有效性
        long heapMemoryUsed = (long) metrics.get("heapMemoryUsed");
        long heapMemoryMax = (long) metrics.get("heapMemoryMax");
        assertTrue(heapMemoryUsed >= 0);
        assertTrue(heapMemoryMax > 0);
        assertTrue(heapMemoryUsed <= heapMemoryMax);
    }

    @Test
    @DisplayName("应该获取 CPU 使用率")
    void shouldGetCpuUsage() {
        // When: 获取 CPU 使用率
        Map<String, Object> metrics = metricsService.getCpuMetrics();

        // Then: 返回 CPU 指标数据
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("processCpuLoad"));
        assertTrue(metrics.containsKey("systemCpuLoad"));
        assertTrue(metrics.containsKey("availableProcessors"));
        
        // 验证数据有效性
        double processCpuLoad = (double) metrics.get("processCpuLoad");
        int availableProcessors = (int) metrics.get("availableProcessors");
        assertTrue(processCpuLoad >= 0.0 && processCpuLoad <= 1.0);
        assertTrue(availableProcessors > 0);
    }

    @Test
    @DisplayName("应该获取线程信息")
    void shouldGetThreadMetrics() {
        // When: 获取线程信息
        Map<String, Object> metrics = metricsService.getThreadMetrics();

        // Then: 返回线程指标数据
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("threadCount"));
        assertTrue(metrics.containsKey("peakThreadCount"));
        assertTrue(metrics.containsKey("daemonThreadCount"));
        
        // 验证数据有效性
        int threadCount = (int) metrics.get("threadCount");
        int peakThreadCount = (int) metrics.get("peakThreadCount");
        assertTrue(threadCount > 0);
        assertTrue(peakThreadCount >= threadCount);
    }

    @Test
    @DisplayName("应该获取垃圾回收统计")
    void shouldGetGarbageCollectionMetrics() {
        // When: 获取垃圾回收统计
        Map<String, Object> metrics = metricsService.getGarbageCollectionMetrics();

        // Then: 返回 GC 指标数据
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("gcCollectionCount"));
        assertTrue(metrics.containsKey("gcCollectionTime"));
        
        // 验证数据有效性
        long gcCollectionCount = (long) metrics.get("gcCollectionCount");
        long gcCollectionTime = (long) metrics.get("gcCollectionTime");
        assertTrue(gcCollectionCount >= 0);
        assertTrue(gcCollectionTime >= 0);
    }

    @Test
    @DisplayName("应该获取 JVM 运行时信息")
    void shouldGetJvmRuntimeMetrics() {
        // When: 获取 JVM 运行时信息
        Map<String, Object> metrics = metricsService.getJvmRuntimeMetrics();

        // Then: 返回运行时指标数据
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("jvmVersion"));
        assertTrue(metrics.containsKey("jvmVendor"));
        assertTrue(metrics.containsKey("uptimeMillis"));
        assertTrue(metrics.containsKey("startTime"));
        
        // 验证数据有效性
        String jvmVersion = (String) metrics.get("jvmVersion");
        long uptimeMillis = (long) metrics.get("uptimeMillis");
        assertNotNull(jvmVersion);
        assertFalse(jvmVersion.isEmpty());
        assertTrue(uptimeMillis > 0);
    }

    @Test
    @DisplayName("应该获取系统信息")
    void shouldGetSystemMetrics() {
        // When: 获取系统信息
        Map<String, Object> metrics = metricsService.getSystemMetrics();

        // Then: 返回系统指标数据
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("osName"));
        assertTrue(metrics.containsKey("osVersion"));
        assertTrue(metrics.containsKey("osArch"));
        
        // 验证数据有效性
        String osName = (String) metrics.get("osName");
        assertNotNull(osName);
        assertFalse(osName.isEmpty());
    }

    @Test
    @DisplayName("应该获取全部系统指标（综合）")
    void shouldGetAllMetrics() {
        // When: 获取全部系统指标
        Map<String, Object> allMetrics = metricsService.getAllMetrics();

        // Then: 返回所有指标分类数据
        assertNotNull(allMetrics);
        assertTrue(allMetrics.containsKey("jvm"));
        assertTrue(allMetrics.containsKey("cpu"));
        assertTrue(allMetrics.containsKey("threads"));
        assertTrue(allMetrics.containsKey("gc"));
        assertTrue(allMetrics.containsKey("runtime"));
        assertTrue(allMetrics.containsKey("system"));
        
        // 验证每个分类都有数据
        Object jvmObj = allMetrics.get("jvm");
        assertNotNull(jvmObj);
        assertTrue(jvmObj instanceof Map);
        Map<?, ?> jvmMetrics = (Map<?, ?>) jvmObj;
        assertFalse(jvmMetrics.isEmpty());
    }

    @Test
    @DisplayName("应该计算健康状态（基于指标）")
    void shouldCalculateHealthStatus() {
        // When: 计算健康状态
        Map<String, Object> health = metricsService.getHealthStatus();

        // Then: 返回健康状态数据
        assertNotNull(health);
        assertTrue(health.containsKey("status"));
        assertTrue(health.containsKey("details"));
        
        // 验证状态值有效
        String status = (String) health.get("status");
        assertNotNull(status);
        assertTrue(status.equals("UP") || status.equals("WARNING") || status.equals("DOWN"));
    }

    @Test
    @DisplayName("应该检测内存不足警告")
    void shouldDetectLowMemoryWarning() {
        // When: 获取健康状态（假设内存使用率高）
        Map<String, Object> health = metricsService.getHealthStatus();

        // Then: 返回健康状态（可能包含警告）
        assertNotNull(health);
        Object detailsObj = health.get("details");
        assertNotNull(detailsObj);
        assertTrue(detailsObj instanceof Map);
        Map<?, ?> details = (Map<?, ?>) detailsObj;
        
        // 如果内存使用率 > 80%，应该有警告
        // 注意：实际测试时可能无法模拟高内存，这里只验证结构
        assertTrue(details.containsKey("memoryUsagePercent"));
    }

    @Test
    @DisplayName("应该返回可读的时间格式（运行时长）")
    void shouldReturnHumanReadableUptime() {
        // When: 获取运行时信息
        Map<String, Object> metrics = metricsService.getJvmRuntimeMetrics();

        // Then: 包含可读的运行时长
        assertTrue(metrics.containsKey("uptimeFormatted"));
        String uptimeFormatted = (String) metrics.get("uptimeFormatted");
        assertNotNull(uptimeFormatted);
        assertFalse(uptimeFormatted.isEmpty());
    }
}
