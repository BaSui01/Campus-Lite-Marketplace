package com.campus.marketplace.integration;

import com.campus.marketplace.common.component.OrderAutomationScheduler;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 订单自动化集成测试
 * 
 * 测试订单自动化流程在真实数据库环境下的完整功能
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("订单自动化集成测试")
class OrderAutomationIntegrationTest {

    @Container
    @SuppressWarnings("resource") // Testcontainers 自动管理容器生命周期
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private OrderAutomationScheduler scheduler;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoodsRepository goodsRepository;

    private User buyer;
    private User seller;
    private Goods goods;

    @BeforeEach
    void setUp() {
        // 创建买家
        buyer = User.builder()
            .username("buyer_test")
            .password("password")
            .email("buyer@test.com")
            .phone("13800000001")
            .creditScore(100)
            .build();
        buyer = userRepository.save(buyer);

        // 创建卖家
        seller = User.builder()
            .username("seller_test")
            .password("password")
            .email("seller@test.com")
            .phone("13800000002")
            .creditScore(100)
            .build();
        seller = userRepository.save(seller);

        // 创建商品
        goods = Goods.builder()
            .title("测试商品")
            .description("测试描述")
            .price(BigDecimal.valueOf(100))
            .status(GoodsStatus.APPROVED)
            .sellerId(seller.getId())
            .images(new String[]{"image1.jpg"})
            .build();
        goods = goodsRepository.save(goods);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        goodsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("完整流程 - 自动确认收货应该将超过7天的已送达订单标记为已完成")
    void fullFlow_AutoConfirmReceiptShouldCompleteDeliveredOrdersAfter7Days() {
        // Arrange - 创建一个7天前的已送达订单
        Order order = Order.builder()
            .orderNo("ORD_AUTO_CONFIRM_001")
            .goodsId(goods.getId())
            .buyerId(buyer.getId())
            .sellerId(seller.getId())
            .amount(BigDecimal.valueOf(100))
            .actualAmount(BigDecimal.valueOf(100))
            .discountAmount(BigDecimal.ZERO)
            .status(OrderStatus.DELIVERED)
            .build();
        order = orderRepository.save(order);

        // 手动设置更新时间为7天前
        order.setUpdatedAt(LocalDateTime.now().minusDays(7).minusHours(1));
        order = orderRepository.save(order);

        Long orderId = order.getId();

        // Act - 执行自动确认收货任务
        scheduler.autoConfirmReceiptJob();

        // Assert - 验证订单状态已更新为已完成
        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("完整流程 - 自动确认收货不应该处理7天内的已送达订单")
    void fullFlow_AutoConfirmReceiptShouldNotCompleteRecentDeliveredOrders() {
        // Arrange - 创建一个最近的已送达订单
        Order order = Order.builder()
            .orderNo("ORD_AUTO_CONFIRM_002")
            .goodsId(goods.getId())
            .buyerId(buyer.getId())
            .sellerId(seller.getId())
            .amount(BigDecimal.valueOf(100))
            .actualAmount(BigDecimal.valueOf(100))
            .discountAmount(BigDecimal.ZERO)
            .status(OrderStatus.DELIVERED)
            .build();
        order = orderRepository.save(order);

        // 手动设置更新时间为6天前（未超过7天）
        order.setUpdatedAt(LocalDateTime.now().minusDays(6));
        order = orderRepository.save(order);

        Long orderId = order.getId();

        // Act - 执行自动确认收货任务
        scheduler.autoConfirmReceiptJob();

        // Assert - 验证订单状态仍为已送达
        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("完整流程 - 异常订单检测应该检测已支付但未发货的订单")
    void fullFlow_AbnormalDetectionShouldDetectPaidButNotShipped() {
        // Arrange - 创建一个3天前支付的订单
        Order order = Order.builder()
            .orderNo("ORD_ABNORMAL_001")
            .goodsId(goods.getId())
            .buyerId(buyer.getId())
            .sellerId(seller.getId())
            .amount(BigDecimal.valueOf(100))
            .actualAmount(BigDecimal.valueOf(100))
            .discountAmount(BigDecimal.ZERO)
            .status(OrderStatus.PAID)
            .paymentTime(LocalDateTime.now().minusDays(3).minusHours(1))
            .build();
        order = orderRepository.save(order);

        // 手动设置更新时间为3天前
        order.setUpdatedAt(LocalDateTime.now().minusDays(3).minusHours(1));
        order = orderRepository.save(order);

        Long orderId = order.getId();

        // Act - 执行异常订单检测任务
        scheduler.detectAbnormalOrdersJob();

        // Assert - 验证订单状态仍为已支付（只发送通知，不改变状态）
        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("完整流程 - 异常订单检测应该检测已发货但未送达的订单")
    void fullFlow_AbnormalDetectionShouldDetectShippedButNotDelivered() {
        // Arrange - 创建一个7天前发货的订单
        Order order = Order.builder()
            .orderNo("ORD_ABNORMAL_002")
            .goodsId(goods.getId())
            .buyerId(buyer.getId())
            .sellerId(seller.getId())
            .amount(BigDecimal.valueOf(100))
            .actualAmount(BigDecimal.valueOf(100))
            .discountAmount(BigDecimal.ZERO)
            .status(OrderStatus.SHIPPED)
            .build();
        order = orderRepository.save(order);

        // 手动设置更新时间为7天前
        order.setUpdatedAt(LocalDateTime.now().minusDays(7).minusHours(1));
        order = orderRepository.save(order);

        Long orderId = order.getId();

        // Act - 执行异常订单检测任务
        scheduler.detectAbnormalOrdersJob();

        // Assert - 验证订单状态仍为已发货（只发送通知，不改变状态）
        Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("完整流程 - 批量处理多个超时订单")
    void fullFlow_BatchProcessMultipleTimeoutOrders() {
        // Arrange - 创建多个7天前的已送达订单
        for (int i = 0; i < 5; i++) {
            Order order = Order.builder()
                .orderNo("ORD_BATCH_" + i)
                .goodsId(goods.getId())
                .buyerId(buyer.getId())
                .sellerId(seller.getId())
                .amount(BigDecimal.valueOf(100))
                .actualAmount(BigDecimal.valueOf(100))
                .discountAmount(BigDecimal.ZERO)
                .status(OrderStatus.DELIVERED)
                .build();
            order = orderRepository.save(order);

            // 手动设置更新时间为7天前
            order.setUpdatedAt(LocalDateTime.now().minusDays(7).minusHours(1));
            orderRepository.save(order);
        }

        // Act - 执行自动确认收货任务
        scheduler.autoConfirmReceiptJob();

        // Assert - 验证所有订单状态都已更新为已完成
        List<Order> completedOrders = orderRepository.findAll();
        assertThat(completedOrders)
            .hasSize(5)
            .allMatch(order -> order.getStatus() == OrderStatus.COMPLETED);
    }
}
