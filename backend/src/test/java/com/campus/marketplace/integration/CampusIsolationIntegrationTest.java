package com.campus.marketplace.integration;

import com.campus.marketplace.common.dto.request.CreateOrderRequest;
import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.CampusRepository;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CampusIsolationIntegrationTest extends IntegrationTestBase {

    // 数据源/安全等环境由 IntegrationTestBase 统一提供

    @Autowired CampusRepository campusRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired GoodsRepository goodsRepository;
    @Autowired UserRepository userRepository;
    @Autowired OrderService orderService;

    @Test
    @DisplayName("跨校下单：无跨校权限被拒，拥有权限放行（管理员场景）")
    void crossCampusOrderIsolation() {
        // 创建两个校区（修复外键约束问题）
        Campus campusA = Campus.builder()
                .code("CAMPUS_A")
                .name("北京大学")
                .status(CampusStatus.ACTIVE)
                .build();
        campusRepository.save(campusA);

        Campus campusB = Campus.builder()
                .code("CAMPUS_B")
                .name("清华大学")
                .status(CampusStatus.ACTIVE)
                .build();
        campusRepository.save(campusB);

        Category cat = new Category();
        cat.setName("数码");
        categoryRepository.save(cat);

        User buyer = User.builder().username("buyer").password("p").email("buyer@c.edu").build();
        buyer.setCampusId(campusA.getId());
        userRepository.save(buyer);

        User seller = User.builder().username("seller").password("p").email("seller@c.edu").build();
        seller.setCampusId(campusB.getId());
        userRepository.save(seller);

        Goods goods = Goods.builder().title("Macbook Pro").description("九成新")
                .price(new BigDecimal("9999.00")).categoryId(cat.getId()).sellerId(seller.getId())
                .status(GoodsStatus.APPROVED).build();
        goods.setCampusId(campusB.getId());
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
