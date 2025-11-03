package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.ApiPerformanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API性能日志Repository
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Repository
public interface ApiPerformanceLogRepository extends JpaRepository<ApiPerformanceLog, Long> {

    /**
     * 查询慢查询日志
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 慢查询日志列表
     */
    @Query("SELECT a FROM ApiPerformanceLog a WHERE a.isSlow = true " +
           "AND a.requestTime BETWEEN :startTime AND :endTime ORDER BY a.durationMs DESC")
    List<ApiPerformanceLog> findSlowQueries(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询指定端点的平均响应时间
     * 
     * @param endpoint 端点
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均响应时间（毫秒）
     */
    @Query("SELECT AVG(a.durationMs) FROM ApiPerformanceLog a " +
           "WHERE a.endpoint = :endpoint " +
           "AND a.requestTime BETWEEN :startTime AND :endTime")
    Double getAverageDuration(
        @Param("endpoint") String endpoint,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计端点调用次数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 端点调用统计
     */
    @Query("SELECT a.endpoint, COUNT(a), AVG(a.durationMs), MAX(a.durationMs) " +
           "FROM ApiPerformanceLog a " +
           "WHERE a.requestTime BETWEEN :startTime AND :endTime " +
           "GROUP BY a.endpoint ORDER BY COUNT(a) DESC")
    List<Object[]> getEndpointStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计错误请求
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 错误请求列表
     */
    @Query("SELECT a FROM ApiPerformanceLog a WHERE a.isSuccess = false " +
           "AND a.requestTime BETWEEN :startTime AND :endTime " +
           "ORDER BY a.requestTime DESC")
    List<ApiPerformanceLog> findErrorRequests(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 删除指定时间之前的日志
     * 
     * @param before 截止时间
     */
    void deleteByRequestTimeBefore(LocalDateTime before);

    /**
     * 查询QPS统计（按分钟聚合）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return QPS统计数据
     */
    @Query(value = "SELECT DATE_TRUNC('minute', request_time) as minute, COUNT(*) as count " +
                   "FROM t_api_performance_log " +
                   "WHERE request_time BETWEEN :startTime AND :endTime " +
                   "GROUP BY DATE_TRUNC('minute', request_time) " +
                   "ORDER BY minute", nativeQuery = true)
    List<Object[]> getQpsStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
