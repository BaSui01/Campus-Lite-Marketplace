package com.campus.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 消息搜索历史DTO
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSearchHistory {

    /**
     * 搜索ID
     */
    private String id;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索时间
     */
    private LocalDateTime searchedAt;

    /**
     * 搜索结果数量
     */
    private Integer resultCount;

    /**
     * 搜索筛选条件（JSON格式）
     */
    private Map<String, Object> filters;
}