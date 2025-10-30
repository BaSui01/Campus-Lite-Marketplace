package com.campus.marketplace.common.exception;

import com.campus.marketplace.common.dto.response.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("全局异常处理国际化测试")
class GlobalExceptionHandlerI18nTest {

    private GlobalExceptionHandler newHandler() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        return new GlobalExceptionHandler(ms);
    }

    @AfterEach
    void clearLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    @DisplayName("英文环境返回英文错误信息")
    void englishMessage() {
        LocaleContextHolder.setLocale(Locale.US);
        GlobalExceptionHandler handler = newHandler();
        BusinessException ex = new BusinessException(ErrorCode.PERMISSION_DENIED);
        ResponseEntity<ApiResponse<Void>> resp = handler.handleBusinessException(ex);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals("Forbidden", resp.getBody().getMessage());
    }

    @Test
    @DisplayName("中文环境返回中文错误信息")
    void chineseMessage() {
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE);
        GlobalExceptionHandler handler = newHandler();
        BusinessException ex = new BusinessException(ErrorCode.PERMISSION_DENIED);
        ResponseEntity<ApiResponse<Void>> resp = handler.handleBusinessException(ex);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals("权限不足", resp.getBody().getMessage());
    }
}
