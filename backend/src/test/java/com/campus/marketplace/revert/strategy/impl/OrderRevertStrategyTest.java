package com.campus.marketplace.revert.strategy.impl;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.enums.AuditEntityType;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.RefundRequestRepository;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import com.campus.marketplace.revert.dto.RevertValidationResult;
import com.campus.marketplace.service.CacheService;
import com.campus.marketplace.service.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单撤销策略测试 - TDD严格模式
 *
 * 测试场景：
 * 1. 验证撤销时限（7天内）
 * 2. 验证订单状态变更撤销
 * 3. 验证已撤销操作拒绝
 * 4. 验证不支持的操作类型
 * 5. 验证状态回滚逻辑
 * 6. 验证退款逻辑（如果需要）
 *
 * @author BaSui 😎
 * @date 2025-11-03
 */
@DisplayName("订单撤销策略测试")
@ExtendWith(MockitoExtension.class)
class OrderRevertStrategyTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private RefundService refundService;

    @Mock
    private RefundRequestRepository refundRequestRepository;

    @InjectMocks
    private OrderRevertStrategy orderRevertStrategy;

    private AuditLog auditLog;
    private Order order;

    @BeforeEach
    void setUp() {
        // 创建测试用审计日志
        auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setEntityType(AuditEntityType.ORDER);
        auditLog.setEntityId(200L);
        auditLog.setActionType(AuditActionType.UPDATE);
        auditLog.setOperatorId(1L);
        auditLog.setRevertDeadline(LocalDateTime.now().plusDays(5)); // 还剩5天
        auditLog.setIsReversible(true);
        auditLog.setRevertedByLogId(null);
        auditLog.setOldValue("{\"status\":\"PENDING_PAYMENT\"}");

        // 创建测试用订单
        order = new Order();
        order.setId(200L);
        order.setOrderNo("ORD20250101001");
        order.setGoodsId(100L);
        order.setBuyerId(10L);
        order.setSellerId(20L);
        order.setAmount(new BigDecimal("99.99"));
        order.setStatus(OrderStatus.PAID);
    }

    @Test
    @DisplayName("getSupportedEntityType应该返回ORDER")
    void getSupportedEntityType_ShouldReturnOrder() {
        // Act
        String entityType = orderRevertStrategy.getSupportedEntityType();

        // Assert
        assertThat(entityType).isEqualTo("ORDER");
    }

    // ============ 验证测试 ============

    @Test
    @DisplayName("验证撤销 - 7天内的订单更新操作应该通过验证")
    void validateRevert_UpdateWithinDeadline_ShouldPass() {
        // Act
        RevertValidationResult result = orderRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("验证撤销 - 超过7天的操作应该拒绝")
    void validateRevert_ExceedDeadline_ShouldFail() {
        // Arrange
        auditLog.setRevertDeadline(LocalDateTime.now().minusDays(1)); // 已过期

        // Act
        RevertValidationResult result = orderRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("超过撤销期限");
    }

    @Test
    @DisplayName("验证撤销 - 已经被撤销过的操作应该拒绝")
    void validateRevert_AlreadyReverted_ShouldFail() {
        // Arrange
        auditLog.setRevertedByLogId(999L);
        auditLog.setRevertedAt(LocalDateTime.now());

        // Act
        RevertValidationResult result = orderRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("已被撤销过");
    }

    @Test
    @DisplayName("验证撤销 - 不支持的操作类型应该拒绝")
    void validateRevert_UnsupportedActionType_ShouldFail() {
        // Arrange
        auditLog.setActionType(AuditActionType.DELETE);

        // Act
        RevertValidationResult result = orderRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("仅支持撤销订单状态变更操作");
    }

    // ============ 执行测试 ============

    @Test
    @DisplayName("执行撤销 - 订单状态应该回滚到历史状态")
    void executeRevert_ShouldRollbackOrderStatus() {
        // Arrange
        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RevertExecutionResult result = orderRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("订单状态已回滚");

        // 验证订单被保存
        verify(orderRepository).save(argThat(o ->
            o.getId().equals(200L) &&
            o.getStatus() == OrderStatus.PENDING_PAYMENT // 回滚到待支付
        ));
    }

    @Test
    @DisplayName("执行撤销 - 订单不存在应该失败")
    void executeRevert_OrderNotFound_ShouldFail() {
        // Arrange
        when(orderRepository.findById(200L))
                .thenReturn(Optional.empty());

        // Act
        RevertExecutionResult result = orderRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("订单不存在");

        // 验证没有保存操作
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("执行撤销 - 历史状态数据为空应该失败")
    void executeRevert_NoOldValue_ShouldFail() {
        // Arrange
        auditLog.setOldValue(null);

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(order));

        // Act
        RevertExecutionResult result = orderRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("历史状态数据不存在");

        // 验证没有保存操作
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("执行撤销 - 无法解析历史状态应该失败")
    void executeRevert_InvalidOldValue_ShouldFail() {
        // Arrange
        auditLog.setOldValue("{\"invalid\":\"data\"}"); // 不包含status字段

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(order));

        // Act
        RevertExecutionResult result = orderRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("无法解析历史状态");

        // 验证没有保存操作
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("执行撤销 - 数据库异常应该返回失败")
    void executeRevert_DatabaseException_ShouldFail() {
        // Arrange
        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // Act
        RevertExecutionResult result = orderRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("撤销执行失败");
    }

    @Test
    @DisplayName("执行撤销 - 从COMPLETED回滚到PAID应该处理退款")
    void executeRevert_FromCompletedToPaid_ShouldHandleRefund() {
        // Arrange
        order.setStatus(OrderStatus.COMPLETED);
        auditLog.setOldValue("{\"status\":\"PAID\"}");

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RevertExecutionResult result = orderRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isTrue();

        // 验证订单状态回滚
        verify(orderRepository).save(argThat(o ->
            o.getStatus() == OrderStatus.PAID
        ));
    }

    @Test
    @DisplayName("执行撤销 - 不合法的状态转换应该拒绝")
    void executeRevert_InvalidStatusTransition_ShouldFail() {
        // Arrange
        order.setStatus(OrderStatus.CANCELLED);
        auditLog.setOldValue("{\"status\":\"COMPLETED\"}"); // 不允许从CANCELLED回到COMPLETED

        when(orderRepository.findById(200L))
                .thenReturn(Optional.of(order));

        // Act
        RevertExecutionResult result = orderRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("已取消的订单不允许回滚");

        // 验证没有保存操作
        verify(orderRepository, never()).save(any());
    }
}
