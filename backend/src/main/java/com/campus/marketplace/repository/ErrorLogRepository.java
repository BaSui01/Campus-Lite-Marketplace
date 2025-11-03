package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ErrorLog;
import com.campus.marketplace.common.enums.ErrorSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 错误日志Repository
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    /**
     * 查询未解决的错误
     * 
     * @return 未解决的错误列表
     */
    @Query("SELECT e FROM ErrorLog e WHERE e.isResolved = false ORDER BY e.errorTime DESC")
    List<ErrorLog> findUnresolvedErrors();

    /**
     * 查询指定严重程度的错误
     * 
     * @param severity 严重程度
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 错误列表
     */
    @Query("SELECT e FROM ErrorLog e WHERE e.severity = :severity " +
           "AND e.errorTime BETWEEN :startTime AND :endTime ORDER BY e.errorTime DESC")
    List<ErrorLog> findBySeverityAndTimeRange(
        @Param("severity") ErrorSeverity severity,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计指定时间范围内的错误数量（按严重程度分组）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Query("SELECT e.severity, COUNT(e) FROM ErrorLog e " +
           "WHERE e.errorTime BETWEEN :startTime AND :endTime " +
           "GROUP BY e.severity")
    List<Object[]> countBySeverityAndTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询最近的错误（未告警）
     * 
     * @param severity 严重程度
     * @param since 起始时间
     * @return 未告警的错误列表
     */
    @Query("SELECT e FROM ErrorLog e WHERE e.severity = :severity " +
           "AND e.isAlerted = false AND e.errorTime >= :since ORDER BY e.errorTime DESC")
    List<ErrorLog> findUnalertedErrors(
        @Param("severity") ErrorSeverity severity,
        @Param("since") LocalDateTime since
    );

    /**
     * 统计错误类型分布
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 错误类型统计
     */
    @Query("SELECT e.errorType, COUNT(e) FROM ErrorLog e " +
           "WHERE e.errorTime BETWEEN :startTime AND :endTime " +
           "GROUP BY e.errorType ORDER BY COUNT(e) DESC")
    List<Object[]> countByErrorType(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 删除指定时间之前的日志
     * 
     * @param before 截止时间
     */
    void deleteByErrorTimeBefore(LocalDateTime before);
}
