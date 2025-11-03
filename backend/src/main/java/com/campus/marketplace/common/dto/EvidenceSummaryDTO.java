package com.campus.marketplace.common.dto;

import lombok.*;

import java.io.Serializable;

/**
 * 证据摘要DTO
 *
 * 用于快速展示纠纷的证据统计
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 纠纷ID
     */
    private Long disputeId;

    /**
     * 证据总数
     */
    private Long totalCount;

    /**
     * 买家证据总数
     */
    private Long buyerEvidenceCount;

    /**
     * 卖家证据总数
     */
    private Long sellerEvidenceCount;

    /**
     * 图片证据数
     */
    private Long imageCount;

    /**
     * 视频证据数
     */
    private Long videoCount;

    /**
     * 聊天记录证据数
     */
    private Long chatRecordCount;

    /**
     * 有效证据数
     */
    private Long validEvidenceCount;

    /**
     * 无效证据数
     */
    private Long invalidEvidenceCount;

    /**
     * 存疑证据数
     */
    private Long doubtfulEvidenceCount;

    /**
     * 待评估证据数
     */
    private Long unevaluatedEvidenceCount;

    /**
     * 检查买家是否提供证据
     */
    public boolean hasBuyerEvidence() {
        return buyerEvidenceCount != null && buyerEvidenceCount > 0;
    }

    /**
     * 检查卖家是否提供证据
     */
    public boolean hasSellerEvidence() {
        return sellerEvidenceCount != null && sellerEvidenceCount > 0;
    }

    /**
     * 检查是否有待评估证据
     */
    public boolean hasUnevaluatedEvidence() {
        return unevaluatedEvidenceCount != null && unevaluatedEvidenceCount > 0;
    }
}
