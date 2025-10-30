package com.campus.marketplace.common.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 自定义错误信息
     */
    private final String customMessage;

    /**
     * 构造函数 - 使用错误码默认信息
     *
     * @param errorCode 错误码
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    /**
     * 构造函数 - 使用自定义错误信息
     *
     * @param errorCode 错误码
     * @param customMessage 自定义错误信息
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    /**
     * 构造函数 - 带异常原因
     *
     * @param errorCode 错误码
     * @param customMessage 自定义错误信息
     * @param cause 异常原因
     */
    public BusinessException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    /**
     * 获取最终显示的错误信息
     *
     * @return 错误信息
     */
    public String getDisplayMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }

    /**
     * 获取错误码的数字代码（任务 GlobalExceptionHandler 遗漏 - 已补充！）
     *
     * @return 错误码
     */
    public int getCode() {
        return errorCode.getCode();
    }
}
