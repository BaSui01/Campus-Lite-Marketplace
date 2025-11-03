package com.campus.marketplace.revert.strategy.impl;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.entity.BatchTask;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.enums.AuditEntityType;
import com.campus.marketplace.common.enums.BatchTaskStatus;
import com.campus.marketplace.common.enums.BatchType;
import com.campus.marketplace.repository.BatchTaskRepository;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import com.campus.marketplace.revert.dto.RevertValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 批量撤销策略测试 - TDD严格模式（最后一关）🎯
 *
 * 测试场景：
 * 1. 验证撤销时限（7天内）
 * 2. 验证批量任务状态检查
 * 3. 验证已撤销操作拒绝
 * 4. 验证不支持的操作类型
 * 5. 验证批量任务取消逻辑
 * 6. 验证已完成任务撤销警告
 *
 * @author BaSui 😎 - 最后一个策略测试了！加油！
 * @date 2025-11-03
 */
@DisplayName("批量撤销策略测试")
@ExtendWith(MockitoExtension.class)
class BatchRevertStrategyTest {

    @Mock
    private BatchTaskRepository batchTaskRepository;

    @InjectMocks
    private BatchRevertStrategy batchRevertStrategy;

    private AuditLog auditLog;
    private BatchTask batchTask;

    @BeforeEach
    void setUp() {
        // 创建测试用审计日志
        auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setEntityType(AuditEntityType.BATCH_OPERATION);
        auditLog.setEntityId(400L);
        auditLog.setActionType(AuditActionType.UPDATE);
        auditLog.setOperatorId(1L);
        auditLog.setRevertDeadline(LocalDateTime.now().plusDays(5)); // 还剩5天
        auditLog.setIsReversible(true);
        auditLog.setRevertedByLogId(null);

        // 创建测试用批量任务
        batchTask = new BatchTask();
        batchTask.setId(400L);
        batchTask.setTaskCode("BATCH_TASK_400");
        batchTask.setBatchType(BatchType.GOODS_BATCH);
        batchTask.setUserId(1L);
        batchTask.setStatus(BatchTaskStatus.PROCESSING);
        batchTask.setTotalCount(100);
        batchTask.setSuccessCount(50);
        batchTask.setErrorCount(5);
    }

    @Test
    @DisplayName("getSupportedEntityType应该返回BATCH_OPERATION")
    void getSupportedEntityType_ShouldReturnBatchOperation() {
        // Act
        String entityType = batchRevertStrategy.getSupportedEntityType();

        // Assert
        assertThat(entityType).isEqualTo("BATCH_OPERATION");
    }

    // ============ 验证测试 ============

    @Test
    @DisplayName("验证撤销 - 7天内的批量操作应该通过验证")
    void validateRevert_WithinDeadline_ShouldPass() {
        // Arrange
        when(batchTaskRepository.findById(400L))
                .thenReturn(Optional.of(batchTask));

        // Act
        RevertValidationResult result = batchRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("验证撤销 - 超过7天的操作应该拒绝")
    void validateRevert_ExceedDeadline_ShouldFail() {
        // Arrange
        auditLog.setRevertDeadline(LocalDateTime.now().minusDays(1)); // 已过期

        // Act
        RevertValidationResult result = batchRevertStrategy.validateRevert(auditLog, 1L);

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
        RevertValidationResult result = batchRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("已被撤销过");
    }

    @Test
    @DisplayName("验证撤销 - 批量任务不存在应该拒绝")
    void validateRevert_TaskNotFound_ShouldFail() {
        // Arrange
        when(batchTaskRepository.findById(400L))
                .thenReturn(Optional.empty());

        // Act
        RevertValidationResult result = batchRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("批量任务不存在");
    }

    @Test
    @DisplayName("验证撤销 - 已取消的批量任务应该拒绝")
    void validateRevert_CancelledTask_ShouldFail() {
        // Arrange
        batchTask.setStatus(BatchTaskStatus.CANCELLED);

        when(batchTaskRepository.findById(400L))
                .thenReturn(Optional.of(batchTask));

        // Act
        RevertValidationResult result = batchRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("已被取消");
    }

    @Test
    @DisplayName("验证撤销 - 已完成的批量任务应该返回警告")
    void validateRevert_CompletedTask_ShouldReturnWarning() {
        // Arrange
        batchTask.setStatus(BatchTaskStatus.SUCCESS);
        batchTask.setSuccessCount(95);
        batchTask.setErrorCount(5);
        batchTask.setTotalCount(100);

        when(batchTaskRepository.findById(400L))
                .thenReturn(Optional.of(batchTask));

        // Act
        RevertValidationResult result = batchRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isTrue(); // 警告仍然算验证通过
        assertThat(result.getMessage()).contains("已完成");
        assertThat(result.getMessage()).contains("严格审批");
    }

    @Test
    @DisplayName("验证撤销 - 不支持的操作类型应该拒绝")
    void validateRevert_UnsupportedActionType_ShouldFail() {
        // Arrange
        auditLog.setActionType(AuditActionType.DELETE);

        // Act
        RevertValidationResult result = batchRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("不支持撤销该类型的批量操作");
    }

    // ============ 执行测试 ============

    @Test
    @DisplayName("执行撤销 - 进行中的批量任务应该被取消")
    void executeRevert_ProcessingTask_ShouldBeCancelled() {
        // Arrange
        batchTask.setStatus(BatchTaskStatus.PROCESSING);

        when(batchTaskRepository.findById(400L))
                .thenReturn(Optional.of(batchTask));
        when(batchTaskRepository.save(any(BatchTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RevertExecutionResult result = batchRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("批量任务已取消");

        // 验证任务状态被更新为CANCELLED
        verify(batchTaskRepository).save(argThat(t ->
            t.getStatus() == BatchTaskStatus.CANCELLED
        ));
    }

    @Test
    @DisplayName("执行撤销 - 批量任务不存在应该失败")
    void executeRevert_TaskNotFound_ShouldFail() {
        // Arrange
        when(batchTaskRepository.findById(400L))
                .thenReturn(Optional.empty());

        // Act
        RevertExecutionResult result = batchRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("批量任务不存在");

        // 验证没有保存操作
        verify(batchTaskRepository, never()).save(any());
    }

    @Test
    @DisplayName("执行撤销 - 数据库异常应该返回失败")
    void executeRevert_DatabaseException_ShouldFail() {
        // Arrange
        when(batchTaskRepository.findById(400L))
                .thenReturn(Optional.of(batchTask));
        when(batchTaskRepository.save(any(BatchTask.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // Act
        RevertExecutionResult result = batchRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("撤销执行失败");
    }
}
