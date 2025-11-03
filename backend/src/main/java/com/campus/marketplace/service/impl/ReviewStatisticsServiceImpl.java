package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.ReviewStatisticsDTO;
import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.service.ReviewStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评价统计服务实现类
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewStatisticsServiceImpl implements ReviewStatisticsService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Override
    public ReviewStatisticsDTO getGoodsReviewStatistics(Long goodsId) {
        log.info("获取商品评价统计: goodsId={}", goodsId);

        // 1. 通过商品ID查询所有订单
        List<Long> orderIds = orderRepository.findByGoodsId(goodsId).stream()
                .map(order -> order.getId())
                .toList();

        // 2. 通过订单ID列表查询评价
        List<Review> reviews = orderIds.isEmpty() 
                ? List.of() 
                : reviewRepository.findByOrderIdIn(orderIds);

        return calculateStatistics(reviews);
    }

    @Override
    public ReviewStatisticsDTO getSellerReviewStatistics(Long sellerId) {
        log.info("获取卖家评价统计: sellerId={}", sellerId);

        List<Review> reviews = reviewRepository.findBySellerId(sellerId);

        return calculateStatistics(reviews);
    }

    @Override
    public Double calculatePositiveRate(Long sellerId) {
        List<Review> reviews = reviewRepository.findBySellerId(sellerId);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        long positiveCount = reviews.stream()
            .filter(r -> r.getRating() >= 4) // 4星及以上为好评
            .count();

        return (double) positiveCount / reviews.size();
    }

    @Override
    public Double[] calculateAverageScores(Long sellerId) {
        List<Review> reviews = reviewRepository.findBySellerId(sellerId);

        if (reviews.isEmpty()) {
            return new Double[]{0.0, 0.0, 0.0};
        }

        double avgQuality = reviews.stream()
            .mapToInt(Review::getQualityScore)
            .average()
            .orElse(0.0);

        double avgService = reviews.stream()
            .mapToInt(Review::getServiceScore)
            .average()
            .orElse(0.0);

        double avgDelivery = reviews.stream()
            .mapToInt(Review::getDeliveryScore)
            .average()
            .orElse(0.0);

        return new Double[]{avgQuality, avgService, avgDelivery};
    }

    /**
     * 计算评价统计
     */
    private ReviewStatisticsDTO calculateStatistics(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return ReviewStatisticsDTO.builder()
                .totalCount(0L)
                .positiveCount(0L)
                .neutralCount(0L)
                .negativeCount(0L)
                .positiveRate(0.0)
                .avgRating(0.0)
                .qualityScore(0.0)
                .serviceScore(0.0)
                .logisticsScore(0.0)
                .build();
        }

        long totalCount = reviews.size();

        // 统计好评、中评、差评数量
        long positiveCount = reviews.stream().filter(r -> r.getRating() >= 4).count(); // 4-5星
        long neutralCount = reviews.stream().filter(r -> r.getRating() == 3).count();   // 3星
        long negativeCount = reviews.stream().filter(r -> r.getRating() <= 2).count();  // 1-2星

        // 计算好评率
        double positiveRate = (double) positiveCount / totalCount;

        // 计算平均评分
        double avgRating = reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);

        // 计算三维评分
        double qualityScore = reviews.stream()
            .mapToInt(Review::getQualityScore)
            .average()
            .orElse(0.0);

        double serviceScore = reviews.stream()
            .mapToInt(Review::getServiceScore)
            .average()
            .orElse(0.0);

        double logisticsScore = reviews.stream()
            .mapToInt(Review::getDeliveryScore)
            .average()
            .orElse(0.0);

        return ReviewStatisticsDTO.builder()
            .totalCount(totalCount)
            .positiveCount(positiveCount)
            .neutralCount(neutralCount)
            .negativeCount(negativeCount)
            .positiveRate(positiveRate)
            .avgRating(avgRating)
            .qualityScore(qualityScore)
            .serviceScore(serviceScore)
            .logisticsScore(logisticsScore)
            .build();
    }
}
