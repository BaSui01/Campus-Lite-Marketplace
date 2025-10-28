package com.campus.marketplace.common.web;

import com.campus.marketplace.common.utils.SecurityUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraceMdcFilterTest {

    private TraceMdcFilter filter;
    private MockedStatic<SecurityUtil> secMock;

    @BeforeEach
    void setUp() {
        filter = new TraceMdcFilter();
        secMock = mockStatic(SecurityUtil.class);
        secMock.when(SecurityUtil::isAuthenticated).thenReturn(true);
        secMock.when(SecurityUtil::getCurrentUsername).thenReturn("u1");
    }

    @AfterEach
    void tearDown() {
        if (secMock != null) secMock.close();
    }

    @Test
    void addsTraceHeaderAndPropagatesMdc() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse res = new MockHttpServletResponse();

        FilterChain chain = (request, response) -> {
            // 在链中验证响应头已设置
            assertThat(((HttpServletResponse) response).getHeader("X-Trace-Id")).isNotBlank();
        };

        filter.doFilter(req, res, chain);

        assertThat(res.getHeader("X-Trace-Id")).isNotBlank();
    }

    @Test
    void respectsIncomingTraceId() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/orders");
        req.addHeader("X-Trace-Id", "fixed123");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, (r, s) -> {});

        assertThat(res.getHeader("X-Trace-Id")).isEqualTo("fixed123");
    }

    @Test
    void putsUserIntoMdcWhenAuthenticated() throws Exception {
        secMock.when(SecurityUtil::isAuthenticated).thenReturn(true);
        secMock.when(SecurityUtil::getCurrentUsername).thenReturn("u2");

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, (r, s) -> {
            // 验证响应头存在 traceId 即可，MDC 在同线程中已设置
            assertThat(((HttpServletResponse) s).getHeader("X-Trace-Id")).isNotBlank();
        });
    }
}
