package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.dto.request.PayOrderRequest;
import com.campus.marketplace.common.dto.request.PaymentCallbackRequest;
import com.campus.marketplace.common.dto.response.OrderResponse;
import com.campus.marketplace.common.dto.response.PaymentResponse;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;

/**
 * 订单服务接口
 *
 * 提供订单创建、查询、支付等功能
 *
 * @author BaSui
 * @date 2025-10-29
 */

public interface OrderService {

    /**
     * 创建订单
     * 
     * @param request 订单请求
     * @return 订单号
     */
    String createOrder(CreateOrderRequest request);

    /**
     * 支付订单
     * 
     * @param request 支付请求
     * @return 支付响应（包含支付链接）
     */
    PaymentResponse payOrder(PayOrderRequest request);

    /**
     * 处理支付回调
     *
     * @param request 回调请求
     * @param signatureVerified 回调签名是否已通过校验
     * @return 是否处理成功
     */
    boolean handlePaymentCallback(PaymentCallbackRequest request, boolean signatureVerified);

    /**
     * 取消超时订单
     * 
     * @return 取消的订单数量
     */
    int cancelTimeoutOrders();

    /**
     * 取消订单（买家/卖家均可在未支付时取消）
     *
     * @param orderNo 订单号
     */
    void cancelOrder(String orderNo);

    /**
     * 评价订单
     * 
     * @param request 评价请求
     */
    void reviewOrder(com.campus.marketplace.common.dto.request.ReviewOrderRequest request);

    /**
     * 查询买家订单列表
     * 
     * @param status 订单状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    Page<OrderResponse> listBuyerOrders(String status, int page, int size);

    /**
     * 查询卖家订单列表
     * 
     * @param status 订单状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    Page<OrderResponse> listSellerOrders(String status, int page, int size);

    /**
     * 查询订单详情
     * 
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderResponse getOrderDetail(String orderNo);

    /**
     * 获取订单实付金额（系统内部使用）
     * 不进行权限校验，供支付回调等系统流程查询金额校验使用
     */
    BigDecimal getOrderActualAmount(String orderNo);

    /**
     * 更新订单的配送与收货信息
     * @param orderNo 订单号
     * @param request 配送信息
     */
    void updateOrderDelivery(String orderNo, com.campus.marketplace.common.dto.request.UpdateOrderDeliveryRequest request);

    /**
     * 卖家发货（快递）→ 订单进入 SHIPPED
     * @param orderNo 订单号
     * @param request 发货信息（物流公司、运单号）
     */
    void shipOrder(String orderNo, com.campus.marketplace.common.dto.request.ShipOrderRequest request);

    /**
     * 买家确认收货 → 订单从 DELIVERED 流转到 COMPLETED
     * @param orderNo 订单号
     */
    void confirmReceipt(String orderNo);
}
