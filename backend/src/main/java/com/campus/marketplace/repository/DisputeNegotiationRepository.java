package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.DisputeNegotiation;
import com.campus.marketplace.common.enums.DisputeRole;
import com.campus.marketplace.common.enums.NegotiationMessageType;
import com.campus.marketplace.common.enums.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 纠纷协商数据访问接口
 *
 * 提供协商消息和方案的CRUD操作和自定义查询
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Repository
public interface DisputeNegotiationRepository extends JpaRepository<DisputeNegotiation, Long> {

    /**
     * 查询纠纷的所有协商消息
     */
    List<DisputeNegotiation> findByDisputeIdOrderByCreatedAtAsc(Long disputeId);

    /**
     * 查询纠纷特定类型的消息
     */
    List<DisputeNegotiation> findByDisputeIdAndMessageTypeOrderByCreatedAtAsc(
            Long disputeId,
            NegotiationMessageType messageType
    );

    /**
     * 查询纠纷特定发送者的消息
     */
    List<DisputeNegotiation> findByDisputeIdAndSenderIdOrderByCreatedAtAsc(
            Long disputeId,
            Long senderId
    );

    /**
     * 查询纠纷特定角色的消息
     */
    List<DisputeNegotiation> findByDisputeIdAndSenderRoleOrderByCreatedAtAsc(
            Long disputeId,
            DisputeRole senderRole
    );

    /**
     * 查询纠纷的所有方案
     */
    @Query("SELECT n FROM DisputeNegotiation n WHERE n.disputeId = :disputeId " +
           "AND n.messageType = 'PROPOSAL' ORDER BY n.createdAt ASC")
    List<DisputeNegotiation> findProposalsByDisputeId(@Param("disputeId") Long disputeId);

    /**
     * 查询纠纷待响应的方案
     */
    @Query("SELECT n FROM DisputeNegotiation n WHERE n.disputeId = :disputeId " +
           "AND n.messageType = 'PROPOSAL' AND n.proposalStatus = 'PENDING' " +
           "ORDER BY n.createdAt ASC")
    List<DisputeNegotiation> findPendingProposals(@Param("disputeId") Long disputeId);

    /**
     * 查询纠纷最新的待响应方案
     */
    @Query("SELECT n FROM DisputeNegotiation n WHERE n.disputeId = :disputeId " +
           "AND n.messageType = 'PROPOSAL' AND n.proposalStatus = 'PENDING' " +
           "ORDER BY n.createdAt DESC")
    Optional<DisputeNegotiation> findLatestPendingProposal(@Param("disputeId") Long disputeId);

    /**
     * 查询纠纷已接受的方案
     */
    @Query("SELECT n FROM DisputeNegotiation n WHERE n.disputeId = :disputeId " +
           "AND n.messageType = 'PROPOSAL' AND n.proposalStatus = 'ACCEPTED' " +
           "ORDER BY n.createdAt DESC")
    Optional<DisputeNegotiation> findAcceptedProposal(@Param("disputeId") Long disputeId);

    /**
     * 统计纠纷的消息数量
     */
    long countByDisputeId(Long disputeId);

    /**
     * 统计纠纷特定类型的消息数量
     */
    long countByDisputeIdAndMessageType(Long disputeId, NegotiationMessageType messageType);

    /**
     * 统计纠纷特定角色的消息数量
     */
    long countByDisputeIdAndSenderRole(Long disputeId, DisputeRole senderRole);

    /**
     * 统计纠纷特定状态的方案数量
     */
    @Query("SELECT COUNT(n) FROM DisputeNegotiation n WHERE n.disputeId = :disputeId " +
           "AND n.messageType = 'PROPOSAL' AND n.proposalStatus = :status")
    long countProposalsByStatus(
            @Param("disputeId") Long disputeId,
            @Param("status") ProposalStatus status
    );

    /**
     * 检查纠纷是否有待响应的方案
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END " +
           "FROM DisputeNegotiation n WHERE n.disputeId = :disputeId " +
           "AND n.messageType = 'PROPOSAL' AND n.proposalStatus = 'PENDING'")
    boolean hasPendingProposal(@Param("disputeId") Long disputeId);

    /**
     * 检查纠纷是否达成协议
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END " +
           "FROM DisputeNegotiation n WHERE n.disputeId = :disputeId " +
           "AND n.messageType = 'PROPOSAL' AND n.proposalStatus = 'ACCEPTED'")
    boolean hasAcceptedProposal(@Param("disputeId") Long disputeId);

    /**
     * 查询用户发送的方案
     */
    @Query("SELECT n FROM DisputeNegotiation n WHERE n.senderId = :userId " +
           "AND n.messageType = 'PROPOSAL' ORDER BY n.createdAt DESC")
    List<DisputeNegotiation> findProposalsByUserId(@Param("userId") Long userId);

    /**
     * 统计方案状态分布
     */
    @Query("SELECT n.proposalStatus, COUNT(n) FROM DisputeNegotiation n " +
           "WHERE n.disputeId = :disputeId AND n.messageType = 'PROPOSAL' " +
           "GROUP BY n.proposalStatus")
    List<Object[]> countProposalStatusDistribution(@Param("disputeId") Long disputeId);
}
