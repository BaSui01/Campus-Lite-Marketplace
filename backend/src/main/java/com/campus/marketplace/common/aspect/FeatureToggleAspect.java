package com.campus.marketplace.common.aspect;

import com.campus.marketplace.common.annotation.FeatureToggle;
import com.campus.marketplace.common.context.CampusContextHolder;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Feature Toggle Aspect
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureToggleAspect {

    private final FeatureFlagService featureFlagService;
    private final MessageSource messageSource;

    @Value("${spring.profiles.active:dev}")
    private String env;

    @Around("@annotation(toggle)")
    public Object around(ProceedingJoinPoint pjp, FeatureToggle toggle) throws Throwable {
        String key = toggle.value();
        Long userId = null;
        try {
            if (SecurityUtil.isAuthenticated()) {
                // 按用户名作为 ID 也可，真实项目可替换
                // 这里不强依赖 getCurrentUserId()
                userId = null;
            }
        } catch (Exception ignored) { }
        Long campusId = CampusContextHolder.getCampusId();

        boolean enabled = featureFlagService.isEnabled(key, userId, campusId, env);
        if (!enabled && toggle.failClosed()) {
            String msg = messageSource.getMessage("feature.disabled", null,
                    ErrorCode.FORBIDDEN.getMessage(), LocaleContextHolder.getLocale());
            throw new BusinessException(ErrorCode.FORBIDDEN, msg);
        }
        return pjp.proceed();
    }
}
