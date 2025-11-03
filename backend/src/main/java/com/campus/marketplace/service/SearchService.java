package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.SearchFilterDTO;
import com.campus.marketplace.common.dto.SearchSuggestionDTO;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.dto.response.SearchResultItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 搜索服务接口
 *
 * 提供智能搜索功能：FTS全文搜索、建议、筛选、排序、统计
 *
 * @author BaSui
 * @date 2025-11-03
 */
public interface SearchService {

    /**
     * 全文搜索（FTS）- 支持商品和帖子
     *
     * @param type 搜索类型（"goods" 或 "post"）
     * @param keyword 搜索关键词
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param tagIds 标签ID列表（可选，仅商品搜索支持）
     * @return 搜索结果分页
     * @throws IllegalArgumentException 如果关键词为空或类型无效
     */
    Page<SearchResultItem> search(String type, String keyword, int page, int size, List<Long> tagIds);

    /**
     * 获取搜索建议
     *
     * @param userId 用户ID
     * @param keyword 关键词前缀
     * @return 搜索建议
     */
    SearchSuggestionDTO getSearchSuggestions(Long userId, String keyword);

    /**
     * 搜索商品（简化版，保留向后兼容）
     *
     * @param keyword 搜索关键词
     * @param filter 筛选条件
     * @param pageable 分页参数
     * @param userId 用户ID（可选）
     * @return 商品分页结果
     */
    Page<GoodsResponse> searchGoods(String keyword, SearchFilterDTO filter, Pageable pageable, Long userId);

    /**
     * 记录搜索历史
     *
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param resultCount 结果数量
     * @param hasClick 是否有点击行为
     */
    void recordSearchHistory(Long userId, String keyword, int resultCount, boolean hasClick);

    /**
     * 更新热门搜索关键词
     *
     * @param keyword 搜索关键词
     */
    void updateHotKeyword(String keyword);

    /**
     * 获取热门搜索词
     *
     * @param limit 返回数量
     * @return 热门关键词列表
     */
    List<String> getHotKeywords(int limit);

    /**
     * 获取用户搜索历史
     *
     * @param userId 用户ID
     * @param limit 返回数量
     * @return 搜索历史列表
     */
    List<String> getUserSearchHistory(Long userId, int limit);

    /**
     * 清空用户搜索历史
     *
     * @param userId 用户ID
     */
    void clearUserSearchHistory(Long userId);
}
