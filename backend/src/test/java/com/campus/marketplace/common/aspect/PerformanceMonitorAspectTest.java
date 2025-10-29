package com.campus.marketplace.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 性能监控切面测试类（单元测试）
 * 
 * TDD 开发：先写测试，定义性能监控的预期行为
 * 不依赖 Spring 容器，纯单元测试
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("性能监控切面测试")
class PerformanceMonitorAspectTest {

    @InjectMocks
    private PerformanceMonitorAspect performanceMonitorAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        // Mock JoinPoint 返回方法签名信息
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toString()).thenReturn("GoodsService.listGoods(..)");
    }

    @Test
    @DisplayName("应该记录方法执行时间")
    void shouldRecordMethodExecutionTime() throws Throwable {
        // Given: Mock 方法正常执行
        when(joinPoint.proceed()).thenReturn("test result");
        
        // When: 调用性能监控切面（模拟 AOP 拦截）
        Object result = performanceMonitorAspect.monitorPerformance(joinPoint);

        // Then: 验证方法被执行，并返回结果
        assertEquals("test result", result);
        verify(joinPoint, times(1)).proceed();

        Map<String, Object> stats = performanceMonitorAspect.getPerformanceStats();
        assertThat(stats).containsKey("GoodsService.listGoods(..)");
        Map<String, Object> methodStats = (Map<String, Object>) stats.get("GoodsService.listGoods(..)");
        assertThat(methodStats.get("totalCalls")).isEqualTo(1L);
        assertThat(methodStats.get("totalErrors")).isEqualTo(0L);
    }

    @Test
    @DisplayName("应该记录慢查询（执行时间 > 1000ms）")
    void shouldRecordSlowQuery() throws Throwable {
        // Given: Mock 一个慢查询（延迟 1500ms）
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(1500); // 模拟慢查询
            return "slow result";
        });

        // When: 调用性能监控切面
        Object result = performanceMonitorAspect.monitorPerformance(joinPoint);

        // Then: 验证慢查询被记录（日志中应该有 WARN 日志）
        assertEquals("slow result", result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("应该在方法抛出异常时也记录性能数据")
    void shouldRecordPerformanceEvenWhenExceptionThrown() throws Throwable {
        // Given: Mock 方法抛出异常
        when(joinPoint.proceed()).thenThrow(new RuntimeException("测试异常"));

        // When & Then: 调用性能监控应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            performanceMonitorAspect.monitorPerformance(joinPoint);
        });

        // 即使抛出异常，性能监控也应该记录数据
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("应该统计方法调用次数（QPS）")
    void shouldTrackMethodInvocationCount() throws Throwable {
        // Given: Mock 方法正常执行
        when(joinPoint.proceed()).thenReturn("result");
        
        // When: 连续调用 5 次
        for (int i = 0; i < 5; i++) {
            performanceMonitorAspect.monitorPerformance(joinPoint);
        }

        // Then: 验证方法被调用 5 次
        verify(joinPoint, times(5)).proceed();
        
        // 获取性能统计数据
        Map<String, Object> stats = performanceMonitorAspect.getPerformanceStats();
        assertNotNull(stats);
        Map<String, Object> methodStats = (Map<String, Object>) stats.get("GoodsService.listGoods(..)");
        assertThat(methodStats.get("totalCalls")).isEqualTo(5L);
        assertThat((Long) methodStats.get("totalExecutionTime")).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("应该获取性能统计报告")
    void shouldGetPerformanceReport() throws Throwable {
        // Given: 先调用几次方法生成性能数据
        when(joinPoint.proceed()).thenReturn("result");
        
        for (int i = 0; i < 3; i++) {
            performanceMonitorAspect.monitorPerformance(joinPoint);
        }

        // When: 获取性能统计报告
        Map<String, Object> stats = performanceMonitorAspect.getPerformanceStats();

        // Then: 验证报告包含必要信息
        assertNotNull(stats);
        Map<String, Object> methodStats = (Map<String, Object>) stats.get("GoodsService.listGoods(..)");
        assertThat(methodStats).containsKeys("avgExecutionTime", "maxExecutionTime", "slowCount");
    }
}
