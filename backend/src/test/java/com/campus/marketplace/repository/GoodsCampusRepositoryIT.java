package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@Import({com.campus.marketplace.common.entity.BaseEntity.class})
class GoodsCampusRepositoryIT {

    @SuppressWarnings("resource")
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    @Autowired GoodsRepository goodsRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired UserRepository userRepository;

    @Test
    @DisplayName("按校区过滤物品")
    void filterGoodsByCampus() {
        // 基础数据
        Category cat = new Category();
        cat.setName("数码");
        categoryRepository.save(cat);

        User u1 = User.builder().username("u1").password("p").email("u1@campus.edu").build();
        u1.setCampusId(1L);
        userRepository.save(u1);
        User u2 = User.builder().username("u2").password("p").email("u2@campus.edu").build();
        u2.setCampusId(2L);
        userRepository.save(u2);

        Goods g1 = Goods.builder().title("A").description("d").price(new BigDecimal("1"))
                .categoryId(cat.getId()).sellerId(u1.getId()).status(GoodsStatus.APPROVED).build();
        g1.setCampusId(1L);
        goodsRepository.save(g1);
        Goods g2 = Goods.builder().title("B").description("d").price(new BigDecimal("2"))
                .categoryId(cat.getId()).sellerId(u2.getId()).status(GoodsStatus.APPROVED).build();
        g2.setCampusId(2L);
        goodsRepository.save(g2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Goods> page1 = goodsRepository.findByConditionsWithCampus(
                GoodsStatus.APPROVED, null, null, null, null, 1L, pageable);
        assertThat(page1.getContent()).extracting(Goods::getCampusId).containsOnly(1L);

        Page<Goods> page2 = goodsRepository.findByConditionsWithCampus(
                GoodsStatus.APPROVED, null, null, null, null, 2L, pageable);
        assertThat(page2.getContent()).extracting(Goods::getCampusId).containsOnly(2L);
    }
}
