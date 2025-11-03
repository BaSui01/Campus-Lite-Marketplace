package com.campus.marketplace.controller;

import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.SearchService;
import com.campus.marketplace.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SearchControllerValidationTest {

    private MockMvc mockMvc;

    @Mock
    private SearchService searchService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new SearchController(searchService, userRepository))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("空关键词应返回400")
    void emptyQueryReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("type", "goods")
                        .param("keyword", ""))
                .andExpect(status().isBadRequest());
    }
}
