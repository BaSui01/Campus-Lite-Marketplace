package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.SearchFilterDTO;
import com.campus.marketplace.common.dto.SearchSuggestionDTO;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "搜索功能", description = "智能搜索相关接口")
public class SearchController {

    private final SearchService searchService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "全文搜索（FTS）", description = "支持商品和帖子的全文搜索，支持标签筛选")
    public ApiResponse<Page<com.campus.marketplace.common.dto.response.SearchResultItem>> search(
        @Parameter(description = "搜索类型（goods/post）", required = true) @RequestParam String type,
        @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,
        @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "标签ID列表（可选，仅商品搜索支持）") @RequestParam(required = false) List<Long> tagIds
    ) {
        // 验证关键词不为空
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "搜索关键词不能为空");
        }
        
        Page<com.campus.marketplace.common.dto.response.SearchResultItem> result =
                searchService.search(type, keyword, page, size, tagIds);
        return ApiResponse.success(result);
    }

    @GetMapping("/suggestions")
    @Operation(summary = "获取搜索建议")
    public ApiResponse<SearchSuggestionDTO> getSearchSuggestions(
        @Parameter(description = "关键词前缀") @RequestParam(required = false) String keyword
    ) {
        Long userId = getCurrentUserIdOrNull();
        SearchSuggestionDTO suggestions = searchService.getSearchSuggestions(userId, keyword);
        return ApiResponse.success(suggestions);
    }

    @GetMapping("/goods")
    @Operation(summary = "搜索商品")
    public ApiResponse<Page<GoodsResponse>> searchGoods(
        @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
        @Parameter(description = "最低价格") @RequestParam(required = false) java.math.BigDecimal minPrice,
        @Parameter(description = "最高价格") @RequestParam(required = false) java.math.BigDecimal maxPrice,
        @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
        @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
        @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String sortDirection,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = getCurrentUserIdOrNull();
        
        SearchFilterDTO filter = SearchFilterDTO.builder()
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .categoryId(categoryId)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();

        Pageable pageable = PageRequest.of(page, size);
        Page<GoodsResponse> result = searchService.searchGoods(keyword, filter, pageable, userId);
        
        return ApiResponse.success(result);
    }

    @GetMapping("/hot-keywords")
    @Operation(summary = "获取热门搜索词")
    public ApiResponse<List<String>> getHotKeywords(
        @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") int limit
    ) {
        List<String> keywords = searchService.getHotKeywords(limit);
        return ApiResponse.success(keywords);
    }

    @GetMapping("/history")
    @Operation(summary = "获取我的搜索历史")
    public ApiResponse<List<String>> getUserSearchHistory(
        @Parameter(description = "返回数量") @RequestParam(defaultValue = "20") int limit
    ) {
        Long userId = getCurrentUserId();
        List<String> history = searchService.getUserSearchHistory(userId, limit);
        return ApiResponse.success(history);
    }

    @DeleteMapping("/history")
    @Operation(summary = "清空搜索历史")
    public ApiResponse<Void> clearSearchHistory() {
        Long userId = getCurrentUserId();
        searchService.clearUserSearchHistory(userId);
        return ApiResponse.success();
    }

    /**
     * 获取当前登录用户ID（可能为null）
     */
    private Long getCurrentUserIdOrNull() {
        try {
            String username = SecurityUtil.getCurrentUsername();
            return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户ID（必须登录）
     */
    private Long getCurrentUserId() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
