package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.*;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.entity.ViewLog;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.*;
import com.campus.marketplace.service.GoodsDetailService;
import com.campus.marketplace.service.ReviewStatisticsService;
import com.campus.marketplace.service.CreditService;
import com.campus.marketplace.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品详情服务实现类
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsDetailServiceImpl implements GoodsDetailService {

    private final GoodsRepository goodsRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final ViewLogRepository viewLogRepository;
    private final ReviewRepository reviewRepository;
    
    @org.springframework.context.annotation.Lazy
    private ReviewStatisticsService reviewStatisticsService;
    
    @org.springframework.context.annotation.Lazy
    private CreditService creditService;
    
    @org.springframework.context.annotation.Lazy
    private OnlineStatusService onlineStatusService;

    @org.springframework.beans.factory.annotation.Autowired
    public void setReviewStatisticsService(@org.springframework.context.annotation.Lazy ReviewStatisticsService reviewStatisticsService) {
        this.reviewStatisticsService = reviewStatisticsService;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public void setCreditService(@org.springframework.context.annotation.Lazy CreditService creditService) {
        this.creditService = creditService;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public void setOnlineStatusService(@org.springframework.context.annotation.Lazy OnlineStatusService onlineStatusService) {
        this.onlineStatusService = onlineStatusService;
    }

    @Override
    public GoodsDetailDTO getGoodsDetail(Long goodsId, Long userId) {
        log.info("获取商品详情: goodsId={}, userId={}", goodsId, userId);

        // 1. 获取商品基本信息
        Goods goods = goodsRepository.findById(goodsId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));

        GoodsResponse goodsResponse = convertToResponse(goods);

        // 2. 获取卖家信息
        SellerInfoDTO sellerInfo = getSellerInfo(goods.getSellerId());

        // 3. 获取相似商品推荐
        List<GoodsResponse> similarGoods = getSimilarGoods(goodsId, 10);

        // 4. 获取评价统计
        ReviewStatisticsDTO reviewStatistics = reviewStatisticsService != null 
            ? reviewStatisticsService.getSellerReviewStatistics(goods.getSellerId())
            : null;

        // 5. 获取最新评价（前3条）
        List<Review> recentReviews = getRecentReviews(goodsId, 3);

        // 6. 判断是否已收藏
        Boolean isFavorited = false;
        if (userId != null) {
            isFavorited = favoriteRepository.existsByUserIdAndGoodsId(userId, goodsId);
        }

        // 7. 获取浏览次数
        Long viewCount = viewLogRepository.countByGoodsId(goodsId);

        // 8. 记录浏览行为
        if (userId != null) {
            recordView(goodsId, userId, "detail");
        }

        return GoodsDetailDTO.builder()
            .goods(goodsResponse)
            .seller(sellerInfo)
            .similarGoods(similarGoods)
            .reviewStatistics(reviewStatistics)
            .recentReviews(recentReviews)
            .isFavorited(isFavorited)
            .viewCount(viewCount)
            .build();
    }

    @Override
    public List<GoodsResponse> getSimilarGoods(Long goodsId, int limit) {
        log.info("获取相似商品推荐: goodsId={}, limit={}", goodsId, limit);

        Goods goods = goodsRepository.findById(goodsId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));

        List<Goods> similarGoods = new ArrayList<>();

        // 1. 同类别商品（优先级最高）
        List<Goods> sameCategoryGoods = goodsRepository
            .findByCategoryIdAndStatus(goods.getCategoryId(), GoodsStatus.APPROVED, PageRequest.of(0, limit))
            .getContent().stream()
            .filter(g -> !g.getId().equals(goodsId))
            .collect(Collectors.toList());
        similarGoods.addAll(sameCategoryGoods);

        // 2. 如果同类别商品不足，添加相似价格区间的商品
        if (similarGoods.size() < limit) {
            BigDecimal minPrice = goods.getPrice().multiply(BigDecimal.valueOf(0.8));
            BigDecimal maxPrice = goods.getPrice().multiply(BigDecimal.valueOf(1.2));

            List<Goods> similarPriceGoods = goodsRepository
                .findByConditions(
                    GoodsStatus.APPROVED,
                    null,
                    minPrice,
                    maxPrice,
                    null,
                    PageRequest.of(0, limit - similarGoods.size())
                ).getContent().stream()
                .filter(g -> !g.getId().equals(goodsId))
                .collect(Collectors.toList());

            for (Goods g : similarPriceGoods) {
                if (!similarGoods.contains(g)) {
                    similarGoods.add(g);
                }
            }
        }

        // 3. 如果还不足，添加同卖家的其他商品
        if (similarGoods.size() < limit) {
            List<Goods> sameSellerGoods = goodsRepository
                .findBySellerId(goods.getSellerId(), PageRequest.of(0, limit - similarGoods.size()))
                .getContent().stream()
                .filter(g -> !g.getId().equals(goodsId) && g.getStatus() == GoodsStatus.APPROVED)
                .collect(Collectors.toList());

            for (Goods g : sameSellerGoods) {
                if (!similarGoods.contains(g)) {
                    similarGoods.add(g);
                }
            }
        }

        return similarGoods.stream()
            .limit(limit)
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordView(Long goodsId, Long userId, String sourcePage) {
        log.debug("记录商品浏览: goodsId={}, userId={}, sourcePage={}", goodsId, userId, sourcePage);

        // 检查最近是否已浏览过（5分钟内不重复记录）
        ViewLog recentView = viewLogRepository.findFirstByUserIdAndGoodsIdOrderByCreatedAtDesc(userId, goodsId);
        if (recentView != null) {
            long minutesSinceLastView = java.time.Duration.between(
                recentView.getCreatedAt(),
                java.time.LocalDateTime.now()
            ).toMinutes();

            if (minutesSinceLastView < 5) {
                log.debug("5分钟内已浏览过，跳过记录");
                return;
            }
        }

        // 创建浏览记录
        ViewLog viewLog = ViewLog.builder()
            .userId(userId)
            .goodsId(goodsId)
            .sourcePage(sourcePage)
            .build();

        viewLogRepository.save(viewLog);

        // 更新商品浏览量
        Goods goods = goodsRepository.findById(goodsId).orElse(null);
        if (goods != null) {
            goods.setViewCount(goods.getViewCount() + 1);
            goodsRepository.save(goods);
        }
    }

    @Override
    public List<GoodsResponse> getUserViewHistory(Long userId, int limit) {
        log.info("获取用户浏览历史: userId={}, limit={}", userId, limit);

        List<Long> goodsIds = viewLogRepository.findRecentViewedGoodsIds(
            userId,
            PageRequest.of(0, limit)
        );

        return goodsIds.stream()
            .map(goodsRepository::findById)
            .filter(opt -> opt.isPresent())
            .map(opt -> opt.get())
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearUserViewHistory(Long userId) {
        log.info("清空用户浏览历史: userId={}", userId);
        viewLogRepository.deleteByUserId(userId);
    }

    /**
     * 获取卖家信息
     */
    private SellerInfoDTO getSellerInfo(Long sellerId) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 统计卖家商品数量
        long totalGoodsCount = goodsRepository.countBySellerId(sellerId);

        // 获取信用等级
        String creditLevel = creditService.getCreditLevelDescription(sellerId);

        // 检查在线状态
        boolean isOnline = onlineStatusService.isUserOnline(sellerId);

        // 计算平均响应时间
        Integer avgResponseTime = creditService.calculateAvgResponseTime(sellerId);

        // 计算好评率
        Double positiveRate = reviewStatisticsService.calculatePositiveRate(sellerId);

        return SellerInfoDTO.builder()
            .sellerId(sellerId)
            .username(seller.getUsername())
            .avatar(seller.getAvatar())
            .creditLevel(creditLevel)
            .isOnline(isOnline)
            .avgResponseTime(avgResponseTime)
            .totalGoodsCount(totalGoodsCount)
            .positiveRate(positiveRate)
            .build();
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

    /**
     * 获取商品的最新评价
     */
    private List<Review> getRecentReviews(Long goodsId, int limit) {
        // 1. 通过商品ID查询所有订单
        List<Long> orderIds = orderRepository.findByGoodsId(goodsId).stream()
                .map(order -> order.getId())
                .toList();

        if (orderIds.isEmpty()) {
            return List.of();
        }

        // 2. 通过订单ID列表查询评价
        List<Review> allReviews = reviewRepository.findByOrderIdIn(orderIds);

        // 3. 按创建时间倒序排序，取前N条
        return allReviews.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(limit)
                .toList();
    }
}
