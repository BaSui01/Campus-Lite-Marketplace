package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.GoodsStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品筛选请求参数
 *
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 商品列表查询的筛选参数，继承通用筛选基类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品筛选请求参数")
public class GoodsFilterRequest extends BaseFilterRequest {

    /**
     * 分类 ID
     */
    @Schema(description = "分类 ID", example = "101")
    private Long categoryId;

    /**
     * 最低价格
     */
    @Schema(description = "最低价格", example = "1000")
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    @Schema(description = "最高价格", example = "5000")
    private BigDecimal maxPrice;

    /**
     * 商品状态
     */
    @Schema(description = "商品状态（PENDING/APPROVED/REJECTED/SOLD/OFFLINE）", example = "APPROVED")
    private GoodsStatus status;

    /**
     * 标签 ID 列表（全部匹配）
     */
    @Schema(description = "标签 ID 列表（全部匹配）", example = "[1, 3, 5]")
    private List<Long> tagIds;

    /**
     * 卖家 ID
     */
    @Schema(description = "卖家 ID", example = "123")
    private Long sellerId;

    /**
     * 校区 ID
     */
    @Schema(description = "校区 ID", example = "1")
    private Long campusId;
}
