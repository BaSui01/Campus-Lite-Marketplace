package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 评价筛选请求参数
 *
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 评价列表查询的筛选参数，继承通用筛选基类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "评价筛选请求参数")
public class ReviewFilterRequest extends BaseFilterRequest {

    /**
     * 评分（1-5星）
     */
    @Schema(description = "评分（1-5星）", example = "5")
    private Integer rating;

    /**
     * 最低评分
     */
    @Schema(description = "最低评分", example = "3")
    private Integer minRating;

    /**
     * 最高评分
     */
    @Schema(description = "最高评分", example = "5")
    private Integer maxRating;

    /**
     * 商品 ID
     */
    @Schema(description = "商品 ID", example = "1001")
    private Long goodsId;

    /**
     * 买家 ID
     */
    @Schema(description = "买家 ID", example = "2001")
    private Long buyerId;

    /**
     * 是否有图片
     */
    @Schema(description = "是否有图片", example = "true")
    private Boolean hasImages;
}
