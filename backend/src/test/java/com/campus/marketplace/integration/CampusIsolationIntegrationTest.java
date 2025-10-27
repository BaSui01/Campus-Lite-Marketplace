package com.campus.marketplace.integration;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(SpringExtension.class)
class CampusIsolationIntegrationTest {

    @SuppressWarnings("resource")
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    @Autowired CategoryRepository categoryRepository;
    @Autowired GoodsRepository goodsRepository;
    @Autowired UserRepository userRepository;
    @Autowired OrderService orderService;

    @Test
    @DisplayName("跨校下单：无跨校权限被拒，拥有权限放行（管理员场景）")
    void crossCampusOrderIsolation() {
        Category cat = new Category();
        cat.setName("数码");
        categoryRepository.save(cat);

        User buyer = User.builder().username("buyer").password("p").email("buyer@c.edu").build();
        buyer.setCampusId(10L);
        userRepository.save(buyer);

        User seller = User.builder().username("seller").password("p").email("seller@c.edu").build();
        seller.setCampusId(20L);
        userRepository.save(seller);

        Goods goods = Goods.builder().title("Macbook Pro").description("九成新")
                .price(new BigDecimal("9999.00")).categoryId(cat.getId()).sellerId(seller.getId())
                .status(GoodsStatus.APPROVED).build();
        goods.setCampusId(20L);
        goodsRepository.save(goods);

        // 无跨校权限（学生）
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer", null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))
        );
        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(goods.getId(), null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.FORBIDDEN);

        // 赋予跨校权限（管理员）
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer", null, List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("system:campus:cross")
                ))
        );
        String orderNo = orderService.createOrder(new CreateOrderRequest(goods.getId(), null));
        assertThat(orderNo).isNotBlank();
    }
}
