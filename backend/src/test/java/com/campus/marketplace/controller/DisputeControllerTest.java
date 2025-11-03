package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.DisputeDTO;
import com.campus.marketplace.common.dto.DisputeDetailDTO;
import com.campus.marketplace.common.dto.request.CreateDisputeRequest;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.DisputeStatus;
import com.campus.marketplace.common.enums.DisputeType;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.DisputeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Dispute Controller Integration Test
 *
 * @author BaSui
 * @since 2025-11-03
 */
@WebMvcTest(DisputeController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Dispute Controller Test")
class DisputeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DisputeService disputeService;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Mock UserRepository for SecurityUtil.getCurrentUserId()
        // 所有测试方法都使用 username="testuser"
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(mockUser));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Submit dispute - should return 200")
    void submitDispute_ShouldReturn200() throws Exception {

        CreateDisputeRequest request = new CreateDisputeRequest();
        request.setOrderId(1L);
        request.setDisputeType(DisputeType.QUALITY_ISSUE);
        request.setDescription("Test description with at least 20 characters for validation");

        when(disputeService.submitDispute(any(), anyLong())).thenReturn(1L);

        mockMvc.perform(post("/api/disputes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Get user disputes - should return page")
    void getUserDisputes_ShouldReturnPage() throws Exception {
        DisputeDTO dispute = DisputeDTO.builder()
                .id(1L)
                .disputeCode("DSP-20251103-000001")
                .status(DisputeStatus.NEGOTIATING)
                .build();

        Page<DisputeDTO> page = new PageImpl<>(List.of(dispute), PageRequest.of(0, 20), 1);
        when(disputeService.getUserDisputes(anyLong(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/disputes")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Get dispute detail - should return detail")
    void getDisputeDetail_ShouldReturnDetail() throws Exception {
        DisputeDetailDTO detail = DisputeDetailDTO.builder()
                .id(1L)
                .disputeCode("DSP-20251103-000001")
                .status(DisputeStatus.NEGOTIATING)
                .build();

        when(disputeService.getDisputeDetail(anyLong())).thenReturn(detail);

        mockMvc.perform(get("/api/disputes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    @DisplayName("Escalate dispute - should return success")
    void escalateToArbitration_ShouldReturnSuccess() throws Exception {
        when(disputeService.escalateToArbitration(anyLong())).thenReturn(true);

        mockMvc.perform(post("/api/disputes/1/escalate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "ADMIN")
    @DisplayName("Close dispute - should return success")
    void closeDispute_ShouldReturnSuccess() throws Exception {
        when(disputeService.closeDispute(anyLong(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/disputes/1/close")
                        .with(csrf())
                        .param("closeReason", "Test reason"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "ADMIN")
    @DisplayName("Get all disputes - should return page")
    void getAllDisputes_ShouldReturnPage() throws Exception {
        DisputeDTO dispute = DisputeDTO.builder()
                .id(1L)
                .disputeCode("DSP-20251103-000001")
                .status(DisputeStatus.NEGOTIATING)
                .build();

        Page<DisputeDTO> page = new PageImpl<>(List.of(dispute), PageRequest.of(0, 20), 1);
        when(disputeService.getUserDisputes(isNull(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/disputes/admin/all")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1L));
    }
}
