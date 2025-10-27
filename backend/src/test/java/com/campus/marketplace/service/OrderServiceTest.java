package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.enums.UserStatus;
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

import java.math.BigDecimal;
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

    @InjectMocks
    private OrderServiceImpl orderService;

    private MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> securityUtilMock;
    private User testBuyer;
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
                .build();
        testBuyer.setId(1L);

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
}
