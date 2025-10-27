package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.dto.response.OrderResponse;
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

    /**
     * 创建订单
     * 
     * 业务流程：
     * 1. 验证买家身份
     * 2. 验证物品状态（存在、已审核、未售出）
     * 3. 验证不能购买自己的物品
     * 4. 验证物品没有未取消的订单
     * 5. 生成唯一订单号
     * 6. 创建订单记录
     * 7. 更新物品状态为已售出
     * 
     * @param request 订单请求
     * @return 订单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(CreateOrderRequest request) {
        log.info("创建订单: goodsId={}, couponId={}", request.goodsId(), request.couponId());

        // 1. 获取当前买家
        String username = SecurityUtil.getCurrentUsername();
        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 验证物品存在
        Goods goods = goodsRepository.findById(request.goodsId())
                .orElseThrow(() -> new BusinessException(ErrorCode.GOODS_NOT_FOUND));

        // 3. 验证物品未售出（优先检查）
        if (goods.getStatus() == GoodsStatus.SOLD) {
            log.warn("物品已售出: goodsId={}", goods.getId());
            throw new BusinessException(ErrorCode.GOODS_ALREADY_SOLD);
        }

        // 4. 验证物品已审核
        if (goods.getStatus() != GoodsStatus.APPROVED) {
            log.warn("物品未审核: goodsId={}, status={}", goods.getId(), goods.getStatus());
            throw new BusinessException(ErrorCode.GOODS_NOT_APPROVED);
        }

        // 5. 验证不能购买自己的物品
        if (goods.getSellerId().equals(buyer.getId())) {
            log.warn("不能购买自己的物品: buyerId={}, goodsId={}", buyer.getId(), goods.getId());
            throw new BusinessException(ErrorCode.CANNOT_BUY_OWN_GOODS);
        }

        // 6. 验证物品没有未取消的订单
        boolean hasActiveOrder = orderRepository.existsByGoodsIdAndStatusNot(
                goods.getId(), OrderStatus.CANCELLED
        );
        if (hasActiveOrder) {
            log.warn("物品已有未取消订单: goodsId={}", goods.getId());
            throw new BusinessException(ErrorCode.GOODS_ALREADY_SOLD);
        }

        // 7. 生成唯一订单号（ORD + 17位时间戳）
        String orderNo = generateOrderNo();

        // 8. 计算订单金额（暂不考虑优惠券）
        BigDecimal amount = goods.getPrice();
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal actualAmount = amount.subtract(discountAmount);

        // 9. 创建订单
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

        // 10. 更新物品状态为已售出
        goods.setStatus(GoodsStatus.SOLD);
        goodsRepository.save(goods);
        log.info("物品状态更新为已售出: goodsId={}", goods.getId());

        return orderNo;
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
     * 
     * 格式：ORD + 17位时间戳（yyyyMMddHHmmssSSS）
     * 
     * @return 订单号
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
