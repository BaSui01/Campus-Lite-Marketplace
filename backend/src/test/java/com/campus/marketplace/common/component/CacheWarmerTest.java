package com.campus.marketplace.common.component;

import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheWarmer 测试")
class CacheWarmerTest {

    @Mock
    private CacheService cacheService;
    @Mock
    private GoodsRepository goodsRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private CacheWarmer cacheWarmer;

    @BeforeEach
    void setUp() {
        cacheWarmer = new CacheWarmer(cacheService, goodsRepository, categoryRepository);
    }

    @Test
    @DisplayName("启动预热会缓存热门商品与分类")
    void warmUpCacheOnStartup_shouldPopulateCaches() {
        Goods goods = Goods.builder().title("热销").build();
        goods.setId(1L);
        when(goodsRepository.findByStatus(eq(GoodsStatus.APPROVED), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(goods)));
        Category cat1 = Category.builder().name("数码").build();
        cat1.setId(1L);
        Category cat2 = Category.builder().name("图书").build();
        cat2.setId(2L);
        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));
        when(categoryRepository.findByParentIdIsNullOrderBySortOrder()).thenReturn(List.of(cat1));

        cacheWarmer.warmUpCacheOnStartup();

        verify(cacheService, times(3)).set(anyString(), any(), eq(1L), eq(TimeUnit.HOURS));
    }

    @Test
    @DisplayName("刷新缓存会调用预热逻辑")
    void refreshAllCache_shouldReRunWarmup() {
        when(goodsRepository.findByStatus(eq(GoodsStatus.APPROVED), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(categoryRepository.findAll()).thenReturn(List.of());
        when(categoryRepository.findByParentIdIsNullOrderBySortOrder()).thenReturn(List.of());

        cacheWarmer.refreshAllCache();

        verify(goodsRepository, atLeastOnce()).findByStatus(eq(GoodsStatus.APPROVED), any(PageRequest.class));
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("失效缓存时会删除对应键")
    void invalidateCache_shouldDeleteKeys() {
        cacheWarmer.invalidateHotGoodsCache();
        cacheWarmer.invalidateCategoriesCache();

        verify(cacheService, atLeast(1)).delete(contains("hot:goods"));
        verify(cacheService, atLeast(2)).delete(contains("category"));
    }
}
