package com.campus.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索建议 DTO
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionDTO {

    /**
     * 历史搜索记录
     */
    private List<String> historyKeywords;

    /**
     * 热门搜索词
     */
    private List<String> hotKeywords;

    /**
     * 智能补全建议
     */
    private List<String> autoCompleteKeywords;
}
