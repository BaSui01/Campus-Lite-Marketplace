package com.campus.marketplace.common.exception;

import lombok.Getter;

/**
 * 错误码枚举
 * 
 * 定义系统所有错误码和错误消息
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Getter
public enum ErrorCode {

    // ============================================
    // 通用错误 (1000-1999)
    // ============================================
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(1000, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    NOT_FOUND(1002, "资源不存在"),
    OPERATION_FAILED(1003, "操作失败"),

    // ============================================
    // 用户相关错误 (2000-2999)
    // ============================================
    USER_NOT_FOUND(2000, "用户不存在"),
    USERNAME_EXISTS(2001, "用户名已存在"),
    EMAIL_EXISTS(2002, "邮箱已存在"),
    PASSWORD_ERROR(2003, "用户名或密码错误"),
    USER_BANNED(2004, "账号已被封禁"),
    UNAUTHORIZED(2005, "未授权，请先登录"),
    TOKEN_EXPIRED(2006, "Token 已过期"),
    TOKEN_INVALID(2007, "Token 无效"),

    // ============================================
    // 物品相关错误 (3000-3999)
    // ============================================
    GOODS_NOT_FOUND(3000, "物品不存在"),
    GOODS_SOLD(3001, "物品已售出"),
    GOODS_NOT_APPROVED(3002, "物品未审核通过"),
    GOODS_OFFLINE(3003, "物品已下架"),

    // ============================================
    // 订单相关错误 (4000-4999)
    // ============================================
    ORDER_NOT_FOUND(4000, "订单不存在"),
    ORDER_PAID(4001, "订单已支付"),
    ORDER_CANCELLED(4002, "订单已取消"),
    PAYMENT_FAILED(4003, "支付失败"),
    ORDER_TIMEOUT(4004, "订单已超时"),

    // ============================================
    // 消息相关错误 (5000-5999)
    // ============================================
    MESSAGE_NOT_FOUND(5000, "消息不存在"),
    RECALL_TIMEOUT(5001, "消息已超过撤回时限"),
    BLOCKED_USER(5002, "无法发送消息，对方已将您拉黑"),
    CONVERSATION_NOT_FOUND(5003, "会话不存在"),

    // ============================================
    // 权限相关错误 (6000-6999)
    // ============================================
    PERMISSION_DENIED(6000, "权限不足"),
    ROLE_NOT_FOUND(6001, "角色不存在"),

    // ============================================
    // 业务相关错误 (7000-7999)
    // ============================================
    FAVORITE_EXISTS(7000, "已收藏该物品"),
    FAVORITE_NOT_FOUND(7001, "收藏不存在"),
    CATEGORY_NOT_FOUND(7002, "分类不存在"),
    POST_NOT_FOUND(7003, "帖子不存在"),
    COUPON_NOT_FOUND(7004, "优惠券不存在"),
    COUPON_EXPIRED(7005, "优惠券已过期"),
    COUPON_USED(7006, "优惠券已使用"),
    POINTS_INSUFFICIENT(7007, "积分不足"),

    // ============================================
    // 限流相关错误 (8000-8999)
    // ============================================
    RATE_LIMIT_EXCEEDED(8000, "请求过于频繁，请稍后再试"),
    DAILY_LIMIT_EXCEEDED(8001, "今日操作次数已达上限");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
