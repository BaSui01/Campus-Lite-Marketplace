package com.campus.marketplace.common.schedule;

import com.campus.marketplace.common.entity.PrivacyRequest;
import com.campus.marketplace.common.entity.Role;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.PrivacyRequestStatus;
import com.campus.marketplace.common.enums.PrivacyRequestType;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.repository.PrivacyRequestRepository;
import com.campus.marketplace.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrivacyCleanupScheduler 测试")
class PrivacyCleanupSchedulerTest {

    @Mock
    private PrivacyRequestRepository privacyRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EntityManager entityManager;

    private PrivacyCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new PrivacyCleanupScheduler(privacyRequestRepository, userRepository, entityManager);
    }

    @Test
    @DisplayName("到期删除请求会软删除用户并标记完成")
    void processScheduledAccountDeletions_shouldSoftDeleteUsers() {
        PrivacyRequest request = PrivacyRequest.builder()
                .userId(1L)
                .type(PrivacyRequestType.DELETE)
                .status(PrivacyRequestStatus.PENDING)
                .scheduledAt(LocalDateTime.now().minusHours(1))
                .build();
        request.setId(10L);
        User user = User.builder()
                .id(1L)
                .username("foo")
                .status(UserStatus.ACTIVE)
                .build();

        when(privacyRequestRepository.findByTypeAndStatusInAndScheduledAtBefore(
                eq(PrivacyRequestType.DELETE), anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(request));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(privacyRequestRepository.save(request)).thenReturn(request);

        scheduler.processScheduledAccountDeletions();

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(request.getStatus()).isEqualTo(PrivacyRequestStatus.COMPLETED);
        assertThat(request.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("过期已删除账号会被物理删除")
    void purgeDeletedAccounts_shouldRemoveUser() {
        User deletedUser = User.builder()
                .id(100L)
                .username("deleted")
                .status(UserStatus.DELETED)
                .deletedAt(LocalDateTime.now().minusDays(40))
                .roles(new java.util.HashSet<>(Set.of(Role.builder().name("ROLE_STUDENT").build())))
                .build();

        when(userRepository.findAll()).thenReturn(List.of(deletedUser));
        when(entityManager.contains(deletedUser)).thenReturn(false);
        when(entityManager.merge(deletedUser)).thenReturn(deletedUser);

        scheduler.purgeDeletedAccounts();

        verify(userRepository).save(deletedUser);
        verify(entityManager).remove(deletedUser);
        assertThat(deletedUser.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("物理删除失败时退化为匿名化处理")
    void purgeDeletedAccounts_shouldAnonymizeWhenRemoveFails() {
        User deletedUser = User.builder()
                .id(101L)
                .username("deleted2")
                .status(UserStatus.DELETED)
                .deletedAt(LocalDateTime.now().minusDays(40))
                .roles(new java.util.HashSet<>())
                .build();

        when(userRepository.findAll()).thenReturn(List.of(deletedUser));
        when(entityManager.contains(deletedUser)).thenReturn(true);
        doThrow(new RuntimeException("FK")).when(entityManager).remove(deletedUser);

        scheduler.purgeDeletedAccounts();

        verify(userRepository, times(2)).save(deletedUser);
        assertThat(deletedUser.getUsername()).startsWith("deleted_");
        assertThat(deletedUser.getPassword()).isEqualTo("{noop}invalid");
        assertThat(deletedUser.getNickname()).isEqualTo("已注销用户");
    }
}
