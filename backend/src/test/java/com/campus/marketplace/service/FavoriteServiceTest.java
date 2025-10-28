package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Favorite;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.FavoriteRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.FavoriteServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mockStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 收藏服务单元测试
 * 
 * 测试物品收藏、取消收藏、查询收藏列表等功能
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("收藏服务测试")
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private GoodsRepository goodsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> securityUtilMock;
    private User testUser;
    private Goods testGoods;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Mock SecurityUtil 静态方法
        securityUtilMock = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        securityUtilMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenReturn("testuser");
        // 准备测试用户
        testUser = User.builder()
                .username("testuser")
                .email("test@campus.edu")
                .status(UserStatus.ACTIVE)
                .points(100)
                .build();
        testUser.setId(1L);

        // 准备测试分类
        testCategory = Category.builder()
                .name("电子产品")
                .description("电子产品分类")
                .build();
        testCategory.setId(1L);

        // 准备测试物品
        testGoods = Goods.builder()
                .title("iPhone 13 Pro")
                .description("九成新，无划痕")
                .price(new BigDecimal("4999.00"))
                .categoryId(1L)
                .sellerId(2L)
                .status(GoodsStatus.APPROVED)
                .viewCount(10)
                .favoriteCount(5)
                .images(new String[]{"image1.jpg"})
                .build();
        testGoods.setId(1L);
    }

    @AfterEach
    void tearDown() {
        // 关闭 SecurityUtil Mock
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("添加收藏成功 - 所有条件满足")
    void addFavorite_Success_WhenAllConditionsMet() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        when(favoriteRepository.existsByUserIdAndGoodsId(1L, 1L)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(goodsRepository.save(any(Goods.class))).thenReturn(testGoods);

        // When
        favoriteService.addFavorite(1L);

        // Then
        verify(favoriteRepository).save(argThat(favorite ->
                favorite.getUserId().equals(1L) &&
                favorite.getGoodsId().equals(1L)
        ));
        verify(goodsRepository).save(argThat(goods ->
                goods.getFavoriteCount() == 6  // 原来是 5，增加 1
        ));
    }

    @Test
    @DisplayName("添加收藏失败 - 跨校收藏无权限")
    void addFavorite_Fail_CrossCampusWithoutAuthority() {
        testUser.setCampusId(1L);
        testGoods.setCampusId(2L);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        // 默认 hasAuthority 返回 false

        assertThatThrownBy(() -> favoriteService.addFavorite(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN.getCode())
                .hasMessageContaining("跨校区收藏被禁止");
    }

    @Test
    @DisplayName("添加收藏失败 - 物品未审核")
    void addFavorite_Fail_WhenGoodsNotApproved() {
        // Given
        testGoods.setStatus(GoodsStatus.PENDING);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));

        // When & Then
        assertThatThrownBy(() -> favoriteService.addFavorite(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GOODS_NOT_APPROVED.getCode());

        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("添加收藏失败 - 已收藏")
    void addFavorite_Fail_WhenAlreadyFavorited() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        when(favoriteRepository.existsByUserIdAndGoodsId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> favoriteService.addFavorite(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FAVORITE_EXISTS.getCode());

        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    @DisplayName("取消收藏成功 - 减少收藏数")
    void removeFavorite_Success_DecrementsFavoriteCount() {
        // Given
        Favorite favorite = Favorite.builder()
                .userId(1L)
                .goodsId(1L)
                .build();
        favorite.setId(1L);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(favoriteRepository.findByUserIdAndGoodsId(1L, 1L)).thenReturn(Optional.of(favorite));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        when(goodsRepository.save(any(Goods.class))).thenReturn(testGoods);

        // When
        favoriteService.removeFavorite(1L);

        // Then
        verify(favoriteRepository).delete(favorite);
        verify(goodsRepository).save(argThat(goods ->
                goods.getFavoriteCount() == 4  // 原来是 5，减少 1
        ));
    }

    @Test
    @DisplayName("取消收藏失败 - 收藏不存在")
    void removeFavorite_Fail_WhenFavoriteNotFound() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(favoriteRepository.findByUserIdAndGoodsId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> favoriteService.removeFavorite(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FAVORITE_NOT_FOUND.getCode());

        verify(favoriteRepository, never()).delete(any(Favorite.class));
    }

    @Test
    @DisplayName("查询收藏列表成功 - 返回收藏的物品")
    void listFavorites_Success_ReturnsFavoritedGoods() {
        // Given
        Favorite favorite1 = createTestFavorite(1L, 1L, 1L);
        favorite1.setGoods(testGoods);
        
        Page<Favorite> favoritePage = new PageImpl<>(List.of(favorite1));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(favoriteRepository.findByUserIdWithGoods(eq(1L), any(Pageable.class)))
                .thenReturn(favoritePage);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));

        // When
        Page<GoodsResponse> result = favoriteService.listFavorites(0, 20);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("iPhone 13 Pro");
        verify(favoriteRepository).findByUserIdWithGoods(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("检查是否已收藏 - 已收藏返回 true")
    void isFavorited_ReturnsTrue_WhenFavorited() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(favoriteRepository.existsByUserIdAndGoodsId(1L, 1L)).thenReturn(true);

        // When
        boolean result = favoriteService.isFavorited(1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查是否已收藏 - 未收藏返回 false")
    void isFavorited_ReturnsFalse_WhenNotFavorited() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(favoriteRepository.existsByUserIdAndGoodsId(1L, 1L)).thenReturn(false);

        // When
        boolean result = favoriteService.isFavorited(1L);

        // Then
        assertThat(result).isFalse();
    }

    /**
     * 创建测试收藏
     */
    private Favorite createTestFavorite(Long id, Long userId, Long goodsId) {
        Favorite favorite = Favorite.builder()
                .userId(userId)
                .goodsId(goodsId)
                .build();
        favorite.setId(id);
        return favorite;
    }
}
