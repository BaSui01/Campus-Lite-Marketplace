package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.GoodsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 商品缓存 DTO
 *
 * 专门用于 Redis 缓存，避免 Hibernate 懒加载序列化问题。
 * 只包含必要的字段，不包含懒加载的关联对象。
 *
 * 为啥要用 DTO？🤔
 * 1. 解耦 Entity 和缓存层，避免 Hibernate Session 关闭后的懒加载异常
 * 2. 减少缓存数据量，只存储需要的字段
 * 3. 防止 Jackson 序列化 Hibernate 代理对象时出错
 * 4. 符合 DDD 设计原则，Entity 是领域模型，DTO 是数据传输对象
 *
 * @author BaSui 😎
 * @date 2025-10-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsCacheDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品 ID
     */
    private Long id;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 分类 ID（避免懒加载 Category 对象）
     */
    private Long categoryId;

    /**
     * 分类名称（冗余字段，方便展示）
     */
    private String categoryName;

    /**
     * 卖家 ID（避免懒加载 User 对象）
     */
    private Long sellerId;

    /**
     * 卖家昵称（冗余字段，方便展示）
     */
    private String sellerNickname;

    /**
     * 校区 ID（避免懒加载 Campus 对象）
     */
    private Long campusId;

    /**
     * 校区名称（冗余字段，方便展示）
     */
    private String campusName;

    /**
     * 商品状态
     */
    private GoodsStatus status;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 收藏量
     */
    private Integer favoriteCount;

    /**
     * 图片 URL 数组（第一张作为封面）
     */
    private String[] images;

    /**
     * 扩展属性（JSONB）
     */
    private Map<String, Object> extraAttrs;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 从 Goods 实体转换为 DTO
     *
     * ⚠️ 注意：此方法必须在 Hibernate Session 打开的事务内调用，
     * 否则访问懒加载字段（category、seller、campus）会抛出异常！
     *
     * @param goods 商品实体
     * @return 商品缓存 DTO
     */
    public static GoodsCacheDTO from(Goods goods) {
        if (goods == null) {
            return null;
        }

        return GoodsCacheDTO.builder()
                .id(goods.getId())
                .title(goods.getTitle())
                .description(goods.getDescription())
                .price(goods.getPrice())
                .categoryId(goods.getCategoryId())
                // ⚠️ 安全访问懒加载字段：如果 category 未初始化，则为 null
                .categoryName(goods.getCategory() != null ? goods.getCategory().getName() : null)
                .sellerId(goods.getSellerId())
                .sellerNickname(goods.getSeller() != null ? goods.getSeller().getNickname() : null)
                .campusId(goods.getCampusId())
                .campusName(goods.getCampus() != null ? goods.getCampus().getName() : null)
                .status(goods.getStatus())
                .viewCount(goods.getViewCount())
                .favoriteCount(goods.getFavoriteCount())
                .images(goods.getImages())
                .extraAttrs(goods.getExtraAttrs())
                .createdAt(goods.getCreatedAt())
                .updatedAt(goods.getUpdatedAt())
                .build();
    }

    /**
     * 获取封面图片 URL
     *
     * @return 封面图片 URL，如果没有图片则返回 null
     */
    public String getCoverImage() {
        return (images != null && images.length > 0) ? images[0] : null;
    }

    /**
     * 检查是否已售出
     */
    public boolean isSold() {
        return this.status == GoodsStatus.SOLD;
    }

    /**
     * 检查是否已审核通过
     */
    public boolean isApproved() {
        return this.status == GoodsStatus.APPROVED;
    }
}
