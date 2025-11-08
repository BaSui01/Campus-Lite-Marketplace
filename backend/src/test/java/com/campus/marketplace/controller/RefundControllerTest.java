package com.campus.marketplace.controller;

import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.enums.RefundStatus;
import com.campus.marketplace.common.exception.GlobalExceptionHandler;
import com.campus.marketplace.service.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundController MockMvc 测试")
class RefundControllerTest {

    @Mock
    private RefundService refundService;

    @InjectMocks
    private RefundController refundController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(refundController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("提交退款申请返回退款单号")
    void applyRefund_success() throws Exception {
        when(refundService.applyRefund(eq("ORDER-1"), eq("质量问题"), anyMap())).thenReturn("REF-100");

        mockMvc.perform(post("/orders/{orderNo}/refunds", "ORDER-1")
                        .param("reason", "质量问题")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "瑕疵明显",
                                  "images": ["https://cdn/img1.png"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("REF-100"));

        verify(refundService).applyRefund(eq("ORDER-1"), eq("质量问题"), argThat(evidence -> {
            assertThat(evidence).containsEntry("note", "瑕疵明显");
            assertThat(evidence).containsEntry("images", List.of("https://cdn/img1.png"));
            return true;
        }));
    }

    @Test
    @DisplayName("管理员审批通过退款")
    void approveRefund_success() throws Exception {
        mockMvc.perform(put("/admin/refunds/{refundNo}/approve", "REF-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(refundService).approveAndRefund("REF-1");
    }

    @Test
    @DisplayName("管理员驳回退款时传递原因")
    void rejectRefund_success() throws Exception {
        mockMvc.perform(put("/admin/refunds/{refundNo}/reject", "REF-2")
                        .param("reason", "材料不足"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(refundService).reject("REF-2", "材料不足");
    }

    @Test
    @DisplayName("查询退款详情返回实体")
    void detail_success() throws Exception {
        RefundRequest request = RefundRequest.builder()
                .refundNo("REF-3")
                .orderNo("ORDER-3")
                .applicantId(9L)
                .reason("商品损坏")
                .status(RefundStatus.APPLIED)
                .amount(new BigDecimal("99.90"))
                .build();
        when(refundService.getByRefundNo("REF-3")).thenReturn(request);

        mockMvc.perform(get("/admin/refunds/{refundNo}", "REF-3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.refundNo").value("REF-3"))
                .andExpect(jsonPath("$.data.reason").value("商品损坏"))
                .andExpect(jsonPath("$.data.amount").value(99.90));

        verify(refundService).getByRefundNo("REF-3");
    }
}
