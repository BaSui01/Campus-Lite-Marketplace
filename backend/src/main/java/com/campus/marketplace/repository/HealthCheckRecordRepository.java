package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.HealthCheckRecord;
import com.campus.marketplace.common.enums.HealthStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康检查记录Repository
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface HealthCheckRecordRepository extends JpaRepository<HealthCheckRecord, Long> {

    /**
     * 查询最近的健康检查记录
     * 
     * @param since 起始时间
     * @return 健康检查记录列表
     */
    @Query("SELECT h FROM HealthCheckRecord h WHERE h.checkTime >= :since ORDER BY h.checkTime DESC")
    List<HealthCheckRecord> findRecentRecords(@Param("since") LocalDateTime since);

    /**
     * 查询指定状态的健康检查记录数量
     * 
     * @param status 健康状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 记录数量
     */
    @Query("SELECT COUNT(h) FROM HealthCheckRecord h WHERE h.overallStatus = :status " +
           "AND h.checkTime BETWEEN :startTime AND :endTime")
    long countByStatusAndTimeRange(
        @Param("status") HealthStatus status,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询平均响应时间
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均响应时间（毫秒）
     */
    @Query("SELECT AVG(h.responseTimeMs) FROM HealthCheckRecord h " +
           "WHERE h.checkTime BETWEEN :startTime AND :endTime")
    Double getAverageResponseTime(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询最近的异常记录
     * 
     * @param since 起始时间
     * @return 异常记录列表
     */
    @Query("SELECT h FROM HealthCheckRecord h WHERE h.overallStatus != 'HEALTHY' " +
           "AND h.checkTime >= :since ORDER BY h.checkTime DESC")
    List<HealthCheckRecord> findRecentUnhealthyRecords(@Param("since") LocalDateTime since);

    /**
     * 删除指定时间之前的记录（数据清理）
     * 
     * @param before 截止时间
     */
    void deleteByCheckTimeBefore(LocalDateTime before);
}
