package com.campus.marketplace.e2e;

import com.campus.marketplace.integration.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E 示例：搜索流程最小黑盒验证
 */
class SearchFlowIT extends IntegrationTestBase {

    @Test
    @DisplayName("搜索接口：非空关键词返回200（黑盒路径冒烟）")
    void searchWithQueryReturnsOk() throws Exception {
        mockMvc.perform(get("/api/search").param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
