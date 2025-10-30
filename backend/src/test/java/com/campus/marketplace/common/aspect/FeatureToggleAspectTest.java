package com.campus.marketplace.common.aspect;

import com.campus.marketplace.common.annotation.FeatureToggle;
import com.campus.marketplace.common.context.CampusContextHolder;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.FeatureFlagService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeatureToggleAspect 单元测试")
class FeatureToggleAspectTest {

    @Mock
    private FeatureFlagService featureFlagService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private FeatureToggle toggle;

    @InjectMocks
    private FeatureToggleAspect aspect;

    @AfterEach
    void tearDown() {
        CampusContextHolder.clear();
    }

    @Test
    @DisplayName("特性开关开启时允许继续执行")
    void around_featureEnabled_allowsProceed() throws Throwable {
        ReflectionTestUtils.setField(aspect, "env", "prod");
        CampusContextHolder.setCampusId(100L);
        when(toggle.value()).thenReturn("experiment-beta");
        when(featureFlagService.isEnabled(eq("experiment-beta"), any(), eq(100L), eq("prod")))
                .thenReturn(true);
        when(joinPoint.proceed()).thenReturn("OK");

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isAuthenticated).thenReturn(true);

            Object result = aspect.around(joinPoint, toggle);

            assertThat(result).isEqualTo("OK");
        }
    }

    @Test
    @DisplayName("特性关闭且 failClosed=true 时抛出业务异常")
    void around_featureDisabled_throwsBusinessException() throws Throwable {
        ReflectionTestUtils.setField(aspect, "env", "test");
        CampusContextHolder.setCampusId(null);
        when(toggle.value()).thenReturn("new-checkout");
        when(toggle.failClosed()).thenReturn(true);
        when(featureFlagService.isEnabled(eq("new-checkout"), any(), any(), eq("test")))
                .thenReturn(false);
        when(messageSource.getMessage(eq("feature.disabled"), any(), eq(ErrorCode.FORBIDDEN.getMessage()), any(Locale.class)))
                .thenReturn("该功能暂未开放");

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::isAuthenticated).thenReturn(false);

            assertThatThrownBy(() -> aspect.around(joinPoint, toggle))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("该功能暂未开放");
        }
    }
}
