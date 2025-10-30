package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateSubscriptionRequest;
import com.campus.marketplace.common.dto.response.SubscriptionResponse;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Subscription;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.SubscriptionRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.SubscriptionServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("关键词订阅服务实现测试")
class SubscriptionServiceImplTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("首次订阅创建新记录")
    void subscribe_createNew() {
        User user = new User();
        user.setId(10L);
        user.setCampusId(3L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserIdAndKeywordIgnoreCaseAndCampusId(10L, "macbook", 3L))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription saved = inv.getArgument(0);
            saved.setId(88L);
            return saved;
        });

        Long id = subscriptionService.subscribe(new CreateSubscriptionRequest(" MACBOOK ", null));

        assertThat(id).isEqualTo(88L);
        verify(subscriptionRepository).save(argThat(sub ->
                sub.getKeyword().equals("macbook") && Long.valueOf(3L).equals(sub.getCampusId())));
    }

    @Test
    @DisplayName("重复订阅活跃记录抛冲突")
    void subscribe_duplicateActive() {
        User user = new User();
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        Subscription existing = Subscription.builder()
                .userId(10L)
                .keyword("java")
                .campusId(5L)
                .active(true)
                .build();
        existing.setId(99L);
        when(subscriptionRepository.findByUserIdAndKeywordIgnoreCaseAndCampusId(10L, "java", 5L))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> subscriptionService.subscribe(new CreateSubscriptionRequest("Java", 5L)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.DUPLICATE_RESOURCE.getCode());
    }

    @Test
    @DisplayName("重复订阅非活跃记录重新激活")
    void subscribe_reactivate() {
        User user = new User();
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        Subscription existing = Subscription.builder()
                .userId(10L)
                .keyword("java")
                .campusId(5L)
                .active(false)
                .build();
        existing.setId(77L);
        when(subscriptionRepository.findByUserIdAndKeywordIgnoreCaseAndCampusId(10L, "java", 5L))
                .thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(existing)).thenReturn(existing);

        Long id = subscriptionService.subscribe(new CreateSubscriptionRequest("java", 5L));

        assertThat(id).isEqualTo(77L);
        assertThat(existing.getActive()).isTrue();
        verify(subscriptionRepository).save(existing);
    }

    @Test
    @DisplayName("取消订阅非本人记录抛拒绝")
    void unsubscribe_forbidden() {
        Subscription subscription = Subscription.builder()
                .userId(99L)
                .keyword("macbook")
                .build();
        subscription.setId(55L);
        when(subscriptionRepository.findById(55L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> subscriptionService.unsubscribe(55L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN.getCode());
    }

    @Test
    @DisplayName("取消订阅成功删除记录")
    void unsubscribe_success() {
        Subscription subscription = Subscription.builder()
                .userId(10L)
                .keyword("macbook")
                .build();
        subscription.setId(56L);
        when(subscriptionRepository.findById(56L)).thenReturn(Optional.of(subscription));

        subscriptionService.unsubscribe(56L);

        verify(subscriptionRepository).delete(subscription);
    }

    @Test
    @DisplayName("列出我的订阅返回响应")
    void listMySubscriptions() {
        Subscription subscription = Subscription.builder()
                .userId(10L)
                .keyword("ipad")
                .campusId(2L)
                .active(true)
                .build();
        subscription.setId(11L);
        when(subscriptionRepository.findByUserId(10L)).thenReturn(List.of(subscription));

        List<SubscriptionResponse> responses = subscriptionService.listMySubscriptions();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().keyword()).isEqualTo("ipad");
    }

    @Test
    @DisplayName("通知订阅匹配商品时触发推送")
    void notifySubscribers_match() {
        Subscription sub1 = Subscription.builder()
                .userId(20L)
                .keyword("mac")
                .campusId(3L)
                .active(true)
                .build();
        sub1.setId(1L);
        Subscription sub2 = Subscription.builder()
                .userId(21L)
                .keyword("ipad")
                .campusId(null)
                .active(false)
                .build();
        sub2.setId(2L);
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub1, sub2));
        Goods goods = Goods.builder()
                .title("全新MacBook Pro")
                .description("16G 内存")
                .campusId(3L)
                .build();
        goods.setId(100L);

        subscriptionService.notifySubscribersOnGoodsApproved(goods);

        verify(notificationService).sendNotification(
                eq(20L),
                eq(NotificationType.SUBSCRIPTION_MATCHED_GOODS),
                anyString(),
                contains("MacBook"),
                eq(100L),
                eq("GOODS"),
                eq("/goods/100")
        );
        verify(notificationService, never()).sendNotification(eq(21L), any(), any(), any(), any(), any(), any());
    }
}
