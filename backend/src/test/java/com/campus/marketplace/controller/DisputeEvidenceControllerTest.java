package com.campus.marketplace.controller;

import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.DisputeEvidenceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisputeEvidenceController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Dispute Evidence Controller Test")
class DisputeEvidenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DisputeEvidenceService evidenceService;

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
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Get dispute evidence - should return list")
    void getDisputeEvidence_ShouldReturnList() throws Exception {
        when(evidenceService.getDisputeEvidence(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/disputes/evidence/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Get buyer evidence - should return list")
    void getBuyerEvidence_ShouldReturnList() throws Exception {
        when(evidenceService.getBuyerEvidence(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/disputes/evidence/1/buyer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Get seller evidence - should return list")
    void getSellerEvidence_ShouldReturnList() throws Exception {
        when(evidenceService.getSellerEvidence(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/disputes/evidence/1/seller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
