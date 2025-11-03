package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.DisputeStatisticsDTO;
import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.entity.DisputeArbitration;
import com.campus.marketplace.common.enums.*;
import com.campus.marketplace.repository.DisputeArbitrationRepository;
import com.campus.marketplace.repository.DisputeRepository;
import com.campus.marketplace.service.impl.DisputeStatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Dispute Statistics Service Test
 *
 * @author BaSui
 * @since 2025-11-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Dispute Statistics Service Test")
class DisputeStatisticsServiceTest {

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private DisputeArbitrationRepository arbitrationRepository;

    @InjectMocks
    private DisputeStatisticsServiceImpl statisticsService;

    private List<Dispute> mockDisputes;
    private List<DisputeArbitration> mockArbitrations;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime twoHoursAgo = now.minusHours(2);

        // Create mock disputes
        Dispute dispute1 = new Dispute();
        dispute1.setId(1L);
        dispute1.setStatus(DisputeStatus.NEGOTIATING);
        dispute1.setDisputeType(DisputeType.QUALITY_ISSUE);
        dispute1.setCreatedAt(twoHoursAgo);

        Dispute dispute2 = new Dispute();
        dispute2.setId(2L);
        dispute2.setStatus(DisputeStatus.PENDING_ARBITRATION);
        dispute2.setDisputeType(DisputeType.GOODS_MISMATCH);
        dispute2.setCreatedAt(twoHoursAgo);

        Dispute dispute3 = new Dispute();
        dispute3.setId(3L);
        dispute3.setStatus(DisputeStatus.ARBITRATING);
        dispute3.setDisputeType(DisputeType.LOGISTICS_DELAY);
        dispute3.setCreatedAt(twoHoursAgo);

        Dispute dispute4 = new Dispute();
        dispute4.setId(4L);
        dispute4.setStatus(DisputeStatus.COMPLETED);
        dispute4.setDisputeType(DisputeType.QUALITY_ISSUE);
        dispute4.setCreatedAt(twoHoursAgo);
        dispute4.setCompletedAt(now);

        Dispute dispute5 = new Dispute();
        dispute5.setId(5L);
        dispute5.setStatus(DisputeStatus.CLOSED);
        dispute5.setDisputeType(DisputeType.GOODS_MISMATCH);
        dispute5.setCreatedAt(twoHoursAgo);
        dispute5.setClosedAt(now);

        mockDisputes = Arrays.asList(dispute1, dispute2, dispute3, dispute4, dispute5);

        // Create mock arbitrations
        DisputeArbitration arb1 = new DisputeArbitration();
        arb1.setId(1L);
        arb1.setDisputeId(4L);
        arb1.setResult(ArbitrationResult.FULL_REFUND);
        arb1.setCreatedAt(oneHourAgo);

