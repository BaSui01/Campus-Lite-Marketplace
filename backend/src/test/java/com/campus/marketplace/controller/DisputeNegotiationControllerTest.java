package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.ProposeDisputeRequest;
import com.campus.marketplace.common.dto.request.RespondProposalRequest;
import com.campus.marketplace.common.dto.request.SendNegotiationRequest;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.DisputeNegotiationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dispute Negotiation Controller Test
 *
 * @author BaSui
 * @since 2025-11-03
 */
@WebMvcTest(DisputeNegotiationController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Dispute Negotiation Controller Test")
class DisputeNegotiationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DisputeNegotiationService negotiationService;

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
    @DisplayName("Send message - should return message ID")
    void sendMessage_ShouldReturnMessageId() throws Exception {
        SendNegotiationRequest request = new SendNegotiationRequest(1L, "Test message");
        when(negotiationService.sendTextMessage(any(), anyLong())).thenReturn(1L);

        mockMvc.perform(post("/disputes/negotiations/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Propose resolution - should return proposal ID")
    void proposeResolution_ShouldReturnProposalId() throws Exception {
        ProposeDisputeRequest request = new ProposeDisputeRequest();
        request.setDisputeId(1L);
        request.setProposedRefundAmount(BigDecimal.valueOf(100));
        request.setContent("Test proposal content with minimum length");

        when(negotiationService.proposeResolution(any(), anyLong())).thenReturn(1L);

        mockMvc.perform(post("/disputes/negotiations/proposals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Respond to proposal - should return success")
    void respondToProposal_ShouldReturnSuccess() throws Exception {
        RespondProposalRequest request = new RespondProposalRequest();
        request.setProposalId(1L);
        request.setAccepted(true);
        request.setResponseNote("Accepted");

        when(negotiationService.respondToProposal(any(), anyLong())).thenReturn(true);

        mockMvc.perform(post("/disputes/negotiations/proposals/1/respond")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Get negotiation history - should return list")
    void getNegotiationHistory_ShouldReturnList() throws Exception {
        when(negotiationService.getNegotiationHistory(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/disputes/negotiations/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Get pending proposal - should return optional")
    void getPendingProposal_ShouldReturnOptional() throws Exception {
        when(negotiationService.getPendingProposal(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/disputes/negotiations/1/pending-proposal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
