package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.NotificationResponse;
import com.campus.marketplace.common.entity.Notification;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.NotificationStatus;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.NotificationRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 通知服务测试类 - TDD 红灯先行！
 *
 * @author BaSui 😎
 * @since 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("通知服务测试")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private NotificationPreferenceService preferenceService;

    @Mock
    private WebPushService webPushService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .status(UserStatus.ACTIVE)
                .build();

        testNotification = Notification.builder()
                .id(100L)
                .receiverId(1L)
                .type(NotificationType.ORDER_PAID)
                .title("订单支付成功")
                .content("您的订单 #123456 已支付成功")
                .relatedId(123456L)
                .relatedType("order")
                .link("/orders/123456")
                .status(NotificationStatus.UNREAD)
                .emailSent(false)
                .createdAt(LocalDateTime.now())
                .build();

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 默认偏好：全部开启，非静默，未退订
        lenient().when(preferenceService.isChannelEnabled(anyLong(), any())).thenReturn(true);
        lenient().when(preferenceService.isInQuietHours(anyLong(), any(), any())).thenReturn(false);
        lenient().when(preferenceService.isUnsubscribed(anyLong(), anyString(), any())).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    @DisplayName("发送站内通知成功")
    void sendNotification_Success() {
        // 🎯 准备
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // 🚀 执行
        notificationService.sendNotification(
                1L,
                NotificationType.ORDER_PAID,
                "订单支付成功",
                "您的订单 #123456 已支付成功",
                123456L,
                "order",
                "/orders/123456"
        );

        // ✅ 验证
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification savedNotification = captor.getValue();
        assertThat(savedNotification.getReceiverId()).isEqualTo(1L);
        assertThat(savedNotification.getType()).isEqualTo(NotificationType.ORDER_PAID);
        assertThat(savedNotification.getTitle()).isEqualTo("订单支付成功");
        assertThat(savedNotification.getStatus()).isEqualTo(NotificationStatus.UNREAD);

        // 验证 Redis 未读数增加
        verify(valueOperations).increment("notification:unread:" + 1L);
    }

    @Test
    @DisplayName("发送邮件通知成功（异步）")
    void sendEmailNotification_Success() {
        // 🎯 准备
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 🚀 执行
        notificationService.sendEmailNotification(
                1L,
                "订单支付成功",
                "您的订单 #123456 已支付成功"
        );

        // ✅ 验证邮件发送
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentEmail = captor.getValue();
        assertThat(sentEmail.getTo()).containsExactly("test@example.com");
        assertThat(sentEmail.getSubject()).isEqualTo("订单支付成功");
        assertThat(sentEmail.getText()).isEqualTo("您的订单 #123456 已支付成功");
    }

    @Test
    @DisplayName("查询通知列表成功")
    void listNotifications_Success() {
        // 🎯 准备
        Notification notification2 = Notification.builder()
                .id(101L)
                .receiverId(1L)
                .type(NotificationType.GOODS_APPROVED)
                .title("商品审核通过")
                .content("您的商品已审核通过")
                .status(NotificationStatus.READ)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        Page<Notification> page = new PageImpl<>(
                Arrays.asList(testNotification, notification2),
                PageRequest.of(0, 10),
                2
        );

        when(notificationRepository.findByReceiverIdAndStatus(eq(1L), isNull(), any(Pageable.class)))
                .thenReturn(page);

        // 🚀 执行
        Page<NotificationResponse> result = notificationService.listNotifications(null, PageRequest.of(0, 10));

        // ✅ 验证
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(100L);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("订单支付成功");
    }

    @Test
    @DisplayName("查询未读通知数量成功")
    void getUnreadCount_Success() {
        // 🎯 准备
        when(valueOperations.get("notification:unread:" + 1L)).thenReturn(5L);

        // 🚀 执行
        long count = notificationService.getUnreadCount();

        // ✅ 验证
        assertThat(count).isEqualTo(5L);
        verify(valueOperations).get("notification:unread:" + 1L);
    }

    @Test
    @DisplayName("标记通知为已读成功")
    void markAsRead_Success() {
        // 🎯 准备
        List<Long> notificationIds = Arrays.asList(100L, 101L);
        when(notificationRepository.markAsRead(1L, notificationIds)).thenReturn(2);

        // 🚀 执行
        notificationService.markAsRead(notificationIds);

        // ✅ 验证
        verify(notificationRepository).markAsRead(1L, notificationIds);
        verify(valueOperations).decrement("notification:unread:" + 1L, 2);
    }

    @Test
    @DisplayName("全部标记为已读成功")
    void markAllAsRead_Success() {
        // 🎯 准备
        when(notificationRepository.markAllAsRead(1L)).thenReturn(5);

        // 🚀 执行
        notificationService.markAllAsRead();

        // ✅ 验证
        verify(notificationRepository).markAllAsRead(1L);
        verify(redisTemplate).delete("notification:unread:" + 1L);
    }

    @Test
    @DisplayName("删除通知成功")
    void deleteNotifications_Success() {
        // 🎯 准备
        List<Long> notificationIds = Arrays.asList(100L, 101L);
        when(notificationRepository.deleteByIds(1L, notificationIds)).thenReturn(2);

        // 🚀 执行
        notificationService.deleteNotifications(notificationIds);

        // ✅ 验证
        verify(notificationRepository).deleteByIds(1L, notificationIds);
    }

    @Test
    @DisplayName("同时发送站内通知和邮件通知")
    void sendNotificationWithEmail_Success() {
        // 🎯 准备
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 🚀 执行
        notificationService.sendNotificationWithEmail(
                1L,
                NotificationType.ORDER_PAID,
                "订单支付成功",
                "您的订单 #123456 已支付成功",
                123456L,
                "order",
                "/orders/123456"
        );

        // ✅ 验证站内通知
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getTitle()).isEqualTo("订单支付成功");
        assertThat(notificationCaptor.getValue().getEmailSent()).isTrue();

        // ✅ 验证邮件发送
        ArgumentCaptor<SimpleMailMessage> emailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(emailCaptor.capture());
        assertThat(emailCaptor.getValue().getTo()).containsExactly("test@example.com");
    }
}
