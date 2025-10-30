package com.campus.marketplace.service.perf;

import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfSeedServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private GoodsRepository goodsRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PerfSeedService perfSeedService;

    private User seller;
    private User buyer;
    private Category category;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .id(10L)
                .username("seller_north")
                .campusId(1L)
                .status(UserStatus.ACTIVE)
                .password("noop")
                .email("seller@example.com")
                .build();

        buyer = User.builder()
                .id(20L)
                .username("buyer_grad")
                .campusId(1L)
                .status(UserStatus.ACTIVE)
                .password("noop")
                .email("buyer@example.com")
                .build();

        category = Category.builder()
                .name("性能压测分类")
                .sortOrder(0)
                .build();
        category.setId(30L);
    }

    @Test
    @DisplayName("seedTimeoutOrders 应构造指定数量订单并更新超时标记")
    void shouldSeedTimeoutOrders() {
        when(userRepository.findByUsername("seller_north")).thenReturn(Optional.of(seller));
        when(userRepository.findByUsername("buyer_grad")).thenReturn(Optional.of(buyer));
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        when(jdbcTemplate.update(eq("UPDATE t_order SET created_at = ?, updated_at = ? WHERE id = ?"), any(), any(), any()))
                .thenReturn(1);

        doNothing().when(goodsRepository).flush();
        doNothing().when(orderRepository).flush();

        List<Goods> storedGoods = new CopyOnWriteArrayList<>();
        when(goodsRepository.saveAll(anyIterable())).thenAnswer(invocation -> {
            Iterable<Goods> incoming = invocation.getArgument(0);
            long id = 1;
            for (Goods goods : incoming) {
                goods.setId(id++);
                storedGoods.add(goods);
            }
            return storedGoods;
        });

        List<Order> storedOrders = new CopyOnWriteArrayList<>();
        when(orderRepository.saveAll(anyIterable())).thenAnswer(invocation -> {
            Iterable<Order> incoming = invocation.getArgument(0);
            long id = 100;
            for (Order order : incoming) {
                order.setId(id++);
                storedOrders.add(order);
            }
            return storedOrders;
        });

        int created = perfSeedService.seedTimeoutOrders(5);

        assertThat(created).isEqualTo(5);
        assertThat(storedGoods).hasSize(5);
        assertThat(storedOrders)
                .hasSize(5)
                .allSatisfy(order -> {
                    assertThat(order.getOrderNo()).startsWith("PERF-");
                    assertThat(order.getBuyerId()).isEqualTo(buyer.getId());
                    assertThat(order.getSellerId()).isEqualTo(seller.getId());
                    assertThat(order.getGoodsId()).isNotNull();
                    assertThat(order.getAmount()).isEqualByComparingTo(new BigDecimal("199.99"));
                });

        verify(goodsRepository).saveAll(anyIterable());
        verify(orderRepository).saveAll(anyIterable());
        verify(jdbcTemplate, times(5)).update(eq("UPDATE t_order SET created_at = ?, updated_at = ? WHERE id = ?"), any(), any(), any());
    }

    @Test
    @DisplayName("seedTimeoutOrders 为 0 时应直接返回")
    void shouldReturnImmediatelyWhenCountIsZero() {
        int created = perfSeedService.seedTimeoutOrders(0);

        assertThat(created).isZero();
        verifyNoInteractions(goodsRepository, orderRepository, jdbcTemplate);
    }
}
