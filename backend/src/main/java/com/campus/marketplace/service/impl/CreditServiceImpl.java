package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.enums.CreditLevel;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.service.CreditService;
import com.campus.marketplace.service.ReviewStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 信用服务实现类
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final OrderRepository orderRepository;
    private final ReviewStatisticsService reviewStatisticsService;

    @Override
    public CreditLevel calculateCreditLevel(Long userId) {
        log.info("计算用户信用等级: userId={}", userId);

        // 统计已完成的订单数量
        long completedOrders = orderRepository.countBySellerIdAndStatus(userId, OrderStatus.COMPLETED);

        // 计算好评率
        Double positiveRate = reviewStatisticsService.calculatePositiveRate(userId);

        // 根据订单数量和好评率计算信用等级
        return CreditLevel.fromOrderCountAndRate(completedOrders, positiveRate != null ? positiveRate : 0.0);
    }

    @Override
    public String getCreditLevelDescription(Long userId) {
        CreditLevel level = calculateCreditLevel(userId);
        return level.getDescription();
    }

    @Override
    public Integer calculateAvgResponseTime(Long sellerId) {
        log.debug("平均响应时间计算功能需要聊天系统支持，当前返回 null");
        // 需要聊天系统支持：查询卖家的聊天记录，计算首次回复时间的平均值
        // 当聊天系统实现后，可以通过 ChatMessageRepository 查询并计算
        return null;
    }
}
