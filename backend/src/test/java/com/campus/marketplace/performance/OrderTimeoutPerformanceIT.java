package com.campus.marketplace.performance;

import com.campus.marketplace.common.aspect.PerformanceMonitorAspect;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.repository.CouponUserRelationRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.AuditLogService;
import com.campus.marketplace.service.CouponService;
import com.campus.marketplace.service.NotificationService;
import com.campus.marketplace.service.OrderService;
import com.campus.marketplace.service.PaymentService;
import com.campus.marketplace.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("性能与稳定性验证：超时订单取消性能测试")
class OrderTimeoutPerformanceIT {

    private static final int LOAD_SIZE = 200;
    private static final long LATENCY_THRESHOLD_MS = 1200L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GoodsRepository goodsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private com.campus.marketplace.common.component.NotificationDispatcher notificationDispatcher;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CouponUserRelationRepository couponUserRelationRepository;

    @Mock
    private CouponService couponService;

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    private OrderService orderService;
    private PerformanceMonitorAspect performanceMonitorAspect;

    @BeforeEach
    void setUp() {
        com.campus.marketplace.common.utils.SensitiveWordFilter sensitiveWordFilter = new com.campus.marketplace.common.utils.SensitiveWordFilter();
        sensitiveWordFilter.init();

        orderServiceImpl = new OrderServiceImpl(
                orderRepository,
                goodsRepository,
                userRepository,
                paymentService,
                reviewRepository,
                sensitiveWordFilter,
                notificationService,
                notificationDispatcher,
                auditLogService,
                couponUserRelationRepository,
                couponService
        );

        performanceMonitorAspect = new PerformanceMonitorAspect();
        AspectJProxyFactory factory = new AspectJProxyFactory(orderServiceImpl);
        factory.addAspect(performanceMonitorAspect);
        orderService = factory.getProxy();
    }

    @Test
    @DisplayName("cancelTimeoutOrders 应在阈值内处理大量待支付订单并记录性能指标")
    void cancelTimeoutOrdersShouldMeetLatencySlo() {
        Map<Long, Goods> goodsStore = new HashMap<>();
        List<Order> timeoutOrders = new ArrayList<>(LOAD_SIZE);

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(45);

        for (int i = 0; i < LOAD_SIZE; i++) {
            long goodsId = i + 1L;
            Goods goods = Goods.builder()
                    .title("性能物品" + i)
                    .description("性能测试物品描述" + i)
                    .price(new BigDecimal("99.99"))
                    .sellerId(2000L + i)
                    .categoryId(100L)
                    .status(GoodsStatus.SOLD)
                    .build();
            goods.setId(goodsId);
            goodsStore.put(goodsId, goods);

            Order order = Order.builder()
                    .orderNo("ORD-PERF-" + UUID.randomUUID())
                    .goodsId(goodsId)
                    .buyerId(3000L + i)
                    .sellerId(2000L + i)
                    .campusId(1L)
                    .amount(new BigDecimal("99.99"))
                    .actualAmount(new BigDecimal("99.99"))
                    .status(OrderStatus.PENDING_PAYMENT)
                    .build();
            order.setId(5000L + i);
            order.setCreatedAt(timeoutThreshold.minusMinutes(1));
            timeoutOrders.add(order);
        }

        when(orderRepository.findTimeoutOrders(any(OrderStatus.class), any(LocalDateTime.class)))
                .thenReturn(timeoutOrders);

        when(goodsRepository.findById(anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(goodsStore.get(invocation.getArgument(0))));

        when(goodsRepository.save(any(Goods.class)))
                .thenAnswer(invocation -> {
                    Goods g = invocation.getArgument(0);
                    goodsStore.put(g.getId(), g);
                    return g;
                });

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        long start = System.nanoTime();
        int cancelled = orderService.cancelTimeoutOrders();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertThat(cancelled).isEqualTo(LOAD_SIZE);
        assertThat(durationMs).isLessThanOrEqualTo(LATENCY_THRESHOLD_MS);

        assertThat(timeoutOrders)
                .hasSize(LOAD_SIZE)
                .allMatch(o -> o.getStatus() == OrderStatus.CANCELLED);

        assertThat(goodsStore.values())
                .hasSize(LOAD_SIZE)
                .allMatch(g -> g.getStatus() == GoodsStatus.APPROVED);

        Map<String, Map<String, Object>> stats = performanceMonitorAspect.getPerformanceStats();
        String signature = stats.keySet().stream()
                .filter(key -> key.endsWith("cancelTimeoutOrders()"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未找到 cancelTimeoutOrders 的性能统计"));

        Map<String, Object> methodStats = stats.get(signature);
        assertThat(methodStats)
                .containsKeys("totalCalls", "totalErrors", "totalExecutionTime", "avgExecutionTime", "maxExecutionTime", "slowCount");

        assertThat(((Number) methodStats.get("totalCalls")).longValue()).isGreaterThanOrEqualTo(1);
        assertThat(((Number) methodStats.get("totalErrors")).longValue()).isZero();
        assertThat(((Number) methodStats.get("maxExecutionTime")).longValue()).isLessThanOrEqualTo(LATENCY_THRESHOLD_MS);
        assertThat(((Number) methodStats.get("slowCount")).longValue()).isZero();
    }
}
