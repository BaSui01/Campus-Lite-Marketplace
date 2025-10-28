package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.GoodsTagRepository;
import com.campus.marketplace.repository.PostRepository;
import com.campus.marketplace.repository.SearchLogRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.projection.GoodsSearchProjection;
import com.campus.marketplace.repository.projection.PostSearchProjection;
import com.campus.marketplace.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("搜索服务实现测试")
class SearchServiceImplTest {

    @Mock private GoodsRepository goodsRepository;
    @Mock private GoodsTagRepository goodsTagRepository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private SearchLogRepository searchLogRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::isAuthenticated).thenReturn(true);
        securityUtilMock.when(() -> SecurityUtil.hasAuthority("system:campus:cross")).thenReturn(false);
        securityUtilMock.when(SecurityUtil::getCurrentUsername).thenReturn("tester");
        lenient().when(searchLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("空搜索关键词抛出非法参数")
    void search_blankKeyword_throws() {
        assertThatThrownBy(() -> searchService.search("goods", "  ", 0, 10, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能为空");
    }

    @Test
    @DisplayName("超过标签上限抛出业务异常")
    void search_tagLimitExceeded() {
        List<Long> tagIds = java.util.stream.LongStream.range(0, 12).boxed().toList();
        assertThatThrownBy(() -> searchService.search("goods", "phone", 0, 10, tagIds))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_PARAMETER.getCode());
    }

    @Test
    @DisplayName("按商品搜索返回结果并根据校区过滤")
    void search_goods_success() {
        User user = User.builder().username("tester").status(UserStatus.ACTIVE).campusId(8L).build();
        user.setId(99L);
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));

        GoodsSearchProjection projection = mock(GoodsSearchProjection.class);
        when(projection.getId()).thenReturn(123L);
        when(projection.getTitle()).thenReturn("MacBook");
        when(projection.getSnippet()).thenReturn("轻薄本");
        when(projection.getPrice()).thenReturn(new BigDecimal("8999.00"));
        when(projection.getCampusId()).thenReturn(8L);

        PageRequest pageable = PageRequest.of(0, 5);
        when(goodsRepository.searchGoodsFts(eq("mac"), eq(8L), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(projection), pageable, 1));

        Page<com.campus.marketplace.common.dto.response.SearchResultItem> page = searchService.search("goods", "mac", 0, 5, null);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getType()).isEqualTo("GOODS");
        assertThat(page.getContent().getFirst().getTitle()).isEqualTo("MacBook");
    }

    @Test
    @DisplayName("带标签搜索无匹配时返回空页")
    void search_goods_withTagsNoMatch() {
        when(goodsTagRepository.findGoodsIdsByAllTagIds(eq(List.of(1L, 2L)), eq(2L))).thenReturn(List.of());

        Page<com.campus.marketplace.common.dto.response.SearchResultItem> page =
                searchService.search("goods", "pad", 0, 10, List.of(1L, 2L));

        assertThat(page).isEmpty();
        verify(goodsRepository, never()).searchGoodsFtsWithIds(anyString(), any(), any(), any());
    }

    @Test
    @DisplayName("帖子搜索尊重校区限制")
    void search_post_success() {
        User user = User.builder().username("tester").status(UserStatus.ACTIVE).campusId(3L).build();
        user.setId(55L);
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));

        PostSearchProjection projection = mock(PostSearchProjection.class);
        when(projection.getId()).thenReturn(44L);
        when(projection.getTitle()).thenReturn("二手单车");
        when(projection.getSnippet()).thenReturn("轻便");
        when(projection.getCampusId()).thenReturn(3L);

        PageRequest pageable = PageRequest.of(1, 5);
        when(postRepository.searchPostsFts(eq("bike"), eq(3L), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(projection), pageable, 1));

        Page<com.campus.marketplace.common.dto.response.SearchResultItem> page =
                searchService.search("post", "bike", 1, 5, null);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getType()).isEqualTo("POST");
    }

    @Test
    @DisplayName("未知类型搜索抛出业务异常")
    void search_unknownType() {
        assertThatThrownBy(() -> searchService.search("unknown", "anything", 0, 10, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PARAM_ERROR.getCode());
    }
}
