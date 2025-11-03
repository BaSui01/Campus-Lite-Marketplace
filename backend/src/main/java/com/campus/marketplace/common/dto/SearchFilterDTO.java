package com.campus.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 搜索筛选条件 DTO
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilterDTO {

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 成色（如：全新、95新、9成新）
     */
    private String condition;

    /**
     * 发货地（校区）
     */
    private String location;

    /**
     * 排序字段（price/createdAt/salesVolume）
     */
    private String sortBy;

    /**
     * 排序方向（ASC/DESC）
     */
    private String sortDirection;
}
