package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.NotificationPreference;
import com.campus.marketplace.common.entity.NotificationUnsubscribe;
import com.campus.marketplace.common.enums.NotificationChannel;
import com.campus.marketplace.repository.NotificationPreferenceRepository;
import com.campus.marketplace.repository.NotificationUnsubscribeRepository;
import com.campus.marketplace.service.impl.NotificationPreferenceServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("通知偏好服务测试：偏好/退订/静默时段")
class NotificationPreferenceServiceImplTest {

    @Mock
    NotificationPreferenceRepository prefRepo;
    @Mock
    NotificationUnsubscribeRepository unsubRepo;

    @InjectMocks
    NotificationPreferenceServiceImpl service;

    @Test
    @DisplayName("isChannelEnabled: 默认开启，关闭后为 false")
    void isChannelEnabled_toggle() {
        when(prefRepo.findByUserIdAndChannel(1L, NotificationChannel.EMAIL))
                .thenReturn(Optional.of(NotificationPreference.builder().userId(1L).channel(NotificationChannel.EMAIL).enabled(false).build()));
        assertThat(service.isChannelEnabled(1L, NotificationChannel.EMAIL)).isFalse();
    }

    @Test
    @DisplayName("isInQuietHours: 命中静默时段")
    void isInQuietHours_hit() {
        LocalTime start = LocalTime.of(22, 0);
        LocalTime end = LocalTime.of(7, 0); // 跨午夜
        when(prefRepo.findByUserIdAndChannel(1L, NotificationChannel.EMAIL))
                .thenReturn(Optional.of(NotificationPreference.builder().userId(1L).channel(NotificationChannel.EMAIL).enabled(true).quietStart(start).quietEnd(end).build()));
        assertThat(service.isInQuietHours(1L, NotificationChannel.EMAIL, LocalTime.of(23, 0))).isTrue();
        assertThat(service.isInQuietHours(1L, NotificationChannel.EMAIL, LocalTime.of(6, 30))).isTrue();
        assertThat(service.isInQuietHours(1L, NotificationChannel.EMAIL, LocalTime.of(12, 0))).isFalse();
    }

    @Test
    @DisplayName("unsubscribe/resubscribe: 退订后命中，再次订阅后不命中")
    void unsubscribe_and_resubscribe() {
        when(unsubRepo.existsByUserIdAndTemplateCodeAndChannel(1L, "ORDER_PAID", NotificationChannel.EMAIL))
                .thenReturn(false)
                .thenReturn(true)
                .thenReturn(false);
        service.unsubscribe(1L, "ORDER_PAID", NotificationChannel.EMAIL);
        verify(unsubRepo).save(any(NotificationUnsubscribe.class));
        assertThat(service.isUnsubscribed(1L, "ORDER_PAID", NotificationChannel.EMAIL)).isTrue();
        service.resubscribe(1L, "ORDER_PAID", NotificationChannel.EMAIL);
        verify(unsubRepo).deleteByUserIdAndTemplateCodeAndChannel(1L, "ORDER_PAID", NotificationChannel.EMAIL);
        assertThat(service.isUnsubscribed(1L, "ORDER_PAID", NotificationChannel.EMAIL)).isFalse();
    }
}
