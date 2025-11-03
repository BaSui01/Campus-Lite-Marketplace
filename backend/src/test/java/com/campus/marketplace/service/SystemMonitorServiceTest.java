package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.HealthCheckResponse;
import com.campus.marketplace.common.dto.response.SystemMetricsResponse;
import com.campus.marketplace.common.entity.HealthCheckRecord;
import com.campus.marketplace.common.enums.HealthStatus;
import com.campus.marketplace.repository.HealthCheckRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 系统监控服务测试
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
class SystemMonitorServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HealthCheckRecordRepository healthCheckRecordRepository;

    @InjectMocks
    private SystemMonitorService systemMonitorService;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
    }

    @Test
    @DisplayName("健康检查应该返回所有组件状态")
    void healthCheckShouldReturnAllComponentStatus() throws Exception {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(anyInt())).thenReturn(true);
        when(redisTemplate.getConnectionFactory()).thenReturn(mock(org.springframework.data.redis.connection.RedisConnectionFactory.class));

        // Act
        HealthCheckResponse response = systemMonitorService.performHealthCheck();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOverallStatus()).isIn(HealthStatus.HEALTHY, HealthStatus.DEGRADED);
        assertThat(response.getComponents()).containsKeys("database", "redis", "jvm");
    }

    @Test
    @DisplayName("数据库连接失败应该标记为UNHEALTHY")
    void databaseConnectionFailureShouldMarkAsUnhealthy() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenThrow(new RuntimeException("Connection refused"));

        // Act
        HealthCheckResponse response = systemMonitorService.performHealthCheck();

        // Assert
        assertThat(response.getOverallStatus()).isEqualTo(HealthStatus.UNHEALTHY);
        assertThat(response.getComponents().get("database").getStatus()).isEqualTo(HealthStatus.UNHEALTHY);
    }

    @Test
    @DisplayName("Redis连接失败应该标记为DEGRADED")
    void redisConnectionFailureShouldMarkAsDegraded() throws Exception {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(anyInt())).thenReturn(true);
        when(redisTemplate.getConnectionFactory()).thenThrow(new RuntimeException("Redis unavailable"));

        // Act
        HealthCheckResponse response = systemMonitorService.performHealthCheck();

        // Assert
        assertThat(response.getOverallStatus()).isIn(HealthStatus.DEGRADED, HealthStatus.UNHEALTHY);
        assertThat(response.getComponents().get("redis").getStatus()).isEqualTo(HealthStatus.UNHEALTHY);
    }

    @Test
    @DisplayName("JVM内存使用超过85%应该发出警告")
    void highJvmMemoryUsageShouldTriggerWarning() {
        // Act
        HealthCheckResponse response = systemMonitorService.performHealthCheck();

        // Assert - JVM组件应该存在
        assertThat(response.getComponents()).containsKey("jvm");
        Map<String, Object> jvmDetails = response.getComponents().get("jvm").getDetails();
        assertThat(jvmDetails).containsKeys("memoryUsagePercent", "totalMemoryMB", "usedMemoryMB");
    }

    @Test
    @DisplayName("应该能保存健康检查记录")
    void shouldSaveHealthCheckRecord() {
        // Arrange
        HealthCheckRecord record = new HealthCheckRecord();
        record.setOverallStatus(HealthStatus.HEALTHY);
        when(healthCheckRecordRepository.save(any(HealthCheckRecord.class))).thenReturn(record);

        // Act
        systemMonitorService.performHealthCheck();

        // Assert
        verify(healthCheckRecordRepository, times(1)).save(any(HealthCheckRecord.class));
    }

    @Test
    @DisplayName("应该能获取系统指标")
    void shouldGetSystemMetrics() {
        // Act
        SystemMetricsResponse response = systemMonitorService.getSystemMetrics();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCpuUsagePercent()).isGreaterThanOrEqualTo(0);
        assertThat(response.getMemoryUsagePercent()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("应该能获取健康检查历史")
    void shouldGetHealthCheckHistory() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<HealthCheckRecord> mockRecords = List.of(
            createMockRecord(now, HealthStatus.HEALTHY),
            createMockRecord(now.minusMinutes(5), HealthStatus.HEALTHY)
        );
        when(healthCheckRecordRepository.findRecentRecords(any(LocalDateTime.class)))
            .thenReturn(mockRecords);

        // Act
        List<HealthCheckRecord> records = systemMonitorService.getHealthCheckHistory(24);

        // Assert
        assertThat(records).hasSize(2);
        verify(healthCheckRecordRepository).findRecentRecords(any(LocalDateTime.class));
    }

    private HealthCheckRecord createMockRecord(LocalDateTime time, HealthStatus status) {
        HealthCheckRecord record = new HealthCheckRecord();
        record.setOverallStatus(status);
        record.setCheckTime(time);
        return record;
    }
}
