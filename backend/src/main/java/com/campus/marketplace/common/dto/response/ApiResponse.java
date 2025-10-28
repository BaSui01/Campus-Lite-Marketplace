package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应格式
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应包装结构")
public class ApiResponse<T> {

    /**
     * 状态码
     */
    @Schema(description = "业务状态码，0 表示成功")
    private int code;

    /**
     * 响应消息
     */
    @Schema(description = "人类可读的响应消息", example = "操作成功")
    private String message;

    /**
     * 响应数据
     */
    @Schema(description = "业务数据载荷", nullable = true)
    private T data;

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message(ErrorCode.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param message 消息
     * @param data 数据
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失败响应
     *
     * @param errorCode 错误码
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .data(null)
                .build();
    }

    /**
     * 失败响应（自定义消息）
     *
     * @param errorCode 错误码
     * @param message 自定义消息
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(message)
                .data(null)
                .build();
    }

    /**
     * 失败响应（自定义状态码和消息）
     *
     * @param code 状态码
     * @param message 消息
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
