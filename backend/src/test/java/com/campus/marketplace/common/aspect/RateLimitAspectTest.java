package com.campus.marketplace.common.aspect;

import com.campus.marketplace.common.annotation.RateLimit;
import com.campus.marketplace.common.component.RateLimitRuleManager;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.UserRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitAspect 单元测试")
class RateLimitAspectTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private RateLimit rateLimit;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private RateLimitRuleManager ruleManager;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RateLimitAspect aspect;

    @BeforeEach
    void setUp() {
        ruleManager = new RateLimitRuleManager();
        aspect = new RateLimitAspect(redisTemplate, ruleManager, userRepository);
        org.mockito.Mockito.lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("限流总开关关闭时直接放行")
    void around_ruleDisabled_proceeds() throws Throwable {
        ruleManager.setEnabled(false);
        configureRateLimitDefaults();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(joinPoint.proceed()).thenReturn("OK");

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
            Object result = aspect.around(joinPoint, rateLimit);
            assertThat(result).isEqualTo("OK");
        }

        verify(redisTemplate, never()).opsForZSet();
    }

    @Test
    @DisplayName("命中 IP 黑名单时抛出禁止访问")
    void around_blacklistedIp_throwsForbidden() throws Throwable {
        configureRateLimitDefaults();
        ruleManager.addIpBlacklist("203.0.113.5");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.5");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

            assertThatThrownBy(() -> aspect.around(joinPoint, rateLimit))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
        }

        verify(redisTemplate, never()).opsForZSet();
    }

    @Test
    @DisplayName("滑动窗口算法在额度内放行并写入 Redis")
    void around_slidingWindow_withinLimit() throws Throwable {
        configureRateLimitDefaults();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(1L);
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("RESULT");

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(42L);

            Object result = aspect.around(joinPoint, rateLimit);
            assertThat(result).isEqualTo("RESULT");
        }

        verify(zSetOperations).removeRangeByScore(anyString(), anyDouble(), anyDouble());
        verify(zSetOperations).count(anyString(), anyDouble(), anyDouble());
        verify(zSetOperations).add(anyString(), anyString(), anyDouble());
        verify(redisTemplate).expire(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
        assertThat(response.getHeader("RateLimit-Limit")).isEqualTo("3");
        assertThat(response.getHeader("RateLimit-Remaining")).isEqualTo("1");
    }

    @Test
    @DisplayName("滑动窗口算法触发限流时抛出 TOO_MANY_REQUESTS")
    void around_slidingWindow_exceedsLimit() throws Throwable {
        configureRateLimitDefaults();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(3L);

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(99L);

            assertThatThrownBy(() -> aspect.around(joinPoint, rateLimit))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);
        }

        assertThat(response.getHeader("RateLimit-Limit")).isEqualTo("3");
        assertThat(response.getHeader("RateLimit-Remaining")).isEqualTo("0");
        verify(zSetOperations, never()).add(anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("令牌桶算法有剩余令牌时放行并设置响应头")
    void around_tokenBucket_withTokens() throws Throwable {
        configureRateLimitDefaults();
        org.mockito.Mockito.lenient().when(rateLimit.algorithm()).thenReturn(RateLimit.Algorithm.TOKEN_BUCKET);
        org.mockito.Mockito.lenient().when(rateLimit.tokenBucketCapacity()).thenReturn(5);
        org.mockito.Mockito.lenient().when(rateLimit.refillTokens()).thenReturn(2);
        org.mockito.Mockito.lenient().when(rateLimit.refillInterval()).thenReturn(30L);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.10");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        when(redisTemplate.execute(any(), anyList(), any(), any(), any(), any()))
                .thenReturn(List.of(4L, System.currentTimeMillis()));
        when(joinPoint.proceed()).thenReturn("PASS");

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(2025L);

            Object result = aspect.around(joinPoint, rateLimit);
            assertThat(result).isEqualTo("PASS");
        }

        verify(redisTemplate).execute(any(), anyList(), any(), any(), any(), any());
        verify(redisTemplate).expire(anyString(), anyLong(), eq(TimeUnit.MILLISECONDS));
        assertThat(response.getHeader("RateLimit-Limit")).isEqualTo("5");
        assertThat(response.getHeader("RateLimit-Remaining")).isEqualTo("4");
        assertThat(response.getHeader("RateLimit-Reset")).isNotBlank();
    }

    @Test
    @DisplayName("令牌桶算法无令牌时抛出 TOO_MANY_REQUESTS")
    void around_tokenBucket_noTokens() throws Throwable {
        configureRateLimitDefaults();
        org.mockito.Mockito.lenient().when(rateLimit.algorithm()).thenReturn(RateLimit.Algorithm.TOKEN_BUCKET);
        org.mockito.Mockito.lenient().when(rateLimit.tokenBucketCapacity()).thenReturn(3);
        org.mockito.Mockito.lenient().when(rateLimit.refillTokens()).thenReturn(1);
        org.mockito.Mockito.lenient().when(rateLimit.refillInterval()).thenReturn(60L);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.11");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        when(redisTemplate.execute(any(), anyList(), any(), any(), any(), any()))
                .thenReturn(List.of(-1L, System.currentTimeMillis()));

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(88L);

            assertThatThrownBy(() -> aspect.around(joinPoint, rateLimit))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOO_MANY_REQUESTS);
        }

        assertThat(response.getHeader("RateLimit-Limit")).isEqualTo("3");
        assertThat(response.getHeader("RateLimit-Remaining")).isEqualTo("0");
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("X-Forwarded-For 首个 IP 在白名单时直接放行")
    void around_whitelist_viaForwardedFor() throws Throwable {
        configureRateLimitDefaults();
        ruleManager.addIpWhitelist("203.0.113.10");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(joinPoint.proceed()).thenReturn("ALLOW");

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(7L);

            Object result = aspect.around(joinPoint, rateLimit);
            assertThat(result).isEqualTo("ALLOW");
        }

        verify(redisTemplate, never()).opsForZSet();
        verify(redisTemplate, never()).execute(any(), anyList(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("X-Real-IP 在黑名单时抛出禁止访问")
    void around_blacklistedRealIp_forbidden() throws Throwable {
        configureRateLimitDefaults();
        ruleManager.addIpBlacklist("198.51.100.50");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "198.51.100.50");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(12L);

            assertThatThrownBy(() -> aspect.around(joinPoint, rateLimit))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
        }

        verify(redisTemplate, never()).opsForZSet();
    }

    @Test
    @DisplayName("未带代理头时使用 RemoteAddr 进行限流")
    void around_remoteAddrFallback_ipLimit() throws Throwable {
        configureRateLimitDefaults();
        org.mockito.Mockito.lenient().when(rateLimit.key()).thenReturn("");
        org.mockito.Mockito.lenient().when(rateLimit.limitType()).thenReturn(RateLimit.LimitType.IP);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.99");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("OK");
        MethodSignature signature = org.mockito.Mockito.mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(RateLimitAspectTest.class);
        when(signature.getName()).thenReturn("testMethod");

        try (MockedStatic<SecurityUtil> security = mockStatic(SecurityUtil.class)) {
            security.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            Object result = aspect.around(joinPoint, rateLimit);
            assertThat(result).isEqualTo("OK");
        }

        verify(redisTemplate, atLeastOnce()).opsForZSet();
        assertThat(response.getHeader("RateLimit-Limit")).isEqualTo("3");
    }

    private void configureRateLimitDefaults() {
        org.mockito.Mockito.lenient().when(rateLimit.key()).thenReturn("message:send");
        org.mockito.Mockito.lenient().when(rateLimit.limitType()).thenReturn(RateLimit.LimitType.USER);
        org.mockito.Mockito.lenient().when(rateLimit.maxRequests()).thenReturn(3);
        org.mockito.Mockito.lenient().when(rateLimit.timeWindow()).thenReturn(60L);
        org.mockito.Mockito.lenient().when(rateLimit.timeUnit()).thenReturn(TimeUnit.SECONDS);
        org.mockito.Mockito.lenient().when(rateLimit.algorithm()).thenReturn(RateLimit.Algorithm.SLIDING_WINDOW);
    }
}
