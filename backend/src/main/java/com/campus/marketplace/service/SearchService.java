package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.SearchResultItem;
import org.springframework.data.domain.Page;

import java.util.List;

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
}
