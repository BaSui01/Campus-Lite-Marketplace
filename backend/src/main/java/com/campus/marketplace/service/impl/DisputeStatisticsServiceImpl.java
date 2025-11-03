package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.DisputeStatisticsDTO;
import com.campus.marketplace.common.entity.Dispute;
import com.campus.marketplace.common.entity.DisputeArbitration;
import com.campus.marketplace.common.enums.DisputeStatus;
import com.campus.marketplace.repository.DisputeArbitrationRepository;
import com.campus.marketplace.repository.DisputeRepository;
import com.campus.marketplace.service.DisputeStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dispute Statistics Service Implementation
 *
 * @author BaSui
 * @since 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeStatisticsServiceImpl implements DisputeStatisticsService {

    private final DisputeRepository disputeRepository;
    private final DisputeArbitrationRepository arbitrationRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "disputeStatistics", key = "'global'", unless = "#result == null")
    public DisputeStatisticsDTO getStatistics() {
        log.debug("Calculating dispute statistics...");

        List<Dispute> allDisputes = disputeRepository.findAll();
        List<DisputeArbitration> allArbitrations = arbitrationRepository.findAll();

        // Calculate status counts
        long totalDisputes = allDisputes.size();
        long negotiatingCount = disputeRepository.countByStatus(DisputeStatus.NEGOTIATING);
        long pendingArbitrationCount = disputeRepository.countByStatus(DisputeStatus.PENDING_ARBITRATION);
        long arbitratingCount = disputeRepository.countByStatus(DisputeStatus.ARBITRATING);
        long completedCount = disputeRepository.countByStatus(DisputeStatus.COMPLETED);
        long closedCount = disputeRepository.countByStatus(DisputeStatus.CLOSED);

        // Calculate type distribution
        Map<String, Long> typeDistribution = allDisputes.stream()
                .collect(Collectors.groupingBy(
                        dispute -> dispute.getDisputeType().name(),
                        Collectors.counting()
                ));

        // Calculate arbitration result distribution
        Map<String, Long> resultDistribution = allArbitrations.stream()
                .collect(Collectors.groupingBy(
                        arb -> arb.getResult().name(),
                        Collectors.counting()
                ));

        // Calculate average durations
        double avgNegotiationDuration = calculateAvgNegotiationDuration(allDisputes);
        double avgArbitrationDuration = calculateAvgArbitrationDuration(allDisputes);

        // Calculate negotiation success rate
        double negotiationSuccessRate = calculateNegotiationSuccessRate(allDisputes);

        // Calculate this month statistics
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long thisMonthNewCount = allDisputes.stream()
                .filter(d -> d.getCreatedAt().isAfter(startOfMonth))
                .count();
        long thisMonthResolvedCount = allDisputes.stream()
                .filter(d -> d.getCompletedAt() != null && d.getCompletedAt().isAfter(startOfMonth))
                .count();

        DisputeStatisticsDTO stats = DisputeStatisticsDTO.builder()
                .totalDisputes(totalDisputes)
                .negotiatingCount(negotiatingCount)
                .pendingArbitrationCount(pendingArbitrationCount)
                .arbitratingCount(arbitratingCount)
                .completedCount(completedCount)
                .closedCount(closedCount)
                .disputeTypeDistribution(typeDistribution)
                .arbitrationResultDistribution(resultDistribution)
                .avgNegotiationDuration(avgNegotiationDuration)
                .avgArbitrationDuration(avgArbitrationDuration)
                .negotiationSuccessRate(negotiationSuccessRate)
                .thisMonthNewCount(thisMonthNewCount)
                .thisMonthResolvedCount(thisMonthResolvedCount)
                .build();

        log.info("Dispute statistics calculated: total={}, negotiating={}, arbitrating={}",
                totalDisputes, negotiatingCount, arbitratingCount);

        return stats;
    }

    /**
     * Calculate average negotiation duration in hours
     * For disputes in NEGOTIATING status
     */
    private double calculateAvgNegotiationDuration(List<Dispute> disputes) {
        List<Long> durations = disputes.stream()
                .filter(d -> d.getStatus() == DisputeStatus.NEGOTIATING)
                .map(d -> Duration.between(d.getCreatedAt(), LocalDateTime.now()).toHours())
                .collect(Collectors.toList());

        if (durations.isEmpty()) {
            return 0.0;
        }

        return durations.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculate average arbitration duration in hours
     * From creation to completion
     */
    private double calculateAvgArbitrationDuration(List<Dispute> disputes) {
        List<Long> durations = disputes.stream()
                .filter(d -> d.getCompletedAt() != null)
                .map(d -> Duration.between(d.getCreatedAt(), d.getCompletedAt()).toHours())
                .collect(Collectors.toList());

        if (durations.isEmpty()) {
            return 0.0;
        }

        return durations.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculate negotiation success rate
     * Success = closed disputes / total disputes * 100
     */
    private double calculateNegotiationSuccessRate(List<Dispute> disputes) {
        if (disputes.isEmpty()) {
            return 0.0;
        }

        long closedCount = disputes.stream()
                .filter(d -> d.getStatus() == DisputeStatus.CLOSED)
                .count();

        return (closedCount * 100.0) / disputes.size();
    }
}
