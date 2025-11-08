package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.dto.response.OrderResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.service.OrderService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("OrderController MockMvc 集成测试")
class OrderControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/orders -> 创建订单返回订单号")
    @WithMockUser(roles = "STUDENT")
    void createOrder_returnsOrderNo() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(12345L, 888L);
        when(orderService.createOrder(request)).thenReturn("O202510270001");

        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("O202510270001"));

        verify(orderService).createOrder(request);
    }

    @Test
    @DisplayName("GET /api/orders/buyer -> 查询买家订单列表")
    @WithMockUser(roles = "TEACHER")
    void listBuyerOrders_returnsPagedOrders() throws Exception {
        OrderResponse order = OrderResponse.builder()
                .id(1L)
                .orderNo("O202510270001")
                .goodsId(12345L)
                .goodsTitle("iPad Pro")
                .amount(new BigDecimal("5999"))
                .actualAmount(new BigDecimal("5599"))
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.now())
                .build();
        when(orderService.listBuyerOrders(eq("PAID"), eq(0), eq(5)))
                .thenReturn(new PageImpl<>(List.of(order)));

        mockMvc.perform(get("/api/orders/buyer")
                        .param("status", "PAID")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].orderNo").value("O202510270001"))
                .andExpect(jsonPath("$.data.content[0].status").value("PAID"));
    }

    @Test
    @DisplayName("GET /api/orders/seller -> 查询卖家订单列表")
    @WithMockUser(roles = "STUDENT")
    void listSellerOrders_returnsPagedOrders() throws Exception {
        when(orderService.listSellerOrders(eq(null), eq(1), eq(10)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/orders/seller")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(orderService).listSellerOrders(null, 1, 10);
    }

    @Test
    @DisplayName("GET /api/orders/{orderNo} -> 查询订单详情")
    @WithMockUser(roles = "STUDENT")
    void getOrderDetail_returnsEntity() throws Exception {
        Order order = Order.builder()
                .orderNo("O202510270001")
                .goodsId(12345L)
                .buyerId(2001L)
                .sellerId(3001L)
                .amount(new BigDecimal("5999"))
                .actualAmount(new BigDecimal("5599"))
                .status(OrderStatus.PAID)
                .build();
        when(orderService.getOrderDetail("O202510270001")).thenReturn(order);

        mockMvc.perform(get("/api/orders/{orderNo}", "O202510270001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("O202510270001"))
                .andExpect(jsonPath("$.data.amount").value(5999));

        verify(orderService).getOrderDetail("O202510270001");
    }

    @Test
    @DisplayName("POST /api/orders/{orderNo}/cancel -> 取消订单")
    @WithMockUser(roles = "TEACHER")
    void cancelOrder_returnsSuccess() throws Exception {

        mockMvc.perform(post("/api/orders/{orderNo}/cancel", "O202510270002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(orderService).cancelOrder("O202510270002");
    }
}
