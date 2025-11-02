package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Appeal;
import com.campus.marketplace.common.enums.AppealStatus;
import com.campus.marketplace.common.enums.AppealTargetType;
import com.campus.marketplace.common.enums.AppealType;
import com.campus.marketplace.repository.AppealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 申诉权限验证服务测试
 * 
 * TDD测试驱动开发：先写失败的测试，然后实现功能让测试通过
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("申诉权限验证服务测试")
class AppealPermissionServiceTest {

    @Mock
    private AppealRepository appealRepository;

    private AppealPermissionService appealPermissionService;

    @BeforeEach
    void setUp() {
        appealPermissionService = new com.campus.marketplace.service.impl.AppealPermissionServiceImpl(appealRepository);
    }

    @Test
    @DisplayName("用户应该可以查看自己的申诉")
    void userShouldBeAbleToViewOwnAppeal() {
        // Arrange
        Long userId = 123L;
        Appeal appeal = createTestAppeal(userId);

        when(appealRepository.findById(appeal.getId())).thenReturn(Optional.of(appeal));

        // Act
        boolean canView = appealPermissionService.canViewAppeal(userId, appeal.getId());

        // Assert
        assertThat(canView).isTrue();
    }

    @Test
    @DisplayName("用户不应该可以查看他人的申诉")
    void userShouldNotBeAbleToViewOthersAppeal() {
        // Arrange
        Long userId = 123L;
        Long otherUserId = 456L;
        Appeal appeal = createTestAppeal(otherUserId);

        when(appealRepository.findById(appeal.getId())).thenReturn(Optional.of(appeal));

        // Act
        boolean canView = appealPermissionService.canViewAppeal(userId, appeal.getId());

        // Assert
        assertThat(canView).isFalse();
    }

    @Test
    @DisplayName("管理员应该可以查看所有申诉")
    void adminShouldBeAbleToViewAllAppeals() {
        // Arrange
        Long adminUserId = 999L;
        Appeal appeal = createTestAppeal(123L);

        when(appealRepository.findById(appeal.getId())).thenReturn(Optional.of(appeal));

        // Act
        boolean canView = appealPermissionService.canViewAppealAsAdmin(adminUserId, appeal.getId());

        // Assert
        assertThat(canView).isTrue();
    }

    @Test
    @DisplayName("用户可以编辑待处理状态的申诉")
    void userCanEditPendingAppeal() {
        // Arrange
        Long userId = 123L;
        Appeal appeal = createTestAppeal(userId);
        appeal.setStatus(AppealStatus.PENDING);

        when(appealRepository.findById(appeal.getId())).thenReturn(Optional.of(appeal));

        // Act
        boolean canEdit = appealPermissionService.canEditAppeal(userId, appeal.getId());

        // Assert
        assertThat(canEdit).isTrue();
    }

    @Test
    @DisplayName("用户不能编辑已处理的申诉")
    void userCannotEditProcessedAppeal() {
        // Arrange
        Long userId = 123L;
        Appeal appeal = createTestAppeal(userId);
        appeal.setStatus(AppealStatus.APPROVED);

        when(appealRepository.findById(appeal.getId())).thenReturn(Optional.of(appeal));

        // Act
        boolean canEdit = appealPermissionService.canEditAppeal(userId, appeal.getId());

        // Assert
        assertThat(canEdit).isFalse();
    }

    @Test
    @DisplayName("用户可以取消自己的待处理申诉")
    void userCanCancelOwnPendingAppeal() {
        // Arrange
        Long userId = 123L;
        Appeal appeal = createTestAppeal(userId);
        appeal.setStatus(AppealStatus.PENDING);

        when(appealRepository.findById(appeal.getId())).thenReturn(Optional.of(appeal));

        // Act
        boolean canCancel = appealPermissionService.canCancelAppeal(userId, appeal.getId());

        // Assert
        assertThat(canCancel).isTrue();
    }

