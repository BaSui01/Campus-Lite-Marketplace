package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.OrderResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 * 
 * 提供订单创建、查询等 REST API
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     * 
     * POST /api/orders
     * 
     * @param request 订单请求
     * @return 订单号
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ApiResponse<String> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("收到创建订单请求: goodsId={}", request.goodsId());
        String orderNo = orderService.createOrder(request);
        return ApiResponse.success(orderNo);
    }

    /**
     * 查询买家订单列表
     * 
     * GET /api/orders/buyer?status=PENDING_PAYMENT&page=0&size=20
     * 
     * @param status 订单状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    @GetMapping("/buyer")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ApiResponse<Page<OrderResponse>> listBuyerOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("查询买家订单列表: status={}, page={}, size={}", status, page, size);
        Page<OrderResponse> orders = orderService.listBuyerOrders(status, page, size);
        return ApiResponse.success(orders);
    }

    /**
     * 查询卖家订单列表
     * 
     * GET /api/orders/seller?status=PAID&page=0&size=20
     * 
     * @param status 订单状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    @GetMapping("/seller")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ApiResponse<Page<OrderResponse>> listSellerOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("查询卖家订单列表: status={}, page={}, size={}", status, page, size);
        Page<OrderResponse> orders = orderService.listSellerOrders(status, page, size);
        return ApiResponse.success(orders);
    }

    /**
     * 查询订单详情
     * 
     * GET /api/orders/{orderNo}
     * 
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/{orderNo}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ApiResponse<Order> getOrderDetail(@PathVariable String orderNo) {
        log.info("查询订单详情: orderNo={}", orderNo);
        Order order = orderService.getOrderDetail(orderNo);
        return ApiResponse.success(order);
    }

    /**
     * 取消订单（未支付）
     *
     * POST /api/orders/{orderNo}/cancel
     */
    @PostMapping("/{orderNo}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ApiResponse<Void> cancelOrder(@PathVariable String orderNo) {
        log.info("取消订单: orderNo={}", orderNo);
        orderService.cancelOrder(orderNo);
        return ApiResponse.success(null);
    }
}
