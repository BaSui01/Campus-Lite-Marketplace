package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.request.ApproveGoodsRequest;
import com.campus.marketplace.common.dto.request.CreateGoodsRequest;
import com.campus.marketplace.common.dto.response.GoodsDetailResponse;
import com.campus.marketplace.common.dto.response.GoodsResponse;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.service.GoodsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = GoodsController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("GoodsController MockMvc 集成测试")
class GoodsControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoodsService goodsService;

    @Test
    @DisplayName("POST /api/goods -> 发布物品返回物品ID")
    @WithMockUser(roles = "STUDENT")
    void createGoods_returnsGoodsId() throws Exception {
        CreateGoodsRequest request = new CreateGoodsRequest(
                "Apple MacBook Pro",
                "M1 16G 512G 几乎全新，支持自提",
                new BigDecimal("6999.00"),
                101L,
                List.of("https://cdn/goods-1.png"),
                List.of(1L, 3L)
        );
        when(goodsService.createGoods(request)).thenReturn(42L);

        mockMvc.perform(post("/goods")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(42));

        verify(goodsService).createGoods(request);
    }

    @Test
    @DisplayName("GET /api/goods -> 带筛选条件的列表查询")
    void listGoods_withFilters_returnsPagedData() throws Exception {
        GoodsResponse response = GoodsResponse.builder()
                .id(7L)
                .title("Sony WH-1000XM5")
                .price(new BigDecimal("2299"))
                .categoryId(12L)
                .categoryName("耳机")
                .sellerId(88L)
                .sellerUsername("alice")
                .status(GoodsStatus.APPROVED)
                .viewCount(128)
                .favoriteCount(5)
                .coverImage("https://cdn/wh1000.png")
                .createdAt(LocalDateTime.now())
                .build();
        when(goodsService.listGoods(eq("耳机"), eq(12L), any(), any(), eq(0), eq(10),
                eq("createdAt"), eq("DESC"), eq(List.of(1L, 2L))))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/goods")
                        .param("keyword", "耳机")
                        .param("categoryId", "12")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "DESC")
                        .param("tags", "1")
                        .param("tags", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].title").value("Sony WH-1000XM5"))
                .andExpect(jsonPath("$.data.content[0].categoryName").value("耳机"));
    }

    @Test
    @DisplayName("GET /api/goods/{id} -> 查询物品详情")
    void getGoodsDetail_returnsDetail() throws Exception {
        GoodsDetailResponse response = GoodsDetailResponse.builder()
                .id(7L)
                .title("Switch OLED")
                .price(new BigDecimal("1999"))
                .status(GoodsStatus.APPROVED)
                .images(List.of("https://cdn/switch.png"))
                .build();
        when(goodsService.getGoodsDetail(7L)).thenReturn(response);

        mockMvc.perform(get("/goods/{id}", 7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Switch OLED"))
                .andExpect(jsonPath("$.data.images[0]").value("https://cdn/switch.png"));
    }

    @Test
    @DisplayName("GET /api/goods/pending -> 管理员查询待审核列表")
    @WithMockUser(authorities = PermissionCodes.SYSTEM_GOODS_APPROVE)
    void listPendingGoods_requiresPermission() throws Exception {
        when(goodsService.listPendingGoods(1, 5))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/goods/pending").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(goodsService).listPendingGoods(1, 5);
    }

    @Test
    @DisplayName("POST /api/goods/{id}/approve -> 审核物品")
    @WithMockUser(authorities = PermissionCodes.SYSTEM_GOODS_APPROVE)
    void approveGoods_updatesStatus() throws Exception {
        ApproveGoodsRequest request = new ApproveGoodsRequest(true, null);

        mockMvc.perform(post("/goods/{id}/approve", 55)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(content().json("{\"code\":200,\"message\":\"操作成功\",\"data\":null}"));

        verify(goodsService).approveGoods(55L, true, null);
    }
}
