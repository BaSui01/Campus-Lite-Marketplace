package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.GoodsStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.campus.marketplace.integration.IntegrationTestBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class GoodsCampusRepositoryIT extends IntegrationTestBase {

    @Autowired GoodsRepository goodsRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired UserRepository userRepository;
    @Autowired CampusRepository campusRepository;

    @Test
    @DisplayName("按校区过滤物品")
    void filterGoodsByCampus() {
        // 基础数据
        Category cat = new Category();
        cat.setName("数码");
        cat.setCreatedAt(java.time.LocalDateTime.now());
        cat.setUpdatedAt(java.time.LocalDateTime.now());
        categoryRepository.save(cat);

        com.campus.marketplace.common.entity.Campus c1 = com.campus.marketplace.common.entity.Campus.builder()
                .code("C1").name("校园1").build();
        c1.setCreatedAt(java.time.LocalDateTime.now());
        c1.setUpdatedAt(java.time.LocalDateTime.now());
        campusRepository.save(c1);

        com.campus.marketplace.common.entity.Campus c2 = com.campus.marketplace.common.entity.Campus.builder()
                .code("C2").name("校园2").build();
        c2.setCreatedAt(java.time.LocalDateTime.now());
        c2.setUpdatedAt(java.time.LocalDateTime.now());
        campusRepository.save(c2);

        User u1 = User.builder().username("u1").password("p").email("u1@campus.edu").build();
        u1.setCampusId(c1.getId());
        u1.setCreatedAt(java.time.LocalDateTime.now());
        u1.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(u1);
        User u2 = User.builder().username("u2").password("p").email("u2@campus.edu").build();
        u2.setCampusId(c2.getId());
        u2.setCreatedAt(java.time.LocalDateTime.now());
        u2.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(u2);

        Goods g1 = Goods.builder().title("A").description("d").price(new BigDecimal("1"))
                .categoryId(cat.getId()).sellerId(u1.getId()).status(GoodsStatus.APPROVED).build();
        g1.setCampusId(1L);
        g1.setCreatedAt(java.time.LocalDateTime.now());
        g1.setUpdatedAt(java.time.LocalDateTime.now());
        goodsRepository.save(g1);
        Goods g2 = Goods.builder().title("B").description("d").price(new BigDecimal("2"))
                .categoryId(cat.getId()).sellerId(u2.getId()).status(GoodsStatus.APPROVED).build();
        g2.setCampusId(2L);
        g2.setCreatedAt(java.time.LocalDateTime.now());
        g2.setUpdatedAt(java.time.LocalDateTime.now());
        goodsRepository.save(g2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Goods> page1 = goodsRepository.findByConditionsWithCampus(
                GoodsStatus.APPROVED, null, null, null, null, 1L, pageable);
        assertThat(page1.getContent()).extracting(Goods::getCampusId).containsOnly(c1.getId());

        Page<Goods> page2 = goodsRepository.findByConditionsWithCampus(
                GoodsStatus.APPROVED, null, null, null, null, 2L, pageable);
        assertThat(page2.getContent()).extracting(Goods::getCampusId).containsOnly(c2.getId());
    }
}
