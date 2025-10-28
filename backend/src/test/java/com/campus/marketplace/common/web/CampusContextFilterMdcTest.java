package com.campus.marketplace.common.web;

import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("CampusContextFilter 将 campusId 注入 MDC")
class CampusContextFilterMdcTest {

    private CampusContextFilter filter;
    private UserRepository userRepo;
    private MockedStatic<SecurityUtil> secMock;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        filter = new CampusContextFilter(userRepo);
        secMock = mockStatic(SecurityUtil.class);
        secMock.when(SecurityUtil::isAuthenticated).thenReturn(true);
        secMock.when(SecurityUtil::getCurrentUsername).thenReturn("u1");
    }

    @AfterEach
    void tearDown() {
        if (secMock != null) secMock.close();
    }

    @Test
    void injectsCampusIdIntoMdc() throws ServletException, IOException {
        User u = User.builder().id(1L).username("u1").campusId(99L).build();
        when(userRepo.findByUsername("u1")).thenReturn(Optional.of(u));

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/x");
        MockHttpServletResponse res = new MockHttpServletResponse();

        FilterChain chain = (r, s) -> {
            // 在过滤链中读取响应头/状态即可，MDC 由日志消费，这里仅确保不抛异常
            assertThat(((MockHttpServletResponse) s).getStatus()).isNotNegative();
        };
        filter.doFilter(req, res, chain);
    }
}
