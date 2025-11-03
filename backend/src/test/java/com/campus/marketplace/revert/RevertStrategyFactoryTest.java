package com.campus.marketplace.revert;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.enums.AuditEntityType;
import com.campus.marketplace.revert.factory.RevertStrategyFactory;
import com.campus.marketplace.revert.strategy.RevertStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 撤销策略工厂测试 - TDD第1步
 * 
 * 测试策略：
 * 1. 策略注册和发现：验证策略自动注册机制
 * 2. 策略获取：验证根据实体类型获取正确策略
 * 3. 不支持类型处理：验证异常处理机制
 * 4. 策略列表查询：验证支持的策略类型列表
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@DisplayName("撤销策略工厂测试")
@ExtendWith(MockitoExtension.class)
class RevertStrategyFactoryTest {

    private RevertStrategyFactory strategyFactory;

    @BeforeEach
    void setUp() {
        strategyFactory = new RevertStrategyFactory();
    }

    @Test
    @DisplayName("应该根据实体类型获取正确的策略")
    void shouldGetCorrectStrategyByEntityType() {
        // Given
        AuditLog auditLog = AuditLog.builder()
                .entityType(AuditEntityType.GOODS)
                .entityId(1001L)
                .build();

        // When
        RevertStrategy strategy = strategyFactory.getStrategy(auditLog);

        // Then
        assertThat(strategy).isNotNull();
        assertThat(strategy.getSupportedEntityType()).isEqualTo("GOODS");
    }

    @Test
    @DisplayName("应该支持订单撤销策略")
    void shouldSupportOrderRevertStrategy() {
        // Given
        AuditLog auditLog = AuditLog.builder()
                .entityType(AuditEntityType.ORDER)
                .entityId(2001L)
                .build();

        // When
        RevertStrategy strategy = strategyFactory.getStrategy(auditLog);

        // Then
        assertThat(strategy).isNotNull();
        assertThat(strategy.getSupportedEntityType()).isEqualTo("ORDER");
    }

    @Test
    @DisplayName("应该支持用户撤销策略")
    void shouldSupportUserRevertStrategy() {
        // Given
        AuditLog auditLog = AuditLog.builder()
                .entityType(AuditEntityType.USER)
                .entityId(3001L)
                .build();

        // When
        RevertStrategy strategy = strategyFactory.getStrategy(auditLog);

        // Then
        assertThat(strategy).isNotNull();
        assertThat(strategy.getSupportedEntityType()).isEqualTo("USER");
    }

    @Test
    @DisplayName("应该支持批量操作撤销策略")
    void shouldSupportBatchRevertStrategy() {
        // Given
        AuditLog auditLog = AuditLog.builder()
                .entityType(AuditEntityType.BATCH_OPERATION)
                .entityId(4001L)
                .build();

        // When
        RevertStrategy strategy = strategyFactory.getStrategy(auditLog);

        // Then
        assertThat(strategy).isNotNull();
        assertThat(strategy.getSupportedEntityType()).isEqualTo("BATCH_OPERATION");
    }

    @Test
    @DisplayName("不支持的实体类型应该抛出异常")
    void shouldThrowExceptionForUnsupportedEntityType() {
        // Given
        AuditLog auditLog = AuditLog.builder()
                .entityType(AuditEntityType.POST) // 不支持的类型
                .entityId(5001L)
                .build();

        // When & Then
        assertThatThrownBy(() -> strategyFactory.getStrategy(auditLog))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的撤销实体类型");
    }

    @Test
    @DisplayName("应该返回所有支持的实体类型列表")
    void shouldReturnAllSupportedEntityTypes() {
        // When
        var supportedTypes = strategyFactory.getSupportedEntityTypes();

        // Then
        assertThat(supportedTypes).isNotEmpty();
        assertThat(supportedTypes).contains("GOODS", "ORDER", "USER", "BATCH_OPERATION");
    }

    @Test
    @DisplayName("应该检查实体类型是否支持撤销")
    void shouldCheckIfEntityTypeIsSupported() {
        // When & Then
        assertThat(strategyFactory.isSupported(AuditEntityType.GOODS)).isTrue();
        assertThat(strategyFactory.isSupported(AuditEntityType.ORDER)).isTrue();
        assertThat(strategyFactory.isSupported(AuditEntityType.USER)).isTrue();
        assertThat(strategyFactory.isSupported(AuditEntityType.POST)).isFalse();
    }
}
