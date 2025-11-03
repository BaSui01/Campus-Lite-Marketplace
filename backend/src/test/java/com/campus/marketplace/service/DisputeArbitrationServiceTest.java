package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.ArbitrationDTO;
import com.campus.marketplace.common.dto.request.ArbitrateDisputeRequest;
import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.entity.DisputeArbitration;
import com.campus.marketplace.common.enums.*;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.DisputeArbitrationRepository;
import com.campus.marketplace.repository.DisputeRepository;
import com.campus.marketplace.service.impl.DisputeArbitrationServiceImpl;
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
 * 纠纷仲裁服务测试类
 *
 * 遵循TDD原则，测试先行！验证仲裁员处理纠纷的核心流程！💪
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("纠纷仲裁服务测试")
class DisputeArbitrationServiceTest {

    @Mock
    private DisputeArbitrationRepository arbitrationRepository;

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private DisputeArbitrationServiceImpl arbitrationService;

    private Dispute testDispute;
    private DisputeArbitration testArbitration;

    @BeforeEach
    void setUp() {
        // 初始化测试纠纷
        testDispute = new Dispute();
        testDispute.setId(1L);
        testDispute.setDisputeCode("DSP-20251103-000001");
        testDispute.setOrderId(123L);
        testDispute.setInitiatorId(100L);
        testDispute.setInitiatorRole(DisputeRole.BUYER);
        testDispute.setRespondentId(200L);
        testDispute.setStatus(DisputeStatus.PENDING_ARBITRATION);
        testDispute.setArbitratorId(300L);

        // 初始化测试仲裁
        testArbitration = new DisputeArbitration();
        testArbitration.setId(1L);
        testArbitration.setDisputeId(1L);
        testArbitration.setArbitratorId(300L);
        testArbitration.setResult(ArbitrationResult.FULL_REFUND);
        testArbitration.setRefundAmount(new BigDecimal("100.00"));
        testArbitration.setReason("买家证据充分，商品确实与描述不符，支持全额退款");
        testArbitration.setBuyerEvidenceAnalysis("提供了完整的商品照片和聊天记录");
        testArbitration.setSellerEvidenceAnalysis("未提供有力反驳证据");
        testArbitration.setArbitratedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("分配仲裁员 - 应该成功分配")
    void assignArbitrator_ShouldAssignSuccessfully() {
        // Arrange
        testDispute.setStatus(DisputeStatus.NEGOTIATING);
        testDispute.setArbitratorId(null);
        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // Act
        boolean result = arbitrationService.assignArbitrator(1L, 300L);

        // Assert
        assertThat(result).isTrue();
        assertThat(testDispute.getArbitratorId()).isEqualTo(300L);
        assertThat(testDispute.getStatus()).isEqualTo(DisputeStatus.ARBITRATING);
        assertThat(testDispute.getArbitrationDeadline()).isNotNull();
        verify(disputeRepository, times(1)).save(testDispute);
        verify(notificationService, times(2)).sendNotification(
                anyLong(), any(), anyString(), anyString(), anyLong(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("分配仲裁员 - 纠纷不存在时应该抛出异常")
    void assignArbitrator_ShouldThrowException_WhenDisputeNotFound() {
        // Arrange
        when(disputeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> arbitrationService.assignArbitrator(999L, 300L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("纠纷不存在");
    }

    @Test
    @DisplayName("分配仲裁员 - 已分配时应该抛出异常")
    void assignArbitrator_ShouldThrowException_WhenAlreadyAssigned() {
        // Arrange
        testDispute.setArbitratorId(300L);
        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));

        // Act & Assert
        assertThatThrownBy(() -> arbitrationService.assignArbitrator(1L, 400L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("该纠纷已分配仲裁员");
    }

    @Test
    @DisplayName("提交仲裁决定 - 全额退款应该成功")
    void submitArbitration_FullRefund_ShouldSucceed() {
        // Arrange
        ArbitrateDisputeRequest request = ArbitrateDisputeRequest.builder()
                .disputeId(1L)
                .result(ArbitrationResult.FULL_REFUND)
                .refundAmount(new BigDecimal("100.00"))
                .reason("买家证据充分，商品确实与描述不符，支持全额退款，这是经过详细审查的合理判断")
                .buyerEvidenceAnalysis("提供了完整的商品照片和聊天记录")
                .sellerEvidenceAnalysis("未提供有力反驳证据")
                .build();

        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(arbitrationRepository.existsByDisputeId(anyLong())).thenReturn(false);
        when(arbitrationRepository.save(any(DisputeArbitration.class)))
                .thenAnswer(invocation -> {
                    DisputeArbitration arb = invocation.getArgument(0);
                    arb.setId(1L);
                    return arb;
                });
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // Act
        Long arbitrationId = arbitrationService.submitArbitration(request, 300L);

        // Assert
        assertThat(arbitrationId).isNotNull().isEqualTo(1L);
        assertThat(testDispute.getStatus()).isEqualTo(DisputeStatus.COMPLETED);
        verify(arbitrationRepository, times(1)).save(any(DisputeArbitration.class));
        verify(disputeRepository, times(1)).save(testDispute);
        verify(notificationService, times(2)).sendNotification(
                anyLong(), any(), anyString(), anyString(), anyLong(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("提交仲裁决定 - 部分退款应该成功")
    void submitArbitration_PartialRefund_ShouldSucceed() {
        // Arrange
        ArbitrateDisputeRequest request = ArbitrateDisputeRequest.builder()
                .disputeId(1L)
                .result(ArbitrationResult.PARTIAL_REFUND)
                .refundAmount(new BigDecimal("50.00"))
                .reason("双方均有一定责任，根据实际情况判定部分退款，这是经过详细审查的合理判断")
                .buyerEvidenceAnalysis("有一定瑕疵证据")
                .sellerEvidenceAnalysis("描述基本符合但存在疏漏")
                .build();

        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(arbitrationRepository.existsByDisputeId(anyLong())).thenReturn(false);
        when(arbitrationRepository.save(any(DisputeArbitration.class)))
                .thenAnswer(invocation -> {
                    DisputeArbitration arb = invocation.getArgument(0);
                    arb.setId(2L);
                    return arb;
                });
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // Act
        Long arbitrationId = arbitrationService.submitArbitration(request, 300L);

        // Assert
        assertThat(arbitrationId).isNotNull().isEqualTo(2L);
        verify(arbitrationRepository, times(1)).save(any(DisputeArbitration.class));
    }

    @Test
    @DisplayName("提交仲裁决定 - 驳回申请应该成功")
    void submitArbitration_Reject_ShouldSucceed() {
        // Arrange
        ArbitrateDisputeRequest request = ArbitrateDisputeRequest.builder()
                .disputeId(1L)
                .result(ArbitrationResult.REJECT)
                .refundAmount(null)
                .reason("卖家描述准确，买家证据不足，驳回退款申请，这是经过详细审查的合理判断")
                .buyerEvidenceAnalysis("证据不足")
                .sellerEvidenceAnalysis("描述准确完整")
                .build();

        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(arbitrationRepository.existsByDisputeId(anyLong())).thenReturn(false);
        when(arbitrationRepository.save(any(DisputeArbitration.class)))
                .thenAnswer(invocation -> {
                    DisputeArbitration arb = invocation.getArgument(0);
                    arb.setId(3L);
                    return arb;
                });
        when(disputeRepository.save(any(Dispute.class))).thenReturn(testDispute);

        // Act
        Long arbitrationId = arbitrationService.submitArbitration(request, 300L);

        // Assert
        assertThat(arbitrationId).isNotNull().isEqualTo(3L);
        verify(arbitrationRepository, times(1)).save(any(DisputeArbitration.class));
    }

    @Test
    @DisplayName("提交仲裁决定 - 纠纷不存在时应该抛出异常")
    void submitArbitration_ShouldThrowException_WhenDisputeNotFound() {
        // Arrange
        ArbitrateDisputeRequest request = ArbitrateDisputeRequest.builder()
                .disputeId(999L)
                .result(ArbitrationResult.FULL_REFUND)
                .refundAmount(new BigDecimal("100.00"))
                .reason("测试理由，这是一个足够长的理由用于满足验证要求的测试内容")
                .build();

        when(disputeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> arbitrationService.submitArbitration(request, 300L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("纠纷不存在");
    }

    @Test
    @DisplayName("提交仲裁决定 - 已有仲裁记录时应该抛出异常")
    void submitArbitration_ShouldThrowException_WhenArbitrationExists() {
        // Arrange
        ArbitrateDisputeRequest request = ArbitrateDisputeRequest.builder()
                .disputeId(1L)
                .result(ArbitrationResult.FULL_REFUND)
                .refundAmount(new BigDecimal("100.00"))
                .reason("测试理由，这是一个足够长的理由用于满足验证要求的测试内容")
                .build();

        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(arbitrationRepository.existsByDisputeId(anyLong())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> arbitrationService.submitArbitration(request, 300L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("该纠纷已有仲裁记录");
    }

    @Test
    @DisplayName("查询仲裁详情 - 应该返回完整信息")
    void getArbitrationDetail_ShouldReturnCompleteInformation() {
        // Arrange
        when(arbitrationRepository.findByDisputeId(anyLong()))
                .thenReturn(Optional.of(testArbitration));

        // Act
        Optional<ArbitrationDTO> result = arbitrationService.getArbitrationDetail(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getDisputeId()).isEqualTo(1L);
        assertThat(result.get().getResult()).isEqualTo(ArbitrationResult.FULL_REFUND);
        assertThat(result.get().getRefundAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("查询仲裁详情 - 无仲裁记录时应该返回空")
    void getArbitrationDetail_ShouldReturnEmpty_WhenNoArbitration() {
        // Arrange
        when(arbitrationRepository.findByDisputeId(anyLong())).thenReturn(Optional.empty());

        // Act
        Optional<ArbitrationDTO> result = arbitrationService.getArbitrationDetail(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("查询仲裁员案件列表 - 应该返回所有案件")
    void getArbitratorCases_ShouldReturnAllCases() {
        // Arrange
        DisputeArbitration case1 = new DisputeArbitration();
        case1.setId(1L);
        case1.setArbitratorId(300L);
        case1.setResult(ArbitrationResult.FULL_REFUND);

        DisputeArbitration case2 = new DisputeArbitration();
        case2.setId(2L);
        case2.setArbitratorId(300L);
        case2.setResult(ArbitrationResult.PARTIAL_REFUND);

        when(arbitrationRepository.findByArbitratorIdOrderByArbitratedAtDesc(anyLong()))
                .thenReturn(List.of(case1, case2));

        // Act
        List<ArbitrationDTO> result = arbitrationService.getArbitratorCases(300L);

        // Assert
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getResult()).isEqualTo(ArbitrationResult.FULL_REFUND);
        assertThat(result.get(1).getResult()).isEqualTo(ArbitrationResult.PARTIAL_REFUND);
    }

    @Test
    @DisplayName("查询待执行仲裁列表 - 应该返回待执行案件")
    void getPendingExecutions_ShouldReturnPendingCases() {
        // Arrange
        DisputeArbitration pending1 = new DisputeArbitration();
        pending1.setId(1L);
        pending1.setResult(ArbitrationResult.FULL_REFUND);
        pending1.setExecuted(false);

        DisputeArbitration pending2 = new DisputeArbitration();
        pending2.setId(2L);
        pending2.setResult(ArbitrationResult.PARTIAL_REFUND);
        pending2.setExecuted(false);

        when(arbitrationRepository.findPendingExecution())
                .thenReturn(List.of(pending1, pending2));

        // Act
        List<ArbitrationDTO> result = arbitrationService.getPendingExecutions();

        // Assert
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).isExecuted()).isFalse();
        assertThat(result.get(1).isExecuted()).isFalse();
    }

    @Test
    @DisplayName("标记仲裁为已执行 - 应该成功标记")
    void markExecuted_ShouldMarkSuccessfully() {
        // Arrange
        testArbitration.setExecuted(false);
        when(arbitrationRepository.findById(anyLong())).thenReturn(Optional.of(testArbitration));
        when(arbitrationRepository.save(any(DisputeArbitration.class))).thenReturn(testArbitration);

        // Act
        boolean result = arbitrationService.markExecuted(1L, "退款已成功处理");

        // Assert
        assertThat(result).isTrue();
        assertThat(testArbitration.isExecuted()).isTrue();
        assertThat(testArbitration.getExecutedAt()).isNotNull();
        assertThat(testArbitration.getExecutionNote()).isEqualTo("退款已成功处理");
        verify(arbitrationRepository, times(1)).save(testArbitration);
    }

    @Test
    @DisplayName("标记仲裁为已执行 - 仲裁不存在时应该抛出异常")
    void markExecuted_ShouldThrowException_WhenArbitrationNotFound() {
        // Arrange
        when(arbitrationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> arbitrationService.markExecuted(999L, "测试"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仲裁记录不存在");
    }
}
