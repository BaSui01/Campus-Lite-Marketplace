package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.RefundRequest;

/**
 * 退款/售后服务
 */
public interface RefundService {

    /**
     * 买家提交退款申请
     */
    String applyRefund(String orderNo, String reason, java.util.Map<String, Object> evidence);

    /**
     * 管理员审核通过并发起渠道退款
     */
    void approveAndRefund(String refundNo);

    /**
     * 管理员驳回退款
     */
    void reject(String refundNo, String reason);

    /**
     * 查询退款详情
     */
    RefundRequest getByRefundNo(String refundNo);

    /**
     * 渠道退款回调处理（幂等）
     *
     * @param orderNo 订单号
     * @param channel 支付渠道
     * @param success 是否成功
     * @param payload 回调负载
     * @return 是否处理成功
     */
    boolean handleRefundCallback(String orderNo, String channel, boolean success, java.util.Map<String, String> payload);
}
