package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.dto.request.ReviewOrderRequest;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.Review;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.ReviewRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务测试")
class OrderServiceTest {

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
    private com.campus.marketplace.common.utils.SensitiveWordFilter sensitiveWordFilter;

    @InjectMocks
    private OrderServiceImpl orderService;

    private MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> securityUtilMock;
    private User testBuyer;
    private User testSeller;
    private Goods testGoods;

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        securityUtilMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenReturn("buyer");

        testBuyer = User.builder()
                .username("buyer")
                .email("buyer@campus.edu")
                .status(UserStatus.ACTIVE)
                .points(100)
                .creditScore(100)
                .build();
        testBuyer.setId(1L);

        testSeller = User.builder()
                .username("seller")
                .email("seller@campus.edu")
                .status(UserStatus.ACTIVE)
                .points(200)
                .creditScore(100)
                .build();
        testSeller.setId(2L);

        testGoods = Goods.builder()
                .title("iPhone 13 Pro")
                .price(new BigDecimal("4999.00"))
                .categoryId(1L)
                .sellerId(2L)
                .status(GoodsStatus.APPROVED)
                .images(new String[]{"image1.jpg"})
                .build();
        testGoods.setId(1L);
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("创建订单成功")
    void createOrder_Success() {
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        when(orderRepository.existsByGoodsIdAndStatusNot(1L, OrderStatus.CANCELLED)).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(goodsRepository.save(any(Goods.class))).thenReturn(testGoods);

        String orderNo = orderService.createOrder(new CreateOrderRequest(1L, null));

        assertThat(orderNo).isNotNull().startsWith("ORD");
        verify(goodsRepository).save(argThat(goods -> goods.getStatus() == GoodsStatus.SOLD));
    }

