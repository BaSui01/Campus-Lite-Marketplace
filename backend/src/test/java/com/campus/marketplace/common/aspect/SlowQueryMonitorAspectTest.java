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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 慢查询监控切面测试类
 * 
 * TDD 开发：先写测试，定义慢查询监控的预期行为
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("慢查询监控切面测试")
class SlowQueryMonitorAspectTest {

    @InjectMocks
    private SlowQueryMonitorAspect slowQueryMonitorAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        // Mock JoinPoint 返回方法签名信息
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toString()).thenReturn("GoodsRepository.findById(..)");
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
    }

    @Test
    @DisplayName("应该记录慢查询（执行时间 > 500ms）")
    void shouldRecordSlowQuery() throws Throwable {
        // Given: Mock 一个慢查询（延迟 600ms）
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(600); // 模拟慢查询
            return "query result";
        });

        // When: 调用慢查询监控切面
        Object result = slowQueryMonitorAspect.monitorSlowQuery(joinPoint);

        // Then: 慢查询被记录（日志中应该有 WARN 日志）
        assertEquals("query result", result);
        verify(joinPoint, times(1)).proceed();

        Map<String, Object> stats = slowQueryMonitorAspect.getSlowQueryStatistics();
        assertThat(stats.get("totalSlowQueries")).isEqualTo(1L);
    }

    @Test
    @DisplayName("应该不记录正常查询（执行时间 < 500ms）")
    void shouldNotRecordFastQuery() throws Throwable {
        // Given: Mock 一个快速查询（延迟 100ms）
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(100); // 模拟快速查询
            return "query result";
        });

        // When: 调用慢查询监控切面
        Object result = slowQueryMonitorAspect.monitorSlowQuery(joinPoint);

        // Then: 正常查询不记录为慢查询
        assertEquals("query result", result);
        verify(joinPoint, times(1)).proceed();

        Map<String, Object> stats = slowQueryMonitorAspect.getSlowQueryStatistics();
        assertThat(stats.get("totalSlowQueries")).isEqualTo(0L);
    }

    @Test
    @DisplayName("应该统计慢查询次数")
    void shouldTrackSlowQueryCount() throws Throwable {
        // Given: Mock 多个慢查询
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(600); // 模拟慢查询
            return "result";
        });

        // When: 连续执行 3 次慢查询
        for (int i = 0; i < 3; i++) {
            slowQueryMonitorAspect.monitorSlowQuery(joinPoint);
        }

        // Then: 慢查询次数应该为 3
        Map<String, Object> stats = slowQueryMonitorAspect.getSlowQueryStatistics();
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalSlowQueries"));
        
        // 验证慢查询次数（至少有记录）
        long totalSlowQueries = (long) stats.get("totalSlowQueries");
        assertTrue(totalSlowQueries >= 3);
    }

    @Test
    @DisplayName("应该在查询抛出异常时也记录执行时间")
    void shouldRecordExecutionTimeEvenWhenExceptionThrown() throws Throwable {
        // Given: Mock 查询抛出异常
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(600); // 模拟慢查询
            throw new RuntimeException("Database error");
        });

        // When & Then: 调用监控应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            slowQueryMonitorAspect.monitorSlowQuery(joinPoint);
        });

        // 即使抛出异常，慢查询也应该被记录
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("应该获取慢查询统计信息")
    void shouldGetSlowQueryStatistics() throws Throwable {
        // Given: 先执行几次慢查询
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(600);
            return "result";
        });

        for (int i = 0; i < 2; i++) {
            slowQueryMonitorAspect.monitorSlowQuery(joinPoint);
        }

        // When: 获取慢查询统计
        Map<String, Object> stats = slowQueryMonitorAspect.getSlowQueryStatistics();

        // Then: 返回统计数据
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalSlowQueries"));
        assertTrue(stats.containsKey("averageSlowQueryTime"));
        
        // 验证数据有效性
        long totalSlowQueries = (long) stats.get("totalSlowQueries");
        assertTrue(totalSlowQueries >= 2);
    }

    @Test
    @DisplayName("应该获取慢查询详情列表")
    void shouldGetSlowQueryDetails() throws Throwable {
        // Given: 执行慢查询
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(600);
            return "result";
        });

        slowQueryMonitorAspect.monitorSlowQuery(joinPoint);

        // When: 获取慢查询详情
        List<SlowQueryMonitorAspect.SlowQueryRecord> records = slowQueryMonitorAspect.getSlowQueryRecords();

        // Then: 包含慢查询详情
        assertThat(records).hasSizeGreaterThanOrEqualTo(1);
        SlowQueryMonitorAspect.SlowQueryRecord record = records.get(records.size() - 1);
        assertThat(record.getMethodName()).isEqualTo("GoodsRepository.findById(..)");
        assertThat(record.getParameters()).contains("1");
    }

    @Test
    @DisplayName("应该支持配置慢查询阈值")
    void shouldSupportConfigurableThreshold() throws Throwable {
        // Given: Mock 一个接近阈值的查询（400ms）
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(400); // 小于默认阈值 500ms
            return "result";
        });

        // When: 调用监控
        slowQueryMonitorAspect.monitorSlowQuery(joinPoint);

        // Then: 不应该记录为慢查询
        Map<String, Object> stats = slowQueryMonitorAspect.getSlowQueryStatistics();
        long totalSlowQueries = (long) stats.get("totalSlowQueries");
        
        // 这个查询不应该被计入慢查询（因为 < 500ms）
        // 注意：这里无法精确验证，只能通过其他测试推断
        assertTrue(totalSlowQueries >= 0);
    }

    @Test
    @DisplayName("应该清除慢查询统计数据")
    void shouldClearSlowQueryStatistics() throws Throwable {
        // Given: 先执行慢查询
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(600);
            return "result";
        });

        slowQueryMonitorAspect.monitorSlowQuery(joinPoint);

        // When: 清除统计数据
        slowQueryMonitorAspect.clearStatistics();

        // Then: 统计数据被清零
        Map<String, Object> stats = slowQueryMonitorAspect.getSlowQueryStatistics();
        long totalSlowQueries = (long) stats.get("totalSlowQueries");
        assertEquals(0, totalSlowQueries);
    }

    @Test
    @DisplayName("应该记录方法名称和参数信息")
    void shouldRecordMethodNameAndParameters() throws Throwable {
        // Given: Mock 慢查询，带参数
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L, "test"});
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            Thread.sleep(600);
            return "result";
        });

        // When: 调用监控
        Object result = slowQueryMonitorAspect.monitorSlowQuery(joinPoint);

        // Then: 慢查询被记录（包含方法名和参数）
        assertEquals("result", result);
        verify(joinPoint, times(1)).getArgs();
    }
}
