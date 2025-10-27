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

/**
 * 订单服务单元测试
 * 
 * 测试订单创建、查询等功能
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务测试")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private GoodsRepository goodsRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private MockedStatic<com.campus.marketplace.common.utils.SecurityUtil> securityUtilMock;
    private User testBuyer;
    private User testSeller;
    private Goods testGoods;
    private CreateOrderRequest validRequest;

    @BeforeEach
    void setUp() {
        // Mock SecurityUtil 静态方法
        securityUtilMock = mockStatic(com.campus.marketplace.common.utils.SecurityUtil.class);
        securityUtilMock.when(com.campus.marketplace.common.utils.SecurityUtil::getCurrentUsername)
                .thenReturn("buyer");

        // 准备测试买家
        testBuyer = User.builder()
                .username("buyer")
                .email("buyer@campus.edu")
                .status(UserStatus.ACTIVE)
                .points(100)
                .build();
        testBuyer.setId(1L);

        // 准备测试卖家
        testSeller = User.builder()
                .username("seller")
                .email("seller@campus.edu")
                .status(UserStatus.ACTIVE)
                .points(200)
                .build();
        testSeller.setId(2L);

        // 准备测试物品
        testGoods = Goods.builder()
                .title("iPhone 13 Pro")
                .description("九成新，无划痕")
                .price(new BigDecimal("4999.00"))
                .categoryId(1L)
                .sellerId(2L)
                .status(GoodsStatus.APPROVED)
                .viewCount(10)
                .favoriteCount(5)
                .images(new String[]{"image1.jpg"})
                .build();
        testGoods.setId(1L);

        // 准备测试请求
        validRequest = new CreateOrderRequest(1L, null);
    }

    @AfterEach
    void tearDown() {
        // 关闭 SecurityUtil Mock
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("创建订单成功 - 所有条件满足")
    void createOrder_Success_WhenAllConditionsMet() {
        // Given
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        when(orderRepository.existsByGoodsIdAndStatusNot(1L, OrderStatus.CANCELLED))
                .thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(goodsRepository.save(any(Goods.class))).thenReturn(testGoods);

        // When
        String orderNo = orderService.createOrder(validRequest);

        // Then
        assertThat(orderNo).isNotNull();
        assertThat(orderNo).startsWith("ORD");
        
        verify(orderRepository).save(argThat(order ->
                order.getGoodsId().equals(1L) &&
                order.getBuyerId().equals(1L) &&
                order.getSellerId().equals(2L) &&
                order.getAmount().compareTo(new BigDecimal("4999.00")) == 0 &&
                order.getActualAmount().compareTo(new BigDecimal("4999.00")) == 0 &&
                order.getStatus() == OrderStatus.PENDING_PAYMENT
        ));
        
        verify(goodsRepository).save(argThat(goods ->
                goods.getStatus() == GoodsStatus.SOLD
        ));
    }

    @Test
    @DisplayName("创建订单失败 - 物品不存在")
    void createOrder_Fail_WhenGoodsNotFound() {
        // Given
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GOODS_NOT_FOUND.getCode());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("创建订单失败 - 物品未审核")
    void createOrder_Fail_WhenGoodsNotApproved() {
        // Given
        testGoods.setStatus(GoodsStatus.PENDING);
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GOODS_NOT_APPROVED.getCode());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("创建订单失败 - 物品已售出")
    void createOrder_Fail_WhenGoodsAlreadySold() {
        // Given
        testGoods.setStatus(GoodsStatus.SOLD);
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GOODS_ALREADY_SOLD.getCode());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("创建订单失败 - 不能购买自己的物品")
    void createOrder_Fail_WhenBuyOwnGoods() {
        // Given
        testGoods.setSellerId(1L);  // 卖家 ID 和买家 ID 相同
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CANNOT_BUY_OWN_GOODS.getCode());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("创建订单失败 - 物品已有未取消订单")
    void createOrder_Fail_WhenGoodsHasActiveOrder() {
        // Given
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        when(orderRepository.existsByGoodsIdAndStatusNot(1L, OrderStatus.CANCELLED))
                .thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.GOODS_ALREADY_SOLD.getCode());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("创建订单成功 - 验证订单号格式")
    void createOrder_Success_VerifyOrderNoFormat() {
        // Given
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        when(orderRepository.existsByGoodsIdAndStatusNot(1L, OrderStatus.CANCELLED))
                .thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(goodsRepository.save(any(Goods.class))).thenReturn(testGoods);

        // When
        String orderNo = orderService.createOrder(validRequest);

        // Then
        assertThat(orderNo).matches("ORD\\d{17}");  // ORD + 17位时间戳
    }

    @Test
    @DisplayName("创建订单成功 - 验证事务性")
    void createOrder_Success_VerifyTransactional() {
        // Given
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(testBuyer));
        when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGoods));
        when(orderRepository.existsByGoodsIdAndStatusNot(1L, OrderStatus.CANCELLED))
                .thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(goodsRepository.save(any(Goods.class))).thenReturn(testGoods);

        // When
        orderService.createOrder(validRequest);

        // Then - 验证订单保存和物品状态更新都被调用
        verify(orderRepository).save(any(Order.class));
        verify(goodsRepository).save(any(Goods.class));
    }
}
