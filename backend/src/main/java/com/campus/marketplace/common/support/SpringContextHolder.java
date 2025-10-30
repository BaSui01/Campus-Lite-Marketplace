package com.campus.marketplace.common.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Spring 上下文持有者
 *
 * 支持在无法直接注入的静态工具类中获取 Bean。
 *
 * @author Codex
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        SpringContextHolder.applicationContext = context;
    }

    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext 未准备就绪，无法获取 Bean：" + clazz.getName());
        }
        return applicationContext.getBean(clazz);
    }
}
