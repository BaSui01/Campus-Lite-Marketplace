package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.DisputeStatisticsDTO;

/**
 * Dispute Statistics Service
 *
 * Provides comprehensive dispute statistics including:
 * - Status distribution
 * - Type distribution
 * - Arbitration result distribution
 * - Average processing duration
 * - Success rates
 *
 * @author BaSui
 * @since 2025-11-03
 */
public interface DisputeStatisticsService {

    /**
     * Get comprehensive dispute statistics
     *
     * @return statistics DTO with all metrics
     */
    DisputeStatisticsDTO getStatistics();
}
