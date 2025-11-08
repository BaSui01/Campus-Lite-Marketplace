package com.campus.marketplace.controller;

import com.campus.marketplace.common.config.JwtAuthenticationFilter;
import com.campus.marketplace.common.dto.response.SearchResultItem;
import com.campus.marketplace.service.SearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SearchController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(TestSecurityConfig.class)
@DisplayName("SearchController MockMvc 集成测试")
class SearchControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @MockBean
    private com.campus.marketplace.repository.UserRepository userRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // Mock User
        com.campus.marketplace.common.entity.User mockUser = new com.campus.marketplace.common.entity.User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername(anyString())).thenReturn(java.util.Optional.of(mockUser));
    }

    @Test
    @DisplayName("GET /api/search -> 返回分页搜索结果")
    void search_returnsPagedResult() throws Exception {
        SearchResultItem item = SearchResultItem.builder()
                .type("GOODS")
                .id(99L)
                .title("AirPods Pro 2")
                .snippet("Apple <em>耳机</em> 降噪神器")
                .price(new BigDecimal("1999"))
                .campusId(3L)
                .build();
        when(searchService.search(eq("goods"), eq("耳机"), eq(0), eq(10), eq(List.of(1L))))
                .thenReturn(new PageImpl<>(List.of(item)));

        mockMvc.perform(get("/search")
                        .param("type", "goods")
                        .param("keyword", "耳机")
                        .param("tagIds", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].title").value("AirPods Pro 2"))
                .andExpect(jsonPath("$.data.content[0].snippet").value("Apple <em>耳机</em> 降噪神器"));
    }

    @Test
    @DisplayName("GET /api/search -> 关键词为空返回400")
    void search_withBlankKeyword_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/search")
                        .param("type", "goods")
                        .param("keyword", "  "))
                .andExpect(status().isBadRequest());
    }
}
