package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.DisputeArbitration;
import com.campus.marketplace.common.enums.ArbitrationResult;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 纠纷仲裁数据访问接口
 *
 * 提供仲裁记录的CRUD操作和自定义查询
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Repository
public interface DisputeArbitrationRepository extends JpaRepository<DisputeArbitration, Long> {

    /**
     * 根据纠纷ID查询仲裁记录
     */
    Optional<DisputeArbitration> findByDisputeId(Long disputeId);

    /**
     * 根据纠纷ID查询仲裁记录（包含关联数据）
     */
    @EntityGraph(attributePaths = {"dispute", "arbitrator"})
    @Query("SELECT a FROM DisputeArbitration a WHERE a.disputeId = :disputeId")
    Optional<DisputeArbitration> findByDisputeIdWithDetails(@Param("disputeId") Long disputeId);

    /**
     * 检查纠纷是否已有仲裁记录
     */
    boolean existsByDisputeId(Long disputeId);

    /**
     * 查询仲裁员处理的仲裁记录
     */
    List<DisputeArbitration> findByArbitratorIdOrderByArbitratedAtDesc(Long arbitratorId);

    /**
     * 查询仲裁员在指定时间段内的仲裁记录
     */
    @Query("SELECT a FROM DisputeArbitration a WHERE a.arbitratorId = :arbitratorId " +
           "AND a.arbitratedAt BETWEEN :startTime AND :endTime " +
           "ORDER BY a.arbitratedAt DESC")
    List<DisputeArbitration> findByArbitratorAndTimeRange(
            @Param("arbitratorId") Long arbitratorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询特定结果的仲裁记录
     */
    List<DisputeArbitration> findByResultOrderByArbitratedAtDesc(ArbitrationResult result);

    /**
     * 查询待执行的仲裁记录
     */
    @Query("SELECT a FROM DisputeArbitration a WHERE a.executed = false " +
           "AND a.result IN ('FULL_REFUND', 'PARTIAL_REFUND') " +
           "ORDER BY a.arbitratedAt ASC")
    List<DisputeArbitration> findPendingExecution();

    /**
     * 统计仲裁员处理的仲裁数量
     */
    long countByArbitratorId(Long arbitratorId);

    /**
     * 统计仲裁员特定结果的仲裁数量
     */
    long countByArbitratorIdAndResult(Long arbitratorId, ArbitrationResult result);

    /**
     * 统计仲裁结果分布
     */
    @Query("SELECT a.result, COUNT(a) FROM DisputeArbitration a GROUP BY a.result")
    List<Object[]> countByResultDistribution();

    /**
     * 统计仲裁员的仲裁结果分布
     */
    @Query("SELECT a.result, COUNT(a) FROM DisputeArbitration a " +
           "WHERE a.arbitratorId = :arbitratorId GROUP BY a.result")
    List<Object[]> countByArbitratorGroupByResult(@Param("arbitratorId") Long arbitratorId);

    /**
     * 查询指定时间段内的仲裁记录
     */
    @Query("SELECT a FROM DisputeArbitration a " +
           "WHERE a.arbitratedAt BETWEEN :startTime AND :endTime " +
           "ORDER BY a.arbitratedAt DESC")
    List<DisputeArbitration> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计待执行的仲裁数量
     */
    @Query("SELECT COUNT(a) FROM DisputeArbitration a WHERE a.executed = false " +
           "AND a.result IN ('FULL_REFUND', 'PARTIAL_REFUND')")
    long countPendingExecution();

    /**
     * 查询仲裁员最近处理的仲裁
     */
    @Query("SELECT a FROM DisputeArbitration a WHERE a.arbitratorId = :arbitratorId " +
           "ORDER BY a.arbitratedAt DESC")
    List<DisputeArbitration> findRecentByArbitrator(
            @Param("arbitratorId") Long arbitratorId,
            org.springframework.data.domain.Pageable pageable
    );

    /**
     * 计算仲裁员的平均处理时间（从纠纷创建到仲裁完成）
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, d.createdAt, a.arbitratedAt)) " +
           "FROM DisputeArbitration a JOIN Dispute d ON a.disputeId = d.id " +
           "WHERE a.arbitratorId = :arbitratorId")
    Double calculateAverageProcessingTime(@Param("arbitratorId") Long arbitratorId);

    /**
     * 统计仲裁员在指定时间之后的仲裁数量
     */
    long countByArbitratorIdAndArbitratedAtAfter(Long arbitratorId, LocalDateTime arbitratedAt);
}
