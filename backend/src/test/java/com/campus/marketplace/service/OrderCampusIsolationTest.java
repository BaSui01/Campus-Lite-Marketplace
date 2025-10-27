package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单-校区隔离测试")
class OrderCampusIsolationTest {

    @Mock private OrderRepository orderRepository;
    @Mock private GoodsRepository goodsRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentService paymentService;
    @Mock private com.campus.marketplace.repository.ReviewRepository reviewRepository;
    @Mock private com.campus.marketplace.common.utils.SensitiveWordFilter sensitiveWordFilter;

    @InjectMocks private OrderServiceImpl orderService;

    private MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> securityUtilMock;

    private User buyer;
    private User seller;
    private Goods goods;

    @BeforeEach
    void init() {
        securityUtilMock = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        securityUtilMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenReturn("buyer");
        securityUtilMock.when(() -> com.campus.marketplace.common.utils.SecurityUtil.hasAuthority(anyString()))
                .thenReturn(false);
        securityUtilMock.when(() -> com.campus.marketplace.common.utils.SecurityUtil.hasRole(anyString()))
                .thenReturn(false);

        buyer = User.builder().username("buyer").email("buyer@campus.edu").status(UserStatus.ACTIVE).points(0).creditScore(100).build();
        buyer.setId(1L);
        buyer.setCampusId(10L);

        seller = User.builder().username("seller").email("seller@campus.edu").status(UserStatus.ACTIVE).points(0).creditScore(100).build();
        seller.setId(2L);
        seller.setCampusId(20L);

        goods = Goods.builder()
                .title("Macbook Pro")
                .price(new BigDecimal("9999.00"))
                .categoryId(1L)
                .sellerId(2L)
                .status(GoodsStatus.APPROVED)
                .images(new String[]{"a.jpg"})
                .build();
        goods.setId(100L);
        goods.setCampusId(20L);
    }

    @AfterEach
    void cleanup() {
        if (securityUtilMock != null) securityUtilMock.close();
    }

    @Test
    @DisplayName("跨校下单-无跨校权限应被拒绝")
    void createOrder_forbidden_whenCrossCampusWithoutAuthority() {
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(goodsRepository.findById(100L)).thenReturn(Optional.of(goods));

        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(100L, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN.getCode())
                .hasMessageContaining("跨校区购买被禁止");
    }

    @Test
    @DisplayName("跨校下单-拥有跨校权限可通过")
    void createOrder_success_whenCrossCampusWithAuthority() {
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(goodsRepository.findById(100L)).thenReturn(Optional.of(goods));
        when(orderRepository.existsByGoodsIdAndStatusNot(100L, OrderStatus.CANCELLED)).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(999L);
            return o;
        });
        when(goodsRepository.save(any(Goods.class))).thenAnswer(invocation -> invocation.getArgument(0));

        securityUtilMock.when(() -> com.campus.marketplace.common.utils.SecurityUtil.hasAuthority("system:campus:cross"))
                .thenReturn(true);

        String orderNo = orderService.createOrder(new CreateOrderRequest(100L, null));
        assertThat(orderNo).isNotBlank();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("非买家/卖家/管理员查询订单应被拒绝")
    void getOrderDetail_forbidden_whenNotOwnerOrAdmin() {
        // 当前用户换成 other
        securityUtilMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenReturn("other");
        User other = User.builder().username("other").email("o@campus.edu").status(UserStatus.ACTIVE).points(0).creditScore(100).build();
        other.setId(99L);
        other.setCampusId(10L);

        Order order = Order.builder()
                .orderNo("ORD123")
                .goodsId(100L)
                .buyerId(1L)
                .sellerId(2L)
                .status(OrderStatus.PAID)
                .build();
        order.setCampusId(10L);

        when(orderRepository.findByOrderNoWithDetails("ORD123")).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> orderService.getOrderDetail("ORD123"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN.getCode());
    }
}
