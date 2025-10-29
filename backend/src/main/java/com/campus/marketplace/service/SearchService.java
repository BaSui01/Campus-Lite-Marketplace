package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.SearchResultItem;
import org.springframework.data.domain.Page;

import java.util.List;
/**
 * Search Service
 *
 * @author BaSui
 * @date 2025-10-29
 */


public interface SearchService {
    /**
     * 全文检索
     * @param type goods/post
     * @param q 关键词
     * @param page 页码
     * @param size 每页大小
     * @param tagIds 标签过滤（仅 goods 生效）
     */
    Page<SearchResultItem> search(String type, String q, int page, int size, List<Long> tagIds);

    /**
     * 重载方法，向后兼容旧调用方（无标签过滤）
     */
    default Page<SearchResultItem> search(String type, String q, int page, int size) {
        return search(type, q, page, size, null);
    }
}
