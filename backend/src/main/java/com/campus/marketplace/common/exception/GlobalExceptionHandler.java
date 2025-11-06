package com.campus.marketplace.common.exception;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.ErrorLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * 统一处理系统中的所有异常，返回标准的错误响应
 * 增加了错误日志记录功能
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    
    @Autowired(required = false)
    private ErrorLogService errorLogService;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // 兼容部分单元测试直接 new 的场景
    public GlobalExceptionHandler() {
        org.springframework.context.support.ResourceBundleMessageSource ms = new org.springframework.context.support.ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        this.messageSource = ms;
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        String msg = e.getCustomMessage() != null ? e.getCustomMessage()
                : resolveErrorMessage(e.getCode(), e.getErrorCode().getMessage());
        log.warn("业务异常: code={}, message={}", e.getCode(), msg);
        
        // 根据错误码返回适当的HTTP状态码
        if (e.getErrorCode() == ErrorCode.TOO_MANY_REQUESTS) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error(e.getCode(), msg));
        }
        if (e.getErrorCode() == ErrorCode.INVALID_PARAMETER || e.getErrorCode() == ErrorCode.INVALID_PARAM) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getCode(), msg));
        }
        
        return ResponseEntity.ok(ApiResponse.error(e.getCode(), msg));
    }

    /**
     * 处理加密解密异常
     * 
     * @param e CryptoException
     * @return 错误响应
     */
    @ExceptionHandler(CryptoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleCryptoException(CryptoException e) {
        log.error("❌ 密码解密失败: {}", e.getMessage());
        return ApiResponse.error(ErrorCode.PARAM_ERROR.getCode(), "密码格式错误，请重试");
    }

    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("参数校验失败: {}", errorMessage);
        return ApiResponse.error(ErrorCode.PARAM_ERROR.getCode(),
                resolveErrorMessage(ErrorCode.PARAM_ERROR.getCode(), errorMessage));
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("参数绑定失败: {}", errorMessage);
        return ApiResponse.error(ErrorCode.PARAM_ERROR.getCode(),
                resolveErrorMessage(ErrorCode.PARAM_ERROR.getCode(), errorMessage));
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String errorMessage = String.format("缺少必填参数: %s", e.getParameterName());
        log.warn("请求参数缺失: {}", errorMessage);
        return ApiResponse.error(ErrorCode.PARAM_ERROR.getCode(),
                resolveErrorMessage(ErrorCode.PARAM_ERROR.getCode(), errorMessage));
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("约束违反: {}", errorMessage);
        return ApiResponse.error(ErrorCode.PARAM_ERROR.getCode(),
                resolveErrorMessage(ErrorCode.PARAM_ERROR.getCode(), errorMessage));
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuthenticationException(AuthenticationException e) {
        log.warn("认证失败: {}", e.getMessage());
        return ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(),
                resolveErrorMessage(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage()));
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return ApiResponse.error(ErrorCode.PERMISSION_DENIED.getCode(),
                resolveErrorMessage(ErrorCode.PERMISSION_DENIED.getCode(), ErrorCode.PERMISSION_DENIED.getMessage()));
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return ApiResponse.error(ErrorCode.PARAM_ERROR.getCode(),
                resolveErrorMessage(ErrorCode.PARAM_ERROR.getCode(), e.getMessage()));
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        logError(e);
        return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(),
                resolveErrorMessage(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage()));
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常", e);
        logError(e);
        return ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(),
                resolveErrorMessage(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage()));
    }

    /**
     * 记录错误日志
     */
    private void logError(Throwable exception) {
        if (errorLogService == null) {
            return;
        }

        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                Map<String, Object> requestParams = extractRequestParams(request);
                errorLogService.logError(
                    exception,
                    request.getRequestURI(),
                    request.getMethod(),
                    getClientIp(request),
                    requestParams
                );
            }
        } catch (Exception e) {
            log.error("记录错误日志失败", e);
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 提取请求参数
     */
    private Map<String, Object> extractRequestParams(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (value.length == 1) {
                params.put(key, value[0]);
            } else {
                params.put(key, value);
            }
        });
        return params;
    }

    private String resolveErrorMessage(int code, String defaultMsg) {
        String key = "error." + code;
        return messageSource.getMessage(key, null, defaultMsg, LocaleContextHolder.getLocale());
    }
}
