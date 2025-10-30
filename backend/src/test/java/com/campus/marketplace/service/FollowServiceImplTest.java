package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.FollowResponse;
import com.campus.marketplace.common.entity.Follow;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.FollowRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.FollowServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("关注服务实现测试")
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FollowServiceImpl followService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilMock.close();
    }

    @Test
    @DisplayName("关注卖家成功会写入关注关系")
    void followSeller_success() {
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
        User seller = User.builder().username("seller").build();
        seller.setId(20L);

        when(userRepository.findById(20L)).thenReturn(Optional.of(seller));
        when(followRepository.findByFollowerIdAndSellerId(10L, 20L)).thenReturn(Optional.empty());

        followService.followSeller(20L);

        verify(followRepository).save(argThat(follow ->
                follow.getFollowerId().equals(10L) && follow.getSellerId().equals(20L)));
    }

    @Test
    @DisplayName("关注自己会抛出业务异常")
    void followSeller_self() {
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(10L);

        assertThatThrownBy(() -> followService.followSeller(10L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode());
    }

    @Test
    @DisplayName("关注卖家但已存在记录会提示重复")
    void followSeller_duplicate() {
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
        when(userRepository.findById(20L)).thenReturn(Optional.of(new User()));
        Follow existing = Follow.builder().followerId(10L).sellerId(20L).build();
        when(followRepository.findByFollowerIdAndSellerId(10L, 20L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> followService.followSeller(20L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.DUPLICATE_RESOURCE.getCode());
    }

    @Test
    @DisplayName("取消关注成功会删除记录")
    void unfollowSeller_success() {
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
        Follow follow = Follow.builder().followerId(10L).sellerId(20L).build();
        when(followRepository.findByFollowerIdAndSellerId(10L, 20L)).thenReturn(Optional.of(follow));

        followService.unfollowSeller(20L);

        verify(followRepository).delete(follow);
    }

    @Test
    @DisplayName("取消关注不存在的记录会失败")
    void unfollowSeller_notFound() {
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
        when(followRepository.findByFollowerIdAndSellerId(10L, 20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.unfollowSeller(20L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FOLLOW_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("查询关注列表会填充卖家信息")
    void listFollowings_returnsSellerInfo() {
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
        Follow follow = Follow.builder()
                .followerId(10L)
                .sellerId(20L)
                .build();
        follow.setCreatedAt(LocalDateTime.now());
        when(followRepository.findByFollowerId(10L)).thenReturn(List.of(follow));

        User seller = User.builder().username("seller").avatar("/avatar.png").build();
        seller.setId(20L);
        when(userRepository.findById(20L)).thenReturn(Optional.of(seller));

        List<FollowResponse> responses = followService.listFollowings();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().sellerName()).isEqualTo("seller");
    }

    @Test
    @DisplayName("关注卖家时卖家不存在抛出异常")
    void followSeller_sellerNotFound() {
        securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(10L);
        when(userRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.followSeller(20L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.USER_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("商品审核通过会通知所有关注者")
    void notifyFollowersOnGoodsApproved_sendsNotifications() {
        Goods goods = Goods.builder().title("MacBook Air").sellerId(20L).build();
        goods.setId(88L);
        Follow f1 = Follow.builder().followerId(1L).sellerId(20L).build();
        Follow f2 = Follow.builder().followerId(2L).sellerId(20L).build();

        when(followRepository.findBySellerId(20L)).thenReturn(List.of(f1, f2));

        followService.notifyFollowersOnGoodsApproved(goods);

        verify(notificationService, times(2)).sendNotification(
                anyLong(),
                eq(NotificationType.FOLLOW_SELLER_NEW_GOODS),
                anyString(),
                contains("MacBook Air"),
                eq(88L),
                eq("GOODS"),
                eq("/goods/88")
        );
    }

    @Test
    @DisplayName("没有关注者时不会触发通知")
    void notifyFollowersOnGoodsApproved_noFollowers() {
        Goods goods = Goods.builder().title("MacBook Pro").sellerId(20L).build();
        goods.setId(99L);
        when(followRepository.findBySellerId(20L)).thenReturn(List.of());

        followService.notifyFollowersOnGoodsApproved(goods);

        verifyNoInteractions(notificationService);
    }
}
