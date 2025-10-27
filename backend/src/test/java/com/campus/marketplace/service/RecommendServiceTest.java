package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.RedisUtil;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.ViewLogRepository;
import com.campus.marketplace.repository.FavoriteRepository;
import com.campus.marketplace.service.impl.RecommendServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("推荐服务测试")
class RecommendServiceTest {

    @Mock
    private GoodsRepository goodsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ViewLogRepository viewLogRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private RedisUtil redis;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock rLock;

    @InjectMocks
    private RecommendServiceImpl recommendService;

    private MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> secMock;

    @BeforeEach
    void setup() {
        org.mockito.Mockito.lenient().when(redissonClient.getLock(anyString())).thenReturn(rLock);
        try {
            org.mockito.Mockito.lenient().when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("热榜-缓存命中")
    void hot_cache_hit() {
        // 缓存返回两个ID
        when(redis.zReverseRange(eq("goods:rank:1"), eq(0L), eq(1L)))
                .thenReturn(new LinkedHashSet<>(Arrays.asList(1L, 2L)));

        Goods g1 = Goods.builder().title("A").price(new BigDecimal("10"))
                .categoryId(1L).sellerId(1L).status(GoodsStatus.APPROVED).build();
        g1.setId(1L);
        Goods g2 = Goods.builder().title("B").price(new BigDecimal("20"))
                .categoryId(1L).sellerId(2L).status(GoodsStatus.APPROVED).build();
        g2.setId(2L);
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(g1));
        when(goodsRepository.findById(2L)).thenReturn(Optional.of(g2));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        var list = recommendService.getHotList(1L, 2);
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(1L);
        assertThat(list.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("热榜-缓存未命中降级到数据库")
    void hot_cache_miss_fallback_db() {
        when(redis.zReverseRange(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptySet());

        Goods g1 = Goods.builder().title("A").price(new BigDecimal("10"))
                .categoryId(1L).sellerId(1L).status(GoodsStatus.APPROVED).viewCount(100).favoriteCount(10).build();
        g1.setId(1L);

        when(goodsRepository.findHotGoodsByCampus(eq(GoodsStatus.APPROVED), eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(g1));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        var list = recommendService.getHotList(1L, 1);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("个性化推荐-未登录抛错")
    void personal_unauthenticated() {
        secMock = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        secMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenThrow(new BusinessException(ErrorCode.UNAUTHORIZED));

        assertThatThrownBy(() -> recommendService.getPersonalRecommendations(10))
                .isInstanceOf(BusinessException.class);

        if (secMock != null) secMock.close();
    }

    @Test
    @DisplayName("个性化推荐-缓存命中")
    void personal_cache_hit() {
        secMock = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        secMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenReturn("u1");
        var user = com.campus.marketplace.common.entity.User.builder().username("u1").build();
        user.setId(9L);
        user.setCampusId(1L);
        when(userRepository.findByUsername("u1")).thenReturn(java.util.Optional.of(user));

        when(redis.lRange("recommend:user:9", 0L, 4L))
                .thenReturn(java.util.List.of(1L, 2L));

        Goods g1 = Goods.builder().title("A").price(new java.math.BigDecimal("10"))
                .categoryId(1L).sellerId(1L).status(GoodsStatus.APPROVED).build();
        g1.setId(1L);
        Goods g2 = Goods.builder().title("B").price(new java.math.BigDecimal("20"))
                .categoryId(1L).sellerId(2L).status(GoodsStatus.APPROVED).build();
        g2.setId(2L);
        when(goodsRepository.findById(1L)).thenReturn(java.util.Optional.of(g1));
        when(goodsRepository.findById(2L)).thenReturn(java.util.Optional.of(g2));
        when(categoryRepository.findById(anyLong())).thenReturn(java.util.Optional.empty());
        when(userRepository.findById(anyLong())).thenReturn(java.util.Optional.empty());

        var list = recommendService.getPersonalRecommendations(5);
        org.assertj.core.api.Assertions.assertThat(list).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(list.get(0).getId()).isEqualTo(1L);
        if (secMock != null) secMock.close();
    }

    @Test
    @DisplayName("个性化推荐-按收藏分类召回，缓存回填")
    void personal_from_favorites() {
        secMock = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        secMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenReturn("u1");
        var user = com.campus.marketplace.common.entity.User.builder().username("u1").build();
        user.setId(9L);
        user.setCampusId(1L);
        when(userRepository.findByUsername("u1")).thenReturn(java.util.Optional.of(user));

        when(redis.lRange("recommend:user:9", 0L, 4L))
                .thenReturn(java.util.List.of());

        when(favoriteRepository.findTopCategoryIdsByUserFavorites(9L))
                .thenReturn(java.util.List.<Object[]>of(new Object[]{1L, 3L}));

        Goods g1 = Goods.builder().title("A").price(new java.math.BigDecimal("10"))
                .categoryId(1L).sellerId(1L).status(GoodsStatus.APPROVED).viewCount(10).favoriteCount(1).build();
        g1.setId(1L);

        when(goodsRepository.findHotGoodsByCampusAndCategories(eq(GoodsStatus.APPROVED), eq(1L), anyList(), any(PageRequest.class)))
                .thenReturn(java.util.List.of(g1));
        when(categoryRepository.findById(anyLong())).thenReturn(java.util.Optional.empty());
        when(userRepository.findById(anyLong())).thenReturn(java.util.Optional.empty());

        var list = recommendService.getPersonalRecommendations(5);
        org.assertj.core.api.Assertions.assertThat(list).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(list.get(0).getId()).isEqualTo(1L);
        verify(redis).delete("recommend:user:9");
        verify(redis, atLeastOnce()).lPush(eq("recommend:user:9"), any());
        verify(redis).expire(eq("recommend:user:9"), anyLong(), any());
        if (secMock != null) secMock.close();
    }
}
