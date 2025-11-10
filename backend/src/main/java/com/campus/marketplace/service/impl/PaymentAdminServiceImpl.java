package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.PaymentRecordDTO;
import com.campus.marketplace.common.dto.response.PaymentStatisticsDTO;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.service.PaymentAdminService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付管理服务实现类（管理员）
 * 
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentAdminServiceImpl implements PaymentAdminService {

    private final OrderRepository orderRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentRecordDTO> listPayments(
        String keyword,
        String status,
        String paymentMethod,
        String startDate,
        String endDate,
        int page,
        int size
    ) {
        log.info("查询支付记录列表: keyword={}, status={}, method={}, page={}, size={}",
                keyword, status, paymentMethod, page, size);

        // 创建分页对象
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentTime"));

        // 使用 Criteria API 构建动态查询
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // 查询总数
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Order> countRoot = countQuery.from(Order.class);
        countRoot.fetch("goods");
        countRoot.fetch("buyer");
        countRoot.fetch("seller");
        countQuery.select(cb.count(countRoot));
        countQuery.where(buildPredicates(cb, countRoot, keyword, status, paymentMethod, startDate, endDate));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        // 查询数据
        CriteriaQuery<Order> dataQuery = cb.createQuery(Order.class);
        Root<Order> dataRoot = dataQuery.from(Order.class);
        dataRoot.fetch("goods");
        dataRoot.fetch("buyer");
        dataRoot.fetch("seller");
        dataQuery.select(dataRoot);
        dataQuery.where(buildPredicates(cb, dataRoot, keyword, status, paymentMethod, startDate, endDate));
        dataQuery.orderBy(cb.desc(dataRoot.get("paymentTime")));

        List<Order> orders = entityManager.createQuery(dataQuery)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        // 转换为 DTO
        List<PaymentRecordDTO> dtos = orders.stream()
                .map(this::convertToDTO)
                .toList();

        return new PageImpl<>(dtos, pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentRecordDTO getPaymentDetail(String orderNo) {
        log.info("查询支付详情: orderNo={}", orderNo);

        Order order = orderRepository.findByOrderNoWithDetails(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        return convertToDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatisticsDTO getStatistics(String startDate, String endDate) {
        log.info("查询支付统计: startDate={}, endDate={}", startDate, endDate);

        LocalDateTime start = parseDateTime(startDate, true);
        LocalDateTime end = parseDateTime(endDate, false);

        // 查询已支付订单（PAID/SHIPPED/COMPLETED/REFUNDED）
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> query = cb.createQuery(Order.class);
        Root<Order> root = query.from(Order.class);
        query.select(root);
        
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(root.get("status").in(OrderStatus.PAID, OrderStatus.COMPLETED, OrderStatus.REFUNDED));
        predicates.add(cb.isNotNull(root.get("paymentTime")));
        if (start != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("paymentTime"), start));
        }
        if (end != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("paymentTime"), end));
        }
        query.where(predicates.toArray(new Predicate[0]));

        List<Order> orders = entityManager.createQuery(query).getResultList();

        // 统计数据
        BigDecimal totalAmount = BigDecimal.ZERO;
        long successPayments = 0;
        long failedPayments = 0;
        long refundedPayments = 0;
        Map<String, BigDecimal> amountByMethod = new HashMap<>();
        Map<String, Long> countByMethod = new HashMap<>();

        for (Order order : orders) {
            totalAmount = totalAmount.add(order.getActualAmount());
            
            if (order.getStatus() == OrderStatus.REFUNDED) {
                refundedPayments++;
            } else {
                successPayments++;
            }

            String method = order.getPaymentMethod();
            if (method != null) {
                amountByMethod.merge(method, order.getActualAmount(), BigDecimal::add);
                countByMethod.merge(method, 1L, (a, b) -> a + b);
            }
        }

        BigDecimal averageAmount = orders.isEmpty() ? BigDecimal.ZERO :
                totalAmount.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

        return PaymentStatisticsDTO.builder()
                .totalAmount(totalAmount)
                .totalPayments((long) orders.size())
                .successPayments(successPayments)
                .failedPayments(failedPayments)
                .refundedPayments(refundedPayments)
                .amountByMethod(amountByMethod)
                .countByMethod(countByMethod)
                .averageAmount(averageAmount)
                .build();
    }

    /**
     * 构建查询条件
     */
    private Predicate[] buildPredicates(
        CriteriaBuilder cb,
        Root<Order> root,
        String keyword,
        String status,
        String paymentMethod,
        String startDate,
        String endDate
    ) {
        List<Predicate> predicates = new ArrayList<>();

        // 只查询已支付的订单
        if (status != null && !status.isEmpty()) {
            String[] statusArray = status.split(",");
            List<OrderStatus> statusList = new ArrayList<>();
            for (String s : statusArray) {
                try {
                    statusList.add(OrderStatus.valueOf(s.trim()));
                } catch (IllegalArgumentException e) {
                    log.warn("无效的订单状态: {}", s);
                }
            }
            if (!statusList.isEmpty()) {
                predicates.add(root.get("status").in(statusList));
            }
        } else {
            // 默认只查询已支付状态
            predicates.add(root.get("status").in(OrderStatus.PAID, OrderStatus.COMPLETED, OrderStatus.REFUNDED));
        }

        // 关键词搜索（订单号/用户名/商品名）
        if (keyword != null && !keyword.isEmpty()) {
            String likeKeyword = "%" + keyword + "%";
            predicates.add(cb.or(
                    cb.like(root.get("orderNo"), likeKeyword),
                    cb.like(root.get("buyer").get("username"), likeKeyword),
                    cb.like(root.get("seller").get("username"), likeKeyword),
                    cb.like(root.get("goods").get("title"), likeKeyword)
            ));
        }

        // 支付方式筛选
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            predicates.add(cb.equal(root.get("paymentMethod"), paymentMethod));
        }

        // 时间范围
        LocalDateTime start = parseDateTime(startDate, true);
        LocalDateTime end = parseDateTime(endDate, false);
        if (start != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("paymentTime"), start));
        }
        if (end != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("paymentTime"), end));
        }

        // 支付时间不为空
        predicates.add(cb.isNotNull(root.get("paymentTime")));

        return predicates.toArray(new Predicate[0]);
    }

    /**
     * 转换为 DTO
     */
    private PaymentRecordDTO convertToDTO(Order order) {
        return PaymentRecordDTO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .transactionId(order.getOrderNo()) // 订单号作为交易ID
                .amount(order.getActualAmount())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus().name())
                .paidAt(order.getPaymentTime())
                .createdAt(order.getCreatedAt())
                .goodsTitle(order.getGoods() != null ? order.getGoods().getTitle() : null)
                .buyerUsername(order.getBuyer() != null ? order.getBuyer().getUsername() : null)
                .sellerUsername(order.getSeller() != null ? order.getSeller().getUsername() : null)
                .build();
    }

    /**
     * 解析日期时间
     */
    private LocalDateTime parseDateTime(String dateStr, boolean isStart) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return isStart ? date.atStartOfDay() : date.atTime(LocalTime.MAX);
        } catch (Exception e) {
            log.warn("日期解析失败: {}", dateStr);
            return null;
        }
    }
}
