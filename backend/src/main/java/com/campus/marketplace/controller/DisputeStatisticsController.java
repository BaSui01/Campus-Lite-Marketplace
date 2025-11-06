package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.DisputeStatisticsDTO;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.DisputeStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dispute Statistics Controller
 *
 * Provides dispute statistics API for administrators
 *
 * @author BaSui
 * @since 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/disputes/statistics")
@RequiredArgsConstructor
@Tag(name = "Dispute Statistics", description = "Dispute statistics and analytics")
public class DisputeStatisticsController {

    private final DisputeStatisticsService statisticsService;

    /**
     * Get comprehensive dispute statistics
     *
     * GET /api/disputes/statistics
     *
     * @return statistics DTO
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get dispute statistics", description = "Get comprehensive dispute statistics (admin only)")
    public ApiResponse<DisputeStatisticsDTO> getStatistics() {
        log.info("Get dispute statistics request");
        DisputeStatisticsDTO stats = statisticsService.getStatistics();
        return ApiResponse.success(stats);
    }
}
