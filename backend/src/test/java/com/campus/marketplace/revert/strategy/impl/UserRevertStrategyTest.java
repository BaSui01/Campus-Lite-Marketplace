package com.campus.marketplace.revert.strategy.impl;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.AuditActionType;
import com.campus.marketplace.common.enums.AuditEntityType;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.revert.dto.RevertExecutionResult;
import com.campus.marketplace.revert.dto.RevertValidationResult;
import com.campus.marketplace.service.CacheService;
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
 * 用户撤销策略测试 - TDD严格模式
 *
 * 测试场景：
 * 1. 验证撤销时限（15天内）
 * 2. 验证用户状态变更撤销
 * 3. 验证已撤销操作拒绝
 * 4. 验证不支持的操作类型
 * 5. 验证用户状态回滚逻辑
 * 6. 验证封禁/解封逻辑
 *
 * @author BaSui 😎
 * @date 2025-11-03
 */
@DisplayName("用户撤销策略测试")
@ExtendWith(MockitoExtension.class)
class UserRevertStrategyTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private UserRevertStrategy userRevertStrategy;

    private AuditLog auditLog;
    private User user;

    @BeforeEach
    void setUp() {
        // 创建测试用审计日志
        auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setEntityType(AuditEntityType.USER);
        auditLog.setEntityId(300L);
        auditLog.setActionType(AuditActionType.UPDATE);
        auditLog.setOperatorId(1L);
        auditLog.setRevertDeadline(LocalDateTime.now().plusDays(10)); // 还剩10天
        auditLog.setIsReversible(true);
        auditLog.setRevertedByLogId(null);
        auditLog.setOldValue("{\"status\":\"ACTIVE\"}");

        // 创建测试用用户
        user = new User();
        user.setId(300L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.BANNED);
    }

    @Test
    @DisplayName("getSupportedEntityType应该返回USER")
    void getSupportedEntityType_ShouldReturnUser() {
        // Act
        String entityType = userRevertStrategy.getSupportedEntityType();

        // Assert
        assertThat(entityType).isEqualTo("USER");
    }

    // ============ 验证测试 ============

    @Test
    @DisplayName("验证撤销 - 15天内的用户更新操作应该通过验证")
    void validateRevert_UpdateWithinDeadline_ShouldPass() {
        // Act
        RevertValidationResult result = userRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).contains("验证通过");
    }

    @Test
    @DisplayName("验证撤销 - 超过15天的操作应该拒绝")
    void validateRevert_ExceedDeadline_ShouldFail() {
        // Arrange
        auditLog.setRevertDeadline(LocalDateTime.now().minusDays(1)); // 已过期

        // Act
        RevertValidationResult result = userRevertStrategy.validateRevert(auditLog, 1L);

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
        RevertValidationResult result = userRevertStrategy.validateRevert(auditLog, 1L);

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
        RevertValidationResult result = userRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("仅支持撤销用户信息更新操作");
    }

    @Test
    @DisplayName("验证撤销 - 历史数据为空应该拒绝")
    void validateRevert_NoOldValue_ShouldFail() {
        // Arrange
        auditLog.setOldValue(null);

        // Act
        RevertValidationResult result = userRevertStrategy.validateRevert(auditLog, 1L);

        // Assert
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("用户历史数据不存在");
    }

    // ============ 执行测试 ============

    @Test
    @DisplayName("执行撤销 - 用户状态应该回滚到历史状态")
    void executeRevert_ShouldRollbackUserStatus() {
        // Arrange
        when(userRepository.findById(300L))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RevertExecutionResult result = userRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("用户操作已撤销");

        // 验证用户被保存
        verify(userRepository).save(argThat(u ->
            u.getId().equals(300L) &&
            u.getStatus() == UserStatus.ACTIVE // 回滚到ACTIVE
        ));
    }

    @Test
    @DisplayName("执行撤销 - 用户不存在应该失败")
    void executeRevert_UserNotFound_ShouldFail() {
        // Arrange
        when(userRepository.findById(300L))
                .thenReturn(Optional.empty());

        // Act
        RevertExecutionResult result = userRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("用户不存在");

        // 验证没有保存操作
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("执行撤销 - 数据库异常应该返回失败")
    void executeRevert_DatabaseException_ShouldFail() {
        // Arrange
        when(userRepository.findById(300L))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // Act
        RevertExecutionResult result = userRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("撤销执行失败");
    }

    @Test
    @DisplayName("执行撤销 - 从BANNED回滚到ACTIVE应该成功")
    void executeRevert_FromBannedToActive_ShouldSucceed() {
        // Arrange
        user.setStatus(UserStatus.BANNED);
        auditLog.setOldValue("{\"status\":\"ACTIVE\"}");

        when(userRepository.findById(300L))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RevertExecutionResult result = userRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isTrue();

        // 验证用户状态回滚
        verify(userRepository).save(argThat(u ->
            u.getStatus() == UserStatus.ACTIVE
        ));
    }

    @Test
    @DisplayName("执行撤销 - 无法解析状态时应该仍然成功但不修改状态")
    void executeRevert_InvalidOldValue_ShouldSucceedWithoutStatusChange() {
        // Arrange
        user.setStatus(UserStatus.BANNED);
        auditLog.setOldValue("{\"email\":\"old@example.com\"}"); // 不包含status字段

        when(userRepository.findById(300L))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RevertExecutionResult result = userRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isTrue();

        // 验证用户仍然被保存（即使状态未改变）
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("执行撤销 - 状态相同时应该跳过状态更新")
    void executeRevert_SameStatus_ShouldSkipStatusUpdate() {
        // Arrange
        user.setStatus(UserStatus.ACTIVE);
        auditLog.setOldValue("{\"status\":\"ACTIVE\"}");

        when(userRepository.findById(300L))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RevertExecutionResult result = userRevertStrategy.executeRevert(auditLog, 1L);

        // Assert
        assertThat(result.isSuccess()).isTrue();

        // 验证用户被保存（即使状态未改变）
        verify(userRepository).save(any(User.class));
    }
}
