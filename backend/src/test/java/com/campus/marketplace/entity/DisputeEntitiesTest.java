package com.campus.marketplace.entity;

import com.campus.marketplace.common.entity.*;
import com.campus.marketplace.common.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 纠纷仲裁系统实体类测试
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@DisplayName("纠纷仲裁系统实体测试")
class DisputeEntitiesTest {

    @Test
    @DisplayName("应该成功创建Dispute实体")
    void shouldCreateDisputeEntity() {
        // Arrange & Act
        Dispute dispute = Dispute.builder()
            .disputeCode("DSP-20251103-000001")
            .orderId(1L)
            .initiatorId(100L)
            .initiatorRole(DisputeRole.BUYER)
            .respondentId(200L)
            .disputeType(DisputeType.GOODS_MISMATCH)
            .description("商品与描述不符")
            .status(DisputeStatus.SUBMITTED)
            .negotiationDeadline(LocalDateTime.now().plusHours(48))
            .build();

        // Assert
        assertThat(dispute).isNotNull();
        assertThat(dispute.getDisputeCode()).isEqualTo("DSP-20251103-000001");
        assertThat(dispute.getOrderId()).isEqualTo(1L);
        assertThat(dispute.getInitiatorId()).isEqualTo(100L);
        assertThat(dispute.getInitiatorRole()).isEqualTo(DisputeRole.BUYER);
        assertThat(dispute.getRespondentId()).isEqualTo(200L);
        assertThat(dispute.getDisputeType()).isEqualTo(DisputeType.GOODS_MISMATCH);
        assertThat(dispute.getStatus()).isEqualTo(DisputeStatus.SUBMITTED);
    }

    @Test
    @DisplayName("应该成功创建DisputeEvidence实体")
    void shouldCreateDisputeEvidenceEntity() {
        // Arrange & Act
        DisputeEvidence evidence = DisputeEvidence.builder()
            .disputeId(1L)
            .uploaderId(100L)
            .uploaderRole(DisputeRole.BUYER)
            .evidenceType(EvidenceType.IMAGE)
            .fileUrl("https://example.com/evidence/img001.jpg")
            .fileName("商品照片.jpg")
            .fileSize(1024000L)
            .description("实物照片")
            .validity(EvidenceValidity.VALID)
            .build();

        // Assert
        assertThat(evidence).isNotNull();
        assertThat(evidence.getDisputeId()).isEqualTo(1L);
        assertThat(evidence.getUploaderId()).isEqualTo(100L);
        assertThat(evidence.getUploaderRole()).isEqualTo(DisputeRole.BUYER);
        assertThat(evidence.getEvidenceType()).isEqualTo(EvidenceType.IMAGE);
        assertThat(evidence.getFileUrl()).contains("img001.jpg");
        assertThat(evidence.getValidity()).isEqualTo(EvidenceValidity.VALID);
    }

    @Test
    @DisplayName("应该成功创建DisputeNegotiation实体")
    void shouldCreateDisputeNegotiationEntity() {
        // Arrange & Act
        DisputeNegotiation negotiation = DisputeNegotiation.builder()
            .disputeId(1L)
            .senderId(100L)
            .senderRole(DisputeRole.BUYER)
            .messageType(NegotiationMessageType.PROPOSAL)
            .content("建议全额退款")
            .proposedRefundAmount(new BigDecimal("99.99"))
            .proposalStatus(ProposalStatus.PENDING)
            .build();

        // Assert
        assertThat(negotiation).isNotNull();
        assertThat(negotiation.getDisputeId()).isEqualTo(1L);
        assertThat(negotiation.getSenderId()).isEqualTo(100L);
        assertThat(negotiation.getSenderRole()).isEqualTo(DisputeRole.BUYER);
        assertThat(negotiation.getMessageType()).isEqualTo(NegotiationMessageType.PROPOSAL);
        assertThat(negotiation.getProposedRefundAmount()).isEqualByComparingTo("99.99");
        assertThat(negotiation.getProposalStatus()).isEqualTo(ProposalStatus.PENDING);
    }

