package com.campus.marketplace.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 错误码枚举
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /**
     * 操作成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 用户未登录
     */
    UNAUTHORIZED(401, "用户未登录"),

    /**
     * 权限不足
     */
    PERMISSION_DENIED(403, "权限不足"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 资源不存在（通用）
     */
    RESOURCE_NOT_FOUND(404, "资源不存在"),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(40401, "用户不存在"),

    /**
     * 无效的操作
     */
    INVALID_OPERATION(400, "无效的操作"),

    /**
     * 操作失败
     */
    OPERATION_FAILED(500, "操作失败"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(500, "系统错误"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 无效的参数
     */
    INVALID_PARAMETER(400, "无效的参数"),

    /**
     * 无权限访问
     */
    FORBIDDEN(403, "无权限访问"),

    /**
     * 资源已存在
     */
    DUPLICATE_RESOURCE(409, "资源已存在"),

    /**
     * 支付订单创建失败
     */
    PAYMENT_CREATE_FAILED(50001, "支付订单创建失败"),

    /**
     * 支付方式不支持
     */
    PAYMENT_METHOD_NOT_SUPPORTED(50002, "不支持的支付方式"),

    /**
     * 支付签名验证失败
     */
    PAYMENT_VERIFY_FAILED(50003, "支付签名验证失败"),

    /**
     * 支付订单查询失败
     */
    PAYMENT_QUERY_FAILED(50005, "支付订单查询失败"),

    // ==================== BaSui 补充的遗漏错误码 ====================

    /**
     * Token 无效（任务 7 遗漏 - 已补充！）
     */
    TOKEN_INVALID(40101, "Token 无效或已过期"),

    /**
     * 用户被封禁（任务 29 遗漏 - 已补充！）
     */
    USER_BANNED(40301, "用户已被封禁，无法操作"),

    /**
     * 分类不存在（任务 7 遗漏 - 已补充！）
     */
    CATEGORY_NOT_FOUND(40402, "分类不存在"),

    /**
     * 物品不存在（任务 7 遗漏 - 已补充！）
     */
    GOODS_NOT_FOUND(40403, "物品不存在"),

    // ==================== 订单相关错误码 ====================

    /**
     * 订单不存在
     */
    ORDER_NOT_FOUND(40404, "订单不存在"),

    /**
     * 物品已售出
     */
    GOODS_ALREADY_SOLD(40001, "物品已售出"),

    /**
     * 物品未审核通过
     */
    GOODS_NOT_APPROVED(40002, "物品未审核通过"),

    /**
     * 不能购买自己的物品
     */
    CANNOT_BUY_OWN_GOODS(40003, "不能购买自己的物品"),

    /**
     * 订单已支付
     */
    ORDER_PAID(40004, "订单已支付"),

    /**
     * 订单已取消
     */
    ORDER_CANCELLED(40005, "订单已取消"),

    /**
     * 支付失败
     */
    PAYMENT_FAILED(50004, "支付失败"),

    // ==================== 用户相关错误码 ====================

    /**
     * 用户名已存在
     */
    USERNAME_EXISTS(40006, "用户名已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_EXISTS(40007, "邮箱已存在"),

    /**
     * 角色不存在
     */
    ROLE_NOT_FOUND(40405, "角色不存在"),

    /**
     * 密码错误
     */
    PASSWORD_ERROR(40008, "密码错误"),

    // ==================== 收藏相关错误码 ====================

    /**
     * 收藏已存在
     */
    FAVORITE_EXISTS(40009, "已收藏过该物品"),

    /**
     * 收藏不存在
     */
    FAVORITE_NOT_FOUND(40406, "收藏不存在"),

    // ==================== 积分相关错误码 ====================

    /**
     * 积分不足
     */
    POINTS_INSUFFICIENT(40010, "积分不足"),

    // ==================== 论坛相关错误码 ====================

    /**
     * 超过每日发帖限制
     */
    POST_LIMIT_EXCEEDED(40012, "超过每日发帖限制"),

    /**
     * 帖子不存在
     */
    POST_NOT_FOUND(40407, "帖子不存在"),

    // ==================== 其他错误码 ====================

    /**
     * 无效参数
     */
    INVALID_PARAM(40011, "无效参数"),

    // ==================== 限流相关错误码 ====================

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // ==================== 标签/订阅/隐私 ====================

    TAG_NOT_FOUND(40408, "标签不存在"),
    FOLLOW_NOT_FOUND(40409, "关注关系不存在"),
    SUBSCRIPTION_NOT_FOUND(40410, "订阅不存在"),
    PRIVACY_REQUEST_NOT_FOUND(40411, "隐私请求不存在"),
    PRIVACY_REQUEST_CONFLICT(40013, "存在未处理的隐私请求");

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误信息
     */
    private final String message;
}
