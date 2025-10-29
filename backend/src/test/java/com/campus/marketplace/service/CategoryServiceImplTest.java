package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateCategoryRequest;
import com.campus.marketplace.common.dto.request.UpdateCategoryRequest;
import com.campus.marketplace.common.dto.response.CategoryNodeResponse;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Category Service Impl Test
 *
 * @author BaSui
 * @date 2025-10-29
 */

@ExtendWith(MockitoExtension.class)
@DisplayName("分类服务实现测试")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private GoodsRepository goodsRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category existing;

    @BeforeEach
    void setUp() {
        existing = Category.builder()
                .name("数码")
                .description("电子产品")
                .parentId(null)
                .sortOrder(1)
                .build();
        existing.setId(100L);
    }

    @AfterEach
    void tearDown() {
        reset(categoryRepository);
        reset(goodsRepository);
    }

    @Test
    @DisplayName("创建分类成功会去重名称并保存")
    void createCategory_success() {
        CreateCategoryRequest request = new CreateCategoryRequest("  校园生活  ", "desc", null, 5);
        when(categoryRepository.findByName("校园生活")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(200L);
            return saved;
        });

        Long id = categoryService.createCategory(request);

        assertThat(id).isEqualTo(200L);
        verify(categoryRepository).save(argThat(category ->
                category.getName().equals("校园生活") &&
                        category.getSortOrder() == 5));
    }

    @Test
    @DisplayName("创建分类名称重复抛出业务异常")
    void createCategory_duplicateName() {
        CreateCategoryRequest request = new CreateCategoryRequest("数码", null, null, null);
        when(categoryRepository.findByName("数码")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.DUPLICATE_RESOURCE.getCode());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("创建分类父级不存在抛出异常")
    void createCategory_parentNotFound() {
        CreateCategoryRequest request = new CreateCategoryRequest("生活", null, 999L, null);
        when(categoryRepository.findByName("生活")).thenReturn(Optional.empty());
        when(categoryRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CATEGORY_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("更新分类可修改父级并保持唯一名称")
    void updateCategory_success() {
        Category parent = Category.builder().name("根类目").build();
        parent.setId(1L);
        existing.setParentId(null);

        when(categoryRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("数码配件")).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));

        UpdateCategoryRequest request = new UpdateCategoryRequest("数码配件", "附件", 1L, 8);
        categoryService.updateCategory(100L, request);

        assertThat(existing.getName()).isEqualTo("数码配件");
        assertThat(existing.getParentId()).isEqualTo(1L);
        assertThat(existing.getSortOrder()).isEqualTo(8);
        verify(categoryRepository).save(existing);
    }

    @Test
    @DisplayName("更新分类名称冲突抛出异常")
    void updateCategory_duplicateName() {
        Category other = Category.builder().name("生活").build();
        other.setId(300L);
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("生活")).thenReturn(Optional.of(other));

        UpdateCategoryRequest request = new UpdateCategoryRequest("生活", null, null, null);

        assertThatThrownBy(() -> categoryService.updateCategory(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.DUPLICATE_RESOURCE.getCode());
    }

    @Test
    @DisplayName("更新分类设置自己为父级将失败")
    void updateCategory_selfParent() {
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(existing));

        UpdateCategoryRequest request = new UpdateCategoryRequest("数码", null, 100L, null);

        assertThatThrownBy(() -> categoryService.updateCategory(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode());
    }

    @Test
    @DisplayName("更新分类父级不存在抛出异常")
    void updateCategory_parentNotFound() {
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("数码"))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

        UpdateCategoryRequest request = new UpdateCategoryRequest("数码", null, 9L, null);

        assertThatThrownBy(() -> categoryService.updateCategory(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CATEGORY_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("更新分类检测环路时发现循环抛出异常")
    void updateCategory_detectCircularReference() {
        Category parent = Category.builder().name("父").parentId(300L).build();
        parent.setId(200L);
        Category ancestor = Category.builder().name("祖").parentId(100L).build();
        ancestor.setId(300L);

        when(categoryRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("数码"))
                .thenReturn(Optional.of(existing));
        when(categoryRepository.findById(200L)).thenReturn(Optional.of(parent));
        when(categoryRepository.findById(300L)).thenReturn(Optional.of(ancestor));

        UpdateCategoryRequest request = new UpdateCategoryRequest("数码", null, 200L, null);

        assertThatThrownBy(() -> categoryService.updateCategory(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode());
    }

    @Test
    @DisplayName("删除分类成功会检查子分类与商品数量")
    void deleteCategory_success() {
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByParentIdOrderBySortOrder(100L)).thenReturn(List.of());
        when(goodsRepository.countByCategoryId(100L)).thenReturn(0L);

        categoryService.deleteCategory(100L);

        verify(categoryRepository).delete(existing);
    }

    @Test
    @DisplayName("删除分类存在子分类将失败")
    void deleteCategory_hasChildren() {
        Category child = Category.builder().name("子").build();
        child.setId(101L);

        when(categoryRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByParentIdOrderBySortOrder(100L)).thenReturn(List.of(child));

        assertThatThrownBy(() -> categoryService.deleteCategory(100L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode());
        verify(goodsRepository, never()).countByCategoryId(anyLong());
    }

    @Test
    @DisplayName("删除分类存在商品将失败")
    void deleteCategory_hasGoods() {
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByParentIdOrderBySortOrder(100L)).thenReturn(List.of());
        when(goodsRepository.countByCategoryId(100L)).thenReturn(3L);

        assertThatThrownBy(() -> categoryService.deleteCategory(100L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode());
    }

    @Test
    @DisplayName("分类树查询返回按层级排序的结果")
    void getCategoryTree_returnsNestedStructure() {
        Category root = Category.builder().name("根").parentId(null).sortOrder(1).build();
        root.setId(1L);
        root.setCreatedAt(LocalDateTime.now());
        Category child = Category.builder().name("子").parentId(1L).sortOrder(2).build();
        child.setId(2L);
        child.setCreatedAt(LocalDateTime.now());

        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of(root, child));

        List<CategoryNodeResponse> responses = categoryService.getCategoryTree();

        assertThat(responses).hasSize(1);
        CategoryNodeResponse rootNode = responses.get(0);
        assertThat(rootNode.name()).isEqualTo("根");
        assertThat(rootNode.children()).hasSize(1);
        assertThat(rootNode.children().getFirst().name()).isEqualTo("子");
    }
}