    @Test
    @DisplayName("创建订单失败 - 物品已售出")
    void createOrder_Fail_GoodsSold() {
        // Arrange - 准备测试数据
        testGoods.setStatus(GoodsStatus.SOLD);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));

        // Act & Assert - 执行并断言
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(1L, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GOODS_ALREADY_SOLD.getCode())
                .hasMessageContaining("已售出");
    }

    @Test
    @DisplayName("创建订单失败 - 物品未审核")
    void createOrder_Fail_GoodsNotApproved() {
        // Arrange
        testGoods.setStatus(GoodsStatus.PENDING);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(1L, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GOODS_NOT_APPROVED.getCode())
                .hasMessageContaining("未审核");
    }

    @Test
    @DisplayName("创建订单失败 - 不能购买自己的物品")
    void createOrder_Fail_CannotBuyOwnGoods() {
        // Arrange - 买家和卖家是同一人
        testGoods.setSellerId(1L);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(1L, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CANNOT_BUY_OWN_GOODS.getCode())
                .hasMessageContaining("不能购买自己的物品");
    }

    @Test
    @DisplayName("创建订单失败 - 物品不存在")
    void createOrder_Fail_GoodsNotFound() {
        // Arrange
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(999L, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GOODS_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("创建订单失败 - 物品已有未取消订单")
    void createOrder_Fail_GoodsHasActiveOrder() {
        // Arrange
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        when(orderRepository.existsByGoodsIdAndStatusNot(1L, OrderStatus.CANCELLED)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(1L, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GOODS_ALREADY_SOLD.getCode());
    }

    @Test
    @DisplayName("评价订单成功")
    void reviewOrder_Success() {
        Order order = Order.builder()
                .orderNo("ORD20251027110510358")
                .buyerId(1L)
                .sellerId(2L)
                .status(OrderStatus.COMPLETED)
                .build();
        order.setId(1L);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(orderRepository.findByOrderNo("ORD20251027110510358")).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrderId(1L)).thenReturn(false);
        when(sensitiveWordFilter.filter(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testSeller));
        when(userRepository.save(any(User.class))).thenReturn(testSeller);

        ReviewOrderRequest request = new ReviewOrderRequest("ORD20251027110510358", 5, "非常好的卖家");
        orderService.reviewOrder(request);

        verify(reviewRepository).save(argThat(review ->
                review.getOrderId().equals(1L) &&
                review.getBuyerId().equals(1L) &&
                review.getSellerId().equals(2L) &&
                review.getRating().equals(5)
        ));
        verify(userRepository).save(argThat(user ->
                user.getId().equals(2L) &&
                user.getCreditScore() > 100
        ));
    }

    @Test
    @DisplayName("评价订单失败 - 订单未完成")
    void reviewOrder_Fail_OrderNotCompleted() {
        Order order = Order.builder()
                .orderNo("ORD20251027110510358")
                .buyerId(1L)
                .status(OrderStatus.PAID)
                .build();

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(orderRepository.findByOrderNo("ORD20251027110510358")).thenReturn(Optional.of(order));

        ReviewOrderRequest request = new ReviewOrderRequest("ORD20251027110510358", 5, "非常好");

        assertThatThrownBy(() -> orderService.reviewOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode());
    }

    @Test
    @DisplayName("评价订单失败 - 已评价")
    void reviewOrder_Fail_AlreadyReviewed() {
        Order order = Order.builder()
                .orderNo("ORD20251027110510358")
                .buyerId(1L)
                .status(OrderStatus.COMPLETED)
                .build();
        order.setId(1L);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(orderRepository.findByOrderNo("ORD20251027110510358")).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrderId(1L)).thenReturn(true);

        ReviewOrderRequest request = new ReviewOrderRequest("ORD20251027110510358", 5, "非常好");

        assertThatThrownBy(() -> orderService.reviewOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.OPERATION_FAILED.getCode());
    }

    @Test
    @DisplayName("评价订单失败 - 不是买家")
    void reviewOrder_Fail_NotBuyer() {
        Order order = Order.builder()
                .orderNo("ORD20251027110510358")
                .buyerId(999L)
                .status(OrderStatus.COMPLETED)
                .build();

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(orderRepository.findByOrderNo("ORD20251027110510358")).thenReturn(Optional.of(order));

        ReviewOrderRequest request = new ReviewOrderRequest("ORD20251027110510358", 5, "非常好");

        assertThatThrownBy(() -> orderService.reviewOrder(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.PERMISSION_DENIED.getCode());
    }

    @Test
    @DisplayName("取消超时订单成功")
    void cancelTimeoutOrders_Success() {
        // Arrange - 创建30分钟前的超时订单
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(31);
        Order timeoutOrder = Order.builder()
                .orderNo("ORD20251027100000001")
                .goodsId(1L)
                .buyerId(1L)
                .sellerId(2L)
                .amount(new BigDecimal("4999.00"))
                .actualAmount(new BigDecimal("4999.00"))
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        timeoutOrder.setId(1L);
        timeoutOrder.setCreatedAt(timeoutTime);

        testGoods.setStatus(GoodsStatus.SOLD);

        when(orderRepository.findTimeoutOrders(
                eq(OrderStatus.PENDING_PAYMENT),
                any(LocalDateTime.class)
        )).thenReturn(List.of(timeoutOrder));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        when(orderRepository.save(any(Order.class))).thenReturn(timeoutOrder);
        when(goodsRepository.save(any(Goods.class))).thenReturn(testGoods);

        // Act - 执行取消超时订单
        int cancelledCount = orderService.cancelTimeoutOrders();

        // Assert - 验证结果
        assertThat(cancelledCount).isEqualTo(1);
        verify(orderRepository).save(argThat(order ->
                order.getStatus() == OrderStatus.CANCELLED
        ));
        verify(goodsRepository).save(argThat(goods ->
                goods.getStatus() == GoodsStatus.APPROVED
        ));
    }

    @Test
    @DisplayName("取消超时订单 - 无超时订单")
    void cancelTimeoutOrders_NoTimeoutOrders() {
        // Arrange
        when(orderRepository.findTimeoutOrders(
                eq(OrderStatus.PENDING_PAYMENT),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        // Act
        int cancelledCount = orderService.cancelTimeoutOrders();

        // Assert
        assertThat(cancelledCount).isZero();
        verify(orderRepository, never()).save(any(Order.class));
        verify(goodsRepository, never()).save(any(Goods.class));
    }

    @Test
    @DisplayName("取消超时订单 - 部分订单取消失败")
    void cancelTimeoutOrders_PartialFailure() {
        // Arrange - 创建两个超时订单
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(31);
        Order order1 = Order.builder()
                .orderNo("ORD20251027100000001")
                .goodsId(1L)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        order1.setCreatedAt(timeoutTime);

        Order order2 = Order.builder()
                .orderNo("ORD20251027100000002")
                .goodsId(999L) // 不存在的物品
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        order2.setCreatedAt(timeoutTime);

        when(orderRepository.findTimeoutOrders(
                eq(OrderStatus.PENDING_PAYMENT),
                any(LocalDateTime.class)
        )).thenReturn(List.of(order1, order2));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        when(goodsRepository.findById(999L)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        int cancelledCount = orderService.cancelTimeoutOrders();

        // Assert - 两个订单都应该被取消（即使物品不存在）
        assertThat(cancelledCount).isEqualTo(2);
        verify(orderRepository, times(2)).save(any(Order.class));
    }
}
