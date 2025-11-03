package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.EvidenceDTO;
import com.campus.marketplace.common.dto.EvidenceSummaryDTO;
import com.campus.marketplace.common.dto.request.UploadEvidenceRequest;
import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.entity.DisputeEvidence;
import com.campus.marketplace.common.enums.*;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.DisputeEvidenceRepository;
import com.campus.marketplace.repository.DisputeRepository;
import com.campus.marketplace.service.impl.DisputeEvidenceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 纠纷证据服务测试类
 *
 * 遵循TDD原则，测试先行！验证证据上传、查询和评估功能！💪
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("纠纷证据服务测试")
class DisputeEvidenceServiceTest {

    @Mock
    private DisputeEvidenceRepository evidenceRepository;

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private DisputeEvidenceServiceImpl evidenceService;

    private Dispute testDispute;
    private DisputeEvidence testEvidence;

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
        testDispute.setStatus(DisputeStatus.NEGOTIATING);

        // 初始化测试证据
        testEvidence = new DisputeEvidence();
        testEvidence.setId(1L);
        testEvidence.setDisputeId(1L);
        testEvidence.setUploaderId(100L);
        testEvidence.setUploaderRole(DisputeRole.BUYER);
        testEvidence.setEvidenceType(EvidenceType.IMAGE);
        testEvidence.setFileUrl("https://example.com/evidence/image1.jpg");
        testEvidence.setFileName("商品照片.jpg");
        testEvidence.setFileSize(1024000L);
        testEvidence.setDescription("商品与描述不符的证据照片");
        testEvidence.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("上传证据 - 应该成功上传并返回证据ID")
    void uploadEvidence_ShouldUploadSuccessfully() {
        // Arrange
        UploadEvidenceRequest request = UploadEvidenceRequest.builder()
                .disputeId(1L)
                .evidenceType(EvidenceType.IMAGE)
                .fileUrl("https://example.com/evidence/image1.jpg")
                .fileName("商品照片.jpg")
                .fileSize(1024000L)
                .description("商品与描述不符的证据照片")
                .build();

        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));
        when(evidenceRepository.save(any(DisputeEvidence.class)))
                .thenAnswer(invocation -> {
                    DisputeEvidence evidence = invocation.getArgument(0);
                    evidence.setId(1L);
                    return evidence;
                });

        // Act
        Long evidenceId = evidenceService.uploadEvidence(request, 100L);

        // Assert
        assertThat(evidenceId).isNotNull().isEqualTo(1L);
        verify(evidenceRepository, times(1)).save(any(DisputeEvidence.class));
        verify(notificationService, times(1)).sendNotification(
                anyLong(), any(), anyString(), anyString(), anyLong(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("上传证据 - 纠纷不存在时应该抛出异常")
    void uploadEvidence_ShouldThrowException_WhenDisputeNotFound() {
        // Arrange
        UploadEvidenceRequest request = UploadEvidenceRequest.builder()
                .disputeId(999L)
                .evidenceType(EvidenceType.IMAGE)
                .fileUrl("https://example.com/evidence/image1.jpg")
                .fileName("test.jpg")
                .fileSize(1024000L)
                .build();

        when(disputeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> evidenceService.uploadEvidence(request, 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("纠纷不存在");
    }

    @Test
    @DisplayName("上传证据 - 用户非纠纷参与方时应该抛出异常")
    void uploadEvidence_ShouldThrowException_WhenUserNotParticipant() {
        // Arrange
        UploadEvidenceRequest request = UploadEvidenceRequest.builder()
                .disputeId(1L)
                .evidenceType(EvidenceType.IMAGE)
                .fileUrl("https://example.com/evidence/image1.jpg")
                .fileName("test.jpg")
                .fileSize(1024000L)
                .build();

        when(disputeRepository.findById(anyLong())).thenReturn(Optional.of(testDispute));

        // Act & Assert
        assertThatThrownBy(() -> evidenceService.uploadEvidence(request, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("您不是该纠纷的参与方");
    }

    @Test
    @DisplayName("查询纠纷所有证据 - 应该返回所有证据列表")
    void getDisputeEvidence_ShouldReturnAllEvidence() {
        // Arrange
        DisputeEvidence evidence1 = new DisputeEvidence();
        evidence1.setId(1L);
        evidence1.setEvidenceType(EvidenceType.IMAGE);

        DisputeEvidence evidence2 = new DisputeEvidence();
        evidence2.setId(2L);
        evidence2.setEvidenceType(EvidenceType.VIDEO);

        when(evidenceRepository.findByDisputeIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(List.of(evidence1, evidence2));

        // Act
        List<EvidenceDTO> result = evidenceService.getDisputeEvidence(1L);

        // Assert
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getEvidenceType()).isEqualTo(EvidenceType.IMAGE);
        assertThat(result.get(1).getEvidenceType()).isEqualTo(EvidenceType.VIDEO);
    }

    @Test
    @DisplayName("查询买家证据 - 应该返回买家上传的证据")
    void getBuyerEvidence_ShouldReturnBuyerEvidence() {
        // Arrange
        when(evidenceRepository.findByDisputeIdAndUploaderRoleOrderByCreatedAtAsc(
                anyLong(), any(DisputeRole.class)))
                .thenReturn(List.of(testEvidence));

        // Act
        List<EvidenceDTO> result = evidenceService.getBuyerEvidence(1L);

        // Assert
        assertThat(result).isNotNull().hasSize(1);
        verify(evidenceRepository).findByDisputeIdAndUploaderRoleOrderByCreatedAtAsc(
                1L, DisputeRole.BUYER);
    }

    @Test
    @DisplayName("查询卖家证据 - 应该返回卖家上传的证据")
    void getSellerEvidence_ShouldReturnSellerEvidence() {
        // Arrange
        testEvidence.setUploaderRole(DisputeRole.SELLER);
        when(evidenceRepository.findByDisputeIdAndUploaderRoleOrderByCreatedAtAsc(
                anyLong(), any(DisputeRole.class)))
                .thenReturn(List.of(testEvidence));

        // Act
        List<EvidenceDTO> result = evidenceService.getSellerEvidence(1L);

        // Assert
        assertThat(result).isNotNull().hasSize(1);
        verify(evidenceRepository).findByDisputeIdAndUploaderRoleOrderByCreatedAtAsc(
                1L, DisputeRole.SELLER);
    }

    @Test
    @DisplayName("评估证据有效性 - 应该成功评估")
    void evaluateEvidence_ShouldEvaluateSuccessfully() {
        // Arrange
        testEvidence.setValidity(null);
        when(evidenceRepository.findById(anyLong())).thenReturn(Optional.of(testEvidence));
        when(evidenceRepository.save(any(DisputeEvidence.class))).thenReturn(testEvidence);

        // Act
        boolean result = evidenceService.evaluateEvidence(
                1L, EvidenceValidity.VALID, "证据真实有效", 300L);

        // Assert
        assertThat(result).isTrue();
        assertThat(testEvidence.getValidity()).isEqualTo(EvidenceValidity.VALID);
        assertThat(testEvidence.getValidityReason()).isEqualTo("证据真实有效");
        assertThat(testEvidence.getEvaluatedBy()).isEqualTo(300L);
        verify(evidenceRepository, times(1)).save(testEvidence);
    }

    @Test
    @DisplayName("评估证据有效性 - 证据不存在时应该抛出异常")
    void evaluateEvidence_ShouldThrowException_WhenEvidenceNotFound() {
        // Arrange
        when(evidenceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> evidenceService.evaluateEvidence(
                999L, EvidenceValidity.VALID, "test", 300L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("证据不存在");
    }

    @Test
    @DisplayName("评估证据有效性 - 已评估的证据应该抛出异常")
    void evaluateEvidence_ShouldThrowException_WhenAlreadyEvaluated() {
        // Arrange
        testEvidence.setValidity(EvidenceValidity.VALID);
        when(evidenceRepository.findById(anyLong())).thenReturn(Optional.of(testEvidence));

        // Act & Assert
        assertThatThrownBy(() -> evidenceService.evaluateEvidence(
                1L, EvidenceValidity.INVALID, "test", 300L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("该证据已被评估");
    }

    @Test
    @DisplayName("查询证据统计 - 应该返回统计信息")
    void getEvidenceSummary_ShouldReturnSummary() {
        // Arrange
        when(evidenceRepository.countByDisputeId(anyLong())).thenReturn(5L);
        when(evidenceRepository.countByDisputeIdAndUploaderRole(anyLong(), eq(DisputeRole.BUYER)))
                .thenReturn(3L);
        when(evidenceRepository.countByDisputeIdAndUploaderRole(anyLong(), eq(DisputeRole.SELLER)))
                .thenReturn(2L);
        when(evidenceRepository.countByDisputeIdAndValidity(anyLong(), eq(EvidenceValidity.VALID)))
                .thenReturn(4L);
        when(evidenceRepository.countByDisputeIdAndValidity(anyLong(), eq(EvidenceValidity.INVALID)))
                .thenReturn(1L);
        when(evidenceRepository.countByDisputeIdAndValidity(anyLong(), eq(EvidenceValidity.DOUBTFUL)))
                .thenReturn(0L);
        when(evidenceRepository.findUnevaluatedEvidence(anyLong()))
                .thenReturn(List.of());

        // Act
        EvidenceSummaryDTO summary = evidenceService.getEvidenceSummary(1L);

        // Assert
        assertThat(summary).isNotNull();
        assertThat(summary.getBuyerEvidenceCount()).isEqualTo(3L);
        assertThat(summary.getSellerEvidenceCount()).isEqualTo(2L);
        assertThat(summary.getValidEvidenceCount()).isEqualTo(4L);
        assertThat(summary.getInvalidEvidenceCount()).isEqualTo(1L);
        assertThat(summary.getDoubtfulEvidenceCount()).isEqualTo(0L);
        assertThat(summary.getUnevaluatedEvidenceCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("查询待评估证据 - 应该返回未评估的证据")
    void getUnevaluatedEvidence_ShouldReturnUnevaluatedEvidence() {
        // Arrange
        testEvidence.setValidity(null);
        when(evidenceRepository.findUnevaluatedEvidence(anyLong()))
                .thenReturn(List.of(testEvidence));

        // Act
        List<EvidenceDTO> result = evidenceService.getUnevaluatedEvidence(1L);

        // Assert
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getValidity()).isNull();
    }

    @Test
    @DisplayName("删除证据 - 应该成功删除")
    void deleteEvidence_ShouldDeleteSuccessfully() {
        // Arrange
        when(evidenceRepository.findById(anyLong())).thenReturn(Optional.of(testEvidence));
        doNothing().when(evidenceRepository).delete(any(DisputeEvidence.class));

        // Act
        boolean result = evidenceService.deleteEvidence(1L, 100L);

        // Assert
        assertThat(result).isTrue();
        verify(evidenceRepository, times(1)).delete(testEvidence);
    }

    @Test
    @DisplayName("删除证据 - 非上传者不能删除")
    void deleteEvidence_ShouldThrowException_WhenNotUploader() {
        // Arrange
        when(evidenceRepository.findById(anyLong())).thenReturn(Optional.of(testEvidence));

        // Act & Assert
        assertThatThrownBy(() -> evidenceService.deleteEvidence(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只能删除自己上传的证据");
    }

    @Test
    @DisplayName("删除证据 - 已评估的证据不能删除")
    void deleteEvidence_ShouldThrowException_WhenAlreadyEvaluated() {
        // Arrange
        testEvidence.setValidity(EvidenceValidity.VALID);
        when(evidenceRepository.findById(anyLong())).thenReturn(Optional.of(testEvidence));

        // Act & Assert
        assertThatThrownBy(() -> evidenceService.deleteEvidence(1L, 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已评估的证据不能删除");
    }
}
