package com.campus.marketplace.revert;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.enums.AuditEntityType;
import com.campus.marketplace.revert.factory.RevertStrategyFactory;
import com.campus.marketplace.revert.strategy.RevertStrategy;
import com.campus.marketplace.revert.strategy.impl.BatchRevertStrategy;
import com.campus.marketplace.revert.strategy.impl.GoodsRevertStrategy;
import com.campus.marketplace.revert.strategy.impl.OrderRevertStrategy;
import com.campus.marketplace.revert.strategy.impl.UserRevertStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
 * @author BaSui 😎 - 修复了构造函数依赖注入问题！
 * @date 2025-11-03
 */
@DisplayName("撤销策略工厂测试")
@ExtendWith(MockitoExtension.class)
class RevertStrategyFactoryTest {

    private RevertStrategyFactory strategyFactory;

    @Mock
    private com.campus.marketplace.repository.GoodsRepository goodsRepository;

    @Mock
    private com.campus.marketplace.service.DataBackupService dataBackupService;

    @Mock
    private com.campus.marketplace.service.CacheService cacheService;

    @Mock
    private com.campus.marketplace.repository.OrderRepository orderRepository;

    @Mock
    private com.campus.marketplace.service.PaymentService paymentService;

    @Mock
    private com.campus.marketplace.service.RefundService refundService;

    @Mock
    private com.campus.marketplace.repository.UserRepository userRepository;

    @Mock
    private com.campus.marketplace.repository.BatchTaskRepository batchTaskRepository;

    @BeforeEach
    void setUp() {
        // 创建所有撤销策略实例（Mock 依赖）
        GoodsRevertStrategy goodsStrategy = new GoodsRevertStrategy(goodsRepository, dataBackupService, cacheService);
        OrderRevertStrategy orderStrategy = new OrderRevertStrategy(orderRepository, cacheService, paymentService, refundService);
        UserRevertStrategy userStrategy = new UserRevertStrategy(userRepository, dataBackupService, cacheService);
        BatchRevertStrategy batchStrategy = new BatchRevertStrategy(batchTaskRepository);

        // 注入策略列表到工厂
        List<RevertStrategy> strategies = List.of(goodsStrategy, orderStrategy, userStrategy, batchStrategy);
        strategyFactory = new RevertStrategyFactory(strategies);

        // 手动调用初始化方法（模拟 @PostConstruct）
        strategyFactory.init();
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
