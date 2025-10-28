package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.UserProfileResponse;
import com.campus.marketplace.common.entity.Blacklist;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.RedisUtil;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.BlacklistRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.BlacklistServiceImpl;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("黑名单服务实现测试")
class BlacklistServiceImplTest {

    @Mock private BlacklistRepository blacklistRepository;
    @Mock private UserRepository userRepository;
    @Mock private RedisUtil redisUtil;

    @InjectMocks
    private BlacklistServiceImpl blacklistService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        securityUtilMock = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUsername).thenReturn("alice");
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("添加黑名单成功写入缓存")
    void addToBlacklist_success() {
        User current = user(1L, "alice");
        User target = user(2L, "bob");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(current));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(blacklistRepository.existsByUserIdAndBlockedUserId(1L, 2L)).thenReturn(false);

        blacklistService.addToBlacklist(2L, "spam");

        verify(blacklistRepository).save(argThat(bl -> bl.getUserId().equals(1L) && bl.getBlockedUserId().equals(2L)));
        verify(redisUtil).sAdd("blacklist:1", 2L);
        verify(redisUtil).expire(eq("blacklist:1"), eq(7L), eq(java.util.concurrent.TimeUnit.DAYS));
    }

    @Test
    @DisplayName("添加黑名单不能拉黑自己")
    void addToBlacklist_self() {
        User current = user(1L, "alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(current));

        assertThatThrownBy(() -> blacklistService.addToBlacklist(1L, ""))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_PARAM.getCode());
    }

    @Test
    @DisplayName("移除黑名单成功更新缓存")
    void removeFromBlacklist_success() {
        User current = user(1L, "alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(current));
        when(blacklistRepository.existsByUserIdAndBlockedUserId(1L, 2L)).thenReturn(true);

        blacklistService.removeFromBlacklist(2L);

        verify(blacklistRepository).deleteByUserIdAndBlockedUserId(1L, 2L);
        verify(redisUtil).sRemove("blacklist:1", 2L);
    }

    @Test
    @DisplayName("移除黑名单不存在记录抛异常")
    void removeFromBlacklist_notExists() {
        User current = user(1L, "alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(current));
        when(blacklistRepository.existsByUserIdAndBlockedUserId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> blacklistService.removeFromBlacklist(2L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode());
    }

    @Test
    @DisplayName("查询黑名单返回用户档案")
    void listBlacklist_success() {
        User current = user(1L, "alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(current));
        Blacklist record = Blacklist.builder().userId(1L).blockedUserId(2L).build();
        when(blacklistRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(record)));
        User blocked = user(2L, "bob");
        blocked.setAvatar("avatar.png");
        when(userRepository.findById(2L)).thenReturn(Optional.of(blocked));

        Page<UserProfileResponse> page = blacklistService.listBlacklist(0, 10);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getUsername()).isEqualTo("bob");
    }

    @Test
    @DisplayName("Redis 缓存命中直接返回拉黑状态")
    void isBlocked_cacheHit() {
        User current = user(1L, "alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(current));
        when(redisUtil.sIsMember("blacklist:1", 3L)).thenReturn(true);

        assertThat(blacklistService.isBlocked(3L)).isTrue();
        verify(blacklistRepository, never()).existsByUserIdAndBlockedUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Redis 未命中则回源数据库并写缓存")
    void isBlocked_cacheMiss() {
        User current = user(1L, "alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(current));
        when(redisUtil.sIsMember("blacklist:1", 3L)).thenReturn(false);
        when(blacklistRepository.existsByUserIdAndBlockedUserId(1L, 3L)).thenReturn(true);

        assertThat(blacklistService.isBlocked(3L)).isTrue();
        verify(redisUtil).sAdd("blacklist:1", 3L);
        verify(redisUtil).expire("blacklist:1", 7, java.util.concurrent.TimeUnit.DAYS);
    }

    @Test
    @DisplayName("双向拉黑检查使用仓库")
    void isBlockedBetween() {
        when(blacklistRepository.existsMutualBlock(1L, 2L)).thenReturn(true);

        assertThat(blacklistService.isBlockedBetween(1L, 2L)).isTrue();
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
