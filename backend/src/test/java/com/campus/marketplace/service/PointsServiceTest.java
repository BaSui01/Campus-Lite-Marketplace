package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.PointsLog;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.PointsType;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.PointsLogRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.PointsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("积分服务测试")
class PointsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointsLogRepository pointsLogRepository;

    @InjectMocks
    private PointsServiceImpl pointsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@campus.edu")
                .status(UserStatus.ACTIVE)
                .points(100)
                .build();
        testUser.setId(1L);
    }

    @Test
    @DisplayName("增加积分成功")
    void addPoints_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(pointsLogRepository.save(any(PointsLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pointsService.addPoints(1L, PointsType.DAILY_LOGIN, "每日登录奖励");

        verify(userRepository).save(argThat(user ->
                user.getPoints().equals(105)
        ));
        verify(pointsLogRepository).save(argThat(log ->
                log.getUserId().equals(1L) &&
                log.getType() == PointsType.DAILY_LOGIN &&
                log.getPoints().equals(5) &&
                log.getBalance().equals(105)
        ));
    }

    @Test
    @DisplayName("扣除积分成功")
    void deductPoints_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(pointsLogRepository.save(any(PointsLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pointsService.deductPoints(1L, 20, "兑换优惠券");

        verify(userRepository).save(argThat(user ->
                user.getPoints().equals(80)
        ));
        verify(pointsLogRepository).save(argThat(log ->
                log.getPoints().equals(-20) &&
                log.getBalance().equals(80)
        ));
    }

    @Test
    @DisplayName("扣除积分失败 - 积分不足")
    void deductPoints_Fail_InsufficientPoints() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> pointsService.deductPoints(1L, 200, "兑换"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.POINTS_INSUFFICIENT.getCode());
    }

    @Test
    @DisplayName("查询当前积分")
    void getCurrentPoints_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Integer points = pointsService.getCurrentPoints(1L);

        assertThat(points).isEqualTo(100);
    }
}
