package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateGoodsRequest;
import com.campus.marketplace.common.dto.response.GoodsDetailResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.common.utils.SensitiveWordFilter;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.GoodsServiceImpl;
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
 * 物品服务单元测试
 * 
 * 测试物品发布、查询、审核等功能
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("物品服务测试")
class GoodsServiceTest {

    @Mock
    private GoodsRepository goodsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SensitiveWordFilter sensitiveWordFilter;

    @Mock
    private MessageService messageService;

    @Mock
    private com.campus.marketplace.repository.TagRepository tagRepository;

    @Mock
    private com.campus.marketplace.repository.GoodsTagRepository goodsTagRepository;

    @Mock
    private FollowService followService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private com.campus.marketplace.common.utils.EncryptUtil encryptUtil;

    @InjectMocks
    private GoodsServiceImpl goodsService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User testUser;
    private Category testCategory;
    private CreateGoodsRequest validRequest;

    @BeforeEach
    void setUp() {
        // Mock SecurityUtil 静态方法
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
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

        // 准备测试请求
        validRequest = new CreateGoodsRequest(
                "iPhone 13 Pro",
                "九成新，无划痕，配件齐全",
                new BigDecimal("4999.00"),
                1L,
                List.of("image1.jpg", "image2.jpg"),
                java.util.List.of()
        );

        // 默认返回空标签绑定
        org.mockito.Mockito.lenient().when(goodsTagRepository.findByGoodsIdIn(any())).thenReturn(java.util.List.of());
        org.mockito.Mockito.lenient().when(goodsTagRepository.findByGoodsId(anyLong())).thenReturn(java.util.List.of());
        org.mockito.Mockito.lenient().when(goodsTagRepository.findGoodsIdsByAllTagIds(anyCollection(), anyLong())).thenReturn(java.util.List.of());
        org.mockito.Mockito.lenient().when(tagRepository.findAllById(anyCollection())).thenReturn(java.util.List.of());
        org.mockito.Mockito.lenient().when(encryptUtil.maskEmail(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        // 关闭 SecurityUtil Mock
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("发布物品成功 - 所有条件满足")
    void createGoods_Success_WhenAllConditionsMet() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(sensitiveWordFilter.filter(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(goodsRepository.save(any(Goods.class))).thenAnswer(invocation -> {
            Goods goods = invocation.getArgument(0);
            goods.setId(1L);
            return goods;
        });

        // When
        Long goodsId = goodsService.createGoods(validRequest);

        // Then
        assertThat(goodsId).isEqualTo(1L);
        verify(userRepository).findByUsername(anyString());
        verify(categoryRepository).existsById(1L);
        verify(sensitiveWordFilter, times(2)).filter(anyString());
        verify(goodsRepository).save(argThat(goods ->
                goods.getTitle().equals("iPhone 13 Pro") &&
                goods.getStatus() == GoodsStatus.PENDING &&
                goods.getViewCount() == 0 &&
                goods.getFavoriteCount() == 0
        ));
    }

    @Test
    @DisplayName("发布物品失败 - 分类不存在")
    void createGoods_Fail_WhenCategoryNotFound() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> goodsService.createGoods(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CATEGORY_NOT_FOUND.getCode());

        verify(goodsRepository, never()).save(any(Goods.class));
    }

    @Test
    @DisplayName("发布物品成功 - 验证敏感词过滤")
    void createGoods_Success_WithSensitiveWordFiltering() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(sensitiveWordFilter.filter("iPhone 13 Pro")).thenReturn("iPhone 13 Pro");
        when(sensitiveWordFilter.filter("九成新，无划痕，配件齐全")).thenReturn("九成新，无划痕，配件齐全");
        when(goodsRepository.save(any(Goods.class))).thenAnswer(invocation -> {
            Goods goods = invocation.getArgument(0);
            goods.setId(1L);
            return goods;
        });

        // When
        goodsService.createGoods(validRequest);

        // Then
        verify(sensitiveWordFilter).filter("iPhone 13 Pro");
        verify(sensitiveWordFilter).filter("九成新，无划痕，配件齐全");
    }

    @Test
    @DisplayName("查询物品列表成功 - 返回已审核物品")
    void listGoods_Success_ReturnsApprovedGoods() {
        // Given
        Goods goods1 = createTestGoods(1L, "商品1", GoodsStatus.APPROVED);
        Goods goods2 = createTestGoods(2L, "商品2", GoodsStatus.APPROVED);
        Page<Goods> goodsPage = new PageImpl<>(List.of(goods1, goods2));

        when(goodsRepository.findByConditionsWithCampus(
                eq(GoodsStatus.APPROVED), any(), any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(goodsPage);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Page<GoodsResponse> result = goodsService.listGoods(
                null, null, null, null, 0, 20, "createdAt", "DESC", null
        );

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("商品1");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("商品2");
        verify(goodsRepository).findByConditionsWithCampus(
                eq(GoodsStatus.APPROVED), any(), any(), any(), any(), any(), any(Pageable.class)
        );
    }

    @Test
    @DisplayName("查询物品详情成功 - 增加浏览量")
    void getGoodsDetail_Success_IncrementsViewCount() {
        // Given
        Goods goods = createTestGoods(1L, "测试商品", GoodsStatus.APPROVED);
        goods.setSeller(testUser);
        goods.setCategory(testCategory);
        int initialViewCount = goods.getViewCount();

        when(goodsRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(goods));
        when(goodsRepository.save(any(Goods.class))).thenReturn(goods);

        // When
        GoodsDetailResponse response = goodsService.getGoodsDetail(1L);

        // Then
        assertThat(response.getTitle()).isEqualTo("测试商品");
        verify(goodsRepository).save(argThat(g -> g.getViewCount() == initialViewCount + 1));
    }

    @Test
    @DisplayName("查询物品详情失败 - 物品不存在")
    void getGoodsDetail_Fail_WhenGoodsNotFound() {
        // Given
        when(goodsRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> goodsService.getGoodsDetail(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GOODS_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("审核物品成功 - 通过审核")
    void approveGoods_Success_Approved() {
        // Given
        Goods goods = createTestGoods(1L, "待审核商品", GoodsStatus.PENDING);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(goods));
        when(goodsRepository.save(any(Goods.class))).thenReturn(goods);

        // When
        goodsService.approveGoods(1L, true, null);

        // Then
        verify(goodsRepository).save(argThat(g -> g.getStatus() == GoodsStatus.APPROVED));
        verify(followService).notifyFollowersOnGoodsApproved(goods);
        verify(subscriptionService).notifySubscribersOnGoodsApproved(goods);
    }

    @Test
    @DisplayName("审核物品成功 - 拒绝审核")
    void approveGoods_Success_Rejected() {
        // Given
        Goods goods = createTestGoods(1L, "待审核商品", GoodsStatus.PENDING);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(goods));
        when(goodsRepository.save(any(Goods.class))).thenReturn(goods);

        // When
        goodsService.approveGoods(1L, false, "内容不符合规范");

        // Then
        verify(goodsRepository).save(argThat(g -> g.getStatus() == GoodsStatus.REJECTED));
        verify(followService, never()).notifyFollowersOnGoodsApproved(any());
        verify(subscriptionService, never()).notifySubscribersOnGoodsApproved(any());
    }

    @Test
    @DisplayName("审核物品失败 - 物品已审核")
    void approveGoods_Fail_WhenAlreadyApproved() {
        // Given
        Goods goods = createTestGoods(1L, "已审核商品", GoodsStatus.APPROVED);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(goods));

        // When & Then
        assertThatThrownBy(() -> goodsService.approveGoods(1L, true, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode());

        verify(goodsRepository, never()).save(any(Goods.class));
    }

    /**
     * 创建测试物品
     */
    private Goods createTestGoods(Long id, String title, GoodsStatus status) {
        Goods goods = Goods.builder()
                .title(title)
                .description("测试描述")
                .price(new BigDecimal("100.00"))
                .categoryId(1L)
                .sellerId(1L)
                .status(status)
                .viewCount(0)
                .favoriteCount(0)
                .images(new String[]{"image1.jpg"})
                .build();
        goods.setId(id);
        return goods;
    }
}
