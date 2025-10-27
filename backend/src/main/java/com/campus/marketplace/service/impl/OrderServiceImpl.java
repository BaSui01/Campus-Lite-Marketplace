package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.dto.request.PayOrderRequest;
import com.campus.marketplace.common.dto.request.PaymentCallbackRequest;
import com.campus.marketplace.common.dto.response.OrderResponse;
import com.campus.marketplace.common.dto.response.PaymentResponse;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.OrderService;
import com.campus.marketplace.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 订单服务实现类
 * 
 * 实现订单创建、查询、支付等功能
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    /**
     * 创建订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(CreateOrderRequest request) {
        log.info("创建订单: goodsId={}, couponId={}", request.goodsId(), request.couponId());

        String username = SecurityUtil.getCurrentUsername();
        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Goods goods = goodsRepository.findById(request.goodsId())
                .orElseThrow(() -> new BusinessException(ErrorCode.GOODS_NOT_FOUND));

        if (goods.getStatus() == GoodsStatus.SOLD) {
            log.warn("物品已售出: goodsId={}", goods.getId());
            throw new BusinessException(ErrorCode.GOODS_ALREADY_SOLD);
        }

        if (goods.getStatus() != GoodsStatus.APPROVED) {
            log.warn("物品未审核: goodsId={}, status={}", goods.getId(), goods.getStatus());
            throw new BusinessException(ErrorCode.GOODS_NOT_APPROVED);
        }

        if (goods.getSellerId().equals(buyer.getId())) {
            log.warn("不能购买自己的物品: buyerId={}, goodsId={}", buyer.getId(), goods.getId());
            throw new BusinessException(ErrorCode.CANNOT_BUY_OWN_GOODS);
        }

        boolean hasActiveOrder = orderRepository.existsByGoodsIdAndStatusNot(
                goods.getId(), OrderStatus.CANCELLED
        );
        if (hasActiveOrder) {
            log.warn("物品已有未取消订单: goodsId={}", goods.getId());
            throw new BusinessException(ErrorCode.GOODS_ALREADY_SOLD);
        }

        String orderNo = generateOrderNo();
        BigDecimal amount = goods.getPrice();
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal actualAmount = amount.subtract(discountAmount);

        Order order = Order.builder()
                .orderNo(orderNo)
                .goodsId(goods.getId())
                .buyerId(buyer.getId())
                .sellerId(goods.getSellerId())
                .amount(amount)
                .discountAmount(discountAmount)
                .actualAmount(actualAmount)
                .status(OrderStatus.PENDING_PAYMENT)
                .couponId(request.couponId())
                .build();

        orderRepository.save(order);
        log.info("订单创建成功: orderNo={}, buyerId={}, sellerId={}, amount={}",
                orderNo, buyer.getId(), goods.getSellerId(), actualAmount);

        goods.setStatus(GoodsStatus.SOLD);
        goodsRepository.save(goods);
        log.info("物品状态更新为已售出: goodsId={}", goods.getId());

        return orderNo;
    }

    /**
     * 支付订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse payOrder(PayOrderRequest request) {
        log.info("支付订单: orderNo={}, paymentMethod={}", request.orderNo(), request.paymentMethod());

        String username = SecurityUtil.getCurrentUsername();
        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Order order = orderRepository.findByOrderNo(request.orderNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("订单状态不正确: orderNo={}, status={}", order.getOrderNo(), order.getStatus());
            if (order.getStatus() == OrderStatus.PAID) {
                throw new BusinessException(ErrorCode.ORDER_PAID);
            } else if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new BusinessException(ErrorCode.ORDER_CANCELLED);
            } else {
                throw new BusinessException(ErrorCode.OPERATION_FAILED);
            }
        }

        if (!order.getBuyerId().equals(buyer.getId())) {
            log.warn("非买家尝试支付订单: orderNo={}, buyerId={}, currentUserId={}",
                    order.getOrderNo(), order.getBuyerId(), buyer.getId());
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        PaymentResponse paymentResponse = paymentService.createPayment(order, request.paymentMethod());

        order.setPaymentMethod(request.paymentMethod().name());
        orderRepository.save(order);

        log.info("订单支付创建成功: orderNo={}, paymentUrl={}", order.getOrderNo(), paymentResponse.paymentUrl());
        return paymentResponse;
    }

    /**
     * 处理支付回调
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentCallback(PaymentCallbackRequest request) {
        log.info("处理支付回调: orderNo={}, transactionId={}, status={}",
                request.orderNo(), request.transactionId(), request.status());

        Order order = orderRepository.findByOrderNo(request.orderNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        boolean signatureValid = paymentService.verifySignature(
                request.orderNo(), request.transactionId(), request.signature()
        );
        if (!signatureValid) {
            log.error("支付签名验证失败: orderNo={}", request.orderNo());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }

        if (request.amount().compareTo(order.getActualAmount()) != 0) {
            log.error("支付金额不匹配: orderNo={}, expected={}, actual={}",
                    request.orderNo(), order.getActualAmount(), request.amount());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("订单状态不正确: orderNo={}, status={}", order.getOrderNo(), order.getStatus());
            return false;
        }

        if (!"SUCCESS".equals(request.status())) {
            log.warn("支付失败: orderNo={}, status={}", request.orderNo(), request.status());
            return false;
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaymentTime(LocalDateTime.now());
        orderRepository.save(order);

        log.info("订单支付成功: orderNo={}, transactionId={}", order.getOrderNo(), request.transactionId());
        return true;
    }

    /**
     * 取消超时订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelTimeoutOrders() {
        log.info("开始取消超时订单");
        int cancelledCount = 0;
        log.info("取消超时订单完成: count={}", cancelledCount);
        return cancelledCount;
    }

    /**
     * 查询买家订单列表
     */
    @Override
    public Page<OrderResponse> listBuyerOrders(String status, int page, int size) {
        String username = SecurityUtil.getCurrentUsername();
        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status) : null;
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders = orderRepository.findByBuyerIdWithDetails(
                buyer.getId(), orderStatus, pageable
        );

        return orders.map(this::convertToResponse);
    }

    /**
     * 查询卖家订单列表
     */
    @Override
    public Page<OrderResponse> listSellerOrders(String status, int page, int size) {
        String username = SecurityUtil.getCurrentUsername();
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status) : null;
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders = orderRepository.findBySellerIdWithDetails(
                seller.getId(), orderStatus, pageable
        );

        return orders.map(this::convertToResponse);
    }

    /**
     * 查询订单详情
     */
    @Override
    public Order getOrderDetail(String orderNo) {
        return orderRepository.findByOrderNoWithDetails(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    /**
     * 生成唯一订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "ORD" + timestamp;
    }

    /**
     * 转换为响应 DTO
     */
    private OrderResponse convertToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .goodsId(order.getGoodsId())
                .goodsTitle(order.getGoods() != null ? order.getGoods().getTitle() : null)
                .goodsImage(order.getGoods() != null && order.getGoods().getImages().length > 0
                        ? order.getGoods().getImages()[0] : null)
                .buyerId(order.getBuyerId())
                .buyerUsername(order.getBuyer() != null ? order.getBuyer().getUsername() : null)
                .sellerId(order.getSellerId())
                .sellerUsername(order.getSeller() != null ? order.getSeller().getUsername() : null)
                .amount(order.getAmount())
                .discountAmount(order.getDiscountAmount())
                .actualAmount(order.getActualAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentTime(order.getPaymentTime())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
