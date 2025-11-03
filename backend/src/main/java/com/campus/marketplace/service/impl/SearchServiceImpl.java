package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.SearchFilterDTO;
import com.campus.marketplace.common.dto.SearchSuggestionDTO;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.dto.response.SearchResultItem;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.SearchHistory;
import com.campus.marketplace.common.entity.SearchKeyword;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.GoodsTagRepository;
import com.campus.marketplace.repository.PostRepository;
import com.campus.marketplace.repository.SearchHistoryRepository;
import com.campus.marketplace.repository.SearchKeywordRepository;
import com.campus.marketplace.repository.SearchLogRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.projection.GoodsSearchProjection;
import com.campus.marketplace.repository.projection.PostSearchProjection;
import com.campus.marketplace.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务实现类
 *
 * 支持 FTS 全文搜索（商品+帖子）和简化版商品搜索
 *
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final GoodsRepository goodsRepository;
    private final GoodsTagRepository goodsTagRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final SearchKeywordRepository searchKeywordRepository;
    private final SearchLogRepository searchLogRepository;

    @Override
    public Page<SearchResultItem> search(String type, String keyword, int page, int size, List<Long> tagIds) {
        // 参数校验
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        // 标签数量限制（最多10个）
        if (tagIds != null && tagIds.size() > 10) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "标签数量不能超过10个");
        }

        // 获取当前用户的校区ID
        Long campusId = getCurrentUserCampusId();

        // 创建分页参数
        Pageable pageable = PageRequest.of(page, size);

        // 根据类型执行不同的搜索
        Page<SearchResultItem> result;
        if ("goods".equalsIgnoreCase(type)) {
            result = searchGoods(keyword.trim(), campusId, tagIds, pageable);
        } else if ("post".equalsIgnoreCase(type)) {
            result = searchPosts(keyword.trim(), campusId, pageable);
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的搜索类型: " + type);
        }

        // 记录搜索日志（异步）
        recordSearchLog(keyword.trim(), type, (int) result.getTotalElements());

        log.info("搜索完成: type={}, keyword=, campusId={}, totalElements={}",
                type, keyword, campusId, result.getTotalElements());

        return result;
    }

    /**
     * 搜索商品（FTS）
     */
    private Page<SearchResultItem> searchGoods(String keyword, Long campusId, List<Long> tagIds, Pageable pageable) {
        Page<GoodsSearchProjection> goodsPage;

        if (tagIds != null && !tagIds.isEmpty()) {
            // 先根据标签筛选商品ID
            List<Long> goodsIds = goodsTagRepository.findGoodsIdsByAllTagIds(tagIds, (long) tagIds.size());

            if (goodsIds.isEmpty()) {
                // 没有匹配的商品，返回空页
                return Page.empty(pageable);
            }

            // 使用标签筛选后的商品ID进行FTS搜索
            Long[] goodsIdsArray = goodsIds.toArray(new Long[0]);
            goodsPage = goodsRepository.searchGoodsFtsWithIds(keyword, campusId, goodsIdsArray, pageable);
        } else {
            // 直接FTS搜索
            goodsPage = goodsRepository.searchGoodsFts(keyword, campusId, pageable);
        }

        // 转换为 SearchResultItem
        List<SearchResultItem> items = goodsPage.getContent().stream()
                .map(projection -> SearchResultItem.builder()
                        .type("GOODS")
                        .id(projection.getId())
                        .title(projection.getTitle())
                        .snippet(projection.getSnippet())
                        .price(projection.getPrice())
                        .campusId(projection.getCampusId())
                        .build())
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, goodsPage.getTotalElements());
    }

    /**
     * 搜索帖子（FTS）
     */
    private Page<SearchResultItem> searchPosts(String keyword, Long campusId, Pageable pageable) {
        Page<PostSearchProjection> postPage = postRepository.searchPostsFts(keyword, campusId, pageable);

        // 转换为 SearchResultItem
        List<SearchResultItem> items = postPage.getContent().stream()
                .map(projection -> SearchResultItem.builder()
                        .type("POST")
                        .id(projection.getId())
                        .title(projection.getTitle())
                        .snippet(projection.getSnippet())
                        .price(null) // 帖子没有价格
                        .campusId(projection.getCampusId())
                        .build())
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, postPage.getTotalElements());
    }

    /**
     * 获取当前用户的校区ID
     */
    private Long getCurrentUserCampusId() {
        try {
            // 检查是否有跨校区权限
            if (SecurityUtil.hasAuthority("system:campus:cross")) {
                return null; // 返回null表示不限制校区
            }

            // 获取当前用户的校区ID
            String username = SecurityUtil.getCurrentUsername();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            return user.getCampusId();
        } catch (Exception e) {
            log.warn("获取用户校区ID失败，使用默认校区: {}", e.getMessage());
            return null; // 未登录或获取失败时不限制校区
        }
    }

    /**
     * 记录搜索日志（异步）
     */
    private void recordSearchLog(String keyword, String type, int resultCount) {
        try {
            String username = SecurityUtil.getCurrentUsername();
            Long campusId = getCurrentUserCampusId();

            SearchLog searchLog = SearchLog.builder()
                    .username(username)
                    .keyword(keyword)
                    .campusId(campusId)
                    .resultCount((long) resultCount)
                    .build();

            searchLogRepository.save(searchLog);
            log.debug("搜索日志保存成功: username={}, keyword={}, type={}, resultCount={}",
                    username, keyword, type, resultCount);
        } catch (Exception e) {
            log.warn("记录搜索日志失败: {}", e.getMessage());
        }
    }

    @Override
    public SearchSuggestionDTO getSearchSuggestions(Long userId, String keyword) {
        log.info("获取搜索建议: userId={}, keyword={}", userId, keyword);

        SearchSuggestionDTO suggestion = new SearchSuggestionDTO();

        // 1. 获取用户搜索历史（最多10条）
        if (userId != null) {
            List<String> historyKeywords = searchHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 10))
                .stream()
                .map(SearchHistory::getKeyword)
                .distinct()
                .collect(Collectors.toList());
            suggestion.setHistoryKeywords(historyKeywords);
        } else {
            suggestion.setHistoryKeywords(new ArrayList<>());
        }

        // 2. 获取热门搜索词（最多10条）
        List<String> hotKeywords = searchKeywordRepository
            .findAllByOrderBySearchCountDesc(PageRequest.of(0, 10))
            .stream()
            .map(SearchKeyword::getKeyword)
            .collect(Collectors.toList());
        suggestion.setHotKeywords(hotKeywords);

        // 3. 智能补全（根据关键词前缀匹配）
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<String> autoCompleteKeywords = searchKeywordRepository
                .findByKeywordStartingWithIgnoreCaseOrderBySearchCountDesc(
                    keyword.trim(), 
                    PageRequest.of(0, 10)
                )
                .stream()
                .map(SearchKeyword::getKeyword)
                .collect(Collectors.toList());
            suggestion.setAutoCompleteKeywords(autoCompleteKeywords);
        } else {
            suggestion.setAutoCompleteKeywords(new ArrayList<>());
        }

        return suggestion;
    }

    @Override
    public Page<GoodsResponse> searchGoods(String keyword, SearchFilterDTO filter, Pageable pageable, Long userId) {
        log.info("搜索商品: keyword={}, filter={}", keyword, filter);

        // 构建查询条件
        List<Goods> goodsList = new ArrayList<>();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            // 无关键词，返回所有商品
            goodsList = goodsRepository.findByStatus(GoodsStatus.APPROVED, pageable).getContent();
        } else {
            // 根据关键词搜索（简化版：标题或描述包含关键词）
            goodsList = goodsRepository.findAll().stream()
                .filter(g -> g.getStatus() == GoodsStatus.APPROVED)
                .filter(g -> (g.getTitle() != null && g.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                        || (g.getDescription() != null && g.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());
        }

        // 应用筛选条件
        if (filter != null) {
            // 价格筛选
            if (filter.getMinPrice() != null) {
                BigDecimal minPrice = filter.getMinPrice();
                goodsList = goodsList.stream()
                    .filter(g -> g.getPrice().compareTo(minPrice) >= 0)
                    .collect(Collectors.toList());
            }
            if (filter.getMaxPrice() != null) {
                BigDecimal maxPrice = filter.getMaxPrice();
                goodsList = goodsList.stream()
                    .filter(g -> g.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
            }

            // 分类筛选
            if (filter.getCategoryId() != null) {
                goodsList = goodsList.stream()
                    .filter(g -> g.getCategoryId().equals(filter.getCategoryId()))
                    .collect(Collectors.toList());
            }

            // 排序
            if (filter.getSortBy() != null) {
                String sortBy = filter.getSortBy();
                boolean ascending = "ASC".equalsIgnoreCase(filter.getSortDirection());
                
                if ("price".equals(sortBy)) {
                    goodsList.sort((g1, g2) -> ascending 
                        ? g1.getPrice().compareTo(g2.getPrice())
                        : g2.getPrice().compareTo(g1.getPrice()));
                } else if ("createdAt".equals(sortBy)) {
                    goodsList.sort((g1, g2) -> ascending
                        ? g1.getCreatedAt().compareTo(g2.getCreatedAt())
                        : g2.getCreatedAt().compareTo(g1.getCreatedAt()));
                }
            }
        }

        // 转换为 GoodsResponse
        List<GoodsResponse> responseList = goodsList.stream()
            .skip((long) pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .map(this::convertToResponse)
            .collect(Collectors.toList());

        // 记录搜索历史
        if (userId != null && keyword != null && !keyword.trim().isEmpty()) {
            recordSearchHistory(userId, keyword.trim(), goodsList.size(), false);
        }

        // 更新热门关键词
        if (keyword != null && !keyword.trim().isEmpty()) {
            updateHotKeyword(keyword.trim());
        }

        return new PageImpl<>(responseList, pageable, goodsList.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordSearchHistory(Long userId, String keyword, int resultCount, boolean hasClick) {
        log.info("记录搜索历史: userId={}, keyword={}, resultCount={}", userId, keyword, resultCount);

        SearchHistory history = SearchHistory.builder()
            .userId(userId)
            .keyword(keyword)
            .resultCount(resultCount)
            .hasClick(hasClick)
            .build();

        searchHistoryRepository.save(history);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateHotKeyword(String keyword) {
        log.debug("更新热门关键词: keyword={}", keyword);

        SearchKeyword searchKeyword = searchKeywordRepository.findByKeyword(keyword)
            .orElse(SearchKeyword.builder()
                .keyword(keyword)
                .searchCount(0L)
                .lastSearchTime(LocalDateTime.now())
                .build());

        searchKeyword.incrementSearchCount();
        searchKeywordRepository.save(searchKeyword);
    }

    @Override
    public List<String> getHotKeywords(int limit) {
        return searchKeywordRepository
            .findAllByOrderBySearchCountDesc(PageRequest.of(0, limit))
            .stream()
            .map(SearchKeyword::getKeyword)
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserSearchHistory(Long userId, int limit) {
        return searchHistoryRepository
            .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
            .stream()
            .map(SearchHistory::getKeyword)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearUserSearchHistory(Long userId) {
        log.info("清空用户搜索历史: userId={}", userId);
        searchHistoryRepository.deleteByUserId(userId);
    }

    /**
     * 转换商品实体为响应 DTO
     */
    private GoodsResponse convertToResponse(Goods goods) {
        return GoodsResponse.builder()
            .id(goods.getId())
            .title(goods.getTitle())
            .description(goods.getDescription())
            .price(goods.getPrice())
            .categoryId(goods.getCategoryId())
            .sellerId(goods.getSellerId())
            .status(goods.getStatus())
            .viewCount(goods.getViewCount())
            .favoriteCount(goods.getFavoriteCount())
            .createdAt(goods.getCreatedAt())
            .build();
    }
}
