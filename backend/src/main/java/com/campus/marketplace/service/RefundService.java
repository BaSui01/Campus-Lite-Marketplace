package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.enums.RefundStatus;
import org.springframework.data.domain.Page;

/**
 * 退款/售后服务
 *
 * @author BaSui
 * @date 2025-10-29
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
     * 查询退款详情（管理员）
     */
    RefundRequest getByRefundNo(String refundNo);

    /**
     * 用户查询自己的退款列表（分页 + 可选状态筛选）
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param status 退款状态（可选）
     * @return 分页退款列表
     */
    Page<RefundRequest> listMyRefunds(int page, int size, RefundStatus status);

    /**
     * 用户查询自己的退款详情
     *
     * @param refundNo 退款单号
     * @return 退款详情
     * @throws com.campus.marketplace.common.exception.BusinessException 如果不是自己的退款
     */
    RefundRequest getMyRefund(String refundNo);

    /**
     * 管理员查询所有退款列表（分页 + 筛选）
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param status 退款状态（可选）
     * @param keyword 搜索关键词（可选，匹配退款单号或订单号）
     * @return 分页退款列表
     */
    Page<RefundRequest> listAllRefunds(int page, int size, RefundStatus status, String keyword);

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
