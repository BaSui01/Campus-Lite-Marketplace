package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.BanUserRequest;
import com.campus.marketplace.common.entity.BanLog;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.BanLogRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户封禁功能测试")
class UserBanServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BanLogRepository banLogRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> securityUtilMock;
    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        securityUtilMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenReturn("admin");

        testUser = User.builder()
                .username("testuser")
                .email("test@campus.edu")
                .status(UserStatus.ACTIVE)
                .points(100)
                .build();
        testUser.setId(1L);

        testAdmin = User.builder()
                .username("admin")
                .email("admin@campus.edu")
                .status(UserStatus.ACTIVE)
                .build();
        testAdmin.setId(999L);
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("封禁用户成功 - 临时封禁")
    void banUser_Success_Temporary() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(banLogRepository.save(any(BanLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BanUserRequest request = new BanUserRequest(1L, "违规发布内容", 7);
        userService.banUser(request);

        verify(userRepository).save(argThat(user ->
                user.getStatus() == UserStatus.BANNED
        ));
        verify(banLogRepository).save(argThat(log ->
                log.getUserId().equals(1L) &&
                log.getAdminId().equals(999L) &&
                log.getDays().equals(7) &&
                log.getUnbanTime() != null &&
                !log.getIsUnbanned()
        ));
    }

    @Test
    @DisplayName("封禁用户成功 - 永久封禁")
    void banUser_Success_Permanent() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(banLogRepository.save(any(BanLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BanUserRequest request = new BanUserRequest(1L, "严重违规", 0);
        userService.banUser(request);

        verify(banLogRepository).save(argThat(log ->
                log.getDays().equals(0) &&
                log.getUnbanTime() == null
        ));
    }

    @Test
    @DisplayName("封禁用户失败 - 用户不存在")
    void banUser_Fail_UserNotFound() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        BanUserRequest request = new BanUserRequest(1L, "违规", 7);

        assertThatThrownBy(() -> userService.banUser(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("解封用户成功")
    void unbanUser_Success() {
        testUser.setStatus(UserStatus.BANNED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.unbanUser(1L);

        verify(userRepository).save(argThat(user ->
                user.getStatus() == UserStatus.ACTIVE
        ));
    }

    @Test
    @DisplayName("自动解封过期用户")
    void autoUnbanExpiredUsers_Success() {
        User bannedUser1 = User.builder()
                .username("user1")
                .status(UserStatus.BANNED)
                .build();
        bannedUser1.setId(1L);

        User bannedUser2 = User.builder()
                .username("user2")
                .status(UserStatus.BANNED)
                .build();
        bannedUser2.setId(2L);

        BanLog log1 = BanLog.builder()
                .userId(1L)
                .adminId(999L)
                .reason("test")
                .days(7)
                .unbanTime(LocalDateTime.now().minusDays(1))
                .isUnbanned(false)
                .build();

        BanLog log2 = BanLog.builder()
                .userId(2L)
                .adminId(999L)
                .reason("test")
                .days(7)
                .unbanTime(LocalDateTime.now().minusDays(1))
                .isUnbanned(false)
                .build();

        when(banLogRepository.findExpiredBans(any(LocalDateTime.class)))
                .thenReturn(List.of(log1, log2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(bannedUser1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bannedUser2));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(banLogRepository.save(any(BanLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int count = userService.autoUnbanExpiredUsers();

        assertThat(count).isEqualTo(2);
        verify(userRepository, times(2)).save(argThat(user ->
                user.getStatus() == UserStatus.ACTIVE
        ));
        verify(banLogRepository, times(2)).save(argThat(log ->
                log.getIsUnbanned()
        ));
    }
}
