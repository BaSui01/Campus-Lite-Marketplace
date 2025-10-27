package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.dto.request.PayOrderRequest;
import com.campus.marketplace.common.dto.request.PaymentCallbackRequest;
import com.campus.marketplace.common.dto.response.OrderResponse;
import com.campus.marketplace.common.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;

/**
 * 订单服务接口
 * 
 * 提供订单创建、查询、支付等功能
 * 
 * @author BaSui
 * @date 2025-10-27
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
     * @return 是否处理成功
     */
    boolean handlePaymentCallback(PaymentCallbackRequest request);

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
    com.campus.marketplace.common.entity.Order getOrderDetail(String orderNo);
}
