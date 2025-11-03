package com.campus.marketplace.logistics;

/**
 * 物流API异常
 * <p>
 * 当调用快递公司API失败时抛出此异常。
 * 包含错误码、错误消息和原始异常信息。
 * </p>
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
public class LogisticsApiException extends RuntimeException {

    /**
     * 错误码（快递公司返回的错误码）
     */
    private final String errorCode;

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public LogisticsApiException(String message) {
        super(message);
        this.errorCode = "UNKNOWN";
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public LogisticsApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNKNOWN";
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误消息
     */
    public LogisticsApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误消息
     * @param cause     原始异常
     */
    public LogisticsApiException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
}
