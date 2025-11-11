package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.OrderResponse;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单创建、买卖家查询与取消")
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "创建订单", description = "提交下单请求，返回订单号")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "创建订单的 JSON 请求体",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateOrderRequest.class),
                    examples = @ExampleObject(
                            name = "请求示例",
                            value = """
                                    {
                                      \"goodsId\": 12345,
                                      \"couponId\": 888
                                    }
                                    """
                    )
            )
    )
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "买家订单列表", description = "分页查询我作为买家的订单")
    public ApiResponse<Page<OrderResponse>> listBuyerOrders(
            @Parameter(description = "订单状态", example = "PENDING_PAYMENT") @RequestParam(required = false) String status,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "卖家订单列表", description = "分页查询我作为卖家的订单")
    public ApiResponse<Page<OrderResponse>> listSellerOrders(
            @Parameter(description = "订单状态", example = "PAID") @RequestParam(required = false) String status,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "订单详情", description = "根据订单号查询订单详情")
    public ApiResponse<Order> getOrderDetail(@Parameter(description = "订单号", example = "O202510270001") @PathVariable String orderNo) {
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "取消订单", description = "仅未支付订单可取消")
    public ApiResponse<Void> cancelOrder(@Parameter(description = "订单号", example = "O202510270001") @PathVariable String orderNo) {
        log.info("取消订单: orderNo={}", orderNo);
        orderService.cancelOrder(orderNo);
        return ApiResponse.success(null);
    }
}
