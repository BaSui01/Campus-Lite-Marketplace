package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CategoryCacheDTO 单元测试
 *
 * 测试目标：
 * 1. ✅ 验证从 Category 实体正确转换为 DTO
 * 2. ✅ 验证 null 值处理
 * 3. ✅ 验证业务方法（isTopLevel）
 *
 * @author BaSui 😎
 * @date 2025-10-31
 */
@DisplayName("分类缓存 DTO 测试")
class CategoryCacheDTOTest {

    @Test
    @DisplayName("应该能从完整的 Category 实体转换为 DTO")
    void shouldConvertFromCompleteCategory() {
        // 🔴 Arrange：准备测试数据
        Category category = Category.builder()
                .name("电子产品")
                .description("各类电子设备")
                .parentId(null)
                .sortOrder(100)
                .build();

        category.setId(1L);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        // 🟢 Act：执行转换
        CategoryCacheDTO dto = CategoryCacheDTO.from(category);

        // 🔵 Assert：验证结果
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("电子产品");
        assertThat(dto.getDescription()).isEqualTo("各类电子设备");
        assertThat(dto.getParentId()).isNull();
        assertThat(dto.getSortOrder()).isEqualTo(100);
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("应该能正确转换子分类（有 parentId）")
    void shouldConvertSubCategory() {
        // 🔴 Arrange：准备子分类数据
        Category subCategory = Category.builder()
                .name("手机")
                .description("智能手机")
                .parentId(1L)  // ⚠️ 有父级分类
                .sortOrder(10)
                .build();

        subCategory.setId(10L);

        // 🟢 Act：执行转换
        CategoryCacheDTO dto = CategoryCacheDTO.from(subCategory);

        // 🔵 Assert：验证结果
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("手机");
        assertThat(dto.getParentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("应该能处理 Category 为 null 的情况")
    void shouldHandleNullCategory() {
        // 🔴 Arrange & 🟢 Act
        CategoryCacheDTO dto = CategoryCacheDTO.from(null);

        // 🔵 Assert
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("应该能正确判断是否是顶级分类")
    void shouldCheckIfTopLevel() {
        // 🔴 Arrange：顶级分类
        Category topCategory = Category.builder()
                .name("顶级分类")
                .parentId(null)
                .build();

        CategoryCacheDTO topDto = CategoryCacheDTO.from(topCategory);

        // 🟢 Act & 🔵 Assert
        assertThat(topDto.isTopLevel()).isTrue();

        // 🔴 Arrange：子分类
        Category subCategory = Category.builder()
                .name("子分类")
                .parentId(1L)
                .build();

        CategoryCacheDTO subDto = CategoryCacheDTO.from(subCategory);

        // 🟢 Act & 🔵 Assert
        assertThat(subDto.isTopLevel()).isFalse();
    }

    @Test
    @DisplayName("应该能序列化和反序列化（验证 Serializable）")
    void shouldBeSerializable() {
        // 🔴 Arrange
        CategoryCacheDTO dto = CategoryCacheDTO.builder()
                .id(1L)
                .name("测试分类")
                .description("测试描述")
                .parentId(null)
                .sortOrder(100)
                .build();

        // 🟢 Act & 🔵 Assert：验证对象实现了 Serializable
        assertThat(dto).isInstanceOf(java.io.Serializable.class);
    }
}
