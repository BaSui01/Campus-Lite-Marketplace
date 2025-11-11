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
import com.campus.marketplace.common.security.PermissionCodes;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.OrderService;
import com.campus.marketplace.service.PaymentService;
import com.campus.marketplace.service.NotificationService;
import com.campus.marketplace.common.component.NotificationDispatcher;
import com.campus.marketplace.common.config.properties.OrderProperties;
import com.campus.marketplace.service.AuditLogService;
import com.campus.marketplace.repository.CouponUserRelationRepository;
import com.campus.marketplace.service.CouponService;
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
 * @date 2025-10-29
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final com.campus.marketplace.repository.ReviewRepository reviewRepository;
    private final com.campus.marketplace.common.utils.SensitiveWordFilter sensitiveWordFilter;
    private final NotificationService notificationService;
    private final NotificationDispatcher notificationDispatcher;
    private final OrderProperties orderProperties;
    private final AuditLogService auditLogService;
    private final CouponUserRelationRepository couponUserRelationRepository;
    private final CouponService couponService;
    private final com.campus.marketplace.service.EmailTemplateService emailTemplateService;

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

        // 为避免并发下单，使用行级写锁加载商品
        Goods goods = goodsRepository.findByIdForUpdate(request.goodsId())
                .orElseThrow(() -> new BusinessException(ErrorCode.GOODS_NOT_FOUND));
        // 校区隔离：普通用户禁止跨校购买
        try {
            // 无跨校权限时，要求买家与物品同校区
            if (!com.campus.marketplace.common.utils.SecurityUtil.hasAuthority(PermissionCodes.SYSTEM_CAMPUS_CROSS)) {
                if (buyer.getCampusId() != null && goods.getCampusId() != null
                        && !buyer.getCampusId().equals(goods.getCampusId())) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "跨校区购买被禁止");
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception ignored) { }

        if (goods.getStatus() == GoodsStatus.SOLD) {
            log.warn("物品已售出: goodsId={}", goods.getId());
            throw new BusinessException(ErrorCode.GOODS_ALREADY_SOLD);
        }

        // 仅支持从 APPROVED 状态进入下单锁定
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
                .campusId(buyer.getCampusId())
                .amount(amount)
                .discountAmount(discountAmount)
                .actualAmount(actualAmount)
                .status(OrderStatus.PENDING_PAYMENT)
                .couponId(request.couponId())
                .build();

        orderRepository.save(order);
        log.info("订单创建成功: orderNo={}, buyerId={}, sellerId={}, amount={}",
                orderNo, buyer.getId(), goods.getSellerId(), actualAmount);

        // 下单后锁定商品，等待支付
        goods.setStatus(GoodsStatus.LOCKED);
        goodsRepository.save(goods);
        log.info("物品状态更新为已锁定(待支付): goodsId={}", goods.getId());

        // 下单即发送锁定期通知（买家/卖家）
        try {
            if (notificationDispatcher != null) {
                java.util.Map<String, Object> params = new java.util.HashMap<>();
                params.put("orderNo", orderNo);
                params.put("expireMinutes", orderProperties.getMinutes());
                params.put("goodsTitle", goods.getTitle());
                params.put("price", goods.getPrice() != null ? goods.getPrice().toPlainString() : null);

                // 买家提醒
                notificationDispatcher.enqueueTemplate(
                        buyer.getId(),
                        "ORDER_CREATED_LOCKED_BUYER",
                        params,
                        com.campus.marketplace.common.enums.NotificationType.ORDER_CREATED.name(),
                        null,
                        "ORDER",
                        "/orders/" + orderNo
                );
                // 卖家提醒
                notificationDispatcher.enqueueTemplate(
                        goods.getSellerId(),
                        "ORDER_CREATED_LOCKED_SELLER",
                        params,
                        com.campus.marketplace.common.enums.NotificationType.ORDER_CREATED.name(),
                        null,
                        "ORDER",
                        "/orders/" + orderNo
                );
            }
        } catch (Exception e) {
            log.warn("发送下单锁定通知失败: orderNo={}, error={}", orderNo, e.getMessage());
        }

        // 需求变更：提交订单后不再发送邮件/通知，改为仅在支付成功后发送购买成功通知
        // 原“ORDER_CREATED”模板通知调用已移除，避免用户在未支付时就收到邮件

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
    public boolean handlePaymentCallback(PaymentCallbackRequest request, boolean signatureVerified) {
        log.info("处理支付回调: orderNo={}, transactionId={}, status={}",
                request.orderNo(), request.transactionId(), request.status());

        Order order = orderRepository.findByOrderNo(request.orderNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!signatureVerified) {
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

        // 支付成功后将商品从 LOCKED → SOLD（幂等处理）
        try {
            Goods goods = goodsRepository.findById(order.getGoodsId()).orElse(null);
            if (goods != null) {
                if (goods.getStatus() == GoodsStatus.LOCKED) {
                    goods.setStatus(GoodsStatus.SOLD);
                    goods.incrementSoldCount();
                    goodsRepository.save(goods);
                    log.info("支付成功，物品状态由LOCKED→SOLD: goodsId={}, orderNo={}", goods.getId(), order.getOrderNo());
                } else {
                    log.info("支付成功但商品状态非LOCKED，保持不变: goodsId={}, status={}", goods.getId(), goods.getStatus());
                }
            }
        } catch (Exception e) {
            log.warn("支付成功后更新商品状态失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage());
        }

        log.info("订单支付成功: orderNo={}, transactionId={}", order.getOrderNo(), request.transactionId());
        
        // 仅在此处进行一次通知发送（通过模板队列触发站内+邮件），避免重复邮件
        // 若后续需要更丰富的邮件内容，应改造模板而非在此重复直发

        // 通知买家与卖家
        try {
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("orderNo", order.getOrderNo());
            try {
                Goods g = goodsRepository.findById(order.getGoodsId()).orElse(null);
                if (g != null) {
                    params.put("goodsTitle", g.getTitle());
                    params.put("goodsPrice", g.getPrice() != null ? g.getPrice().toPlainString() : null);
                }
            } catch (Exception ignored) {}
            params.put("actualAmount", order.getActualAmount() != null ? order.getActualAmount().toPlainString() : null);
            params.put("paymentMethod", order.getPaymentMethod());
            params.put("paymentTime", order.getPaymentTime() != null ? order.getPaymentTime().toString() : null);
            params.put("transactionId", request.transactionId());
            notificationDispatcher.enqueueTemplate(order.getBuyerId(), "ORDER_PAID", params,
                    com.campus.marketplace.common.enums.NotificationType.ORDER_PAID.name(),
                    order.getId(), "ORDER", "/orders/" + order.getOrderNo());
            notificationDispatcher.enqueueTemplate(order.getSellerId(), "ORDER_PAID", params,
                    com.campus.marketplace.common.enums.NotificationType.ORDER_PAID.name(),
                    order.getId(), "ORDER", "/orders/" + order.getOrderNo());
        } catch (Exception ignored) {}
        return true;
    }

    /**
     * 取消超时订单
     *
     * 自动取消超过30分钟未支付的订单，并恢复物品状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelTimeoutOrders() {
        log.info("开始取消超时订单");

        // 查找超时未支付订单：创建时间早于超时阈值
        int minutes = Math.max(1, orderProperties.getMinutes());
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(minutes);
        var timeoutOrders = orderRepository.findTimeoutOrders(
                OrderStatus.PENDING_PAYMENT,
                timeoutThreshold
        );

        int cancelledCount = 0;
        for (Order order : timeoutOrders) {
            try {
                // 取消订单
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                // 恢复物品状态（仅当处于 LOCKED 时回退到 APPROVED）
                Goods goods = goodsRepository.findById(order.getGoodsId())
                        .orElse(null);
                if (goods != null) {
                    if (goods.getStatus() == GoodsStatus.LOCKED) {
                        goods.setStatus(GoodsStatus.APPROVED);
                        goodsRepository.save(goods);
                        log.info("超时取消，物品状态由LOCKED→APPROVED: goodsId={}, orderNo={}", goods.getId(), order.getOrderNo());
                    } else {
                        log.info("超时取消但商品状态非LOCKED，保持不变: goodsId={}, status={}", goods.getId(), goods.getStatus());
                    }
                }

                // 🎯 BaSui 新增：发送超时取消通知
                try {
                    // 通知买家：订单超时未支付已自动取消
                    if (notificationDispatcher != null) {
                        java.util.Map<String, Object> params = new java.util.HashMap<>();
                        params.put("orderNo", order.getOrderNo());
                        params.put("reason", "超时未支付");

                        notificationDispatcher.enqueueTemplate(
                                order.getBuyerId(),
                                "ORDER_TIMEOUT_CANCELLED",
                                params,
                                com.campus.marketplace.common.enums.NotificationType.ORDER_CANCELLED.name(),
                                order.getId(),
                                "ORDER",
                                "/orders/" + order.getOrderNo()
                        );

                        // 通知卖家：订单超时未支付已自动取消
                        notificationDispatcher.enqueueTemplate(
                                order.getSellerId(),
                                "ORDER_TIMEOUT_CANCELLED_SELLER",
                                params,
                                com.campus.marketplace.common.enums.NotificationType.ORDER_CANCELLED.name(),
                                order.getId(),
                                "ORDER",
                                "/orders/" + order.getOrderNo()
                        );
                    }
                } catch (Exception e) {
                    log.warn("发送超时取消通知失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage());
                }

                cancelledCount++;
                log.info("订单已取消: orderNo={}, createdAt={}",
                        order.getOrderNo(), order.getCreatedAt());
            } catch (Exception e) {
                log.error("取消订单失败: orderNo={}", order.getOrderNo(), e);
            }
        }

        log.info("取消超时订单完成: count={}", cancelledCount);
        return cancelledCount;
    }

    /**
     * 取消订单（未支付）并回退资源
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        String username = SecurityUtil.getCurrentUsername();
        User current = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        boolean isOwner = order.getBuyerId().equals(current.getId()) || order.getSellerId().equals(current.getId());
        if (!isOwner && !(SecurityUtil.hasRole("ADMIN") || SecurityUtil.hasRole("SUPER_ADMIN"))) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return; // 幂等
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "非待支付订单不可直接取消，请走退款流程");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        Goods goods = goodsRepository.findById(order.getGoodsId()).orElse(null);
        if (goods != null) {
            if (goods.getStatus() == GoodsStatus.LOCKED) {
                goods.setStatus(GoodsStatus.APPROVED);
                goodsRepository.save(goods);
            }
        }

        // 优惠券回退（若有绑定）
        couponUserRelationRepository.findFirstByOrderIdAndStatus(order.getId(), com.campus.marketplace.common.enums.CouponStatus.USED)
                .ifPresent(rel -> couponService.refundCoupon(rel.getId()));

        // 通知双方（入队模板）
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("orderNo", orderNo);
        if (notificationDispatcher != null) {
            try {
                notificationDispatcher.enqueueTemplate(order.getBuyerId(), "ORDER_CANCELLED", params,
                        com.campus.marketplace.common.enums.NotificationType.ORDER_CANCELLED.name(),
                        order.getId(), "ORDER", "/orders/" + orderNo);
                notificationDispatcher.enqueueTemplate(order.getSellerId(), "ORDER_CANCELLED", params,
                        com.campus.marketplace.common.enums.NotificationType.ORDER_CANCELLED.name(),
                        order.getId(), "ORDER", "/orders/" + orderNo);
            } catch (Exception ignored) {}
        }

        // 向后兼容：直接触发站内通知（单元测试使用此接口校验）
        try {
            notificationService.sendNotification(
                    order.getBuyerId(),
                    com.campus.marketplace.common.enums.NotificationType.ORDER_CANCELLED,
                    "订单已取消",
                    "您的订单 " + orderNo + " 已取消，物品已恢复上架",
                    order.getId(),
                    "ORDER",
                    "/orders/" + orderNo
            );
            notificationService.sendNotification(
                    order.getSellerId(),
                    com.campus.marketplace.common.enums.NotificationType.ORDER_CANCELLED,
                    "订单已取消",
                    "订单 " + orderNo + " 已被取消，物品已恢复上架",
                    order.getId(),
                    "ORDER",
                    "/orders/" + orderNo
            );
        } catch (Exception ignored) {}

        // 审计记录
        auditLogService.logActionAsync(current.getId(), current.getUsername(),
                com.campus.marketplace.common.enums.AuditActionType.ORDER_CANCEL,
                "ORDER", order.getId(), "CANCEL", "SUCCESS", null, null);
    }

    /**
     * 评价订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewOrder(com.campus.marketplace.common.dto.request.ReviewOrderRequest request) {
        log.info("评价订单: orderNo={}, rating={}", request.orderNo(), request.rating());

        String username = SecurityUtil.getCurrentUsername();
        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Order order = orderRepository.findByOrderNo(request.orderNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getBuyerId().equals(buyer.getId())) {
            log.warn("非买家尝试评价订单: orderNo={}, buyerId={}, currentUserId={}",
                    order.getOrderNo(), order.getBuyerId(), buyer.getId());
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            log.warn("订单未完成，无法评价: orderNo={}, status={}", order.getOrderNo(), order.getStatus());
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }

        if (reviewRepository.existsByOrderId(order.getId())) {
            log.warn("订单已评价: orderNo={}", order.getOrderNo());
            throw new BusinessException(ErrorCode.OPERATION_FAILED);
        }

        String filteredContent = sensitiveWordFilter.filter(request.content());

        com.campus.marketplace.common.entity.Review review = com.campus.marketplace.common.entity.Review.builder()
                .orderId(order.getId())
                .buyerId(buyer.getId())
                .sellerId(order.getSellerId())
                .rating(request.rating())
                .content(filteredContent)
                .build();

        reviewRepository.save(review);
        log.info("评价保存成功: orderNo={}, rating={}", order.getOrderNo(), request.rating());

        User seller = userRepository.findById(order.getSellerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        int creditScoreChange = (request.rating() - 3) * 2;
        seller.setCreditScore(seller.getCreditScore() + creditScoreChange);
        userRepository.save(seller);

        log.info("卖家信用分更新: sellerId={}, change={}, newScore={}",
                seller.getId(), creditScoreChange, seller.getCreditScore());
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
    public OrderResponse getOrderDetail(String orderNo) {
        Order order = orderRepository.findByOrderNoWithDetails(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 所有者或管理员可见；且无跨校权限的用户若非同校拒绝
        try {
            String username = SecurityUtil.getCurrentUsername();
            User current = userRepository.findByUsername(username).orElse(null);
            boolean isOwner = current != null && (order.getBuyerId().equals(current.getId()) || order.getSellerId().equals(current.getId()));
            boolean isAdmin = SecurityUtil.hasRole("ADMIN") || SecurityUtil.hasRole("SUPER_ADMIN");
            if (!isOwner && !isAdmin) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            if (!SecurityUtil.hasAuthority(PermissionCodes.SYSTEM_CAMPUS_CROSS)) {
                if (current != null && order.getCampusId() != null && current.getCampusId() != null
                        && !order.getCampusId().equals(current.getCampusId())) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "跨校区访问被禁止");
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception ignored) { }

        return convertToResponse(order);
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
     * 发送支付成功邮件 📧
     * 
     * 在支付回调成功后调用，发送包含商品详情的邮件给买家
     * 
     * @param order 订单对象
     * @param transactionId 交易流水号
     */
    private void sendPaymentSuccessEmail(Order order, String transactionId) {
        log.info("📧 准备发送支付成功邮件: orderNo={}", order.getOrderNo());

        // 1. 查询买家信息（获取邮箱）
        User buyer = userRepository.findById(order.getBuyerId())
                .orElse(null);
        if (buyer == null || buyer.getEmail() == null || buyer.getEmail().isEmpty()) {
            log.warn("⚠️ 买家未绑定邮箱，跳过邮件发送: buyerId={}", order.getBuyerId());
            return;
        }

        // 2. 查询商品信息
        Goods goods = goodsRepository.findById(order.getGoodsId())
                .orElse(null);
        if (goods == null) {
            log.warn("⚠️ 商品不存在，跳过邮件发送: goodsId={}", order.getGoodsId());
            return;
        }

        // 3. 查询卖家信息
        User seller = userRepository.findById(order.getSellerId())
                .orElse(null);
        String sellerName = seller != null ? seller.getUsername() : "未知卖家";

        // 4. 准备邮件数据
        String goodsTitle = goods.getTitle();
        String goodsDescription = goods.getDescription() != null && !goods.getDescription().isEmpty() 
                ? goods.getDescription() 
                : "暂无描述";
        String goodsPrice = String.format("%.2f", goods.getPrice());
        String goodsImage = (goods.getImages() != null && goods.getImages().length > 0 && goods.getImages()[0] != null && !goods.getImages()[0].isEmpty())
                ? goods.getImages()[0]
                : "https://picsum.photos/200/200?random=" + goods.getId();
        String actualAmount = String.format("%.2f", order.getActualAmount());
        
        // 支付方式格式化
        String paymentMethod = order.getPaymentMethod();
        if ("WECHAT".equalsIgnoreCase(paymentMethod)) {
            paymentMethod = "微信支付";
        } else if ("ALIPAY".equalsIgnoreCase(paymentMethod)) {
            paymentMethod = "支付宝";
        }
        
        String paymentTime = order.getPaymentTime() != null 
                ? order.getPaymentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 5. 发送邮件
        try {
            emailTemplateService.sendPaymentSuccess(
                    buyer.getEmail(),
                    order.getOrderNo(),
                    goodsTitle,
                    goodsDescription,
                    goodsPrice,
                    goodsImage,
                    actualAmount,
                    paymentMethod,
                    paymentTime,
                    transactionId,
                    sellerName
            );
            log.info("✅ 支付成功邮件发送成功: orderNo={}, email={}", order.getOrderNo(), buyer.getEmail());
        } catch (Exception e) {
            log.error("💥 支付成功邮件发送异常: orderNo={}", order.getOrderNo(), e);
            throw e;
        }
    }

    /**
     * 转换为响应 DTO
     */
    private OrderResponse convertToResponse(Order order) {
        // 计算支付截止时间
        int minutes = 30;
        try {
            minutes = Math.max(1, orderProperties.getMinutes());
        } catch (Exception ignored) {}
        java.time.LocalDateTime expireAt = order.getCreatedAt() != null
                ? order.getCreatedAt().plusMinutes(minutes)
                : null;

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
                .paymentExpireAt(expireAt)
                .timeoutMinutes(minutes)
                .build();
    }
}
