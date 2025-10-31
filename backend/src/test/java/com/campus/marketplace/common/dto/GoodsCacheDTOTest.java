package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GoodsCacheDTO 单元测试
 *
 * 测试目标：
 * 1. ✅ 验证从 Goods 实体正确转换为 DTO
 * 2. ✅ 验证懒加载字段的安全访问（不会抛出异常）
 * 3. ✅ 验证 null 值处理
 * 4. ✅ 验证业务方法（getCoverImage、isSold 等）
 *
 * @author BaSui 😎
 * @date 2025-10-31
 */
@DisplayName("商品缓存 DTO 测试")
class GoodsCacheDTOTest {

    @Test
    @DisplayName("应该能从完整的 Goods 实体转换为 DTO")
    void shouldConvertFromCompleteGoods() {
        // 🔴 Arrange：准备测试数据
        Category category = Category.builder()
                .name("电子产品")
                .build();

        User seller = User.builder()
                .nickname("卖家小明")
                .build();

        Campus campus = Campus.builder()
                .name("南校区")
                .build();

        Map<String, Object> extraAttrs = new HashMap<>();
        extraAttrs.put("brand", "Apple");
        extraAttrs.put("condition", "95新");

        Goods goods = Goods.builder()
                .title("iPhone 13 Pro")
                .description("95新，无拆无修")
                .price(new BigDecimal("4999.00"))
                .categoryId(10L)
                .category(category)
                .sellerId(100L)
                .seller(seller)
                .campusId(5L)
                .campus(campus)
                .status(GoodsStatus.APPROVED)
                .viewCount(100)
                .favoriteCount(20)
                .images(new String[]{"image1.jpg", "image2.jpg"})
                .extraAttrs(extraAttrs)
                .build();

        goods.setId(1L);  // ✅ ID 通过 setter 设置（来自 BaseEntity）
        goods.setCreatedAt(LocalDateTime.now());
        goods.setUpdatedAt(LocalDateTime.now());

        // 🟢 Act：执行转换
        GoodsCacheDTO dto = GoodsCacheDTO.from(goods);

        // 🔵 Assert：验证结果
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("iPhone 13 Pro");
        assertThat(dto.getDescription()).isEqualTo("95新，无拆无修");
        assertThat(dto.getPrice()).isEqualByComparingTo(new BigDecimal("4999.00"));

        // 验证关联字段的 ID 和名称
        assertThat(dto.getCategoryId()).isEqualTo(10L);
        assertThat(dto.getCategoryName()).isEqualTo("电子产品");
        assertThat(dto.getSellerId()).isEqualTo(100L);
        assertThat(dto.getSellerNickname()).isEqualTo("卖家小明");
        assertThat(dto.getCampusId()).isEqualTo(5L);
        assertThat(dto.getCampusName()).isEqualTo("南校区");

        // 验证其他字段
        assertThat(dto.getStatus()).isEqualTo(GoodsStatus.APPROVED);
        assertThat(dto.getViewCount()).isEqualTo(100);
        assertThat(dto.getFavoriteCount()).isEqualTo(20);
        assertThat(dto.getImages()).containsExactly("image1.jpg", "image2.jpg");
        assertThat(dto.getExtraAttrs()).containsEntry("brand", "Apple");
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("应该能安全处理懒加载字段为 null 的情况")
    void shouldHandleNullLazyLoadedFields() {
        // 🔴 Arrange：准备只有 ID 没有关联对象的数据
        Goods goods = Goods.builder()
                .title("测试商品")
                .description("测试描述")
                .price(new BigDecimal("100.00"))
                .categoryId(10L)
                .category(null)  // ⚠️ 懒加载字段未初始化
                .sellerId(100L)
                .seller(null)    // ⚠️ 懒加载字段未初始化
                .campusId(5L)
                .campus(null)    // ⚠️ 懒加载字段未初始化
                .status(GoodsStatus.APPROVED)
                .viewCount(0)
                .favoriteCount(0)
                .build();

        goods.setId(1L);  // ✅ ID 通过 setter 设置

        // 🟢 Act：执行转换（不应该抛出异常）
        GoodsCacheDTO dto = GoodsCacheDTO.from(goods);

        // 🔵 Assert：验证 ID 存在但名称为 null
        assertThat(dto).isNotNull();
        assertThat(dto.getCategoryId()).isEqualTo(10L);
        assertThat(dto.getCategoryName()).isNull();  // ✅ 安全处理
        assertThat(dto.getSellerId()).isEqualTo(100L);
        assertThat(dto.getSellerNickname()).isNull();  // ✅ 安全处理
        assertThat(dto.getCampusId()).isEqualTo(5L);
        assertThat(dto.getCampusName()).isNull();  // ✅ 安全处理
    }

    @Test
    @DisplayName("应该能处理 Goods 为 null 的情况")
    void shouldHandleNullGoods() {
        // 🔴 Arrange & 🟢 Act
        GoodsCacheDTO dto = GoodsCacheDTO.from(null);

        // 🔵 Assert
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("应该能正确获取封面图片")
    void shouldGetCoverImage() {
        // 🔴 Arrange：有图片的商品
        Goods goodsWithImages = Goods.builder()
                .images(new String[]{"cover.jpg", "image2.jpg"})
                .build();

        GoodsCacheDTO dtoWithImages = GoodsCacheDTO.from(goodsWithImages);

        // 🟢 Act & 🔵 Assert
        assertThat(dtoWithImages.getCoverImage()).isEqualTo("cover.jpg");

        // 🔴 Arrange：没有图片的商品
        Goods goodsWithoutImages = Goods.builder()
                .images(null)
                .build();

        GoodsCacheDTO dtoWithoutImages = GoodsCacheDTO.from(goodsWithoutImages);

        // 🟢 Act & 🔵 Assert
        assertThat(dtoWithoutImages.getCoverImage()).isNull();
    }

    @Test
    @DisplayName("应该能正确判断是否已售出")
    void shouldCheckIfSold() {
        // 🔴 Arrange：已售出商品
        Goods soldGoods = Goods.builder()
                .status(GoodsStatus.SOLD)
                .build();

        GoodsCacheDTO soldDto = GoodsCacheDTO.from(soldGoods);

        // 🟢 Act & 🔵 Assert
        assertThat(soldDto.isSold()).isTrue();

        // 🔴 Arrange：审核通过的商品（未售出）
        Goods approvedGoods = Goods.builder()
                .status(GoodsStatus.APPROVED)
                .build();

        GoodsCacheDTO approvedDto = GoodsCacheDTO.from(approvedGoods);

        // 🟢 Act & 🔵 Assert
        assertThat(approvedDto.isSold()).isFalse();
    }

    @Test
    @DisplayName("应该能正确判断是否已审核通过")
    void shouldCheckIfApproved() {
        // 🔴 Arrange：已审核通过商品
        Goods approvedGoods = Goods.builder()
                .status(GoodsStatus.APPROVED)
                .build();

        GoodsCacheDTO approvedDto = GoodsCacheDTO.from(approvedGoods);

        // 🟢 Act & 🔵 Assert
        assertThat(approvedDto.isApproved()).isTrue();

        // 🔴 Arrange：待审核商品
        Goods pendingGoods = Goods.builder()
                .status(GoodsStatus.PENDING)
                .build();

        GoodsCacheDTO pendingDto = GoodsCacheDTO.from(pendingGoods);

        // 🟢 Act & 🔵 Assert
        assertThat(pendingDto.isApproved()).isFalse();
    }

    @Test
    @DisplayName("应该能序列化和反序列化（验证 Serializable）")
    void shouldBeSerializable() {
        // 🔴 Arrange
        GoodsCacheDTO dto = GoodsCacheDTO.builder()
                .id(1L)
                .title("测试商品")
                .price(new BigDecimal("100.00"))
                .categoryId(10L)
                .categoryName("电子产品")
                .status(GoodsStatus.APPROVED)
                .viewCount(100)
                .favoriteCount(20)
                .build();

        // 🟢 Act & 🔵 Assert：验证对象实现了 Serializable
        assertThat(dto).isInstanceOf(java.io.Serializable.class);
    }
}
