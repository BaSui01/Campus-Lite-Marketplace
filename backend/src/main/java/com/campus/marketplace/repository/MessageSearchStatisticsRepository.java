package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.MessageSearchStatisticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 消息搜索统计Repository
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Repository
public interface MessageSearchStatisticsRepository extends JpaRepository<MessageSearchStatisticsEntity, Long> {

    /**
     * 查找用户在指定纠纷和日期的统计记录
     */
    Optional<MessageSearchStatisticsEntity> findByUserIdAndDisputeIdAndSearchDate(
            Long userId, Long disputeId, LocalDate searchDate);

    /**
     * 查询用户最近的统计记录
     */
    Optional<MessageSearchStatisticsEntity> findFirstByUserIdAndDisputeIdOrderBySearchDateDesc(
            Long userId, Long disputeId);

    /**
     * 统计用户总搜索次数
     */
    @Query("SELECT COALESCE(SUM(s.totalSearches), 0) FROM MessageSearchStatisticsEntity s " +
           "WHERE s.userId = :userId AND s.disputeId = :disputeId")
    Long getTotalSearches(@Param("userId") Long userId, @Param("disputeId") Long disputeId);

    /**
     * 统计用户总成功搜索次数
     */
    @Query("SELECT COALESCE(SUM(s.successfulSearches), 0) FROM MessageSearchStatisticsEntity s " +
           "WHERE s.userId = :userId AND s.disputeId = :disputeId")
    Long getTotalSuccessfulSearches(@Param("userId") Long userId, @Param("disputeId") Long disputeId);

    /**
     * 删除旧的统计记录（保留最近90天）
     */
    void deleteBySearchDateBefore(LocalDate cutoff);
}