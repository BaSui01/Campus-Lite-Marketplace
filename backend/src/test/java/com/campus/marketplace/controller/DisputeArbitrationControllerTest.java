package com.campus.marketplace.controller;

import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.DisputeArbitrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisputeArbitrationController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Dispute Arbitration Controller Test")
class DisputeArbitrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DisputeArbitrationService arbitrationService;

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
    @DisplayName("Assign arbitrator - should return success")
    void assignArbitrator_ShouldReturnSuccess() throws Exception {
        when(arbitrationService.assignArbitrator(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(post("/api/disputes/arbitrations/1/assign")
                        .with(csrf())
                        .param("arbitratorId", "300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "ADMIN")
    @DisplayName("Get arbitration detail - should return optional")
    void getArbitrationDetail_ShouldReturnOptional() throws Exception {
        when(arbitrationService.getArbitrationDetail(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/disputes/arbitrations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "ADMIN")
    @DisplayName("Get arbitrator cases - should return list")
    void getArbitratorCases_ShouldReturnList() throws Exception {
        when(arbitrationService.getArbitratorCases(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/api/disputes/arbitrations/my-cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
