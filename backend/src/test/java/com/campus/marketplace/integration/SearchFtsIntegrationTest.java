package com.campus.marketplace.integration;

import com.campus.marketplace.common.dto.response.SearchResultItem;
import com.campus.marketplace.common.entity.Campus;
import com.campus.marketplace.common.entity.Category;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.CampusStatus;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.repository.CampusRepository;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.SearchService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(SpringExtension.class)
class SearchFtsIntegrationTest {

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

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired CampusRepository campusRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired GoodsRepository goodsRepository;
    @Autowired UserRepository userRepository;
    @Autowired SearchService searchService;
    @Autowired MockMvc mockMvc;

    @BeforeEach
    void setupFts() {
        // 建立 FTS 配置与触发器（支持无 pg_jieba 环境）
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pg_jieba");
            jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_ts_config WHERE cfgname='chinese') THEN CREATE TEXT SEARCH CONFIGURATION chinese (PARSER = jieba); END IF; END $$;");
        } catch (Exception ignored) {
            jdbcTemplate.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_ts_config WHERE cfgname='chinese') THEN CREATE TEXT SEARCH CONFIGURATION chinese (COPY = simple); END IF; END $$;");
        }
        // goods
        jdbcTemplate.execute("ALTER TABLE t_goods ADD COLUMN IF NOT EXISTS search_vector tsvector");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_goods_search_vector ON t_goods USING GIN (search_vector)");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION goods_search_vector_update() RETURNS trigger AS $$ BEGIN NEW.search_vector := setweight(to_tsvector('chinese', coalesce(NEW.title,'')),'A') || setweight(to_tsvector('chinese', coalesce(NEW.description,'')),'B'); RETURN NEW; END; $$ LANGUAGE plpgsql");
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trig_goods_search_vector_update ON t_goods");
        jdbcTemplate.execute("CREATE TRIGGER trig_goods_search_vector_update BEFORE INSERT OR UPDATE OF title,description ON t_goods FOR EACH ROW EXECUTE FUNCTION goods_search_vector_update()");
        // post
        jdbcTemplate.execute("ALTER TABLE t_post ADD COLUMN IF NOT EXISTS search_vector tsvector");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_post_search_vector ON t_post USING GIN (search_vector)");
        jdbcTemplate.execute("CREATE OR REPLACE FUNCTION post_search_vector_update() RETURNS trigger AS $$ BEGIN NEW.search_vector := setweight(to_tsvector('chinese', coalesce(NEW.title,'')),'A') || setweight(to_tsvector('chinese', coalesce(NEW.content,'')),'B'); RETURN NEW; END; $$ LANGUAGE plpgsql");
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trig_post_search_vector_update ON t_post");
        jdbcTemplate.execute("CREATE TRIGGER trig_post_search_vector_update BEFORE INSERT OR UPDATE OF title,content ON t_post FOR EACH ROW EXECUTE FUNCTION post_search_vector_update()");
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("FTS：高亮与排序，以及校区过滤")
    void fts_highlight_rank_and_campusFilter() {
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

        User s1 = User.builder().username("s1").password("p").email("s1@c.edu").build();
        s1.setCampusId(campusA.getId());
        userRepository.save(s1);
        User s2 = User.builder().username("s2").password("p").email("s2@c.edu").build();
        s2.setCampusId(campusB.getId());
        userRepository.save(s2);

        Goods g1 = Goods.builder().title("二手自行车").description("很好很好的自行车 自行车").price(new BigDecimal("100"))
                .categoryId(cat.getId()).sellerId(s1.getId()).status(GoodsStatus.APPROVED).build();
        g1.setCampusId(campusA.getId());
        goodsRepository.save(g1);

        Goods g2 = Goods.builder().title("低价转让").description("自行车一辆").price(new BigDecimal("90"))
                .categoryId(cat.getId()).sellerId(s2.getId()).status(GoodsStatus.APPROVED).build();
        g2.setCampusId(campusB.getId());
        goodsRepository.save(g2);

        // 手动触发 FTS 向量更新（确保触发器生效）
        goodsRepository.flush();
        jdbcTemplate.execute("UPDATE t_goods SET search_vector = setweight(to_tsvector('chinese', coalesce(title,'')),'A') || setweight(to_tsvector('chinese', coalesce(description,'')),'B') WHERE id IN (" + g1.getId() + "," + g2.getId() + ")");

        Page<SearchResultItem> all = searchService.search("goods", "自行车", 0, 10);
        assertThat(all.getTotalElements()).isEqualTo(2);
        assertThat(all.getContent().get(0).getId()).isEqualTo(g1.getId());
        assertThat(all.getContent().get(0).getSnippet()).contains("<em>");

        // 设置普通用户（无跨校权限），仅能看到本校
        var auth = new UsernamePasswordAuthenticationToken("u10", null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        User u10 = User.builder().username("u10").password("p").email("u10@c.edu").build();
        u10.setCampusId(campusA.getId());
        userRepository.save(u10);

        Page<SearchResultItem> onlyCampus = searchService.search("goods", "自行车", 0, 10);
        assertThat(onlyCampus.getTotalElements()).isEqualTo(1);
        assertThat(onlyCampus.getContent().get(0).getCampusId()).isEqualTo(campusA.getId());
    }

    @Test
    @DisplayName("空查询400（Controller层集成）")
    void emptyQueryBadRequest() throws Exception {
        mockMvc.perform(get("/api/search").param("q", ""))
                .andExpect(status().isBadRequest());
    }
}
