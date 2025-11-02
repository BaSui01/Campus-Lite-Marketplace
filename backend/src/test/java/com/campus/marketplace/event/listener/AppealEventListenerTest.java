package com.campus.marketplace.event.listener;

import com.campus.marketplace.common.entity.Appeal;
import com.campus.marketplace.common.enums.AppealStatus;
import com.campus.marketplace.common.enums.AppealTargetType;
import com.campus.marketplace.common.enums.AppealType;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.event.AppealCreatedEvent;
import com.campus.marketplace.event.AppealHandledEvent;
import com.campus.marketplace.event.AppealStatusChangedEvent;
import com.campus.marketplace.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 申诉事件监听器测试
 * 
 * TDD测试驱动开发：先写失败的测试，然后实现功能让测试通过
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("申诉事件监听器测试")
class AppealEventListenerTest {

    @Mock
    private NotificationService notificationService;

    private AppealEventListener appealEventListener;

    @BeforeEach
    void setUp() {
        appealEventListener = new AppealEventListener(notificationService);
    }

    @Test
    @DisplayName("应该在申诉创建时发送通知给管理员")
    void shouldSendNotificationToAdminWhenAppealCreated() {
        // Arrange
        Appeal appeal = createTestAppeal(123L, AppealStatus.PENDING);
        AppealCreatedEvent event = new AppealCreatedEvent(this, appeal);

        // Act
        appealEventListener.handleAppealCreated(event);

        // Assert
        ArgumentCaptor<Long> receiverIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<NotificationType> typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificationService, times(1)).sendNotification(
            receiverIdCaptor.capture(),
            typeCaptor.capture(),
            titleCaptor.capture(),
            contentCaptor.capture(),
            eq(appeal.getId()),
            eq("APPEAL"),
            anyString()
        );

        assertThat(typeCaptor.getValue()).isEqualTo(NotificationType.SYSTEM_ANNOUNCEMENT);
        assertThat(titleCaptor.getValue()).contains("新申诉");
    }

    @Test
    @DisplayName("应该在申诉状态变更时通知申诉人")
    void shouldNotifyAppellantWhenStatusChanged() {
        // Arrange
        Appeal appeal = createTestAppeal(123L, AppealStatus.REVIEWING);
        AppealStatusChangedEvent event = new AppealStatusChangedEvent(
            this, appeal, AppealStatus.PENDING, AppealStatus.REVIEWING
        );

        // Act
        appealEventListener.handleStatusChanged(event);

        // Assert
        verify(notificationService, times(1)).sendNotification(
            eq(123L),
            eq(NotificationType.SYSTEM_ANNOUNCEMENT),
            anyString(),
            contains("审核中"),
            eq(appeal.getId()),
            eq("APPEAL"),
            anyString()
        );
    }

    @Test
    @DisplayName("应该在申诉通过时发送详细通知")
    void shouldSendDetailedNotificationWhenAppealApproved() {
        // Arrange
        Appeal appeal = createTestAppeal(123L, AppealStatus.APPROVED);
        appeal.setReviewComment("申诉理由充分，已恢复账户");
        AppealHandledEvent event = new AppealHandledEvent(this, appeal, 999L, "管理员张三");

        // Act
        appealEventListener.handleAppealHandled(event);

        // Assert
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(notificationService, times(1)).sendNotification(
            eq(123L),
            eq(NotificationType.SYSTEM_ANNOUNCEMENT),
            anyString(),
            contentCaptor.capture(),
            eq(appeal.getId()),
            eq("APPEAL"),
            anyString()
        );

        String content = contentCaptor.getValue();
        assertThat(content)
            .contains("通过")
            .contains("申诉理由充分");
    }

    @Test
    @DisplayName("应该在申诉驳回时发送驳回原因")
    void shouldSendRejectionReasonWhenAppealRejected() {
        // Arrange
        Appeal appeal = createTestAppeal(123L, AppealStatus.REJECTED);
        appeal.setReviewComment("证据不足，驳回申诉");
        AppealHandledEvent event = new AppealHandledEvent(this, appeal, 999L, "管理员李四");

        // Act
        appealEventListener.handleAppealHandled(event);

        // Assert
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(notificationService, times(1)).sendNotification(
            eq(123L),
            eq(NotificationType.SYSTEM_ANNOUNCEMENT),
            anyString(),
            contentCaptor.capture(),
            eq(appeal.getId()),
            eq("APPEAL"),
            anyString()
        );

        String content = contentCaptor.getValue();
        assertThat(content)
            .contains("驳回")
            .contains("证据不足");
    }

    @Test
    @DisplayName("应该在申诉处理时通知相关人员")
    void shouldNotifyRelatedPartiesWhenAppealHandled() {
        // Arrange
        Appeal appeal = createTestAppeal(123L, AppealStatus.APPROVED);
        appeal.setTargetType(AppealTargetType.USER_BAN);
        appeal.setTargetId(456L); // 被申诉的用户ID
        
        AppealHandledEvent event = new AppealHandledEvent(this, appeal, 999L, "管理员王五");

        // Act
        appealEventListener.handleAppealHandled(event);

        // Assert - 应该通知申诉人
        verify(notificationService, atLeastOnce()).sendNotification(
            eq(123L),
            any(NotificationType.class),
            anyString(),
            anyString(),
            eq(appeal.getId()),
            eq("APPEAL"),
            anyString()
        );
    }

    @Test
    @DisplayName("监听器应该捕获并记录异常而不中断事件处理")
    void shouldCatchAndLogExceptionsWithoutInterruptingEventProcessing() {
        // Arrange
        Appeal appeal = createTestAppeal(123L, AppealStatus.APPROVED);
        AppealHandledEvent event = new AppealHandledEvent(this, appeal, 999L, "管理员");

        // Mock通知服务抛出异常
        doThrow(new RuntimeException("通知服务异常"))
            .when(notificationService)
            .sendNotification(anyLong(), any(), anyString(), anyString(), anyLong(), anyString(), anyString());

        // Act & Assert - 不应该抛出异常
        assertThatCode(() -> appealEventListener.handleAppealHandled(event))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("应该使用模板渲染通知内容")
    void shouldUseTemplateToRenderNotificationContent() {
        // Arrange
        Appeal appeal = createTestAppeal(123L, AppealStatus.APPROVED);
        appeal.setAppealType(AppealType.UNJUST_BAN);
        AppealHandledEvent event = new AppealHandledEvent(this, appeal, 999L, "管理员赵六");

        // Act
        appealEventListener.handleAppealHandled(event);

        // Assert - 验证通知内容格式
        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(notificationService).sendNotification(
            anyLong(),
            any(NotificationType.class),
            titleCaptor.capture(),
            contentCaptor.capture(),
            anyLong(),
            anyString(),
            anyString()
        );

        assertThat(titleCaptor.getValue()).isNotEmpty();
        assertThat(contentCaptor.getValue()).isNotEmpty();
    }

    // ========== 辅助方法 ==========

    private Appeal createTestAppeal(Long userId, AppealStatus status) {
        Appeal appeal = new Appeal();
        appeal.setId(1000L);
        appeal.setUserId(userId);
        appeal.setTargetType(AppealTargetType.USER_BAN);
        appeal.setTargetId(456L);
        appeal.setAppealType(AppealType.UNJUST_BAN);
        appeal.setReason("我没有违规，请求解封");
        appeal.setStatus(status);
        appeal.setCreatedAt(LocalDateTime.now());
        appeal.setDeadline(LocalDateTime.now().plusDays(7));
        return appeal;
    }
}