    @Test
    @DisplayName("应该成功创建DisputeArbitration实体")
    void shouldCreateDisputeArbitrationEntity() {
        // Arrange & Act
        DisputeArbitration arbitration = DisputeArbitration.builder()
            .disputeId(1L)
            .arbitratorId(999L)
            .result(ArbitrationResult.FULL_REFUND)
            .refundAmount(new BigDecimal("99.99"))
            .reason("商品确实存在质量问题，支持买家申诉")
            .arbitratedAt(LocalDateTime.now())
            .build();

        // Assert
        assertThat(arbitration).isNotNull();
        assertThat(arbitration.getDisputeId()).isEqualTo(1L);
        assertThat(arbitration.getArbitratorId()).isEqualTo(999L);
        assertThat(arbitration.getResult()).isEqualTo(ArbitrationResult.FULL_REFUND);
        assertThat(arbitration.getRefundAmount()).isEqualByComparingTo("99.99");
        assertThat(arbitration.getReason()).contains("质量问题");
    }

    @Test
    @DisplayName("Dispute实体应该继承BaseEntity的审计字段")
    void disputeShouldInheritAuditFields() {
        // Arrange
        Dispute dispute = new Dispute();

        // Act - 模拟JPA审计功能
        dispute.setCreatedAt(LocalDateTime.now());
        dispute.setUpdatedAt(LocalDateTime.now());

        // Assert
        assertThat(dispute.getCreatedAt()).isNotNull();
        assertThat(dispute.getUpdatedAt()).isNotNull();
        assertThat(dispute.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Dispute实体应该支持软删除")
    void disputeShouldSupportSoftDelete() {
        // Arrange
        Dispute dispute = new Dispute();

        // Act
        dispute.markDeleted();

        // Assert
        assertThat(dispute.isDeleted()).isTrue();
        assertThat(dispute.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("DisputeNegotiation应该区分TEXT和PROPOSAL消息")
    void negotiationShouldDistinguishMessageTypes() {
        // Arrange & Act
        DisputeNegotiation textMessage = DisputeNegotiation.builder()
            .messageType(NegotiationMessageType.TEXT)
            .content("请问什么时候发货？")
            .build();

        DisputeNegotiation proposalMessage = DisputeNegotiation.builder()
            .messageType(NegotiationMessageType.PROPOSAL)
            .content("建议退款50%")
            .proposedRefundAmount(new BigDecimal("50.00"))
            .proposalStatus(ProposalStatus.PENDING)
            .build();

        // Assert
        assertThat(textMessage.getMessageType()).isEqualTo(NegotiationMessageType.TEXT);
        assertThat(textMessage.getProposedRefundAmount()).isNull();

        assertThat(proposalMessage.getMessageType()).isEqualTo(NegotiationMessageType.PROPOSAL);
        assertThat(proposalMessage.getProposedRefundAmount()).isNotNull();
    }

    @Test
    @DisplayName("DisputeEvidence应该支持多种证据类型")
    void evidenceShouldSupportMultipleTypes() {
        // Arrange & Act
        DisputeEvidence imageEvidence = DisputeEvidence.builder()
            .evidenceType(EvidenceType.IMAGE)
            .fileName("photo.jpg")
            .build();

        DisputeEvidence videoEvidence = DisputeEvidence.builder()
            .evidenceType(EvidenceType.VIDEO)
            .fileName("video.mp4")
            .build();

        DisputeEvidence chatEvidence = DisputeEvidence.builder()
            .evidenceType(EvidenceType.CHAT_RECORD)
            .fileName("chat.png")
            .build();

        // Assert
        assertThat(imageEvidence.getEvidenceType()).isEqualTo(EvidenceType.IMAGE);
        assertThat(videoEvidence.getEvidenceType()).isEqualTo(EvidenceType.VIDEO);
        assertThat(chatEvidence.getEvidenceType()).isEqualTo(EvidenceType.CHAT_RECORD);
    }

    @Test
    @DisplayName("DisputeArbitration应该包含完整的仲裁信息")
    void arbitrationShouldContainCompleteInfo() {
        // Arrange
        LocalDateTime arbitratedTime = LocalDateTime.now();

        // Act
        DisputeArbitration arbitration = DisputeArbitration.builder()
            .disputeId(1L)
            .arbitratorId(999L)
            .result(ArbitrationResult.PARTIAL_REFUND)
            .refundAmount(new BigDecimal("50.00"))
            .reason("双方各承担50%责任")
            .arbitratedAt(arbitratedTime)
            .build();

        // Assert
        assertThat(arbitration.getDisputeId()).isEqualTo(1L);
        assertThat(arbitration.getArbitratorId()).isEqualTo(999L);
        assertThat(arbitration.getResult()).isEqualTo(ArbitrationResult.PARTIAL_REFUND);
        assertThat(arbitration.getRefundAmount()).isEqualByComparingTo("50.00");
        assertThat(arbitration.getReason()).isNotBlank();
        assertThat(arbitration.getArbitratedAt()).isEqualTo(arbitratedTime);
    }
}
