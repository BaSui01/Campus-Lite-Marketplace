package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.CreditHistoryResponse;
import com.campus.marketplace.common.dto.response.UserCreditInfoResponse;
import com.campus.marketplace.common.dto.response.UserCreditInfoResponse.CreditLevelInfo;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.CreditLevel;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.CreditService;
import com.campus.marketplace.service.ReviewStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;

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
    private final UserRepository userRepository;

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

    @Override
    public UserCreditInfoResponse getMyCredit(Long userId) {
        log.info("获取用户信用信息: userId={}", userId);
        return buildCreditInfo(userId);
    }

    @Override
    public UserCreditInfoResponse getUserCredit(Long userId) {
        log.info("查询指定用户信用信息: userId={}", userId);
        return buildCreditInfo(userId);
    }

    @Override
    public Page<CreditHistoryResponse> getCreditHistory(Long userId, int page, int size) {
        log.info("获取信用历史: userId={}, page={}, size={}", userId, page, size);
        
        // 暂时返回空列表，后续可扩展信用历史记录表
        // TODO: 实现信用历史记录功能需要创建 CreditHistory 实体和 Repository
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    /**
     * 构建信用信息响应
     */
    private UserCreditInfoResponse buildCreditInfo(Long userId) {
        // 查询用户基本信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 计算信用等级
        CreditLevel creditLevel = calculateCreditLevel(userId);
        
        // 统计订单数量
        long orderCount = orderRepository.countBySellerIdAndStatus(userId, OrderStatus.COMPLETED);
        
        // 计算好评率
        Double positiveRate = reviewStatisticsService.calculatePositiveRate(userId);
        if (positiveRate == null) {
            positiveRate = 0.0;
        }
        
        // 计算平均响应时间
        Integer avgResponseTime = calculateAvgResponseTime(userId);
        if (avgResponseTime == null) {
            avgResponseTime = 0;
        }

        // 构建当前等级信息
        CreditLevelInfo currentLevelInfo = buildLevelInfo(creditLevel);
        
        // 构建下一等级信息
        CreditLevel nextLevel = getNextLevel(creditLevel);
        CreditLevelInfo nextLevelInfo = nextLevel != null ? buildLevelInfo(nextLevel) : null;
        
        // 计算到下一等级的进度（基于订单数量）
        Double progressToNextLevel = calculateProgress(orderCount, creditLevel, nextLevel);

        return UserCreditInfoResponse.builder()
                .userId(userId)
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .creditScore(creditLevel.getMinOrders())
                .creditLevel(creditLevel)
                .orderCount(orderCount)
                .positiveRate(positiveRate)
                .avgResponseTime(avgResponseTime)
                .currentLevelInfo(currentLevelInfo)
                .nextLevelInfo(nextLevelInfo)
                .progressToNextLevel(progressToNextLevel)
                .build();
    }

    /**
     * 构建等级信息
     */
    private CreditLevelInfo buildLevelInfo(CreditLevel level) {
        return CreditLevelInfo.builder()
                .level(level.name())
                .name(level.getDescription())
                .color(getLevelColor(level))
                .minOrders(level.getMinOrders())
                .maxOrders(level.getMaxOrders())
                .build();
    }

    /**
     * 获取等级颜色
     */
    private String getLevelColor(CreditLevel level) {
        switch (level) {
            case NEWBIE: return "#95a5a6";
            case BRONZE: return "#cd7f32";
            case SILVER: return "#c0c0c0";
            case GOLD: return "#ffd700";
            case DIAMOND: return "#b9f2ff";
            default: return "#95a5a6";
        }
    }

    /**
     * 获取下一等级
     */
    private CreditLevel getNextLevel(CreditLevel current) {
        CreditLevel[] levels = CreditLevel.values();
        for (int i = 0; i < levels.length - 1; i++) {
            if (levels[i] == current) {
                return levels[i + 1];
            }
        }
        return null; // 已是最高等级
    }

    /**
     * 计算到下一等级的进度
     */
    private Double calculateProgress(long currentOrders, CreditLevel current, CreditLevel next) {
        if (next == null) {
            return 1.0; // 已是最高等级
        }
        
        int currentMin = current.getMinOrders();
        int nextMin = next.getMinOrders();
        
        if (nextMin <= currentMin) {
            return 1.0;
        }
        
        double progress = (double) (currentOrders - currentMin) / (nextMin - currentMin);
        return Math.min(Math.max(progress, 0.0), 1.0);
    }
}