        mockArbitrations = List.of(arb1);
    }

    @Test
    @DisplayName("Get statistics - should return complete statistics")
    void getStatistics_ShouldReturnCompleteStatistics() {
        // Mock repository responses
        when(disputeRepository.findAll()).thenReturn(mockDisputes);
        when(disputeRepository.countByStatus(DisputeStatus.NEGOTIATING)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.PENDING_ARBITRATION)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.ARBITRATING)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.COMPLETED)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.CLOSED)).thenReturn(1L);
        when(arbitrationRepository.findAll()).thenReturn(mockArbitrations);

        // Execute
        DisputeStatisticsDTO stats = statisticsService.getStatistics();

        // Verify
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalDisputes()).isEqualTo(5L);
        assertThat(stats.getNegotiatingCount()).isEqualTo(1L);
        assertThat(stats.getPendingArbitrationCount()).isEqualTo(1L);
        assertThat(stats.getArbitratingCount()).isEqualTo(1L);
        assertThat(stats.getCompletedCount()).isEqualTo(1L);
        assertThat(stats.getClosedCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Get statistics - should calculate type distribution correctly")
    void getStatistics_ShouldCalculateTypeDistribution() {
        when(disputeRepository.findAll()).thenReturn(mockDisputes);
        when(disputeRepository.countByStatus(DisputeStatus.NEGOTIATING)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.PENDING_ARBITRATION)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.ARBITRATING)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.COMPLETED)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.CLOSED)).thenReturn(1L);
        when(arbitrationRepository.findAll()).thenReturn(mockArbitrations);

        DisputeStatisticsDTO stats = statisticsService.getStatistics();

        Map<String, Long> typeDistribution = stats.getDisputeTypeDistribution();
        assertThat(typeDistribution).isNotNull();
        assertThat(typeDistribution.get("QUALITY_ISSUE")).isEqualTo(2L);
        assertThat(typeDistribution.get("GOODS_MISMATCH")).isEqualTo(2L);
        assertThat(typeDistribution.get("LOGISTICS_DELAY")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Get statistics - should calculate arbitration result distribution")
    void getStatistics_ShouldCalculateArbitrationResultDistribution() {
        when(disputeRepository.findAll()).thenReturn(mockDisputes);
        when(disputeRepository.countByStatus(DisputeStatus.NEGOTIATING)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.PENDING_ARBITRATION)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.ARBITRATING)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.COMPLETED)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.CLOSED)).thenReturn(1L);
        when(arbitrationRepository.findAll()).thenReturn(mockArbitrations);

        DisputeStatisticsDTO stats = statisticsService.getStatistics();

        Map<String, Long> resultDistribution = stats.getArbitrationResultDistribution();
        assertThat(resultDistribution).isNotNull();
        assertThat(resultDistribution.get("FULL_REFUND")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Get statistics - should calculate negotiation success rate")
    void getStatistics_ShouldCalculateNegotiationSuccessRate() {
        when(disputeRepository.findAll()).thenReturn(mockDisputes);
        when(disputeRepository.countByStatus(DisputeStatus.NEGOTIATING)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.PENDING_ARBITRATION)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.ARBITRATING)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.COMPLETED)).thenReturn(1L);
        when(disputeRepository.countByStatus(DisputeStatus.CLOSED)).thenReturn(1L);
        when(arbitrationRepository.findAll()).thenReturn(mockArbitrations);

        DisputeStatisticsDTO stats = statisticsService.getStatistics();

        // Total: 5, Closed without escalation: 1, Success rate = 1/5 = 20%
        assertThat(stats.getNegotiationSuccessRate()).isNotNull();
        assertThat(stats.getNegotiationSuccessRate()).isGreaterThanOrEqualTo(0.0);
        assertThat(stats.getNegotiationSuccessRate()).isLessThanOrEqualTo(100.0);
    }

    @Test
    @DisplayName("Get statistics - should handle empty data")
    void getStatistics_ShouldHandleEmptyData() {
        when(disputeRepository.findAll()).thenReturn(List.of());
        when(disputeRepository.countByStatus(DisputeStatus.NEGOTIATING)).thenReturn(0L);
        when(disputeRepository.countByStatus(DisputeStatus.PENDING_ARBITRATION)).thenReturn(0L);
        when(disputeRepository.countByStatus(DisputeStatus.ARBITRATING)).thenReturn(0L);
        when(disputeRepository.countByStatus(DisputeStatus.COMPLETED)).thenReturn(0L);
        when(disputeRepository.countByStatus(DisputeStatus.CLOSED)).thenReturn(0L);
        when(arbitrationRepository.findAll()).thenReturn(List.of());

        DisputeStatisticsDTO stats = statisticsService.getStatistics();

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalDisputes()).isEqualTo(0L);
        assertThat(stats.getNegotiationSuccessRate()).isEqualTo(0.0);
        assertThat(stats.getDisputeTypeDistribution()).isEmpty();
        assertThat(stats.getArbitrationResultDistribution()).isEmpty();
    }
}
