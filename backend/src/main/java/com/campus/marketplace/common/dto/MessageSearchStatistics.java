package com.campus.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 消息搜索统计DTO
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchStatistics {

    /**
     * 总搜索次数
     */
    private Long totalSearches;

    /**
     * 热门关键词列表
     */
    private List<PopularKeyword> popularKeywords;

    /**
     * 最近搜索记录
     */
    private List<MessageSearchHistory> recentSearches;

    /**
     * 搜索成功率
     */
    private Double successRate;

    /**
     * 热门关键词
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularKeyword {
        /**
         * 关键词
         */
        private String keyword;

        /**
         * 搜索次数
         */
        private Long count;
    }
}