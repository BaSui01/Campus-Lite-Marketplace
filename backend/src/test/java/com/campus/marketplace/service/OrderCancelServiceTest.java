package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.repository.CouponUserRelationRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单取消测试")
class OrderCancelServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private GoodsRepository goodsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private com.campus.marketplace.repository.ReviewRepository reviewRepository;
    @Mock
    private com.campus.marketplace.common.utils.SensitiveWordFilter sensitiveWordFilter;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private CouponUserRelationRepository couponUserRelationRepository;
    @Mock
    private CouponService couponService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> secMock;

    private User buyer;
    private Goods goods;
    private Order order;

    @BeforeEach
    void setup() {
        secMock = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        secMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenReturn("buyer");

        buyer = User.builder().username("buyer").email("b@e.com").status(UserStatus.ACTIVE).build();
        buyer.setId(1L);
        goods = Goods.builder().title("X").sellerId(2L).status(GoodsStatus.SOLD).categoryId(1L).price(java.math.BigDecimal.TEN).build();
        goods.setId(10L);
        order = Order.builder().orderNo("ORD1").goodsId(10L).buyerId(1L).sellerId(2L).status(OrderStatus.PENDING_PAYMENT).build();
        order.setId(100L);
    }

    @AfterEach
    void tearDown() {
        if (secMock != null) secMock.close();
    }

    @Test
    @DisplayName("买家取消未支付订单成功，商品恢复")
    void cancel_success() {
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findByOrderNo("ORD1")).thenReturn(Optional.of(order));
        when(goodsRepository.findById(10L)).thenReturn(Optional.of(goods));

        orderService.cancelOrder("ORD1");

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CANCELLED));
        verify(goodsRepository).save(argThat(g -> g.getStatus() == GoodsStatus.APPROVED));
        verify(notificationService, times(2)).sendNotification(anyLong(), any(), anyString(), anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("非待支付订单取消失败")
    void cancel_invalid_status() {
        order.setStatus(OrderStatus.PAID);
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findByOrderNo("ORD1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder("ORD1"))
                .isInstanceOf(BusinessException.class);
    }
}