    @Test
    @DisplayName("用户不能取消已审核的申诉")
    void userCannotCancelReviewedAppeal() {
        // Arrange
        Long userId = 123L;
        Appeal appeal = createTestAppeal(userId);
        appeal.setStatus(AppealStatus.REVIEWING);

        when(appealRepository.findById(appeal.getId())).thenReturn(Optional.of(appeal));

        // Act
        boolean canCancel = appealPermissionService.canCancelAppeal(userId, appeal.getId());

        // Assert
        assertThat(canCancel).isFalse();
    }

    @Test
    @DisplayName("管理员可以处理任何待处理的申诉")
    void adminCanHandleAnyPendingAppeal() {
        // Arrange
        Long adminUserId = 999L;
        Appeal appeal = createTestAppeal(123L);
        appeal.setStatus(AppealStatus.PENDING);

        when(appealRepository.findById(appeal.getId())).thenReturn(Optional.of(appeal));

        // Act
        boolean canHandle = appealPermissionService.canHandleAppeal(adminUserId, appeal.getId());

        // Assert
        assertThat(canHandle).isTrue();
    }

    @Test
    @DisplayName("管理员不能重复处理已完成的申诉")
    void adminCannotReHandleCompletedAppeal() {
        // Arrange
        Long adminUserId = 999L;
        Appeal appeal = createTestAppeal(123L);
        appeal.setStatus(AppealStatus.APPROVED);

        when(appealRepository.findById(appeal.getId())).thenReturn(Optional.of(appeal));

        // Act
        boolean canHandle = appealPermissionService.canHandleAppeal(adminUserId, appeal.getId());

        // Assert
        assertThat(canHandle).isFalse();
    }

    @Test
    @DisplayName("不存在的申诉应该返回无权限")
    void nonExistentAppealShouldReturnNoPermission() {
        // Arrange
        Long userId = 123L;
        Long appealId = 999L;

        when(appealRepository.findById(appealId)).thenReturn(Optional.empty());

        // Act
        boolean canView = appealPermissionService.canViewAppeal(userId, appealId);

        // Assert
        assertThat(canView).isFalse();
    }

    @Test
    @DisplayName("应该验证用户是否有申诉权限")
    void shouldValidateUserHasAppealPermission() {
        // Arrange
        Long userId = 123L;

        // Act
        boolean hasPermission = appealPermissionService.hasAppealPermission(userId);

        // Assert
        assertThat(hasPermission).isTrue(); // 默认所有用户都有申诉权限
    }

    @Test
    @DisplayName("应该检查用户是否超过申诉次数限制")
    void shouldCheckUserAppealCountLimit() {
        // Arrange
        Long userId = 123L;
        int currentCount = 5;
        int maxCount = 10;

        when(appealRepository.countByUserIdAndCreatedAtAfter(eq(userId), any(LocalDateTime.class)))
            .thenReturn((long) currentCount);

        // Act
        boolean isWithinLimit = appealPermissionService.isWithinAppealLimit(userId, maxCount);

        // Assert
        assertThat(isWithinLimit).isTrue();
    }

    @Test
    @DisplayName("应该拒绝超过限制次数的申诉")
    void shouldRejectAppealBeyondLimit() {
        // Arrange
        Long userId = 123L;
        int currentCount = 10;
        int maxCount = 10;

        when(appealRepository.countByUserIdAndCreatedAtAfter(eq(userId), any(LocalDateTime.class)))
            .thenReturn((long) currentCount);

        // Act
        boolean isWithinLimit = appealPermissionService.isWithinAppealLimit(userId, maxCount);

        // Assert
        assertThat(isWithinLimit).isFalse();
    }

    // ========== 辅助方法 ==========

    private Appeal createTestAppeal(Long userId) {
        Appeal appeal = new Appeal();
        appeal.setId(1000L);
        appeal.setUserId(userId);
        appeal.setTargetType(AppealTargetType.USER_BAN);
        appeal.setTargetId(456L);
        appeal.setAppealType(AppealType.UNJUST_BAN);
        appeal.setReason("测试申诉原因");
        appeal.setStatus(AppealStatus.PENDING);
        appeal.setCreatedAt(LocalDateTime.now());
        appeal.setDeadline(LocalDateTime.now().plusDays(7));
        return appeal;
    }
}
