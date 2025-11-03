package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.entity.DisputeArbitration;
import com.campus.marketplace.common.enums.ArbitrationResult;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仲裁信息DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArbitrationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 仲裁ID
     */
    private Long id;

    /**
     * 纠纷ID
     */
    private Long disputeId;

    /**
     * 仲裁员ID
     */
    private Long arbitratorId;

    /**
     * 仲裁员昵称
     */
    private String arbitratorNickname;

    /**
     * 仲裁结果
     */
    private ArbitrationResult result;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 仲裁理由
     */
    private String reason;

    /**
     * 买家证据分析
     */
    private String buyerEvidenceAnalysis;

    /**
     * 卖家证据分析
     */
    private String sellerEvidenceAnalysis;

    /**
     * 仲裁时间
     */
    private LocalDateTime arbitratedAt;

    /**
     * 是否已执行
     */
    private boolean executed;

    /**
     * 执行时间
     */
    private LocalDateTime executedAt;

    /**
     * 执行说明
     */
    private String executionNote;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 从实体转换为DTO
     */
    public static ArbitrationDTO from(DisputeArbitration arbitration) {
        if (arbitration == null) {
            return null;
        }

        return ArbitrationDTO.builder()
                .id(arbitration.getId())
                .disputeId(arbitration.getDisputeId())
                .arbitratorId(arbitration.getArbitratorId())
                .arbitratorNickname(arbitration.getArbitrator() != null ? arbitration.getArbitrator().getNickname() : null)
                .result(arbitration.getResult())
                .refundAmount(arbitration.getRefundAmount())
                .reason(arbitration.getReason())
                .buyerEvidenceAnalysis(arbitration.getBuyerEvidenceAnalysis())
                .sellerEvidenceAnalysis(arbitration.getSellerEvidenceAnalysis())
                .arbitratedAt(arbitration.getArbitratedAt())
                .executed(arbitration.isExecuted())
                .executedAt(arbitration.getExecutedAt())
                .executionNote(arbitration.getExecutionNote())
                .createdAt(arbitration.getCreatedAt())
                .build();
    }

    /**
     * 检查是否需要退款
     */
    public boolean requiresRefund() {
        return this.result == ArbitrationResult.FULL_REFUND ||
               this.result == ArbitrationResult.PARTIAL_REFUND;
    }
}
