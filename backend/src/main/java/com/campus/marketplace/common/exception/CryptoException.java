package com.campus.marketplace.common.exception;

/**
 * 加密解密异常
 * 
 * 用于密码加密解密过程中的异常处理
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */
public class CryptoException extends RuntimeException {
    
    /**
     * 构造函数
     * 
     * @param message 异常信息
     */
    public CryptoException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 异常信息
     * @param cause 原始异常
     */
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
