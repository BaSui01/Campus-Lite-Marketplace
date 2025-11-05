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
     * 查询慢查询日志（响应时间超过1000ms）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 慢查询日志列表
     */
    @Query("SELECT a FROM ApiPerformanceLog a WHERE a.responseTime > 1000 " +
           "AND a.createdAt BETWEEN :startTime AND :endTime ORDER BY a.responseTime DESC")
    List<ApiPerformanceLog> findSlowQueries(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询指定API路径的平均响应时间
     * 
     * @param apiPath API路径
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 平均响应时间（毫秒）
     */
    @Query("SELECT AVG(a.responseTime) FROM ApiPerformanceLog a " +
           "WHERE a.apiPath = :apiPath " +
           "AND a.createdAt BETWEEN :startTime AND :endTime")
    Double getAverageDuration(
        @Param("apiPath") String apiPath,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计API路径调用次数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return API路径调用统计
     */
    @Query("SELECT a.apiPath, COUNT(a), AVG(a.responseTime), MAX(a.responseTime) " +
           "FROM ApiPerformanceLog a " +
           "WHERE a.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY a.apiPath ORDER BY COUNT(a) DESC")
    List<Object[]> getEndpointStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计错误请求（HTTP状态码 >= 400）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 错误请求列表
     */
    @Query("SELECT a FROM ApiPerformanceLog a WHERE a.statusCode >= 400 " +
           "AND a.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY a.createdAt DESC")
    List<ApiPerformanceLog> findErrorRequests(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 删除指定时间之前的日志
     * 
     * @param before 截止时间
     */
    void deleteByCreatedAtBefore(LocalDateTime before);

    /**
     * 查询QPS统计（按分钟聚合）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return QPS统计数据
     */
    @Query(value = "SELECT DATE_TRUNC('minute', created_at) as minute, COUNT(*) as count " +
                   "FROM t_api_performance_log " +
                   "WHERE created_at BETWEEN :startTime AND :endTime " +
                   "GROUP BY DATE_TRUNC('minute', created_at) " +
                   "ORDER BY minute", nativeQuery = true)
    List<Object[]> getQpsStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
