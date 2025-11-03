package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.MarketingCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 营销活动数据访问接口
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
@Repository
public interface MarketingCampaignRepository extends JpaRepository<MarketingCampaign, Long> {

    /**
     * 查询指定商家的所有活动
     */
    List<MarketingCampaign> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    /**
     * 查询指定商家指定状态的活动
     */
    List<MarketingCampaign> findByMerchantIdAndStatus(Long merchantId, String status);

    /**
     * 查询进行中的活动（当前时间在活动时间范围内）
     */
    @Query("SELECT mc FROM MarketingCampaign mc WHERE mc.status = 'RUNNING' " +
           "AND :now BETWEEN mc.startTime AND mc.endTime")
    List<MarketingCampaign> findRunningCampaigns(@Param("now") LocalDateTime now);

    /**
     * 查询即将开始的活动（未来24小时内）
     */
    @Query("SELECT mc FROM MarketingCampaign mc WHERE mc.status = 'APPROVED' " +
           "AND mc.startTime BETWEEN :now AND :future")
    List<MarketingCampaign> findUpcomingCampaigns(
            @Param("now") LocalDateTime now,
            @Param("future") LocalDateTime future
    );

    /**
     * 查询已过期但状态未更新的活动
     */
    @Query("SELECT mc FROM MarketingCampaign mc WHERE mc.status = 'RUNNING' " +
           "AND mc.endTime < :now")
    List<MarketingCampaign> findExpiredCampaigns(@Param("now") LocalDateTime now);

    /**
     * 统计指定商家的活动数量
     */
    long countByMerchantId(Long merchantId);

    /**
     * 统计指定商家进行中的活动数量
     */
    long countByMerchantIdAndStatus(Long merchantId, String status);
}
