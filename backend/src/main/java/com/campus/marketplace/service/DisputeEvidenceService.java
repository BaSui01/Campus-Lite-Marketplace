package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.EvidenceDTO;
import com.campus.marketplace.common.dto.EvidenceSummaryDTO;
import com.campus.marketplace.common.dto.request.UploadEvidenceRequest;
import com.campus.marketplace.common.enums.EvidenceValidity;

import java.util.List;

/**
 * 纠纷证据服务接口
 *
 * 负责证据的上传、查询、评估和删除
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
public interface DisputeEvidenceService {

    /**
     * 上传证据
     *
     * 买家或卖家上传证据材料支持纠纷主张
     *
     * @param request 上传请求
     * @param uploaderId 上传者ID
     * @return 证据ID
     * @throws com.campus.marketplace.common.exception.BusinessException 纠纷不存在、用户非参与方等
     */
    Long uploadEvidence(UploadEvidenceRequest request, Long uploaderId);

    /**
     * 查询纠纷的所有证据
     *
     * @param disputeId 纠纷ID
     * @return 证据列表
     */
    List<EvidenceDTO> getDisputeEvidence(Long disputeId);

    /**
     * 查询买家上传的证据
     *
     * @param disputeId 纠纷ID
     * @return 买家证据列表
     */
    List<EvidenceDTO> getBuyerEvidence(Long disputeId);

    /**
     * 查询卖家上传的证据
     *
     * @param disputeId 纠纷ID
     * @return 卖家证据列表
     */
    List<EvidenceDTO> getSellerEvidence(Long disputeId);

    /**
     * 评估证据有效性
     *
     * 仲裁员对证据进行有效性评估
     *
     * @param evidenceId 证据ID
     * @param validity 有效性
     * @param reason 评估理由
     * @param evaluatorId 评估人ID
     * @return 是否成功
     * @throws com.campus.marketplace.common.exception.BusinessException 证据不存在、已评估等
     */
    boolean evaluateEvidence(Long evidenceId, EvidenceValidity validity,
                             String reason, Long evaluatorId);

    /**
     * 查询证据统计信息
     *
     * @param disputeId 纠纷ID
     * @return 统计信息
     */
    EvidenceSummaryDTO getEvidenceSummary(Long disputeId);

    /**
     * 查询待评估的证据
     *
     * @param disputeId 纠纷ID
     * @return 待评估证据列表
     */
    List<EvidenceDTO> getUnevaluatedEvidence(Long disputeId);

    /**
     * 删除证据
     *
     * 仅上传者可以删除，且仅限未评估的证据
     *
     * @param evidenceId 证据ID
     * @param userId 用户ID
     * @return 是否成功
     * @throws com.campus.marketplace.common.exception.BusinessException 证据不存在、无权限、已评估等
     */
    boolean deleteEvidence(Long evidenceId, Long userId);
}
