package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.DisputeStatisticsDTO;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.DisputeStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dispute Statistics Controller Test
 *
 * @author BaSui
 * @since 2025-11-03
 */
@WebMvcTest(DisputeStatisticsController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Dispute Statistics Controller Test")
class DisputeStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DisputeStatisticsService statisticsService;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Mock UserRepository for SecurityUtil.getCurrentUserId()
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "ADMIN")
    @DisplayName("Get statistics - should return statistics")
    void getStatistics_ShouldReturnStatistics() throws Exception {
        DisputeStatisticsDTO stats = DisputeStatisticsDTO.builder()
                .totalDisputes(100L)
                .negotiatingCount(20L)
                .arbitratingCount(10L)
                .completedCount(50L)
                .closedCount(20L)
                .negotiationSuccessRate(30.0)
                .disputeTypeDistribution(new HashMap<>())
                .arbitrationResultDistribution(new HashMap<>())
                .build();

        when(statisticsService.getStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/disputes/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalDisputes").value(100))
                .andExpect(jsonPath("$.data.negotiationSuccessRate").value(30.0));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Get statistics without admin role - should return 403")
    void getStatistics_WithoutAdminRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/disputes/statistics"))
                .andExpect(status().isForbidden());
    }
}
