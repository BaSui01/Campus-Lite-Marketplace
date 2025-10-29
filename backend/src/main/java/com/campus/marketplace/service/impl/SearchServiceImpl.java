package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.SearchResultItem;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.entity.SearchLog;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.GoodsTagRepository;
import com.campus.marketplace.repository.PostRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.SearchLogRepository;
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Search Service Impl
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final GoodsRepository goodsRepository;
    private final GoodsTagRepository goodsTagRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SearchLogRepository searchLogRepository;

    @Override
    public Page<SearchResultItem> search(String type, String q, int page, int size, List<Long> tagIds) {
        if (q == null || q.isBlank()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }

        List<Long> sanitizedTagIds = tagIds == null
                ? List.of()
                : tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (sanitizedTagIds.size() > 10) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "最多选择 10 个标签");
        }

        long start = System.currentTimeMillis();

        // 校区过滤：普通用户限定本校
        Long campusFilter = null;
        try {
            if (SecurityUtil.isAuthenticated() && !SecurityUtil.hasAuthority(PermissionCodes.SYSTEM_CAMPUS_CROSS)) {
                String username = SecurityUtil.getCurrentUsername();
                User u = userRepository.findByUsername(username).orElse(null);
                campusFilter = u != null ? u.getCampusId() : null;
            }
        } catch (Exception ignored) { }

        Pageable pageable = PageRequest.of(page, size);
        Page<SearchResultItem> result;

        switch (type == null ? "goods" : type.toLowerCase()) {
            case "goods" -> {
                List<Long> goodsIdsFilter = null;
                if (!sanitizedTagIds.isEmpty()) {
                    goodsIdsFilter = goodsTagRepository.findGoodsIdsByAllTagIds(
                            sanitizedTagIds,
                            sanitizedTagIds.size()
                    );
                    if (goodsIdsFilter.isEmpty()) {
                        result = Page.empty(pageable);
                        break;
                    }
                }

                Page<GoodsSearchProjection> pageData = goodsIdsFilter == null
                        ? goodsRepository.searchGoodsFts(q, campusFilter, pageable)
                        : goodsRepository.searchGoodsFtsWithIds(q, campusFilter, goodsIdsFilter.toArray(Long[]::new), pageable);

                result = new PageImpl<>(
                        pageData.stream().map(p -> SearchResultItem.builder()
                                .type("GOODS")
                                .id(p.getId())
                                .title(p.getTitle())
                                .snippet(p.getSnippet())
                                .price(p.getPrice())
                                .campusId(p.getCampusId())
                                .build()).collect(Collectors.toList()),
                        pageable,
                        pageData.getTotalElements()
                );
            }
            case "post" -> {
                Page<PostSearchProjection> pageData = postRepository.searchPostsFts(q, campusFilter, pageable);
                result = new PageImpl<>(
                        pageData.stream().map(p -> SearchResultItem.builder()
                                .type("POST")
                                .id(p.getId())
                                .title(p.getTitle())
                                .snippet(p.getSnippet())
                                .campusId(p.getCampusId())
                                .build()).collect(Collectors.toList()),
                        pageable,
                        pageData.getTotalElements()
                );
            }
            default -> throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的搜索类型");
        }

        long duration = System.currentTimeMillis() - start;

        // 异步保存搜索日志（不影响主流程）
        String currentUsername = null;
        try {
            if (SecurityUtil.isAuthenticated()) {
                currentUsername = SecurityUtil.getCurrentUsername();
            }
        } catch (Exception ignored) { }
        final String username = currentUsername;
        final Long campusIdFinal = campusFilter;
        final long total = result.getTotalElements();
        Thread.ofVirtual().start(() -> {
            try {
                SearchLog logEntity = SearchLog.builder()
                        .username(username)
                        .keyword(q)
                        .campusId(campusIdFinal)
                        .durationMs(duration)
                        .resultCount(total)
                        .build();
                searchLogRepository.save(logEntity);
            } catch (Exception ignored) { }
        });

        log.info("搜索完成: type={}, q='{}', campusId={}, tags={}, cost={}ms, total={}",
                type, q, campusFilter, sanitizedTagIds, duration, result.getTotalElements());
        return result;
    }
}
