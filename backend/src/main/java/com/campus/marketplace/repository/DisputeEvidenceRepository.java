package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.DisputeEvidence;
import com.campus.marketplace.common.enums.DisputeRole;
import com.campus.marketplace.common.enums.EvidenceType;
import com.campus.marketplace.common.enums.EvidenceValidity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 纠纷证据数据访问接口
 *
 * 提供证据的CRUD操作和自定义查询
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Repository
public interface DisputeEvidenceRepository extends JpaRepository<DisputeEvidence, Long> {

    /**
     * 查询纠纷的所有证据
     */
    List<DisputeEvidence> findByDisputeIdOrderByCreatedAtAsc(Long disputeId);

    /**
     * 查询纠纷特定上传者的证据
     */
    List<DisputeEvidence> findByDisputeIdAndUploaderIdOrderByCreatedAtAsc(
            Long disputeId,
            Long uploaderId
    );

    /**
     * 查询纠纷特定角色的证据
     */
    List<DisputeEvidence> findByDisputeIdAndUploaderRoleOrderByCreatedAtAsc(
            Long disputeId,
            DisputeRole uploaderRole
    );

    /**
     * 查询纠纷特定类型的证据
     */
    List<DisputeEvidence> findByDisputeIdAndEvidenceTypeOrderByCreatedAtAsc(
            Long disputeId,
            EvidenceType evidenceType
    );

    /**
     * 统计纠纷的证据数量
     */
    long countByDisputeId(Long disputeId);

    /**
     * 统计纠纷特定角色的证据数量
     */
    long countByDisputeIdAndUploaderRole(Long disputeId, DisputeRole uploaderRole);

    /**
     * 统计纠纷特定有效性的证据数量
     */
    long countByDisputeIdAndValidity(Long disputeId, EvidenceValidity validity);

    /**
     * 查询待评估的证据
     */
    @Query("SELECT e FROM DisputeEvidence e WHERE e.disputeId = :disputeId " +
           "AND e.validity IS NULL ORDER BY e.createdAt ASC")
    List<DisputeEvidence> findUnevaluatedEvidence(@Param("disputeId") Long disputeId);

    /**
     * 查询特定有效性的证据
     */
    List<DisputeEvidence> findByDisputeIdAndValidityOrderByCreatedAtAsc(
            Long disputeId,
            EvidenceValidity validity
    );

    /**
     * 检查用户是否已上传证据
     */
    boolean existsByDisputeIdAndUploaderId(Long disputeId, Long uploaderId);

    /**
     * 统计评估人评估的证据数量
     */
    @Query("SELECT e.validity, COUNT(e) FROM DisputeEvidence e " +
           "WHERE e.evaluatedBy = :evaluatorId GROUP BY e.validity")
    List<Object[]> countByEvaluatorGroupByValidity(@Param("evaluatorId") Long evaluatorId);
}
