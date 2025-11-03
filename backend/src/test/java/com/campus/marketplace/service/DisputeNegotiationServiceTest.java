package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.NegotiationMessageDTO;
import com.campus.marketplace.common.dto.request.ProposeDisputeRequest;
import com.campus.marketplace.common.dto.request.RespondProposalRequest;
import com.campus.marketplace.common.dto.request.SendNegotiationRequest;
import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.entity.DisputeNegotiation;
import com.campus.marketplace.common.enums.*;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.DisputeNegotiationRepository;
import com.campus.marketplace.repository.DisputeRepository;
import com.campus.marketplace.service.impl.DisputeNegotiationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 纠纷协商服务测试类
 *
 * 遵循TDD原则，测试先行！
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("纠纷协商服务测试")
class DisputeNegotiationServiceTest {

    @Mock
    private DisputeNegotiationRepository negotiationRepository;

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DisputeNegotiationServiceImpl negotiationService;

    private Dispute testDispute;
    private DisputeNegotiation textMessage;
    private DisputeNegotiation proposalMessage;

    @BeforeEach
    void setUp() {
        // 初始化纠纷
        testDispute = new Dispute();
        testDispute.setId(1L);
        testDispute.setDisputeCode("DSP-20251103-000001");
        testDispute.setOrderId(123L);
        testDispute.setInitiatorId(100L);
        testDispute.setInitiatorRole(DisputeRole.BUYER);
        testDispute.setRespondentId(200L);
        testDispute.setStatus(DisputeStatus.NEGOTIATING);

        // 初始化文字消息
        textMessage = new DisputeNegotiation();
        textMessage.setId(1L);
        textMessage.setDisputeId(1L);
        textMessage.setSenderId(100L);
        textMessage.setSenderRole(DisputeRole.BUYER);
        textMessage.setMessageType(NegotiationMessageType.TEXT);
        textMessage.setContent("商品与描述不符，要求退款");
        textMessage.setCreatedAt(LocalDateTime.now());

        // 初始化方案消息
        proposalMessage = new DisputeNegotiation();
        proposalMessage.setId(2L);
        proposalMessage.setDisputeId(1L);
        proposalMessage.setSenderId(200L);
        proposalMessage.setSenderRole(DisputeRole.SELLER);
        proposalMessage.setMessageType(NegotiationMessageType.PROPOSAL);
        proposalMessage.setContent("同意退款50元解决此纠纷");
        proposalMessage.setProposedRefundAmount(new BigDecimal("50.00"));
        proposalMessage.setProposalStatus(ProposalStatus.PENDING);
        proposalMessage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("发送文字消息 - 应该成功发送并返回消息ID")
    void sendTextMessage_ShouldSendSuccessfully() {
        // Arrange
        SendNegotiationRequest request = new SendNegotiationRequest(1L, "商品与描述不符");
        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(negotiationRepository.save(any(DisputeNegotiation.class)))
                .thenAnswer(invocation -> {
                    DisputeNegotiation neg = invocation.getArgument(0);
                    neg.setId(1L);
                    return neg;
                });

        // Act
        Long messageId = negotiationService.sendTextMessage(request, 100L);

        // Assert
        assertThat(messageId).isNotNull().isEqualTo(1L);
        verify(negotiationRepository, times(1)).save(any(DisputeNegotiation.class));
        verify(notificationService, times(1)).sendNotification(
                anyLong(), any(), anyString(), anyString(), anyLong(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("发送文字消息 - 纠纷不存在时应该抛出异常")
    void sendTextMessage_ShouldThrowException_WhenDisputeNotFound() {
        // Arrange
        SendNegotiationRequest request = new SendNegotiationRequest(999L, "测试消息");
        when(disputeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> negotiationService.sendTextMessage(request, 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("纠纷不存在");
    }

    @Test
    @DisplayName("提出解决方案 - 应该成功提出方案")
    void proposeResolution_ShouldProposeSuccessfully() {
        // Arrange
        ProposeDisputeRequest request = ProposeDisputeRequest.builder()
                .disputeId(1L)
                .content("同意退款50元解决纠纷，这是一个合理的解决方案")
                .proposedRefundAmount(new BigDecimal("50.00"))
                .build();

        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(negotiationRepository.hasPendingProposal(anyLong())).thenReturn(false);
        when(negotiationRepository.save(any(DisputeNegotiation.class)))
                .thenAnswer(invocation -> {
                    DisputeNegotiation neg = invocation.getArgument(0);
                    neg.setId(2L);
                    return neg;
                });

        // Act
        Long proposalId = negotiationService.proposeResolution(request, 200L);

        // Assert
        assertThat(proposalId).isNotNull().isEqualTo(2L);
        verify(negotiationRepository, times(1)).save(any(DisputeNegotiation.class));
        verify(notificationService, times(1)).sendNotification(
                anyLong(), any(), anyString(), anyString(), anyLong(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("提出解决方案 - 已有待响应方案时应该抛出异常")
    void proposeResolution_ShouldThrowException_WhenPendingProposalExists() {
        // Arrange
        ProposeDisputeRequest request = ProposeDisputeRequest.builder()
                .disputeId(1L)
                .content("同意退款50元解决纠纷，这是一个合理的解决方案")
                .proposedRefundAmount(new BigDecimal("50.00"))
                .build();

        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(negotiationRepository.hasPendingProposal(anyLong())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> negotiationService.proposeResolution(request, 200L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已有待响应的方案");
    }

    @Test
    @DisplayName("响应方案 - 接受方案应该成功")
    void respondToProposal_Accept_ShouldSucceed() {
        // Arrange
        RespondProposalRequest request = RespondProposalRequest.builder()
                .proposalId(2L)
                .accepted(true)
                .responseNote("接受此方案")
                .build();

        when(negotiationRepository.findById(anyLong())).thenReturn(Optional.of(proposalMessage));
        when(negotiationRepository.save(any(DisputeNegotiation.class))).thenReturn(proposalMessage);
        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // Act
        boolean result = negotiationService.respondToProposal(request, 100L);

        // Assert
        assertThat(result).isTrue();
        assertThat(proposalMessage.getProposalStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(proposalMessage.getRespondedBy()).isEqualTo(100L);
        assertThat(proposalMessage.getRespondedAt()).isNotNull();
        verify(negotiationRepository, times(1)).save(proposalMessage);
    }

    @Test
    @DisplayName("响应方案 - 拒绝方案应该成功")
    void respondToProposal_Reject_ShouldSucceed() {
        // Arrange
        RespondProposalRequest request = RespondProposalRequest.builder()
                .proposalId(2L)
                .accepted(false)
                .responseNote("金额不够，无法接受")
                .build();

        when(negotiationRepository.findById(anyLong())).thenReturn(Optional.of(proposalMessage));
        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(negotiationRepository.save(any(DisputeNegotiation.class))).thenReturn(proposalMessage);

        // Act
        boolean result = negotiationService.respondToProposal(request, 100L);

        // Assert
        assertThat(result).isTrue();
        assertThat(proposalMessage.getProposalStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(proposalMessage.getRespondedBy()).isEqualTo(100L);
        verify(negotiationRepository, times(1)).save(proposalMessage);
    }

    @Test
    @DisplayName("响应方案 - 方案不存在时应该抛出异常")
    void respondToProposal_ShouldThrowException_WhenProposalNotFound() {
        // Arrange
        RespondProposalRequest request = RespondProposalRequest.builder()
                .proposalId(999L)
                .accepted(true)
                .build();

        when(negotiationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> negotiationService.respondToProposal(request, 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("方案不存在");
    }

    @Test
    @DisplayName("查询协商历史 - 应该返回所有消息")
    void getNegotiationHistory_ShouldReturnAllMessages() {
        // Arrange
        List<DisputeNegotiation> messages = List.of(textMessage, proposalMessage);
        when(negotiationRepository.findByDisputeIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(messages);

        // Act
        List<NegotiationMessageDTO> result = negotiationService.getNegotiationHistory(1L);

        // Assert
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getMessageType()).isEqualTo(NegotiationMessageType.TEXT);
        assertThat(result.get(1).getMessageType()).isEqualTo(NegotiationMessageType.PROPOSAL);
    }

    @Test
    @DisplayName("查询待响应方案 - 应该返回待响应方案")
    void getPendingProposal_ShouldReturnPendingProposal() {
        // Arrange
        when(negotiationRepository.findLatestPendingProposal(anyLong()))
                .thenReturn(Optional.of(proposalMessage));

        // Act
        Optional<NegotiationMessageDTO> result = negotiationService.getPendingProposal(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getProposalStatus()).isEqualTo(ProposalStatus.PENDING);
        assertThat(result.get().getProposedRefundAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("查询已接受方案 - 应该返回已接受方案")
    void getAcceptedProposal_ShouldReturnAcceptedProposal() {
        // Arrange
        proposalMessage.setProposalStatus(ProposalStatus.ACCEPTED);
        proposalMessage.setRespondedBy(100L);
        proposalMessage.setRespondedAt(LocalDateTime.now());

        when(negotiationRepository.findAcceptedProposal(anyLong()))
                .thenReturn(Optional.of(proposalMessage));

        // Act
        Optional<NegotiationMessageDTO> result = negotiationService.getAcceptedProposal(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getProposalStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(result.get().getRespondedBy()).isEqualTo(100L);
    }
}
