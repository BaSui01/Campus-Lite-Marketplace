package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.SearchFilterDTO;
import com.campus.marketplace.common.dto.SearchSuggestionDTO;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.SearchHistory;
import com.campus.marketplace.common.entity.SearchKeyword;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.SearchHistoryRepository;
import com.campus.marketplace.repository.SearchKeywordRepository;
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
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final GoodsRepository goodsRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final SearchKeywordRepository searchKeywordRepository;

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
            String searchKeyword = "%" + keyword.trim() + "%";
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
