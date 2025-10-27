package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 搜索结果项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultItem {
    private String type;      // GOODS / POST
    private Long id;
    private String title;
    private String snippet;   // 高亮片段（含 <em> 标签）
    private BigDecimal price; // 仅物品有值
    private Long campusId;
}
