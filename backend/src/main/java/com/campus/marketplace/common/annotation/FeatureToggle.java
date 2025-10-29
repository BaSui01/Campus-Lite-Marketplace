package com.campus.marketplace.common.annotation;

import java.lang.annotation.*;

/**
 * Feature Toggle
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeatureToggle {
    String value();
    boolean failClosed() default true;
}
