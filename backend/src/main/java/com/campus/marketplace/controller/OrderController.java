package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.dto.request.PayOrderRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.OrderResponse;
import com.campus.marketplace.common.dto.response.PaymentResponse;
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
    public ApiResponse<OrderResponse> getOrderDetail(@Parameter(description = "订单号", example = "O202510270001") @PathVariable String orderNo) {
        log.info("查询订单详情: orderNo={}", orderNo);
        OrderResponse order = orderService.getOrderDetail(orderNo);
        return ApiResponse.success(order);
    }

    /**
     * 支付订单 💳
     * 
     * POST /api/orders/{orderNo}/pay
     * 
     * @param orderNo 订单号
     * @param request 支付请求（包含支付方式）
     * @return 支付响应（包含支付链接或二维码）
     */
    @PostMapping("/{orderNo}/pay")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "支付订单", description = "为指定订单创建支付请求")
    public ApiResponse<PaymentResponse> payOrder(
            @Parameter(description = "订单号", example = "O202510270001") @PathVariable String orderNo,
            @Valid @RequestBody PayOrderRequest request
    ) {
        log.info("支付订单: orderNo={}, paymentMethod={}", orderNo, request.paymentMethod());
        
        // 确保 orderNo 一致
        if (!orderNo.equals(request.orderNo())) {
            throw new IllegalArgumentException("订单号不一致");
        }
        
        PaymentResponse paymentResponse = orderService.payOrder(request);
        return ApiResponse.success(paymentResponse);
    }

    /**
     * 更新订单配送/收货信息
     *
     * POST /api/orders/{orderNo}/delivery
     */
    @PostMapping("/{orderNo}/delivery")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新订单配送/收货信息", description = "设置配送方式（面交/快递）及收货信息")
    public ApiResponse<Void> updateOrderDelivery(
            @Parameter(description = "订单号", example = "O202510270001") @PathVariable String orderNo,
            @Valid @RequestBody com.campus.marketplace.common.dto.request.UpdateOrderDeliveryRequest request
    ) {
        orderService.updateOrderDelivery(orderNo, request);
        return ApiResponse.success(null);
    }

    /**
     * 卖家发货（快递）
     *
     * POST /api/orders/{orderNo}/ship
     */
    @PostMapping("/{orderNo}/ship")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "卖家发货", description = "仅卖家可操作，订单需为已支付且为快递配送")
    public ApiResponse<Void> shipOrder(
            @Parameter(description = "订单号", example = "O202510270001") @PathVariable String orderNo,
            @Valid @RequestBody com.campus.marketplace.common.dto.request.ShipOrderRequest request
    ) {
        orderService.shipOrder(orderNo, request);
        return ApiResponse.success(null);
    }

    /**
     * 买家确认收货
     *
     * POST /api/orders/{orderNo}/confirm-receipt
     */
    @PostMapping("/{orderNo}/confirm-receipt")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "买家确认收货", description = "仅买家可操作，订单需处于 DELIVERED 状态")
    public ApiResponse<Void> confirmReceipt(
            @Parameter(description = "订单号", example = "O202510270001") @PathVariable String orderNo
    ) {
        orderService.confirmReceipt(orderNo);
        return ApiResponse.success(null);
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
