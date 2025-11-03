package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 纠纷数据访问接口
 *
 * 提供纠纷的CRUD操作和自定义查询
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    /**
     * 根据纠纷编号查询纠纷
     */
    Optional<Dispute> findByDisputeCode(String disputeCode);

    /**
     * 根据纠纷编号查询纠纷（包含关联数据）
     */
    @EntityGraph(attributePaths = {"order", "initiator", "respondent", "arbitrator"})
    @Query("SELECT d FROM Dispute d WHERE d.disputeCode = :disputeCode")
    Optional<Dispute> findByDisputeCodeWithDetails(@Param("disputeCode") String disputeCode);

    /**
     * 根据订单ID查询纠纷
     */
    Optional<Dispute> findByOrderId(Long orderId);

    /**
     * 检查订单是否已有纠纷
     */
    boolean existsByOrderId(Long orderId);

    /**
     * 查询用户的纠纷列表（作为发起人）
     */
    @Query("SELECT d FROM Dispute d WHERE d.initiatorId = :userId " +
           "AND (:status IS NULL OR d.status = :status) " +
           "ORDER BY d.createdAt DESC")
    Page<Dispute> findByInitiatorIdWithStatus(
            @Param("userId") Long userId,
            @Param("status") DisputeStatus status,
            Pageable pageable
    );

    /**
     * 查询用户的纠纷列表（作为被投诉人）
     */
    @Query("SELECT d FROM Dispute d WHERE d.respondentId = :userId " +
           "AND (:status IS NULL OR d.status = :status) " +
           "ORDER BY d.createdAt DESC")
    Page<Dispute> findByRespondentIdWithStatus(
            @Param("userId") Long userId,
            @Param("status") DisputeStatus status,
            Pageable pageable
    );

    /**
     * 查询用户的所有纠纷（作为发起人或被投诉人）
     */
    @Query("SELECT d FROM Dispute d WHERE (d.initiatorId = :userId OR d.respondentId = :userId) " +
           "AND (:status IS NULL OR d.status = :status) " +
           "ORDER BY d.createdAt DESC")
    Page<Dispute> findByUserIdWithStatus(
            @Param("userId") Long userId,
            @Param("status") DisputeStatus status,
            Pageable pageable
    );

    /**
     * 查询仲裁员的纠纷列表
     */
    @Query("SELECT d FROM Dispute d WHERE d.arbitratorId = :arbitratorId " +
           "AND (:status IS NULL OR d.status = :status) " +
           "ORDER BY d.createdAt DESC")
    Page<Dispute> findByArbitratorIdWithStatus(
            @Param("arbitratorId") Long arbitratorId,
            @Param("status") DisputeStatus status,
            Pageable pageable
    );

    /**
     * 查询特定状态的纠纷列表
     */
    Page<Dispute> findByStatusOrderByCreatedAtDesc(DisputeStatus status, Pageable pageable);

    /**
     * 查找协商超时的纠纷
     */
    @Query("SELECT d FROM Dispute d WHERE d.status = :status " +
           "AND d.negotiationDeadline < :now")
    List<Dispute> findExpiredNegotiations(
            @Param("status") DisputeStatus status,
            @Param("now") LocalDateTime now
    );

    /**
     * 查找仲裁超时的纠纷
     */
    @Query("SELECT d FROM Dispute d WHERE d.status = :status " +
           "AND d.arbitrationDeadline < :now")
    List<Dispute> findExpiredArbitrations(
            @Param("status") DisputeStatus status,
            @Param("now") LocalDateTime now
    );

    /**
     * 统计特定状态的纠纷数量
     */
    long countByStatus(DisputeStatus status);

    /**
     * 统计用户在指定时间之后的纠纷数量
     */
    long countByInitiatorIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);

    /**
     * 统计仲裁员处理的纠纷数量
     */
    @Query("SELECT d.status, COUNT(d) FROM Dispute d " +
           "WHERE d.arbitratorId = :arbitratorId GROUP BY d.status")
    List<Object[]> countByArbitratorIdGroupByStatus(@Param("arbitratorId") Long arbitratorId);

    /**
     * 查询待分配仲裁员的纠纷
     */
    @Query("SELECT d FROM Dispute d WHERE d.status = 'PENDING_ARBITRATION' " +
           "AND d.arbitratorId IS NULL ORDER BY d.createdAt ASC")
    List<Dispute> findUnassignedArbitrations(Pageable pageable);
}
